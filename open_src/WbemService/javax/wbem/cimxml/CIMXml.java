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


package javax.wbem.cimxml;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.CIMExport;
import javax.wbem.client.CIMOperation;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

public interface CIMXml {

    public final static String CIM = "CIM";
    public final static String CLASS = "CLASS";
    public final static String CLASSNAME = "CLASSNAME";
    public final static String CLASSPATH = "CLASSPATH";
    public final static String DECLARATION = "DECLARATION";
    public final static String ERROR = "ERROR";
    public final static String EXPPARAMVALUE = "EXPPARAMVALUE";
    public final static String HOST = "HOST";
    public final static String IMETHODCALL = "IMETHODCALL";
    public final static String IMETHODRESPONSE = "IMETHODRESPONSE";
    public final static String INSTANCE = "INSTANCE";
    public final static String INSTANCENAME = "INSTANCENAME";
    public final static String INSTANCEPATH = "INSTANCEPATH";
    public final static String IPARAMVALUE = "IPARAMVALUE";
    public final static String IRETURNVALUE = "IRETURNVALUE";
    public final static String KEYBINDING = "KEYBINDING";
    public final static String KEYVALUE = "KEYVALUE";
    public final static String LOCALCLASSPATH = "LOCALCLASSPATH";
    public final static String LOCALINSTANCEPATH = "LOCALINSTANCEPATH";
    public final static String LOCALNAMESPACEPATH = "LOCALNAMESPACEPATH";
    public final static String MESSAGE = "MESSAGE";
    public final static String METHOD = "METHOD";
    public final static String METHODCALL = "METHODCALL";
    public final static String METHODRESPONSE = "METHODRESPONSE";
    public final static String MULTIREQ = "MULTIREQ";
    public final static String MULTIRSP = "MULTIRSP";
    public final static String NAMESPACE = "NAMESPACE";
    public final static String NAMESPACEPATH = "NAMESPACEPATH";
    public final static String OBJECTPATH = "OBJECTPATH";
    public final static String PARAMETER = "PARAMETER";
    public final static String PARAMETER_ARRAY = "PARAMETER.ARRAY";
    public final static String PARAMETER_REFARRAY = "PARAMETER.REFARRAY";
    public final static String PARAMETER_REFERENCE = "PARAMETER.REFERENCE";
    public final static String PARAMVALUE = "PARAMVALUE";
    public final static String PARAMTYPE = "PARAMTYPE";
    public final static String PROPERTY = "PROPERTY";
    public final static String PROPERTY_ARRAY = "PROPERTY.ARRAY";
    public final static String PROPERTY_REFERENCE = "PROPERTY.REFERENCE";
    public final static String QUALIFIER = "QUALIFIER";
    public final static String QUALIFIER_DECLARATION = "QUALIFIER.DECLARATION";
    public final static String RETURNVALUE = "RETURNVALUE";
    public final static String SCOPE = "SCOPE";
    public final static String SIMPLEREQ = "SIMPLEREQ";
    public final static String SIMPLERSP = "SIMPLERSP";    
    public final static String VALUE = "VALUE";
    public final static String VALUE_ARRAY = "VALUE.ARRAY";
    public final static String VALUE_NAMEDINSTANCE = "VALUE.NAMEDINSTANCE";
    public final static String VALUE_NAMEDOBJECT = "VALUE.NAMEDOBJECT";
    public final static String VALUE_OBJECT = "VALUE.OBJECT";
    public final static String VALUE_OBJECTWITHLOCALPATH = 
					"VALUE.OBJECTWITHLOCALPATH";
    public final static String VALUE_OBJECTWITHPATH = "VALUE.OBJECTWITHPATH";
    public final static String VALUE_REFARRAY = "VALUE.REFARRAY";
    public final static String VALUE_REFERENCE = "VALUE.REFERENCE";
    public final static String SIMPLEEXPREQ = "SIMPLEEXPREQ";
    public final static String SIMPLEEXPRSP = "SIMPLEEXPRSP";
    public final static String MULTIEXPREQ = "MULTIEXPREQ";
    public final static String MULTIEXPRSP = "MULTIEXPRSP";
    public final static String EXPMETHODCALL = "EXPMETHODCALL";

