package com.modpackswork.ageofrobotics.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** Guards that the repo's CI definition and docs actually describe building this mod (issue #4). */
class BuildPipelineTest {

  private static final Path REPO_ROOT = Path.of(System.getProperty("aor.repoRoot", "."));

  private static String read(String relativePath) throws IOException {
    final Path path = REPO_ROOT.resolve(relativePath);
    assertTrue(Files.isRegularFile(path), "expected file to exist: " + path);
    return Files.readString(path, StandardCharsets.UTF_8);
  }

  @Test
  void gradleWrapperIsCommitted() {
    for (final String file :
        new String[] {
          "gradlew", "gradlew.bat",
          "gradle/wrapper/gradle-wrapper.jar", "gradle/wrapper/gradle-wrapper.properties"
        }) {
      assertTrue(
          Files.isRegularFile(REPO_ROOT.resolve(file)),
          "wrapper file must be committed so CI can build without a preinstalled Gradle: " + file);
    }
  }

  @Test
  void azurePipelineBuildsTheModInsteadOfEchoingAPlaceholder() throws IOException {
    final String pipeline = read("azure-pipelines.yml");

    assertFalse(
        pipeline.toLowerCase(java.util.Locale.ROOT).contains("placeholder"),
        "azure-pipelines.yml is still the stub from the initial commit");
    assertTrue(pipeline.contains("./gradlew"), "pipeline must invoke the Gradle wrapper");
    assertTrue(pipeline.contains("build"), "pipeline must run the build task");
    assertTrue(
        pipeline.contains("JavaToolInstaller@0") || pipeline.contains("jdkVersionOption"),
        "pipeline must provision the JDK the mod targets");
  }

  @Test
  void readmeDocumentsTheProjectAndHowToBuildIt() throws IOException {
    final String readme = read("README.md").toLowerCase(java.util.Locale.ROOT);

    assertTrue(readme.contains("age of robotics"), "README must name the project");
    assertTrue(readme.contains("neoforge"), "README must state the mod loader");
    assertTrue(readme.contains("1.21.1"), "README must state the Minecraft version");
    assertTrue(readme.contains("gradlew build"), "README must document the build command");
  }
}
