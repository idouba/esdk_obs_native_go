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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.PasswordAuthentication;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * @since       WBEM 1.0
 */
class Response {

    static Vector getCIMResponse(Document response) 
	throws IOException, CIMException {
	Vector responses = new Vector();
	NodeTreeWalker walker = new NodeTreeWalker(response);
	for (Node node = walker.getCurrentNode(); node != null; ) {
	    String nodename = node.getNodeName();
	    if (nodename.endsWith(CIMXml.SIMPLERSP)) {
		responses.addElement(getSimpleCIMResponse(node));
		node = walker.getNextElement(nodename);
	    } else {
		node = walker.nextNode();
	    }
	}

	return responses;
    }

    private static Hashtable getSimpleCIMResponse(Node response) 
	throws IOException, CIMException {
	XMLParser parser = new XMLParser();
	Vector v = new Vector();
	Hashtable simple = new Hashtable(1);
	String method = null;
	String cimtype = null;
	boolean isinvokemethod = false;
	NodeTreeWalker walker = new NodeTreeWalker(response);
	for (Node node = walker.getCurrentNode(); node != null; ) {
	    String nodename = node.getNodeName();
	    if (nodename.endsWith(CIMXml.METHODRESPONSE)) {
		if (nodename.equals(CIMXml.METHODRESPONSE)) {
		    isinvokemethod = true;
		}
		method = ((Element)node).getAttribute(XmlInstance.NAME);
		node = walker.nextNode();
                //WSI Bug#126: The following 3 lines set the cimtype
            } else if (nodename.equals(CIMXml.RETURNVALUE)) {
                cimtype = ((Element)node).getAttribute(XmlInstance.PARAMTYPE);
                node = walker.nextNode();
	    } else if (nodename.equals(CIMXml.ERROR)) {
		int code = Integer.parseInt(((Element)node).getAttribute(
					    XmlInstance.CODE));
                String desc = ((Element)node).getAttribute(XmlInstance.DESCRIPTION);
		CIMException e = new CIMException();
		e = new CIMException(e.getXmlCode(code));
		e.setDescription(desc);
		v.addElement(e);
		node = walker.nextNode();
	    } else if (nodename.equals(CIMXml.CLASS)) {
		v.addElement(XMLParser.getCIMClass(node));
		node = walker.getNextElement(nodename);
	    } else if (nodename.equals(CIMXml.INSTANCE) ||
		       nodename.equals(CIMXml.VALUE_NAMEDINSTANCE)) {
		v.addElement(XMLParser.getCIMInstance(node, null));
		node = walker.getNextElement(nodename);
	    } else if (nodename.equals(CIMXml.CLASSNAME) ||
                nodename.equals(CIMXml.INSTANCENAME) ||
                nodename.equals(CIMXml.INSTANCEPATH) ||
                nodename.equals(CIMXml.OBJECTPATH)) {
                   
                v.addElement(XMLParser.getCIMObjectPath(node));
		node = walker.getNextElement(nodename);
	    } else if (nodename.equals(CIMXml.VALUE)) {
		v.addElement(XMLParser.getCIMValue(node, cimtype));
		node = walker.nextNode();
	    } else if (nodename.equals(CIMXml.VALUE_OBJECTWITHPATH)) {
		NodeList nl = node.getChildNodes();
                CIMObjectPath instPath = new CIMObjectPath();
		for (int i = 0; i < nl.getLength(); i++) {                    
		    Node item = nl.item(i);
		    String itemnodename = item.getNodeName();
                    if (itemnodename.equals(CIMXml.INSTANCEPATH)) {
                        instPath = XMLParser.getCIMObjectPath(node);
                    } else if (itemnodename.equals(CIMXml.CLASS)) {
			v.addElement(XMLParser.getCIMClass(item, instPath));
		    } else if (itemnodename.equals(CIMXml.INSTANCE)) {
			v.addElement(XMLParser.getCIMInstance(item, instPath));
                    }
	        }
		node = walker.getNextElement(nodename);
	    } else if (nodename.equals(CIMXml.QUALIFIER_DECLARATION)) {
		v.addElement(XMLParser.getCIMQualifierType(node));
		node = walker.getNextElement(nodename);
	    } else if (nodename.equals(CIMXml.VALUE_ARRAY) ||
		       nodename.equals(CIMXml.VALUE_REFERENCE)) {
		v.addElement(XMLParser.getCIMValue(node, cimtype));
		break;
	    } else if (nodename.equals(CIMXml.PARAMVALUE)) {
		v.addElement(getParamValue(node, parser));
		node = walker.getNextElement(nodename);
	    } else {
		node = walker.nextNode();
	    }
	}

	if (isinvokemethod) {
	    Vector v2 = new Vector();
	    v2.addElement(v);
	    simple.put(method, v2);
	} else {
	    simple.put(method, v);
	}
	return simple;
    }

