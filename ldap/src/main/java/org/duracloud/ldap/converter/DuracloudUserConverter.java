/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.converter;

import org.duracloud.ldap.domain.DuracloudUser;
import org.duracloud.ldap.error.ContextMapperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ldap.core.DirContextAdapter;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import java.security.InvalidParameterException;

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
 *         Date: 1/4/13
 */
public class DuracloudUserConverter implements DomainConverter<DuracloudUser> {

    private final Logger log =
        LoggerFactory.getLogger(DuracloudUserConverter.class);

    @Override
    public Attributes toAttributes(DuracloudUser item) {
        log.debug("toAttributes for: {}", item.getUsername());

        String fullname = item.getFirstName() + " " + item.getLastName();

        Attributes attrs = new BasicAttributes();
        attrs.put(OBJECT_CLASS.toString(), PERSON.toString());
        attrs.put(USER_ID.toString(), item.getUsername());
        attrs.put(UNIQUE_ID.toString(), Integer.toString(item.getId()));
        attrs.put(SURNAME.toString(), item.getLastName());
        attrs.put(GIVEN_NAME.toString(), item.getFirstName());
        attrs.put(COMMON_NAME.toString(), fullname);
        attrs.put(DISPLAY_NAME.toString(), fullname);
        attrs.put(PASSWORD.toString(), item.getPassword());
        attrs.put(MAIL.toString(), item.getEmail());
        attrs.put(QUESTION.toString(), item.getSecurityQuestion());
        attrs.put(ANSWER.toString(), item.getSecurityAnswer());
        attrs.put(ENABLED.toString(),
                  Boolean.toString(item.isEnabled()).toUpperCase());
        attrs.put(ACCT_NON_EXPIRED.toString(),
                  Boolean.toString(item.isAccountNonExpired()).toUpperCase());
        attrs.put(CREDENTIALS_NON_EXPIRED.toString(),
                  Boolean.toString(item.isCredentialsNonExpired())
                         .toUpperCase());
        attrs.put(ACCT_NON_LOCKED.toString(),
                  Boolean.toString(item.isAccountNonLocked()).toUpperCase());

        return attrs;
    }

    @Override
    public DuracloudUser mapFromContext(Object o) {
        log.debug("mapFromContent for class: {}", o.getClass());

        if (!(o instanceof DirContextAdapter)) {
            throw new InvalidParameterException("Illegal arg: " + o.getClass());
        }

        DirContextAdapter adapter = (DirContextAdapter) o;
        Attributes attrs = adapter.getAttributes();

        Attribute useridAttr = getAttribute(attrs, USER_ID.toString());
        Attribute uniqueIdentifierAttr = getAttribute(attrs,
                                                      UNIQUE_ID.toString());
        Attribute snAttr = getAttribute(attrs, SURNAME.toString());
        Attribute givenNameAttr = getAttribute(attrs, GIVEN_NAME.toString());
        Attribute userPasswordAttr = getAttribute(attrs, PASSWORD.toString());
        Attribute mailAttr = getAttribute(attrs, MAIL.toString());
        Attribute securityQuestionAttr = getAttribute(attrs,
                                                      QUESTION.toString());
        Attribute securityAnswerAttr = getAttribute(attrs, ANSWER.toString());

        int uniqueIdentifier = getInt(uniqueIdentifierAttr);
        String userid = getString(useridAttr);
        String userPassword = getBytes(userPasswordAttr);
        String givenName = getString(givenNameAttr);
        String sn = getString(snAttr);
        String mail = getString(mailAttr);
        String securityQuestion = getString(securityQuestionAttr);
        String securityAnswer = getString(securityAnswerAttr);

        return new DuracloudUser(uniqueIdentifier,
                                 userid,
                                 userPassword,
                                 givenName,
                                 sn,
                                 mail,
                                 securityQuestion,
                                 securityAnswer);
    }

    private Attribute getAttribute(Attributes attrs, String key) {
        Attribute attr = attrs.get(key);
        if (null == attr) {
            throw new ContextMapperException("Attribute not found: " + key);
        }

        if (attr.size() != 1) {
            throw new ContextMapperException(
                "Unexpected number of values: " + attr.size());
        }

        return attr;
    }

    private int getInt(Attribute attr) {
        try {
            return Integer.parseInt((String) attr.get());

        } catch (NamingException e) {
            throw new ContextMapperException(attr.getID(), e);
        }
    }

    private String getString(Attribute attr) {
        try {
            return (String) attr.get();

        } catch (NamingException e) {
            throw new ContextMapperException(attr.getID(), e);
        }
    }

    private String getBytes(Attribute passwordAttr) {
        byte[] password;
        try {
            password = (byte[]) passwordAttr.get();

        } catch (NamingException e) {
            throw new ContextMapperException(passwordAttr.getID(), e);
        }
        return new String(password);
    }


}
