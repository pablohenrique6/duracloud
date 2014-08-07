/*
 * The contents of this file are subject to the license and copyright
 * detailed in the LICENSE and NOTICE files at the root of the source
 * tree and available online at
 *
 *     http://duracloud.org/license/
 */
package org.duracloud.snapshot.dto.bridge;

import org.duracloud.common.json.JaxbJsonSerializer;
import org.duracloud.snapshot.dto.RestoreStatus;
import org.duracloud.snapshot.error.SnapshotDataException;

import javax.xml.bind.annotation.XmlValue;
import java.io.IOException;

/**
 * @author Daniel Bernstein
 *         Date: 7/28/14
 */
public class GetRestoreStatusBridgeResult {

    /**
     * The Restoration status 
     */
    @XmlValue
    private RestoreStatus status;

    /**
     * The details of the current status
     */
    @XmlValue
    private String details;

    public GetRestoreStatusBridgeResult(){}

    public GetRestoreStatusBridgeResult(RestoreStatus status,
                                          String details) {
        this.status = status;
        this.details = details;
    }

    
    
    /**
     * Parses properties from bridge result string
     *
     * @param bridgeResult - JSON formatted set of properties
     */
    public static GetRestoreStatusBridgeResult deserialize(String bridgeResult) {
        JaxbJsonSerializer<GetRestoreStatusBridgeResult> serializer =
            new JaxbJsonSerializer<>(GetRestoreStatusBridgeResult.class);
        try {
            return serializer.deserialize(bridgeResult);
        } catch(IOException e) {
            throw new SnapshotDataException(
                "Unable to deserialize result due to: " + e.getMessage());
        }
    }

    public RestoreStatus getStatus() {
        return status;
    }

    public void setStatus(RestoreStatus status) {
        this.status = status;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

}