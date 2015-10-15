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

package javax.wbem.security;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.client.CIMClient;
import javax.wbem.client.CIMSecurityException;
import javax.wbem.client.ClientProperties;
import javax.wbem.client.Debug;

/** 
 * This class is a factory for creating new instances of client security
 * objects.  The method createClientSecurity is called with the protocol
 * and security mechanism to be used.  These parameters are mapped into
 * the name of the corresponding class that implements the client security
 * functionality for that protocol using the security algorithms of the
 * specified mechanism.  The security class must implement the
 * ClientSecurity interface.
 *<p>
 * 
 * @author	Sun Microsystems, Inc. 
 */
 
public class ClientSecurityFactory {

    // ====================================================================
    //
    // Private define constants
    //
    // ====================================================================

    // Configuration property name snippets.
    private static final String PROP_SECURITY = "security";
    private static final String PROP_TRANSPORT = "transport";
    private static final String PROP_PROTOCOL  = "protocol";
    private static final String PROP_MECHANISM = "mechanism";
    private static final String PROP_NAME  = "name";
    private static final String PROP_DEFAULT = "default";
    private static final String PROP_CLASS = "client.class";

    // Default values for transport security for well-known transports
    // if configuration file unavailable.
    private static final String DFLT_XML_SECURITY = "basic";

    // Default values for security context implementation classes
    // for well-known mechanisms if configuration file unavailable.
    private static final String DFLT_BASIC_CLASS =
			"javax.wbem.security.BasicClientSecurity";


    // ====================================================================
    //
    // Static Methods
    //
    // ====================================================================

