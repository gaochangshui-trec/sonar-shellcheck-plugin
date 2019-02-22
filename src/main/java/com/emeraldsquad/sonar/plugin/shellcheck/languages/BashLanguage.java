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
package com.emeraldsquad.sonar.plugin.shellcheck.languages;

import com.emeraldsquad.sonar.plugin.shellcheck.settings.BashLanguageProperties;
import org.apache.commons.lang.StringUtils;
import org.sonar.api.config.Configuration;
import org.sonar.api.resources.AbstractLanguage;

/**
 * This class defines th bash language
 */
public final class BashLanguage extends AbstractLanguage {

  public static final String NAME = "Shell scripts";
  public static final String KEY = "shell";

  private final Configuration config;

  public BashLanguage(Configuration config) {
    super(KEY, NAME);
    this.config = config;
  }

  @Override
  public String[] getFileSuffixes() {
    String suffixes = config.get(BashLanguageProperties.FILE_SUFFIXES_KEY)
      .orElse(BashLanguageProperties.FILE_SUFFIXES_DEFAULT_VALUE);

    return StringUtils.split(suffixes, ",");
  }

}
