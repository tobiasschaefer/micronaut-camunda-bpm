package info.novatec.micronaut.camunda.bpm.example;

import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;

@Controller("/default")
public class HelloController {

    @Get("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    @ExecuteOn(TaskExecutors.IO)
    public String name() {
        return "Hello";
    }
}
