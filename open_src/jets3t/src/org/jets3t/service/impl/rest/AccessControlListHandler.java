/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2007 James Murty
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
package org.jets3t.service.impl.rest;

import org.jets3t.service.acl.S3AccessControlList;
import org.jets3t.service.acl.S3CanonicalGrantee;
import org.jets3t.service.acl.EmailAddressGrantee;
import org.jets3t.service.acl.S3GranteeInterface;
import org.jets3t.service.acl.S3GroupGrantee;
import org.jets3t.service.acl.S3Permission;
import org.jets3t.service.model.S3Owner;
import org.jets3t.service.model.StorageOwner;

/**
 * Handler for AccessControlList response XML documents.
 * The document is parsed into an {@link org.jets3t.service.acl.S3AccessControlList} object available via the
 * {@link #getAccessControlList()} method.
 *
 * @author James Murty
 *
 */
public class AccessControlListHandler extends DefaultXmlHandler {
    protected S3AccessControlList accessControlList = null;

    protected StorageOwner owner = null;
    protected S3GranteeInterface currentGrantee = null;
    protected S3Permission currentPermission = null;

    protected boolean insideACL = false;

    /**
     * @return
     * an object representing the ACL document.
     */
    public S3AccessControlList getAccessControlList() {
        return accessControlList;
    }

    @Override
    public void startElement(String name) {
        if (name.equals("Owner")) {
            owner = new S3Owner();
        } else if (name.equals("AccessControlList")) {
            accessControlList = new S3AccessControlList();
            accessControlList.setOwner(owner);
            insideACL = true;
        }
    }

    @Override
    public void endElement(String name, String elementText) {
        // Owner details.
        if (name.equals("ID") && !insideACL) {
            owner.setId(elementText);
        } else if (name.equals("DisplayName") && !insideACL) {
            owner.setDisplayName(elementText);
        }
        // ACL details.
        else if (name.equals("ID")) {
            currentGrantee = new S3CanonicalGrantee();
            currentGrantee.setIdentifier(elementText);
        } else if (name.equals("EmailAddress")) {
            currentGrantee = new EmailAddressGrantee();
            currentGrantee.setIdentifier(elementText);
        } else if (name.equals("URI")) {
            currentGrantee = new S3GroupGrantee();
            currentGrantee.setIdentifier(elementText);
        } else if (name.equals("DisplayName")) {
            // In some cases we may get a DisplayName field for non-canonical grantees.
            if (currentGrantee instanceof S3CanonicalGrantee) {
                ((S3CanonicalGrantee) currentGrantee).setDisplayName(elementText);
            }
        } else if (name.equals("Permission")) {
            currentPermission = S3Permission.parsePermission(elementText);
        } else if (name.equals("Grant")) {
            accessControlList.grantPermission(currentGrantee, currentPermission);
        } else if (name.equals("AccessControlList")) {
            insideACL = false;
        }
    }
}
