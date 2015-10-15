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

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Enumeration;
import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMFlavor;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMScope;
import javax.wbem.cim.CIMValue;
import javax.wbem.client.adapter.http.DtdResolver;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * This class creates well formed XML documents/elements
 * based on the DMTF CIM DTD.
 *
 * @since       WBEM 1.0
 */
class XmlInstance extends XmlElement {

    final static String ARRAYSIZE = "ARRAYSIZE";
    final static String ASSOCIATION = "ASSOCIATION";
    final static String CIMVERSION = "CIMVERSION";
    private final static String CIMVERSIONSTRING = "2.0";
    final static String CLASSORIGIN = "CLASSORIGIN";
    final static String CODE = "CODE";
    final static String DESCRIPTION = "DESCRIPTION";
    private final static String DTDVERSION = "DTDVERSION";
    private final static String DTDVERSIONSTRING = "2.0";
    final static String EMBEDDEDOBJECT = "EmbeddedObject";
    final static String ID = "ID";
    final static String INDICATION = "INDICATION";
    final static String ISARRAY = "ISARRAY";
    final static String METHOD = "METHOD";
    final static String NAME = "NAME";
    final static String OVERRIDABLE = "OVERRIDABLE";
    final static String PARAMETER = "PARAMETER";
    final static String PROTOCOLVERSION = "PROTOCOLVERSION";
    final static String PROTOCOLVERSIONSTRING = "1.0";
    final static String PROPAGATED = "PROPAGATED";
    final static String PROPERTY = "PROPERTY";
    final static String REFERENCE = "REFERENCE";
    final static String REFERENCECLASS = "REFERENCECLASS";
    final static String SUPERCLASS = "SUPERCLASS";
    final static String TOSUBCLASS = "TOSUBCLASS";
    final static String TRANSLATABLE = "TRANSLATABLE";
    final static String TYPE = "TYPE";
    final static String VALUETYPE = "VALUETYPE";
    final static String PARAMTYPE = "PARAMTYPE";
    final static String XMLERROR = "XMLERROR";
    final static String CNAME = "__Namespace";
    private final static String REFERENCETYPE = "reference";
    final static String TRUE = "true";
    final static String FALSE = "false";
    final static String LOOPBACK_ADDRESS = "127.0.0.1";
    DtdResolver resolver = null;
    private final static String SYSTEMID =
       "http://www.dmtf.org/cim/mapping/xml/v2.0";
    private final static String PUBLICID = "-//DMTF//DTD CIM 2.0//EN";
    private final static String NEWINDICATION = "NewIndication";
    private boolean validate = false;
    boolean sendDoctype =
            (System.getProperty("setdoctype") != null
            ? "true".equalsIgnoreCase(System.getProperty("setdoctype"))
            : false);

    /**
     * This class provides methods for creating well-formed XML
     * instances and fragments.  It uses elements and content
     * models defined by the Distributed Management Task Force.
     */     
    public XmlInstance() {
	super();
    }

    public XmlInstance(boolean showImplied) {
	super(showImplied);
    }

    public XmlInstance(boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied) {
	super(includeQualifiers, includeClassOrigin, 
            propertyList, showImplied);
    }

    public XmlInstance(boolean includeQualifiers,
			boolean includeClassOrigin,
			String [] propertyList,
			boolean showImplied,
			boolean showDefault,
			boolean showHost,
			boolean showNamespace) {
	super(includeQualifiers, includeClassOrigin, propertyList, 
		showImplied, showDefault, showHost, showNamespace);
    }

    Document pcdata2Xml(String pcdata)
	throws SAXException, IOException, ParserConfigurationException {

	InputSource input = new InputSource(new StringReader(pcdata));
	DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	dbf.setNamespaceAware(true);
	dbf.setValidating(validate);
	if (resolver == null) {
	    resolver = new DtdResolver();
	    resolver.registerCatalogEntry(PUBLICID,
                 "javax/wbem/client/adapter/http/cim20.dtd",
                 getClass().getClassLoader());
	}
	DocumentBuilder builder = dbf.newDocumentBuilder();
        builder.setEntityResolver(resolver);
        return builder.parse(input);
    }