    boolean isMultiRsp(Document response) {
	NodeTreeWalker walker = new NodeTreeWalker(response);
	for (Node node = walker.getCurrentNode(); node != null; ) {
	    if (node.getNodeName().equals(CIMXml.MULTIRSP)) {
		return true;
	    }
	}
	return false;
    }

    static Document getXMLResponse(XmlResponse rsp,
					Document request,
					PasswordAuthentication auth)
    	throws IOException, SAXException, ParserConfigurationException
    {
    	ByteArrayOutputStream buf = new ByteArrayOutputStream();
    	Writer out = new OutputStreamWriter(buf, "UTF8");
        XmlInstance xi = processRequest(rsp, request, auth, out);
        return xi.pcdata2Xml(buf.toString("UTF8"));
    }

    static void getXMLResponse(XmlResponse rsp,
                    Document request,
                    PasswordAuthentication auth,
                    DataOutputStream out)
        throws IOException, SAXException
    {
        Writer outw = new OutputStreamWriter(out, "UTF8");
        outw.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        outw.flush();
        processRequest(rsp, request, auth, outw);
    }

    private static XmlInstance processRequest(XmlResponse rsp,
                    Document request,
                    PasswordAuthentication auth,
                    Writer out)
        throws IOException, SAXException
    {
	XmlInstance xi = new XmlInstance();
	NodeTreeWalker walker = new NodeTreeWalker(request);

	String roottag = CIMXml.CIM;
	String nodename = null;
	String callname = null;
	String version = null;
	NamedNodeMap attrs;

	for (Node node = walker.getCurrentNode();
	    node != null;
	    node = walker.nextNode()) {
	    nodename = node.getNodeName();
	    if (nodename.equals(roottag) ||
		nodename.equals(CIMXml.MESSAGE)) {
		attrs = node.getAttributes();
		if (attrs != null) {
		    for (int i = 0; i < attrs.getLength(); i++) {
			Node item = attrs.item(i);
			xi.setAttr(item.getNodeName(), item.getNodeValue());
			if (item.getNodeName().equals(XmlInstance.CIMVERSION)) {
			    version = item.getNodeValue();
			}
		    }
		}
		out.write(xi.stag(nodename));
	    } else if (nodename.equals(CIMXml.MULTIREQ)) {
		out.write(xi.stag(CIMXml.MULTIRSP));
	    } else if (nodename.equals(CIMXml.SIMPLEREQ)) {
		out.write(xi.stag(CIMXml.SIMPLERSP));
	    } else if (nodename.equals(CIMXml.IMETHODCALL) ||
		      nodename.equals(CIMXml.METHODCALL) ||
		      nodename.equals(CIMXml.EXPMETHODCALL)) {
		attrs = node.getAttributes();
		if (attrs != null) {
		    for (int i = 0; i < attrs.getLength(); i++) {
			Node item = attrs.item(i);
			xi.setAttr(item.getNodeName(), item.getNodeValue());
			callname = item.getNodeName().equals(XmlInstance.NAME) ?
			    item.getNodeValue() :
			    callname;
		    }
		}
		String methodtype = nodename.substring(0,
				     nodename.indexOf("CALL"));
		out.write(xi.stag(methodtype + "RESPONSE"));

		if (nodename.equals(CIMXml.EXPMETHODCALL)) {
		    out.write(xi.etag(CIMXml.SIMPLEEXPRSP));
		} else {
		    if (callname != null) {
		    	
		    	//修改coverity forward null
		    	if(null == rsp)
		    	{
		    		throw new NullPointerException();
		    	}
		    	
			out.write(rsp.getSimpleXMLResponse(methodtype, 
				callname, node, auth, version));
		    }
		    out.write(xi.etag(CIMXml.SIMPLERSP));
		}
	    } else if (nodename.equals(CIMXml.MULTIEXPREQ)) {
		out.write(xi.stag(CIMXml.MULTIEXPRSP));
	    } else if (nodename.equals(CIMXml.SIMPLEEXPREQ)) {
		out.write(xi.stag(CIMXml.SIMPLEEXPRSP));
	    }
	}
	out.write(xi.etag(roottag));
	out.flush();
    return xi;
    }

