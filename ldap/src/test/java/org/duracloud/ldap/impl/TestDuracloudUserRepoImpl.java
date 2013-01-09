/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.impl;

import junit.framework.Assert;
import org.duracloud.ldap.domain.DuracloudUser;
import org.duracloud.ldap.error.DBNotFoundException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
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
public class TestDuracloudUserRepoImpl extends BaseTestDuracloudRepoImpl {

    private DuracloudUserRepoImpl repo;
    private LdapTemplate ldapTemplate;

    private Set<Integer> ids;


    @Before
    public void setUp() throws Exception {
        ids = new HashSet<Integer>();

        ldapTemplate = new LdapTemplate(getContextSource());
        repo = new DuracloudUserRepoImpl(ldapTemplate);
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
        new DuracloudUserRepoImpl(ldapTemplate).removeDn();
    }

    @Test
    public void testFindById() throws Exception {
        int id = generateId();
        DuracloudUser user = createUser(id);

        // Create test object
        verifyExists(id, false);
        repo.save(user);
        verifyExists(id, true);

        // Perform the test
        DuracloudUser found = repo.findById(user.getId());
        verifyUser(user, found);
    }

    @Test
    public void testFindByUsername() throws Exception {
        int id = generateId();
        DuracloudUser user = createUser(id);

        // Create test object
        verifyExists(id, false);
        repo.save(user);
        verifyExists(id, true);

        // Perform the test
        DuracloudUser found = repo.findByUsername(user.getUsername());
        verifyUser(user, found);
    }

    private void verifyUser(DuracloudUser user, DuracloudUser found) {
        Assert.assertNotNull(found);

        Assert.assertEquals(user.getId(), found.getId());
        Assert.assertEquals(user.getUsername(), found.getUsername());
        Assert.assertEquals(user.getFirstName(), found.getFirstName());
        Assert.assertEquals(user.getLastName(), found.getLastName());
        Assert.assertEquals(user.getPassword(), found.getPassword());
        Assert.assertEquals(user.getEmail(), found.getEmail());
        Assert.assertEquals(user.getSecurityQuestion(),
                            found.getSecurityQuestion());
        Assert.assertEquals(user.getSecurityAnswer(),
                            found.getSecurityAnswer());
        Assert.assertEquals(user.isEnabled(), found.isEnabled());
        Assert.assertEquals(user.isAccountNonExpired(),
                            found.isAccountNonExpired());
        Assert.assertEquals(user.isCredentialsNonExpired(),
                            found.isCredentialsNonExpired());
        Assert.assertEquals(user.isAccountNonLocked(),
                            found.isAccountNonLocked());
    }

    @Test
    public void testSave() throws Exception {
        int id = generateId();
        DuracloudUser user = createUser(id);

        verifyExists(user.getId(), false);

        // Perform test
        repo.save(user);

        verifyExists(user.getId(), true);
    }

    @Test
    public void testDelete() throws Exception {
        int id = generateId();
        DuracloudUser user = createUser(id);

        // Create test item
        verifyExists(user.getId(), false);
        repo.save(user);
        verifyExists(user.getId(), true);

        // Perform test
        repo.delete(user.getId());

        verifyExists(user.getId(), false);
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
            repo.save(createUser(generateId()));
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

    private DuracloudUser createUser(int id) {
        String uid = "test-" + id;
        int uniqueIdentifier = id;
        String sn = "User-" + id;
        String givenName = "Test-" + id;
        String userPassword = "6aabbdd62a11ef721d1542d8" + id;
        String mail = "info-" + id + "@duracloud.org";
        String securityQuestion = "What is my email address?-" + id;
        String securityAnswer = "info-" + id + "@duracloud.org";

        return new DuracloudUser(uniqueIdentifier,
                                 uid,
                                 userPassword,
                                 givenName,
                                 sn,
                                 mail,
                                 securityQuestion,
                                 securityAnswer);
    }

}