    String xml2Pcdata(Document doc) throws IOException {
	// Use a Transformer for output
	TransformerFactory tFactory = TransformerFactory.newInstance();
	Transformer transformer;
	try {
	    transformer = tFactory.newTransformer();
	    DocumentType docType = doc.getDoctype();
	    if (docType != null) {
		String systemId =docType.getSystemId();
		if (systemId != null) {
		String systemValue = (new File(systemId)).getName();
	    	transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemValue);
		}
	    }
	    DOMSource source = new DOMSource(doc);
	    StringWriter sw = new StringWriter();
	    StreamResult result = new StreamResult(sw);
	    transformer.transform(source, result);
	    return sw.toString();
	} catch (TransformerConfigurationException e) {
	    e.printStackTrace();
	} catch (TransformerException e) {
	    e.printStackTrace();
	}
	return null;
    }

    /*
     * <!-- ************************************************** -->
     * <!--                                                    -->
     * <!-- This DTD defines the schema for XML 1.0 Documents  -->
     * <!-- representing CIM Element Declarations or Messages. -->
     * <!--                                                    -->
     * <!--    DTD Version : 2.0                               -->
     * <!--    Date: July 20th 1999                            -->
     * <!-- ************************************************** -->
     * 
     * <!-- ************************************************** -->
     * <!-- Entity Declarations                                -->
     * <!-- ************************************************** -->
     * 
     * <!ENTITY % CIMName         "NAME           CDATA         #REQUIRED">
     */
    void setCIMName(String name) {
	setAttr(NAME, name);
    }

    void setCIMName(CIMElement ce) {
	setCIMName(ce.getName());
    }

    /*
     * 
     * <!ENTITY % CIMType  "TYPE (boolean|string|char16|uint8|sint8|uint16|
     * sint16|uint32|sint32|uint64|sint64|datetime|real32|real64)">
     * 
     */
    private void setCIMType(CIMDataType cdt) {
	if (cdt != null && !cdt.isReferenceType()) {
	    setAttr(TYPE, getCIMType(cdt));
	}
    }

    private String getCIMType(CIMDataType cdt) {
	String type = null;
	if (cdt == null) {
	    return type;
	}
	if (!cdt.isReferenceType()) {
	    type = cdt.toString().toLowerCase();
	    if (type.indexOf("_") > 0) {
		type = type.substring(0, type.indexOf("_"));
	    } else if (type.indexOf("[") > 0) {
		type = type.substring(0, type.indexOf("["));
	    }
	} else {
	    type = REFERENCETYPE;
	}
	return type;
    }
    
    /*
     * 
     * <!ENTITY % ParamType  "PARAMTYPE (boolean|string|char16|uint8|sint8|uint16|
     * sint16|uint32|sint32|uint64|sint64|datetime|real32|real64|reference)">
     * 
     */
    private void setParamType(CIMDataType cdt) {
	setAttr(PARAMTYPE, getCIMType(cdt));
    }


    /*
     * <!ENTITY % QualifierFlavor "OVERRIDABLE    (true|false)  'true'
     *                             TOSUBCLASS     (true|false)  'true'
     *                             TOINSTANCE     (true|false)  'false'
     *                             TRANSLATABLE   (true|false)  'false'">
     */
    private void setQualifierFlavor(CIMQualifier cq) {
	if (cq.hasFlavor(new CIMFlavor(CIMFlavor.DISABLEOVERRIDE))) {
	    setAttr(OVERRIDABLE, FALSE);
	} else if (getShowDefault()) {
	    setAttr(OVERRIDABLE, TRUE);
	}
	if (cq.hasFlavor(new CIMFlavor(CIMFlavor.RESTRICTED))) {
	    setAttr(TOSUBCLASS, FALSE);
	} else if (getShowDefault()) {
	    setAttr(TOSUBCLASS, TRUE);
	}
	if (cq.hasFlavor(new CIMFlavor(CIMFlavor.TRANSLATE))) {
	    setAttr(TRANSLATABLE, TRUE);
	} else if (getShowDefault()) {
	    setAttr(TRANSLATABLE, FALSE);
	}
    }

    /*
     * 
     * <!ENTITY % ClassOrigin     "CLASSORIGIN    CDATA         #IMPLIED">
     */
    private void setClassOrigin(String origin) {
        final boolean haveOrigin = origin != null && origin.length() > 0;        
	if (haveOrigin && (getIncludeClassOrigin() || getShowImplied())) {
	    setAttr(CLASSORIGIN, origin);
	}
    }

    private void setClassOrigin(CIMProperty cp) {
	if (getIncludeClassOrigin() || getShowImplied()) {
	    setClassOrigin(cp.getOriginClass());
	}
    }

    private void setClassOrigin(CIMMethod cm) {
	if (getIncludeClassOrigin() || getShowImplied()) {
	    setClassOrigin(cm.getOriginClass());
	}
    }

    /*
     * 
     * <!ENTITY % Propagated      "PROPAGATED     (true|false)  'false'">
     */

    private void setPropagated(boolean propagated) {
	String s = (propagated ? TRUE : FALSE);
	if (getShowDefault() || propagated) {
	    setAttr(PROPAGATED, s);
	}
    }

    /*
     * "name" is that of the class containing the property
     */
    private void setPropagated(CIMProperty cp, String name) {
	if (cp.isPropagated()) {
	    setPropagated(true);
	} else {
	    setPropagated(false);
	}
    }

    private void setPropagated(CIMQualifier cq) {
	setPropagated(false);
    }

    /*
     *  "name" is that of the class containing the method
     */
    private void setPropagated(CIMMethod cm, String name) {
	if (name.equals(cm.getOriginClass())) {
	    setPropagated(false);
	} else {
	    setPropagated(true);
	}
    }

    /*
     * 
     * <!ENTITY % ArraySize       "ARRAYSIZE      CDATA         #IMPLIED">
     * 
     */
