/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.converter;

import junit.framework.Assert;
import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.domain.LdapRdn;
import org.duracloud.ldap.impl.DuracloudUserRepoImpl;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.util.HashSet;
import java.util.Set;

import static org.duracloud.ldap.domain.LdapAttribute.ACCOUNT;
import static org.duracloud.ldap.domain.LdapAttribute.COMMON_NAME;
import static org.duracloud.ldap.domain.LdapAttribute.DISPLAY_NAME;
import static org.duracloud.ldap.domain.LdapAttribute.MEMBER;
import static org.duracloud.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.ldap.domain.LdapObjectClass.GROUP;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 6/7/12
 */
public class DuracloudGroupConverterTest extends DomainConverterTest<DuracloudGroup> {

    private static final int id = 0;
    private static final String displayName = "name";
    private static final String name = DuracloudGroup.PREFIX + displayName;
    private static final int accountId = 7;
    private static final Set<Integer> userIds = new HashSet<Integer>();

    private static final String baseDn = "dc=test,dc=org";

    @Override
    protected DomainConverter<DuracloudGroup> createConverter() {
        return new DuracloudGroupConverter(baseDn);
    }

    @Override
    protected DuracloudGroup createTestItem() {
        for (int i = 0; i < 5; ++i) {
            userIds.add(i);
        }
        return new DuracloudGroup(id, name, accountId, userIds);
    }

    @Override
    protected Attributes createTestAttributes() {
        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS.toString(), GROUP.toString());
        attrs.put(UNIQUE_ID.toString(), Integer.toString(id));
        attrs.put(COMMON_NAME.toString(), name);
        attrs.put(DISPLAY_NAME.toString(), displayName);
        attrs.put(ACCOUNT.toString(), getAccountDn(accountId));

        Attribute members = new BasicAttribute(MEMBER.toString());
        for (Integer userId : userIds) {
            members.add(getMemberDn(userId));
        }
        attrs.put(members);

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

    private Object getMemberDn(Integer userId) {
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

    @Override
    protected void verifyItem(DuracloudGroup group) {
        Assert.assertNotNull(group);

        Assert.assertNotNull(group.getName());
        Assert.assertNotNull(group.getDisplayName());
        Assert.assertNotNull(group.getUserIds());

        Assert.assertEquals(id, group.getId());
        Assert.assertEquals(name, group.getName());
        Assert.assertEquals(displayName, group.getDisplayName());
        Assert.assertEquals(accountId, group.getAccountId());

        Set<Integer> groupUserIds = group.getUserIds();
        Assert.assertEquals(userIds.size(), groupUserIds.size());
        for (int userId : userIds) {
            groupUserIds.contains(userId);
        }
    }

}
