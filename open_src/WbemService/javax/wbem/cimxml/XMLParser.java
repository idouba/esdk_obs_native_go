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

import java.io.ByteArrayInputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMDateTime;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMFlavor;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMMethod;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMParameter;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifier;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMScope;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.UnsignedInt16;
import javax.wbem.cim.UnsignedInt32;
import javax.wbem.cim.UnsignedInt64;
import javax.wbem.cim.UnsignedInt8;
import javax.wbem.client.Debug;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


class XMLParser {
    
    static CIMClass getCIMClass(Node node, CIMObjectPath op)  {
        CIMClass cc = getCIMClass(node);
        if (op != null) {
            cc.setObjectPath(op);
        } 
        return cc;
    }
    
    static CIMClass getCIMClass(Node node)  {
	CIMClass cc = new CIMClass();
        Vector q = new Vector();
        Vector p = new Vector();
        Vector m = new Vector();

        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode(); node != null; ) {
            String nodename = node.getNodeName();
            if (nodename.equals(CIMXml.CLASS)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    String itemname = item.getNodeName();
                    if (itemname.equals(XmlInstance.NAME)) {
                        cc.setName(item.getNodeValue());
                    } else if (itemname.equals(XmlInstance.SUPERCLASS)) {
                        cc.setSuperClass(item.getNodeValue());
                    }
                }
                node = walker.nextNode();
            } else if (nodename.equals(CIMXml.QUALIFIER)) {
                if (node.getParentNode().getNodeName().equals(
                    CIMXml.CLASS)) {
                    CIMQualifier cq = getCIMQualifier(node);
                    q.addElement(cq);
                    if (cq.getName().equalsIgnoreCase(XmlInstance.ASSOCIATION)) {
                        cc.setIsAssociation(true);
                    } else if (cq.getName().equalsIgnoreCase(
                        XmlInstance.INDICATION)) {
                            
                        cc.setIsAssociation(true);
                    }

                }
                node = node.getNextSibling();
            } else if (nodename.startsWith(CIMXml.PROPERTY)) {
                CIMProperty prop = getCIMProperty(node);
                p.addElement(prop);
                if (prop.isKey()) {
                    cc.setIsKeyed(true);
                }
                node = node.getNextSibling();
            } else if (nodename.equals(CIMXml.METHOD)) {
                m.addElement(getCIMMethod(node));
                node = node.getNextSibling();
            } else {
                node = node.getNextSibling();
            }
        }
        if (m.size() > 0) {
            cc.setMethods(m);
        }
        if (q.size() > 0) {
            cc.setQualifiers(q);
        }
        if (p.size() > 0) {
            cc.setProperties(p);
        }

	return cc;
    }

    static CIMInstance getCIMInstance(Node node, CIMObjectPath path)  {
        Vector keys = new Vector();
        CIMInstance ci = new CIMInstance();
        Vector q = new Vector();
        Vector p = new Vector();
        String nodename;

        NodeTreeWalker walker = new NodeTreeWalker(node);
	    node = walker.getCurrentNode();
        if (node.getNodeName().equals(CIMXml.VALUE_NAMEDINSTANCE) ||
            node.getNodeName().equals(CIMXml.VALUE_OBJECTWITHPATH)) {
                    keys = getCIMObjectPath(node).getKeys();            
        }
    	while (node != null && 
    		!node.getNodeName().equals(CIMXml.INSTANCE)) {
    	    node = walker.nextNode();
    	}

        for (node = walker.getCurrentNode(); node != null;) {
            nodename = node.getNodeName();
            if (nodename.equals(CIMXml.INSTANCE)) {
		          ((Element)node).normalize();
                ci.setClassName(((Element)node).getAttribute(
					CIMXml.CLASSNAME));
                node = node.getFirstChild();
            } else if (nodename.equals(CIMXml.QUALIFIER)) {
                q.addElement(getCIMQualifier(node));
                node = node.getNextSibling();
            } else if (nodename.startsWith(CIMXml.PROPERTY)) {
                //We need to see if this is a key. 
                //This will not take up time if it is not a NAMEDINSTANCE.
                //We may be able to spped this up by not looping through for 
                //each property
                CIMProperty cp = getCIMProperty(node);
                Enumeration eKeys = keys.elements();
                while (eKeys.hasMoreElements()) {
                    CIMProperty cpKey = (CIMProperty)eKeys.nextElement();
                    if (cp.getName().equalsIgnoreCase(cpKey.getName())) {
                        cp.setKey(true);
                        cp.setValue(cpKey.getValue());
                    }
                }
                if (path != null) {
                    eKeys = path.getKeys().elements();
                    while (eKeys.hasMoreElements()) {
                        CIMProperty cpKey = (CIMProperty)eKeys.nextElement();
                        if (cp.getName().equalsIgnoreCase(cpKey.getName())) {
                            cp.setKey(true);
                            cp.setValue(cpKey.getValue());
                        }
                    }
                }
                p.addElement(cp);
                node = node.getNextSibling();
            } else {
                node = node.getNextSibling();
            }
        }
        if (p.size() > 0) {
            ci.setProperties(p);
        }
        if (path != null) {
            ci.setObjectPath(path);
        }
        return ci;
    }

    static CIMMethod getCIMMethod(Node node)  {
	CIMMethod cm = new CIMMethod();
        Vector q = new Vector();
        Vector p = new Vector();
        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode(); node != null;
            node = walker.nextNode()) {
            String nodename = node.getNodeName();
            if (nodename.equals(CIMXml.METHOD)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    String itemname = item.getNodeName();
                    if (itemname.equals(XmlInstance.NAME)) {
                        cm.setName(item.getNodeValue());
                    } else if (itemname.equals(XmlInstance.TYPE)) {
			String cimtype = item.getNodeValue();
			try {
			    CIMDataType cdt = CIMDataType.getDataType(cimtype);
			    cm.setType(cdt);
			} catch (CIMException e) {
			    Debug.trace1("Got exception", e);
			}
                    } else if (itemname.equals(XmlInstance.PROPAGATED)) {
                    } else if (itemname.equals(XmlInstance.CLASSORIGIN)) {
                        cm.setOriginClass(item.getNodeValue());
                    }
                }
            } else if (nodename.equals(CIMXml.QUALIFIER)) {
                 if (node.getParentNode().getNodeName().equals(
                    CIMXml.METHOD)) {
                    q.addElement(getCIMQualifier(node));
                 }
                
            } else if (nodename.startsWith(CIMXml.PARAMETER)) {
                p.addElement(getCIMParameter(node));
            }
        }
        if (q.size() > 0) {
            cm.setQualifiers(q);
        }
        if (p.size() > 0) {
            cm.setParameters(p);
        }
	return cm;
    }


    static CIMObjectPath getCIMObjectPath(Node node) 
	 {
	String className = "";
	String nameSpace = "";
	CIMObjectPath cop = new CIMObjectPath();
        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode();
            node != null;
	     /* node = walker.getNext() */) {
                String nodename = node.getNodeName();
                if (nodename.equals(CIMXml.NAMESPACEPATH) ||
                      nodename.equals(CIMXml.LOCALNAMESPACEPATH) ||
                      nodename.equals(CIMXml.CLASSPATH) ||
                      nodename.equals(CIMXml.LOCALCLASSPATH) ||
                      nodename.equals(CIMXml.INSTANCEPATH) ||
                      nodename.equals(CIMXml.LOCALINSTANCEPATH) ||
                      nodename.equals(CIMXml.OBJECTPATH) 
		      /* || nodename.equals(CIMXml.VALUE_REFERENCE) */) {
		} else if (nodename.equals(CIMXml.VALUE_REFERENCE)) {
		    // Remove the reference node, we need to do this
		    // because it was processed by the KEYBINDING tag below
		    node = walker.removeCurrent();
                    //node = walker.getNext();
		    // skip the 'walker.getNext()' call as 'removeCurrent'
		    // does that for us.
		    continue;
            } else if (nodename.equals(CIMXml.HOST)) {
                String host = getTextNode(node);
                if (host == null || host.length() == 0) {
                    continue;
                }
		cop.setHost(host);
            } else if (nodename.equals(CIMXml.NAMESPACE)) {
                //There may be multiple NAMESPACE tags so we build it up.
		nameSpace += "/" + ((Element)node).getAttribute(XmlInstance.NAME);
		cop.setNameSpace(nameSpace);
                node = node.getNextSibling();
	    } else if (nodename.equals(CIMXml.CLASSNAME)) {
		cop.setObjectName(((Element)node).getAttribute(
						XmlInstance.NAME));
            } else if (nodename.equals(CIMXml.INSTANCENAME)) {
		className = ((Element)node).getAttribute(
						CIMXml.CLASSNAME);
		cop.setObjectName(className);
            } else if (node.getNodeName() == CIMXml.KEYBINDING) {
                CIMProperty cp = getCIMProperty(node);
                cop.addKey(cp);

                if (className.equals(XmlInstance.CNAME)) {
                    if (cp.getName().startsWith("Name")) {
                        nameSpace = cp.getValue().getValue().toString();
                    }
                }
		// remove the current node, because it has been processed
		// by the getCIMProperty code
		node = walker.removeCurrent();
                //node=walker.getNext();
		// do a continue so that 'walker.getNext()' is NOT executed
		// the 'walker.removeCurrent()' does this for us
		continue;
            }

		node = walker.nextNode();
        }
	if (className.equals(XmlInstance.CNAME)) {
	    return new CIMObjectPath("", nameSpace);   
	}
	return cop;
    }

    static CIMParameter getCIMParameter(Node node)  {
	CIMParameter cp = new CIMParameter();
        Vector q = new Vector();
        NodeTreeWalker walker = new NodeTreeWalker(node);
        String refclassname = null;
        for (node = walker.getCurrentNode();
            node != null;
            node = walker.nextNode()) {
            String nodename = node.getNodeName();
            if (nodename.startsWith(CIMXml.PARAMETER)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    String itemname = item.getNodeName();
                    if (itemname.equals(XmlInstance.NAME)) {
                        cp.setName(item.getNodeValue());
                    } else if (itemname.equals(XmlInstance.TYPE)) {
                        String cimtype = item.getNodeValue();
                        
			try {
                            cp.setType(CIMDataType.getDataType(cimtype,
                                             nodename.endsWith(".ARRAY")));
			} catch (CIMException e) {
			    Debug.trace1("Got exception", e);
			}
                    } else if (itemname.equals(XmlInstance.ARRAYSIZE)) {
                        cp.setSize(Integer.parseInt(item.getNodeValue()));
                    } else if (itemname.equals(XmlInstance.REFERENCECLASS)) {
                        refclassname = xml2string(item.getNodeValue());
                    }
                }
                if (nodename.endsWith(".REFERENCE")) {
                    cp.setType(new CIMDataType(
				refclassname != null ? refclassname : ""));
                }
                if (nodename.endsWith(".REFARRAY")) {
                    int refSize = cp.getSize(); 
                    if ( refSize <= 0) {
                        refSize = CIMDataType.SIZE_UNLIMITED; 
                    }
                    cp.setType(new CIMDataType(
                        CIMDataType.REFERENCE_ARRAY, refSize));
                }
            } else if (nodename.equals(CIMXml.QUALIFIER)) {
                for (node = walker.getCurrentNode();
                    node != null;
                    node = walker.getNextElement(nodename)) {
                    q.addElement(getCIMQualifier(node));
                }
            } 
        }
        if (q.size() > 0) {
            cp.setQualifiers(q);
        }
	return cp;
    }

    static CIMProperty getCIMProperty(Node node)  {
	CIMProperty cp = new CIMProperty();
	String cimtype = null;
	boolean isEmbeddedObj = false;
	boolean isValueNode = false;
	Object obj = null;
        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode(); node != null;) {
            String nodename = node.getNodeName();
            if (nodename.startsWith(CIMXml.PROPERTY)) {
                NamedNodeMap attrs = node.getAttributes();
                String name = null,
                    origin = null,
                    size = null,
                    propagated = XmlInstance.FALSE,
                    reference = null;
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    String itemname = item.getNodeName();
                    if (itemname.equals(XmlInstance.NAME)) {
                        name = item.getNodeValue();
                    } else if (itemname.equals(XmlInstance.TYPE)) {
                        cimtype = item.getNodeValue();
			try {
			    cp.setType(CIMDataType.getDataType(cimtype,
                                         nodename.endsWith(".ARRAY")));
			} catch (CIMException e) {
			    Debug.trace1("Got exception", e);
			}
                    } else if (itemname.equals(XmlInstance.CLASSORIGIN)) {
                        origin = item.getNodeValue();
                    } else if (itemname.equals(XmlInstance.ARRAYSIZE)) {
                        size = item.getNodeValue();
                    } else if (itemname.equals(XmlInstance.PROPAGATED)) {
                        propagated = item.getNodeValue();
                    } else if (itemname.equals(XmlInstance.REFERENCECLASS)) {
                        reference = item.getNodeValue();
                    }
                }
                cp.setPropagated(propagated.equalsIgnoreCase(XmlInstance.TRUE));
                if (name != null) {
                    cp.setName(name);
                }
                if (origin != null) {
                    cp.setOriginClass(origin);
                }
                if (size != null) {
                    //cp.setSize(Integer.parseInt(size));
                }
                if (reference != null) {
                    cp.setType(new CIMDataType(reference));
                }
                node = walker.nextNode();
            } else if (nodename.equals(CIMXml.QUALIFIER)) {
		try {
		    String q = ((Element)node).getAttribute(
						XmlInstance.NAME);
		    if (q.equals(XmlInstance.EMBEDDEDOBJECT)) {
			isEmbeddedObj = true;
		    }
		    cp.addQualifier(getCIMQualifier(node));
		} catch (CIMException e) {
		    Debug.trace1("Got exception", e);
		}
                node = node.getNextSibling();
            } else if (nodename.equals(CIMXml.KEYBINDING)) {
                if (cp.getName().length() == 0) {
                    cp.setName(((Element)node).getAttribute(
						XmlInstance.NAME));
                }
                node = walker.nextNode();
            } else if (nodename.equals(CIMXml.KEYVALUE)) {
                /*
                 * In Representation of CIM using XML 2.0, the 
                 * specification (Section 3.2.4.13) only allowed a hint 
                 * as to what the data type fo the key value was. In 
                 * version 2.2, it was augmented to allow the true 
                 * datatype to be sent as an optional tag. This means that
                 * we need to support both from the server, making sure
                 * we optimize in the case of the 2.2 specificaton. In the
                 * case of the 2.0 specification, we need to get the meta
                 * data to populate the values using the correct data type.
                 * 
                 * If a server is... 
                 * using the new method we populate it properly now.
                 * using the old method, we set the type to invalid. 
                 * Note: We do not even bother getting the old type as
                 * it is of no value. We always set to invalid since we 
                 * need to look it up anyway.
                 */
                cimtype = ((Element)node).getAttribute(
                    XmlInstance.TYPE);
		if (!cimtype.equals("")) {
                    Object obj2 = valueObject(node, cimtype);
                    CIMDataType cdt = null; 
                    try {
                        cdt = CIMDataType.getDataType(cimtype);
                    } catch (Exception e) {
                        cp.setType(new CIMDataType(CIMDataType.INVALID));
                        cdt = new CIMDataType(CIMDataType.STRING);                  
                    }
                    cp.setValue(new CIMValue(obj2, cdt));
		} else {
                    String value = getStringValue(node);
                    cp.setValue(new CIMValue(value));
                    cp.setType(new CIMDataType(CIMDataType.INVALID));
                }
                cp.setKey(true);
		break;
  
            } else if (nodename.equals(CIMXml.VALUE_REFERENCE)) {
                cp.setValue(getCIMValue(node, ""));
                cp.setType(new CIMDataType(
                    ((CIMObjectPath)cp.getValue().getValue()).getObjectName()));
                break;
	    } else if (nodename.equals(CIMXml.VALUE_ARRAY)) {
		cp.setValue(getCIMValue(node, cimtype));
		break;
            } else if (nodename.equals(CIMXml.VALUE)) {
		isValueNode = true;
		obj = valueObject(node, cimtype);
                break;
            } else {
                node = node.getNextSibling();
            }
        }

	if (isValueNode) {
	    if (isEmbeddedObj) {
		try {
		    obj = getEmbeddedObject((String)obj);
		} catch (CIMException e) {
		    Debug.trace1("Got exception", e);
		}
	    }
	    cp.setValue(new CIMValue(obj));
	}    
	return cp;    
    }

    static Object getEmbeddedObject(String xml) 
	throws CIMException {
	Object o = null;
        Document d = null;

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(true);
	try {
            DocumentBuilder builder = dbf.newDocumentBuilder();
            InputSource in = new InputSource();
	    byte [] buffer = stripQuote(xml).getBytes();
	    in.setByteStream(new ByteArrayInputStream(buffer));
            d = builder.parse(in);            
	} catch (Exception e) {
	    throw new CIMException(XmlInstance.XMLERROR, e);
	}

        NodeTreeWalker walker = new NodeTreeWalker(d);
        for (Node node = walker.getCurrentNode(); node != null; ) {
            String nodename = node.getNodeName();
            if (nodename.equals(CIMXml.CLASS)) {
		o =  getCIMClass(node, null);
		node = node.getNextSibling();
	    } else if (nodename.equals(CIMXml.INSTANCE)) {
		o = getCIMInstance(node, null);
		node = node.getNextSibling();
	    } else {
		node = walker.nextNode();
	    }
	}
	return o;
    }

    static String stripQuote(String pcdata) {
	StringBuffer buf = new StringBuffer(pcdata.length());
	for (int i = 0; i < pcdata.length(); i++) {
	    if (pcdata.charAt(i) == '\\' &&
		(pcdata.charAt(i+1) == '\\' ||
		 pcdata.charAt(i+1) == '"')) {
		buf.append(pcdata.charAt(++i));
		continue;
	    }
	    buf.append(pcdata.charAt(i));
	}
	return buf.toString();
    }

    static CIMQualifier getCIMQualifier(Node node)  {
	CIMQualifier cq = new CIMQualifier();
        String type = null;
        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode(); node != null;) {
            String nodename = node.getNodeName();
            if (nodename.equals(CIMXml.QUALIFIER)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    if (item.getNodeName().equals(XmlInstance.NAME)) {
                        cq.setName(item.getNodeValue());
                    } else if (item.getNodeName().equals(XmlInstance.TYPE)) {
                        type = item.getNodeValue();
                    } else if (item.getNodeName().equals(
					XmlInstance.OVERRIDABLE)) {
                        if (item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
                            cq.addFlavor(new CIMFlavor(
						CIMFlavor.ENABLEOVERRIDE));
                        } else {
                            cq.addFlavor(new CIMFlavor(
						CIMFlavor.DISABLEOVERRIDE));
                        }
                    } else if (item.getNodeName().equals(
					XmlInstance.TOSUBCLASS)) {
                        if (item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
                            cq.addFlavor(new CIMFlavor(CIMFlavor.TOSUBCLASS));
                        } else {
                            cq.addFlavor(new CIMFlavor(CIMFlavor.RESTRICTED));
                        }
                    } else if (item.getNodeName().equals(
					XmlInstance.TRANSLATABLE)) {
                        if (item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
                            cq.addFlavor(new CIMFlavor(CIMFlavor.TRANSLATE));
                        }
                    }
                }
                node = walker.nextNode();
            } else if (nodename.startsWith(CIMXml.VALUE)) {
                cq.setValue(getCIMValue(node, type));
                break;
            } else {
                node = walker.nextNode();
            }
        }
	return cq;
    }

    static CIMQualifierType getCIMQualifierType(Node node)  {
	CIMQualifierType cqt = new CIMQualifierType();
        String cimtype = null;
        boolean isarray = false;
	int qSize = CIMDataType.SIZE_UNLIMITED;
        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode(); node != null;) {
            String nodename = node.getNodeName();
            if (nodename.equals(CIMXml.QUALIFIER_DECLARATION)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    if (item.getNodeName().equals(XmlInstance.NAME)) {
                        cqt.setName(item.getNodeValue());
                    } else if (item.getNodeName().equals(
					XmlInstance.TYPE)) {
                        cimtype = item.getNodeValue();
                    } else if (item.getNodeName().equals(
					XmlInstance.ISARRAY)) {
                        isarray = item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE);
                    } else if (item.getNodeName().equals(
					XmlInstance.ARRAYSIZE)) {
			isarray = true;
                        qSize = Integer.parseInt(item.getNodeValue());
                    } else if (item.getNodeName().equals(
					XmlInstance.OVERRIDABLE)) {
                        if (item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
			    cqt.addFlavor(new CIMFlavor(
						CIMFlavor.ENABLEOVERRIDE));
                        } else {
                            cqt.addFlavor(new CIMFlavor(
						CIMFlavor.DISABLEOVERRIDE));
                        }
                    } else if (item.getNodeName().equals(
					XmlInstance.TOSUBCLASS)) {
                        if (item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
                            cqt.addFlavor(new CIMFlavor(CIMFlavor.TOSUBCLASS));
                        } else {
                            cqt.addFlavor(new CIMFlavor(CIMFlavor.RESTRICTED));
                        }
                    } else if (item.getNodeName().equals(
					XmlInstance.TRANSLATABLE)) {
                        if (item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
                            cqt.addFlavor(new CIMFlavor(CIMFlavor.TRANSLATE));
                        }
                    }
                }
		try {
                    CIMDataType cdt = CIMDataType.getDataType(cimtype, isarray);
		    if (isarray) {
			cdt = new CIMDataType(cdt.getType(), qSize);
	  	    }
                    cqt.setType(cdt);
		} catch (CIMException e) {
		    Debug.trace1("Got exception", e);
		}
                node = walker.nextNode();
            } else if (nodename.startsWith(CIMXml.SCOPE)) {
                NamedNodeMap attrs = node.getAttributes();
                for (int i = 0; i < attrs.getLength(); i++) {
                    Node item = attrs.item(i);
                    if (item.getNodeName().equals(CIMXml.CLASS) &&
                        	item.getNodeValue().equalsIgnoreCase(
						XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.CLASS));
                    } else if (item.getNodeName().equals(
				XmlInstance.ASSOCIATION) &&
				item.getNodeValue().equalsIgnoreCase(
					XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.ASSOCIATION));
                    } else if (item.getNodeName().equals(
				XmlInstance.REFERENCE) &&
				item.getNodeValue().equalsIgnoreCase(
					XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.REFERENCE));
                    } else if (item.getNodeName().equals(
				XmlInstance.PROPERTY) &&
				item.getNodeValue().equalsIgnoreCase(
					XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.PROPERTY));
                    } else if (item.getNodeName().equals(
				XmlInstance.METHOD) &&
				item.getNodeValue().equalsIgnoreCase(
					XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.METHOD));
                    } else if (item.getNodeName().equals(
				XmlInstance.PARAMETER) &&
				item.getNodeValue().equalsIgnoreCase(
					XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.PARAMETER));
                    } else if (item.getNodeName().equals( 
				XmlInstance.INDICATION) &&
				item.getNodeValue().equalsIgnoreCase(
					XmlInstance.TRUE)) {
                        cqt.addScope(new CIMScope(CIMScope.INDICATION));
                    }
                }
                node = walker.nextNode();
            } else if (nodename.startsWith(CIMXml.VALUE)) {
                cqt.setDefaultValue(getCIMValue(node, cimtype));
                break;
            } else {
                node = walker.nextNode();
            }
        }
	return cqt;
    }

    static CIMValue getCIMValue(Node node, String type)  {
	Object o = null;
        NodeTreeWalker walker = new NodeTreeWalker(node);

        for (node = walker.getCurrentNode(); node != null;) {
            String nodename = node.getNodeName();
            if (nodename.equals(CIMXml.KEYVALUE)) {
                o = valueObject(node,
                                ((Element)node).getAttribute(
					XmlInstance.VALUETYPE));
                break;
            } else if (nodename.equals(CIMXml.VALUE)) {
                o = valueObject(node, type);
                break;
            } else if (nodename.equals(CIMXml.VALUE_ARRAY)) {
                Vector v = new Vector();
                for (node = walker.getNextElement(CIMXml.VALUE);
                     node != null; ) {
                    v.addElement(valueObject(node, type));
                    node = walker.getNextElement(CIMXml.VALUE);
                }
                o = v;
                break;
            } else if (nodename.equals(CIMXml.VALUE_REFERENCE)) {
                o = getCIMObjectPath(getFirstChildElement(node));
                break;
            } else {
                node = walker.nextNode();
            }
        }
        return new CIMValue(o);
    }

    private static Object valueObject(Node node, String type)  {
        Object o = null;
        String value = getStringValue(node);

	if (value == null || type == null) {
            return value;
        }
        type = type.length() > 0 ? type : "string";
        int radix = 10;
        if ((type.startsWith("sint") &&
            (value.startsWith("0x") ||
            value.startsWith("+0x") ||
            value.startsWith("-0x") ||
            value.startsWith("0X") ||
            value.startsWith("+0X") ||
            value.startsWith("-0X"))) ||
            (type.startsWith("uint") &&
            (value.startsWith("0x") ||
            value.startsWith("0X")))) {
            radix = 16;
            int dot = (value.indexOf("x") > 0 ?
                       value.indexOf("x") :
                       value.indexOf("X")) + 1;
            value = (value.startsWith("-") ?
                     "-" + value.substring(dot) :
                     value.substring(dot));
        }
        if (type.equals("boolean")) {
            o = Boolean.valueOf(value);
        } else if (type.equals("char16")) {
            o = new Character(value.charAt(0));
        } else if (type.equals("datetime")) {
            o = new CIMDateTime(value);
        } else if (type.equals("real32")) {
            o = new Float(value);
        } else if (type.equals("real64")) {
            o = new Double(value);
        } else if (type.equals("sint16")) {
            o = Short.valueOf(value, radix);
        } else if (type.equals("sint32")) {
            o = Integer.valueOf(value, radix);
        } else if (type.equals("sint64")) {
            o = Long.valueOf(value, radix);
        } else if (type.equals("sint8")) {
            o = Byte.valueOf(value, radix);
        } else if (type.equals("string")) {
            o = value;
        } else if (type.equals("reference")) {
            o = new CIMObjectPath(value);
        } else if (type.equals("uint16")) {
            o = new UnsignedInt16((Integer.valueOf(value, radix)).intValue());
        } else if (type.equals("uint32")) {
            o = new UnsignedInt32((Long.valueOf(value, radix)).longValue());
        } else if (type.equals("uint64")) {
            o = new UnsignedInt64(new java.math.BigInteger(value, radix));
        } else if (type.equals("uint8")) {
            o = new UnsignedInt8((Short.valueOf(value, radix)).shortValue());
        } else if (value.indexOf("e") > 0 ||
                  value.indexOf("E") > 0) {
            o =  new Double(value);
        } else if (value.startsWith("+") ||
                  value.startsWith("-")) {
            o = new Long(value);
        }
        return o;
    }

    static boolean getBooleanValue(Node node) {
        NodeTreeWalker walker = new NodeTreeWalker(node);
        node = walker.getNextElement(CIMXml.VALUE);
        return getTextNode(node).equalsIgnoreCase(XmlInstance.TRUE);
    }

    private static Node getFirstChildElement(Node node) {
        if (node.hasChildNodes()) {
            NodeList nl = node.getChildNodes();
            for (int i = 0; i < nl.getLength(); i++) {
                Node item = nl.item(i);
                if (item.getNodeType() == Node.ELEMENT_NODE) {
                    return item;
                }
            }
        }
        return null;
    }

    static String getStringValue(Node node) {
        String nodename = node.getNodeName();
        if (nodename.endsWith(CIMXml.PARAMVALUE)) {
            node = getFirstChildElement(node);
        }
        return getTextNode(node);
    }

    private static String getTextNode(Node node) {
        NodeTreeWalker walker = new NodeTreeWalker(node);
        for (node = walker.getCurrentNode();
            node != null;
            node = walker.nextNode()) {
                if (node != null && (node.getNodeType() == Node.TEXT_NODE ||
		    node.getNodeType() == Node.CDATA_SECTION_NODE)) {
                    //JDD 03-10-03
                    //The TextNode.java from crimson does not
                    //handle the &quot; or &apos;
                    return xml2string(node.getNodeValue());
                }
        }

        // <PJA> 9-November-2002
        // Server can return <VALUE /> as a synonymous alternative to <VALUE></VALUE>.  The latter has a text node
        // containing "", the former has no text node.  Therefore treat the absence of a text node as the same as
        // the presence of a single text node containing an empty string.
        // If CIM requires transmitting a null value, the protocol requires that this be encoded with NO <VALUE>
        // element, rather than a <VALUE/> element with no content.
        return "";
    }
    
    /*
     *This method takes in an XML cdata string and replaces
     *any XML markup with the proper character.
     */
    static String xml2string(String pcdata) {
        pcdata.replaceAll("&lt;", "<");
        pcdata.replaceAll("&amp;", "&");
        pcdata.replaceAll("&quot;", "\"");
        pcdata.replaceAll("&gt;", ">");
        pcdata.replaceAll("&apos;", "\'");
        return pcdata;
    }

}
