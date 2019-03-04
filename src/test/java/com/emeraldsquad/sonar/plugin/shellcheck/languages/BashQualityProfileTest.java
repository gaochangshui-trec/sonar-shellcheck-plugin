package com.emeraldsquad.sonar.plugin.shellcheck.languages;

import static org.assertj.core.api.Assertions.assertThat;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

import java.util.ArrayList;
import java.util.List;

import com.emeraldsquad.sonar.plugin.shellcheck.utils.ResourceList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition;
import org.sonar.api.server.profile.BuiltInQualityProfilesDefinition.BuiltInQualityProfile;
import org.sonar.api.utils.ValidationMessages;

@RunWith(PowerMockRunner.class)
@PrepareForTest( { ResourceList.class })
public class BashQualityProfileTest {

   @Test
  public void should_create_quality_profile() {
    List<String> docList = new ArrayList<>();
    docList.add("doc/SC0042.html");
    docList.add("doc/SC1234.html");

    mockStatic(ResourceList.class);
    when(ResourceList.getDocList()).thenReturn(docList);

    ValidationMessages validation = ValidationMessages.create();

    BuiltInQualityProfile profile = getBuiltInQualityProfile();

    assertThat(profile.language()).isEqualTo(BashLanguage.KEY);
    assertThat(profile.name()).isEqualTo(BashQualityProfile.PROFILE_NAME);
    assertThat(validation.hasErrors()).isFalse();
    assertThat(profile.rules().size()).isEqualTo(2);
  }

  private static BuiltInQualityProfile getBuiltInQualityProfile() {
    BashQualityProfile definition = new BashQualityProfile();
    BuiltInQualityProfilesDefinition.Context context = new BuiltInQualityProfilesDefinition.Context();
    definition.define(context);
    return context.profile(BashLanguage.KEY, BashQualityProfile.PROFILE_NAME);
  }
}