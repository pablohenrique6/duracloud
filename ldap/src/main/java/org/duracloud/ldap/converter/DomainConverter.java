/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.ldap.converter;

import org.springframework.ldap.core.simple.ParameterizedContextMapper;

import javax.naming.directory.Attributes;


/**
 * Note: This class is a proper subset of the class of the same name in
 * Management Console baseline.
 * TODO: Refactor
 *
 * @author Andrew Woods
 *         Date: 1/3/13
 */
public interface DomainConverter<T> extends ParameterizedContextMapper<T> {

    /**
     * This method converts the arg domain object to a set of LDAP attributes.
     *
     * @param item to convert
     * @return LDAP attributes representing arg object
     */
    public Attributes toAttributes(T item);

}
