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
import org.duracloud.ldap.DuracloudGroupRepo;
import org.duracloud.ldap.DuracloudRepoMgr;
import org.duracloud.ldap.DuracloudRightsRepo;
import org.duracloud.ldap.DuracloudUserRepo;
import org.duracloud.ldap.domain.AccountRights;
import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.domain.DuracloudUser;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.error.DBNotFoundException;
import org.duracloud.ldap.error.DBUninitializedException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 1/4/13
 */
public class LdapImplTest {

    private LdapImpl ldap;

    private DuracloudRepoMgr repoMgr;
    private DuracloudUserRepo userRepo;
    private DuracloudGroupRepo groupRepo;
    private DuracloudRightsRepo rightsRepo;

    private static final int USER_ID = 5;

    private Set<Integer> accountIds;
    private List<AccountRights> rights;
    private Map<Integer, Set<DuracloudGroup>> groupSets;

    private static final int NUM_ACCTS = 4;
    private static final int NUM_GROUPS = 3;

    @Before
    public void setUp() throws Exception {
        accountIds = new HashSet<Integer>();
        rights = new ArrayList<AccountRights>();
        groupSets = new HashMap<Integer, Set<DuracloudGroup>>();

        userRepo = EasyMock.createMock("DuracloudUserRepo",
                                       DuracloudUserRepo.class);
        groupRepo = EasyMock.createMock("DuracloudGroupRepo",
                                        DuracloudGroupRepo.class);
        rightsRepo = EasyMock.createMock("DuracloudRightsRepo",
                                         DuracloudRightsRepo.class);

        repoMgr = EasyMock.createMock("DuracloudRepoMgr",
                                      DuracloudRepoMgr.class);
        ldap = new LdapImpl(repoMgr);

    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(repoMgr, userRepo, groupRepo, rightsRepo);
    }

    private void replayMocks() {
        EasyMock.replay(repoMgr, userRepo, groupRepo, rightsRepo);
    }

