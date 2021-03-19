package info.novatec.micronaut.camunda.bpm.example;

import io.micronaut.context.annotation.Factory;
import org.camunda.bpm.engine.impl.cfg.ProcessEnginePlugin;
import org.camunda.bpm.identity.impl.ldap.plugin.LdapIdentityProviderPlugin;

import javax.inject.Singleton;

@Factory
public class PluginConfiguration {

    @Singleton
    public ProcessEnginePlugin ldap() {
        // Using an open online LDAP to provide an example
        // https://www.forumsys.com/tutorials/integration-how-to/ldap/online-ldap-test-server/
        // Log in e.g. with 'einstein' / 'password'
        LdapIdentityProviderPlugin ldap = new LdapIdentityProviderPlugin();
        ldap.setServerUrl("ldap://ldap.forumsys.com:389");
        ldap.setManagerDn("cn=read-only-admin,dc=example,dc=com");
        ldap.setManagerPassword("password");
        ldap.setBaseDn("dc=example,dc=com");
        return ldap;
    }
}
