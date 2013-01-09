/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.domain;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/3/13
 */
public class BaseDomainData implements Identifiable {
    /**
     * A generic placeholder value which can be assigned to data which does
     * not yet have a known value, but needs to be updated in the future.
     */
    public static final String PLACEHOLDER_VALUE = "TBD";

    protected int id;

    @Override
    public int getId() {
        return id;
    }
}
