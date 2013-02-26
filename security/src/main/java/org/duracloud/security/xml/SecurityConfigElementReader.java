/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.duracloud.IdUtilType;
import org.duracloud.LdapType;
import org.duracloud.SecurityConfigDocument;
import org.duracloud.SecurityConfigType;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.ldap.domain.IdUtilConfig;
import org.duracloud.ldap.domain.LdapConfig;
import org.duracloud.security.domain.SecurityConfigBean;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is responsible for binding SecurityConfig xml documents to a
 * SecurityConfigBean.
 *
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityConfigElementReader {

    /**
     * This method binds a SecurityConfig xml document to a SecurityConfigBean.
     *
     * @param doc SecurityConfig xml document
     * @return SecurityConfigBean
     */
    public static SecurityConfigBean createSecurityConfigFrom(
        SecurityConfigDocument doc) {
        LdapConfig ldapConfig = null;
        IdUtilConfig idUtilConfig = null;
        Set<Integer> acctIds = null;

        SecurityConfigType configType = doc.getSecurityConfig();
        if (null != configType) {
            checkSchemaVersion(configType.getSchemaVersion());

            LdapType ldapType = configType.getLdap();
            if (null != ldapType) {
                ldapConfig = createLdapConfig(ldapType);
            }

            IdUtilType idUtilType = configType.getIdUtil();
            if (null != ldapType) {
                idUtilConfig = createIdUtilConfig(idUtilType);
            }

            List<Integer> acctIdsList = configType.getAcctIds();
            if (null != acctIdsList) {
                acctIds = new HashSet<>();
                acctIds.addAll(acctIdsList);
            }
        }

        if (null != ldapConfig && null != acctIds) {
            return new SecurityConfigBean(ldapConfig, idUtilConfig, acctIds);

        } else {
            StringBuilder err = new StringBuilder();
            err.append("Incomplete SecurityConfig! ");
            err.append("ldapConfig: ");
            err.append(null == ldapConfig ? "null" : "valid");
            err.append(", ");
            err.append("acctIds: ");
            err.append(null == acctIds ? "null" : "valid");
            throw new DuraCloudRuntimeException(err.toString());
        }
    }

    private static LdapConfig createLdapConfig(LdapType ldapType) {
        LdapConfig ldapConfig = new LdapConfig();

        ldapConfig.setLdapBaseDn(ldapType.getBasedn());
        ldapConfig.setLdapUserDn(ldapType.getUserdn());
        ldapConfig.setLdapPassword(ldapType.getPassword());
        ldapConfig.setLdapUrl(ldapType.getUrl());

        return ldapConfig;
    }

    private static IdUtilConfig createIdUtilConfig(IdUtilType idUtilType) {
        IdUtilConfig idUtilConfig = new IdUtilConfig();

        idUtilConfig.setHost(idUtilType.getHost());
        idUtilConfig.setPort(idUtilType.getPort());
        idUtilConfig.setContext(idUtilType.getContext());

        return idUtilConfig;
    }

    private static void checkSchemaVersion(String schemaVersion) {
        if (!schemaVersion.equals(SecurityConfigBean.SCHEMA_VERSION)) {
            throw new DuraCloudRuntimeException(
                "Unsupported schema version: " + schemaVersion);
        }
    }

}