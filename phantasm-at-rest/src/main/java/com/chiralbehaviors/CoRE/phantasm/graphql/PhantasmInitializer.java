/**
 * Copyright (c) 2016 Chiral Behaviors, LLC, all rights reserved.
 *

 *  This file is part of Ultrastructure.
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

package com.chiralbehaviors.CoRE.phantasm.graphql;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.chiralbehaviors.CoRE.phantasm.Phantasm;
import com.chiralbehaviors.CoRE.phantasm.java.annotations.Facet;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

public class PhantasmInitializer implements DataFetcher {
    private final Method method;
    private final Object plugin;

    public PhantasmInitializer(Method method, Object plugin) {
        this.method = method;
        this.plugin = plugin;
    }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        if (environment.getSource() == null) {
            return null;
        }
        try {
            Object result = method.invoke(plugin, environment);
            return result != null && method.getReturnType()
                                           .isAnnotationPresent(Facet.class) ? ((Phantasm) result).getRuleform()
                                                                             : result;
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }
}
