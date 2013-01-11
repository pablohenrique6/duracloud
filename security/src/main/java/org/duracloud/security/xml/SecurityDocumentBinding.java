/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.security.xml;

import org.apache.xmlbeans.XmlException;
import org.apache.xmlbeans.XmlObject;
import org.duracloud.SecurityConfigDocument;
import org.duracloud.SecurityConfigType;
import org.duracloud.common.error.DuraCloudRuntimeException;
import org.duracloud.security.domain.SecurityConfigBean;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class is a helper utility for binding SecurityConfigBean objects to a
 * SecurityConfig xml document.
 *
 * @author Andrew Woods
 *         Date: Apr 15, 2010
 */
public class SecurityDocumentBinding {

    /**
     * This method binds a SecurityConfigBean to the content of the arg xml.
     *
     * @param xml document to be bound to SecurityConfigBean
     * @return SecurityConfigBean
     */
    public static SecurityConfigBean createSecurityConfigFrom(InputStream xml) {
        try {
            SecurityConfigDocument doc = SecurityConfigDocument.Factory.parse(
                xml);
            return SecurityConfigElementReader.createSecurityConfigFrom(doc);
        } catch (XmlException e) {
            throw new DuraCloudRuntimeException(e);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        }
    }

    /**
     * This method serializes the arg SecurityConfigBean into an xml document.
     *
     * @param config SecurityConfigBean to be serialized
     * @return SecurityConfig xml document
     */
    public static String createDocumentFrom(SecurityConfigBean config) {
        SecurityConfigDocument doc =
            SecurityConfigDocument.Factory.newInstance();
        if (null != config) {
            SecurityConfigType configType =
                SecurityConfigElementWriter.createSecurityConfigElementFrom(
                    config);
            doc.setSecurityConfig(configType);
        }
        return docToString(doc);
    }

    private static String docToString(XmlObject doc) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            doc.save(outputStream);
        } catch (IOException e) {
            throw new DuraCloudRuntimeException(e);
        } finally {
            try {
                outputStream.flush();
                outputStream.close();
            } catch (IOException e) {
                throw new DuraCloudRuntimeException(e);
            }
        }
        return outputStream.toString();
    }

}