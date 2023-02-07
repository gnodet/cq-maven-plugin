/*
 * Copyright (c) 2020 CQ Maven Plugin
 * project contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.l2x6.cq.maven;

import java.io.File;
import java.nio.charset.Charset;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.l2x6.pom.tuner.PomTransformer;
import org.l2x6.pom.tuner.PomTransformer.SimpleElementWhitespace;
import org.l2x6.pom.tuner.PomTransformer.TransformationContext;
import org.l2x6.pom.tuner.PomTunerUtils;

/**
 * Flattens the dependency management section of the current pom.xml file.
 *
 * @since 3.5.0
 */
@Mojo(name = "remove-exclusions", threadSafe = true, requiresProject = true)
public class RemoveExclusionsMojo extends AbstractMojo {

    /**
     * Directory where the changes should be performed. Default is the current directory of the current Java process.
     *
     * @since 3.5.0
     */
    @Parameter(property = "cq.basedir", defaultValue = "${project.basedir}")
    File basedir;

    /**
     * Encoding to read and write files in the current source tree
     *
     * @since 3.5.0
     */
    @Parameter(defaultValue = "${project.build.sourceEncoding}", required = true, property = "project.build.sourceEncoding")
    String encoding;
    Charset charset;

    /**
     * Skip the execution of this mojo.
     *
     * @since 3.5.0
     */
    @Parameter(property = "cq.remove-exclusions.skip", defaultValue = "false")
    boolean skip;

    /**
     * How to format simple XML elements ({@code <elem/>}) - with or without space before the slash.
     *
     * @since 3.5.0
     */
    @Parameter(property = "cq.simpleElementWhitespace", defaultValue = "EMPTY")
    SimpleElementWhitespace simpleElementWhitespace;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException {
        if (skip) {
            getLog().info("Skipping as requested by the user");
            return;
        }
        charset = Charset.forName(encoding);

        new PomTransformer(basedir.toPath().resolve("pom.xml"), charset, simpleElementWhitespace)
                .transform((org.w3c.dom.Document doc, TransformationContext context) -> {
                    context.removeNodes(
                            PomTunerUtils.anyNs("project", "dependencyManagement", "dependencies", "dependency", "exclusions"),
                            TransformationContext.removePrecedingCommentsAndWhiteSpace(true, true));

                    context.removeNodes(
                            "/" + PomTunerUtils.anyNs("configuration", "bomEntryTransformations", "bomEntryTransformation"),
                            TransformationContext.removePrecedingCommentsAndWhiteSpace(true, true));

                });
    }

}