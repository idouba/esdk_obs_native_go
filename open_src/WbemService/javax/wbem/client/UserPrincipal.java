/*
 *EXHIBIT A - Sun Industry Standards Source License
 *
 *"The contents of this file are subject to the Sun Industry
 *Standards Source License Version 1.2 (the "License");
 *You may not use this file except in compliance with the
 *License. You may obtain a copy of the 
 *License at http://wbemservices.sourceforge.net/license.html
 *
 *Software distributed under the License is distributed on
 *an "AS IS" basis, WITHOUT WARRANTY OF ANY KIND, either
 *express or implied. See the License for the specific
 *language governing rights and limitations under the License.
 *
 *The Original Code is WBEM Services.
 *
 *The Initial Developer of the Original Code is:
 *Sun Microsystems, Inc.
 *
 *Portions created by: Sun Microsystems, Inc.
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): WBEM Solutions, Inc.
 */

package javax.wbem.client;

import java.io.Serializable;
import java.security.Principal;

/** 
 * 
 * This class implements a Java security Principal identity for
 * a client user identity that authenticates with a Unix username
 * and login password.  That is, it represents the user's login
 * identity on the remote server system which is running the CIMOM.
 * 
 * @author	Sun Microsystems, Inc. 
 * @since	WBEM 1.0
 */
 
public class UserPrincipal implements Principal, Serializable {

    private String username;	// User name
    private String hostname;	// Host where user was authenticated


    /**
     * Create a new UserPrincipal Object with a 
     * null username and password
     */
    public UserPrincipal() {

	username = null;
	hostname = null;

    }

    /**
     * This constructor accepts the user name.
     *
     * @param userName	The user login name.
     *
     */
    public UserPrincipal(String userName) {

	username = userName;
	hostname = null;

    }

    // ==============================================================

    // Methods defined in the Principal interface

    /**
     * Return the name of this principal identity; that is, return
     * the Unix user login name.
     *
     * @return	The name of this principal identity.
     *
     */
    public String getName() {

	return (username);

    }

    /**
     * The equals method checks if the specified object is the same
     * principal as this object.  The principals are equal if the
     * specified object is an instance of UserPrincipal and
     * the user name and authentication host name are the same.
     *
     * @param otherPrincipal	Principal instance to compare for equality
     *
     * @return	The name of this principal identity.
     *
     */
    public boolean equals(Object otherPrincipal) {

	// See if other principal is our type!
	UserPrincipal op = null;
	try {
	    op = (UserPrincipal) otherPrincipal;
	} catch (Exception ex) {
	    return (false);
	}

	// Use a dummy do-while for more structured code.
	// Only go through once.
	boolean bool = false;
	do {

	    int ohash = op.hashCode();
	    if (hashCode() != ohash)
		break;

	    String ouser = op.getUserName();
	    if ((username == null) && (ouser != null))
		break;
	    if ((username != null) && (ouser == null))
		break;
	    if ((username != null) && (! username.equals(ouser)))
		break;

	    String ohost = op.getHostName();
	    if ((hostname == null) && (ohost != null))
		break;
	    if ((hostname != null) && (ohost == null))
		break;
	    if ((hostname != null) && (! hostname.equals(ohost)))
		break;

	    bool = true;

	} while (false);

	return (bool);

    }

    /**
     * The toString method returns a string representation of the
     * principal suitable for displaying in messages.  It should
     * not be used for making authorization checks, however.
     * The format of the returned string is "user @ host".
     *
     * @return	A printable string form of the principal identity.
     *
     */
    public String toString() {

	String name = "";
	if (username != null)
	    name = name + username;
	if (hostname != null)
	    name = name + " @ " + hostname;
	return (name);

    }

    /**
     * The hashCode method returns an integer hash code to represent
     * this principal.  It can be used to test for non-equality, or
     * as an index key in a hash table.
     *
     * @return	An integer hash code representing the principal.
     *
     */
    public int hashCode() {

	int result = 0;
	if (username != null)
	    result += username.hashCode();
	if (hostname != null)
	    result += hostname.hashCode();
	return (result);

    }

    // Extended accessor methods

    /**
     * Return the principal's login user name.
     *
     * @return	The user login name.
     *
     */
    public String getUserName() {

	return (username);
    }

    /**
     * Return the server host name where this principal
     * identity was authenticated.  If null, not yet authenticated.
     *
     * @return	The server host name.
     *
     */
    public String getHostName() {

	return (hostname);
    }

    // Extend mutator methods

    /**
     * Set the principal user login name.
     *
     * @param userName	The user login name.
     *
     */
    public void setUserName(String userName) {

	username = userName;
    }

    /**
     * Set the server host name where this principal was authenticated.
     *
     * @param hostName	The server host name.
     *
     */
    public void setHostName(String hostName) {

	hostname = hostName;

    }

} // UserPrincipal
