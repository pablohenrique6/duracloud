/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.chrontask;

import org.duracloud.chronstorage.ChronStageStorageProvider;
import org.duracloud.chrontask.snapshot.SnapshotTaskRunner;
import org.duracloud.storage.provider.TaskProviderBase;
import org.slf4j.LoggerFactory;

/**
 * @author: Bill Branan
 * Date: 1/29/14
 */
public class ChronStageTaskProvider extends TaskProviderBase {

    public ChronStageTaskProvider(String accessKey, String secretKey) {
        log = LoggerFactory.getLogger(ChronStageTaskProvider.class);

        ChronStageStorageProvider chronStageProvider =
            new ChronStageStorageProvider(accessKey, secretKey);

        taskList.add(new SnapshotTaskRunner(chronStageProvider));
    }

}