    Document getXmlRequest(CIMOperation op)
	throws CIMException, MalformedURLException;

    Document getXmlRequest(CIMOperation[] op)
	throws CIMException, MalformedURLException;

    Document getXmlRequest(CIMExport op)
	throws CIMException, MalformedURLException;

    Document getXmlRequest(CIMExport[] op)
	throws CIMException, MalformedURLException;

    Vector getCIMResponse(Document response)
	throws IOException, CIMException;

    Document getXMLResponse(XmlResponse rsp,
				Document request,
				PasswordAuthentication auth)
	throws IOException, SAXException, ParserConfigurationException;

    void getXMLResponse(XmlResponse rsp,
                Document request,
                PasswordAuthentication auth,
                DataOutputStream out)
    throws IOException, SAXException;

    public Map getXmlRequestHeaders(Document msg)
	throws CIMException;
    public String getError(CIMException e, boolean showImplied);
    public boolean getBooleanValue(Node node);
    public String getStringValue(Node node);

    public String createInstanceResult(CIMObjectPath ci,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied);

    public String getClassResult(CIMClass cc,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied);

    public String getInstanceResult(CIMInstance ci,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied);

    public String getPropertyResult(CIMValue cv,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied);

    public String getQualifierResult(CIMQualifierType qt,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied);

    public String enumerateClassesResult(Enumeration enumClasses,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied);

    public String enumerateClassNamesResult(Enumeration enumClassNames,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied);

    public String enumerateInstancesResult(Enumeration enumInstances,
	boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied);

    public String enumerateInstanceNamesResult(Enumeration enumInstanceNames,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied);

    public String enumerateQualifiersResult(Enumeration enumQualifiers,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied);

    public String associatorsResult(Enumeration enumAssoc, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied);

    public String associatorNamesResult(Enumeration enumAssocNames, 
        CIMNameSpace ns, boolean includeQualifiers, boolean includeClassOrigin, 
        String [] propertyList, boolean showImplied);

    public String referencesResult(Enumeration enumRefs, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied);

    public String referenceNamesResult(Enumeration enumRefNames, 
        CIMNameSpace ns, boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied);

    public String enumerateNamespaceResult(Enumeration enumNamespace,
	boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied);

    public String execQueryResult(Enumeration enumQuery,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied);

    public String invokeArgsMethodResult(Vector v, boolean includeQualifiers,
        boolean includeClassOrigin, String [] propertyList, 
        boolean showImplied);


    /**
     * Returns an XML representation of this CIM class.
     *
     * @param obj      a CIM Class object
     */
    public String CIMClassToXml(CIMClass obj);

