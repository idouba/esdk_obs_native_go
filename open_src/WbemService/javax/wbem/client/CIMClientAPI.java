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

import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.security.ClientSecurityContext;

/**
 * The CIMClientAPI is the Interface for protocol transport implementation
 * classes.  Each implementation class must define a constructor with
 * signature:
 *<p>
 * public <code>&lt;classname&gt;</code>(String version, CIMNameSpace namespace, 
 *                                       CIMListener listener, Integer debug);
 *
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public interface CIMClientAPI {

    public void createNameSpace(String version,
				CIMNameSpace currNs,
				CIMNameSpace relNs) throws CIMException; 

    public void deleteNameSpace(String version, 
				CIMNameSpace currNs,
				CIMNameSpace relNs) throws CIMException; 

    public void deleteClass(String version, 
				CIMNameSpace currNs,
				CIMObjectPath path) throws CIMException; 

    public void deleteInstance(String version, 
				CIMNameSpace currNs,
				CIMObjectPath path) throws CIMException; 

    public void deleteQualifierType(String version, 
				CIMNameSpace currNs,
				CIMObjectPath path) throws CIMException; 

    public Vector enumerateClassNames(String version,
				      CIMNameSpace currNs,
				      CIMObjectPath path,
				      boolean deep) throws CIMException;

    public Vector enumerateClasses(String version, 
				   CIMNameSpace currNs,
				   CIMObjectPath path, 
				   boolean deep, 
				   boolean localOnly,
				   boolean includeQualifiers,
				   boolean includeClassOrigin) 
	throws CIMException; 

    public Vector enumNameSpace(String version, 
				CIMNameSpace currNs,
				CIMObjectPath path,
				boolean deep) throws CIMException; 

    public Vector enumerateInstances(String version,
                                CIMNameSpace currNs,
                                CIMObjectPath path,
                                boolean deep,
                                boolean localOnly,
                                boolean includeQualifiers,
                                boolean includeClassOrigin,
                                String propertyList[]) throws CIMException;

    public Vector enumerateInstanceNames(String version,
                                CIMNameSpace currNs,
                                CIMObjectPath path) throws CIMException;


    public Vector enumQualifierTypes(String version, 
				CIMNameSpace currNs,
				CIMObjectPath path) throws CIMException; 


    public CIMClass getClass(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name, 
				boolean localOnly,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException;

    public CIMInstance getInstance(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name,
				boolean localOnly,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException;

    public CIMValue invokeMethod(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name, 
				String methodName, 
				CIMArgument[] inArgs, 
				CIMArgument[] outArgs) throws CIMException; 

    public CIMQualifierType getQualifierType(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name) 
				throws CIMException; 

    public void setClass(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name, 
				CIMClass cc) throws CIMException; 

    public void setInstance(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name, 
				CIMInstance ci,
				boolean includeQualifiers,
				String[] propertyList) throws CIMException; 

    public void setQualifierType(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name, 
				CIMQualifierType qt) throws CIMException; 

    public void createClass(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name, 
				CIMClass cc) throws CIMException; 
    
    public CIMObjectPath createInstance(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name,
				CIMInstance ci) throws CIMException; 

    public void createQualifierType(String version, 
				CIMNameSpace currNs,
				CIMObjectPath name,
				CIMQualifierType qt) throws CIMException; 
    
    public CIMValue getProperty(String version, 
				CIMNameSpace currNs,
    				CIMObjectPath instanceName, 
				String propertyName) throws CIMException;
    
    public void setProperty(String version, 
				CIMNameSpace currNs,
				CIMObjectPath instanceName,
				String propertyName, 
				CIMValue cv) throws CIMException;

    public Vector execQuery(String version, 
				CIMNameSpace currNs,
				CIMObjectPath relNS,
				String query, 
				String queryLanguage) throws CIMException;

    public Vector associators(String version,
				CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException;

    public Vector associatorNames(String version, 
				CIMNameSpace currNs,
				CIMObjectPath objectName,
				String assocClass,
				String resultClass,
				String role,
				String resultRole) throws CIMException;

    public Vector references(String version, 
				CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String propertyList[]) throws CIMException;

    public Vector referenceNames(String version, 
				CIMNameSpace currNs,
				CIMObjectPath objectName,
				String resultClass,
				String role) throws CIMException;

    public Vector performOperations(String version,
				CIMOperation[] batchedOperations) 
				throws CIMException;

    public void initSecurityContext(String version,
				ClientSecurityContext csc)
				throws CIMException;
 
    public String getProtocol();

    /**
     * @exception CIMException The close method throws a CIM exception.
     */  
    public void close(String version) throws CIMException;

    /**
     * The protocol adapter starts a listener to received indications here.
     */
    public void setListener(String version) throws CIMException;

    /**
     * The protocol adapter starts a listener to received indications here.
     */
    public void setListener(String version, int port) throws CIMException;

    /**
     * The protocol adapter returns a CIMInstance corresponding to the
     * CIM_IndicationHandler that is associated with this protocol.
     *
     * @param     listener for which a handler is being returned. In the future
     *            the CIMListener may be extended to allow protocols to
     *            have different handlers depending on the input listener.
     * @exception CIMException is thrown with an ID of CIM_ERR_NOT_SUPPORTED
     *            if the CIMOMHandle does not support the operation. 
     *            Additionally
     */
    public CIMInstance getIndicationHandler(CIMListener listener) throws 
        CIMException;
    
    /**
     * The protocol adapter returns a CIMInstance corresponding to the
     * CIM_ListenerDestination that is associated with this protocol.
     *
     * @param     listener for which a handler is being returned. In the future
     *            the CIMListener may be extended to allow protocols to
     *            have different handlers depending on the input listener.
     * @exception CIMException is thrown with an ID of CIM_ERR_NOT_SUPPORTED
     *            if the CIMOMHandle does not support the operation. 
     *            Additionally
     */
    public CIMInstance getIndicationListener(CIMListener listener) throws 
        CIMException;
    
    public void forceShutdown(boolean isForce);
    
    public void closeEventListener(int port) throws CIMException;
}
