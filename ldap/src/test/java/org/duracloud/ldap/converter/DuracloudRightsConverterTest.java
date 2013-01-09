/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.converter;

import junit.framework.Assert;
import org.duracloud.ldap.domain.AccountRights;
import org.duracloud.ldap.domain.LdapRdn;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.impl.DuracloudUserRepoImpl;
import org.junit.BeforeClass;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import java.util.HashSet;
import java.util.Set;

import static org.duracloud.ldap.domain.LdapAttribute.ACCOUNT;
import static org.duracloud.ldap.domain.LdapAttribute.COMMON_NAME;
import static org.duracloud.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.ldap.domain.LdapAttribute.ROLE;
import static org.duracloud.ldap.domain.LdapAttribute.ROLE_OCCUPANT;
import static org.duracloud.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.ldap.domain.LdapObjectClass.RIGHTS;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 6/7/12
 */
public class DuracloudRightsConverterTest extends DomainConverterTest<AccountRights> {

    private static final int id = 0;
    private static final int accountId = 9;
    private static final int userId = 11;
    private static Set<Role> roles;

    private static final String baseDn = "dc=test,dc=org";


    @BeforeClass
    public static void beforeClass() {
        roles = new HashSet<Role>();
        roles.add(Role.ROLE_ADMIN);
        roles.add(Role.ROLE_USER);
        roles.add(Role.ROLE_ANONYMOUS);
    }

    @Override
    protected DomainConverter<AccountRights> createConverter() {
        return new DuracloudRightsConverter(baseDn);
    }

    @Override
    protected AccountRights createTestItem() {
        return new AccountRights(id, accountId, userId, roles);
    }

    @Override
    protected Attributes createTestAttributes() {
        Attributes attrs = new BasicAttributes();

        attrs.put(OBJECT_CLASS.toString(), RIGHTS.toString());
        attrs.put(UNIQUE_ID.toString(), Integer.toString(id));
        attrs.put(ACCOUNT.toString(), getAccountDn(accountId));
        attrs.put(ROLE_OCCUPANT.toString(), getUserDn(userId));

        Attribute userRoles = new BasicAttribute(ROLE.toString());
        for (Role role : roles) {
            userRoles.add(getRoleDn(role.name()));
        }
        attrs.put(userRoles);

        return attrs;
    }

    private Object getAccountDn(int accountId) {
        StringBuilder dn = new StringBuilder();
        dn.append(UNIQUE_ID);
        dn.append("=");
        dn.append(accountId);
        dn.append(",");
        dn.append(LdapRdn.ACCOUNT_OU);
        dn.append(",");
        dn.append(baseDn);
        return dn.toString();
    }

    private Object getUserDn(Integer userId) {
        StringBuilder dn = new StringBuilder();
        dn.append(UNIQUE_ID);
        dn.append("=");
        dn.append(userId);
        dn.append(",");
        dn.append(DuracloudUserRepoImpl.BASE_OU);
        dn.append(",");
        dn.append(baseDn);
        return dn.toString();
    }

    private Object getRoleDn(String role) {
        StringBuilder dn = new StringBuilder();
        dn.append(COMMON_NAME);
        dn.append("=");
        dn.append(role);
        dn.append(",");
        dn.append(LdapRdn.ROLE_DC);
        dn.append(",");
        dn.append(baseDn);
        return dn.toString();
    }

    @Override
    protected void verifyItem(AccountRights rights) {
        Assert.assertNotNull(rights);

        Assert.assertNotNull(rights.getRoles());

        Assert.assertEquals(id, rights.getId());
        Assert.assertEquals(accountId, rights.getAccountId());
        Assert.assertEquals(userId, rights.getUserId());
        Assert.assertEquals(roles.size(), rights.getRoles().size());

        Set<Role> rightsRoles = rights.getRoles();
        for (Role role : roles) {
            Assert.assertTrue(rightsRoles.contains(role));
        }
    }

}
