/*
 * Copyright 2014-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package com.facebook.buck.jvm.java;

import static org.junit.Assert.assertEquals;

import com.facebook.buck.io.BuildCellRelativePath;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.jvm.core.JavaPackageFinder;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.BuildTargetFactory;
import com.facebook.buck.rules.BuildContext;
import com.facebook.buck.rules.DefaultSourcePathResolver;
import com.facebook.buck.rules.FakeBuildContext;
import com.facebook.buck.rules.FakeSourcePath;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.rules.TestBuildRuleResolver;
import com.facebook.buck.step.Step;
import com.facebook.buck.step.fs.MkdirStep;
import com.facebook.buck.step.fs.SymlinkFileStep;
import com.facebook.buck.testutil.FakeProjectFilesystem;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import org.junit.Test;

public class CopyResourcesStepTest {
  @Test
  public void testAddResourceCommandsWithBuildFileParentOfSrcDirectory()
      throws InterruptedException {
    SourcePathRuleFinder ruleFinder = new SourcePathRuleFinder(new TestBuildRuleResolver());
    SourcePathResolver resolver = DefaultSourcePathResolver.from(ruleFinder);
    // Files:
    // android/java/BUCK
    // android/java/src/com/facebook/base/data.json
    // android/java/src/com/facebook/common/util/data.json
    ProjectFilesystem filesystem = FakeProjectFilesystem.createJavaOnlyFilesystem();
    BuildTarget buildTarget =
        BuildTargetFactory.newInstance(filesystem.getRootPath(), "//android/java:resources");
    JavaPackageFinder javaPackageFinder = createJavaPackageFinder();

    BuildContext buildContext =
        FakeBuildContext.withSourcePathResolver(resolver)
            .withJavaPackageFinder(javaPackageFinder)
            .withBuildCellRootPath(filesystem.getRootPath());

    CopyResourcesStep step =
        new CopyResourcesStep(
            filesystem,
            buildContext,
            ruleFinder,
            buildTarget,
            ResourcesParameters.builder()
                .setResources(
                    ImmutableSortedSet.of(
                        FakeSourcePath.of(
                            filesystem, "android/java/src/com/facebook/base/data.json"),
                        FakeSourcePath.of(
                            filesystem, "android/java/src/com/facebook/common/util/data.json")))
                .setResourcesRoot(Optional.empty())
                .build(),
            filesystem
                .getBuckPaths()
                .getScratchDir()
                .resolve("android/java/lib__resources__classes"));

    Path target =
        filesystem
            .getBuckPaths()
            .getScratchDir()
            .resolve("android/java/lib__resources__classes/com/facebook/common/util/data.json");
    Path target1 =
        filesystem
            .getBuckPaths()
            .getScratchDir()
            .resolve("android/java/lib__resources__classes/com/facebook/base/data.json");
    List<? extends Step> expected =
        ImmutableList.of(
            MkdirStep.of(
                BuildCellRelativePath.fromCellRelativePath(
                    buildContext.getBuildCellRootPath(), filesystem, target1.getParent())),
            SymlinkFileStep.builder()
                .setFilesystem(filesystem)
                .setExistingFile(filesystem.resolve("android/java/src/com/facebook/base/data.json"))
                .setDesiredLink(target1)
                .build(),
            MkdirStep.of(
                BuildCellRelativePath.fromCellRelativePath(
                    buildContext.getBuildCellRootPath(), filesystem, target.getParent())),
            SymlinkFileStep.builder()
                .setFilesystem(filesystem)
                .setExistingFile(
                    filesystem.resolve("android/java/src/com/facebook/common/util/data.json"))
                .setDesiredLink(target)
                .build());
    assertEquals(expected, step.buildSteps());
  }

  @Test
  public void testAddResourceCommandsWithBuildFileParentOfJavaPackage()
      throws InterruptedException {
    // Files:
    // android/java/src/BUCK
    // android/java/src/com/facebook/base/data.json
    // android/java/src/com/facebook/common/util/data.json
    BuildTarget buildTarget = BuildTargetFactory.newInstance("//android/java/src:resources");
    JavaPackageFinder javaPackageFinder = createJavaPackageFinder();
    ProjectFilesystem filesystem = FakeProjectFilesystem.createJavaOnlyFilesystem();

    SourcePathRuleFinder ruleFinder = new SourcePathRuleFinder(new TestBuildRuleResolver());
    BuildContext buildContext =
        FakeBuildContext.withSourcePathResolver(DefaultSourcePathResolver.from(ruleFinder))
            .withJavaPackageFinder(javaPackageFinder)
            .withBuildCellRootPath(filesystem.getRootPath());
    CopyResourcesStep step =
        new CopyResourcesStep(
            filesystem,
            buildContext,
            ruleFinder,
            buildTarget,
            ResourcesParameters.builder()
                .setResources(
                    ImmutableSortedSet.<SourcePath>of(
                        FakeSourcePath.of(
                            filesystem, "android/java/src/com/facebook/base/data.json"),
                        FakeSourcePath.of(
                            filesystem, "android/java/src/com/facebook/common/util/data.json")))
                .setResourcesRoot(Optional.empty())
                .build(),
            filesystem
                .getBuckPaths()
                .getScratchDir()
                .resolve("android/java/src/lib__resources__classes"));

    Path target =
        filesystem
            .getBuckPaths()
            .getScratchDir()
            .resolve("android/java/src/lib__resources__classes/com/facebook/common/util/data.json");
    Path target1 =
        filesystem
            .getBuckPaths()
            .getScratchDir()
            .resolve("android/java/src/lib__resources__classes/com/facebook/base/data.json");
    List<? extends Step> expected =
        ImmutableList.of(
            MkdirStep.of(
                BuildCellRelativePath.fromCellRelativePath(
                    buildContext.getBuildCellRootPath(), filesystem, target1.getParent())),
            SymlinkFileStep.builder()
                .setFilesystem(filesystem)
                .setExistingFile(filesystem.resolve("android/java/src/com/facebook/base/data.json"))
                .setDesiredLink(target1)
                .build(),
            MkdirStep.of(
                BuildCellRelativePath.fromCellRelativePath(
                    buildContext.getBuildCellRootPath(), filesystem, target.getParent())),
            SymlinkFileStep.builder()
                .setFilesystem(filesystem)
                .setExistingFile(
                    filesystem.resolve("android/java/src/com/facebook/common/util/data.json"))
                .setDesiredLink(target)
                .build());
    assertEquals(expected, step.buildSteps());
  }

  @Test
  public void testAddResourceCommandsWithBuildFileInJavaPackage() throws InterruptedException {
    // Files:
    // android/java/src/com/facebook/BUCK
    // android/java/src/com/facebook/base/data.json
    // android/java/src/com/facebook/common/util/data.json
    BuildTarget buildTarget =
        BuildTargetFactory.newInstance("//android/java/src/com/facebook:resources");
    JavaPackageFinder javaPackageFinder = createJavaPackageFinder();
    ProjectFilesystem filesystem = FakeProjectFilesystem.createJavaOnlyFilesystem();

    SourcePathRuleFinder ruleFinder = new SourcePathRuleFinder(new TestBuildRuleResolver());
    BuildContext buildContext =
        FakeBuildContext.withSourcePathResolver(DefaultSourcePathResolver.from(ruleFinder))
            .withJavaPackageFinder(javaPackageFinder)
            .withBuildCellRootPath(filesystem.getRootPath());

    CopyResourcesStep step =
        new CopyResourcesStep(
            filesystem,
            buildContext,
            ruleFinder,
            buildTarget,
            ResourcesParameters.builder()
                .setResources(
                    ImmutableSortedSet.of(
                        FakeSourcePath.of(
                            filesystem, "android/java/src/com/facebook/base/data.json"),
                        FakeSourcePath.of(
                            filesystem, "android/java/src/com/facebook/common/util/data.json")))
                .setResourcesRoot(Optional.empty())
                .build(),
            filesystem
                .getBuckPaths()
                .getScratchDir()
                .resolve("android/java/src/com/facebook/lib__resources__classes"));

    Path target =
        filesystem
            .getBuckPaths()
            .getScratchDir()
            .resolve(
                "android/java/src/com/facebook/lib__resources__classes/"
                    + "com/facebook/common/util/data.json");
    Path target1 =
        filesystem
            .getBuckPaths()
            .getScratchDir()
            .resolve(
                "android/java/src/com/facebook/lib__resources__classes/"
                    + "com/facebook/base/data.json");
    List<? extends Step> expected =
        ImmutableList.of(
            MkdirStep.of(
                BuildCellRelativePath.fromCellRelativePath(
                    buildContext.getBuildCellRootPath(), filesystem, target1.getParent())),
            SymlinkFileStep.builder()
                .setFilesystem(filesystem)
                .setExistingFile(filesystem.resolve("android/java/src/com/facebook/base/data.json"))
                .setDesiredLink(target1)
                .build(),
            MkdirStep.of(
                BuildCellRelativePath.fromCellRelativePath(
                    buildContext.getBuildCellRootPath(), filesystem, target.getParent())),
            SymlinkFileStep.builder()
                .setFilesystem(filesystem)
                .setExistingFile(
                    filesystem.resolve("android/java/src/com/facebook/common/util/data.json"))
                .setDesiredLink(target)
                .build());
    assertEquals(expected, step.buildSteps());
  }

  private JavaPackageFinder createJavaPackageFinder() {
    return DefaultJavaPackageFinder.createDefaultJavaPackageFinder(
        ImmutableSet.of("/android/java/src"));
  }
}
