/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

import org.duracloud.ldap.domain.AccountRights;
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
public interface DuracloudRightsRepo extends BaseRepo<AccountRights> {


    /**
     * This method returns the set of rights for a given user
     * The set may be of 0 length
     *
     * @param userId of user
     * @return set of rights
     */
    public Set<AccountRights> findByUserId(int userId);

    /**
     * This method returns the set of rights for a given account
     * The set may be of 0 length
     *
     * @param accountId of account
     * @return set of rights
     */
    public Set<AccountRights> findByAccountId(int accountId);

    /**
     * This method returns the set of rights for a given user in a given account
     *
     * @param accountId of account
     * @param userId    of user
     * @return rights
     * @throws DBNotFoundException if no item found
     */
    public AccountRights findByAccountIdAndUserId(int accountId, int userId)
        throws DBNotFoundException;

    /**
     * This method returns the set of rights for a given user in a given account.
     * If the user is root then those rights will be returned no matter what.
     *
     * @param accountId of account
     * @param userId    of user
     * @return rights
     * @throws DBNotFoundException if no item found
     */
    public AccountRights findAccountRightsForUser(int accountId, int userId)
        throws DBNotFoundException;

    /**
     * This method returns the set of rights for a given account not including
     * the root users
     * The set may be of 0 length
     *
     * @param accountId of account
     * @return set of rights
     */
    public Set<AccountRights> findByAccountIdSkipRoot(int accountId);
}
