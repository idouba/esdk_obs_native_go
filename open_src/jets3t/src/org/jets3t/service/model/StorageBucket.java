/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2006-2010 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.model;

import java.util.Date;

import org.jets3t.service.acl.S3AccessControlList;

/**
 * A generic storage bucket.
 *
 * @author James Murty
 */
public class StorageBucket extends BaseStorageItem {
    private S3AccessControlList acl = null;
    private String location = null;
    private boolean isLocationKnown = false;

    /**
     * Create a bucket without any name or location specified
     */
    public StorageBucket() {
        super();
    }

    /**
     * Create a bucket with a name.
     */
    public StorageBucket(String name) {
        super(name);
    }

    public StorageBucket(String name, String location) {
        super(name);
        this.location = location;
        this.isLocationKnown = true;
    }

    @Override
    public String toString() {
        return "StorageBucket [name=" + getName() + "] Metadata=" + getMetadataMap();
    }

    /**
     * @return
     * the bucket's creation date, or null if it is unknown.
     */
    public Date getCreationDate() {
        return (Date) getMetadata(METADATA_HEADER_CREATION_DATE);
    }

    /**
     * Sets the bucket's creation date - this should only be used internally by JetS3t
     * methods that retrieve information directly from a service.
     *
     * @param creationDate
     */
    public void setCreationDate(Date creationDate) {
        addMetadata(METADATA_HEADER_CREATION_DATE, creationDate);
    }

    /**
     * @return
     * the bucket's Access Control List, or null if it is unknown.
     */
    public S3AccessControlList getAcl() {
        return acl;
    }

    /**
     * Sets the bucket's Access Control List  - this should only be used internally by JetS3t
     * methods that retrieve information directly from a storage service.
     *
     * @param acl
     */
    public void setAcl(S3AccessControlList acl) {
        this.acl = acl;
    }

    /**
     * Set's the bucket's location. This method should only be used internally by
     * JetS3t methods that retrieve information directly from S3.
     *
     * @param location
     * A string representing the location. Legal values include
     * {@link SS3Bucket#LOCATION_US}, {@link SS3Bucket#LOCATION_EUROPE}, {@link GSBucket#LOCATION_US}
     * etc.
     */
    public void setLocation(String location) {
        this.location = location;
        this.isLocationKnown = true;
    }

    /**
     * @return
     * true if this object knows the bucket's location, false otherwise.
     */
    public boolean isLocationKnown() {
        return this.isLocationKnown;
    }

    /**
     * @return
     * the bucket's location represented as a string. "EU"
     * denotes a bucket located in Europe, while null denotes a bucket located
     * in the US.
     */
    public String getLocation() {
        return location;
    }
}
