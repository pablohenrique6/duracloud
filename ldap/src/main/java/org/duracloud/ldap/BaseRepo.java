/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

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
public interface BaseRepo<T> {
    /**
     * This method returns a single item that has a primary key equal to the
     * arg id.
     *
     * @param id of sought item
     * @return item
     * @throws DBNotFoundException if no item found
     */
    public T findById(int id) throws DBNotFoundException;

    /**
     * This method stores the arg item to the underlying persistence layer.
     *
     * @param item to be saved
     */
    public void save(T item);

    /**
     * This method removes an item along with all of its associated attributes
     * from the underlying persistence layer. The item to be removed has a
     * primary key equal to the arg id.
     * <p/>
     * No exception is thrown if the item does not exist.
     *
     * @param id of the item to be removed
     */
    public void delete(int id);

    /**
     * This method returns ids for all items in this repo.
     *
     * @return set of ids
     */
    public Set<Integer> getIds();

}
