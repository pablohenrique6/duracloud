/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.duradmin.control;

import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static javax.servlet.http.HttpServletResponse.SC_METHOD_NOT_ALLOWED;
import static javax.servlet.http.HttpServletResponse.SC_OK;
import static org.duracloud.common.util.ExceptionUtil.getStackTraceAsString;
import static org.duracloud.security.xml.SecurityDocumentBinding.createSecurityConfigFrom;

import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.duracloud.security.DuracloudUserDetailsService;
import org.duracloud.security.domain.SecurityConfigBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractController;

/**
 * This class initializes the application security based on the xml
 * body of the servlet request.
 *
 * @author Andrew Woods
 *         Date: Apr 19, 2010
 */
public class InitSecurityController extends AbstractController {

    private final Logger log = LoggerFactory.getLogger(InitSecurityController.class);

    private DuracloudUserDetailsService userDetailsService;

    public InitSecurityController(DuracloudUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    protected ModelAndView handleRequestInternal(HttpServletRequest request,
                                                 HttpServletResponse response)
        throws Exception {

        String method = request.getMethod();
        if (!method.equalsIgnoreCase("POST")) {
            return respond(response, "unsupported: " + method, SC_METHOD_NOT_ALLOWED);
        }

        ServletInputStream xml = request.getInputStream();
        if (xml != null) {
            try {
                SecurityConfigBean config = createSecurityConfigFrom(xml);
                userDetailsService.initialize(config.getLdapConfig(),
                                              config.getAcctIds());
                return respond(response, "Initialization Successful\n", SC_OK);

            } catch (Exception e) {
                return respond(response,
                        getStackTraceAsString(e),
                        SC_INTERNAL_SERVER_ERROR);
            }
        } else {
            return respond(response, "no users in request\n", SC_BAD_REQUEST);
        }
    }

    private ModelAndView respond(HttpServletResponse response, String msg, int status) {
        response.setStatus(status);
        log.info("writing response: status = " + status + "; msg = " + msg);
        return new ModelAndView("jsonView", "response", msg);
    }


}
