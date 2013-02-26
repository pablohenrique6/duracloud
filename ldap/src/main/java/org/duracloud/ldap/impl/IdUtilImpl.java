/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.impl;

import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.common.web.RestHttpHelper;
import org.duracloud.ldap.IdUtil;
import org.duracloud.ldap.error.DBUninitializedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gets item ids from a remote id-generator.
 *
 * @author Andrew Woods
 *         Date: 1/7/13
 */
public class IdUtilImpl implements IdUtil {

    private final Logger log = LoggerFactory.getLogger(IdUtilImpl.class);

    private String host;
    private String port;
    private String context;
    private RestHttpHelper restHelper;

    @Override
    public void initialize(String host,
                           String port,
                           String context) {
        initialize(host, port, context, null);
    }

    // For unit-test
    protected void initialize(String host,
                              String port,
                              String context,
                              RestHttpHelper restHelper) {
        if (null == host || null == port || null == context) {
            throw new IllegalArgumentException("Args must not be null!");
        }

        this.host = host;
        this.port = port;
        this.context = context;

        if (null == restHelper) {
            restHelper = new RestHttpHelper();
        }
        this.restHelper = restHelper;
    }

    private void checkInitialized() {
        if (null == host || null == port || null == context || null == restHelper) {
            throw new DBUninitializedException("IdUtil must be initialized");
        }
    }

    @Override
    public int newUserId() {
        return doGetId("user");
    }

    @Override
    public int newRightsId() {
        return doGetId("rights");
    }

    @Override
    public int newGroupId() {
        return doGetId("group");
    }

    private int doGetId(String resource) {
        checkInitialized();

        RestHttpHelper.HttpResponse response;
        try {
            response = restHelper.post(getBaseUrl() + "/" + resource,
                                       null,
                                       null);
        } catch (Exception e) {
            log.error("Error getting new '" + resource + "' ID!", e);
            throw new DuraCloudRuntimeException(
                    "Error getting '" + resource + "' ID: msg = " + e.getMessage(),
                    e);
        }

        String body;
        try {
            body = response.getResponseBody();

        } catch (Exception e) {
            log.error("Error getting response body for '" + resource + "' ID!",
                      e);
            throw new DuraCloudRuntimeException(
                    "Error getting response body for '" + resource + "' ID: msg = " + e
                            .getMessage(),
                    e);
        }

        if (null == body) {
            log.error("Response was null for new '" + resource + "' ID!");
        }

        try {
            return Integer.parseInt(body);

        } catch (Exception e) {
            log.error("Error parsing integer from new '" + resource + "' ID body: {}",
                      body,
                      e);
            throw new DuraCloudRuntimeException(
                    "Error parsing integer from new '" + resource + "' ID body: " + body,
                    e);
        }
    }

    private String getBaseUrl() {
        return getProtocol() + getHost() + ":" + getPort() + "/" + getContext();
    }

    private String getProtocol() {
        String protocol = "http://";
        if (getPort().equals("443")) {
            protocol = "https://";
        }
        return protocol;
    }

    private String getHost() {
        return host;
    }

    private String getPort() {
        return port;
    }

    private String getContext() {
        return context;
    }
}
