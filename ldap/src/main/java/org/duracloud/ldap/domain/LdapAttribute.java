/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.domain;

/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/3/13
 */
public enum LdapAttribute {
    USER_ID("uid"),
    SURNAME("sn"),
    GIVEN_NAME("givenName"),
    PASSWORD("userPassword"),
    MAIL("mail"),
    ORGANIZATION("o"),
    DESCRIPTION("description"),
    ORG_UNIT("organizationalUnit"),
    QUESTION("x-idp-securityQuestion"),
    ANSWER("x-idp-securityAnswer"),
    ENABLED("x-idp-enabled"),
    ACCT_NON_EXPIRED("x-idp-accountNonExpired"),
    CREDENTIALS_NON_EXPIRED("x-idp-credentialsNonExpired"),
    ACCT_NON_LOCKED("x-idp-accountNonLocked"),
    UNIQUE_ID("uniqueIdentifier"),
    COMMON_NAME("cn"),
    DISPLAY_NAME("displayName"),
    ACCOUNT("x-idp-account"),
    OBJECT_CLASS("objectClass"),
    ROLE_OCCUPANT("roleOccupant"),
    ROLE("x-idp-role"),
    MEMBER("member");

    private String text;

    private LdapAttribute(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
