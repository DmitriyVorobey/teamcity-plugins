/*
 * Copyright 2000-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.gradle.test.integration;

import java.io.IOException;
import jetbrains.buildServer.gradle.GradleRunnerConstants;
import jetbrains.buildServer.gradle.agent.GradleVersionErrorsListener;
import jetbrains.buildServer.messages.ErrorData;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Author: Nikita.Skvortsov
 * Date: Oct 11, 2010
 */
public class GradleRunnerWrapperTest extends GradleRunnerServiceMessageTest {

  private boolean myExpectInternalError = false;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    myRunnerParams.clear();
    myExpectInternalError = false;
  }

  @Test
  public void simpleWrapperTest() throws Exception {
    myRunnerParams.put(GradleRunnerConstants.GRADLE_WRAPPER_FLAG, Boolean.TRUE.toString());
    myRunnerParams.put(GradleRunnerConstants.GRADLE_WRAPPER_PATH, "gradle-runtime");
    GradleRunConfiguration config = new GradleRunConfiguration("wrappedProjectA", "clean build", "wrappedProjASequence.txt");
    config.setPatternStr("^Downloading(.*)|^Unzipping(.*)|##teamcity\\[(.*?)(?<!\\|)\\]");
    runAndCheckServiceMessages(config);
  }

  @Test
  public void incompatibleStartupScriptAPI() throws Exception {
    myRunnerParams.put(GradleRunnerConstants.GRADLE_WRAPPER_FLAG, Boolean.TRUE.toString());
    GradleRunConfiguration config = new GradleRunConfiguration("wrappedProjectB", "clean build", "wrappedProjBSequence.txt");
    config.setPatternStr("^Downloading(.*)|^Unzipping(.*)|##teamcity\\[(.*?)(?<!\\|)\\]");
    myExpectInternalError = true;
    runAndCheckServiceMessages(config);
  }

  @Test
  public void startupScriptNotSupportedTest() throws Exception {
    myRunnerParams.put(GradleRunnerConstants.GRADLE_WRAPPER_FLAG, Boolean.TRUE.toString());
    GradleRunConfiguration config = new GradleRunConfiguration("wrappedProjectC", "clean test", "wrappedProjCSequence.txt");
    config.setPatternStr("^Downloading(.*)|^Unzipping(.*)|##teamcity\\[(.*?)(?<!\\|)\\]");
    myExpectInternalError = true;
    runAndCheckServiceMessages(config);
  }


  @Override
  protected Mockery initContext(final String projectName, final String gradleParams, final String gradleVersion) throws IOException {
    final Mockery mockery = super.initContext(projectName, gradleParams,
                                              gradleVersion);

    if (myExpectInternalError) {
      final Expectations expectInternalError = new Expectations() {{
            oneOf(myMockLogger).internalError(ErrorData.BUILD_RUNNER_ERROR_TYPE, GradleVersionErrorsListener.WRONG_GRADLE_VERSION, null);
          }};
      mockery.checking(expectInternalError);
    }
    return mockery;    //To change body of overridden methods use File | Settings | File Templates.
  }
}
