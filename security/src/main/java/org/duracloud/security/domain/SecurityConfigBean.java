/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.domain;

import org.duracloud.ldap.domain.IdUtilConfig;
import org.duracloud.ldap.domain.LdapConfig;

import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 1/9/13
 */
public class SecurityConfigBean {

    private LdapConfig ldapConfig;
    private IdUtilConfig idUtilConfig;
    private Set<Integer> acctIds;

    public static final String SCHEMA_VERSION = "2.3";

    public SecurityConfigBean() {
        // default constructor
    }

    public SecurityConfigBean(LdapConfig ldapConfig,
                              IdUtilConfig idUtilConfig,
                              Set<Integer> acctIds) {
        this.ldapConfig = ldapConfig;
        this.idUtilConfig = idUtilConfig;
        this.acctIds = acctIds;
    }

    public LdapConfig getLdapConfig() {
        return ldapConfig;
    }

    public void setLdapConfig(LdapConfig ldapConfig) {
        this.ldapConfig = ldapConfig;
    }

    public IdUtilConfig getIdUtilConfig() {
        return idUtilConfig;
    }

    public void setIdUtilConfig(IdUtilConfig idUtilConfig) {
        this.idUtilConfig = idUtilConfig;
    }

    public Set<Integer> getAcctIds() {
        return acctIds;
    }

    public void setAcctIds(Set<Integer> acctIds) {
        this.acctIds = acctIds;
    }
}
