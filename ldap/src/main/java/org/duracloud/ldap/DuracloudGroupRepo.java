/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.error.DBNotFoundException;

import java.util.Set;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/4/13
 */
public interface DuracloudGroupRepo extends BaseRepo<DuracloudGroup> {
    /**
     * This method returns a single group within the given account, with the
     * given groupname.
     *
     * @param groupname of group
     * @param acctId    associated with group
     * @return group
     * @throws DBNotFoundException if no item found
     */
    public DuracloudGroup findInAccountByGroupname(String groupname, int acctId)
        throws DBNotFoundException;

    /**
     * This method returns all groups within the given account.
     *
     * @param acctId associated with group
     * @return all groups in account
     * @throws DBNotFoundException if no item found
     */
    public Set<DuracloudGroup> findByAccountId(int acctId);

    /**
     * This method returns all groups.
     *
     * @return all groups
     * @throws DBNotFoundException if no groups found
     */
    public Set<DuracloudGroup> findAllGroups();

}