    /**
     * Returns an XML representation of this CIM class.
     *
     * @param obj      a CIM Class object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMClassToXml(CIMClass obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM class.
     *
     * @param obj      a CIM Class object
     * @param includeQualifiers if true, the qualifiers are returned.
     * @param includeClassOrigin if true, the ClassOrigin attribute
     *				 is returned
     * @param propertyList if not null, any Properties missing from this
				 list are not returned
     * @param showImplied  if true, the implied attributes are returned.
     */
    public String CIMClassToXml(CIMClass obj,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String [] propertyList,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM instance.
     *
     * @param obj      a CIM Instance object
     */
    public String CIMInstanceToXml(CIMInstance obj);

    /**
     * Returns an XML representation of this CIM instance.
     *
     * @param obj      a CIM Instance object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMInstanceToXml(CIMInstance obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM instance.
     *
     * @param obj      a CIM Instance object
     * @param includeQualifiers if true, the qualifiers are returned.
     * @param includeClassOrigin if true, the ClassOrigin attribute
     *				 is returned
     * @param propertyList if not null, any Properties missing from this
				 list are not returned
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMInstanceToXml(CIMInstance obj,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String [] propertyList,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM method.
     *
     * @param obj      a CIM Method object
     */
    public String CIMMethodToXml(CIMMethod obj);

    /**
     * Returns an XML representation of this CIM method.
     *
     * @param obj      a CIM Method object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMMethodToXml(CIMMethod obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM object path.
     *
     * @param obj      a CIM ObjectPath object
     */
    public String CIMObjectPathToXml(CIMObjectPath obj);

    /**
     * Returns an XML representation of this CIM object path.
     *
     * @param obj      a CIM ObjectPath object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMObjectPathToXml(CIMObjectPath obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM parameter.
     *
     * @param obj      a CIM Parameter object
     */
    public String CIMParameterToXml(CIMParameter obj);

    /**
     * Returns an XML representation of this CIM parameter.
     *
     * @param obj      a CIM Parameter object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMParameterToXml(CIMParameter obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM qualifier type.
     *
     * @param obj      a CIM Qualifier Type object
     */
    public String CIMQualifierTypeToXml(CIMQualifierType obj);

    /**
     * Returns an XML representation of this CIM qualifier type.
     *
     * @param obj      a CIM Qualifier Type object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMQualifierTypeToXml(CIMQualifierType obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM property.
     *
     * @param obj      a CIM Property object
     */
    public String CIMPropertyToXml(CIMProperty obj);

    /**
     * Returns an XML representation of this CIM property.
     *
     * @param obj      a CIM Property object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMPropertyToXml(CIMProperty obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM qualifier.
     *
     * @param obj      a CIM Qualifier object
     */
    public String CIMQualifierToXml(CIMQualifier obj);

    /**
     * Returns an XML representation of this CIM qualifier.
     *
     * @param obj      a CIM Qualifier object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMQualifierToXml(CIMQualifier obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM namespace.
     *
     * @param obj      a CIM NameSpace object
     */
    public String CIMNameSpaceToXml(CIMNameSpace obj);

    /**
     * Returns an XML representation of this CIM namespace.
     *
     * @param obj      a CIM NameSpace object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMNameSpaceToXml(CIMNameSpace obj,
				boolean showImplied);

    /**
     * Returns an XML representation of this CIM value.
     *
     * @param obj      a CIM Value object
     */
    public String CIMValueToXml(CIMValue obj);

    /**
     * Returns an XML representation of this CIM value.
     *
     * @param obj      a CIM Value object
     * @param showImplied  if true, the implied attributes are returned
     */
    public String CIMValueToXml(CIMValue obj,
				boolean showImplied);

    /**
     * Construct a Java object representing a CIM Class
     * from the specified DOM element node.
     * 
     * @param node      a DOM element node
     */
    public CIMClass getCIMClass(Node node);

    /**
     * Construct a Java object representing a CIM Instance
     * from the specified DOM element node.
     * 
     * @param node      a DOM element node
     */
    public CIMInstance getCIMInstance(Node node);

    /**
     * Construct a Java object representing a CIM Method
     * from the specified DOM element node.
     * 
     * @param node      a DOM element node
     */
    public CIMMethod getCIMMethod(Node node);

    /**
     * Construct a Java object representing a CIM ObjectPath
     * from the specified DOM element node.
     *
     * @param node      a DOM element node
     */
    public CIMObjectPath getCIMObjectPath(Node node);

    /**
     * Construct a Java object representing a CIM Parameter
     * from the specified DOM element node.
     *
     * @param node      a DOM element node
     */
    public CIMParameter getCIMParameter(Node node);

    /**
     * Construct a Java object representing a CIM Property
     * from the specified DOM element node.
     * 
     * @param node      a DOM element node
     */
    public CIMProperty getCIMProperty(Node node);

    /**
     * Construct a Java object representing a CIM Qualifier
     * from the specified DOM element node.
     *
     * @param node      a DOM element node
     */
    public CIMQualifier getCIMQualifier(Node node);

    /**
     * Construct a Java object representing a CIM Qualifier Type
     * from the specified DOM element node.
     *
     * @param node      a DOM element node
     */
    public CIMQualifierType getCIMQualifierType(Node node);

    /**
     * Construct a Java object representing a CIM Value 
     * from the specified DOM element node.
     *
     * @param node      a DOM element node
     * @param type      String for the data type of this CIM Value.
     */
    public CIMValue getCIMValue(Node node, String type);
}
