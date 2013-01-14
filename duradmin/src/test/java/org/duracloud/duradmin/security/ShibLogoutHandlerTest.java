/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import junit.framework.Assert;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.web.RestHttpHelper;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.Authentication;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Note: This class was copied with minor modifications from the Management
 * Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 7/9/12
 */
public class ShibLogoutHandlerTest {

    private ShibLogoutHandler handler;

    private RestHttpHelper restHelper;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private Authentication auth;

    @Before
    public void setUp() throws Exception {
        restHelper = EasyMock.createMock("RestHttpHelper",
                                         RestHttpHelper.class);
        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);
        response = EasyMock.createMock("HttpServletResponse",
                                       HttpServletResponse.class);
        auth = EasyMock.createMock("Authentication", Authentication.class);

        handler = new ShibLogoutHandler(restHelper);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(restHelper, request, response, auth);
    }

    private void replayMocks() {
        EasyMock.replay(restHelper, request, response, auth);
    }

    @Test
    public void testOnLogoutSuccess() throws Exception {
        String url = "https://temp.duracloud.org";
        StringBuffer urlBuffer = new StringBuffer(url);
        EasyMock.expect(request.getRequestURL()).andReturn(urlBuffer);

        RestHttpHelper.HttpResponse logoutResponse = EasyMock.createMock(
            "LogoutResponse",
            RestHttpHelper.HttpResponse.class);
        String body = "body";
        EasyMock.expect(logoutResponse.getResponseBody()).andReturn(body);
        EasyMock.replay(logoutResponse);

        EasyMock.expect(restHelper.get(url + ShibLogoutHandler.logoutPath))
                .andReturn(logoutResponse);

        ServletOutputStream outputStream = EasyMock.createMock(
            "ServletOutputStream",
            ServletOutputStream.class);
        outputStream.write(body.getBytes());
        EasyMock.expectLastCall();

        EasyMock.expect(response.getOutputStream()).andReturn(outputStream);
        response.setContentType("text/html");
        EasyMock.expectLastCall();

        replayMocks();

        handler.logout(request, response, auth);

        outputStream.close();
    }

    @Test
    public void testOnLogoutSuccessNoShib() throws Exception {
        StringBuffer url = new StringBuffer("http://localhost:8080");
        EasyMock.expect(request.getRequestURL()).andReturn(url);
        replayMocks();

        handler.logout(request, response, auth);
    }

    @Test
    public void testOnLogoutSuccessError() throws Exception {
        EasyMock.expect(request.getRequestURL()).andReturn(null);
        replayMocks();

        try {
            handler.logout(request, response, auth);
            Assert.fail("exception expected");

        } catch (DuraCloudRuntimeException e) {
            Assert.assertEquals("Unable to create URI", e.getMessage());
        }
    }
}
