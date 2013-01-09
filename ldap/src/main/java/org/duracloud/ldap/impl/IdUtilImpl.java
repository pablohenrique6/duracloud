/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.ldap.DuracloudUserRepo;
import org.duracloud.ldap.IdUtil;
import org.duracloud.ldap.error.DBUninitializedException;

import java.util.Collection;
import java.util.Collections;

/**
 * This class creates progressively *DECREASING* item ids.
 *
 * @author Andrew Woods
 *         Date: 1/7/13
 */
public class IdUtilImpl implements IdUtil {

    private int userId = 0;

    @Override
    public void initialize(DuracloudUserRepo userRepo) {
        this.userId = min(userRepo.getIds());
    }

    private int min(Collection<? extends Integer> c) {
        // this check is necessary because Collections.min(int)
        // throws a NoSuchElementException when the collection
        // is empty.
        return c.isEmpty() ? -1 : Collections.min(c);
    }

    private void checkInitialized() {
        if (userId == 0) {
            throw new DBUninitializedException("IdUtil must be initialized");
        }
    }

    @Override
    public int newUserId() {
        checkInitialized();
        return --userId;
    }

}
