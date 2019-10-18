/*
Copyright (c) 2010-2018 Grid Dynamics International, Inc. All Rights Reserved
http://www.griddynamics.com

This library is free software; you can redistribute it and/or modify it under the terms of
the GNU Lesser General Public License as published by the Free Software Foundation; either
version 2.1 of the License, or any later version.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

$Id: 
@Project:     Sprimber
@Description: Framework that provide bdd engine and bridges for most popular BDD frameworks
*/

package com.griddynamics.qa.sprimber.discovery;

import com.griddynamics.qa.sprimber.engine.Node;
import com.griddynamics.qa.sprimber.stepdefinition.StepDefinition;
import com.griddynamics.qa.sprimber.stepdefinition.StepDefinitionsRegistry;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.stream.Stream;

/**
 * @author fparamonov
 */
public class ClassicStepFactoryTest {

    @Test
    public void testBasicFunctionality() {
        StepDefinitionsRegistry stepDefinitionsRegistry = Mockito.mock(StepDefinitionsRegistry.class);
        ClassicStepFactory stepFactory = new ClassicStepFactory(stepDefinitionsRegistry);
        Method testMethod = ReflectionUtils.findMethod(ExampleTestController.class, "testMe");
        Mockito.when(stepDefinitionsRegistry.streamAllDefinitions()).thenAnswer(invocation -> Stream.of(dummyStepDefinition(testMethod)));
        Node step = stepFactory.provideStepNode(testMethod);
        Assertions.assertThat(step.getType()).isEqualTo("stepContainer");
        Assertions.assertThat(step.getChildren()).hasSize(1);
        Assertions.assertThat(step.getChildren()).containsOnlyKeys("target");
        Assertions.assertThat(step.getChildren().get("target")).hasSize(1);
        Assertions.assertThat(step.getChildren().get("target"))
                .extracting("name")
                .containsOnly("testMe");
    }

    private StepDefinition dummyStepDefinition(Method testMethod) {
        StepDefinition stepDefinition = new StepDefinition();
        stepDefinition.setMethod(testMethod);
        stepDefinition.setName("Dummy name");
        return stepDefinition;
    }
}