    @Test
    public void testGetSecurityUserUninitialized() throws Exception {
        EasyMock.expect(repoMgr.getUserRepo())
                .andThrow(new DBUninitializedException("canned-exception"));

        replayMocks();

        ldap = new LdapImpl(repoMgr);

        boolean thrown = false;
        try {
            ldap.getSecurityUser("user-name", accountIds);
            Assert.fail("exception expected");
        } catch (DBUninitializedException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testGetSecurityUser() throws Exception {
        String username = createMocksGetSecurityUser();
        replayMocks();

        // Perform test
        SecurityUserBean userBean = ldap.getSecurityUser(username, accountIds);
        Assert.assertNotNull(userBean);

        // Verify user.
        Assert.assertEquals(username, userBean.getUsername());

        // Verify rights.
        List<String> grants = userBean.getGrantedAuthorities();
        Set<Role> expectedRoles = Role.ROLE_ADMIN.getRoleHierarchy();
        Assert.assertNotNull(grants);
        Assert.assertEquals(expectedRoles.size(), grants.size());

        for (Role role : expectedRoles) {
            Assert.assertTrue(grants.contains(role.name()));
        }

        // Verify groups.
        Assert.assertEquals(NUM_ACCTS * NUM_GROUPS,
                            userBean.getGroups().size());
    }

    private String createMocksGetSecurityUser() throws DBNotFoundException {
        for (int i = 0; i < NUM_ACCTS; ++i) {
            accountIds.add(i);

            Role role = (i == 1) ? Role.ROLE_USER : Role.ROLE_ADMIN;
            rights.add(new AccountRights(i,
                                         i,
                                         USER_ID,
                                         role.getRoleHierarchy()));
        }

        Set<Integer> userIds = new HashSet<Integer>();
        userIds.add(USER_ID);

        for (int accountId : accountIds) {
            Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();

            for (int i = 0; i < NUM_GROUPS; ++i) {
                groups.add(new DuracloudGroup(accountId * i + i,
                                              "group-test-" + accountId + ":" +
                                                  i,
                                              accountId,
                                              userIds));
            }
            groupSets.put(accountId, groups);
        }

        int userId = 5;
        String username = "user-name";
        DuracloudUser user = createUser(userId, username);

        // Get user.
        EasyMock.expect(repoMgr.getUserRepo()).andReturn(userRepo);
        EasyMock.expect(userRepo.findByUsername(username)).andReturn(user);

        // Get rights.
        EasyMock.expect(repoMgr.getRightsRepo()).andReturn(rightsRepo).times(
            NUM_ACCTS);
        for (int accountId : accountIds) {
            EasyMock.expect(rightsRepo.findAccountRightsForUser(accountId,
                                                                userId))
                    .andReturn(rights.get(accountId));
        }

        // Get groups.
        EasyMock.expect(repoMgr.getGroupRepo()).andReturn(groupRepo).times(
            NUM_ACCTS);
        for (int accountId : accountIds) {
            EasyMock.expect(groupRepo.findByAccountId(accountId)).andReturn(
                groupSets.get(accountId));
        }
        return username;
    }

    @Test
    public void testGetSecurityUser2() throws Exception {
        String username = createMocksGetSecurityUser2();
        replayMocks();

        // Perform test
        SecurityUserBean userBean = ldap.getSecurityUser(username, accountIds);
        Assert.assertNotNull(userBean);

        // Verify user.
        Assert.assertEquals(username, userBean.getUsername());

        // Verify rights.
        List<String> grants = userBean.getGrantedAuthorities();
        Set<Role> expectedRoles = Role.ROLE_USER.getRoleHierarchy();
        Assert.assertNotNull(grants);
        Assert.assertEquals(expectedRoles.size(), grants.size());

        for (Role role : expectedRoles) {
            Assert.assertTrue(grants.contains(role.name()));
        }

        // Verify groups.
        Assert.assertEquals(NUM_GROUPS, userBean.getGroups().size());
    }

    private String createMocksGetSecurityUser2() throws DBNotFoundException {
        for (int i = 0; i < NUM_ACCTS; ++i) {
            accountIds.add(i);

            Role role = Role.ROLE_USER;
            if (i != 0) {
                rights.add(new AccountRights(i,
                                             i,
                                             USER_ID,
                                             role.getRoleHierarchy()));
            }
        }

        Set<Integer> userIds = new HashSet<Integer>();
        userIds.add(USER_ID);

        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();

        int accountId = 1;
        for (int i = 0; i < NUM_GROUPS; ++i) {
            groups.add(new DuracloudGroup(accountId * i + i,
                                          "group-test-" + accountId + ":" +
                                              i,
                                          accountId,
                                          userIds));
        }
        groupSets.put(accountId, groups);

        int userId = 5;
        String username = "user-name";
        DuracloudUser user = createUser(userId, username);

        // Get user.
        EasyMock.expect(repoMgr.getUserRepo()).andReturn(userRepo);
        EasyMock.expect(userRepo.findByUsername(username)).andReturn(user);

        // Get rights.
        EasyMock.expect(repoMgr.getRightsRepo()).andReturn(rightsRepo).times(
            NUM_ACCTS);
        for (int acctId : accountIds) {
            if (acctId == accountId) {
                EasyMock.expect(rightsRepo.findAccountRightsForUser(acctId,
                                                                    userId))
                        .andReturn(rights.get(acctId));
            } else {
                EasyMock.expect(rightsRepo.findAccountRightsForUser(acctId,
                                                                    userId))
                        .andThrow(new DBNotFoundException("canned-exception"));
            }
        }

        // Get groups.
        EasyMock.expect(repoMgr.getGroupRepo()).andReturn(groupRepo).times(
            NUM_ACCTS);
        for (int acctId : accountIds) {
            EasyMock.expect(groupRepo.findByAccountId(acctId)).andReturn(
                groupSets.get(acctId));
        }
        return username;
    }


    @Test
    public void testGetSecurityUsers() throws Exception {
        int numUsers = 3;
        createMocksGetSecurityUsers(numUsers);
        replayMocks();

        List<SecurityUserBean> userBeans = ldap.getSecurityUsers(accountIds);
        Assert.assertNotNull(userBeans);

        Assert.assertEquals(numUsers, userBeans.size());
        SecurityUserBean userBean = userBeans.get(0);

        // Verify rights.
        List<String> grants = userBean.getGrantedAuthorities();
        Set<Role> expectedRoles = Role.ROLE_ADMIN.getRoleHierarchy();
        Assert.assertNotNull(grants);
        Assert.assertEquals(expectedRoles.size(), grants.size());

        for (Role role : expectedRoles) {
            Assert.assertTrue(grants.contains(role.name()));
        }

        // Verify groups.
        Assert.assertEquals(NUM_GROUPS, userBean.getGroups().size());
    }

    private String createMocksGetSecurityUsers(int numUsers)
        throws DBNotFoundException {
        Map<Integer, Set<AccountRights>> rightsMap =
            new HashMap<Integer, Set<AccountRights>>();
        for (int i = 0; i < NUM_ACCTS; ++i) {
            Set<AccountRights> rightsSet = new HashSet<AccountRights>();

            accountIds.add(i);

            for (int u = 0; u < numUsers; ++u) {
                Role role = Role.ROLE_ADMIN;
                AccountRights right = new AccountRights(i,
                                                        i,
                                                        USER_ID + u,
                                                        role.getRoleHierarchy());
                rights.add(right);
                rightsSet.add(right);
            }
            rightsMap.put(i, rightsSet);
        }

        Set<Integer> userIds = new HashSet<Integer>();
        userIds.add(USER_ID);

        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();

        int accountId = 1;
        for (int i = 0; i < NUM_GROUPS; ++i) {
            groups.add(new DuracloudGroup(accountId * i + i,
                                          "group-test-" + accountId + ":" +
                                              i,
                                          accountId,
                                          userIds));
        }
        groupSets.put(accountId, groups);

        int userId = 5;
        String username = "user-name";
        DuracloudUser userA = createUser(userId, username);
        DuracloudUser userB = createUser(userId + 1, username + 1);
        DuracloudUser userC = createUser(userId + 2, username + 2);

        // Get user.
        EasyMock.expect(repoMgr.getUserRepo()).andReturn(userRepo).times(
            numUsers);
        EasyMock.expect(userRepo.findById(USER_ID)).andReturn(userA);
        EasyMock.expect(userRepo.findById(USER_ID + 1)).andReturn(userB);
        EasyMock.expect(userRepo.findById(USER_ID + 2)).andReturn(userC);

        // Get rights.
        EasyMock.expect(repoMgr.getRightsRepo()).andReturn(rightsRepo).times(
            NUM_ACCTS + (numUsers * NUM_ACCTS));

        for (int acctId : accountIds) {
            EasyMock.expect(rightsRepo.findByAccountId(acctId)).andReturn(
                rightsMap.get(acctId));

            for (int u = 0; u < numUsers; ++u) {
                EasyMock.expect(rightsRepo.findAccountRightsForUser(acctId,
                                                                    USER_ID +
                                                                        u))
                        .andReturn(rights.get(acctId));
            }
        }

        // Get groups.
        EasyMock.expect(repoMgr.getGroupRepo()).andReturn(groupRepo).times(
            NUM_ACCTS * numUsers);
        for (int acctId : accountIds) {
            EasyMock.expect(groupRepo.findByAccountId(acctId)).andReturn(
                groupSets.get(acctId)).times(numUsers);
        }
        return username;
    }

    private DuracloudUser createUser(int userId, String username) {
        String none = "mock-text";
        return new DuracloudUser(userId,
                                 username,
                                 none,
                                 none,
                                 none,
                                 none,
                                 none,
                                 none);
    }
}
