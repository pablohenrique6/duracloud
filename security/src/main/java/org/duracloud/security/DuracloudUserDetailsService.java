/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security;

import org.duracloud.ldap.domain.LdapConfig;
import org.springframework.security.userdetails.UserDetailsService;
import org.duracloud.common.model.SecurityUserBean;

import java.util.List;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public interface DuracloudUserDetailsService extends UserDetailsService {

    public void initialize(LdapConfig ldapConfig, Set<Integer> accountIds);

    @Deprecated
    public void setUsers(List<SecurityUserBean> users);

    public List<SecurityUserBean> getUsers();

    public SecurityUserBean getUserByUsername(String username);

}
