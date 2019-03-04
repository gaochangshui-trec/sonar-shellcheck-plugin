/*
 * Example Plugin for SonarQube
 * Copyright (C) 2009-2016 SonarSource SA
 * mailto:contact AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.emeraldsquad.sonar.plugin.shellcheck;

import static java.util.Arrays.asList;

import com.emeraldsquad.sonar.plugin.shellcheck.issues.BashIssuesLoaderSensor;
import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;
import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashQualityProfile;
import com.emeraldsquad.sonar.plugin.shellcheck.measures.LineOfCodeSensor;
import com.emeraldsquad.sonar.plugin.shellcheck.rules.BashRulesDefinition;
import com.emeraldsquad.sonar.plugin.shellcheck.settings.BashLanguageProperties;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */
public class ShellCheckPlugin implements Plugin {


  @Override
  public void define(Context context) {
    // tutorial on languages
    context.addExtensions(BashLanguage.class, BashQualityProfile.class);
    context.addExtension(BashLanguageProperties.getProperties());

    // tutorial on rules
    context.addExtensions(BashRulesDefinition.class, BashIssuesLoaderSensor.class);
    context.addExtension(LineOfCodeSensor.class);

    context.addExtensions(asList(
      PropertyDefinition.builder("sonar.shell.file.suffixes")
        .name("Shell suffixes")
        .description("Suffixes supported by shellcheck")
        .category("Shell scripts")
        .type(PropertyType.STRING)
        .build()));
  }
}
