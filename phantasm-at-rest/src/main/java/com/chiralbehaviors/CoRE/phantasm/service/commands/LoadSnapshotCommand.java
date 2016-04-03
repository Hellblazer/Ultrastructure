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

import java.io.InputStream;

import org.jooq.DSLContext;

import com.chiralbehaviors.CoRE.json.CoREModule;
import com.chiralbehaviors.CoRE.meta.Model;
import com.chiralbehaviors.CoRE.meta.models.ModelImpl;
import com.chiralbehaviors.CoRE.phantasm.service.config.PhantasmConfiguration;
import com.chiralbehaviors.CoRE.workspace.StateSnapshot;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hellblazer.utils.Utils;

import io.dropwizard.cli.ConfiguredCommand;
import io.dropwizard.setup.Bootstrap;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

/**
 * @author hhildebrand
 *
 */
public class LoadSnapshotCommand
        extends ConfiguredCommand<PhantasmConfiguration> {

    public LoadSnapshotCommand() {
        super("load-snap", "load snapsot state into the CoRE instance");
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("file")
                 .nargs("?")
                 .help("State snapshot file");
    }

    @Override
    public void run(Bootstrap<PhantasmConfiguration> bootstrap,
                    Namespace namespace,
                    PhantasmConfiguration configuration) throws Exception {
        DSLContext create = configuration.create();
        create.transaction(c -> {
            try (Model model = new ModelImpl(create)) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.registerModule(new CoREModule());
                try (InputStream is = Utils.resolveResource(getClass(),
                                                            namespace.getString("file"))) {
                    StateSnapshot snapshot = objectMapper.readValue(is,
                                                                    StateSnapshot.class);
                    snapshot.load(model.create());
                }
            }
        });
    }

}