    /**
     * The createClientSecurity method creates a new instance of the
     * client side security object for authentication and secure messaging.
     * The specific ClientSecurity class to be used depends upon the
     * protocol and security mechanism.  The mechanism is used to define a
     * security property name to be looked up in the WBEM security properties
     * file. The ClientSecurity class name is returned for the property.
     *
     * @param ns	The remote server CIM name space
     * @param principal	The client principal identity
     * @param credential The client principal credentials
     * @param protocol	The index of the communications protocol to be used
     * @param mechanism	The name of the security mechanism to be used.
     */
    public static ClientSecurityContext createClientSecurity(CIMNameSpace ns,
	Principal principal, Object credential, String mechanism,
	String protocol) throws CIMException {

	String name = null;
	String mech = null;

	// Get the protocol name.  If not specified, use the configured
	// default.  We do not validate the protocol name, since this
	// was already done by the adapter factory.
	String proto = protocol;
	if ((protocol == null) || (protocol.trim().length() == 0)) {
	    name = PROP_TRANSPORT + "." + PROP_PROTOCOL + "." + PROP_DEFAULT;
	    proto = ClientProperties.getProperty(name);
	    if (proto == null) {
		proto = CIMClient.CIM_XML;
	    }
	}
	proto = proto.toLowerCase();

	// Make sure we have a security mechanism.
	if ((mech == null) || (mech.trim().length() == 0)) {
	    name = PROP_TRANSPORT + "." + proto + "." + PROP_SECURITY + "." +
				PROP_DEFAULT;
	    mech = ClientProperties.getProperty(name);
	    if (mech == null) {
		if (proto.equals(CIMClient.CIM_XML)) {
		    mech = DFLT_XML_SECURITY;
		} else {
		    Debug.trace1("Security mechanism not valid");
		    throw new CIMSecurityException(CIMException.CIM_ERR_NOT_FOUND,
				"default");
		}
	    }
	}
	mech = mech.toLowerCase();

	// Make sure the security mechanism is an existing mechanism
	name = PROP_SECURITY + "." + PROP_MECHANISM + "." +
			PROP_NAME + "." + mech;
	String temp = ClientProperties.getProperty(name);
	if ((temp == null) || (! mech.equalsIgnoreCase(temp))) {
	    Debug.trace1("Security mechanism not in configuration properties");
	    throw new CIMException(CIMException.CIM_ERR_NOT_FOUND,
			"mechanism-0");
	}
	Debug.trace1("Security mechanism name is " + mech);

	// Make sure our mechanism is compatible with our protocol.
	name = PROP_SECURITY + "." + mech + "." + PROP_TRANSPORT;
	String list = ClientProperties.getProperty(name);
	if (list == null) {
	    if (mech.equals(DFLT_XML_SECURITY)) {
		list = CIMClient.CIM_XML;
	    }
	}
	if (list == null) {
	    Debug.trace1("Security mechanism not supported on protocol");
	    throw new CIMSecurityException(CIMException.CIM_ERR_NOT_FOUND,
				"mechanism-1");
	}
	List al = parseList(list);
	if (! al.contains(proto)) {
	    Debug.trace1("Security mechanism not supported on protocol");
	    throw new CIMSecurityException(CIMException.CIM_ERR_NOT_FOUND,
				"mechanism-2");
	}

	// Get the class name for the given security mechanism
	name = PROP_SECURITY + "." + mech + "." + PROP_CLASS;
	String classname = ClientProperties.getProperty(name);
	if (classname == null) {
	    if (mech.equals(DFLT_XML_SECURITY)) {
		classname = DFLT_BASIC_CLASS;
	    }
	}
	if (classname == null) {
	    Debug.trace1("No security module class for mechanism");
	    throw new CIMSecurityException(CIMException.CIM_ERR_NOT_FOUND,
				"mechanism-3");
	}

	// Get the class for the given security mechanism
	Class cl = null;
	try {
	    cl = Class.forName(classname);
	} catch (Exception ex) {
	    Debug.trace1("Mechanism " + mech + ": class not found: " +
			classname, ex);
	    throw new CIMSecurityException(CIMException.CIM_ERR_NOT_FOUND,
				"mechanism-5");
	}

	// Instantiate the client security object.
	ClientSecurityContext cs = null;
	try {
	    Class [] paramTypes = { javax.wbem.cim.CIMNameSpace.class,
				    java.security.Principal.class,
				    java.lang.Object.class };
	    Object [] paramList = { ns, principal, credential };
	    Constructor ctr = cl.getConstructor(paramTypes);
	    cs = (ClientSecurityContext) ctr.newInstance(paramList);
	} catch (InvocationTargetException ex) {
	    Throwable ax = ex.getTargetException();
	    Debug.trace1("Error in security module class constructor", ax);
	    if (ax instanceof javax.wbem.cim.CIMException) {
		CIMException cex = (CIMException) ax;
		throw cex;
	    }
	    throw new CIMSecurityException(CIMException.CIM_ERR_FAILED, ax);
	} catch (Exception ex) {
	    Debug.trace1("Error creating security module instance", ex);
	    throw new CIMSecurityException(CIMException.CIM_ERR_FAILED, ex);
	}

	// Return the security object.
	return (cs);

    } // createClientSecurity

    /**
     * Return a list of the security mechanism names supported.
     *
     * @return An array of security mechanism names
     *
     */
    public static String [] getSecurityMechanisms() throws CIMException {

	String[] astr = null;
	String name = PROP_SECURITY + "." + PROP_MECHANISM;
	List list = ClientProperties.getPropertyList(name);
	if ((list != null) && (list.size() > 0)) {
	    astr = new String[list.size()];
	    list.toArray(astr);
	}
	return (astr);

    } // getSecurityMechanisms

    // ====================================================================
    //
    // Private static methods
    //
    // ====================================================================

	
    // Parse a comma separated list of strings
    private static List parseList(String list) {

	ArrayList al = new ArrayList(10);
	StringTokenizer st = new StringTokenizer(list, ",");
	while (st.hasMoreElements()) {
	    String elem = (String) st.nextElement();
	    al.add(elem);
	}					// End of while
	return al;
    } // parseList
} // ClientSecurityFactory
