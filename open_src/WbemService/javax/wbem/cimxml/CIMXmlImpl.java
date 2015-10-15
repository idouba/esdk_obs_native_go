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
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMElement;
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
import javax.wbem.client.CIMAssociatorNamesOp;
import javax.wbem.client.CIMAssociatorsOp;
import javax.wbem.client.CIMCreateClassOp;
import javax.wbem.client.CIMCreateInstanceOp;
import javax.wbem.client.CIMCreateNameSpaceOp;
import javax.wbem.client.CIMCreateQualifierTypeOp;
import javax.wbem.client.CIMDeleteClassOp;
import javax.wbem.client.CIMDeleteInstanceOp;
import javax.wbem.client.CIMDeleteNameSpaceOp;
import javax.wbem.client.CIMDeleteQualifierTypeOp;
import javax.wbem.client.CIMEnumClassNamesOp;
import javax.wbem.client.CIMEnumClassOp;
import javax.wbem.client.CIMEnumInstanceNamesOp;
import javax.wbem.client.CIMEnumInstancesOp;
import javax.wbem.client.CIMEnumNameSpaceOp;
import javax.wbem.client.CIMEnumQualifierTypesOp;
import javax.wbem.client.CIMExecQueryOp;
import javax.wbem.client.CIMExport;
import javax.wbem.client.CIMExportIndication;
import javax.wbem.client.CIMGetClassOp;
import javax.wbem.client.CIMGetInstanceOp;
import javax.wbem.client.CIMGetPropertyOp;
import javax.wbem.client.CIMGetQualifierTypeOp;
import javax.wbem.client.CIMInvokeArgsMethodOp;
import javax.wbem.client.CIMOperation;
import javax.wbem.client.CIMReferenceNamesOp;
import javax.wbem.client.CIMReferencesOp;
import javax.wbem.client.CIMSetClassOp;
import javax.wbem.client.CIMSetInstanceOp;
import javax.wbem.client.CIMSetPropertyOp;
import javax.wbem.client.CIMSetQualifierTypeOp;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * CIMXmlImpl implements the CIMXml and CIMXmlUtil classes. 
 */
class CIMXmlImpl implements CIMXml, CIMXmlUtil {

    private final static String LOCALONLY = "LocalOnly";
    private final static String DEEPINHERITANCE = "DeepInheritance";
    private final static String INCLUDEQUALIFIERS = "IncludeQualifiers";
    private final static String INCLUDECLASSORIGIN = "IncludeClassOrigin";
    private final static String PROPERTYLIST = "PropertyList";
    private final static String ASSOCCLASS = "AssocClass";
    private final static String OBJECTNAME = "ObjectName";
    private final static String RESULTCLASS = "ResultClass";
    private final static String ROLE = "Role";
    private final static String RESULTROLE =  "ResultRole";
    private final static String QUERYLANGUAGE = "QueryLanguage";
    private final static String QUERY = "Query";
    private final static String MODIFYCLASS = "ModifyClass";
    private final static String EXPORTINDICATION = "ExportIndication";
    private final static String QUALIFIERNAME = "QualifierName";
    private final static String PROPERTYNAME = "PropertyName";
    private final static String CIMOPERATION = "CIMOperation";
    private final static String METHODCALL = "MethodCall";
    private final static String CIMBATCH = "CIMBatch";
    private final static String CIMMETHOD = "CIMMethod";
    private final static String CIMEXPORT = "CIMExport";
    private final static String METHODREQUEST = "MethodRequest";
    private final static String CIMEXPORTBATCH = "CIMExportBatch";
    private final static String CIMEXPORTMETHOD = "CIMExportMethod";
    private final static String CIMOBJECT = "CIMObject";

    private static HashMap requestHash = new HashMap();
    private static HashMap exprequestHash = new HashMap();

    public CIMXmlImpl() {
	init();
    }

    public Document getXmlRequest(CIMOperation op) 
	throws CIMException, MalformedURLException {
	XmlInstance xm = new XmlInstance();
	String cimOpName = op.getClass().getName();
	XmlRequest obj = (XmlRequest)requestHash.get(
	    cimOpName.substring(cimOpName.lastIndexOf(".")+1));
	return xmlMsg2xmlDoc(xm, xm.request(obj.getRequest(op, xm)));
    }

    public Document getXmlRequest(CIMOperation[] ops)
	throws CIMException, MalformedURLException {
	XmlInstance xm = new XmlInstance();
	Vector calls = new Vector();
	for (int i = 0; i < ops.length; i++) {
	    String cimOpName = ops[i].getClass().getName();
	    XmlRequest obj = (XmlRequest)requestHash.get(
		cimOpName.substring(cimOpName.lastIndexOf(".")+1));
	    calls.addElement(obj.getRequest(ops[i], xm));
	}
	return xmlMsg2xmlDoc(xm, xm.request(calls));
    }

    public Document getXmlRequest(CIMExport op) 
	throws CIMException, MalformedURLException {
	XmlInstance xm = new XmlInstance();
	String cimOpName = op.getClass().getName();
	XmlExpRequest obj = (XmlExpRequest)exprequestHash.get(
	    cimOpName.substring(cimOpName.lastIndexOf(".")+1));
	return xmlMsg2xmlDoc(xm, xm.exprequest(obj.getRequest(op, xm)));
    }

