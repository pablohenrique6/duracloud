/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.durastore.util;

import org.duracloud.storage.domain.StorageAccount;
import org.duracloud.storage.error.StorageException;
import org.duracloud.storage.provider.StorageProvider;

import java.io.InputStream;
import java.util.List;

/**
 * @author Andrew Woods
 *         Date: Aug 19, 2010
 */
public interface StorageProviderFactory {

    public void initialize(InputStream accountXml);

    public List<StorageAccount> getStorageAccounts();

    public StorageProvider getStorageProvider() throws StorageException;

    public StorageProvider getStorageProvider(String storageAccountId)
        throws StorageException;

}
