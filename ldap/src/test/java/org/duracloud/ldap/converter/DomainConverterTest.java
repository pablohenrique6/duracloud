/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.converter;

import org.duracloud.ldap.domain.Identifiable;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.Name;
import javax.naming.NamingEnumeration;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapName;


/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: Jun 11, 2012
 */
public abstract class DomainConverterTest<T extends Identifiable> {

    private DomainConverter<T> converter;

    private T testItem;
    private Attributes testAtts;

    @Before
    public void setUp() throws Exception {
        converter = createConverter();
        testItem = createTestItem();
        testAtts = createTestAttributes();
    }

    protected abstract DomainConverter<T> createConverter();

    protected abstract T createTestItem();

    protected abstract Attributes createTestAttributes();

    protected abstract void verifyItem(T item);

    @Test
    public void testToAttributes() throws Exception {
        Attributes atts = converter.toAttributes(testItem);
        Assert.assertNotNull(atts);

        Assert.assertEquals(testAtts.size(), atts.size());
        String name;
        Object value;

        NamingEnumeration<? extends Attribute> attsItr = atts.getAll();
        while (attsItr.hasMoreElements()) {
            Attribute att = attsItr.next();
            name = att.getID();
            value = att.get();

            boolean found = false;
            String testName;
            Object testValue;

            NamingEnumeration<? extends Attribute> testAttsItr =
                testAtts.getAll();
            while (testAttsItr.hasMoreElements()) {
                Attribute testAtt = testAttsItr.next();
                testName = testAtt.getID();
                testValue = testAtt.get();

                if (testValue instanceof byte[]) {
                    testValue = new String((byte[]) testValue);
                }

                if (testName.equals(name)) {
                    Assert.assertEquals(testName, testValue, value);
                    found = true;
                }
            }
            Assert.assertTrue("attribute not found: " + name, found);
        }
    }

    @Test
    public void testMapFromContext() throws Exception {
        Name dn = new LdapName("dc=test,dc=idp,dc=duracloud,dc=org");
        DirContextAdapter ctxt = new DirContextAdapter(testAtts, dn);
        T item = converter.mapFromContext(ctxt);
        verifyItem(item);
    }

}
