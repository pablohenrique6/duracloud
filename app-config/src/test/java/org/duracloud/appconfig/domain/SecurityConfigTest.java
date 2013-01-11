/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.duracloud.ldap.domain.LdapConfig;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: Apr 21, 2010
 */
public class SecurityConfigTest {

    private static final int NUM_ACCTS = 3;

    private String basedn = "basedn-value";
    private String userdn = "userdn-value";
    private String password = "password-value";
    private String url = "url-value";

    private Set<Integer> acctIds;

    @Before
    public void setUp() {
        acctIds = new HashSet<>();
        for (int i = 20; i < 20 + NUM_ACCTS; ++i) {
            acctIds.add(i);
        }
    }

    @Test
    public void testLoad() {
        SecurityConfig config = new SecurityConfig();
        config.load(createProps());
        verifySecurityConfig(config);
    }

    private Map<String, String> createProps() {
        Map<String, String> props = new HashMap<String, String>();
        String dot = ".";
        String prefix = SecurityConfig.QUALIFIER + dot;

        String ldapPre = prefix + SecurityConfig.ldapKey + dot;
        props.put(ldapPre + SecurityConfig.ldapBaseDnKey, basedn);
        props.put(ldapPre + SecurityConfig.ldapUserDnKey, userdn);
        props.put(ldapPre + SecurityConfig.ldapPasswordKey, password);
        props.put(ldapPre + SecurityConfig.ldapUrlKey, url);

        String acctPre = prefix + SecurityConfig.acctIdKey + dot;
        int x = 0;
        for (int acctId : acctIds) {
            props.put(acctPre + x++, Integer.toString(acctId));
        }

        return props;
    }

    private void verifySecurityConfig(SecurityConfig config) {
        LdapConfig xLdapConfig = config.getLdapConfig();
        Assert.assertNotNull(xLdapConfig);

        Assert.assertEquals(basedn, xLdapConfig.getLdapBaseDn());
        Assert.assertEquals(userdn, xLdapConfig.getLdapUserDn());
        Assert.assertEquals(password, xLdapConfig.getLdapPassword());
        Assert.assertEquals(url, xLdapConfig.getLdapUrl());

        Set<Integer> xAcctIds = config.getAcctIds();
        Assert.assertNotNull(xAcctIds);

        Assert.assertEquals(acctIds.size(), xAcctIds.size());
        for (int acctId : acctIds) {
            Assert.assertTrue(xAcctIds.contains(acctId));
        }
    }

}
