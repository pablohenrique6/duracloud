/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.web.RestHttpHelper;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Andrew Woods
 *         Date: Dec 7, 2010
 */
public class IdUtilImplTest {

    private IdUtilImpl idUtil;

    private static final String host = "host";
    private static final String port = "port";
    private static final String context = "context";

    private RestHttpHelper restHelper;
    private RestHttpHelper.HttpResponse response;

    @Before
    public void setUp() throws Exception {
        restHelper = EasyMock.createMock("RestHttpHelper",
                                         RestHttpHelper.class);
        response = EasyMock.createMock("HttpResponse",
                                       RestHttpHelper.HttpResponse.class);

        idUtil = new IdUtilImpl();
        idUtil.initialize(host, port, context, restHelper);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(restHelper, response);
    }

    private void replayMocks() {
        EasyMock.replay(restHelper, response);
    }

    @Test
    public void testNewUserId() throws Exception {
        Integer id = 7;
        createMocks(id.toString(), "user");
        replayMocks();

        Integer result = idUtil.newUserId();
        Assert.assertEquals(id, result);
    }

    @Test
    public void testNewUserIdError() throws Exception {
        createMocks("not a number", "user");
        replayMocks();

        boolean thrown = false;
        try {
            idUtil.newUserId();
            Assert.fail("exception expected");
        } catch (DuraCloudRuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    @Test
    public void testNewRightsId() throws Exception {
        Integer id = 7;
        createMocks(id.toString(), "rights");
        replayMocks();

        Integer result = idUtil.newRightsId();
        Assert.assertEquals(id, result);
    }

    @Test
    public void testNewRightsIdError() throws Exception {
        createMocks("not a number", "rights");
        replayMocks();

        boolean thrown = false;
        try {
            idUtil.newRightsId();
            Assert.fail("exception expected");
        } catch (DuraCloudRuntimeException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

    private void createMocks(String id, String resource) throws Exception {
        EasyMock.expect(response.getResponseBody()).andReturn(id);
        EasyMock.expect(restHelper.post("http://" + host + ":" + port + "/" + context + "/" + resource,
                                        null,
                                        null)).andReturn(response);
    }

}
