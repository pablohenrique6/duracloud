/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.impl;

import org.duracloud.common.model.Credential;
import org.duracloud.common.model.RootUserCredential;
import org.duracloud.common.model.SystemUserCredential;
import org.duracloud.ldap.Ldap;
import org.duracloud.ldap.domain.IdUtilConfig;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.ldap.error.DBNotFoundException;
import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.security.domain.SecurityConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.security.GrantedAuthority;
import org.springframework.security.GrantedAuthorityImpl;
import org.springframework.security.userdetails.UserDetails;
import org.springframework.security.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * This class acts as the repository of username/password/role info for access
 * to this DuraCloud application.
 *
 * @author Andrew Woods
 *         Date: Jan 03, 2013
 */
public class UserDetailsServiceLdapImpl implements DuracloudUserDetailsService {
    private final Logger log = LoggerFactory.getLogger(
        UserDetailsServiceLdapImpl.class);

    private Set<Integer> acctIds;
    private Ldap ldap;

    private final DuracloudUserDetails systemUser;
    private final DuracloudUserDetails rootUser;

    public UserDetailsServiceLdapImpl(Ldap ldap) {
        this(ldap, null);
    }

    public UserDetailsServiceLdapImpl(Ldap ldap, Set<Integer> acctIds) {
        this.ldap = ldap;
        this.acctIds = acctIds;

        // Create system user.
        List<String> grants = new ArrayList<String>();
        grants.add("ROLE_ADMIN");
        grants.add("ROLE_USER");

        Credential sysCredential = new SystemUserCredential();
        this.systemUser =
            createUserDetails(new SecurityUserBean(sysCredential.getUsername(),
                                                   sysCredential.getPassword(),
                                                   grants));

        // Create root user
        grants = new ArrayList<String>();
        grants.add("ROLE_ROOT");
        grants.add("ROLE_ADMIN");
        grants.add("ROLE_USER");

        RootUserCredential rootCredential = new RootUserCredential();
        this.rootUser =
            createUserDetails(new SecurityUserBean(rootCredential.getUsername(),
                                                   rootCredential.getRootEncodedPassword(),
                                                   grants));
    }

    public void initialize(SecurityConfigBean securityConfig) {
        if (null == securityConfig) {
            throw new IllegalArgumentException("SecurityConfig is null!");
        }

        Set<Integer> acctIds = securityConfig.getAcctIds();
        LdapConfig ldapConfig = securityConfig.getLdapConfig();
        IdUtilConfig idUtilConfig = securityConfig.getIdUtilConfig();

        if (null == ldapConfig) {
            throw new IllegalArgumentException("LdapConfig is null!");
        }
        if (null == idUtilConfig) {
            throw new IllegalArgumentException("IdUtilConfig is null!");
        }
        if (null == acctIds || acctIds.size() == 0) {
            throw new IllegalArgumentException("AccountIds is null or empty!");
        }

        this.ldap.initialize(ldapConfig, idUtilConfig);
        this.acctIds = acctIds;
    }

    /**
     * This method retrieves UserDetails for all users from a flat file in
     * DuraCloud.
     *
     * @param username of principal for whom details are sought
     * @return UserDetails for arg username
     * @throws UsernameNotFoundException if username not found
     * @throws DataAccessException       if system error while retrieving info
     */
    public UserDetails loadUserByUsername(String username)
        throws UsernameNotFoundException, DataAccessException {

        if (username.equals(rootUser.getUsername())) {
            return rootUser;
        } else if (username.equals(systemUser.getUsername())) {
            return systemUser;
        }

        SecurityUserBean securityUser;
        try {
            securityUser = ldap.getSecurityUser(username, acctIds);

        } catch (DBNotFoundException e) {
            throw new UsernameNotFoundException(username, e);
        }

        return createUserDetails(securityUser);
    }

    private DuracloudUserDetails createUserDetails(SecurityUserBean u) {
        List<String> grantBeans = u.getGrantedAuthorities();
        GrantedAuthority[] grants = new GrantedAuthority[grantBeans.size()];
        for (int i = 0; i < grantBeans.size(); ++i) {
            grants[i] = new GrantedAuthorityImpl(grantBeans.get(i));
        }

        return new DuracloudUserDetails(u.getUsername(),
                                        u.getPassword(),
                                        u.getEmail(),
                                        u.isEnabled(),
                                        u.isAccountNonExpired(),
                                        u.isCredentialsNonExpired(),
                                        u.isAccountNonLocked(),
                                        grants,
                                        u.getGroups());
    }

    @Override
    public void setUsers(List<SecurityUserBean> users) {
        throw new UnsupportedOperationException(
            "UserDetailsServiceLdapImpl.setUsers() not supported!");
    }

    /**
     * This method returns all of the non-system-defined users.
     *
     * @return users
     */
    @Override
    public List<SecurityUserBean> getUsers() {
        return ldap.getSecurityUsers(acctIds);
    }

    @Override
    public SecurityUserBean getUserByUsername(String username) {
        try {
            return ldap.getSecurityUser(username, acctIds);
        } catch (DBNotFoundException e) {
            throw new UsernameNotFoundException(username, e);
        }
    }

}
