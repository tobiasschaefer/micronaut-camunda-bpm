/*
 * Copyright 2021 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.context.BeanProvider;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.event.annotation.EventListener;
import io.micronaut.runtime.server.event.ServerShutdownEvent;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import org.camunda.bpm.application.AbstractProcessApplication;
import org.camunda.bpm.application.ProcessApplicationReference;
import org.camunda.bpm.application.impl.ProcessApplicationReferenceImpl;
import org.camunda.bpm.container.RuntimeContainerDelegate;
import org.camunda.bpm.engine.ProcessEngine;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Map;

import static org.camunda.bpm.application.ProcessApplicationInfo.PROP_SERVLET_CONTEXT_PATH;

/**
 * Micronaut implementation of {@link AbstractProcessApplication} so that deployed models
 * @author Tobias Schäfer
 */
// based on https://github.com/camunda/camunda-bpm-platform/blob/master/spring-boot-starter/starter/src/main/java/org/camunda/bpm/spring/boot/starter/SpringBootProcessApplication.java
@Singleton
public class MnProcessApplication extends AbstractProcessApplication {

    protected final BeanProvider<ProcessEngine> processEngineBeanProvider;

    @Inject
    ApplicationConfiguration applicationConfiguration;

    public MnProcessApplication(BeanProvider<ProcessEngine> processEngineBeanProvider) {
        this.processEngineBeanProvider = processEngineBeanProvider;
    }

    @Override
    protected String autodetectProcessApplicationName() {
        // Note: property generate-unique-process-application-name is currently not supported
        return applicationConfiguration.getName().orElse("application");
    }

    @Override
    public ProcessApplicationReference getReference() {
        return new ProcessApplicationReferenceImpl(this);
    }

    @Override
    public Map<String, String> getProperties() {
        return Collections.singletonMap(PROP_SERVLET_CONTEXT_PATH, "/");
    }

    @EventListener
    public void onStartup(ServerStartupEvent event) {
        // Unsure in which scenarios setDefaultDeployToEngineName(...) and registerProcessEngine(...) are needed ...
        ProcessEngine processEngine = processEngineBeanProvider.get();
        setDefaultDeployToEngineName(processEngine.getName());
        RuntimeContainerDelegate.INSTANCE.get().registerProcessEngine(processEngine);

        deploy();
    }

    @EventListener
    public void onShutdown(ServerShutdownEvent event) {
        undeploy();
        RuntimeContainerDelegate.INSTANCE.get().unregisterProcessEngine(processEngineBeanProvider.get());
    }
}
