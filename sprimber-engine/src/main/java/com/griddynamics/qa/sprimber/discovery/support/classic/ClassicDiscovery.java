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

package com.griddynamics.qa.sprimber.discovery.support.classic;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.discovery.annotation.TestController;
import com.griddynamics.qa.sprimber.discovery.annotation.TestMapping;
import com.griddynamics.qa.sprimber.discovery.support.ClassicTestExecutor;
import com.griddynamics.qa.sprimber.discovery.support.TestSuiteDiscovery;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ClassicDiscovery implements TestSuiteDiscovery {

    private final ApplicationContext applicationContext;
    private final ClassicTestExecutor classicTestExecutor;

    @Override
    public TestSuiteDiscovery setAvailableStepDefinitionsSet(List<StepDefinition> stepDefinitions) {
        // not implemented since the list of available step definitions doesn't required yet.
        return this;
    }

    @Override
    public TestSuiteDefinition discover() {
        TestSuiteDefinition testSuiteDefinition = new TestSuiteDefinition();
        testSuiteDefinition.setTestExecutor(classicTestExecutor);
        applicationContext.getBeansWithAnnotation(TestController.class).values().stream()
                .map(this::testCaseDiscover)
                .forEach(testCase -> testSuiteDefinition.getTestCaseDefinitions().add(testCase));
        return testSuiteDefinition;
    }

    private TestSuiteDefinition.TestCaseDefinition testCaseDiscover(Object testController) {
        val testCaseDefinition = new TestSuiteDefinition.TestCaseDefinition();
        Arrays.stream(testController.getClass().getDeclaredMethods())
                .filter(method -> method.isAnnotationPresent(TestMapping.class))
                .forEach(testMethod -> {
                    ClassicTestBinder classicTestBinder = new ClassicTestBinder(testMethod);
                    testCaseDefinition.getTestDefinitions().add(classicTestBinder.bind());
                });
        return testCaseDefinition;
    }
}
