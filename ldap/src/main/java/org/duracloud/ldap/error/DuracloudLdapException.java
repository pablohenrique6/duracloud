/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.error;

import org.duracloud.common.error.DuraCloudRuntimeException;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/4/13
 */
public class DuracloudLdapException extends DuraCloudRuntimeException {

    public DuracloudLdapException(String msg) {
        super(msg);
    }

    public DuracloudLdapException(String msg, Throwable e) {
        super(msg, e);
    }
}
