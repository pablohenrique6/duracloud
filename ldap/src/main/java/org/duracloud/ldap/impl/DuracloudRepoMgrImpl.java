/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.ldap.DuracloudGroupRepo;
import org.duracloud.ldap.DuracloudRepoMgr;
import org.duracloud.ldap.DuracloudRightsRepo;
import org.duracloud.ldap.DuracloudUserRepo;
import org.duracloud.ldap.IdUtil;
import org.duracloud.ldap.domain.IdUtilConfig;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.ldap.error.DBUninitializedException;
import org.duracloud.ldap.error.DuracloudLdapException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/7/13
 */
public class DuracloudRepoMgrImpl implements DuracloudRepoMgr {

    private final Logger log =
        LoggerFactory.getLogger(DuracloudRepoMgrImpl.class);

    private DuracloudUserRepo userRepo;
    private DuracloudGroupRepo groupRepo;
    private DuracloudRightsRepo rightsRepo;

    private LdapTemplate ldapTemplate;
    private IdUtil idUtil;

    public DuracloudRepoMgrImpl(IdUtil idUtil) {
        this.idUtil = idUtil;
    }

    @Override
    public void initialize(LdapConfig ldapConfig, IdUtilConfig idUtilConfig) {
        log.info("initializing");

        this.ldapTemplate = null;
        this.ldapTemplate = createLdapTemplate(ldapConfig);

        this.idUtil.initialize(idUtilConfig.getHost(),
                               idUtilConfig.getPort(),
                               idUtilConfig.getContext());

        // LDAP repos
        this.userRepo = new DuracloudUserRepoImpl(ldapTemplate);
        this.groupRepo = new DuracloudGroupRepoImpl(ldapTemplate);
        this.rightsRepo = new DuracloudRightsRepoImpl(ldapTemplate);
    }

    @Override
    public DuracloudUserRepo getUserRepo() {
        checkInitialized(this.userRepo, "DuracloudUserRepo");
        return this.userRepo;
    }

    @Override
    public DuracloudGroupRepo getGroupRepo() {
        checkInitialized(this.groupRepo, "DuracloudGroupRepo");
        return this.groupRepo;
    }

    @Override
    public DuracloudRightsRepo getRightsRepo() {
        checkInitialized(this.rightsRepo, "DuracloudRightsRepo");
        return this.rightsRepo;
    }

    private void checkInitialized(Object repo, String name) {
        if (null == repo) {
            String msg = name + " is not initialized";
            log.error(msg);
            throw new DBUninitializedException(msg);
        }
    }

    private LdapTemplate createLdapTemplate(LdapConfig config) {
        if (null != ldapTemplate) {
            return ldapTemplate;
        }

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(config.getLdapUrl());
        contextSource.setBase(config.getLdapBaseDn());
        contextSource.setUserDn(config.getLdapUserDn());
        contextSource.setPassword(config.getLdapPassword());
        try {
            contextSource.afterPropertiesSet();

        } catch (Exception e) {
            log.error("Error creating LdapContentSource", e);
            throw new DuracloudLdapException("Error creating LdapContentSource",
                                             e);
        }

        return new LdapTemplate(contextSource);
    }

    /**
     * For unit test
     *
     * @param ldapTemplate mock
     */
    protected void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
    }

    public IdUtil getIdUtil() {
        return idUtil;
    }
}
