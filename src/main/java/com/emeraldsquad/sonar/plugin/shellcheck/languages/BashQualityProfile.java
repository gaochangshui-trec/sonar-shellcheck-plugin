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

import com.emeraldsquad.sonar.plugin.shellcheck.rules.BashRulesDefinition;
import com.emeraldsquad.sonar.plugin.shellcheck.utils.ResourceList;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;

import java.util.Collection;

/**
 * Default, BuiltIn Quality Profile for the projects having bash scripts
 */
public final class BashQualityProfile implements BuiltInQualityProfilesDefinition {

  public static final String PROFILE_NAME = "Shellcheck rules";

  @Override
  public void define(Context context) {
    NewBuiltInQualityProfile profile = context.createBuiltInQualityProfile(PROFILE_NAME, BashLanguage.KEY);
    profile.setDefault(true);

    Collection<String> ruleDocs = ResourceList.getDocList();

    for (String ruleDoc: ruleDocs) {
      String ruleName = ruleDoc.substring(4, 10);
      profile.activateRule(BashRulesDefinition.REPO_KEY, ruleName);
    }

    profile.done();
  }

}
