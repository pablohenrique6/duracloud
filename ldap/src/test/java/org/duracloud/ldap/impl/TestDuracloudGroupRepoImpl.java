/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.impl;

import junit.framework.Assert;
import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.error.DBNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.ldap.core.LdapTemplate;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 6/7/12
 */
public class TestDuracloudGroupRepoImpl extends BaseTestDuracloudRepoImpl {

    private DuracloudGroupRepoImpl repo;
    private LdapTemplate ldapTemplate;

    private Set<Integer> ids;
    private static final int accountId = 7;
    private static Set<Integer> userIds;


    @BeforeClass
    public static void initialize() throws Exception {
        userIds = new HashSet<Integer>();
        userIds.add(6);
        userIds.add(3);
        userIds.add(9);
    }

    @Before
    public void setUp() throws Exception {
        ids = new HashSet<Integer>();

        ldapTemplate = new LdapTemplate(getContextSource());
        repo = new DuracloudGroupRepoImpl(ldapTemplate);
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
        new DuracloudGroupRepoImpl(ldapTemplate).removeDn();
    }

    @Test
    public void testFindById() throws Exception {
        int id = generateId();
        DuracloudGroup group = createGroup(id, accountId);

        // Create test object
        verifyExists(id, false);
        repo.save(group);
        verifyExists(id, true);

        // Perform the test
        DuracloudGroup found = repo.findById(group.getId());
        verifyGroup(group, found);
    }

    private void verifyGroup(DuracloudGroup group, DuracloudGroup found) {
        Assert.assertNotNull(found);

        Assert.assertEquals(group.getId(), found.getId());
        Assert.assertEquals(group.getName(), found.getName());
        Assert.assertEquals(group.getAccountId(), found.getAccountId());

        Set<Integer> groupUserIds = group.getUserIds();
        Set<Integer> foundUserIds = found.getUserIds();

        Assert.assertEquals(groupUserIds.size(), foundUserIds.size());
        for (Integer userId : groupUserIds) {
            Assert.assertTrue(foundUserIds.contains(userId));
        }
    }

    @Test
    public void testFindByAccountId() throws Exception {
        DuracloudGroup groupA = createGroup(generateId(), accountId);
        DuracloudGroup groupB = createGroup(generateId(), accountId);
        DuracloudGroup groupC = createGroup(generateId(), accountId + 1);

        repo.save(groupA);
        repo.save(groupB);
        repo.save(groupC); // In a different account

        Set<DuracloudGroup> expectedGroups = new HashSet<DuracloudGroup>();
        expectedGroups.add(groupA);
        expectedGroups.add(groupB);

        Set<DuracloudGroup> groups = repo.findByAccountId(accountId);

        Assert.assertEquals(expectedGroups.size(), groups.size());

        for (DuracloudGroup expectedGroup : expectedGroups) {
            Assert.assertTrue(groups.contains(expectedGroup));
        }
    }

    @Test
    public void testFindInAccountByGroupname() throws DBNotFoundException {
        int idA = generateId();
        int idB = generateId();
        int idC = generateId();
        DuracloudGroup groupA = createGroup(idA, accountId + idA);
        DuracloudGroup groupB = createGroup(idB, accountId + idB);
        DuracloudGroup groupC = createGroup(idC, accountId + idC);

        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();
        groups.add(groupA);
        groups.add(groupB);
        groups.add(groupC);

        repo.save(groupA);
        repo.save(groupB);
        repo.save(groupC);

        DuracloudGroup found;
        for (DuracloudGroup group : groups) {
            verifyExists(group.getId(), true);

            found = repo.findInAccountByGroupname(group.getName(),
                                                  group.getAccountId());
            Assert.assertNotNull(found);
            verifyGroup(group, found);
        }
    }

    @Test
    public void testFindAllGroups() {
        DuracloudGroup groupA = createGroup(generateId(), accountId);
        DuracloudGroup groupB = createGroup(generateId(), accountId);
        DuracloudGroup groupC = createGroup(generateId(), accountId);

        repo.save(groupA);
        repo.save(groupB);
        repo.save(groupC);

        Set<DuracloudGroup> expectedGroups = new HashSet<DuracloudGroup>();
        expectedGroups.add(groupA);
        expectedGroups.add(groupB);
        expectedGroups.add(groupC);

        Set<DuracloudGroup> groups = repo.findAllGroups();
        Assert.assertEquals(expectedGroups.size(), groups.size());

        for (DuracloudGroup expectedGroup : expectedGroups) {
            Assert.assertTrue(groups.contains(expectedGroup));
        }
    }


    @Test
    public void testSave() throws Exception {
        int id = generateId();
        DuracloudGroup group = createGroup(id, accountId);

        verifyExists(group.getId(), false);

        // Perform test
        repo.save(group);

        verifyExists(group.getId(), true);
    }

    @Test
    public void testDelete() throws Exception {
        int id = generateId();
        DuracloudGroup group = createGroup(id, accountId);

        // Create test item
        verifyExists(group.getId(), false);
        repo.save(group);
        verifyExists(group.getId(), true);

        // Perform test
        repo.delete(group.getId());

        verifyExists(group.getId(), false);
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
            repo.save(createGroup(generateId(), accountId));
        }

        foundIds = repo.getIds();
        Assert.assertNotNull(foundIds);
        Assert.assertEquals(numItems, foundIds.size());

        for (int x : ids) {
            Assert.assertTrue(foundIds.contains(x));
        }
    }

    private int generateId() {
        int id = new Random().nextInt(999999999);
        ids.add(id);
        return id;
    }

    private DuracloudGroup createGroup(int id, int acctId) {
        int uniqueIdentifier = id;
        String cn = DuracloudGroup.PREFIX + id;

        return new DuracloudGroup(uniqueIdentifier, cn, acctId, userIds);
    }

}
