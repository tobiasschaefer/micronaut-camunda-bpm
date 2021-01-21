package info.novatec.micronaut.camunda.bpm.feature;

import info.novatec.micronaut.camunda.bpm.feature.tx.MnTransactionContextFactory;
import info.novatec.micronaut.camunda.bpm.feature.tx.MnTransactionInterceptor;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.transaction.SynchronousTransactionManager;
import org.camunda.bpm.engine.ProcessEngine;
import org.camunda.bpm.engine.ProcessEngineConfiguration;
import org.camunda.bpm.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.camunda.bpm.engine.impl.db.sql.DbSqlSessionFactory;
import org.camunda.bpm.engine.impl.interceptor.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static io.micronaut.transaction.TransactionDefinition.Propagation.REQUIRED;
import static io.micronaut.transaction.TransactionDefinition.Propagation.REQUIRES_NEW;

/**
 * Micronaut implementation of {@link org.camunda.bpm.engine.ProcessEngineConfiguration} which is aware of transaction
 * management, i.e. the surrounding transaction will be used and {@link org.camunda.bpm.engine.delegate.JavaDelegate}s
 * are executed in a transaction allowing the persistence of data with micronaut-data.
 *
 * @author Tobias Schäfer
 * @author Lukasz Frankowski
 */
@Singleton
@Introspected
public class MnProcessEngineConfiguration extends ProcessEngineConfigurationImpl {

    private static final Logger log = LoggerFactory.getLogger(MnProcessEngineConfiguration.class);

    protected final SynchronousTransactionManager<Connection> transactionManager;

    protected final MnJobExecutor jobExecutor;

    protected final Configuration configuration;

    protected final MnTelemetryRegistry telemetryRegistry;

    protected final Environment environment;

    public MnProcessEngineConfiguration(SynchronousTransactionManager<Connection> transactionManager,
                                        MnJobExecutor jobExecutor,
                                        Configuration configuration,
                                        MnTelemetryRegistry telemetryRegistry,
                                        Environment environment,
                                        ApplicationContext applicationContext,
                                        DataSource dataSource,
                                        MnArtifactFactory artifactFactory,
                                        ProcessEngineConfigurationCustomizer processEngineConfigurationCustomizer) {
        this.transactionManager = transactionManager;
        this.jobExecutor = jobExecutor;
        this.configuration = configuration;
        this.telemetryRegistry = telemetryRegistry;
        this.environment = environment;
        setDataSource(dataSource);
        setTransactionsExternallyManaged(true);
        setExpressionManager(new MnExpressionManager(new ApplicationContextElResolver(applicationContext)));
        setArtifactFactory(artifactFactory);

        configureDefaultValues();

        applyGenericProperties(configuration);

        configureTelemetry();

        processEngineConfigurationCustomizer.customize(this);
    }

    @Override
    public ProcessEngine buildProcessEngine() {
        return transactionManager.executeWrite(
            transactionStatus -> {
                log.info("Building process engine connected to {}", dataSource.getConnection().getMetaData().getURL());
                return super.buildProcessEngine();
            }
        );
    }

    @Override
    protected void initTransactionContextFactory() {
        if(transactionContextFactory == null) {
            transactionContextFactory = new MnTransactionContextFactory(transactionManager);
        }
    }

    @Override
    protected void initJobExecutor() {
        setJobExecutor(jobExecutor);
        super.initJobExecutor();
    }

    @Override
    protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequired() {
        return getCommandInterceptors(false);
    }

    @Override
    protected Collection< ? extends CommandInterceptor> getDefaultCommandInterceptorsTxRequiresNew() {
        return getCommandInterceptors(true);
    }

    protected List<CommandInterceptor> getCommandInterceptors(boolean requiresNew) {
        // CRDB interceptor is added before the MnTransactionInterceptor,
        // so that a Micronaut TX may be rolled back before retrying.
        return Stream.of(
                DbSqlSessionFactory.CRDB.equals(databaseType) ? getCrdbRetryInterceptor() : null,
                new LogInterceptor(),
                new CommandCounterInterceptor(this),
                new ProcessApplicationContextInterceptor(this),
                new MnTransactionInterceptor(transactionManager, requiresNew ? REQUIRES_NEW : REQUIRED),
                new CommandContextInterceptor(commandContextFactory, this, requiresNew)
        )
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Configure telemetry registry and always disable if the "test" profile is active, i.e. tests are being executed.
     */
    protected void configureTelemetry() {
        setTelemetryRegistry(telemetryRegistry);
        if (environment.getActiveNames().contains(Environment.TEST)) {
            setInitializeTelemetry(false);
            setTelemetryReporterActivate(false);
        }
    }

    /**
     * Configure sensible defaults so that the user must not take care of it.
     */
    protected void configureDefaultValues() {
        setJobExecutorActivate(!environment.getActiveNames().contains(Environment.TEST));
        setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
    }

    protected void applyGenericProperties(Configuration configuration) {
        BeanIntrospection<MnProcessEngineConfiguration> introspection = BeanIntrospection.getIntrospection(MnProcessEngineConfiguration.class);

        for(Map.Entry<String, Object > entry : configuration.getGenericProperties().getProperties().entrySet()){
            BeanProperty<MnProcessEngineConfiguration, Object> property = introspection.getProperty(entry.getKey())
                    .orElseThrow(() -> new RuntimeException("Invalid engine property: " + entry.getKey()));

            property.set(this, resolveGenericPropertyValue(entry.getValue(), property.getType()));
        }
    }

    protected Object resolveGenericPropertyValue(Object value, Class type) {
        // Even if the value is not of type String we cannot assume that it is of the correct type,
        // e.g. a value of "30" will have the type "int" and can then not be set as a value if the
        // configuration is of type "long".
        // Therefore we always use the string value for primitive types and convert it to the target type.
        if (type == int.class) {
            return Integer.valueOf(String.valueOf(value));
        } else if (type == long.class) {
            return Long.valueOf(String.valueOf(value));
        } else if (type == boolean.class) {
            return Boolean.valueOf(String.valueOf(value));
        } else if (type == String.class) {
            return String.valueOf(value);
        } else {
            return value;
        }
    }

    public ProcessEngineConfigurationImpl setInitializeTelemetry(Boolean telemetryInitialized) {
        // This method makes the Boolean telemetryInitialized a writable property.
        // Otherwise applyGenericProperties cannot set the property.
        return super.setInitializeTelemetry(telemetryInitialized);
    }
}
