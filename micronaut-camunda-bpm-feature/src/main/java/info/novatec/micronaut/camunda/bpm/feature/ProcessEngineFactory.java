package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.io.scan.DefaultClassPathResourceLoader;
import io.micronaut.inject.BeanDefinition;
import io.micronaut.inject.qualifiers.Qualifiers;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Tobias Schäfer
 */
@Factory
public class ProcessEngineFactory {

    public static final String MICRONAUT_AUTO_DEPLOYMENT_NAME = "MicronautAutoDeployment";

    private static final Logger log = LoggerFactory.getLogger(ProcessEngineFactory.class);

    private final ApplicationContext applicationContext;

    public ProcessEngineFactory(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /**
     * The {@link ProcessEngine} is started with the application start so that the task scheduler is started immediately.
     *
     * @param processEngineConfiguration the {@link ProcessEngineConfiguration} to build the {@link ProcessEngine}.
     * @param camundaBpmVersion the @{@link CamundaBpmVersion} to log on application start.
     * @param defaultClassPathResourceLoader hello
     * @return the initialized {@link ProcessEngine} in the application context.
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    @Context
    @Bean(preDestroy = "close")
    public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration, CamundaBpmVersion camundaBpmVersion, DefaultClassPathResourceLoader defaultClassPathResourceLoader
    ) throws IOException {

        log.info("Camunda BPM version: {}", camundaBpmVersion.getVersion().orElse(""));

        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

        deployProcessModels(processEngine, defaultClassPathResourceLoader);

        return processEngine;
    }

    /**
     * Deploys all process models found in the resources.
     *
     * @param processEngine the {@link ProcessEngine}
     * @param defaultClassPathResourceLoader hello
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    private void deployProcessModels(ProcessEngine processEngine, DefaultClassPathResourceLoader defaultClassPathResourceLoader) throws IOException {
        log.info("Deploying models from the resources");

        Collection<BeanDefinition<?>> definitions =  applicationContext.getBeanDefinitions(Qualifiers.byStereotype(ResourceScan.class));
        log.info(String.valueOf(definitions.size()));
        for(BeanDefinition definition : definitions) {
            AnnotationMetadata annotationMetadata = definition.getAnnotationMetadata();
            log.info(String.valueOf("String Value: " + Arrays.toString(annotationMetadata.stringValues(ResourceScan.class, "models"))));
            log.info("Members: " + (annotationMetadata.getAnnotation(ResourceScan.class)).getMemberNames());
            Arrays.stream(annotationMetadata.stringValues(ResourceScan.class, "models")).forEach(model -> {
                log.info("Deploying model: {}", model);
                processEngine.getRepositoryService().createDeployment()
                        .name(MICRONAUT_AUTO_DEPLOYMENT_NAME)
                        .addInputStream(model, defaultClassPathResourceLoader.getResourceAsStream(model).get())
                        .enableDuplicateFiltering(true)
                        .deploy();
            });
        }
    }
}
