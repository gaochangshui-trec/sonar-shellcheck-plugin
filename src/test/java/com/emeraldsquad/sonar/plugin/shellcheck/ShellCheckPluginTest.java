package com.emeraldsquad.sonar.plugin.shellcheck;

import org.junit.Test;
import org.sonar.api.Plugin;
import org.sonar.api.SonarRuntime;
import org.sonar.api.internal.SonarRuntimeImpl;
import org.sonar.api.utils.Version;

import static org.assertj.core.api.Assertions.assertThat;

public class ShellCheckPluginTest {

    private static final Version VERSION_6_7 = Version.create(6, 7);
    private static final Version VERSION_7_2 = Version.create(7, 2);
    private static final Version VERSION_7_4 = Version.create(7, 4);
    private ShellCheckPlugin shellCheckPlugin = new ShellCheckPlugin();

    @Test
    public void sonarLint_6_7_extensions() {
        SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(VERSION_6_7);
        Plugin.Context context = new Plugin.Context(runtime);
        shellCheckPlugin.define(context);
        assertThat(context.getExtensions()).hasSize(7);
    }

    @Test
    public void sonarLint_7_2_extensions() {
        SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(VERSION_7_2);
        Plugin.Context context = new Plugin.Context(runtime);
        shellCheckPlugin.define(context);
        assertThat(context.getExtensions()).hasSize(7);
    }

    @Test
    public void sonarLint_7_4_extensions() {
        SonarRuntime runtime = SonarRuntimeImpl.forSonarLint(VERSION_7_4);
        Plugin.Context context = new Plugin.Context(runtime);
        shellCheckPlugin.define(context);
        assertThat(context.getExtensions()).hasSize(7);
    }

}
