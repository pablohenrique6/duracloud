/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.storage.xml;

import org.duracloud.common.util.EncryptionUtil;
import org.duracloud.storage.domain.AuditConfig;
import org.duracloud.storage.domain.DuraStoreInitConfig;
import org.duracloud.storage.error.StorageException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;

/**
 * @author Bill Branan
 *         Date: 3/18/14
 */
public class DuraStoreInitDocumentBinding {

    private final Logger log =
        LoggerFactory.getLogger(DuraStoreInitDocumentBinding.class);

    private StorageAccountsDocumentBinding accountsBinding;
    private EncryptionUtil encryptionUtil;

    public DuraStoreInitDocumentBinding() {
        accountsBinding = new StorageAccountsDocumentBinding();
        encryptionUtil = new EncryptionUtil();
    }

    /**
     * Deserializes the provided xml into durastore init config
     *
     * @param xml
     * @return
     */
    public DuraStoreInitConfig createFromXml(InputStream xml) {
        DuraStoreInitConfig initConfig = new DuraStoreInitConfig();
        try {
            SAXBuilder builder = new SAXBuilder();
            Document doc = builder.build(xml);
            Element root = doc.getRootElement();

            Element accounts = root.getChild("storageProviderAccounts");
            initConfig.setStorageAccounts(
                accountsBinding.createStorageAccountsFrom(accounts));

            Element audit = root.getChild("storageAudit");
            if(null != audit) {
                AuditConfig auditConfig = new AuditConfig();
                String encUser = audit.getChildText("auditUsername");
                if(null != encUser) {
                    auditConfig.setAuditUsername(encryptionUtil.decrypt(encUser));
                }
                String encPass = audit.getChildText("auditPassword");
                if(null != encPass) {
                    auditConfig.setAuditPassword(encryptionUtil.decrypt(encPass));
                }
                auditConfig.setAuditQueueName(audit.getChildText("auditQueue"));
                initConfig.setAuditConfig(auditConfig);
            }
        } catch (Exception e) {
            String error = "Unable to build storage account information due " +
                "to error: " + e.getMessage();
            log.error(error);
            throw new StorageException(error, e);
        }
        return initConfig;
    }

    /**
     * Serializes the provided durastore init config into xml
     *
     * @param duraStoreInitConfig
     * @param includeCredentials
     * @return
     */
    public String createXmlFrom(DuraStoreInitConfig duraStoreInitConfig,
                                boolean includeCredentials) {
        String xml = "";

        Element durastoreConfig = new Element("durastoreConfig");

        Element accounts =
            accountsBinding.createDocumentFrom(
                duraStoreInitConfig.getStorageAccounts(), includeCredentials);
        durastoreConfig.addContent(accounts);

        Element audit = new Element("storageAudit");
        AuditConfig auditConfig = duraStoreInitConfig.getAuditConfig();
        String username = encryptionUtil.encrypt(auditConfig.getAuditUsername());
        audit.addContent(new Element("auditUsername").setText(username));
        String password = encryptionUtil.encrypt(auditConfig.getAuditPassword());
        audit.addContent(new Element("auditPassword").setText(password));
        audit.addContent(new Element("auditQueue")
                             .setText(auditConfig.getAuditQueueName()));
        durastoreConfig.addContent(audit);

        Document document = new Document(durastoreConfig);
        XMLOutputter outputter = new XMLOutputter(Format.getPrettyFormat());
        return outputter.outputString(document);
    }

}
