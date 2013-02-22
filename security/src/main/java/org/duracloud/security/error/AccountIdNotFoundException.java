/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.error;

import org.duracloud.common.error.DuraCloudCheckedException;

/**
 * @author Andrew Woods
 *         Date: 2/16/13
 */
public class AccountIdNotFoundException extends DuraCloudCheckedException {

    public AccountIdNotFoundException(String msg) {
        super(msg);
    }
}
