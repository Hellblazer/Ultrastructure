/**
 * Copyright (c) 2015 Chiral Behaviors, LLC, all rights reserved.
 *

 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.CoRE.phantasm.service.commands;

import java.io.File;
import java.io.FileOutputStream;

import org.jooq.DSLContext;
import org.jooq.impl.DSL;

import com.chiralbehaviors.CoRE.json.CoREModule;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.models.ModelImpl;
import com.chiralbehaviors.CoRE.utils.CoreDbConfiguration;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.dropwizard.cli.Command;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * @author hhildebrand
 *
 */
public class SnapshotCommand extends Command {

    public SnapshotCommand() {
        super("snap", "Capture the snapshot state of the CoRE instance");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("file")
                 .nargs("?")
                 .help("State snapshot output file");
    }

    @Override
    public void run(Bootstrap<?> bootstrap,
                    Namespace namespace) throws Exception {
        CoreDbConfiguration config = new CoreDbConfiguration();
        config.initializeFromEnvironment();
        DSLContext create = DSL.using(config.getCoreConnection());
        try (Model model = new ModelImpl(create)) {
            create.transaction(c -> {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new CoREModule());
                try (FileOutputStream os = new FileOutputStream(new File(namespace.getString("file")))) {
                    objectMapper.writeValue(os, model.snapshot());
                }
            });
        }
    }
}
