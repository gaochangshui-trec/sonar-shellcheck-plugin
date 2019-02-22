package com.emeraldsquad.sonar.plugin.shellcheck.languages;

import static org.assertj.core.api.Assertions.assertThat;

import com.emeraldsquad.sonar.plugin.shellcheck.settings.BashLanguageProperties;

import org.junit.Test;
import org.sonar.api.config.internal.MapSettings;

public class BashLanguageTest {

  @Test
  public void defaultSuffixes() {
    MapSettings mapSettings = new MapSettings();
    BashLanguage bashLanguage = new BashLanguage(mapSettings.asConfig());
    assertThat(bashLanguage.getFileSuffixes()).containsOnly(".sh");
  }

  @Test
  public void customSuffixes() {
    MapSettings mapSettings = new MapSettings();
    mapSettings.setProperty(BashLanguageProperties.FILE_SUFFIXES_KEY, ".zsh,.csh");
    BashLanguage bashLanguage = new BashLanguage(mapSettings.asConfig());
    assertThat(bashLanguage.getFileSuffixes()).containsExactly(".zsh", ".csh");
  }

}