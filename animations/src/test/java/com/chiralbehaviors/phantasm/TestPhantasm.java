/**
 * Copyright (c) 2015 Chiral Behaviors, LLC, all rights reserved.
 * 
 
 * This file is part of Ultrastructure.
 *
 *  Ultrastructure is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 *  ULtrastructure is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Ultrastructure.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.chiralbehaviors.phantasm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.junit.Test;

import com.chiralbehaviors.CoRE.meta.models.AbstractModelTest;
import com.chiralbehaviors.CoRE.workspace.dsl.WorkspaceImporter;
import com.chiralbehaviors.CoRE.workspace.dsl.WorkspaceLexer;
import com.chiralbehaviors.CoRE.workspace.dsl.WorkspaceParser;
import com.chiralbehaviors.CoRE.workspace.dsl.WorkspaceParser.WorkspaceContext;
import com.chiralbehaviors.CoRE.workspace.dsl.WorkspacePresentation;
import com.chiralbehaviors.phantasm.demo.Thing1;

/**
 * @author hhildebrand
 *
 */
public class TestPhantasm extends AbstractModelTest {

    @Test
    public void testDemo() throws Exception {
        WorkspaceLexer l = new WorkspaceLexer(
                                              new ANTLRInputStream(
                                                                   getClass().getResourceAsStream("/thing.wsp")));
        WorkspaceParser p = new WorkspaceParser(new CommonTokenStream(l));
        p.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer,
                                    Object offendingSymbol, int line,
                                    int charPositionInLine, String msg,
                                    RecognitionException e) {
                throw new IllegalStateException("failed to parse at line "
                                                + line + " due to " + msg, e);
            }
        });
        WorkspaceContext ctx = p.workspace();

        WorkspaceImporter importer = new WorkspaceImporter(
                                                           new WorkspacePresentation(
                                                                                     ctx),
                                                           model);
        em.getTransaction().begin();
        importer.loadWorkspace();
        em.flush();

        Thing1 thing1 = model.construct(Thing1.class, "testy", "test",
                                        kernel.getCore());
        assertNotNull(thing1);
        assertEquals(thing1, thing1.doSomethingElse());
        assertNotNull(thing1.getRuleform());
        assertEquals(thing1.getRuleform().getName(), thing1.getName());
    }

}