/* TODO: Determine if this is needed
    private void setArraySize(int size) {
	if (getShowImplied()) {
	    setAttr(ARRAYSIZE, String.valueOf(size));
	}
    }
*/
    /*
     * <!ENTITY % SuperClass      "SUPERCLASS     CDATA         #IMPLIED">
     * 
     */

    private void setSuperClass(String name) {
	if (getShowImplied()) {
	    setAttr(SUPERCLASS, name);
	}
    }

    private void setSuperClass(CIMClass cc) {
	if (getShowImplied()) {
	    String name = cc.getSuperClass();
	    if (name.length() != 0) {
		setSuperClass(name);
	    }
	}
    }

    /*
     * <!ENTITY % ClassName       "CLASSNAME      CDATA         #REQUIRED">
     */
    private void setClassName(String name) {
	setAttr(CIMXml.CLASSNAME, name);
    }

    private void setClassName(CIMInstance ci) {
	String name = ci.getClassName();
	if (name.length() != 0) {
	    setClassName(name);
	}
    }

    /*
     * 
     * <!ENTITY % ReferenceClass  "REFERENCECLASS CDATA         #IMPLIED">
     * 
     */
    private void setReferenceClass(String name) {
	if (getShowImplied()) {
	    setAttr(REFERENCECLASS, name);
	}
    }

    private void setReferenceClass(CIMProperty cp) {
	if (getShowImplied()) {
	    setReferenceClass(cp.getType().getRefClassName());
	}
    }

    private void setReferenceClass(CIMParameter cp) {
	if (getShowImplied()) {
	    setReferenceClass(cp.getType().getRefClassName());
	}
    }


    /*
     * 
     * <!-- ************************************************** -->
     * <!-- Root element                                       -->
     * <!--     CIMVERSION must be "2.0"                       -->
     * <!--     DTDVERSION must be "2.0"                       -->
     * <!-- ************************************************** -->
     * 
     * <!ELEMENT CIM (MESSAGE|DECLARATION)>
     * <!ATTLIST CIM 
     *          CIMVERSION CDATA #REQUIRED   
     *          DTDVERSION CDATA #REQUIRED>  
     */
    protected String root(String body) {
	String xmlHeader = "";
	if (sendDoctype) {
	    xmlHeader = "<!DOCTYPE CIM PUBLIC '" + PUBLICID + "' '" + SYSTEMID + "'>\n";
	}
	setAttr(CIMVERSION, CIMVERSIONSTRING);
	setAttr(DTDVERSION, DTDVERSIONSTRING);
	return xmlHeader + stag(CIMXml.CIM) + body + etag();
    }

    /*
     * <!-- ************************************************** -->
     * <!-- Object Value elements                              -->
     * <!-- ************************************************** -->
     * 
     * <!ELEMENT VALUE (#PCDATA)>
     * 
     */
    private String value(String pcdata) {
	return stag(CIMXml.VALUE) + pcdata(pcdata) + etag();
    }

    private String value(String [] pcdata) {
	StringBuffer Xml = new StringBuffer();
	Xml.append(stag(CIMXml.VALUE_ARRAY));
	for (int i = 0; i < pcdata.length; i++) {
	    Xml.append(value(pcdata[i]));
	}
	return Xml.toString() + etag();
    }

    private String value(boolean value) {
	return stag(CIMXml.VALUE) +
	    (value ? "TRUE" : "FALSE") + etag();
    }

    protected String value(CIMValue cv) {
	if ((cv == null) || (cv.getValue() == null)) {	    
            return "";
	}
        if ((cv.getType() != null) && (cv.getType().isArrayType())) {
            if(CIMDataType.REFERENCE_ARRAY == cv.getType().getType()) {
                    return valueRefArray(cv);
            } else {
                return valueArray(cv);
            }
	} else {
	    return stag(CIMXml.VALUE) + pcdata(cv) + etag();
	}
    }

    private String value(Vector values) {
	CIMValue cv;
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < values.size(); i++) {
	    cv = new CIMValue(values.elementAt(i));
	    Xml.append(value(cv));
	}
	return Xml.toString();
    }

    private String value(CIMProperty cp) {
	if (cp.isReference()) {
	    return valueReference(cp.getValue());
	} else if (cp.getValue() != null) {
	    return value(cp.getValue());
	}
	return "";	
    }

    private String quoted(String pcdata) {
	StringBuffer buf = new StringBuffer(pcdata.length());
	for (int i = 0; i < pcdata.length(); i++) {
	    if (pcdata.charAt(i) == '\\' ||
		pcdata.charAt(i) == '"') {
		buf.append("\\");
	    }
	    buf.append(pcdata.charAt(i));
	}
	return buf.toString();
    }

    /**
     *This method checks the string to see if it has
     *any XML reserved characters. ssee the cdata(string)
     *method for the list of reserved chars.
     */
    private boolean hasMarkup(String pcdata) {
	return (pcdata.indexOf("<") != -1 ||
		 pcdata.indexOf("&") != -1 ||
		 pcdata.indexOf("\"") != -1 ||
		 pcdata.indexOf(">") != -1 ||                 
                 pcdata.indexOf("\'") != -1);
    }

    /* 
     *This method will take a pcdata string, replace the XML
     *reserved characters with the XML replacements and return 
     *the new string to be included in the XML Document
     * The reserved chars are
     *      < = &lt;
     *      & = &amp;
     *      " = &quot;
     *      > = &gt;
     *      ' = &apos;
     */
    private String cdata(String pcdata) {
	StringBuffer buf = new StringBuffer(pcdata.length());
	for (int i = 0; i < pcdata.length(); i++) {
	    switch (pcdata.charAt(i)) {
	    case '<':
		buf.append("&lt;");
		break;
	    case '&':
		buf.append("&amp;");
		break;
            case '"':
                buf.append("&quot;");
                break;
            case '>':
                buf.append("&gt;");
                break;
            case '\'':
                buf.append("&apos;");
                break;                    
	    default:
		buf.append(pcdata.charAt(i));
	    }
	}
	return buf.toString();
    }

    /**
     * Takes a string and formats it for XML
     * by changing any reserved characters to 
     * the proper XML format (e.g. &quot;)
     *
     * returns a string ready to be included in a XML document
     */
    private String pcdata(String pcdata) {
	String Xml = pcdata;
	if (hasMarkup(pcdata)) {
	    Xml = cdata(pcdata);
	}
	return Xml;
    }

    private String pcdata(CIMValue cv) {
	if (cv == null) {
	    return "";
	}

	if (cv.getType() != null && cv.getType().isArrayType()) {
	    throw new IllegalArgumentException();
	}

	if (cv.getValue() == null) {
	    return "";
	} else {
	    Object obj = cv.getValue();
	    if (obj instanceof CIMInstance) {
		// embeded CIM instance
		return pcdata(quoted(instance((CIMInstance)obj)));
	    } else if (obj instanceof CIMClass) {
		// embeded CIM class
		return pcdata(quoted(cimClass((CIMClass)obj)));
	    } else {
		return pcdata(obj.toString());
	    }
	}
    }


    /* 
     * <!ELEMENT VALUE.ARRAY (VALUE*)>
     */
    private String valueArray(CIMValue cv) {
	return stag(CIMXml.VALUE_ARRAY) + value((Vector)(cv.getValue())) 
					+ etag();
    }

    /* 
     * 
     * <!ELEMENT VALUE.REFERENCE (CLASSPATH|LOCALCLASSPATH|CLASSNAME|
     *                            INSTANCEPATH|LOCALINSTANCEPATH|INSTANCENAME)>
     */
    private String valueReference(CIMObjectPath cop) {
        String ns = cop.getNameSpace();
        String host = cop.getHost();
        final boolean haveNs = ns != null && ns.length() > 0;
        final boolean haveHost = host != null && host.length() > 0;
        boolean bShowNamespace = getShowNamespace();
        boolean bShowHost = getShowHost();
        if (haveHost) {            
            setShowHost(true);
        } else if (haveNs) {
            setShowNamespace(true);
        }
        String x = stag(CIMXml.VALUE_REFERENCE) + pickObjectName(cop) + etag();
        setShowHost(bShowHost);
        this.setShowNamespace(bShowNamespace);
        return x;
    }

    private String valueReference(CIMValue cv) {
 	CIMObjectPath cop = new CIMObjectPath();
 	if (cv != null) {
 	    Object cvo = cv.getValue();
 	    cop = (CIMObjectPath)cvo;
 	}
 	return valueReference(cop);
    }

    /*
     * 
     * <!ELEMENT VALUE.REFARRAY (VALUE.REFERENCE*)>
     */
    private String valueRefArray(CIMValue cv) {
        StringBuffer sb = new StringBuffer();
        sb.append(stag(CIMXml.VALUE_REFARRAY));
        Vector vCOPs = (Vector)cv.getValue();
        Enumeration eCOPs = vCOPs.elements();
        while(eCOPs.hasMoreElements()){
            CIMObjectPath cop = (CIMObjectPath)eCOPs.nextElement();
            sb.append(valueReference(cop));
        }
        sb.append(etag());
        return sb.toString();
    }
    
    /*
     * 
     * <!ELEMENT VALUE.OBJECT (CLASS|INSTANCE)>
     */
    protected String valueObject(Object obj) {
	return stag(CIMXml.VALUE_OBJECT) + object(obj) + etag();
    }

    /*
     * <!ELEMENT VALUE.NAMEDINSTANCE (INSTANCENAME,INSTANCE)>
     * 
     */
    protected String valueNamedInstance(CIMInstance ci) {
	return stag(CIMXml.VALUE_NAMEDINSTANCE) +
	    instanceName(ci) +
	    instance(ci) + etag();
    }

    private String valueNamedInstance(CIMObjectPath name, CIMInstance ci) {
	return stag(CIMXml.VALUE_NAMEDINSTANCE) + instanceName(name) +
	    instance(ci) + etag();
    }

    /*
     * <!ELEMENT VALUE.NAMEDOBJECT (CLASS|(INSTANCENAME,INSTANCE))>
     */
    /* TODO: Determine if needed
    private String valueNamedObject(Object obj) {
	return stag(CIMXml.VALUE_NAMEDOBJECT) + namedObject(obj) + etag();
    }
    */
    
    /*
     * 
     * <!ELEMENT VALUE.OBJECTWITHLOCALPATH ((LOCALCLASSPATH,CLASS)|
     * (LOCALINSTANCEPATH,INSTANCE))>
     */
    /* TODO: Determine if needed
    private String valueObjectWithLocalPath(Object obj) {
	return stag(CIMXml.VALUE_OBJECTWITHLOCALPATH) +
	    objectWithLocalPath(obj) + etag();
    }
    */
    
    /*
     * 
     * <!ELEMENT VALUE.OBJECTWITHPATH ((CLASSPATH,CLASS)|
     * (INSTANCEPATH,INSTANCE))>
     */
    protected String valueObjectWithPath(Object obj, CIMNameSpace ns) {
	return stag(CIMXml.VALUE_OBJECTWITHPATH) + objectWithPath(obj, ns) + etag();
    }

    /*
     * 
     * 
     * <!-- ************************************************** -->
     * <!-- Object naming and locating elements                -->
     * <!-- ************************************************** -->
     */
    private String pickObjectName(CIMObjectPath cop) {
 	if (getShowHost()) {
 	    return objectPath(cop, false);
	} else if (getShowNamespace()) {
 	    return localObjectPath(cop);
 	} else {
 	    return objectName(cop);
 	}
    }
 
    protected String localObjectPath(CIMObjectPath cop) {
 	Vector keys = cop.getKeys();
 	if (keys.size() > 0) {
 	    return localInstancePath(cop);
 	} else {
 	    return localClassPath(cop);
 	}
    }

    private String objectName(CIMObjectPath cop) {
	Vector keys = cop.getKeys();
	if (keys.size() > 0) {
	    return instanceName(cop);
	} else {
	    return className(cop);
	}
    }

    /*
     * 
     * <!ELEMENT NAMESPACEPATH (HOST,LOCALNAMESPACEPATH)>
     */
    private String nameSpacePath(CIMObjectPath cop) {
        String host = cop.getHost();
        final boolean haveHost = host != null && host.length() > 0;
        if (!haveHost) {
            host = "localhost";
            try {
                host = getLocalIPAddress();
            } catch (Exception e) {
                //ignore - just use localhost
            }
        }
	return stag(CIMXml.NAMESPACEPATH) + host(host) + 
            localNameSpacePath(cop) + etag();
    }
    
    /*
     * 
     * <!ELEMENT LOCALNAMESPACEPATH (NAMESPACE+)>
     */
    private String localNameSpacePath(String ns) {
	StringBuffer Xml = new StringBuffer(stag(CIMXml.LOCALNAMESPACEPATH));
	int i = ns.indexOf('/');
	while (i >= 0) {
	    if (i != 0) {
		Xml.append(nameSpace(ns.substring(0, i)));
	    }
	    ns = ns.substring(i+1);
	    i = ns.indexOf('/');
	}
	return Xml.toString() + nameSpace(ns) + etag();
    }

    protected String localNameSpacePath(CIMNameSpace cns) {
	return localNameSpacePath(cns.getNameSpace());
    }

    protected String localNameSpacePath(CIMObjectPath cop) {
	return localNameSpacePath(cop.getNameSpace());
    }

    /*
     * 
     * <!ELEMENT HOST (#PCDATA)>
     */
    private String host(String name) {
	return stag(CIMXml.HOST) + pcdata(name) + etag();
    }

    /* 
     * 
     * <!ELEMENT NAMESPACE EMPTY>
     * <!ATTLIST NAMESPACE
     *          %CIMName;>
     */
    private String nameSpace(String name) {
	setCIMName(name);
	return otag(CIMXml.NAMESPACE);
    }

    /* 
     * 
     * <!ELEMENT CLASSPATH (NAMESPACEPATH,CLASSNAME)>
     */
    private String classPath(CIMObjectPath cop) {
	return stag(CIMXml.CLASSPATH) + nameSpacePath(cop) +
	    className(cop.getObjectName()) + etag();
    }

    /*
     * 
     * <!ELEMENT LOCALCLASSPATH (LOCALNAMESPACEPATH,CLASSNAME)>
     */
    private String localClassPath(CIMObjectPath cop) {
	return stag(CIMXml.LOCALCLASSPATH) + localNameSpacePath(cop) +
	    className(cop) + etag();
    }

    /*
     * 
     * <!ELEMENT CLASSNAME EMPTY>
     * <!ATTLIST CLASSNAME
     *          %CIMName;>
     */
    protected String className(String name) {
	setCIMName(name);
	return otag(CIMXml.CLASSNAME);
    }

    protected String className(CIMClass cc) {
	return className(cc.getName());
    }

    private String className(CIMObjectPath cop) {
        String name = cop.getObjectName();
        if (name != "") {
            return className(name);
        } else {
            return "";
        }        
    }

    /*
     * 
     * <!ELEMENT INSTANCEPATH (NAMESPACEPATH,INSTANCENAME)>
     */

    private String instancePath(CIMObjectPath cop) {
	return stag(CIMXml.INSTANCEPATH) +  nameSpacePath(cop) +
	    instanceName(cop) + etag();
    }

    /*
     * 
     * <!ELEMENT LOCALINSTANCEPATH (LOCALNAMESPACEPATH,INSTANCENAME)>
     */

    private String localInstancePath(CIMObjectPath cop) {
	return stag(CIMXml.LOCALINSTANCEPATH) + localNameSpacePath(cop) +
	    instanceName(cop) + etag();
    }

    /* 
     * 
     * <!ELEMENT INSTANCENAME (KEYBINDING*|KEYVALUE?|VALUE.REFERENCE?)>
     * <!ATTLIST INSTANCENAME
     *          %ClassName;>
     * 
     */

    private String instanceName(Vector cp, String name) {
	String keybinding = keyBinding(cp);
	setClassName(name);
	return stag(CIMXml.INSTANCENAME) + keybinding + etag();
    }

    protected String instanceName(CIMObjectPath cop) {
	return instanceName(cop.getKeys(), cop.getObjectName());
    }

    protected String instanceName(CIMInstance ci) {
	return instanceName(ci.getKeys(), ci.getClassName());
    }

    /*
     * <!ELEMENT OBJECTPATH (INSTANCEPATH|CLASSPATH)>
     * 
     */

    protected String objectPath(CIMObjectPath cop) {
	return objectPath(cop, true);
    }

    private String objectPath(CIMObjectPath cop, boolean includetype) {
	StringBuffer Xml = new StringBuffer();
	Vector keys = cop.getKeys();
	if (keys.size() > 0) {
	    Xml.append(instancePath(cop));
	} else {
	    Xml.append(classPath(cop));
	}
	if (includetype) {
	    Xml = new StringBuffer(stag(CIMXml.OBJECTPATH) + Xml.toString() 
							   + etag());
	}
	return Xml.toString();
    }

    /*
     * <!ELEMENT KEYBINDING (KEYVALUE|VALUE.REFERENCE)>
     * <!ATTLIST KEYBINDING
     *          %CIMName;>
     */

    String keyBinding(CIMProperty cp) {
	setCIMName(cp);
	StringBuffer Xml = new StringBuffer(stag(CIMXml.KEYBINDING));
	if (cp.isReference()) {
	    Xml.append(valueReference(cp.getValue()));
	} else {
	    Xml.append(keyValue(cp));
	}
	return Xml.toString() + etag();
    }

    /*
     * Takes a vector of CIMProperty *keys*
     */

    String keyBinding(Vector cp) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < cp.size(); i++) {
	    if (cp.elementAt(i) instanceof CIMProperty) {
		CIMProperty p = (CIMProperty)cp.elementAt(i);
		Xml.append(keyBinding(p));
	    }
	}
	return Xml.toString();
    }

    /*
     * 
     * <!ELEMENT KEYVALUE (#PCDATA)>
     * <!ATTLIST KEYVALUE
     *          VALUETYPE    (string|boolean|numeric)  'string'>
     *          CIMTYPE #implied (added v2.2)
     */

    private void setValueType(CIMDataType cdt) {
	String type = getCIMType(cdt);
	if (type == null) {
	    return;
	}
	if (type.equals("string") || type.equals("datetime")
            || type.equals("char16")) {
	    type = "string";
	} else if (type.equals("boolean")) {
	    type = "boolean";
	} else {
	    type = "numeric";
	}
	setAttr(VALUETYPE, type);
    }

    String keyValue(CIMProperty cp) {
        setCIMType(cp.getType());
	setValueType(cp.getType());
	return stag(CIMXml.KEYVALUE) +
	    (cp.getValue() == null ? "" : pcdata(cp.getValue())) +
	    etag();
    }

    /*
     * 
     * <!-- ************************************************** -->
     * <!-- Object definition elements                         -->
     * <!-- ************************************************** -->
     * 
     */

    String object(Object obj) {
	if (obj instanceof CIMClass) {
	    return cimClass((CIMClass)obj);
	} else if (obj instanceof CIMInstance) {
	    return instance((CIMInstance)obj);
	}
	return "";
    }

    String namedObject(Object obj) {
	if (obj instanceof CIMClass) {
	    return cimClass((CIMClass)obj);
	} else if (obj instanceof CIMInstance) {
	    return instanceName((CIMInstance)obj) + instance((CIMInstance)obj);
	}
	return "";
    }

    String objectWithPath(Object obj, CIMNameSpace ns) {
        if (obj instanceof CIMClass) {
            CIMClass cl = (CIMClass) obj;
            if (cl.getObjectPath().getNameSpace() == null
                || cl.getObjectPath().getNameSpace().length() == 0) {
                cl.setObjectPath(
                    new CIMObjectPath(cl.getName(), ns.getNameSpace()));
            }
            return classPath(cl.getObjectPath()) + cimClass(cl);
        } else if (obj instanceof CIMInstance) {
            CIMInstance iop = (CIMInstance) obj;
            if (iop.getObjectPath().getNameSpace() == null
                || iop.getObjectPath().getNameSpace().length() == 0) {
                iop.setObjectPath(
                    new CIMObjectPath(iop.getClassName(), ns.getNameSpace()));
            }
            return instancePath(iop.getObjectPath()) + instance(iop);
        }
        return "";
    }

    String objectWithLocalPath(Object obj) {
	if (obj instanceof CIMClass) {
	    return localClassPath(new CIMObjectPath()) + 
					cimClass((CIMClass)obj);
	} else if (obj instanceof CIMInstance) {
	    return localInstancePath(new CIMObjectPath()) + 
					instance((CIMInstance)obj);
	}
	return "";
    }

    /*
     * <!ELEMENT CLASS (QUALIFIER*,(PROPERTY|PROPERTY.ARRAY|
     * PROPERTY.REFERENCE)*,METHOD*)>
     * <!ATTLIST CLASS
     *          %CIMName;
     *          %SuperClass;>
     * 
     */

    String cimClass(CIMClass cc) {
	setCIMName(cc);
	setSuperClass(cc);
	return stag(CIMXml.CLASS) + qualifier(cc) +
	    property(cc) + method(cc) + etag(CIMXml.CLASS);
    }

    /*
     * <!ELEMENT INSTANCE (QUALIFIER*,(PROPERTY|PROPERTY.ARRAY|
     * PROPERTY.REFERENCE)*) >
     * <!ATTLIST INSTANCE
     *          %ClassName;>
     * 
     */

    protected String instance(CIMInstance ci) {
	setClassName(ci);
	return stag(CIMXml.INSTANCE) + qualifier(ci) +
	    property(ci) + etag();
    }

    /*
     * <!ELEMENT QUALIFIER (VALUE|VALUE.ARRAY)>
     * <!ATTLIST QUALIFIER 
     *          %CIMName;
     *          %CIMType;              #REQUIRED
     *          %Propagated;
     *          %QualifierFlavor;>
     * 
     */

    String qualifier(CIMQualifier q) {
	StringBuffer Xml = new StringBuffer();
	if (getIncludeQualifiers()) {
	    setCIMName(q);
	    if (q.getValue() != null) {
		setCIMType(q.getValue().getType());
	    } else {
		// BUGFIX. EmbeddedObject qualifier needs to have a type attribute.
		CIMDataType cdt = new CIMDataType(CIMDataType.BOOLEAN);
		setCIMType(cdt);
	    }
	    setPropagated(q);
	    setQualifierFlavor(q);
	    Xml = new StringBuffer(stag(CIMXml.QUALIFIER));
	    if (q.getValue() != null) {
		if (q.getValue().getType() != null && q.getValue().getType().isArrayType()) {
		    Xml.append(valueArray(q.getValue()));
		} else {
		    Xml.append(value(q.getValue()));
		}
	    } 
 	    else {
		// if there is no value, don't create the VALUE tag
	 	// Xml.append(stag(VALUE));
	    }
	    Xml.append(etag(CIMXml.QUALIFIER));
	}
	return Xml.toString();
    }

    String qualifier(Vector qualifiers) {
	StringBuffer Xml = new StringBuffer();
	if (getIncludeQualifiers()) {
	    for (int i = 0; i < qualifiers.size(); i++) {
		Xml.append(qualifier((CIMQualifier)qualifiers.elementAt(i)));
	    }
	}
	return Xml.toString();
    }

    String qualifier(CIMClass cc) {
	    return getIncludeQualifiers() ?
		qualifier(cc.getQualifiers()) : "";
    }

    String qualifier(CIMInstance ci) {
	    return "";
    }

    String qualifier(CIMProperty cp) {
	    return getIncludeQualifiers() ?
		qualifier(cp.getQualifiers()) : "";
    }

    String qualifier(CIMMethod cm) {
	    return getIncludeQualifiers() ?
		qualifier(cm.getQualifiers()) : "";
    }

    String qualifier(CIMParameter cp) {
	    return getIncludeQualifiers() ?
		qualifier(cp.getQualifiers()) : "";
    }

    /*
     * <!ELEMENT QUALIFIER.DECLARATION (SCOPE?,(VALUE|VALUE.ARRAY)?)>
     * <!ATTLIST QUALIFIER.DECLARATION 
     *          %CIMName;               
     *          %CIMType;               #REQUIRED
     *          ISARRAY    (true|false) #IMPLIED
     *          %ArraySize;
     *          %QualifierFlavor;>
     */

    String qualifierDeclaration(CIMQualifierType qt) {
	setCIMName(qt.getName());
	setCIMType(qt.getType());
	setAttr(ISARRAY, (qt.isArrayValue() ? TRUE : FALSE));
	setQualifierFlavor(new CIMQualifier("", qt));
	return stag(CIMXml.QUALIFIER_DECLARATION) + scope(qt) +
	    (qt.hasDefaultValue() &&
	    (qt.getDefaultValue().getValue() != null) ?
	    value(qt.getDefaultValue()) : "") +
	    etag();
    }

    /*
     * <!ELEMENT SCOPE EMPTY>
     * <!ATTLIST SCOPE 
     *          CLASS        (true|false)      'false'
     *          ASSOCIATION  (true|false)      'false'
     *          REFERENCE    (true|false)      'false'
     *          PROPERTY     (true|false)      'false'
     *          METHOD       (true|false)      'false'
     *          PARAMETER    (true|false)      'false'
     *          INDICATION   (true|false)      'false'>
     */

    String scope(CIMQualifierType qt) {
	boolean showscope = false;
	if (qt.hasScope(new CIMScope(CIMScope.CLASS))) {
	    setAttr(CIMXml.CLASS, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(CIMXml.CLASS, FALSE);
	    showscope = true;
	}
	if (qt.hasScope(new CIMScope(CIMScope.ASSOCIATION))) {
	    setAttr(ASSOCIATION, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(ASSOCIATION, FALSE);
	    showscope = true;
	}
	if (qt.hasScope(new CIMScope(CIMScope.INDICATION))) {
	    setAttr(INDICATION, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(INDICATION, FALSE);
	    showscope = true;
	}
	if (qt.hasScope(new CIMScope(CIMScope.PROPERTY))) {
	    setAttr(PROPERTY, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(PROPERTY, FALSE);
	    showscope = true;
	}
	if (qt.hasScope(new CIMScope(CIMScope.REFERENCE))) {
	    setAttr(REFERENCE, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(REFERENCE, FALSE);
	    showscope = true;
	}
	if (qt.hasScope(new CIMScope(CIMScope.METHOD))) {
	    setAttr(METHOD, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(METHOD, FALSE);
	    showscope = true;
	}
	if (qt.hasScope(new CIMScope(CIMScope.PARAMETER))) {
	    setAttr(PARAMETER, TRUE);
	    showscope = true;
	} else if (getShowDefault()) {
	    setAttr(PARAMETER, FALSE);
	    showscope = true;
	}
	return showscope ? otag(CIMXml.SCOPE) : "";
    }

    /* 
     * <!ELEMENT PROPERTY (QUALIFIER*,VALUE?)>
     * <!ATTLIST PROPERTY
     *          %CIMName;
     *          %ClassOrigin;
     *          %Propagated;
     *          %CIMType;              #REQUIRED>
     * 
     */

    boolean includeProperty(String property) {
	if (PropertyList == null) {
	    return true;
	}
	if (PropertyList.length == 0) {
	    return false;
	}
	for (int i = 0; i < PropertyList.length; i++) {
	    if (property.equalsIgnoreCase(PropertyList[i])) {
		return true;
	    }
	}
	return false;
    }

    String property(CIMProperty cp, String name) {
	if (cp.getOverridingProperty() != null) {
	    return "";
	}
	if (!includeProperty(cp.getName())) {
	    return "";
	}
	if (cp.getType() != null && cp.getType().isArrayType()) {
	    return propertyArray(cp, name);
	}
	if (cp.isReference()) {
	    return propertyReference(cp, name);
	}
	setCIMName(cp);
	setCIMType(cp.getType());
	setClassOrigin(cp);
	setPropagated(cp, name);


	boolean found = false;
	CIMValue cv;
	Vector v = cp.getQualifiers();
	if ((cv = cp.getValue()) != null) {
	    if (cv.getValue() instanceof CIMInstance) {
		for (int i = 0; i < v.size(); i++) {
		    CIMQualifier q = (CIMQualifier)v.elementAt(i);
		    if (q.getName().equals(EMBEDDEDOBJECT)) {
			found = true;
			break;
		    }
	 	}
		if (!found) {
                    CIMQualifier q = new CIMQualifier(EMBEDDEDOBJECT);
                    q.setValue(CIMValue.TRUE);
	            v.addElement(q);
		}
	    }
	}

	return stag(CIMXml.PROPERTY) +
	    qualifier(v) + value(cp) + etag();
    }

    String property(Vector p, String name) {
	String refs = "", notrefs = "";
	for (int i = 0; i < p.size(); i++) {
	    CIMProperty cp = (CIMProperty)p.elementAt(i);
	    if (cp == null) {
		continue;
	    }
	    if (!includeProperty(cp.getName())) {
		continue;
	    }
	    if (cp.getType() == null) {
		continue;
	    }
	    if (cp.getType().isReferenceType()) {
		refs = refs + propertyReference(cp, name);
	    } else {
		notrefs = notrefs + property(cp, name);
	    }
	}
	return refs + notrefs;
    }

	// BUGFIX. Overriding references did not work properly over http.
    String property(CIMClass cc) {
	return property(cc.getProperties(), cc.getName());
    }

    String property(CIMInstance ci) {
	return property(ci.getProperties(), ci.getClassName());
    }

    /*
     * <!ELEMENT PROPERTY.ARRAY (QUALIFIER*,VALUE.ARRAY?)>
     * <!ATTLIST PROPERTY.ARRAY
     *          %CIMName;
     *          %CIMType;              #REQUIRED
     *          %ArraySize;
     *          %ClassOrigin;
     *          %Propagated;>
     * 
     */

    String propertyArray(CIMProperty cp, String name) {
	StringBuffer Xml = new StringBuffer();
	setCIMName(cp);
	setCIMType(cp.getType());
	setClassOrigin(cp);
	setPropagated(cp, name);
	Xml.append(stag(CIMXml.PROPERTY_ARRAY) +
	    qualifier(cp.getQualifiers()));
	if (cp.getValue() != null) {
	    Xml.append(valueArray(cp.getValue()));
	}
	return Xml.toString() + etag();
    }

    /*
     * <!ELEMENT PROPERTY.REFERENCE (QUALIFIER*,(VALUE.REFERENCE)?)>
     * <!ATTLIST PROPERTY.REFERENCE
     *          %CIMName;
     *          %ReferenceClass;
     *          %ClassOrigin;
     *          %Propagated;>
     * 
     */

    String propertyReference(CIMProperty cp, String name) {    	
	CIMObjectPath cop = new CIMObjectPath(cp.getType().getRefClassName());
	CIMValue cv = cp.getValue();
	if (cv != null) {
	    Object cvo = cv.getValue();
	    cop = (CIMObjectPath)cvo;
	}
	setCIMName(cp);
	setReferenceClass(cp);
	setClassOrigin(cp);
	setPropagated(cp, name);
	return stag(CIMXml.PROPERTY_REFERENCE) + qualifier(cp) + 
		valueReference(cop) + etag();
    }

    /* 
     * <!ELEMENT METHOD (QUALIFIER*,(PARAMETER|PARAMETER.REFERENCE|
     * PARAMETER.ARRAY|PARAMETER.REFARRAY)*)>
     * <!ATTLIST METHOD
     *          %CIMName;
     *          %CIMType;              #IMPLIED
     *          %ClassOrigin;
     *          %Propagated;>
     * 
     */

    String method(CIMMethod cm, String name) {
	setCIMName(cm);
	setCIMType(cm.getType());
	setClassOrigin(cm);
	setPropagated(cm, name);
	return stag(CIMXml.METHOD) + qualifier(cm) +
	    parameter(cm.getParameters()) + etag();
    }

    String method(Vector cms, String name) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < cms.size(); i++) {
	    Xml.append(method((CIMMethod)cms.elementAt(i), name));
	}
	return Xml.toString();
    }

    String method(CIMClass cc) {
	return method(cc.getMethods(), cc.getName());
    }

    /*
     * <!ELEMENT PARAMETER (QUALIFIER*)>
     * <!ATTLIST PARAMETER 
     *          %CIMName;
     *          %CIMType;              #REQUIRED>
     * 
     */

    String parameter(CIMParameter cp) {
	if (cp.getType() != null && cp.getType().isArrayType()) {
	    if (cp.getType().isReferenceType()) {
		return parameterRefArray(cp);
	    } else {
		return parameterArray(cp);
	    }
	}
	if (cp.getType() != null && cp.getType().isReferenceType()) {
	    return parameterReference(cp);
	}
	setCIMName(cp);
	setCIMType(cp.getType());
	return stag(CIMXml.PARAMETER) + qualifier(cp) + etag();
    }

    String parameter(Vector cp) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < cp.size(); i++) {
	    Xml.append(parameter((CIMParameter)cp.elementAt(i)));
	}
	return Xml.toString();
    }

    /*
     * <!ELEMENT PARAMETER.REFERENCE (QUALIFIER*)>
     * <!ATTLIST PARAMETER.REFERENCE
     *          %CIMName;
     *          %ReferenceClass;>
     * 
     */

    String parameterReference(CIMParameter cp) {
	setCIMName(cp);
	setReferenceClass(cp);
	return stag(CIMXml.PARAMETER_REFERENCE) + qualifier(cp) + etag();
    }

    /*
     * <!ELEMENT PARAMETER.ARRAY (QUALIFIER*)>
     * <!ATTLIST PARAMETER.ARRAY 
     *          %CIMName;
     *          %CIMType;              #REQUIRED
     *          %ArraySize;>
     * 		
     */

    String parameterArray(CIMParameter cp) {
	setCIMName(cp);
	setCIMType(cp.getType());
	return stag(CIMXml.PARAMETER_ARRAY) + qualifier(cp) + etag();
    }

    /*
     * <!ELEMENT PARAMETER.REFARRAY (QUALIFIER*)>
     * <!ATTLIST PARAMETER.REFARRAY
     *          %CIMName;
     *          %ReferenceClass;
     *          %ArraySize;>
     * 
     */

    String parameterRefArray(CIMParameter cp) {
	setCIMName(cp);
	setReferenceClass(cp);
	return stag(CIMXml.PARAMETER_REFARRAY) + qualifier(cp) + etag();
    }


    /*
     * <!-- ************************************************** -->
     * <!-- Message elements                                   -->
     * <!-- ************************************************** -->
     */

    /*
     * <!ELEMENT ERROR EMPTY>
     * <!ATTLIST ERROR
     *          CODE        CDATA   #REQUIRED
     *          DESCRIPTION CDATA   #IMPLIED>
     * 
     */
    void setError(int code) {
	setAttr(CODE, String.valueOf(code));
    }

    void setError(int code, String description) {
	setAttr(CODE, String.valueOf(code));
	if (getShowImplied()) {
	    setAttr(DESCRIPTION, description);
	}
    }

    void setId(String cdata) {
	setAttr(ID, cdata);
    }

    void setProtocolVersion() {
	setAttr(PROTOCOLVERSION, PROTOCOLVERSIONSTRING);
    }

    /*
     * 
     * <!ELEMENT MESSAGE (SIMPLEREQ|MULTIREQ|SIMPLERSP|MULTIRSP)>
     * <!ATTLIST MESSAGE
     *          ID              CDATA             #REQUIRED
     *          PROTOCOLVERSION CDATA             #REQUIRED>
     * 
     * <!ELEMENT MULTIREQ (SIMPLEREQ,SIMPLEREQ+)>
     *                    
     * <!ELEMENT SIMPLEREQ (IMETHODCALL|METHODCALL)>
     * 
     */

    String message(String body) {
	setId(getUniqueString());
	setProtocolVersion();
	return stag(CIMXml.MESSAGE) + body + etag();
    }

    String request(String call) {
	return stag(CIMXml.SIMPLEREQ) + call + etag();
    }

    String request(Vector calls) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < calls.size(); i++) {
	    Xml.append(request((String)calls.elementAt(i)));
	}
	if (calls.size() > 1) {
	    Xml = new StringBuffer(stag(CIMXml.MULTIREQ) + Xml.toString() 
							 + etag());
	}
	return Xml.toString();
    }

    String exprequest(String call) {
	return stag(CIMXml.SIMPLEEXPREQ) + call + etag();
    }

    String exprequest(Vector calls) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < calls.size(); i++) {
	    Xml.append(exprequest((String)calls.elementAt(i)));
	}
	if (calls.size() > 1) {
	    Xml = new StringBuffer(stag(CIMXml.MULTIEXPREQ) + Xml.toString() 
							 + etag());
	}
	return Xml.toString();
    }
    /*
     * <!ELEMENT IMETHODCALL (LOCALNAMESPACEPATH,IPARAMVALUE*)>
     * <!ATTLIST IMETHODCALL
     *          %CIMName;>
     */

    String call(String name, String body) {
	setCIMName(name);
	return stag(CIMXml.IMETHODCALL) + body + etag();
    }

    String expcall(String name, String body) {
	setCIMName(name);
	return stag(CIMXml.EXPMETHODCALL) + body + etag();
    }

    /* 
     * <!ELEMENT METHODCALL ((LOCALINSTANCEPATH|LOCALCLASSPATH),PARAMVALUE*)>
     * <!ATTLIST METHODCALL
     *          %CIMName;>
     */

    String xcall(String name, String body) {
	setCIMName(name);
	return stag(CIMXml.METHODCALL) + body + etag();
    }

    /*
     * 
     * <!ELEMENT PARAMVALUE (VALUE|VALUE.REFERENCE|VALUE.ARRAY|VALUE.REFARRAY)?>
     * <!ATTLIST PARAMVALUE
     *          %CIMName;
     *		%ParamType;>
     */

    String paramValue(CIMProperty p) {
	setCIMName(p.getName());
	return appendValue(p.getValue(), p.getType());
    }

    String paramValue(Vector params) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < params.size(); i++) {
	    Xml.append(paramValue((CIMProperty)params.elementAt(i)));
	}
	return Xml.toString();
    }

    protected String paramValue(CIMArgument p) {
	setCIMName(p.getName());
	setParamType(p.getType());
	return appendValue(p.getValue(), p.getType());
    }
    
    private String appendValue(CIMValue cv, CIMDataType type) {
        if (cv == null || cv.getValue() == null) {
            return otag(CIMXml.PARAMVALUE);
        }
        StringBuffer b = new StringBuffer();
        b.append(stag(CIMXml.PARAMVALUE));
        if (type.isArrayType()) {
            b.append(value(cv));
        } else if (type.isReferenceType()) {
            b.append(valueReference(cv));
        } else {
            b.append(value(cv));
        }
        b.append(etag(CIMXml.PARAMVALUE));
        return b.toString();
    }

    protected String paramValue(CIMArgument[] params) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < params.length; i++) {
            if (params[i] != null) {
                Xml.append(paramValue(params[i]));
            }
	}
	return Xml.toString();
    }

    /*
     * <!ELEMENT IPARAMVALUE (VALUE|VALUE.ARRAY|VALUE.REFERENCE
     *                        |INSTANCENAME|CLASSNAME|QUALIFIER.DECLARATION
     * 		       |CLASS|INSTANCE|VALUE.NAMEDINSTANCE)?>
     * <!ATTLIST IPARAMVALUE
     *          %CIMName;>
     * 
     */

    protected String iParamValue(String name, String value) {
	setCIMName(name);
	return stag(CIMXml.IPARAMVALUE) + value(value) + etag();
    }

    protected String iParamValue(String name, String [] value) {
	setCIMName(name);
	return stag(CIMXml.IPARAMVALUE) + value(value) + etag();
    }

    protected String iParamValue(String name, boolean b) {
	setCIMName(name);
	return stag(CIMXml.IPARAMVALUE) + value(b) + etag();
    }

    protected String iParamValue(String name, CIMObjectPath cop) {
	setCIMName(name);
	return stag(CIMXml.IPARAMVALUE) + objectName(cop) + etag();
    }

    protected String iParamValue(CIMObjectPath cop) {
	Vector keys = cop.getKeys();
	if (keys.size() > 0) {
	    setCIMName("InstanceName");
	} else {
	    setCIMName("ClassName");
	}
	return stag(CIMXml.IPARAMVALUE) + objectName(cop) + etag();
    }

    protected String iParamValue(CIMValue cv) {
	setCIMName("NewValue");
	return stag(CIMXml.IPARAMVALUE) + value(cv) + etag();
    }

    protected String iParamValue(CIMClass cc, String call) {
	if (call.equals("CreateClass")) {
	    setCIMName("NewClass");
	} else if (call.equals("ModifyClass")) {
	    setCIMName("ModifiedClass");
	}
	return stag(CIMXml.IPARAMVALUE) + cimClass(cc) + etag();
    }

    protected String iParamValue(CIMInstance ci) {
	setCIMName("NewInstance");
	return stag(CIMXml.IPARAMVALUE) + instance(ci) + etag();
    }

    protected String iParamValue(CIMObjectPath name, CIMInstance ci) {
	setCIMName("ModifiedInstance");
	return stag(CIMXml.IPARAMVALUE) + valueNamedInstance(name, ci) + etag();
    }

    protected String iParamValue(CIMQualifierType qt) {
	setCIMName("QualifierDeclaration");
	return stag(CIMXml.IPARAMVALUE) + qualifierDeclaration(qt) + etag();
    }

    /*
     * <!ELEMENT EXPPARAMVALUE (INSTANCE)?>
     * 
     * <!ATTLIST EXPPARAMVALUE
     * 	   %CIMName;>
     * 
     */ 
    protected String expParamValue(CIMInstance ci) {
	setCIMName(NEWINDICATION);
	return stag(CIMXml.EXPPARAMVALUE) + instance(ci) + etag();
    }

    /* TODO: Determine if needed
    private String expParamValue(CIMObjectPath name, CIMInstance ci) {
	setCIMName(NEWINDICATION);
	return stag(CIMXml.EXPPARAMVALUE) + valueNamedInstance(name, ci) + etag();
    }
    */

    /*
     * <!ELEMENT MULTIRSP (SIMPLERSP,SIMPLERSP+)>
     * 
     * <!ELEMENT SIMPLERSP (METHODRESPONSE|IMETHODRESPONSE)>
     * 
     */
