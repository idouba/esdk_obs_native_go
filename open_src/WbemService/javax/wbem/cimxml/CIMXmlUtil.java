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

package javax.wbem.cimxml;

import java.io.IOException;

import javax.wbem.cim.CIMElement;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

/**
 * This interface defines methods to convert CIMObject to CIM-XML
 * representation, and vice versa.
 *
 * @author      Sun Microsystems, Inc.
 * @version     1.0 01/17/02
 * @since       WBEM 2.5
 */
public interface CIMXmlUtil {

    /**
     * Returns an XML representation of this CIM Element.
     *
     * @param obj      a CIM Element
     */
    public String CIMElementToXml(CIMElement obj);

    /**
     * Construct a Java object representing a CIM Element
     * from the provided Xml string.
     * 
     * @param str      a XML string
     * @exception SAXException if any parse errors occurs.
     * @exception IOException if any IO errors occurs.
     */
    public CIMElement getCIMElement(String str)
	throws SAXException, IOException, ParserConfigurationException;
}
