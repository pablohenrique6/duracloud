/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.domain;

/**
 * @author Andrew Woods
 *         Date: 1/3/13
 */
public class LdapConfig {

    private String ldapBaseDn;
    private String ldapUserDn;
    private String ldapPassword;
    private String ldapUrl;

    public LdapConfig(String baseDn,
                      String userDn,
                      String password,
                      String url) {
        this.ldapBaseDn = baseDn;
        this.ldapUserDn = userDn;
        this.ldapPassword = password;
        this.ldapUrl = url;
    }

    public String getLdapBaseDn() {
        return ldapBaseDn;
    }

    public String getLdapUserDn() {
        return ldapUserDn;
    }

    public String getLdapPassword() {
        return ldapPassword;
    }

    public String getLdapUrl() {
        return ldapUrl;
    }
}
