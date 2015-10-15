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
 *Contributor(s): Brian Schlosser
*/

package javax.wbem.security;

import java.security.Principal;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;

/**
 * This class represents an implementation of the client security
 * interface for the Basic Authentication mechanism of
 * the HTTP protocol.  Since the HTTP protocol is essentially
 * connectionless, the client principal identity is authenticated
 * for each request to the remote CIMOM server; that is, there is
 * no security session established and no initial authentication
 * message exchange.
 *
 * @author 	Sun Microsystems, Inc.
 * @since	WBEM 1.0
 */
public class BasicClientSecurity extends ClientSecurity {

    // Our security mechanism name.
    // Must align with the mechanism name in the WbemDefaults properties!
    private static final String mechanism = "basic";

    public BasicClientSecurity() {
        super(mechanism);
    }

    public BasicClientSecurity(CIMNameSpace ns, Principal prin, Object cred)
	throws CIMException {

	this();
	this.setNameSpace(ns);
	this.setPrincipal(prin);
	this.setCredential(cred);
    }
}