    static String getError(CIMException e, boolean showImplied) {
	XmlInstance xm = new XmlInstance();
	xm.setShowImplied(showImplied);
	if (e.isXmlCode(e.getID())) {
	    xm.setError(e.getXmlCode(e.getID()), e.getDescription());
	} else {
	    xm.setError(1, e.getDescription());
	}
	return xm.otag(CIMXml.ERROR);
    }

    static String createInstanceResult(CIMObjectPath ci,
			boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	return xm.stag(CIMXml.IRETURNVALUE) + 
		xm.instanceName(ci) + xm.etag();
    }

    static String getClassResult(CIMClass cc, boolean includeQualifiers,
	boolean includeClassOrigin, String [] propertyList, 
					boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	return xm.stag(CIMXml.IRETURNVALUE) + xm.cimClass(cc) + xm.etag();
    }

    static String getInstanceResult(CIMInstance ci, boolean includeQualifiers,
	boolean includeClassOrigin, String [] propertyList, 
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	return xm.stag(CIMXml.IRETURNVALUE) + xm.instance(ci) + xm.etag();
    }

    static String getPropertyResult(CIMValue cv, boolean includeQualifiers,
	boolean includeClassOrigin, String [] propertyList, 
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	return xm.stag(CIMXml.IRETURNVALUE) + xm.value(cv) + xm.etag();
    }

