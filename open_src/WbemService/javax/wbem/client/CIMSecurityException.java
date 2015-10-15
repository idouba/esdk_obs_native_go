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

package javax.wbem.client;

import javax.wbem.cim.CIMException;

/** 
 * 
 * Exception class representing security exceptions that occur in
 * the CIM object manager during authentication/authorization and in providers
 * during authorization.
 * 
 * @author      Sun Microsystems, Inc.
 * @version 	1.2, 04/03/01
 * @since	WBEM 1.0
 */

public class CIMSecurityException extends CIMException {

    final static long serialVersionUID = 200;


    /**
     * The security authenticator data is invalid, or is not consistent
     * with the security mechanism being used.
     */
    public final static String INVALID_DATA = "INVALID_DATA";

    /** 
     * When the first hello message is not in the correct format. 
     * This should not happen when using the client APIs,
     * except when the data is corrupted.
     */
    public final static String NOT_HELLO = "NOT_HELLO";

    /** 
     * The first response message is not in the correct format. 
     * This should not happen when using the client APIs,
     * except when the data is corrupted.
     */
    public final static String NOT_RESPONSE = "NOT_RESPONSE";

    /**
     * The session with the given id does not exist. 
     * The parameter is a string formed from the session id byte array.
     */
    public final static String NO_SUCH_SESSION = "NO_SUCH_SESSION";

    /**
     * The user has insufficient rights to perform the operation.
     */
    public final static String CIM_ERR_ACCESS_DENIED = "CIM_ERR_ACCESS_DENIED";

    /**
     * Checksum error. 
     * This would happen if the message
     * is corrupted either accidentally or maliciously.
     */
    public final static String CHECKSUM_ERROR = "CHECKSUM_ERROR";

    /**
     * The specified principal(user account) cannot be found. 
     * This error message uses one parameter, the principal name.
     */
    public final static String NO_SUCH_PRINCIPAL = "NO_SUCH_PRINCIPAL";

    /**
     * The provided user or role credential( user passwd) is invalid.
     */
    public final static String INVALID_CREDENTIAL = "INVALID_CREDENTIAL";

    /**
     * The specified role cannot be found, or is not a role identity.
     * This error message uses one parameter, the role name.
     */
    public final static String NO_SUCH_ROLE = "NO_SUCH_ROLE";

    /**
     * The specified principal cannot assume the specified role. 
     * This error message uses two parameters: the user name and role name.
     */
    public final static String CANNOT_ASSUME_ROLE = "CANNOT_ASSUME_ROLE";

    /**
     * Creates a CIMSecurityException with no detail message.
     */
    public CIMSecurityException() {
	super();
    }

    /**
     * Creates a CIMSecurityException with the specified detail
     * message.
     *
     * @param s		the detail message.	
     */
    public CIMSecurityException(String s) {
	super(s);
    }

    /**
     * Creates a CIMSecurityException with the specified detail
     * message and one exception parameter.
     *
     * @param s		the detail message.
     * @param param     exception parameter.
     */
    public CIMSecurityException(String s, Object param) {
	super(s, param);
    }

    /**
     * Constructs a CIMSecurityException with the specified detail
     * message and two exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     */
    public CIMSecurityException(String s, Object param1, Object param2) {
	super(s, param1, param2);
    }

    /**
     * Constructs a CIMSecurityException with the specified detail 
     * message and three exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     * @param param3    third Exception parameter.
     *
     */
    public CIMSecurityException(String s, Object param1, Object param2, 
						Object param3) {
	super(s, param1, param2, param3);
    }

    /**
     * Constructs a CIMSecurityException with the specified detail
     * message and an array of exception parameters.
     *
     * @param s		the detail message.
     * @param param     array of exception parameters.
     *
     */
    public CIMSecurityException(String s, Object[] param) {
	super(s, param);
    }

}
