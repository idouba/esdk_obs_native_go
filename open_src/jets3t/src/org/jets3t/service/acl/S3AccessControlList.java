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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jets3t.service.Constants;
import org.jets3t.service.ServiceException;
import org.jets3t.service.model.StorageOwner;

import com.jamesmurty.utils.XMLBuilder;

/**
 * Represents an Amazon S3 Access Control List (ACL), including the ACL's set of grantees and the
 * permissions assigned to each grantee.
 *
 * @author James Murty
 *
 */
public class S3AccessControlList implements Serializable {

    private static final long serialVersionUID = -4616688515622838285L;

    /**
     * A pre-canned REST ACL to set an object's permissions to Private (only owner can read/write)
     */
    public static final S3AccessControlList REST_CANNED_PRIVATE = new S3AccessControlList();

    /**
     * A pre-canned REST ACL to set an object's permissions to Public Read (anyone can read, only owner
     * can write)
     */
    public static final S3AccessControlList REST_CANNED_PUBLIC_READ = new S3AccessControlList();

    /**
     * A pre-canned REST ACL to set an object's permissions to Public Read and Write (anyone can
     * read/write)
     */
    public static final S3AccessControlList REST_CANNED_PUBLIC_READ_WRITE = new S3AccessControlList();

    /**
     * A pre-canned REST ACL to set an object's permissions to Authenticated Read (authenticated Amazon
     * users can read, only owner can write)
     */
    public static final S3AccessControlList REST_CANNED_AUTHENTICATED_READ = new S3AccessControlList();

    protected final HashSet<S3GrantAndPermission> grants = new HashSet<S3GrantAndPermission>();
    protected StorageOwner owner = null;

    /**
     * Returns a string representation of the ACL contents, useful for debugging.
     */
    @Override
    public String toString() {
        return "AccessControlList [owner=" + owner + ", grants=" + getGrantAndPermissions() + "]";
    }

    public StorageOwner getOwner() {
        return owner;
    }

    public void setOwner(StorageOwner owner) {
        this.owner = owner;
    }

    /**
     * @param grantee
     * @return list of permissions assigned to the given grantee in this ACL
     */
    public List<S3Permission> getPermissionsForGrantee(S3GranteeInterface grantee) {
        List<S3Permission> permissions = new ArrayList<S3Permission>();
        for (S3GrantAndPermission gap: grants) {
            if (gap.getGrantee().equals(grantee)) {
                permissions.add(gap.getPermission());
            }
        }
        return permissions;
    }

    /**
     * @param permission
     * @return list of grantees assigned the given permission in this ACL
     */
    public List<S3GranteeInterface> getGranteesWithPermission(S3Permission permission) {
        List<S3GranteeInterface> grantees = new ArrayList<S3GranteeInterface>();
        for (S3GrantAndPermission gap: grants) {
            if (gap.getPermission().equals(permission)) {
                grantees.add(gap.getGrantee());
            }
        }
        return grantees;
    }

    /**
     * @param grantee
     * @param permission
     * @return true if the given grantee has the given permission in this ACL
     */
    public boolean hasGranteeAndPermission(S3GranteeInterface grantee, S3Permission permission) {
        return getPermissionsForGrantee(grantee).contains(permission);
    }

    /**
     * Adds a grantee to the ACL with the given permission. If this ACL already contains the grantee
     * (ie the same grantee object) the permission for the grantee will be updated.
     *
     * @param grantee
     *        the grantee to whom the permission will apply
     * @param permission
     *        the permission to apply to the grantee.
     */
    public void grantPermission(S3GranteeInterface grantee, S3Permission permission) {
        grants.add(new S3GrantAndPermission(grantee, permission));
    }

    /**
     * Adds a set of grantee/permission pairs to the ACL, where each item in the set is a
     * {@link S3GrantAndPermission} object.
     *
     * @param grantAndPermissions
     * the grant and permission combinations to add.
     */
    public void grantAllPermissions(S3GrantAndPermission[] grantAndPermissions) {
        for (int i = 0; i < grantAndPermissions.length; i++) {
            S3GrantAndPermission gap = grantAndPermissions[i];
            grantPermission(gap.getGrantee(), gap.getPermission());
        }
    }

    /**
     * Revokes the permissions of a grantee by removing the grantee from the ACL.
     *
     * @param grantee
     *        the grantee to remove from this ACL.
     */
    public void revokeAllPermissions(S3GranteeInterface grantee) {
        List<S3GrantAndPermission> grantsToRemove = new ArrayList<S3GrantAndPermission>();
        for (S3GrantAndPermission gap: grants) {
            if (gap.getGrantee().equals(grantee)) {
                grantsToRemove.add(gap);
            }
        }
        grants.removeAll(grantsToRemove);
    }

    /**
     * @return
     * the grant and permission collections in this ACL.
     */
    public S3GrantAndPermission[] getGrantAndPermissions() {
        return grants.toArray(
            new S3GrantAndPermission[grants.size()]);
    }

