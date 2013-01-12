/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.ldap.Ldap;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.ldap.error.DBNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class caches results from its wrapped Ldap implementation.
 * The shelf-life of the cache can be set with the constructor arg:
 * 'refreshSeconds'.
 *
 * @author Andrew Woods
 *         Date: 1/12/13
 */
public class LdapCachingImpl implements Ldap {

    private final Logger log = LoggerFactory.getLogger(LdapCachingImpl.class);

    private int refreshSeconds; // The number of seconds before cache expires.
    private long lastRefresh;

    private Ldap target;

    private Map<String, SecurityUserBean> singleUsers;
    private Map<String, SecurityUserBean> allUsers;

    public LdapCachingImpl(Ldap target) {
        this(target, 600 /*ten minutes*/);
    }

    public LdapCachingImpl(Ldap target, int refreshSeconds) {
        this.refreshSeconds = refreshSeconds;
        this.target = target;
        this.singleUsers = new HashMap<>();
        this.allUsers = new HashMap<>();
    }

    @Override
    public void initialize(LdapConfig config) {
        lastRefresh = 0;
        refreshCache();
        target.initialize(config);
    }

    private void refreshCache() {
        long now = System.currentTimeMillis();
        if ((now - lastRefresh) > (refreshSeconds * 1000)) {
            lastRefresh = now;
            singleUsers.clear();
            allUsers.clear();
        }
    }

    @Override
    public SecurityUserBean getSecurityUser(String username,
                                            Set<Integer> accountIdsMask)
        throws DBNotFoundException {
        long start = System.currentTimeMillis();
        refreshCache();

        SecurityUserBean user = singleUsers.get(username);
        if (null == user) {
            user = allUsers.get(username);
        }

        if (null == user) {
            user = target.getSecurityUser(username, accountIdsMask);
            singleUsers.put(username, user);
        }

        log.debug("getSecurityUser: {}, elapsed millis: {}",
                 username,
                 System.currentTimeMillis() - start);

        return user;
    }

    @Override
    public List<SecurityUserBean> getSecurityUsers(Set<Integer> accountIds) {
        long start = System.currentTimeMillis();
        refreshCache();

        if (allUsers.isEmpty()) {
            List<SecurityUserBean> all = target.getSecurityUsers(accountIds);

            for (SecurityUserBean user : all) {
                allUsers.put(user.getUsername(), user);
                singleUsers.put(user.getUsername(), user);
            }
        }

        log.info("getSecurityUsers: all, elapsed millis: {}",
                 System.currentTimeMillis() - start);

        return new ArrayList<>(allUsers.values());
    }
}
