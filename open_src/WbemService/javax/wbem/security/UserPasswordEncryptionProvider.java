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
 *Contributor(s): _______________________________________
*/

package javax.wbem.security;

import javax.wbem.cim.CIMException;

/**
 * This interface is provided to have a platform independent version for user
 * password encryption. The reference implementation uses this interface to 
 * perform password hashing based upon the password salt value and the clear
 * text user password. This allows password encryption to use platform specific 
 * implementations of password hashing algorithms.  The name of the class which
 * implements this interface is provided in the WBEM configuration properties.
 */

public interface UserPasswordEncryptionProvider {

    /**
     * The property used to define the user password provider
     * implementation class.  This property should be set in the
     * WbemDefaults or WbemClient properties files for the WBEM client.
     */
    public static final String PSWD_HASH_PROV_PROP =
			"security.password.provider.class";

    /**
     * Get the user password encrypted hash for the specified user and
     * password using the specified password salt value.  If the salt value
     * begins with a "$", the salt format is as follows:
     *.p
     * $<algorithm>$<salt_value>
     *.p
     * where the algorithm is one of those specified in crypt.conf(4) and
     * the salt value is the corresponding password salt for the user's
     * password for that algorithm.  If the salt value does not begin with
     * a "$", the standard Unix crypt(3c) encryption hash is assumed and
     * the salt value is a two-character salt.  Some hashing algorithms
     * include special options, which are specified as part of the
     * algorithm in the form of a comma-separated list of key=value
     * options; e.g., "md5,n=1000".
     *.p
     * If the crypt hash is generated, the result is a thirteen character
     * string, where the first two characters are the salt.  If a
     * $<algorithm> based hash is generated, the result is a string in
     * the format:
     *.p
     * $<algorithm>$<salt>$<encryption>
     *.p
     * If the algorithm is not supported, a CIMException is thrown.
     *
     * @param user	The name of the user
     * @param salt	The password salt for the user
     * @param password	The clear text password for the user
     *
     * @return An encrypted password hash
     */
    public String encryptPassword(String user, String salt, String password)
	throws CIMException;

} // UserPasswordEncryptionProvider
