/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.appconfig.domain;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.security.domain.SecurityConfigBean;
import org.duracloud.security.xml.SecurityDocumentBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * This class holds the configuration elements for application security.
 *
 * @author Andrew Woods
 *         Date: Apr 20, 2010
 */
public class SecurityConfig extends BaseConfig implements AppConfig {
    private final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    private final static String INIT_RESOURCE = "/security";

    protected final static String QUALIFIER = "security";
    protected final static String acctIdKey = "acct-id";

    protected final static String ldapKey = "ldap";
    protected final static String ldapBaseDnKey = "basedn";
    protected final static String ldapUserDnKey = "userdn";
    protected final static String ldapPasswordKey = "password";
    protected final static String ldapUrlKey = "url";

    private LdapConfig ldapConfig = new LdapConfig();
    private Set<Integer> acctIds = new HashSet<>();


    public String asXml() {
        return SecurityDocumentBinding.createDocumentFrom(new SecurityConfigBean(
            ldapConfig,
            acctIds));
    }

    public String getInitResource() {
        return INIT_RESOURCE;
    }

    protected String getQualifier() {
        return QUALIFIER;
    }

    protected void loadProperty(String key, String value) {
        key = key.toLowerCase();
        String prefix = getPrefix(key);
        if (prefix.equalsIgnoreCase(ldapKey)) {
            String suffix = getSuffix(key);
            loadLdap(suffix, value);

        } else if (prefix.equalsIgnoreCase(acctIdKey)) {
            String suffix = getSuffix(key);
            loadAccts(suffix, value);

        } else {
            String msg = "unknown key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadLdap(String key, String value) {
        if (key.equalsIgnoreCase(ldapBaseDnKey)) {
            ldapConfig.setLdapBaseDn(value);

        } else if (key.equalsIgnoreCase(ldapUserDnKey)) {
            ldapConfig.setLdapUserDn(value);

        } else if (key.equalsIgnoreCase(ldapPasswordKey)) {
            ldapConfig.setLdapPassword(value);

        } else if (key.equalsIgnoreCase(ldapUrlKey)) {
            ldapConfig.setLdapUrl(value);

        } else {
            String msg = "unknown user key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }
    }

    private void loadAccts(String key, String value) {
        String index = getSuffix(key);

        if (StringUtils.isBlank(index)) {
            String msg = "invalid key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg);
        }

        int acctId;
        try {
            acctId = Integer.parseInt(value);

        } catch (NumberFormatException e) {
            String msg = "Invalid acctId key: " + key + " (" + value + ")";
            log.error(msg);
            throw new DuraCloudRuntimeException(msg, e);
        }

        acctIds.add(acctId);
    }

    public LdapConfig getLdapConfig() {
        return ldapConfig;
    }

    public Set<Integer> getAcctIds() {
        return acctIds;
    }
}
