package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.scan.DefaultClassPathResourceLoader;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

/**
 * @author Tobias Schäfer
 */
@Factory
public class ProcessEngineFactory {

    public static final String MICRONAUT_AUTO_DEPLOYMENT_NAME = "MicronautAutoDeployment";

    private static final Logger log = LoggerFactory.getLogger(ProcessEngineFactory.class);

    private static DefaultClassPathResourceLoader defaultClassPathResourceLoader;

    /**
     * The {@link ProcessEngine} is started with the application start so that the task scheduler is started immediately.
     *
     * @param processEngineConfiguration the {@link ProcessEngineConfiguration} to build the {@link ProcessEngine}.
     * @return the initialized {@link ProcessEngine} in the application context.
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    @Context
    @Bean(preDestroy = "close")
    public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration, DefaultClassPathResourceLoader defaultClassPathResourceLoader) throws IOException {
        this.defaultClassPathResourceLoader = defaultClassPathResourceLoader;

        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

        deployProcessModels(processEngine);

        return processEngine;
    }

    /**
     * Deploys all process models found in root directory of the resources.
     * <p>
     * Note: Currently this is not recursive!
     *
     * @param processEngine the {@link ProcessEngine}
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    private void deployProcessModels(ProcessEngine processEngine) throws IOException {
        log.info("Deploying models from the resources");
        //TODO: determine path for mn.txt
        try (Stream<String> stream = Files.lines(Paths.get("/Users/tos/scratch/micronaut-camunda-bpm/micronaut-camunda-bpm-example/build/resources/main/mn.txt"))) {
            stream.forEach( model -> {
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
