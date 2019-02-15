package com.emeraldsquad.sonar.plugin.shellcheck.rules;

import com.emeraldsquad.sonar.plugin.shellcheck.utils.ResourceList;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonar.api.server.rule.RulesDefinition;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ResourceList.class })
public class BashRulesDefinitionTest {

    @Test
    public void define_rules() {
        List<String> docList = new ArrayList<>();
        docList.add("doc/SC0042.html");
        docList.add("doc/SC1234.html");

        mockStatic(ResourceList.class);
        when(ResourceList.getDocList()).thenReturn(docList);

        BashRulesDefinition rules = new BashRulesDefinition();
        RulesDefinition.Context context = new RulesDefinition.Context();
        rules.define(context);
        RulesDefinition.Repository repository = context.repository("shell-shellcheck");
        assertThat(repository.name()).isEqualTo("Shellcheck rules");
        assertThat(repository.language()).isEqualTo("shell");
        assertThat(repository.rules()).hasSize(2);
    }
}
