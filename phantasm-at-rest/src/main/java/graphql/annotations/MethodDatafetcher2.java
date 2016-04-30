/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

class MethodDataFetcher2 implements DataFetcher {
    private final Method                                              method;
    private final int                                                 envIndex;
    private final Map<Integer, Function<Map<String, Object>, Object>> inputTxfms;

    public MethodDataFetcher2(Method method,
                              Map<Integer, Function<Map<String, Object>, Object>> inputTxfms) {
        this.method = method;
        List<Class<?>> parameterTypes = Arrays.asList(method.getParameters())
                                              .stream()
                                              .map(Parameter::getType)
                                              .collect(Collectors.toList());
        envIndex = parameterTypes.indexOf(DataFetchingEnvironment.class);
        this.inputTxfms = inputTxfms;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Object get(DataFetchingEnvironment environment) {
        if (environment.getSource() == null)
            return null;
        ArrayList<Object> args = new ArrayList<>(environment.getArguments()
                                                            .values());
        if (envIndex >= 0) {
            args.add(envIndex, environment);
        }
        Object[] argv = args.toArray();
        for (int i = 0; i < args.size(); i++) {
            Function<Map<String, Object>, Object> txfm = inputTxfms.get(i);
            if (txfm != null) {
                argv[i] = txfm.apply((Map<String, Object>) argv[i]);
            }
        }
        try {
            return method.invoke(environment.getSource(), argv);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e.getTargetException());
        }
    }
}
