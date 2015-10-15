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
package org.jets3t.service.acl;

/**
 * Simple container object to combine a grantee object with a permission object.
 *
 * @author James Murty
 */
public class S3GrantAndPermission {
    private S3GranteeInterface grantee = null;
    private S3Permission permission = null;

    public S3GrantAndPermission(S3GranteeInterface grantee, S3Permission permission) {
        this.grantee = grantee;
        this.permission = permission;
    }

    public S3GranteeInterface getGrantee() {
        return grantee;
    }

    public S3Permission getPermission() {
        return permission;
    }

    public int hashCode() {
        return (grantee + ":" + permission.toString()).hashCode();
    }

    public boolean equals(Object obj) {
        return (obj instanceof S3GrantAndPermission
            && this.getGrantee().equals(((S3GrantAndPermission)obj).getGrantee())
            && this.getPermission().equals(((S3GrantAndPermission)obj).getPermission())
            );
    }

    public String toString() {
        return "GrantAndPermission [grantee=" + grantee + ", permission=" + permission + "]";
    }

}
