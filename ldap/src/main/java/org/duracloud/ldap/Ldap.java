/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.ldap.error.DBNotFoundException;

import java.util.List;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 1/3/13
 */
public interface Ldap {

    /**
     * This method initiallizes the LDAP
     *
     * @param config for LDAP connection
     */
    public void initialize(LdapConfig config);

    /**
     * This method returns the user with the arg username found within the arg
     * accounts
     *
     * @param username       of sought user
     * @param accountIdsMask in which user is a member
     * @return user object
     * @throws DBNotFoundException if user not found
     */
    public SecurityUserBean getSecurityUser(String username,
                                            Set<Integer> accountIdsMask)
        throws DBNotFoundException;

    /**
     * This method returns a complete list of Users found within the arg
     * accounts
     *
     * @param accountIds in which the users are members
     * @return list of users
     */
    public List<SecurityUserBean> getSecurityUsers(Set<Integer> accountIds);

    public int getAccountId(String institution) throws DBNotFoundException;

    public void saveSecurityUser(SecurityUserBean user, int acctId);
}
