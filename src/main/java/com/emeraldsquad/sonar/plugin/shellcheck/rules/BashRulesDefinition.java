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
package com.emeraldsquad.sonar.plugin.shellcheck.rules;

import com.emeraldsquad.sonar.plugin.shellcheck.languages.BashLanguage;
import com.emeraldsquad.sonar.plugin.shellcheck.utils.ResourceList;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

import java.util.List;

public final class BashRulesDefinition implements RulesDefinition {

  public static final String KEY = "shellcheck";

  public static final String REPO_KEY = BashLanguage.KEY + "-" + KEY;

  private void defineRulesForLanguage(Context context, String repositoryKey, String languageKey) {
    List<String> ruleDocs = ResourceList.getDocList();

    NewRepository repository = context.createRepository(repositoryKey, languageKey);
    repository.setName("Shellcheck rules");

    for (String ruleDoc: ruleDocs) {
      String ruleName = ruleDoc.substring(4, 10);

      NewRule rule = repository.createRule(ruleName);
      rule.setInternalKey(ruleName);
      rule.setName(ruleName);

      rule.setActivatedByDefault(true);
      rule.setType(RuleType.CODE_SMELL);
      rule.setSeverity(Severity.MINOR);
      rule.setStatus(RuleStatus.READY);
      rule.setHtmlDescription(this.getClass().getClassLoader().getResource(ruleDoc));
    }

    repository.done();
  }



  @Override
  public void define(Context context) {
    defineRulesForLanguage(context, REPO_KEY, BashLanguage.KEY);
  }

}
