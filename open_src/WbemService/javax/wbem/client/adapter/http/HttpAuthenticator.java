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

package javax.wbem.client.adapter.http;

import java.net.URL;

/**
 * An interface for all objects that implement HTTP authentication.
 * See the HTTP spec for details on how this works in general.
 * A single class or object can implement an arbitrary number of
 * authentication schemes.  
 *
 * @author David Brown
 *
 * @deprecated -- use java.net.Authenticator instead
 * @see java.net.Authenticator
 */
//
// REMIND:  Unless compatibility with sun.* API's from 1.2 to 2.0 is
// a goal, there's no reason to carry this forward into JDK 2.0.

interface HttpAuthenticator {
    

    /**
     * Indicate whether the specified authentication scheme is
     * supported.  In accordance with HTTP specifications, the
     * scheme name should be checked in a case-insensitive fashion.
     */

    boolean schemeSupported (String scheme);

    /**
     * Returns the String that should be included in the HTTP
     * <B>Authorization</B> field.  Return null if no info was
     * supplied or could be found.
     * <P>
     * Example:
     * --> GET http://www.authorization-required.com/ HTTP/1.0
     * <-- HTTP/1.0 403 Unauthorized
     * <-- WWW-Authenticate: Basic realm="WallyWorld"
     * call schemeSupported("Basic"); (return true)
     * call authString(u, "Basic", "WallyWorld", null);
     *   return "QWadhgWERghghWERfdfQ=="
     * --> GET http://www.authorization-required.com/ HTTP/1.0
     * --> Authorization: Basic QWadhgWERghghWERfdfQ==
     * <-- HTTP/1.0 200 OK
     * <B> YAY!!!</B>
     */

    String authString (URL u, String scheme, String realm);

}




