/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.ldap.DuracloudUserRepo;
import org.duracloud.ldap.converter.DomainConverter;
import org.duracloud.ldap.converter.DuracloudUserConverter;
import org.duracloud.ldap.domain.DuracloudUser;
import org.duracloud.ldap.domain.LdapRdn;
import org.duracloud.ldap.error.DBNotFoundException;
import org.slf4j.LoggerFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.core.LdapTemplate;

import javax.naming.directory.Attributes;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.duracloud.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.ldap.domain.LdapAttribute.USER_ID;
import static org.duracloud.ldap.domain.LdapObjectClass.PERSON;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/4/13
 */
public class DuracloudUserRepoImpl extends BaseDuracloudRepoImpl implements DuracloudUserRepo {

    public static final String BASE_OU = LdapRdn.PEOPLE_OU.toString();

    private DomainConverter<DuracloudUser> converter;


    public DuracloudUserRepoImpl(LdapTemplate ldapTemplate) {
        this(ldapTemplate, null);
    }

    public DuracloudUserRepoImpl(LdapTemplate ldapTemplate,
                                 DomainConverter<DuracloudUser> converter) {
        super(ldapTemplate, BASE_OU);

        this.log = LoggerFactory.getLogger(DuracloudUserRepoImpl.class);

        if (null == converter) {
            converter = new DuracloudUserConverter();
        }
        this.converter = converter;
    }

    @Override
    public DuracloudUser findById(int id) throws DBNotFoundException {

        StringBuilder rdn = new StringBuilder();
        rdn.append(UNIQUE_ID);
        rdn.append("=");
        rdn.append(id);
        rdn.append(",");
        rdn.append(BASE_OU);

        try {
            return (DuracloudUser) ldapTemplate.lookup(rdn.toString(),
                                                       converter);

        } catch (EmptyResultDataAccessException e) {
            throw new DBNotFoundException("No items found for RDN: " + rdn);
        } catch (NameNotFoundException e) {
            throw new DBNotFoundException("No items found for RDN: " + rdn);
        }
    }

    @Override
    public DuracloudUser findByUsername(String username)
        throws DBNotFoundException {

        String filter = USER_ID + "=" + username;
        try {
            return (DuracloudUser) ldapTemplate.searchForObject(BASE_OU,
                                                                filter,
                                                                converter);

        } catch (EmptyResultDataAccessException e) {
            throw new DBNotFoundException("No items found for: " + username);
        } catch (NameNotFoundException e) {
            throw new DBNotFoundException("No items found for: " + username);
        }
    }

    @Override
    public void save(DuracloudUser item) {
        Attributes attrs = converter.toAttributes(item);

        StringBuilder dn = new StringBuilder();
        dn.append(UNIQUE_ID);
        dn.append("=");
        dn.append(item.getId());
        dn.append(",");
        dn.append(BASE_OU);

        try {
            ldapTemplate.bind(dn.toString(), null, attrs);

        } catch (NameNotFoundException e) {
            log.warn("Item not saved: {}, msg: {}", item, e.getMessage());
        } catch (NameAlreadyBoundException e) {
            log.info("Updating item: {}, msg: {}", item, e.getMessage());
            ldapTemplate.rebind(dn.toString(), null, attrs);
        }
    }

    @Override
    public Set<Integer> getIds() {
        List<DuracloudUser> users;

        String filter = OBJECT_CLASS + "=" + PERSON;
        try {
            users = ldapTemplate.search(BASE_OU, filter, converter);

        } catch (NameNotFoundException e) {
            log.info(e.getMessage());
            users = new ArrayList<DuracloudUser>();
        }

        Set<Integer> ids = new HashSet<Integer>();
        for (DuracloudUser user : users) {
            ids.add(user.getId());
        }
        return ids;
    }


}