    public Document getXmlRequest(CIMExport[] ops)
	throws CIMException, MalformedURLException {
	XmlInstance xm = new XmlInstance();
	Vector calls = new Vector();
	for (int i = 0; i < ops.length; i++) {
	    String cimOpName = ops[i].getClass().getName();
	    XmlExpRequest obj = (XmlExpRequest)exprequestHash.get(
		cimOpName.substring(cimOpName.lastIndexOf(".")+1));
	    calls.addElement(obj.getRequest(ops[i], xm));
	}
	return xmlMsg2xmlDoc(xm, xm.exprequest(calls));
    }

    public CIMElement getCIMElement(String str)
    throws SAXException, IOException, ParserConfigurationException {

	XmlInstance xm = new XmlInstance();
	Document xml = xm.pcdata2Xml(str);
	CIMElement ce = new CIMElement();
	NodeTreeWalker walker = new NodeTreeWalker(xml);
	for (Node node = walker.getCurrentNode(); node != null; ) {
	    String nodename = node.getNodeName();
	    if (nodename.equals(CIMXml.CLASS)) {
		ce = XMLParser.getCIMClass(node);
		break;
	    } else if (nodename.equals(CIMXml.INSTANCE) ||
		       nodename.equals(CIMXml.VALUE_NAMEDINSTANCE)) {
		ce = XMLParser.getCIMInstance(node, null);
		break;
	    } else if (nodename.equals(CIMXml.VALUE_OBJECTWITHPATH)) {
		NodeList nl = node.getChildNodes();
                CIMObjectPath tmpOP = null;
		for (int i = 0; i < nl.getLength(); i++) {
		    Node item = nl.item(i);
                    if (CIMXml.INSTANCEPATH.equalsIgnoreCase(item.getNodeName())) {
                        tmpOP =  XMLParser.getCIMObjectPath(item);
                    } else if (CIMXml.CLASS.equalsIgnoreCase(item.getNodeName())) {
			ce = XMLParser.getCIMClass(item, tmpOP);
		    } else if (CIMXml.INSTANCE.equalsIgnoreCase(
						item.getNodeName())) {
			ce = XMLParser.getCIMInstance(item, tmpOP);
		    }
		}
		break;
	    } else if (nodename.equals(CIMXml.QUALIFIER_DECLARATION)) {
		ce = XMLParser.getCIMQualifierType(node);
		break;
	    } else if (nodename.startsWith(CIMXml.PROPERTY)) {
		ce = XMLParser.getCIMProperty(node);
		break;
	    } else if (nodename.startsWith(CIMXml.PARAMETER)) {
		ce = XMLParser.getCIMParameter(node);
		break;
	    } else if (nodename.equals(CIMXml.QUALIFIER)) {
		ce = XMLParser.getCIMQualifier(node);
		break;
	    } else if (nodename.equals(CIMXml.METHOD)) {
		ce = XMLParser.getCIMMethod(node);
		break;
	    } else {
		node = walker.nextNode();
	    }
	}
	return ce;
    }
 
    public String CIMElementToXml(CIMElement ce) {
    	if (ce == null) {
	    return "";
    	}
	String str;
	if (ce instanceof CIMClass) {
	    str =  CIMClassToXml((CIMClass)ce, true);
	} else if (ce instanceof CIMInstance) {
	    str =  CIMInstanceToXml((CIMInstance)ce, true);
	} else if (ce instanceof CIMQualifier) {
	    str =  CIMQualifierToXml((CIMQualifier)ce, true);
	} else if (ce instanceof CIMQualifierType) {
	    str =  CIMQualifierTypeToXml((CIMQualifierType)ce, true);
	} else if (ce instanceof CIMMethod) {
	    str =  CIMMethodToXml((CIMMethod)ce, true);
	} else if (ce instanceof CIMParameter) {
	    str =  CIMParameterToXml((CIMParameter)ce, true);
	} else if (ce instanceof CIMProperty) {
	    str =  CIMPropertyToXml((CIMProperty)ce, true);
	} else {
	    str = "";
	}
	return str;
    }

    public String CIMClassToXml(CIMClass cc) {
	XmlInstance xm = new XmlInstance(true);
	return xm.cimClass(cc);
    }

    public String CIMClassToXml(CIMClass cc, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.cimClass(cc);
    }

