/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.ldap.DuracloudGroupRepo;
import org.duracloud.ldap.DuracloudRepoMgr;
import org.duracloud.ldap.DuracloudRightsRepo;
import org.duracloud.ldap.DuracloudUserRepo;
import org.duracloud.ldap.Ldap;
import org.duracloud.ldap.domain.AccountRights;
import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.domain.DuracloudUser;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.error.DBNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 1/3/13
 */
public class LdapImpl implements Ldap {

    private final Logger log = LoggerFactory.getLogger(LdapImpl.class);

    private DuracloudRepoMgr repoMgr;

    public LdapImpl(DuracloudRepoMgr repoMgr) {
        this.repoMgr = repoMgr;
    }

    @Override
    public void initialize(LdapConfig config) {
        repoMgr.initialize(config);
    }

    @Override
    public SecurityUserBean getSecurityUser(String username,
                                            Set<Integer> accountIdsMask)
        throws DBNotFoundException {
        // Find User.
        DuracloudUser user = userRepo().findByUsername(username);
        return doGetSecurityUser(user, accountIdsMask);
    }

    private SecurityUserBean doGetSecurityUser(DuracloudUser user,
                                               Set<Integer> accountIdsMask) {
        // Find all Roles for User across all arg accounts.
        Set<Role> roles = new HashSet<Role>();
        for (int accountId : accountIdsMask) {
            roles.addAll(findAccountRolesForUser(user, accountId));
        }

        // Find all Groups for all arg accounts.
        Set<DuracloudGroup> groups = new HashSet<DuracloudGroup>();
        for (int accountId : accountIdsMask) {
            groups.addAll(findGroupsByAccountId(accountId));
        }

        return createSecurityUserBean(user, roles, groups);
    }

    private Set<Role> findAccountRolesForUser(DuracloudUser user,
                                              int accountId) {
        Set<Role> roles = null;
        AccountRights rights;
        try {
            rights = rightsRepo().findAccountRightsForUser(accountId,
                                                           user.getId());
            roles = rights.getRoles();

        } catch (DBNotFoundException e) {
            log.debug("No rights found for user:{}, userId:{}, acctId:{}",
                      new Object[]{user.getUsername(),
                                   user.getId(),
                                   accountId});
        }

        if (null == roles) {
            roles = new HashSet<Role>();
        }
        return roles;
    }

    private Set<DuracloudGroup> findGroupsByAccountId(int accountId) {
        Set<DuracloudGroup> groups = groupRepo().findByAccountId(accountId);
        if (null == groups) {
            groups = new HashSet<DuracloudGroup>();
        }

        return groups;
    }

    private SecurityUserBean createSecurityUserBean(DuracloudUser user,
                                                    Set<Role> roleSet,
                                                    Set<DuracloudGroup> groupSet) {
        List<String> grants = new ArrayList<String>();
        for (Role role : roleSet) {
            grants.add(role.name());
        }

        List<String> groups = new ArrayList<String>();
        for (DuracloudGroup group : groupSet) {
            // Is User member of this group?
            if (group.getUserIds().contains(user.getId())) {
                groups.add(group.getName());
            }
        }

        return new SecurityUserBean(user.getUsername(),
                                    user.getPassword(),
                                    user.getEmail(),
                                    user.isEnabled(),
                                    user.isAccountNonExpired(),
                                    user.isCredentialsNonExpired(),
                                    user.isAccountNonLocked(),
                                    grants,
                                    groups);
    }

    @Override
    public List<SecurityUserBean> getSecurityUsers(Set<Integer> accountIds) {
        // Get all Rights for all arg accounts.
        Set<AccountRights> allRights = new HashSet<AccountRights>();
        for (int accountId : accountIds) {
            Set<AccountRights> rights = rightsRepo().findByAccountId(accountId);

            if (null != rights) {
                allRights.addAll(rights);
            }
        }

        // Get all User ids.
        Set<Integer> userIds = new HashSet<Integer>();
        for (AccountRights r : allRights) {
            userIds.add(r.getUserId());
        }

        // Get all Users from found rights.
        List<SecurityUserBean> userBeans = new ArrayList<SecurityUserBean>();
        for (int userId : userIds) {

            DuracloudUser user = findUserById(userId);
            if (null != user) {
                userBeans.add(doGetSecurityUser(user, accountIds));
            }
        }

        return userBeans;
    }

    private DuracloudUser findUserById(int userId) {
        try {
            return userRepo().findById(userId);
        } catch (DBNotFoundException e) {
            return null;
        }
    }

    private DuracloudUserRepo userRepo() {
        return repoMgr.getUserRepo();
    }

    private DuracloudGroupRepo groupRepo() {
        return repoMgr.getGroupRepo();
    }

    private DuracloudRightsRepo rightsRepo() {
        return repoMgr.getRightsRepo();
    }
}
