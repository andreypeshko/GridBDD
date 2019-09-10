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

package com.griddynamics.qa.sprimber.discovery.support;

import com.griddynamics.qa.sprimber.discovery.StepDefinition;
import com.griddynamics.qa.sprimber.discovery.TestSuiteDefinition;
import com.griddynamics.qa.sprimber.engine.ExecutionResult;
import com.griddynamics.qa.sprimber.engine.executor.ErrorMapper;
import com.griddynamics.qa.sprimber.event.SprimberEventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.concurrent.CompletableFuture;

import static com.griddynamics.qa.sprimber.engine.ExecutionResult.Status.PASSED;

/**
 * @author fparamonov
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class BddTestExecutor implements TestSuiteDefinition.TestExecutor {

    private final ErrorMapper errorMapper;
    private final ApplicationContext applicationContext;
    private final SprimberEventPublisher eventPublisher;

    @Override
    public CompletableFuture<ExecutionResult> execute(TestSuiteDefinition.TestDefinition testDefinition) {
        eventPublisher.testStarted(this, testDefinition);
        ExecutionResult result = new ExecutionResult(PASSED);
        // TODO: 2019-09-10 add handling for step results after the execution
        testDefinition.getStepDefinitions().forEach(this::executeStepDefinition);

        testDefinition.setExecutionResult(result);
        eventPublisher.testFinished(this, testDefinition);
        return CompletableFuture.completedFuture(result);
    }

    public void executeStepDefinition(StepDefinition stepDefinition) {
        ExecutionResult result = new ExecutionResult(PASSED);
        Method testMethod = stepDefinition.getMethod();
        Object target = applicationContext.getBean(testMethod.getDeclaringClass());
        // TODO: 2019-09-10 handle the named arguments well
        Object[] args = stepDefinition.getParameters().isEmpty() ? new Object[0] : stepDefinition.getParameters().values().toArray();
        try {
            ReflectionUtils.invokeMethod(testMethod, target, args);
            stepDefinition.setExecutionResult(result);
            eventPublisher.stepStarted(target, stepDefinition);
        } catch (Throwable throwable) {
            result = errorMapper.parseThrowable(throwable);
            stepDefinition.setExecutionResult(result);
            result.conditionallyPrintStacktrace();
            log.trace(result.getErrorMessage());
            log.error(throwable.getLocalizedMessage());
        } finally {
            eventPublisher.stepFinished(target, stepDefinition);
        }
    }
}
