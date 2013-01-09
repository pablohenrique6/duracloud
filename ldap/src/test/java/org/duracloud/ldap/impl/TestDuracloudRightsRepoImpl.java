/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.impl;

import junit.framework.Assert;
import org.duracloud.ldap.domain.AccountRights;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.error.DBNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import static org.duracloud.ldap.domain.Role.ROLE_ROOT;
import static org.duracloud.ldap.impl.DuracloudRightsRepoImpl.WILDCARD_ACCT_ID;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 6/7/12
 */
public class TestDuracloudRightsRepoImpl extends BaseTestDuracloudRepoImpl {

    private DuracloudRightsRepoImpl repo;
    private LdapTemplate ldapTemplate;

    private static final int acctId = 7;
    private static final int userId = 11;

    private Set<Integer> ids;

    @Before
    public void setUp() throws Exception {
        ids = new HashSet<Integer>();

        ldapTemplate = new LdapTemplate(getContextSource());
        repo = new DuracloudRightsRepoImpl(ldapTemplate);
    }

    @After
    public void tearDown() {
        for (int id : ids) {
            try {
                repo.delete(id);
            } catch (Exception e) {
                // do nothing
            }
        }
    }

    @AfterClass
    public static void afterClass() {
        LdapTemplate ldapTemplate = null;
        try {
            ldapTemplate = new LdapTemplate(getContextSource());
        } catch (Exception e) {
            // do nothing
            e.printStackTrace();
        }
        new DuracloudRightsRepoImpl(ldapTemplate).removeDn();
    }

    @Test
    public void testFindById() throws Exception {
        int id = generateId();
        AccountRights rights = createRights(id, acctId, userId);

        // Create test object
        verifyExists(id, false);
        repo.save(rights);
        verifyExists(id, true);

        // Perform the test
        AccountRights found = repo.findById(rights.getId());
        verifyRights(rights, found);
    }

    private void verifyRights(AccountRights rights, AccountRights found) {
        Assert.assertNotNull(found);

        Assert.assertEquals(rights.getId(), found.getId());
        Assert.assertEquals(rights.getAccountId(), found.getAccountId());
        Assert.assertEquals(rights.getUserId(), found.getUserId());

        Set<Role> acctRoles = rights.getRoles();
        Set<Role> foundRoles = found.getRoles();

        Assert.assertEquals(acctRoles.size(), foundRoles.size());
        for (Role role : acctRoles) {
            Assert.assertTrue(foundRoles.contains(role));
        }
    }

    @Test
    public void testFindByAccountId() throws Exception {
        AccountRights rightsA = createRights(generateId(), acctId, userId + 1);
        AccountRights rightsB = createRights(generateId(), acctId, userId + 2);
        AccountRights rightsC = createRights(generateId(),
                                             acctId + 1,
                                             userId + 3);

        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC); // In a different account

        Set<AccountRights> expectedRights = new HashSet<AccountRights>();
        expectedRights.add(rightsA);
        expectedRights.add(rightsB);

        Set<AccountRights> rights = repo.findByAccountId(
            TestDuracloudRightsRepoImpl.acctId);

