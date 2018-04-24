package com.edu.licenta.utils;

/**
 * This enum is used to differentiate whether the artifacts fetch request was made by an NFC
 * scanned event or by user interaction
 * @author Catalin-Ioan Narita
 */
public enum ArtifactsFetchInitiatorEnum {

    /**
     * Value used for when the request is initiated due to a new artifact is discovered via NFC
     */
    NFC,

    /**
     * Value used for when the request is initiated by user interaction (i.e the user navigates to
     * ArtifactsActivity)
     */
    USER

}
