/** 
 * (C) Copyright 2012 Chiral Behaviors, LLC. All Rights Reserved
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
 * limitations under the License.
 */
package com.chiralbehaviors.CoRE.generators;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.reflections.Reflections;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.chiralbehaviors.CoRE.Ruleform;

/**
 * A class for generating a file for deserializing ruleforms. If you want your
 * ruleform deserialized polymorphically, it MUST descend from Ruleform in order
 * to be correctly deserialized.
 * 
 * For now, we're only deserializing to concrete types, but if you need an
 * abstract type, just add it in.
 * 
 * @author hparry
 * 
 * @goal generate
 * 
 * @phase generate-sources
 * 
 *        //
 */
//@Mojo(name = "mixin-generator")
//@Execute(goal = "generate", phase = LifecyclePhase.GENERATE_SOURCES)
public class PolymorphicMixinGenerator extends AbstractMojo {

    /**
     * @parameter
     */
    private String className;

    /**
     * @parameter
     */
    private File   outputDirectory;

    /**
     * @parameter
     */
    private String packageName;

    public PolymorphicMixinGenerator() {
    }

    public PolymorphicMixinGenerator(String packageName, File outputDirectory,
                                     String className) {
        this.packageName = packageName;
        this.outputDirectory = outputDirectory;
        this.className = className;
    }

    public void execute() throws MojoExecutionException {
        System.out.println(String.format("args: %s, %s, %s", packageName,
                                         outputDirectory, className));
        File file = new File(outputDirectory,
                             String.format("%s/%s.java",
                                           packageName.replace('.', '/'),
                                           className));
        File parentDir = new File(outputDirectory,
                                  packageName.replace('.', '/'));
        try {
            Files.createDirectories(parentDir.toPath());
        } catch (IOException e) {
            throw new MojoExecutionException(
                                             String.format("Cannot create parent directories %s",
                                                           file.getParent()), e);
        }
        final Map<String, Class<?>> entityMap = new HashMap<String, Class<?>>();
        Set<String> imports = new HashSet<String>();
        List<AnnotationValue> annotations = new ArrayList<AnnotationValue>();

        Reflections reflections = new Reflections(
                                                  Ruleform.class.getPackage().getName());

        for (Class<? extends Ruleform> form : reflections.getSubTypesOf(Ruleform.class)) {
            if (!Modifier.isAbstract(form.getModifiers())) {
                Class<?> prev = entityMap.put(form.getSimpleName(), form);
                assert prev == null : String.format("Found previous mapping %s of: %s",
                                                    prev, form);
                imports.add(form.getCanonicalName());
                annotations.add(new AnnotationValue(
                                                    form.getSimpleName(),
                                                    form.getSimpleName().toLowerCase()));
            }
        }

        STGroup group = new STGroupFile("templates/polymorphicmixin.stg");
        ST mixin = group.getInstanceOf("mixinclass");
        mixin.add("importdecs", imports);
        mixin.add("annotations", annotations);

        FileOutputStream os;
        try {
            Files.deleteIfExists(file.toPath());
            os = new FileOutputStream(Files.createFile(file.toPath()).toFile());
        } catch (FileNotFoundException e) {
            throw new MojoExecutionException(
                                             String.format("Cannot find file for create %s",
                                                           file.getAbsolutePath(),
                                                           e));
        } catch (IOException e) {

            throw new MojoExecutionException(
                                             String.format("Error creating file %s\nCause: %s",
                                                           file.getAbsolutePath(),
                                                           e.getMessage()));
        }
        try {
            os.write(mixin.render().getBytes());
            os.close();
        } catch (IOException e) {
            throw new MojoExecutionException(
                                             String.format("Error writing file %s",
                                                           file.getAbsolutePath(),
                                                           e));
        }
    }
}
