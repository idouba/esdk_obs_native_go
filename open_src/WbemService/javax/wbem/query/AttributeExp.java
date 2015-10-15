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

import java.util.Vector;

import javax.wbem.cim.CIMDateTime;
import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMPropertyException;
import javax.wbem.cim.CIMValue;

/**
 * This class represents SQL attributes used as arguments to SQL
 * relational constraints.  An <CODE>AttributeExp</CODE> may be
 * used anywhere a <CODE>ValueExp</CODE> is required. 
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 *
 */
public class AttributeExp extends ValueExp {
 
    private String attr = "";

    /**
     * Creates a new <CODE>AttributeExp</CODE> representing the object
     * attribute named by <VAR>attr</VAR>.
     *
     * @param attr The name of the attribute
     */
    public AttributeExp(String attr) {
	this.attr = attr;
    }

    /**
     * Creates a new <CODE>AttributeExp</CODE> with an empty attribute
     * name
     */
    public AttributeExp() {
    }

    /**
     * Accessor for attribute name.
     */
    public String getAttributeName() {
	return attr;
    }
    
    /**
     * Returns the value associated with this attribute for the input 'row'.
     * In WQL, rows are mapped into CIMElements. The only supported CIMElement
     * is CIMInstance. For CIMInstance, the returned Value is the value
     * of the property with the same name as the attribute name.
     * @param obj input 'row'
     * @return ValueExp associated with the 'column' of the same name in the
     *         input 'row'
     */
    public ValueExp apply(CIMElement obj)
	throws CIMException {

	// In the future, we may be able to handle other elements, not
	// just instances. Havent added __Path handling yet.

	Object result = null;
	CIMInstance ci = (CIMInstance)obj;
	CIMProperty cp = ci.getProperty(attr);
	if (cp == null) {
	    throw new 
		CIMPropertyException(CIMException.CIM_ERR_NOT_FOUND, attr);
	}
	CIMValue cv = cp.getValue();
	if (cv == null) {
	    return null;
	}
	result = cv.getValue();
	if (result == null) {
	    return null;
	}
	return doApply(result);
    }

    private ValueExp doApply(Object result)
	throws CIMException {
	
	if (result instanceof Number) {
	    return new NumericValue((Number)result);
	} 

	if (result instanceof Vector) {
	    return new NumericArrayValue((Vector)result);
	}

	if (result instanceof CIMDateTime) {
	    return new DateTimeExp((CIMDateTime)result);
	}
	
	if (result instanceof String) {
	    return new StringValueExp((String)result);
	} else {
	    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	}
    }

    /**
     * Returns the SQL syntax string representing its value.
     *
     * @return	The expression's string value in SQL syntax.
     */
    public String toString() {
	return attr;
    }

}