    static String enumerateClassesResult(Enumeration enumClasses, boolean includeQualifiers,
	boolean includeClassOrigin, String [] propertyList, 
			boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumClasses.hasMoreElements()) {
	    buf.append(xm.cimClass((CIMClass)(enumClasses.nextElement())));
	}
	buf.append(xm.etag(CIMXml.IRETURNVALUE));
	return buf.toString();
    }

    static String enumerateClassNamesResult(Enumeration enumClassNames,
	boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied) {
		
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumClassNames.hasMoreElements()) {
	    buf.append(xm.className(((CIMObjectPath)(
            enumClassNames.nextElement())).getObjectName()));
	}
	buf.append(xm.etag(CIMXml.IRETURNVALUE));
	return buf.toString();
    } 

    static String enumerateInstancesResult(Enumeration enumInstances,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumInstances.hasMoreElements()) {
	    buf.append(xm.valueNamedInstance(
		(CIMInstance)(enumInstances.nextElement())));
	}
	buf.append(xm.etag(CIMXml.IRETURNVALUE));
	return buf.toString();
    }

    static String enumerateInstanceNamesResult(Enumeration enumInstanceNames,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
            
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumInstanceNames.hasMoreElements()) {
	    buf.append(xm.instanceName((CIMObjectPath)(
            enumInstanceNames.nextElement())));
	}
	buf.append(xm.etag(CIMXml.IRETURNVALUE));
	return buf.toString();
    } 

    static String associatorsResult(Enumeration enumAssoc, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {

	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumAssoc.hasMoreElements()) {
	    buf.append(xm.valueObjectWithPath(enumAssoc.nextElement(), ns));
	}
	buf.append(xm.etag());
	return buf.toString();
    }

    static String associatorNamesResult(Enumeration enumAssocNames, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied) {
            
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumAssocNames.hasMoreElements()) {
            buf.append(xm.objectPath((CIMObjectPath)enumAssocNames.nextElement()));
	}
	buf.append(xm.etag());
	return buf.toString();
    }

    static String referencesResult(Enumeration enumRefs, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied) {
            
	return associatorsResult(enumRefs, ns, includeQualifiers, 
            includeClassOrigin, propertyList, showImplied);
    }

    static String referenceNamesResult(Enumeration enumRefNames, CIMNameSpace ns,
        boolean includeQualifiers, boolean includeClassOrigin, 
        String [] propertyList, boolean showImplied) {
            
	return associatorNamesResult(enumRefNames, ns, includeQualifiers, 
			includeClassOrigin, propertyList, showImplied);
    }

    static String enumerateNamespaceResult(Enumeration enumNamespace,
	boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied) {

	String KEY = "key";
	String NAMESPACE = "NameSpace";
        String NAME = "Name";
	String LOCALNAMESPACEPATH = "LocalNameSpacePath";

	CIMClass ns_class = new CIMClass(XmlInstance.CNAME);
	CIMProperty p = new CIMProperty(LOCALNAMESPACEPATH);        
	try {
	    p.addQualifier(new CIMQualifier(KEY));
	} catch (CIMException e) {
	    //e.printStackTrace();
	}
	ns_class.addProperty(p);
	p = new CIMProperty(NAMESPACE);
	try {
	    p.addQualifier(new CIMQualifier(KEY));
	} catch (CIMException e) {
	    //e.printStackTrace();
	}
	ns_class.addProperty(p);
	p = new CIMProperty(NAME);
	ns_class.addProperty(p);
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumNamespace.hasMoreElements()) {
	    String ns_name = ((CIMObjectPath)enumNamespace.nextElement()).getNameSpace();
	    if (ns_name != null && ns_name.length() > 0) {
			CIMInstance ns_instance = ns_class.newInstance();
			try {
				ns_instance.setProperty(LOCALNAMESPACEPATH,
					new CIMValue("", new CIMDataType(CIMDataType.STRING)));
				ns_instance.setProperty(NAMESPACE,
					new CIMValue(ns_name, 
					new CIMDataType(CIMDataType.STRING)));
				ns_instance.setProperty(NAME,
					new CIMValue(ns_name, 
					new CIMDataType(CIMDataType.STRING)));
				buf.append(xm.instanceName(ns_instance));
			}catch (CIMException ce) {
				//printStackTrace()				
			}
		}
	}
	buf.append(xm.etag());
	return buf.toString();
    }

    static String getQualifierResult(CIMQualifierType qt,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	return xm.stag(CIMXml.IRETURNVALUE) + 
			xm.qualifierDeclaration(qt) + xm.etag();
    }

    static String enumerateQualifiersResult(Enumeration enumQualifiers,
        boolean includeQualifiers, boolean includeClassOrigin,
        String [] propertyList, boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumQualifiers.hasMoreElements()) {
	    buf.append(xm.qualifierDeclaration(
            (CIMQualifierType)(enumQualifiers.nextElement())));
	}
	buf.append(xm.etag(CIMXml.IRETURNVALUE));
	return buf.toString();
    }

    static String execQueryResult(Enumeration enumQuery,
        boolean includeQualifiers, boolean includeClassOrigin,
	String [] propertyList, boolean showImplied) {
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
					propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	buf.append(xm.stag(CIMXml.IRETURNVALUE));
	while (enumQuery.hasMoreElements()) {
            //XXX: Should we do a valuObjectWithPath ??
            buf.append(xm.valueObject(enumQuery.nextElement()));
	}
	buf.append(xm.etag(CIMXml.IRETURNVALUE));
	return buf.toString();
    }

    static String invokeArgsMethodResult(Vector v, boolean includeQualifiers,
        boolean includeClassOrigin, String [] propertyList, 
        boolean showImplied) {
            
	XmlInstance xm = new XmlInstance(includeQualifiers, includeClassOrigin,
            propertyList, showImplied);
	StringBuffer buf = new StringBuffer();
	CIMValue cv = (CIMValue)v.elementAt(0);
	buf.append(xm.returnValue(cv));

	for (int i = 1; i < v.size(); i++) {
	    CIMArgument ca = (CIMArgument)v.elementAt(i);
            if (ca != null) {
                buf.append(xm.paramValue(ca));
            }
	}
	return buf.toString();
    }

   private static Object getParamValue(Node node, XMLParser xp) {
	String type = ((Element)node).getAttribute("PARAMTYPE");
	String name = ((Element)node).getAttribute("NAME");

	if (name == null ||
	    name.equals("string") ||
	    name.equals("char16") ||
	    name.equals("datetime") ||
	    name.equals("boolean") ||
	    name.equals("uint8") ||
	    name.equals("uint16") ||
	    name.equals("sint16") ||
	    name.equals("uint32") ||
	    name.equals("sint32") ||
	    name.equals("uint64") ||
	    name.equals("sint64") ||
	    name.equals("real32") ||
	    name.equals("real64") ||
	    name.equals("reference")) {

	    // This was a hack: store the param type in name attr
	    return XMLParser.getCIMValue(node, name);
	} else {

	    // dmtf cr 710.001 : add ParamType
	    return new CIMArgument(name, XMLParser.getCIMValue(node, type));
	}
    }
}
