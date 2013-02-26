/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.security;

import junit.framework.Assert;
import org.duracloud.common.error.DuraCloudCheckedException;
import org.duracloud.common.model.SecurityUserBean;
import org.duracloud.ldap.Ldap;
import org.duracloud.ldap.domain.DuracloudGroup;
import org.duracloud.ldap.domain.Role;
import org.duracloud.ldap.error.DBNotFoundException;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import static org.duracloud.duradmin.security.AutoUserProvisionSecurityFilter.DURACLOUD_USER_URI;
import static org.duracloud.duradmin.security.AutoUserProvisionSecurityFilter.ENTITLEMENT;
import static org.duracloud.duradmin.security.AutoUserProvisionSecurityFilter.EPPN;
import static org.duracloud.duradmin.security.AutoUserProvisionSecurityFilter.MAIL;
import static org.duracloud.ldap.domain.DuracloudGroup.PREFIX;

/**
 * @author Andrew Woods
 *         Date: 2/17/13
 */
public class AutoUserProvisionSecurityFilterTest {

    private AutoUserProvisionSecurityFilter filter;

    private Ldap ldap;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain chain;

    @Before
    public void setUp() throws Exception {
        ldap = EasyMock.createMock("Ldap", Ldap.class);

        request = EasyMock.createMock("HttpServletRequest",
                                      HttpServletRequest.class);
        response = EasyMock.createMock("HttpServletResponse",
                                       HttpServletResponse.class);
        chain = EasyMock.createMock("FilterChain", FilterChain.class);

        filter = new AutoUserProvisionSecurityFilter(ldap);
    }

    @After
    public void tearDown() throws Exception {
        EasyMock.verify(ldap, request, response, chain);
    }

    private void replayMocks() {
        EasyMock.replay(ldap, request, response, chain);
    }

    @Test
    public void testDoFilterHttp() throws Exception {
        String eppnPre = "awoods@";
        boolean exists = true;
        createDoFilterHttpMocks(eppnPre, exists);
        replayMocks();

        filter.doFilterHttp(request, response, chain);
    }

    @Test
    public void testDoFilterHttpDoProvision() throws Exception {
        String eppnPre = "awoods@";
        boolean exists = false;
        createDoFilterHttpMocks(eppnPre, exists);
        replayMocks();

        filter.doFilterHttp(request, response, chain);
    }

    @Test
    public void testDoFilterHttpBadInstitution() throws Exception {
        String eppnPre = "junk";
        boolean exists = false;
        createDoFilterHttpMocks(eppnPre, exists);
        replayMocks();

        filter.doFilterHttp(request, response, chain);
    }

    private void createDoFilterHttpMocks(String eppnPre,
                                         boolean exists) throws Exception {
        String institution = "duraspace";
        String eppn = eppnPre + institution + ".edu";
        String mail = eppnPre + institution + ".edu";

        Hashtable<String, String> headers = new Hashtable<>();
        headers.put(ENTITLEMENT, DURACLOUD_USER_URI);
        headers.put(EPPN, eppn);
        headers.put(MAIL, mail);

        // Read headers
        EasyMock.expect(request.getHeaderNames()).andReturn(headers.keys());

        EasyMock.expect(request.getHeader(ENTITLEMENT))
                .andReturn(headers.get(ENTITLEMENT)).times(2);
        EasyMock.expect(request.getHeader(EPPN))
                .andReturn(headers.get(EPPN))
                .times(2);
        EasyMock.expect(request.getHeader(MAIL))
                .andReturn(headers.get(MAIL))
                .times(2);

        // Get institutional acctId
        int acctId = 8;
        if (eppnPre.contains("@")) {
            EasyMock.expect(ldap.getAccountId(institution)).andReturn(acctId);

            Set<Integer> acctIds = new HashSet<>();
            acctIds.add(acctId);
            SecurityUserBean user = new SecurityUserBean();
            user.setUsername(eppn);
            if (exists) {
                EasyMock.expect(ldap.getSecurityUser(eppn, acctIds))
                        .andReturn(user);

            } else {
                EasyMock.expect(ldap.getSecurityUser(eppn, acctIds))
                        .andThrow(new DBNotFoundException("canned-exception"));

                ldap.saveSecurityUser(EasyMock.isA(SecurityUserBean.class),
                                      EasyMock.eq(acctId));
                EasyMock.expectLastCall();
            }
        }

        // Next filter
        chain.doFilter(request, response);
        EasyMock.expectLastCall();
    }

    @Test
    public void testCreateSecurityUserBean() throws Exception {
        String institution = "duraspace";
        String eppn = "awoods@" + institution + ".edu";
        String mail = "awoods@" + institution + ".edu";

        replayMocks();

        // Perform test
        SecurityUserBean user = filter.createSecurityUserBean(eppn,
                                                              institution,
                                                              mail);
        // Verify
        Assert.assertNotNull(user);

        Assert.assertEquals(eppn, user.getUsername());
        Assert.assertEquals(mail, user.getEmail());

        Assert.assertEquals(96, user.getPassword().length());

        Assert.assertEquals(1, user.getGroups().size());
        Assert.assertEquals(PREFIX + institution, user.getGroups().get(0));

        Assert.assertEquals(2, user.getGrantedAuthorities().size());
        Assert.assertTrue(user.getGrantedAuthorities()
                                  .contains(Role.ROLE_USER.name()));
        Assert.assertTrue(user.getGrantedAuthorities()
                                  .contains(Role.ROLE_ANONYMOUS.name()));

        Assert.assertTrue(user.isAccountNonExpired());
        Assert.assertTrue(user.isAccountNonLocked());
        Assert.assertTrue(user.isCredentialsNonExpired());
        Assert.assertTrue(user.isEnabled());
    }

    @Test
    public void testGetInstitution() throws Exception {
        replayMocks();

        // Test 0
        String eppn = "awoods@duraspace.org";
        String institution = filter.getInstitution(eppn);
        Assert.assertNotNull(institution);
        Assert.assertEquals("duraspace.org", institution);

        // Test 1
        eppn = "awoods@duraspace.edu";
        institution = filter.getInstitution(eppn);
        Assert.assertNotNull(institution);
        Assert.assertEquals("duraspace", institution);

        // Test 2
        boolean thrown = false;
        eppn = "awoods";
        try {
            filter.getInstitution(eppn);
            Assert.fail("exception expected");
        } catch (DuraCloudCheckedException e) {
            thrown = true;
        }
        Assert.assertTrue(thrown);
    }

}
