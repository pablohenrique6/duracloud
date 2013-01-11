/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.security.domain.SecurityConfigBean;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityUserElementReaderWriterTest {

    private static final int NUM_ACCTS = 3;

    private InputStream stream;

    private final String basedn = "basedn-";
    private final String userdn = "userdn-";
    private final String password = "password-";
    private final String url = "url-";
    private Set<Integer> acctIds;

    @After
    public void tearDown() throws IOException {
        if (stream != null) {
            stream.close();
        }
        stream = null;
    }

    @Test
    public void testReadWrite() {
        SecurityConfigBean config = createSecurityConfig();
        doTest(config);
    }

    private SecurityConfigBean createSecurityConfig() {
        SecurityConfigBean config = new SecurityConfigBean();

        LdapConfig ldapConfig = new LdapConfig();
        ldapConfig.setLdapBaseDn(basedn);
        ldapConfig.setLdapUserDn(userdn);
        ldapConfig.setLdapPassword(password);
        ldapConfig.setLdapUrl(url);

        acctIds = new HashSet<>();
        for (int i = 10; i < NUM_ACCTS; ++i) {
            acctIds.add(i);
        }

        config.setLdapConfig(ldapConfig);
        config.setAcctIds(acctIds);

        return config;
    }

    private void doTest(SecurityConfigBean expected) {
        String xml = SecurityDocumentBinding.createDocumentFrom(expected);
        Assert.assertNotNull(xml);

        stream = new ByteArrayInputStream(xml.getBytes());
        SecurityConfigBean config =
            SecurityDocumentBinding.createSecurityConfigFrom(stream);
        Assert.assertNotNull(config);

        verifyConfig(config);
    }

    private void verifyConfig(SecurityConfigBean config) {
        Assert.assertNotNull(config);

        LdapConfig ldapConfig = config.getLdapConfig();
        Set<Integer> acctIdsSet = config.getAcctIds();

        Assert.assertNotNull(ldapConfig);
        Assert.assertNotNull(acctIdsSet);

        Assert.assertNotNull(ldapConfig.getLdapBaseDn());
        Assert.assertNotNull(ldapConfig.getLdapUserDn());
        Assert.assertNotNull(ldapConfig.getLdapPassword());
        Assert.assertNotNull(ldapConfig.getLdapUrl());

        Assert.assertEquals(basedn, ldapConfig.getLdapBaseDn());
        Assert.assertEquals(userdn, ldapConfig.getLdapUserDn());
        Assert.assertEquals(password, ldapConfig.getLdapPassword());
        Assert.assertEquals(url, ldapConfig.getLdapUrl());

        Assert.assertEquals(acctIds.size(), acctIdsSet.size());
        for (Integer acctId : acctIds) {
            Assert.assertTrue(acctIdsSet.contains(acctId));
        }
    }

}