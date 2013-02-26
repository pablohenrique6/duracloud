/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

import org.duracloud.ldap.domain.IdUtilConfig;
import org.duracloud.ldap.domain.LdapConfig;

/**
 * @author Andrew Woods
 *         Date: 1/7/13
 */
public interface DuracloudRepoMgr {

    public void initialize(LdapConfig ldapConfig, IdUtilConfig idUtilConfig);

    public DuracloudUserRepo getUserRepo();

    public DuracloudGroupRepo getGroupRepo();

    public DuracloudRightsRepo getRightsRepo();

    public IdUtil getIdUtil();
}
