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

    public void initialize(DuracloudUserRepo userRepo);

    public int newUserId();

}
