/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

import org.duracloud.ldap.domain.DuracloudUser;
import org.duracloud.ldap.error.DBNotFoundException;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/4/13
 */
public interface DuracloudUserRepo extends BaseRepo<DuracloudUser> {
    /**
     * This method returns a single user with the given username
     *
     * @param username of user
     * @return user
     * @throws DBNotFoundException if no item found
     */
    public DuracloudUser findByUsername(String username)
        throws DBNotFoundException;

}
