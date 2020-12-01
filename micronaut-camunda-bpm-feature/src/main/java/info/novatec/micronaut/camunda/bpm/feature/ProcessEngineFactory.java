package info.novatec.micronaut.camunda.bpm.feature;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Context;
import io.micronaut.context.annotation.Factory;
import io.micronaut.core.io.ResourceLoader;
import io.micronaut.core.io.ResourceResolver;
import io.micronaut.core.io.scan.ClassPathResourceLoader;
import io.micronaut.core.io.scan.DefaultClassPathResourceLoader;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Tobias Schäfer
 */
@Factory
public class ProcessEngineFactory {

    public static final String MICRONAUT_AUTO_DEPLOYMENT_NAME = "MicronautAutoDeployment";

    private static final Logger log = LoggerFactory.getLogger(ProcessEngineFactory.class);

    /**
     * The {@link ProcessEngine} is started with the application start so that the task scheduler is started immediately.
     *
     * @param processEngineConfiguration the {@link ProcessEngineConfiguration} to build the {@link ProcessEngine}.
     * @param camundaBpmVersion the @{@link CamundaBpmVersion} to log on application start.
     * @return the initialized {@link ProcessEngine} in the application context.
     * @throws IOException if a resource, i.e. a model, cannot be loaded.
     */
    @Context
    @Bean(preDestroy = "close")
    public ProcessEngine processEngine(ProcessEngineConfiguration processEngineConfiguration, CamundaBpmVersion camundaBpmVersion, ResourceLoader resourceLoader, ClassPathResourceLoader defaultClassPathResourceLoader) throws IOException {

        log.info("Camunda BPM version: {}", camundaBpmVersion.getVersion());

        ProcessEngine processEngine = processEngineConfiguration.buildProcessEngine();

        // 1.
        Stream<URL> resources = resourceLoader.getResources("*.bpmn");
        List<URL> collect = resources.collect(Collectors.toList());

        // 2.
        Stream<URL> resources1 = defaultClassPathResourceLoader.getResources("helloworld.bpmn");
        List<URL> collect1 = resources1.collect(Collectors.toList());

        // 3.
        String folderPath = getClass().getResource("/").getPath();
        Path p = Paths.get(folderPath);
        Files.walk(p).forEach(x -> {
            System.out.println(x);
        });

        // 4.
        DefaultClassPathResourceLoader loader = (DefaultClassPathResourceLoader) new ResourceResolver().getLoader(ClassPathResourceLoader.class).get();
        Optional<URL> resource = loader.getResource("classpath:helloworld.bpmn");

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
    protected void deployProcessModels(ProcessEngine processEngine) throws IOException {
        log.info("Searching recursively for models in the resources");
        String folderPath = getClass().getResource("/").getPath();
        Path p = Paths.get(folderPath);
        Files.walk(p)
                .filter( p1 -> p1.toString().endsWith("bpmn"))
                .forEach( p2 -> {
                    System.out.println(p2);
                });

        PathMatchingResourcePatternResolver resourceLoader = new PathMatchingResourcePatternResolver();
        // Order of extensions has been chosen as a best fit for inter process dependencies.
        for (String extension : Arrays.asList("dmn", "cmmn", "bpmn")) {
            for (Resource resource : resourceLoader.getResources(PathMatchingResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + "*." + extension)) {
                log.info("Deploying model: {}", resource.getFilename());
                processEngine.getRepositoryService().createDeployment()
                        .name(MICRONAUT_AUTO_DEPLOYMENT_NAME)
                        .addInputStream(resource.getFilename(), resource.getInputStream())
                        .enableDuplicateFiltering(true)
                        .deploy();
            }
        }
    }
}
