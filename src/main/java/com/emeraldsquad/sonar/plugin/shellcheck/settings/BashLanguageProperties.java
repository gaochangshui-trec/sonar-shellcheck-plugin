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
package com.emeraldsquad.sonar.plugin.shellcheck.settings;

import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

import java.util.List;

import static java.util.Arrays.asList;

public class BashLanguageProperties {

  public static final String FILE_SUFFIXES_KEY = "sonar.bash.file.suffixes";
  public static final String FILE_SUFFIXES_DEFAULT_VALUE = ".sh";

  private BashLanguageProperties() {
    // only statics
  }

  public static List<PropertyDefinition> getProperties() {
    return asList(PropertyDefinition.builder(FILE_SUFFIXES_KEY)
      .multiValues(true)
      .defaultValue(FILE_SUFFIXES_DEFAULT_VALUE)
      .category("Bash")
      .name("File Suffixes")
      .description("List of suffixes for files to analyze.")
      .onQualifiers(Qualifiers.PROJECT)
      .build());
  }

}
