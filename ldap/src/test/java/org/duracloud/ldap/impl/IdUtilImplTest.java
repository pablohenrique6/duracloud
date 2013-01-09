/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.impl;

import org.duracloud.ldap.DuracloudUserRepo;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: Dec 7, 2010
 */
public class IdUtilImplTest {

    private IdUtilImpl idUtil;

    private DuracloudUserRepo userRepo;

    private static final int COUNT = 5;

    @Before
    public void setUp() throws Exception {
        userRepo = createMockUserRepo(COUNT);

        idUtil = new IdUtilImpl();
        idUtil.initialize(userRepo);
    }

    private DuracloudUserRepo createMockUserRepo(int count) {
        DuracloudUserRepo repo = EasyMock.createMock(DuracloudUserRepo.class);
        EasyMock.expect(repo.getIds()).andReturn(createIds(count));
        EasyMock.replay(repo);
        return repo;
    }

    private Set<Integer> createIds(int count) {
        Set<Integer> ids = new HashSet<Integer>();
        for (int i = 1; i < count; ++i) {
            ids.add(-i);
        }
        return ids;
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(userRepo);
    }

    @Test
    public void testNewUserId() throws Exception {
        Assert.assertEquals(-COUNT, idUtil.newUserId());
    }

}