/*
    private String response(String[] responses) {
	StringBuffer Xml = new StringBuffer();
	for (int i = 0; i < responses.length; i++) {
	    Xml.append(responses[i]);
	}
	if (responses.length > 1) {
	    Xml = new StringBuffer(stag(CIMXml.MULTIRSP) + Xml.toString() 
							 + etag());
	}
	setId(getUniqueString());
	setProtocolVersion();
	return stag(CIMXml.MESSAGE) + Xml.toString() + etag();
    }
*/
    /*
     * <!ELEMENT METHODRESPONSE (ERROR|(RETURNVALUE?,PARAMVALUE*))>
     * <!ATTLIST METHODRESPONSE
     *          %CIMName;>
     * 
     * <!ELEMENT IMETHODRESPONSE (ERROR|IRETURNVALUE?)>
     * <!ATTLIST IMETHODRESPONSE
     *          %CIMName;>
     *                    
     */

    /*
     * <!ELEMENT RETURNVALUE (VALUE|VALUE.ARRAY|VALUE.REFERENCE|VALUE.REFARRAY)>
     * <!ATTLIST RETURNVALUE
     *		%ParamType;>
     * 
     */

    protected String returnValue(CIMValue cv) {
	setParamType(cv.getType());
	return stag(CIMXml.RETURNVALUE) + value(cv) + etag();
    }

    /* 
     * <!ELEMENT IRETURNVALUE (CLASSNAME*|INSTANCENAME*|VALUE*
     * |VALUE.OBJECTWITHPATH*|VALUE.OBJECTWITHLOCALPATH*|VALUE.OBJECT*
     * |OBJECTPATH*|QUALIFIER.DECLARATION*|VALUE.ARRAY?|VALUE.REFERENCE?
     * |CLASS*|INSTANCE*|VALUE.NAMEDINSTANCE*)>
     * 
     * 
     */


    // Get the IP address of the local machine. 
    private String getLocalIPAddress() {
        String address = LOOPBACK_ADDRESS;
        try {
            address = InetAddress.getLocalHost().getHostAddress();
            if (address.equals(LOOPBACK_ADDRESS)) {
                // if all we get is loopback address (problem with 
                // getHostAddress() on linux using DHCP) try using 
                // Socket.getLocalAddress().  This could fail if this
                // server cannot find the remote address (possibly no name
                // service)
            	Socket socket = null;
                try {
                	socket = new Socket("www.dmtf.org", 80);
                    InetAddress raw_IP = socket.getLocalAddress();
                    address = raw_IP.getHostAddress();
                } catch (Exception e1) {
                    // ignore exception, just use previous address
                }finally{
                	try{
                		if (null != socket){           		{
                			socket.close();
                		}
                	}
                }
				catch (IOException e){
						 // ignore exception, just use previous address
				}
               }
            }
        } catch (Exception e) {
            // ignore, use loopback
        }
        return address;
    }

}
