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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;

/**
 * Factory for getting a CIMClientAPI impl
 *
 * @author Sun Microsystems, Inc.
 * @since  WBEM 1.0
 */
class CIMClientFactory {

    // Default values for well-known transport names.
    /**
     * XML Protocol
     */
    public final static String XML_PROTOCOL_NAME = "cim-xml";
    /**
     * RMI Protocol
     */
    public final static String RMI_PROTOCOL_NAME = "cim-rmi";

    // Configuration property name snippets.
    private final static String PROP_TRANSPORT = "transport";
    private final static String PROP_PROTOCOL  = "protocol";
    private final static String PROP_NAME  = "name";
    private final static String PROP_DEFAULT  = "default";
    private final static String PROP_CLASS = "client.class";

    /**
     * Get a CIMClient implementation for the given info.
     * 
     * @param version client version
     * @param ns namespace
     * @param protocol the protocol name
     * @param debug
     * @param clientListener
     * @return the CIMClientAPI 
     * @exception CIMException The CIMClientFactory throws a
     *         CIMException in response to an invalid protocol.
     */
    public static CIMClientAPI getClientAPI(String version,
					    CIMNameSpace ns,
					    String protocol,
					    int debug,
					    CIMListener clientListener) 
    throws CIMException {

	String propname;

	// Validate protocol and find client api implementation class name.
	// If protocol name is not specified, use the configured default.
	// If no configured default, use compiled in default.
	String proto_name = protocol;
	if ((protocol == null) || (protocol.trim().length() == 0)) {
	    propname = PROP_TRANSPORT + "." + PROP_PROTOCOL + "." +
				PROP_DEFAULT;
	    proto_name = ClientProperties.getProperty(propname);
	    if (proto_name == null) {
		proto_name = RMI_PROTOCOL_NAME;
	    }
        }
	proto_name = proto_name.toLowerCase();

	// Make sure protocol name is in the list of configured protocols.
	// If not, we throw an exception since we cannot talk to a server!
	if (proto_name != null) {
            propname = PROP_TRANSPORT + "." + PROP_PROTOCOL + "." +
			PROP_NAME + "." + proto_name;
	    String temp = ClientProperties.getProperty(propname);
	    if ((temp == null) || (! proto_name.equalsIgnoreCase(temp))) {
		proto_name = null;
	    }
	}
        if (proto_name == null) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER,
                                "protocol name");
        }
	Debug.trace1("Client adapter protocol name: " + proto_name);

	// Get the client adapter class name for the specified protocol.
	propname = PROP_TRANSPORT + "." + proto_name + "." + PROP_CLASS;
	String classname = ClientProperties.getProperty(propname);
	// System.out.println("CIMClientFactory: transport class: " +
	//                    classname);

	// Load and instantiate transport client class.
	Class cl = null;
        try {
            cl = Class.forName(classname);
        } catch (Exception ex) {
	    // System.out.println("CIMClientFactory: no class for protocol");
            throw new CIMTransportException(CIMException.CIM_ERR_NOT_FOUND,
				"protocol class");
        }

        // Instantiate the client security object.  If the constructor
	// throws a CIM exception, just re-throw it.  Otherwise, we
	// throw a CIM transport exception with the causing exception
	// as a parameter.
        CIMClientAPI cc = null;
        try {
            Class [] paramTypes = { java.lang.String.class,
				    javax.wbem.cim.CIMNameSpace.class,
                                    javax.wbem.client.CIMListener.class,
				    java.lang.Integer.class };
            Object [] paramList = { version, ns, clientListener,
				    (new Integer(debug)) };
            Constructor ctr = cl.getConstructor(paramTypes);
            cc = (CIMClientAPI) ctr.newInstance(paramList);
        } catch (InvocationTargetException ex) {
            Throwable ax = ex.getTargetException();
            // System.out.println("CIMClientFactory: error in " +
            //     classname + " constructor: " + ax.getMessage());
	    if (ax instanceof javax.wbem.cim.CIMException) {
		CIMException cex = (CIMException) ax;
		throw cex;
	    }
            throw new CIMTransportException(CIMException.CIM_ERR_FAILED, ax);
        } catch (Exception ex) {
            // System.out.println("CIMClientFactory: error creating " +
            //     classname + " instance: " + ex.getMessage());
            throw new CIMTransportException(CIMException.CIM_ERR_FAILED, ex);
        }

        // Return the transport implementation object.
        return (cc);

    } // getClientAPI
}
