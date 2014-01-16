/**
 * Copyright (C) 2014 Halloran Parry. All rights reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.hellblazer.CoRE.generators;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.reflections.Reflections;
import org.stringtemplate.v4.ST;
import org.stringtemplate.v4.STGroup;
import org.stringtemplate.v4.STGroupFile;

import com.hellblazer.CoRE.Ruleform;

/**
 * A class for generating a file for deserializing ruleforms. If you want your ruleform deserialized
 * polymorphically, it MUST descend from Ruleform in order to be correctly deserialized.
 * 
 * For now, we're only deserializing to concrete types, but if you need an
 * abstract type, just add it in.
 * 
 * @author hparry
 * 
 */
public class PolymorphicMixinGenerator {
    static final String outputDir = "target/generated-sources/com/hellblazer/CoRE/generated/";
    static final String filename = "PolymorphicRuleformMixin.java";
    public static void main(String[] args) throws IOException {
        

        Files.deleteIfExists(Paths.get(outputDir + filename));
        Files.createDirectories(Paths.get(outputDir));
        final Map<String, Class<? extends Ruleform>> entityMap = new HashMap<String, Class<? extends Ruleform>>();
        List<String> imports = new LinkedList<String>();
        List<AnnotationValue> annotations = new LinkedList<AnnotationValue>();
        
        Reflections reflections = new Reflections(
                                                  Ruleform.class.getPackage().getName());
        
        for (Class<? extends Ruleform> form : reflections.getSubTypesOf(Ruleform.class)) {
            if (!Modifier.isAbstract(form.getModifiers())) {
                Class<?> prev = entityMap.put(form.getSimpleName(), form);
                assert prev == null : String.format("Found previous mapping %s of: %s",
                                                    prev, form);
                imports.add(form.getCanonicalName());
                annotations.add(new AnnotationValue(form.getSimpleName(), form.getSimpleName().toLowerCase()));
            }
        }

        STGroup group = new STGroupFile(
                                        "src/main/resources/templates/polymorphicmixin.stg");
        ST mixin = group.getInstanceOf("mixinclass");
        mixin.add("importdecs", imports);
     
        
//        The template has a bunch of comma separated annotations,
//        except the last annotation doesn't end with a comma. I'm
//        sure there's a better way, but this works for now.
        if (annotations.size() > 1) {
            mixin.add("annotations", annotations.subList(0, annotations.size() -2));
        }
        mixin.add("lasta", annotations.get(annotations.size() - 1));
        Path p = Paths.get(outputDir + filename);

        FileOutputStream os = new FileOutputStream(Files.createFile(p).toFile());
        os.write(mixin.render().getBytes());
        os.close();
    }
}
