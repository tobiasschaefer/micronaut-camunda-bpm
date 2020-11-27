package info.novatec.micronaut.camunda.bpm.example.webapps;

import org.camunda.bpm.tasklist.impl.web.TasklistApplication;
import org.glassfish.jersey.server.ResourceConfig;

public class TasklistApp extends ResourceConfig {
    static TasklistApplication tA = new TasklistApplication();
    public TasklistApp() {
        registerClasses(tA.getClasses());
    }
}
