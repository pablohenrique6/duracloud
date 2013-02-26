/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap;

/**
 * @author Andrew Woods
 *         Date: 1/7/13
 */
public interface IdUtil {

    /**
     * This method initializes this IdUtil.
     *
     * @param host    of remote id-generator
     * @param port    of remote id-generator
     * @param context of remote id-generator
     */
    public void initialize(String host, String port, String context);

    public int newUserId();

    public int newRightsId();

    public int newGroupId();

}
