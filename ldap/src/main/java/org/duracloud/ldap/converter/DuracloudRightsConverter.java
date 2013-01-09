/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.converter;

import org.duracloud.ldap.domain.AccountRights;
import org.duracloud.ldap.domain.LdapRdn;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.error.ContextMapperException;
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
 *         Date: 1/3/13
 */
public class DuracloudRightsConverter implements DomainConverter<AccountRights> {

    private final Logger log =
        LoggerFactory.getLogger(DuracloudRightsConverter.class);

    private final String baseDn;

    public DuracloudRightsConverter(String baseDn) {
        this.baseDn = baseDn;
    }

    @Override
    public Attributes toAttributes(AccountRights item) {
        log.debug("toAttributes for: {}", item);

        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS.toString(), RIGHTS.toString());
        attrs.put(UNIQUE_ID.toString(), Integer.toString(item.getId()));
        attrs.put(ACCOUNT.toString(), getAccountDn(item.getAccountId()));
        attrs.put(ROLE_OCCUPANT.toString(), getUserDn(item.getUserId()));

        Attribute roles = new BasicAttribute(ROLE.toString());
        for (Role role : item.getRoles()) {
            roles.add(getMemberDn(role));
        }
        attrs.put(roles);

        return attrs;
    }

    @Override
    public AccountRights mapFromContext(Object o) {
        log.debug("mapFromContext for class: {}", o.getClass());

        if (!(o instanceof DirContextAdapter)) {
            throw new InvalidParameterException("Illegal arg: " + o.getClass());
        }

        DirContextAdapter adapter = (DirContextAdapter) o;
        Attributes attrs = adapter.getAttributes();

        Attribute uniqueIdentifierAttr = getAttribute(attrs,
                                                      UNIQUE_ID.toString());
        Attribute accountIdAttr = getAttribute(attrs, ACCOUNT.toString());
        Attribute roleOccupantAttr = getAttribute(attrs,
                                                  ROLE_OCCUPANT.toString());
        Attribute rolesAttr = getMultiAttribute(attrs, ROLE.toString());

        int uniqueIdentifier = getInt(uniqueIdentifierAttr);
        int accountId = getAccountId(accountIdAttr);
        int userId = getUserId(roleOccupantAttr);

        Set<Role> roles = new HashSet<Role>();
        for (int i = 0; i < rolesAttr.size(); ++i) {
            roles.add(getRole(rolesAttr, i));
        }

        return new AccountRights(uniqueIdentifier, accountId, userId, roles);
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
        if (null == attr) {
            throw new ContextMapperException("Attribute not found: " + key);
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

    private Integer getUserId(Attribute attr) {
        return getUniqueIdentifier(attr, getString(attr));
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

    private Role getRole(Attribute attr, int i) {
        String roleDn;
        try {
            roleDn = (String) attr.get(i);

        } catch (NamingException e) {
            throw new ContextMapperException(attr.getID(), e);
        }

        return getRoleFromDn(roleDn);
    }

    private Role getRoleFromDn(String roleDn) {
        String[] parts = roleDn.split(",");
        if (null == parts || parts.length == 0) {
            throw new ContextMapperException(roleDn + ": empty");
        }

        String roleAtt = COMMON_NAME + "=";
        String role = parts[0].substring(roleAtt.length(), parts[0].length());
        try {
            return Role.valueOf(role.toUpperCase());

        } catch (IllegalArgumentException e) {
            throw new ContextMapperException(roleDn, e);
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

    private String getUserDn(int userId) {
        StringBuilder dn = new StringBuilder();
        dn.append(UNIQUE_ID);
        dn.append("=");
        dn.append(userId);
        dn.append(",");
        dn.append(LdapRdn.PEOPLE_OU);
        dn.append(",");
        dn.append(baseDn);
        return dn.toString();
    }

    private String getMemberDn(Role role) {
        StringBuilder dn = new StringBuilder();
        dn.append(COMMON_NAME);
        dn.append("=");
        dn.append(role.name());
        dn.append(",");
        dn.append(LdapRdn.ROLE_DC);
        dn.append(",");
        dn.append(baseDn);
        return dn.toString();
    }

}
