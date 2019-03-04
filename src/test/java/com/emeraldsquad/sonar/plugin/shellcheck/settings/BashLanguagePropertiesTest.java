package com.emeraldsquad.sonar.plugin.shellcheck.settings;

import org.junit.Test;
import static org.assertj.core.api.Assertions.assertThat;

public class BashLanguagePropertiesTest {

    @Test
    public void propertries_defined() {
        assertThat(BashLanguageProperties.getProperties()).hasSize(1);
    }
}
