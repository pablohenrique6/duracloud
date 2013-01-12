/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import junit.framework.Assert;
import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.ldap.Ldap;
import org.duracloud.ldap.domain.LdapConfig;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 1/12/13
 */
public class LdapCachingImplTest {

    private LdapCachingImpl cache;

    private Ldap target;
    private int refreshSeconds = 1;

    @Before
    public void setUp() throws Exception {
        target = EasyMock.createMock("Ldap", Ldap.class);

        cache = new LdapCachingImpl(target, refreshSeconds);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(target);
    }

    private void replayMocks() {
        EasyMock.replay(target);
    }

    @Test
    public void testInitialize() throws Exception {
        LdapConfig config = new LdapConfig();
        target.initialize(config);
        EasyMock.expectLastCall();
        replayMocks();

        // Perform test.
        cache.initialize(config);
    }

    @Test
    public void testGetSecurityUser() throws Exception {
        String username = "user-name";
        Set<Integer> accts = new HashSet<>();

        SecurityUserBean bean = new SecurityUserBean(username, null, null);
        EasyMock.expect(target.getSecurityUser(username, accts))
                .andReturn(bean);

        replayMocks();

        // Perform test.
        SecurityUserBean user = cache.getSecurityUser(username, accts);
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user.getUsername());
    }


    @Test
    public void testGetSecurityUserAfterRefresh() throws Exception {
        String username = "user-name";
        Set<Integer> accts = new HashSet<>();

        SecurityUserBean bean = new SecurityUserBean(username, null, null);
        EasyMock.expect(target.getSecurityUser(username, accts))
                .andReturn(bean)
                .times(2);

        replayMocks();

        // Perform test.
        SecurityUserBean user = cache.getSecurityUser(username, accts);
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user.getUsername());

        // Will hit cache.
        user = cache.getSecurityUser(username, accts);
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user.getUsername());

        // Let cache expire.
        Thread.sleep(refreshSeconds * 1000);
        user = cache.getSecurityUser(username, accts);
        Assert.assertNotNull(user);
        Assert.assertEquals(username, user.getUsername());

    }

    @Test
    public void testGetSecurityUsers() throws Exception {
        Set<Integer> accts = new HashSet<>();
        SecurityUserBean bean = new SecurityUserBean();
        List<SecurityUserBean> beans = new ArrayList<>();
        beans.add(bean);

        EasyMock.expect(target.getSecurityUsers(accts)).andReturn(beans);

        replayMocks();

        // Perform test.
        List<SecurityUserBean> users = cache.getSecurityUsers(accts);
        Assert.assertNotNull(users);
        Assert.assertEquals(beans.size(), users.size());
    }

    @Test
    public void testGetSecurityUsersAfterRefresh() throws Exception {
        Set<Integer> accts = new HashSet<>();
        SecurityUserBean bean = new SecurityUserBean();
        List<SecurityUserBean> beans = new ArrayList<>();
        beans.add(bean);

        EasyMock.expect(target.getSecurityUsers(accts))
                .andReturn(beans)
                .times(2);

        replayMocks();

        // Perform test.
        List<SecurityUserBean> users = cache.getSecurityUsers(accts);
        Assert.assertNotNull(users);
        Assert.assertEquals(beans.size(), users.size());

        // Will hit cache.
        users = cache.getSecurityUsers(accts);
        Assert.assertNotNull(users);
        Assert.assertEquals(beans.size(), users.size());

        // Let cache expire.
        Thread.sleep(refreshSeconds * 1000);
        users = cache.getSecurityUsers(accts);
        Assert.assertNotNull(users);
        Assert.assertEquals(beans.size(), users.size());
    }
}
