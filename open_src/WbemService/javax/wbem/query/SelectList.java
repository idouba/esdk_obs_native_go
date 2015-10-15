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
package javax.wbem.query;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.Vector;

import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMPropertyException;

/**
 * SelectList represents a list of attributes which are used as the projection
 * for the returned 'row'.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01 
 * @since       WBEM 1.0
 *
 *
 */
public class SelectList implements Serializable {

    private Vector v = new Vector();

    /**
     * Constructor for a single attribute list.
     * @param exp A '*' represents 
     *        projection of all attributes.
     */
    public SelectList(AttributeExp exp) {
	v.addElement(exp);
    }

    /**
     * Adds new attributes to the list.
     * @param exp Attribute to be added.
     */
    public void addElement(AttributeExp exp) {
	v.addElement(exp);
    }

    /**
     * Enumeration of all the attributes.
     * @return Enumeration of all the attributes.j
     */
    public Enumeration elements() {
	return v.elements();
    }

    /**
     * WQL String form of the select list.
     */
    public String toString() {
	StringBuffer b = new StringBuffer();
	Enumeration e = v.elements();
	AttributeExp exp = (AttributeExp)e.nextElement();
	b.append(exp.toString());
	while (e.hasMoreElements()) {
	    exp = (AttributeExp)e.nextElement();
	    b.append(",");
	    b.append(exp.toString());
	}
	return b.toString();
    }

    /**
     * Projects the appropriate 'columns' from the input 'row'. In the case
     * of WQL currently supported rows are 'CIMInstance'. Each column in
     * a CIMInstance property.
     * @return CIMElement resulting from the projection of the attribute list.
     *         Currently only CIMInstance is returned.
     */
    public CIMElement apply(CIMElement ce) throws CIMException {
	Enumeration e = v.elements();
	CIMInstance ci = new CIMInstance();
	CIMInstance inci = (CIMInstance)ce;
        ci.setClassName(inci.getClassName());
        ci.setObjectPath(inci.getObjectPath());
	Vector propVec = new Vector();
	while (e.hasMoreElements()) {
	    String attr = ((AttributeExp)e.nextElement()).getAttributeName();
	    if (attr.equals("*")) {
		return ce;
	    }
	    CIMProperty cp = inci.getProperty(attr);
	    if (cp == null) {
		throw new 
		    CIMPropertyException(CIMException.CIM_ERR_NOT_FOUND, attr);
	    }
	    propVec.addElement(cp);
	}
	ci.setProperties(propVec);
	return ci;
    }
}
