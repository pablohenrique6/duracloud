/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.converter;

import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.domain.LdapRdn;
import org.duracloud.ldap.error.ContextMapperException;
import org.duracloud.ldap.impl.DuracloudUserRepoImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import java.security.InvalidParameterException;
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
 *         Date: 1/3/13
 */
public class DuracloudGroupConverter implements DomainConverter<DuracloudGroup> {

    private final Logger log =
        LoggerFactory.getLogger(DuracloudGroupConverter.class);

    private final String baseDn;

    public DuracloudGroupConverter(String baseDn) {
        this.baseDn = baseDn;
    }

    @Override
    public Attributes toAttributes(DuracloudGroup item) {
        log.debug("toAttributes for: {}", item);

        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS.toString(), GROUP.toString());
        attrs.put(UNIQUE_ID.toString(), Integer.toString(item.getId()));
        attrs.put(COMMON_NAME.toString(), item.getName());
        attrs.put(DISPLAY_NAME.toString(), item.getDisplayName());
        attrs.put(ACCOUNT.toString(), getAccountDn(item.getAccountId()));

        Set<Integer> userIds = item.getUserIds();
        if (null != userIds && userIds.size() > 0) {

            Attribute members = new BasicAttribute(MEMBER.toString());
            for (Integer userId : userIds) {
                members.add(getMemberDn(userId));
            }
            attrs.put(members);
        }

        return attrs;
    }

    @Override
    public DuracloudGroup mapFromContext(Object o) {
        log.debug("mapFromContext for class: {}", o.getClass());

        if (!(o instanceof DirContextAdapter)) {
            throw new InvalidParameterException("Illegal arg: " + o.getClass());
        }

        DirContextAdapter adapter = (DirContextAdapter) o;
        Attributes attrs = adapter.getAttributes();

        Attribute uniqueIdentifierAttr = getAttribute(attrs,
                                                      UNIQUE_ID.toString());
        Attribute cnAttr = getAttribute(attrs, COMMON_NAME.toString());
        Attribute accountIdAttr = getAttribute(attrs, ACCOUNT.toString());
        Attribute membersAttr = getMultiAttribute(attrs, MEMBER.toString());

        int uniqueIdentifier = getInt(uniqueIdentifierAttr);
        String cn = getString(cnAttr);
        int accountId = getAccountId(accountIdAttr);

        Set<Integer> userIds = new HashSet<Integer>();
        if (null != membersAttr) {
            for (int i = 0; i < membersAttr.size(); ++i) {
                userIds.add(getUserId(membersAttr, i));
            }
        }

        return new DuracloudGroup(uniqueIdentifier, cn, accountId, userIds);
    }


    private Attribute getAttribute(Attributes attrs, String key) {
        Attribute attr = attrs.get(key);
        if (null == attr) {
            throw new ContextMapperException("Attribute not found: " + key);
        }

        if (attr.size() != 1) {
            throw new ContextMapperException(
                "Unexpected number of values: " + attr.size());
        }

        return attr;
    }

    private Attribute getMultiAttribute(Attributes attrs, String key) {
        Attribute attr = attrs.get(key);

        // None is fine.
        if (null == attr) {
            log.info("Attribute not found: " + key);
        }

        return attr;
    }

    private int getInt(Attribute attr) {
        try {
            return Integer.parseInt((String) attr.get());

        } catch (NamingException e) {
            throw new ContextMapperException(attr.getID(), e);
        }
    }

    private String getString(Attribute attr) {
        try {
            return (String) attr.get();

        } catch (NamingException e) {
            throw new ContextMapperException(attr.getID(), e);
        }
    }

    private int getAccountId(Attribute attr) {
        return getUniqueIdentifier(attr, getString(attr));
    }

    private Integer getUserId(Attribute attr, int i) {
        String userDn;
        try {
            userDn = (String) attr.get(i);

        } catch (NamingException e) {
            throw new ContextMapperException(attr.getID(), e);
        }

        return getUniqueIdentifier(attr, userDn);
    }

    private Integer getUniqueIdentifier(Attribute attr, String userDn) {
        String[] parts = userDn.split(",");
        if (null == parts || parts.length == 0) {
            throw new ContextMapperException(attr.getID() + ": empty");
        }

        String idAtt = UNIQUE_ID + "=";
        String id = parts[0].substring(idAtt.length(), parts[0].length());
        try {
            return Integer.parseInt(id);

        } catch (NumberFormatException e) {
            throw new ContextMapperException(attr.getID(), e);
        }
    }

    private String getAccountDn(int accountId) {
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

    private String getMemberDn(int userId) {
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


}