        Assert.assertEquals(expectedRights.size(), rights.size());
        for (AccountRights expectedRight : expectedRights) {
            Assert.assertTrue(rights.contains(expectedRight));
        }
    }

    @Test
    public void testFindByAccountIdWithRoot() throws Exception {
        AccountRights rightsA = createRights(generateId(), acctId, userId + 1);
        AccountRights rightsB = createRights(generateId(), acctId, userId + 2);
        AccountRights rightsC = createRights(generateId(),
                                             acctId + 1,
                                             userId + 3);
        AccountRights rightsR = createRights(generateId(),
                                             WILDCARD_ACCT_ID,
                                             userId,
                                             ROLE_ROOT);
        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC); // In a different account
        repo.save(rightsR); // root

        Set<AccountRights> expectedRights = new HashSet<AccountRights>();
        expectedRights.add(rightsA);
        expectedRights.add(rightsB);
        expectedRights.add(rightsR);

        Set<AccountRights> rights = repo.findByAccountId(acctId);

        Assert.assertEquals(expectedRights.size(), rights.size());
        for (AccountRights expectedRight : expectedRights) {
            boolean found = false;
            for (AccountRights right : rights) {
                if (expectedRight.getId() == right.getId()) {
                    found = true;
                    Assert.assertEquals(expectedRight.getUserId(),
                                        right.getUserId());
                    Assert.assertEquals(acctId, right.getAccountId());
                }
            }
            Assert.assertTrue(expectedRight.toString(), found);
        }
    }

    @Test
    public void testSave() throws Exception {
        int id = generateId();
        AccountRights rights = createRights(id, acctId, userId);

        verifyExists(rights.getId(), false);

        // Perform test
        repo.save(rights);

        verifyExists(rights.getId(), true);
    }

    @Test
    public void testDelete() throws Exception {
        int id = generateId();
        AccountRights rights = createRights(id, acctId, userId);

        // Create test item
        verifyExists(rights.getId(), false);
        repo.save(rights);
        verifyExists(rights.getId(), true);

        // Perform test
        repo.delete(rights.getId());

        verifyExists(rights.getId(), false);
    }

    private void verifyExists(int id, boolean exists) {
        Object item = null;
        try {
            item = repo.findById(id);
            Assert.assertTrue("id = " + id, exists);

        } catch (DBNotFoundException e) {
            Assert.assertFalse("id = " + id, exists);
        }
        Assert.assertEquals("id = " + id, null != item, exists);
    }

    @Test
    public void testGetIds() throws Exception {
        Set<Integer> foundIds = repo.getIds();
        Assert.assertNotNull(foundIds);
        Assert.assertEquals(0, foundIds.size());

        final int numItems = 4;
        for (int i = 0; i < numItems; ++i) {
            repo.save(createRights(generateId(), acctId, userId));
        }

        foundIds = repo.getIds();
        Assert.assertNotNull(foundIds);
        Assert.assertEquals(numItems, foundIds.size());

        for (int x : ids) {
            Assert.assertTrue(foundIds.contains(x));
        }
    }

    @Test
    public void testFindAccountRightsForUser() throws Exception {
        doFindByAccountIdAndUserId();
    }

    private void doFindByAccountIdAndUserId() throws Exception {
        final int acctId1 = acctId + 1;
        final int acctId2 = acctId + 2;
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;

        AccountRights rightsA = createRights(generateId(), acctId1, userId1);
        AccountRights rightsB = createRights(generateId(), acctId1, userId2);
        AccountRights rightsC = createRights(generateId(), acctId2, userId1);
        AccountRights rightsD = createRights(generateId(), acctId2, userId2);

        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC);
        repo.save(rightsD);

        // Perform test
        AccountRights rights = repo.findAccountRightsForUser(acctId1, userId2);
        Assert.assertNotNull(rights);

        verifyRights(rightsB, rights);
    }

    @Test
    public void testFindAccountRightsForUserError() throws Exception {
        doFindByAccountIdAndUserIdError();
    }

    private void doFindByAccountIdAndUserIdError() throws DBNotFoundException {
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;

        AccountRights rightsA = createRights(generateId(), acctId, userId1);
        AccountRights rightsB = createRights(generateId(), acctId, userId2);
        AccountRights rightsC = createRights(generateId(), acctId, userId2);

        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC);

        // Perform test
        try {
            repo.findAccountRightsForUser(acctId, userId);
            Assert.fail("exception expected");

        } catch (DBNotFoundException e) {
            String msg = e.getMessage();
            Assert.assertNotNull(msg);
            Assert.assertTrue(msg.contains("No rights found"));
        }
    }

    @Test
    public void testFindAccountRightsForUserRoot() throws Exception {
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;
        final int userIdR = userId + 3;

        AccountRights rightsA = createRights(generateId(), acctId, userId1);
        AccountRights rightsB = createRights(generateId(), acctId, userId2);
        AccountRights rightsC = createRights(generateId(), acctId, userId2);
        // Adding a root user, but not root on this account.
        AccountRights rightsD = createRights(generateId(), acctId, userIdR);
        AccountRights rightsR = createRights(generateId(),
                                             WILDCARD_ACCT_ID,
                                             userIdR,
                                             ROLE_ROOT);
        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC);
        repo.save(rightsD);
        repo.save(rightsR);

        // Perform test
        AccountRights rights = repo.findAccountRightsForUser(acctId, userIdR);
        Assert.assertNotNull(rights);

        AccountRights expected = createRights(rightsR.getId(),
                                              acctId,
                                              rightsR.getUserId(),
                                              ROLE_ROOT);
        verifyRights(expected, rights); // Wildcard root should be found
    }

    @Test
    public void testFindByAccountIdAndUserId() throws Exception {
        doFindByAccountIdAndUserId();
    }

    @Test
    public void testFindByAccountIdAndUserIdError() throws Exception {
        doFindByAccountIdAndUserIdError();
    }

    @Test
    public void testFindByAccountIdAndUserIdRoot() throws Exception {
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;
        final int userIdR = userId + 3;

        AccountRights rightsA = createRights(generateId(), acctId, userId1);
        AccountRights rightsB = createRights(generateId(), acctId, userId2);
        AccountRights rightsC = createRights(generateId(), acctId, userId2);
        // Adding a root user, but not root on this account.
        AccountRights rightsD = createRights(generateId(), acctId, userIdR);
        AccountRights rightsR = createRights(generateId(),
                                             WILDCARD_ACCT_ID,
                                             userIdR,
                                             ROLE_ROOT);
        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC);
        repo.save(rightsD);
        repo.save(rightsR);

        // Perform test
        AccountRights rights = repo.findByAccountIdAndUserId(acctId, userIdR);
        Assert.assertNotNull(rights);
        verifyRights(rightsD, rights); // Non-wildcard root should be found
    }

    @Test
    public void testFindByAccountIdSkipRoot() throws Exception {
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;
        final int userIdR = userId + 3;
        final int userId4 = userId + 4;

        AccountRights rightsA = createRights(generateId(), acctId, userId1);
        AccountRights rightsB = createRights(generateId(), acctId, userId2);
        AccountRights rightsR = createRights(generateId(),
                                             acctId + 1,
                                             userIdR,
                                             ROLE_ROOT);
        AccountRights rightsD = createRights(generateId(), acctId, userId4);

        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsR);
        repo.save(rightsD);

        Set<AccountRights> expectedRights = new HashSet<AccountRights>();
        expectedRights.add(rightsA);
        expectedRights.add(rightsB);
        expectedRights.add(rightsD);

        // Perform test
        Set<AccountRights> rights = repo.findByAccountIdSkipRoot(acctId);

        Assert.assertNotNull(rights);

        Assert.assertEquals(expectedRights.size(), rights.size());
        for (AccountRights expectedRight : expectedRights) {
            Assert.assertTrue(rights.contains(expectedRight));
        }
    }

    @Test
    public void testFindByAccountIdSkipRootWithRoot() throws Exception {
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;
        final int userId3 = userId + 3;
        final int userId4 = userId + 4;
        final int userIdR = userId + 5;

        AccountRights rightsA = createRights(generateId(), acctId, userId1);
        AccountRights rightsB = createRights(generateId(), acctId, userId2);
        AccountRights rightsC = createRights(generateId(),
                                             acctId + 1,
                                             userId3,
                                             ROLE_ROOT);
        AccountRights rightsD = createRights(generateId(), acctId, userId4);
        AccountRights rightsR = createRights(generateId(),
                                             WILDCARD_ACCT_ID,
                                             userIdR,
                                             ROLE_ROOT);
        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC); // root, but not on wildcard acct
        repo.save(rightsD);
        repo.save(rightsR);

        Set<AccountRights> expectedRights = new HashSet<AccountRights>();
        expectedRights.add(rightsA);
        expectedRights.add(rightsB);
        expectedRights.add(rightsD);

        // Perform test
        Set<AccountRights> rights = repo.findByAccountIdSkipRoot(acctId);

        Assert.assertNotNull(rights);

        Assert.assertEquals(expectedRights.size(), rights.size());
        for (AccountRights expectedRight : expectedRights) {
            Assert.assertTrue(rights.contains(expectedRight));
        }
    }

    @Test
    public void testFindByUserId() throws Exception {
        final int acctId1 = acctId + 1;
        final int acctId2 = acctId + 2;
        final int userId1 = userId + 1;
        final int userId2 = userId + 2;

        AccountRights rightsA = createRights(generateId(), acctId1, userId1);
        AccountRights rightsB = createRights(generateId(), acctId1, userId2);
        AccountRights rightsC = createRights(generateId(), acctId2, userId1);
        AccountRights rightsD = createRights(generateId(), acctId2, userId2);

        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC);
        repo.save(rightsD);

        Set<AccountRights> expectedRights = new HashSet<AccountRights>();
        expectedRights.add(rightsA);
        expectedRights.add(rightsC);

        // Perform test
        Set<AccountRights> rights = repo.findByUserId(userId1);
        Assert.assertNotNull(rights);

        Assert.assertEquals(expectedRights.size(), rights.size());
        for (AccountRights expectedRight : expectedRights) {
            Assert.assertTrue(rights.contains(expectedRight));
        }
    }

    @Test
    public void testFindByUserIdRoot() throws Exception {
        final int acctId1 = acctId + 1;
        final int acctId2 = acctId + 2;
        final int acctId3 = acctId + 3;
        final int acctId4 = acctId + 4;
        final int acctId5 = acctId + 5;
        final int userId1 = userId + 1;
        final int userId2 = userId + 2; // root user

        AccountRights rightsA = createRights(generateId(), acctId1, userId1);
        AccountRights rightsB = createRights(generateId(), acctId1, userId2);
        AccountRights rightsC = createRights(generateId(), acctId2, userId1);
        // Set root role
        AccountRights rightsD = createRights(generateId(),
                                             acctId2,
                                             userId2,
                                             ROLE_ROOT);
        AccountRights rightsE = createRights(generateId(), acctId3, userId1);
        AccountRights rightsF = createRights(generateId(), acctId4, userId1);
        AccountRights rightsG = createRights(generateId(), acctId5, userId1);

        repo.save(rightsA);
        repo.save(rightsB);
        repo.save(rightsC);
        repo.save(rightsD);
        repo.save(rightsE);
        repo.save(rightsF);
        repo.save(rightsG);

        Set<AccountRights> expectedRights = new HashSet<AccountRights>();
        expectedRights.add(rightsA);
        expectedRights.add(rightsC);
        expectedRights.add(rightsE);
        expectedRights.add(rightsF);
        expectedRights.add(rightsG);

        // Perform test
        Set<AccountRights> rights = repo.findByUserId(userId2);
        Assert.assertNotNull(rights);

        Assert.assertEquals(expectedRights.size(), rights.size());
        for (AccountRights expectedRight : expectedRights) {
            boolean found = false;
            for (AccountRights right : rights) {
                if (right.getAccountId() == expectedRight.getAccountId() &&
                    right.getUserId() == userId2 && // root user
                    right.getRoles().contains(ROLE_ROOT)) {
                    found = true;
                }
            }
            Assert.assertTrue("AccountRight !found: " + expectedRight, found);
        }
    }

    private int generateId() {
        int id = new Random().nextInt(999999999);
        ids.add(id);
        return id;
    }

    private AccountRights createRights(int id, int acctId, int uId) {
        return createRights(id, acctId, uId, Role.ROLE_ADMIN);
    }

    private AccountRights createRights(int id, int acctId, int uId, Role role) {
        Set<Role> roles = role.getRoleHierarchy();
        return new AccountRights(id, acctId, uId, roles);
    }

}
