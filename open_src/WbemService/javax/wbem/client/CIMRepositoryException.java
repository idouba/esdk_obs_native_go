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
 * Exception class representing exceptions that occur in a CIM persistent
 * store.
 * 
 * @author      Sun Microsystems, Inc.
 * @version 	1.2 04/03/01
 * @since	WBEM 1.0
 */

public class CIMRepositoryException extends CIMException {

    final static long serialVersionUID = 200;


    /** 
     * The repository is unavailable. 
     */
    public final static String PS_UNAVAILABLE = "PS_UNAVAILABLE";

    /** 
     * The repository is configured incorrectly. 
     */
    public final static String PS_CONFIG = "PS_CONFIG";

    /**
     * Creates a CIMRepositoryException with no detail message.
     */
    public CIMRepositoryException() {
	super();
    }

    /**
     * Creates a CIMRepositoryException with the specified 
     * detail message. 
     *
     * @param s		the detail message.
     */
    public CIMRepositoryException(String s) {
	super(s);
    }

    /**
     * Creates a CIMRepositoryException with the specified detail
     * message and one exception parameter.
     *
     * @param s		the detail message.
     * @param param     exception parameter.
     */
    public CIMRepositoryException(String s, Object param) {
	super(s, param);
    }

    /**
     * Creates a CIMRepositoryException with the specified detail
     * message and two exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     */
    public CIMRepositoryException(String s, Object param1, Object param2) {
	super(s, param1, param2);
    }

    /**
     * Creates a CIMRepositoryException with the specified detail
     * message and three exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     * @param param3    third Exception parameter.
     *
     */
    public CIMRepositoryException(String s, 
				  Object param1, 
				  Object param2, 
			    	  Object param3) {
	super(s, param1, param2, param3);
    }

    /**
     * Creates a CIMRepositoryException with the specified detail
     * message and an array of exception parameters.
     *
     * @param s		the detail message.
     * @param param     array of exception parameters.
     *
     */
    public CIMRepositoryException(String s, Object[] param) {
	super(s, param);
    }


}
