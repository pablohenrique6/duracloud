/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.junit.BeforeClass;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 6/11/12
 */
public class BaseTestDuracloudRepoImpl {

    private static LdapContextSource contextSource;

    @BeforeClass
    public static void beforeClass() {
        contextSource = new LdapContextSource();
        contextSource.setUrl(getLdapUrl());
        contextSource.setBase(getLdapBaseDn());
        contextSource.setUserDn(getLdapUserDn());
        contextSource.setPassword(getLdapPassword());

        try {
            contextSource.afterPropertiesSet();
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
    }

    protected static LdapContextSource getContextSource() throws Exception {
        return contextSource;
    }

    private static String getLdapUrl() {
        return getProperty("mc.unit.ldap.url");
    }

    private static String getLdapBaseDn() {
        return getProperty("mc.unit.ldap.basedn");
    }

    private static String getLdapUserDn() {
        return getProperty("mc.unit.ldap.userdn");
    }

    private static String getLdapPassword() {
        return getProperty("mc.unit.ldap.password");
    }

    private static String getProperty(String key) {
        String password = System.getProperty(key);
        if (null == password) {
            throw new DuraCloudRuntimeException(usage(key));
        }
        return password;
    }

    private static String usage(String key) {
        return "System property: " + key + " must be defined.";
    }

}
