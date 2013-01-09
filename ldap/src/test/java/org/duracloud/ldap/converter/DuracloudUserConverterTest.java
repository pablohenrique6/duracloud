/*
 * Copyright (c) 2009-2010 DuraSpace. All rights reserved.
 */
package org.duracloud.ldap.converter;

import junit.framework.Assert;
import org.duracloud.ldap.domain.DuracloudUser;

import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;

import static org.duracloud.ldap.domain.LdapAttribute.ACCT_NON_EXPIRED;
import static org.duracloud.ldap.domain.LdapAttribute.ACCT_NON_LOCKED;
import static org.duracloud.ldap.domain.LdapAttribute.ANSWER;
import static org.duracloud.ldap.domain.LdapAttribute.COMMON_NAME;
import static org.duracloud.ldap.domain.LdapAttribute.CREDENTIALS_NON_EXPIRED;
import static org.duracloud.ldap.domain.LdapAttribute.DISPLAY_NAME;
import static org.duracloud.ldap.domain.LdapAttribute.ENABLED;
import static org.duracloud.ldap.domain.LdapAttribute.GIVEN_NAME;
import static org.duracloud.ldap.domain.LdapAttribute.MAIL;
import static org.duracloud.ldap.domain.LdapAttribute.OBJECT_CLASS;
import static org.duracloud.ldap.domain.LdapAttribute.PASSWORD;
import static org.duracloud.ldap.domain.LdapAttribute.QUESTION;
import static org.duracloud.ldap.domain.LdapAttribute.SURNAME;
import static org.duracloud.ldap.domain.LdapAttribute.UNIQUE_ID;
import static org.duracloud.ldap.domain.LdapAttribute.USER_ID;
import static org.duracloud.ldap.domain.LdapObjectClass.PERSON;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 6/7/12
 */
public class DuracloudUserConverterTest extends DomainConverterTest<DuracloudUser> {

    private static final int id = 0;
    private static final String username = "username";
    private static final String password = "password";
    private static final String firstName = "firstName";
    private static final String lastName = "lastName";
    private static final String email = "email";
    private static final String securityQuestion = "question";
    private static final String securityAnswer = "answer";

    @Override
    protected DomainConverter<DuracloudUser> createConverter() {
        return new DuracloudUserConverter();
    }

    @Override
    protected DuracloudUser createTestItem() {
        return new DuracloudUser(id,
                                 username,
                                 password,
                                 firstName,
                                 lastName,
                                 email,
                                 securityQuestion,
                                 securityAnswer);
    }

    @Override
    protected Attributes createTestAttributes() {
        Attributes attrs = new BasicAttributes();
        String fullname = firstName + " " + lastName;
        String upperTrue = Boolean.TRUE.toString().toUpperCase();

        attrs.put(OBJECT_CLASS.toString(), PERSON.toString());
        attrs.put(USER_ID.toString(), username);
        attrs.put(UNIQUE_ID.toString(), Integer.toString(id));
        attrs.put(SURNAME.toString(), lastName);
        attrs.put(GIVEN_NAME.toString(), firstName);
        attrs.put(COMMON_NAME.toString(), fullname);
        attrs.put(DISPLAY_NAME.toString(), fullname);
        attrs.put(PASSWORD.toString(), password.getBytes());
        attrs.put(MAIL.toString(), email);
        attrs.put(QUESTION.toString(), securityQuestion);
        attrs.put(ANSWER.toString(), securityAnswer);
        attrs.put(ENABLED.toString(), upperTrue);
        attrs.put(ACCT_NON_EXPIRED.toString(), upperTrue);
        attrs.put(CREDENTIALS_NON_EXPIRED.toString(), upperTrue);
        attrs.put(ACCT_NON_LOCKED.toString(), upperTrue);

        return attrs;
    }

    @Override
    protected void verifyItem(DuracloudUser user) {
        Assert.assertNotNull(user);

        Assert.assertNotNull(user.getUsername());
        Assert.assertNotNull(user.getPassword());
        Assert.assertNotNull(user.getFirstName());
        Assert.assertNotNull(user.getLastName());
        Assert.assertNotNull(user.getEmail());
        Assert.assertNotNull(user.getSecurityQuestion());
        Assert.assertNotNull(user.getSecurityAnswer());

        Assert.assertEquals(id, user.getId());
        Assert.assertEquals(username, user.getUsername());
        Assert.assertEquals(password, user.getPassword());
        Assert.assertEquals(firstName, user.getFirstName());
        Assert.assertEquals(lastName, user.getLastName());
        Assert.assertEquals(email, user.getEmail());
        Assert.assertEquals(securityQuestion, user.getSecurityQuestion());
        Assert.assertEquals(securityAnswer, user.getSecurityAnswer());

        Assert.assertTrue(user.isEnabled());
        Assert.assertTrue(user.isAccountNonExpired());
        Assert.assertTrue(user.isCredentialsNonExpired());
        Assert.assertTrue(user.isAccountNonLocked());
    }

}
