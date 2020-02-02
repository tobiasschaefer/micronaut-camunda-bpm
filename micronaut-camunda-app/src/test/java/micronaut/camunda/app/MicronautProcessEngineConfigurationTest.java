package micronaut.camunda.app;

import io.micronaut.test.annotation.MicronautTest;
import org.camunda.bpm.engine.*;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;

import static org.junit.jupiter.api.Assertions.*;

@MicronautTest
class MicronautProcessEngineConfigurationTest {

    @Inject
    ProcessEngine processEngine;

    @Inject
    RuntimeService runtimeService;

    @Inject
    RepositoryService repositoryService;

    @Inject
    ManagementService managementService;

    @Inject
    AuthorizationService authorizationService;

    @Inject
    CaseService caseService;

    @Inject
    DecisionService decisionService;

    @Inject
    ExternalTaskService externalTaskService;

    @Inject
    FilterService filterService;

    @Inject
    FormService formService;

    @Inject
    TaskService taskService;

    @Inject
    HistoryService historyService;

    @Inject
    IdentityService identityService;

    @Test
    void allBeansAreAvailableInApplicationContext() {
        assertNotNull(processEngine);
        assertNotNull(runtimeService);
        assertNotNull(managementService);
        assertNotNull(authorizationService);
        assertNotNull(caseService);
        assertNotNull(decisionService);
        assertNotNull(externalTaskService);
        assertNotNull(filterService);
        assertNotNull(formService);
        assertNotNull(taskService);
        assertNotNull(historyService);
        assertNotNull(identityService);
    }

}