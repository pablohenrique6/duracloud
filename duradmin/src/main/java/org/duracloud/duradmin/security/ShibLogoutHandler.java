/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.web.RestHttpHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.Authentication;
import org.springframework.security.ui.logout.LogoutHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URI;

/**
 * Note: This class was copied with minor modifications from the Management
 * Console baseline.
 * TODO: Refactor
 * <p/>
 * This class clears the Shibboleth session if the webapp is a protected
 * resource.
 *
 * @author Andrew Woods
 *         Date: 7/6/12
 */
public class ShibLogoutHandler implements LogoutHandler {

    private Logger log = LoggerFactory.getLogger(ShibLogoutHandler.class);

    protected static final String logoutPath = "/Shibboleth.sso/Logout";
    protected static final String duracloudDomain = "duracloud.org";

    private RestHttpHelper restHelper;

    public ShibLogoutHandler() {
        this(new RestHttpHelper());
    }

    public ShibLogoutHandler(RestHttpHelper restHelper) {
        this.restHelper = restHelper;
    }

    @Override
    public void logout(HttpServletRequest httpServletRequest,
                       HttpServletResponse httpServletResponse,
                       Authentication authentication) {
        try {
            doLogout(httpServletRequest, httpServletResponse);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException("Error logging out!", e);
        }
    }

    private void doLogout(HttpServletRequest httpServletRequest,
                          HttpServletResponse httpServletResponse)
        throws IOException {
        // Determine host
        URI url = getRequestUrl(httpServletRequest);
        String scheme = url.getScheme();
        String httpHost = url.getSchemeSpecificPart();

        String proto = (null == scheme) ? "none" : scheme;
        String host = (null == httpHost ? "none" : httpHost);
        String baseUrl = proto + ":" + host;
        log.debug("host: {}", baseUrl);

        // Log out of shib if necessary
        if (host.contains(duracloudDomain)) {
            log.info("Logging out of shibboleth: {}", baseUrl);
            RestHttpHelper.HttpResponse response = doShibLogout(baseUrl);

            String body = response.getResponseBody();
            if (null != body) {
                httpServletResponse.getOutputStream().write(body.getBytes());
                httpServletResponse.setContentType("text/html");

            } else {
                log.warn("Null response while logging out of shib: {}", host);
            }

        } else {
            log.debug("No shib logout performed");
        }
    }

    private URI getRequestUrl(HttpServletRequest httpServletRequest) {
        try {
            return new URI(httpServletRequest.getRequestURL().toString());
        } catch (Exception e) {
            throw new DuraCloudRuntimeException("Unable to create URI", e);
        }
    }

    private RestHttpHelper.HttpResponse doShibLogout(String hostText) {
        try {
            return restHelper.get(hostText + logoutPath);
        } catch (Exception e) {
            throw new DuraCloudRuntimeException("Unable to do shib logout", e);
        }
    }
}
