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
public enum LdapObjectClass {
    DC_OBJECT("dcObject"),
    ORGANIZATION("organization"),
    PERSON("x-idp-person"),
    RIGHTS("x-idp-rights"),
    GROUP("x-idp-group");

    private String text;

    LdapObjectClass(String text) {
        this.text = text;
    }

    public String toString() {
        return text;
    }
}
