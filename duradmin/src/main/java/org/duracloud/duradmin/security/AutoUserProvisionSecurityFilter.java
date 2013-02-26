/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import org.apache.commons.lang.StringUtils;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.common.util.ChecksumUtil;
import org.duracloud.ldap.Ldap;
import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.error.DBNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.ui.FilterChainOrder;
import org.springframework.security.ui.SpringSecurityFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * @author Andrew Woods
 *         Date: 2/15/13
 */
public class AutoUserProvisionSecurityFilter extends SpringSecurityFilter {

    protected final Logger log = LoggerFactory.getLogger(
            AutoUserProvisionSecurityFilter.class);

    protected static final String EPPN = "eppn";
    protected static final String MAIL = "mail";
    protected static final String ENTITLEMENT = "entitlement";

    protected static final String DURACLOUD_USER_SHORT = "duracloud-user";
    protected static final String DURACLOUD_USER_URI = "https://duracloud.org/attributes/isDCUser";

    private Ldap ldap;


    public AutoUserProvisionSecurityFilter(Ldap ldap) {
        this.ldap = ldap;
    }

    @Override
    protected void doFilterHttp(HttpServletRequest request,
                                HttpServletResponse response,
                                FilterChain chain) throws IOException, ServletException {
        // Debug
        Enumeration<String> names = request.getHeaderNames();
        while (names.hasMoreElements()) {
            String name = names.nextElement();
            String value = request.getHeader(name);
            log.debug("Header: [{}:{}]", name, value);
        }

        String eppn = request.getHeader(EPPN);
        String mail = request.getHeader(MAIL);
        String entitlement = request.getHeader(ENTITLEMENT);

        if (!StringUtils.isBlank(mail)
                && !StringUtils.isBlank(eppn)
                && !StringUtils.isBlank(entitlement)) {
            try {
                ensureUserProvisioned(mail, eppn, entitlement);

            } catch (Exception e) {
                log.warn("Unable to provision user: {}, due to: {}",
                         eppn,
                         e.getMessage());
            }

        } else {
            log.debug("Not all attributes are available for provisioning.");
        }

        // Next filter
        chain.doFilter(request, response);
    }

    private void ensureUserProvisioned(String eppn,
                                       String mail,
                                       String entitlement) throws DuraCloudCheckedException {
        // Determine account-id
        String institution = getInstitution(eppn);
        int acctId = ldap.getAccountId(institution);

        // Does user exist in system?
        Set<Integer> acctIdMask = new HashSet<>();
        acctIdMask.add(acctId);

        boolean exists = false;
        try {
            ldap.getSecurityUser(eppn, acctIdMask);
            exists = true;

        } catch (DBNotFoundException e) {
            log.info("User not yet provisioned: {}, for acct:{}", eppn, acctId);
        }

        // Provision user if not already exist and valid entitlement
        if (!exists && isDuraCloudEntitlement(entitlement)) {
            // Provision user
            SecurityUserBean user = createSecurityUserBean(eppn,
                                                           institution,
                                                           mail);

            ldap.saveSecurityUser(user, acctId);

        } else {
            log.info(
                    "Not provisioning user:{}, entitlement:{}, exists:{}, acct:{}",
                    new Object[]{eppn, entitlement, exists, acctId});
        }
    }

    // Protected for unit testing
    protected String getInstitution(String eppn) throws DuraCloudCheckedException {
        int start = eppn.indexOf('@');
        if (-1 == start) {
            throw new DuraCloudCheckedException(
                    "EPPN must be scoped with '@': {}",
                    eppn);
        }

        int end = eppn.length();
        if (eppn.toLowerCase().endsWith(".edu")) {
            end = eppn.length() - ".edu".length();
        }

        return eppn.toLowerCase().substring(start + 1, end);
    }

    // Protected for unit testing
    protected SecurityUserBean createSecurityUserBean(String eppn,
                                                      String institution,
                                                      String mail) {
        List<String> grants = new ArrayList<>();
        for (Role role : Role.ROLE_USER.getRoleHierarchy()) {
            grants.add(role.name());
        }

        List<String> groups = new ArrayList<String>();
        groups.add(DuracloudGroup.PREFIX + institution);

        // Generate password
        ChecksumUtil util = new ChecksumUtil(ChecksumUtil.Algorithm.SHA_384);
        String random = Long.toString(Math.abs(new Random().nextLong()), 36);

        String password = util.generateChecksum(random);
        String username = eppn;
        boolean enabled = true;

        return new SecurityUserBean(username,
                                    password,
                                    mail,
                                    enabled,
                                    enabled,
                                    enabled,
                                    enabled,
                                    grants,
                                    groups);
    }

    private boolean isDuraCloudEntitlement(String entitlement) {
        return entitlement.toLowerCase()
                .contains(DURACLOUD_USER_SHORT.toLowerCase())
                || entitlement.toLowerCase()
                .contains(DURACLOUD_USER_URI.toLowerCase());
    }

    @Override
    public int getOrder() {
        // Immediately before pre-auth-filter
        return FilterChainOrder.PRE_AUTH_FILTER - 1;
    }
}
