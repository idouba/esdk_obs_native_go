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

import java.security.Principal;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;

/** 
 * This interface defines the common functionality of WBEM client security
 * contexts.  Concrete classes that implement this interface generate and
 * verify authenticators for establishing a secure session between the WBEM
 * client and remote CIMOM server, generate and verify message authenticators
 * (called MICs) for remote messages sent to the CIMOM, and provide
 * utility functions.  Concrete subclasses must implement a constructor
 * which accepts the remote server host name, the CIM name space, the client
 * Principal identity, and the principal's credentials.  This constructor
 * is called by the ClientSecurityFactory which creates new instances of
 * the concrete classes based upon the communications service protocol and
 * the selected security mechanism used to authenticate with the CIMOM on
 * the remote server.
 *<.p>
 * At this time, the concrete client security classes have specific methods
 * for authenticating with the remote server, and each implementation class
 * of CIMClientAPI will contain logic to call those methods.  When a more
 * standard solution for security is available based upon the GSS-API, for
 * example, a standard set of methods for authenticating and generating
 * message MIC values will be added to this interface.  At that time, the
 * CIMClientAPI implementation classes can be generalized to be security
 * mechanism independent.
 * 
 * @author	Sun Microsystems, Inc. 
 * @version 	1.2 04/03/01
 * @since	WBEM 2.0
 */
 
public interface ClientSecurityContext {

    // ====================================================================
    //
    // Interface method signatures
    //
    // ====================================================================

    /**
     * Return the name of the security mechanism.
     *
     * @return The name of the security mechanism.
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public String getMechanism();

    /**
     * Return the name of the remote peer server.
     *
     * @return The name of the remote server
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public String getServerName();

    /**
     * Return the principal being authenticated.
     *
     * @return The authenticated principal
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public Principal getPrincipal();

    /**
     * Return the CIM name space the principal is authenticated against.
     *
     * @return The authenticated CIM name space
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public CIMNameSpace getNameSpace();

    /**
     * Set the principal to be authenticated.
     *
     * @param principal	The principal to be authenticated
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public void setPrincipal(Principal principal) throws CIMException;

    /**
     * Set the CIM name space to be authenticated against.
     *
     * @param nameSpace	The CIM name space on the server
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public void setNameSpace(CIMNameSpace nameSpace) throws CIMException;

    /**
     * Set the credentials for the principal identity.
     *
     * @param credential	The credentials
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public void setCredential(Object credential) throws CIMException;

    /**
     * Return the status of the security session.
     *
     * @return	True if the security session is established.
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public boolean isEstablished();

    /**
     * Dispose of the current security session.
     *
     * @author	Sun Microsystems, Inc. 
     * @version 1.1, 08/18/00
     */
    public void dispose();

} // ClientSecurityContext