    public String CIMClassToXml(CIMClass cc,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, 
			includeClassOrigin, propertyList, showImplied);
	return xm.cimClass(cc);
    }
    public String CIMInstanceToXml(CIMInstance ci) {
	XmlInstance xm = new XmlInstance(true);
	return xm.instance(ci);
    }

    public String CIMInstanceToXml(CIMInstance ci, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.instance(ci);
    }

    public String CIMInstanceToXml(CIMInstance ci,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, 
			includeClassOrigin, propertyList, showImplied);
	return xm.instance(ci);
    }

    public String CIMMethodToXml(CIMMethod cm) {
	XmlInstance xm = new XmlInstance(true);
	return xm.method(cm, "");
    }

    public String CIMMethodToXml(CIMMethod cm, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.method(cm, "");
    }


    public String CIMQualifierTypeToXml(CIMQualifierType ct) {
	XmlInstance xm = new XmlInstance(true);
	return xm.qualifierDeclaration(ct);
    }

    public String CIMQualifierTypeToXml(CIMQualifierType ct,
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.qualifierDeclaration(ct);
    }

    public String CIMObjectPathToXml(CIMObjectPath cop) {
	XmlInstance xm = new XmlInstance(true, true, null, true, 
					true, true, true);
	return xm.objectPath(cop);
    }

    public String CIMObjectPathToXml(CIMObjectPath cop, boolean showImplied) {
	XmlInstance xm = new XmlInstance(true, true, null, showImplied, 
					true, true, true);
	return xm.objectPath(cop);
    }

    public String CIMParameterToXml(CIMParameter cp) {
	XmlInstance xm = new XmlInstance(true);
	return xm.parameter(cp);
    }

    public String CIMParameterToXml(CIMParameter cp, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.parameter(cp);
    }


    public String CIMPropertyToXml(CIMProperty prop) {
	XmlInstance xm = new XmlInstance(true);
	return xm.property(prop, "");
    }

    public String CIMPropertyToXml(CIMProperty prop, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.property(prop, "");
    }

    public String CIMQualifierToXml(CIMQualifier cq) {
	XmlInstance xm = new XmlInstance(true);
	return xm.qualifier(cq);
    }

    public String CIMQualifierToXml(CIMQualifier cq, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.qualifier(cq);
    }


    public String CIMValueToXml(CIMValue cv) {
	XmlInstance xm = new XmlInstance(true);
	return xm.value(cv);
    }

    public String CIMValueToXml(CIMValue cv, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.value(cv);
    }


    public String CIMNameSpaceToXml(CIMNameSpace cns) {
	XmlInstance xm = new XmlInstance(true);
	return xm.localNameSpacePath(cns);
    }

    public String CIMNameSpaceToXml(CIMNameSpace cns, boolean showImplied) {
	XmlInstance xm = new XmlInstance(showImplied);
	return xm.localNameSpacePath(cns);
    }

    public Vector getCIMResponse(Document response)
	throws IOException, CIMException {
	return Response.getCIMResponse(response);
    }

    public Document getXMLResponse(XmlResponse rsp,
					Document request,
					PasswordAuthentication auth)
	throws IOException, SAXException, ParserConfigurationException {
	return Response.getXMLResponse(rsp, request, auth);
    }

    public void getXMLResponse(XmlResponse rsp,
                    Document request,
                    PasswordAuthentication auth,
                    DataOutputStream out)
    throws IOException, SAXException {
    Response.getXMLResponse(rsp, request, auth, out);
    }

    public CIMClass getCIMClass(Node node) 
	{
	return XMLParser.getCIMClass(node);
    }

    public CIMInstance getCIMInstance(Node node) 
	{
	return XMLParser.getCIMInstance(node, null);
    }

    public CIMMethod getCIMMethod(Node node) 
	{
	return XMLParser.getCIMMethod(node);
    }

    public CIMObjectPath getCIMObjectPath(Node node) 
	{
	return XMLParser.getCIMObjectPath(node);
    }

    public CIMParameter getCIMParameter(Node node) 
	{
	return XMLParser.getCIMParameter(node);
    }

    public CIMProperty getCIMProperty(Node node) 
	{
	return XMLParser.getCIMProperty(node);
    }

    public CIMQualifier getCIMQualifier(Node node) 
	{
	return XMLParser.getCIMQualifier(node);
    }

    public CIMQualifierType getCIMQualifierType(Node node)
        {
	return XMLParser.getCIMQualifierType(node);
    }
    public CIMValue getCIMValue(Node node, String type) 
	{
	return XMLParser.getCIMValue(node, type);
    }

    public String getStringValue(Node node) {
	return XMLParser.getStringValue(node);
    }

    public boolean getBooleanValue(Node node) {
	return XMLParser.getBooleanValue(node);
    }

    public String getError(CIMException e, boolean showImplied) {
	return Response.getError(e, showImplied);
    }

    public String createInstanceResult(CIMObjectPath ci, 
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.createInstanceResult(ci, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String getClassResult(CIMClass cc,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.getClassResult(cc, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String getInstanceResult(CIMInstance ci, boolean includeQualifiers,
        boolean includeClassOrigin, String [] propertyList, 
        boolean showImplied) {
            
	return Response.getInstanceResult(ci, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String getPropertyResult(CIMValue cv,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.getPropertyResult(cv, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String getQualifierResult(CIMQualifierType qt,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.getQualifierResult(qt, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String enumerateClassesResult(Enumeration enumClasses,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.enumerateClassesResult(enumClasses, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String enumerateClassNamesResult(Enumeration enumClassNames,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.enumerateClassNamesResult(enumClassNames,
            includeQualifiers, includeClassOrigin, propertyList,
            showImplied);
    }

    public String enumerateInstancesResult(Enumeration enumInstances,
	boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.enumerateInstancesResult(enumInstances, 
            includeQualifiers, includeClassOrigin, propertyList, showImplied);
    }

    public String enumerateInstanceNamesResult(Enumeration enumInstanceNames,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.enumerateInstanceNamesResult(enumInstanceNames,
	   includeQualifiers, includeClassOrigin, propertyList, showImplied);
    }

    public String enumerateQualifiersResult(Enumeration enumQualifiers,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.enumerateQualifiersResult(enumQualifiers,
	   includeQualifiers, includeClassOrigin, propertyList, showImplied);
    }

    public String associatorsResult(Enumeration enumAssoc, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.associatorsResult(enumAssoc, ns, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String associatorNamesResult(Enumeration enumAssocNames, 
        CIMNameSpace ns, boolean includeQualifiers, boolean includeClassOrigin, 
        String [] propertyList, boolean showImplied) {
            
	return Response.associatorNamesResult(enumAssocNames, ns, 
            includeQualifiers, includeClassOrigin, propertyList, showImplied);
    }

    public String referencesResult(Enumeration enumRefs, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin, 
        String [] propertyList, boolean showImplied) {
            
	return Response.referencesResult(enumRefs, ns, includeQualifiers, 
            includeClassOrigin, propertyList, showImplied);
    }

    public String referenceNamesResult(Enumeration enumRefNames,
        CIMNameSpace ns, boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.referenceNamesResult(enumRefNames, ns, 
            includeQualifiers, includeClassOrigin, propertyList, showImplied);
    }

    public String enumerateNamespaceResult(Enumeration enumNamespace,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.enumerateNamespaceResult(enumNamespace,
            includeQualifiers, includeClassOrigin, propertyList, showImplied);
    }

    public String execQueryResult(Enumeration enumQuery,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	return Response.execQueryResult(enumQuery, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    public String invokeArgsMethodResult(Vector v, boolean includeQualifiers,
        boolean includeClassOrigin, String[] propertyList, 
        boolean showImplied) {
            
	return Response.invokeArgsMethodResult(v, includeQualifiers,
            includeClassOrigin, propertyList, showImplied);
    }

    private static void xmlAppendBooleanParamValue(StringBuffer xml,
        XmlInstance xm, String paramName, boolean value) {
            
	xml.append(xm.iParamValue(paramName, value));
    }

    private static void xmlAppendStringsParamValue(StringBuffer xml,
	XmlInstance xm, String paramName, String[] value) {
	if (value != null) {
	    xml.append(xm.iParamValue(paramName, value));
        }
    }

    private static StringBuffer xmlAppendObjectPath(CIMNameSpace currNs,
        CIMObjectPath name, XmlInstance xm) { 

	StringBuffer xml = getXmlStringBuffer(currNs, name, xm);
	xml.append(xm.iParamValue(name));
	return xml;
    }

    private static StringBuffer getXmlStringBuffer(CIMNameSpace currNs,
        CIMObjectPath name, XmlInstance xm) {

	CIMObjectPath absPath = fixAbsObjectPath(currNs, name);
	return new StringBuffer(xm.localNameSpacePath(absPath));
    }

    private Document xmlMsg2xmlDoc(XmlInstance xm, String xml) 
	throws CIMException {
	StringBuffer newXml = new StringBuffer(xm.root(
					xm.message(xml)));	
	try {
	    return xm.pcdata2Xml(newXml.toString());
	} catch (IOException e) {
	    throw new CIMException(XmlInstance.XMLERROR, e);
	} catch (SAXException e) {
	    throw new CIMException(XmlInstance.XMLERROR, e);
	} catch (ParserConfigurationException e) {
	    throw new CIMException(XmlInstance.XMLERROR, e);
	}
    }

    private static CIMObjectPath fixAbsObjectPath(CIMNameSpace nameSpace,
					   CIMObjectPath op) {
	String s = nameSpace.getNameSpace() + "/" + op.getNameSpace();
	CIMObjectPath rtop =
	    new CIMObjectPath(op.getObjectName(), 
				(Vector)op.getKeys().clone()); 
	rtop.setHost(op.getHost());
	rtop.setNameSpace(s);
	return rtop;
    }

    private static String xmlRequest(String method, String xml, 
        XmlInstance xm) {
	return xm.call(method, xml);
    }

    void init() {
	// CIMOperations
    	requestHash.put("CIMAssociatorNamesOp",
				new AssociatorNamesXml());
    	requestHash.put("CIMAssociatorsOp",
				new AssociatorsXml());
    	requestHash.put("CIMCreateClassOp",
				new CreateClassXml());
    	requestHash.put("CIMCreateInstanceOp",
				new CreateInstanceXml());
    	requestHash.put("CIMCreateNameSpaceOp",
				new CreateNameSpaceXml());
    	requestHash.put("CIMCreateQualifierTypeOp",
				new CreateQualifierTypeXml());
    	requestHash.put("CIMDeleteClassOp",
				new DeleteClassXml());
    	requestHash.put("CIMDeleteInstanceOp",
				new DeleteInstanceXml());
    	requestHash.put("CIMDeleteNameSpaceOp",
				new DeleteNameSpaceXml());
    	requestHash.put("CIMDeleteQualifierTypeOp",
				new DeleteQualifierTypeXml());
    	requestHash.put("CIMEnumClassNamesOp",
				new EnumClassNamesXml());
    	requestHash.put("CIMEnumClassOp",
				new EnumClassXml());
    	requestHash.put("CIMEnumInstanceNamesOp",
				new EnumInstanceNamesXml());
    	requestHash.put("CIMEnumInstancesOp",
				new EnumInstancesXml());
    	requestHash.put("CIMEnumNameSpaceOp",
				new EnumNameSpaceXml());
    	requestHash.put("CIMEnumQualifierTypesOp",
				new EnumQualifierTypesXml());
    	requestHash.put("CIMExecQueryOp",
				new ExecQueryXml());
    	requestHash.put("CIMGetClassOp",
				new GetClassXml());
    	requestHash.put("CIMGetInstanceOp",
				new GetInstanceXml());
    	requestHash.put("CIMGetPropertyOp",
				new GetPropertyXml());
    	requestHash.put("CIMGetQualifierTypeOp",
				new GetQualifierTypeXml());
    	requestHash.put("CIMInvokeArgsMethodOp",
				new InvokeArgsMethodXml());
    	requestHash.put("CIMReferenceNamesOp",
				new ReferenceNamesXml());
    	requestHash.put("CIMReferencesOp",
				new ReferencesXml());
    	requestHash.put("CIMSetClassOp",
				new SetClassXml());
    	requestHash.put("CIMSetInstanceOp",
				new SetInstanceXml());
    	requestHash.put("CIMSetPropertyOp",
				new SetPropertyXml());
    	requestHash.put("CIMSetQualifierTypeOp",
				new SetQualifierTypeXml());

	// CIMExports
	exprequestHash.put("CIMExportIndication",
				new ExportIndicationXml());
    }

    interface XmlExpRequest {

	String getRequest(CIMExport cimop, XmlInstance xm);
    }

    private static class ExportIndicationXml implements XmlExpRequest {
	public String getRequest(CIMExport cimOp, XmlInstance xm) {
	    CIMExportIndication obj = (CIMExportIndication)cimOp;
	    return xm.expcall(EXPORTINDICATION, 
				xm.expParamValue(obj.getIndication()));
	}
    }

    interface XmlRequest {

	String getRequest(CIMOperation cimOp, XmlInstance xm);
    }

    private static class AssociatorNamesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMAssociatorNamesOp obj = (CIMAssociatorNamesOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    String assocClass = obj.getAssociationClass();
	    String resultClass = obj.getResultClass();
	    String resultRole = obj.getResultRole();
	    String role = obj.getRole();
	    
	    xml.append(xm.iParamValue(OBJECTNAME, obj.getModelPath()) +
		   (assocClass != null ?
		    xm.iParamValue(ASSOCCLASS,
				   new CIMObjectPath(assocClass)) : "") +
		   (resultClass != null ?
		    xm.iParamValue(RESULTCLASS,
				   new CIMObjectPath(resultClass)) : "") +
		   (role != null ?
		    xm.iParamValue(ROLE, role) : "") +
		   (resultRole != null ?
		    xm.iParamValue(RESULTROLE,
				   resultRole) : ""));
	    return xmlRequest("AssociatorNames", xml.toString(), xm);
	}
    }

    private static class AssociatorsXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMAssociatorsOp obj = (CIMAssociatorsOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    String assocClass = obj.getAssociationClass();
	    String resultClass = obj.getResultClass();
	    String resultRole = obj.getResultRole();
	    String role = obj.getRole();
	    boolean includeQualifiers = obj.isQualifiersIncluded();
	    boolean includeClassOrigin = obj.isClassOriginIncluded();
	    String[] propertyList = obj.getPropertyList();

	    xml.append(xm.iParamValue(OBJECTNAME, obj.getModelPath()) +
		   (assocClass != null ?
		    xm.iParamValue(ASSOCCLASS,
				   new CIMObjectPath(assocClass)) : "") +
		   (resultClass != null ?
		    xm.iParamValue(RESULTCLASS,
				   new CIMObjectPath(resultClass)) : "") +
		   (resultRole != null ?
		    xm.iParamValue(RESULTROLE,
				   resultRole) : "") +
		   (role != null ?
		    xm.iParamValue(ROLE, role) : "") +
		   (includeQualifiers ?
		    xm.iParamValue(INCLUDEQUALIFIERS,
				   includeQualifiers) : "") +
		   (includeClassOrigin ?
		    xm.iParamValue(INCLUDECLASSORIGIN,
				   includeClassOrigin) : "") +
		   (propertyList != null ?
		    xm.iParamValue(PROPERTYLIST,
				   propertyList) : ""));
	    return xmlRequest("Associators", xml.toString(), xm);
	}
    }

    private static class CreateClassXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMCreateClassOp obj = (CIMCreateClassOp)cimOp;

	    // include SuperClass
	    xm.setShowImplied(true);

	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getCIMClass(), "CreateClass"));
	    return xmlRequest("CreateClass", xml.toString(), xm);
	}
    }

    private static class CreateInstanceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMCreateInstanceOp obj = (CIMCreateInstanceOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getCIMInstance()));
	    return xmlRequest("CreateInstance", xml.toString(), xm);
	}
    }

    private static class CreateNameSpaceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMCreateNameSpaceOp obj = (CIMCreateNameSpaceOp)cimOp;
	    MapNamespace mns = new MapNamespace(obj.getNameSpace(),
					obj.getRelativeNameSpace());
	    StringBuffer xml = getXmlStringBuffer(mns.getMappedNs(),
					mns.getMappedOp(), xm);
	    xml.append(xm.iParamValue(mns.getMappedInstance()));
	    return xmlRequest("CreateInstance", xml.toString(), xm);
	}
    }

    private static class CreateQualifierTypeXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMCreateQualifierTypeOp obj = (CIMCreateQualifierTypeOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xm.setShowImplied(true);
	    xml.append(xm.iParamValue(obj.getCIMQualifierType()));
	    return xmlRequest("SetQualifier", xml.toString(), xm);
	}
    }

    private static class DeleteClassXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMDeleteClassOp obj = (CIMDeleteClassOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    return xmlRequest("DeleteClass", xml.toString(), xm);
	}
    }

    private static class DeleteInstanceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMDeleteInstanceOp obj = (CIMDeleteInstanceOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    return xmlRequest("DeleteInstance", xml.toString(), xm);
	}
    }

    private static class DeleteNameSpaceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMDeleteNameSpaceOp obj = (CIMDeleteNameSpaceOp)cimOp;
	    MapNamespace mns = new MapNamespace(obj.getNameSpace(),
					obj.getRelativeNameSpace());
	    StringBuffer xml = xmlAppendObjectPath(mns.getMappedNs(),
					mns.getMappedOp(), xm);
	    return xmlRequest("DeleteInstance", xml.toString(), xm);
	}
    }

    private static class DeleteQualifierTypeXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMDeleteQualifierTypeOp obj = 
				(CIMDeleteQualifierTypeOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(QUALIFIERNAME,
				obj.getModelPath().getObjectName()));
	    return xmlRequest("DeleteQualifier", xml.toString(), xm);
	}
    }

    private static class EnumInstanceNamesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMEnumInstanceNamesOp obj = (CIMEnumInstanceNamesOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    return xmlRequest("EnumerateInstanceNames", xml.toString(), xm);
	}
    }

    private static class EnumClassNamesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMEnumClassNamesOp obj = (CIMEnumClassNamesOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    if (obj.isDeep()) {
		xml.append(xm.iParamValue(DEEPINHERITANCE, true));
	    }
	    return xmlRequest("EnumerateClassNames", xml.toString(), xm);
	}
    }

    private static class EnumClassXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMEnumClassOp obj = (CIMEnumClassOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    if (!obj.isLocalOnly()) {
		xmlAppendBooleanParamValue(xml, xm, LOCALONLY, false);
	    }
	    if (obj.isDeep()) {
		xmlAppendBooleanParamValue(xml, xm, DEEPINHERITANCE, true);
	    }
	    if (!obj.isQualifiersIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDEQUALIFIERS, false);
	    }
	    if (obj.isClassOriginIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDECLASSORIGIN, true);
	    }
	    return xmlRequest("EnumerateClasses", xml.toString(), xm);
	}
    }

    private static class EnumInstancesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMEnumInstancesOp obj = (CIMEnumInstancesOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    if (!obj.isLocalOnly()) {
		xmlAppendBooleanParamValue(xml, xm, LOCALONLY, false);
	    }
	    if (!obj.isDeep()) {
		xmlAppendBooleanParamValue(xml, xm, DEEPINHERITANCE, false);
	    }
	    if (obj.isQualifiersIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDEQUALIFIERS, true);
	    }
	    if (obj.isClassOriginIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDECLASSORIGIN, true);
	    }
	    xmlAppendStringsParamValue(xml, xm, PROPERTYLIST, 
					obj.getPropertyList());
	    return xmlRequest("EnumerateInstances", xml.toString(), xm);
	}
    }

    private static class EnumNameSpaceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMEnumNameSpaceOp obj = (CIMEnumNameSpaceOp)cimOp;
	    CIMObjectPath path = obj.getModelPath(); 
	    path.setObjectName(XmlInstance.CNAME);
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					path, xm);
	    return xmlRequest("EnumerateInstanceNames", xml.toString(), xm);
	}
    }

    private static class EnumQualifierTypesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMEnumQualifierTypesOp obj = (CIMEnumQualifierTypesOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    return xmlRequest("EnumerateQualifiers", xml.toString(), xm);
	}
    }

    private static class ExecQueryXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMExecQueryOp obj = (CIMExecQueryOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(QUERYLANGUAGE, obj.getQueryLanguage()) +
				xm.iParamValue(QUERY, obj.getQuery()));
	    return xmlRequest("ExecQuery", xml.toString(), xm);
	}
    }

    private static class GetClassXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMGetClassOp obj = (CIMGetClassOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    if (!obj.isLocalOnly()) {
		xmlAppendBooleanParamValue(xml, xm, LOCALONLY, false);
	    }
	    if (!obj.isQualifiersIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDEQUALIFIERS, false);
	    }
	    if (obj.isClassOriginIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDECLASSORIGIN, true);
	    }
	    xmlAppendStringsParamValue(xml, xm, PROPERTYLIST, 
					obj.getPropertyList());
	    return xmlRequest("GetClass", xml.toString(), xm);
	}
    }

    private static class GetInstanceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMGetInstanceOp obj = (CIMGetInstanceOp)cimOp;
	    StringBuffer xml = xmlAppendObjectPath(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    if (!obj.isLocalOnly()) {
		xmlAppendBooleanParamValue(xml, xm, LOCALONLY, false);
	    }
	    if (obj.isQualifiersIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDEQUALIFIERS, true);
	    }
	    if (obj.isClassOriginIncluded()) {
		xmlAppendBooleanParamValue(xml, xm, INCLUDECLASSORIGIN, true);
	    }
	    xmlAppendStringsParamValue(xml, xm, PROPERTYLIST,
						obj.getPropertyList());
	    return xmlRequest("GetInstance", xml.toString(), xm);
	}
    }

    private static class GetPropertyXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMGetPropertyOp obj = (CIMGetPropertyOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getModelPath()));
	    xml.append(xm.iParamValue(PROPERTYNAME, obj.getPropertyName()));
	    return xmlRequest("GetProperty", xml.toString(), xm);
	}
    }

    private static class GetQualifierTypeXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMGetQualifierTypeOp obj = (CIMGetQualifierTypeOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(QUALIFIERNAME,
					obj.getModelPath().getObjectName()));
	    return xmlRequest("GetQualifier", xml.toString(), xm);
	}
    }

    private static class InvokeArgsMethodXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMInvokeArgsMethodOp obj = (CIMInvokeArgsMethodOp)cimOp;
	    CIMObjectPath absPath = fixAbsObjectPath(obj.getNameSpace(),
					obj.getModelPath());
	    StringBuffer xml = new StringBuffer(xm.localObjectPath(absPath));
	    CIMArgument[] args = obj.getInArgs();
	    if (args != null && args.length > 0) {
		xml.append(xm.paramValue(args));
	    }
	    return xm.xcall(obj.getMethodName(), xml.toString());
	}
    }

    private static class ReferenceNamesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMReferenceNamesOp obj = (CIMReferenceNamesOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    String resultClass = obj.getResultClass();
	    String role = obj.getRole();

	    xml.append(xm.iParamValue(OBJECTNAME, obj.getModelPath()) +
		    (resultClass != null ? 
			xm.iParamValue(RESULTCLASS, 
				new CIMObjectPath(resultClass)) : "") +
		    (role != null ? 
			xm.iParamValue(ROLE, role) : ""));
	    return xmlRequest("ReferenceNames", xml.toString(), xm);
	}
    }

    private static class ReferencesXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMReferencesOp obj = (CIMReferencesOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    String resultClass = obj.getResultClass();
	    String role = obj.getRole();
	    boolean includeQualifiers = obj.isQualifiersIncluded();
	    boolean includeClassOrigin = obj.isClassOriginIncluded();
	    String[] propertyList = obj.getPropertyList();

    	    xml.append(xm.iParamValue(OBJECTNAME, obj.getModelPath()) +
    		   (resultClass != null ?
    		    xm.iParamValue(RESULTCLASS,
    				   new CIMObjectPath(resultClass)) : "") +
    		   (role != null ?
    		    xm.iParamValue(ROLE, role) : "") +
    		   (includeQualifiers ?
    		    xm.iParamValue(INCLUDEQUALIFIERS,
    				   includeQualifiers) : "") +
    		   (includeClassOrigin ?
    		    xm.iParamValue(INCLUDECLASSORIGIN,
    				   includeClassOrigin) : "") +
    		   (propertyList != null ?
    		    xm.iParamValue(PROPERTYLIST,
    				   propertyList) : ""));
	    return xmlRequest("References", xml.toString(), xm);
	}
    }

    private static class SetClassXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMSetClassOp obj = (CIMSetClassOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getCIMClass(), MODIFYCLASS));
	    return xmlRequest(MODIFYCLASS, xml.toString(), xm);
	}
    }

    private static class SetInstanceXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMSetInstanceOp obj = (CIMSetInstanceOp)cimOp;
	    String[] propertyList = obj.getPropertyList();
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getModelPath(), 
					obj.getCIMInstance()) +
		xm.iParamValue(INCLUDEQUALIFIERS, 
		    obj.isQualifiersIncluded()) +
		(propertyList != null ?
    		    xm.iParamValue(PROPERTYLIST, propertyList) : ""));
	    return xmlRequest("ModifyInstance", xml.toString(), xm);
	}
    }

    private static class SetPropertyXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMSetPropertyOp obj = (CIMSetPropertyOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getModelPath()));
	    xml.append(xm.iParamValue(PROPERTYNAME, obj.getPropertyName()));
	    xml.append(xm.iParamValue(obj.getCIMValue()));
	    return xmlRequest("SetProperty", xml.toString(), xm);
	}
    }

    private static class SetQualifierTypeXml implements XmlRequest {

	public String getRequest(CIMOperation cimOp, XmlInstance xm) {
	    CIMSetQualifierTypeOp obj = (CIMSetQualifierTypeOp)cimOp;
	    StringBuffer xml = getXmlStringBuffer(obj.getNameSpace(),
					obj.getModelPath(), xm);
	    xml.append(xm.iParamValue(obj.getCIMQualifierType()));
	    return xmlRequest("SetQualifier", xml.toString(), xm);
	}
    }

    public Map getXmlRequestHeaders(Document msg) 
    throws CIMException {
	HashMap headers = new HashMap();
	NodeTreeWalker walker = new NodeTreeWalker(msg);
	Node node;
	String nodename = null;

	for (node = walker.getCurrentNode();
	    node != null;
	    node = walker.nextNode()) {
	    nodename = node.getNodeName();
	    if (nodename.equals(CIMXml.CIM)) {

	    } else if (nodename.equals(CIMXml.MESSAGE)) {
		headers.put("CIMProtocolVersion",
		    ((Element)node).getAttribute(
					XmlInstance.PROTOCOLVERSION));
	    } else if (nodename.equals(CIMXml.MULTIREQ)) {
		// multiple request
		headers.put(CIMOPERATION, METHODCALL);
		headers.put(CIMBATCH, CIMBATCH);
		break;
	    } else if (nodename.equals(CIMXml.IMETHODCALL) ||
		      nodename.equals(CIMXml.METHODCALL)) {
		// simple request
		headers.put(CIMOPERATION, METHODCALL);
		headers.put(CIMMETHOD,
			((Element)node).getAttribute(XmlInstance.NAME));
	    } else if (nodename.equals(CIMXml.MULTIEXPREQ)) {
		// multiple export request
		headers.put(CIMEXPORT, METHODREQUEST);
		headers.put(CIMEXPORTBATCH, CIMEXPORTBATCH);
		break;
	    } else if (nodename.equals(CIMXml.EXPMETHODCALL)) {
		// simple export request
		headers.put(CIMEXPORT, METHODREQUEST);
		headers.put(CIMEXPORTMETHOD,
			((Element)node).getAttribute(XmlInstance.NAME));
		break;
	    } else if (nodename.startsWith("LOCAL")) {
		// for simple request only
		headers.put(CIMOBJECT, objectPath(node));
		break;
	    }
	}
	return (Map)headers;
    }

    private String objectPath(Node node) throws CIMException {
	StringBuffer op = new StringBuffer();
	CIMProperty p = null;
	boolean isFirstKey = true;
	boolean isSingleton = false;
	NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode();
	    node != null;
	    node = walker.nextNode()) {
	    String nodename = node.getNodeName();
	    if (nodename.equals(CIMXml.NAMESPACEPATH)||
		nodename.equals(CIMXml.LOCALNAMESPACEPATH)||
		nodename.equals(CIMXml.CLASSPATH)||
		nodename.equals(CIMXml.LOCALCLASSPATH)||
		nodename.equals(CIMXml.INSTANCEPATH)) {
	    } else if (nodename.equals(CIMXml.LOCALINSTANCEPATH)) {
		isSingleton = true;
	    } else if (nodename.equals(CIMXml.NAMESPACE)) {
		if(op.length() > 0){op.append( "/");};
		op.append(((Element)node).getAttribute(XmlInstance.NAME));
	    } else if (nodename.equals(CIMXml.CLASSNAME)) {
		op.append( ":" + ((Element)node).getAttribute(XmlInstance.NAME));
	    } else if (nodename.equals(CIMXml.INSTANCENAME)) {
		op.append( ":" + ((Element)node).getAttribute(CIMXml.CLASSNAME));
	    } else if (nodename.equals(CIMXml.KEYBINDING)) {
		isSingleton = false;
		op.append((isFirstKey ? "." : ","));
		isFirstKey = false;
		op.append(((Element)node).getAttribute(XmlInstance.NAME));
		p = getCIMProperty(node);
	    } else if (nodename.equals(CIMXml.KEYVALUE)) {
		isSingleton = false;
		op.append( "=");
		String type = ((Element)node).getAttribute(
						XmlInstance.VALUETYPE);
		//for fortify
		if (null == p){
			throw new NullPointerException();
		}
		String value = p.getValue().getValue().toString();
		StringBuffer buf = new StringBuffer(value.length());
		for (int i = 0; i < value.length(); i++) {
		    if (value.charAt(i) == '"') {
			buf.append("\\");
		    }
		    buf.append(value.charAt(i));
		}
		value = buf.toString();
		op.append((type.equals("string") ? "\"" + value + "\"" : value));
	    } else if (nodename.equals(CIMXml.VALUE_REFERENCE)) {
		isSingleton = false;
		op.append( "=" + objectPath(walker.nextNode()));
	    }
	}
	op .append((isSingleton ? "=@" : ""));
	String returnStr;
	try {
	    returnStr = URLEncoder.encode(op.toString(), "UTF-8");
	} catch (UnsupportedEncodingException uee) {
	    throw new CIMException(uee.getLocalizedMessage());
	}finally{
        op = null;
    }
	return returnStr;
    }
    
    private static class MapNamespace {

	String nstr = null;
	Vector v = null;

	MapNamespace(CIMNameSpace currNs, CIMNameSpace ns) {
	    
	    CIMNameSpace newns = new CIMNameSpace();
	    newns.setNameSpace(currNs.getNameSpace() 
				+ '/' + ns.getNameSpace());
	    nstr = newns.getNameSpace();
	    v = new Vector();
	    CIMProperty cp = new CIMProperty("NameSpace");
	    cp.setValue(new CIMValue(nstr.substring(
				nstr.lastIndexOf('/') + 1)));
	    v.addElement(cp);
	}

	CIMNameSpace getMappedNs() {
	    CIMNameSpace newns = new CIMNameSpace();
	    newns.setNameSpace(nstr.substring(0,
				nstr.lastIndexOf('/')));    
	    return newns;
	} 

	CIMObjectPath getMappedOp() {
	    return new CIMObjectPath(XmlInstance.CNAME, v);
	}

	CIMInstance getMappedInstance() {
	    CIMInstance ci = new CIMInstance();
	    ci.setClassName(XmlInstance.CNAME);
	    ci.setProperties(v);
	    return ci;
	}
    }
}
