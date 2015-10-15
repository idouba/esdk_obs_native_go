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
import javax.wbem.client.Debug;

/**
 * This class implements a dummy user password encryption that allows all
 * users to be authenticated.
 */

public class UnsecurePswdEncryptionProvider
	implements UserPasswordEncryptionProvider {

    // Must be same dummy hash as server user password provider!
    private final static String EMPTYHASH = "AA0iBY3PDwjYo";

    /**
     * Get the dummy user password encrypted hash for any user, salt, and
     * password.
     *
     * @param user	The name of the user
     * @param salt	The password salt for the user
     * @param password	The clear text password for the user
     *
     * @return A dummy password hash
     */
    public String encryptPassword(String user, String salt, String password)
	throws CIMException {

	Debug.trace1("Unsecure pswd encrypt provider: using empty hash for "
				+ user);
	return EMPTYHASH;

    }

} // UnsecurePswdEncryptionProvider
