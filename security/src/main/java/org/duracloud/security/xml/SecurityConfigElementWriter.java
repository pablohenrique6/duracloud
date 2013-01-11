/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.duracloud.LdapType;
import org.duracloud.SecurityConfigType;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.security.domain.SecurityConfigBean;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for serializing a SecurityConfigBean into
 * SecurityConfig xml documents.
 *
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityConfigElementWriter {

    /**
     * This method serializes a SecurityConfigBean into a SecurityConfig
     * xml element.
     *
     * @param config list to be serialized
     * @return xml SecurityConfig element with content from arg config
     */
    public static SecurityConfigType createSecurityConfigElementFrom(
        SecurityConfigBean config) {
        SecurityConfigType configType =
            SecurityConfigType.Factory.newInstance();
        populateElementFromObject(configType, config);

        return configType;
    }

    private static void populateElementFromObject(SecurityConfigType configType,
                                                  SecurityConfigBean config) {
        configType.setSchemaVersion(SecurityConfigBean.SCHEMA_VERSION);

        LdapType ldapType = configType.addNewLdap();
        populateLdapType(ldapType, config.getLdapConfig());

        List<Integer> acctIdsList = new ArrayList<>();
        acctIdsList.addAll(config.getAcctIds());
        configType.setAcctIds(acctIdsList);
    }

    private static void populateLdapType(LdapType ldapType, LdapConfig config) {
        ldapType.setBasedn(config.getLdapBaseDn());
        ldapType.setUserdn(config.getLdapUserDn());
        ldapType.setPassword(config.getLdapPassword());
        ldapType.setUrl(config.getLdapUrl());
    }

}