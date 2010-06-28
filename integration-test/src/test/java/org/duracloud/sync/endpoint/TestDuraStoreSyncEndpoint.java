/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.sync.endpoint;

import org.duracloud.sync.SyncIntegrationTestBase;
import org.junit.Test;

/**
 * @author: Bill Branan
 * Date: Apr 9, 2010
 */
public class TestDuraStoreSyncEndpoint extends DuraStoreSyncTestBase {

    @Test
    public void testDuraStoreSyncEndpoint() throws Exception {
        DuraStoreSyncEndpoint endpoint =
            new DuraStoreSyncEndpoint(SyncIntegrationTestBase.host,
                                      Integer.parseInt(SyncIntegrationTestBase.port),
                                      SyncIntegrationTestBase.context,
                                      SyncIntegrationTestBase.getRootCredential().getUsername(),
                                      SyncIntegrationTestBase.getRootCredential().getPassword(),
                                      SyncIntegrationTestBase.spaceId,
                                      true);
        testSync(endpoint);

        endpoint =
            new DuraStoreSyncEndpoint(SyncIntegrationTestBase.host,
                                      Integer.parseInt(SyncIntegrationTestBase.port),
                                      SyncIntegrationTestBase.context,
                                      SyncIntegrationTestBase.getRootCredential().getUsername(),
                                      SyncIntegrationTestBase.getRootCredential().getPassword(),
                                      SyncIntegrationTestBase.spaceId,
                                      false);
        testSyncNoDeletes(endpoint);
    }
    
}
