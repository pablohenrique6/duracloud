/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.ldap.domain.LdapAttribute;
import org.duracloud.ldap.domain.LdapObjectClass;
import org.duracloud.ldap.error.DuracloudLdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import static org.duracloud.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.ldap.domain.LdapAttribute.ORG_UNIT;
import static org.duracloud.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.ldap.domain.LdapObjectClass.DC_OBJECT;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/4/13
 */
public class BaseDuracloudRepoImpl {

    protected Logger log = LoggerFactory.getLogger(BaseDuracloudRepoImpl.class);

    protected LdapTemplate ldapTemplate;
    protected String baseOu;

    public BaseDuracloudRepoImpl(LdapTemplate ldapTemplate, String baseOu) {
        this.ldapTemplate = ldapTemplate;
        this.baseOu = baseOu;

        createBaseDnIfNecessary();
    }

    private void createBaseDnIfNecessary() {
        // Base DN
        String dn = "";
        if (!dnExists(dn)) {
            Attributes attrs = new BasicAttributes();
            Attribute ocAttr = new BasicAttribute(OBJECT_CLASS.toString());
            ocAttr.add(DC_OBJECT.toString());
            ocAttr.add(LdapObjectClass.ORGANIZATION.toString());
            attrs.put(ocAttr);
            attrs.put(LdapAttribute.ORGANIZATION.toString(), "duraspace");
            attrs.put(LdapAttribute.DESCRIPTION.toString(), "DuraCloud IDP");
            createDn(dn, attrs);
        }

        // One level down from Base DN
        dn = baseOu;
        if (!dnExists(dn)) {
            Attributes attrs = new BasicAttributes();
            Attribute ocAttr = new BasicAttribute(OBJECT_CLASS.toString());
            ocAttr.add(ORG_UNIT.toString());
            attrs.put(ocAttr);
            createDn(dn, attrs);
        }
    }

    private boolean dnExists(String dn) {
        try {
            ldapTemplate.lookup(dn);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void createDn(String dn, Attributes attrs) {
        ldapTemplate.bind(dn, null, attrs);
    }

    protected String getBaseDn() {
        try {
            return ldapTemplate.getContextSource()
                               .getReadOnlyContext()
                               .getNameInNamespace();

        } catch (NamingException e) {
            throw new DuracloudLdapException("Error getting baseDn.", e);
        }
    }

    public void delete(int id) {
        try {
            ldapTemplate.unbind(UNIQUE_ID + "=" + id + "," + baseOu);

        } catch (Exception e) {
            log.warn("Item not deleted: {}", id, e);
        }
    }

    /**
     * This method is NOT part of the interface contract.
     */
    public void removeDn() {
        ldapTemplate.unbind("", true);
    }

}
