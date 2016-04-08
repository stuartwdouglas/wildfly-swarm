/**
 * Copyright 2015-2016 Red Hat, Inc, and individual contributors.
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
package org.wildfly.swarm.plugin.maven;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.wildfly.swarm.tools.exec.SwarmExecutor;

import java.io.File;

/**
 * Mojo that runs a swarm app with Fakereplace enabled. This allows for classes to be hot replaced at Runtime.
 */
@Mojo(name = "fakereplace",
        requiresDependencyResolution = ResolutionScope.COMPILE_PLUS_RUNTIME,
        requiresDependencyCollection = ResolutionScope.COMPILE_PLUS_RUNTIME)
@Execute(phase = LifecyclePhase.PACKAGE)
public class FakereplaceMojo extends StartMojo {

    public static final String ORG_FAKEREPLACE = "org.fakereplace";
    public static final String FAKEREPLACE_DIST = "fakereplace-dist";

    public FakereplaceMojo() {
        waitForProcess = true;
    }

    protected void setup(SwarmExecutor executor) throws MojoFailureException {
        Artifact fakereplace = null;
        for(Artifact artifact : project.getArtifacts()) {
            if(artifact.getGroupId().equals(ORG_FAKEREPLACE) && artifact.getArtifactId().equals(FAKEREPLACE_DIST)) {
                fakereplace = artifact;
                break;
            }
        }
        if(fakereplace == null) {
            throw new MojoFailureException("Fakereplace not found, please add it to your projects dependencies with scope <provided>");
        }
        String moduleName = project.getBuild().getFinalName() + "." + project.getPackaging();
        executor.withJVMArgument("-Xbootclasspath/a:" + fakereplace.getFile().getAbsolutePath());
        executor.withJVMArgument("-javaagent:" + fakereplace.getFile().getAbsolutePath());
        String arg = "-Dfakereplace.source-paths." + moduleName + "=" + project.getCompileSourceRoots().stream().collect(StringBuilder::new, (sb, s) -> {
            sb.append(s);
            sb.append(",");
        }, StringBuilder::append);
        executor.withJVMArgument(arg);

    }
}
