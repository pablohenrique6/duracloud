/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.domain;

/**
 * This class is contains the configuration elements for a remote Identifier
 * Generator.
 *
 * @author Andrew Woods
 *         Date: 2/22/13
 */
public class IdUtilConfig {

    private String host;
    private String port;
    private String context;

    public IdUtilConfig() {
        // Default constructor
    }

    public IdUtilConfig(String host, String port, String context) {
        this.host = host;
        this.port = port;
        this.context = context;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }
}