    public XMLBuilder toXMLBuilder() throws ServiceException, ParserConfigurationException,
        FactoryConfigurationError, TransformerException
    {
        if (owner == null) 
        {
            String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Error>" +
            "<Code>InvalidArgument</Code>" +
            "<Message>Invalid AccessControlList: missing an owner</Message>" +
            "<RequestId></RequestId>" +
            "<HostId></HostId>" +
            "</Error>";
            ServiceException se = new ServiceException("Invalid AccessControlList: missing an owner", xmlMessage);
            se.setResponseCode(400);
            throw se;
        }
        
        if (null == owner.getId() || "" == owner.getId())
        {
            String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Error>" +
            "<Code>InvalidArgument</Code>" +
            "<Message>Invalid AccessControlList: missing an owner ID</Message>" +
            "<RequestId></RequestId>" +
            "<HostId></HostId>" +
            "</Error>";
            ServiceException se = new ServiceException("Invalid AccessControlList: missing an owner ID", xmlMessage);
            se.setResponseCode(400);
            throw se;
        }
        
        XMLBuilder builder = null;
        if (null != owner.getDisplayName())
        {
            builder = XMLBuilder.create("AccessControlPolicy")
            .attr("xmlns", Constants.XML_NAMESPACE)
            .elem("Owner")
                .elem("ID").text(owner.getId()).up()
                .elem("DisplayName").text(owner.getDisplayName()).up()
            .up();
        }
        else 
        {
            builder = XMLBuilder.create("AccessControlPolicy")
            .attr("xmlns", Constants.XML_NAMESPACE)
            .elem("Owner")
                .elem("ID").text(owner.getId()).up().up();
        }
            
        XMLBuilder accessControlList = builder.elem("AccessControlList");
        for (S3GrantAndPermission gap: grants) {
            S3GranteeInterface grantee = gap.getGrantee();
            S3Permission permission = gap.getPermission();
            accessControlList
                .elem("Grant")
                    .importXMLBuilder(grantee.toXMLBuilder())
                    .elem("Permission").text(permission.toString());
        }
        return builder;
    }

    /**
     * @return
     * an XML representation of the Access Control List object, suitable to send to
     * a storage service in the request body.
     */
    public String toXml() throws ServiceException {
            try
            {
                return toXMLBuilder().asString();
            }
            catch (TransformerException e)
            {
                String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Error>" +
                    "<Code>TransformerException</Code>" +
                    "<Message>"+ e.getMessage() + "</Message>" +
                    "<RequestId></RequestId>" +
                    "<HostId></HostId>" +
                    "</Error>";
                ServiceException se = new ServiceException("Failed to build XML document for ACL", xmlMessage, e);
                throw se;
            }
            catch (ParserConfigurationException e)
            {
                String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Error>" +
                "<Code>ParserConfigurationException</Code>" +
                "<Message>"+ e.getMessage() + "</Message>" +
                "<RequestId></RequestId>" +
                "<HostId></HostId>" +
                "</Error>";
                ServiceException se = new ServiceException("Failed to build XML document for ACL", xmlMessage, e);
                throw se;
            }
            catch (FactoryConfigurationError e)
            {
                String xmlMessage = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><Error>" +
                "<Code>FactoryConfigurationError</Code>" +
                "<Message>"+ e.getMessage() + "</Message>" +
                "<RequestId></RequestId>" +
                "<HostId></HostId>" +
                "</Error>";
                ServiceException se = new ServiceException("Failed to build XML document for ACL", xmlMessage, e);
                throw se;
            }
    }

    /**
     * @return
     * true if this ACL is a REST pre-canned one, in which case REST/HTTP implementations can use
     * the <tt>x-amz-acl</tt> header as a short-cut to set permissions on upload rather than using
     * a full ACL XML document.
     *
     * @deprecated 0.8.0
     */
    @Deprecated
    public boolean isCannedRestACL() {
        return isRESTHeaderACL();
    }

    /**
     * @return true if this ACL can be set via an HTTP header, rather than via an XML document.
     */
    public boolean isRESTHeaderACL() {
        return getValueForRESTHeaderACL() != null;
    }

    /**
     * @return the header value string for this ACL if it is a canned ACL, otherwise return null;
     */
    public String getValueForRESTHeaderACL() {
        if (S3AccessControlList.REST_CANNED_PRIVATE.equals(this)) {
            return "private";
        } else if (S3AccessControlList.REST_CANNED_PUBLIC_READ.equals(this)) {
            return "public-read";
        } else if (S3AccessControlList.REST_CANNED_PUBLIC_READ_WRITE.equals(this)) {
            return "public-read-write";
        } else if (S3AccessControlList.REST_CANNED_AUTHENTICATED_READ.equals(this)) {
            return "authenticated-read";
        }
        return null;
    }

}
