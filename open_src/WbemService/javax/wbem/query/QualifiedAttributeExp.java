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
 *are Copyright � 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package javax.wbem.query;

import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;

/**
 * This class represents represents an attribute qualified by a classname 
 * and/or alias. If the attribute name is not set, this QualifiedAttributeExp
 * merely represents a 'table'. WQL maps tables to classes.
 *
 * A <CODE>QualifiedAttributeExp</CODE> may be
 * used anywhere a <CODE>ValueExp</CODE> is required. 
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 1.0
 *
 * @see ValueExp
 * @see Query
 */
public class QualifiedAttributeExp extends AttributeExp {

    private String className;
    private String attr;
    private String alias;

    /**
     * Creates a new <CODE>QualifiedAttributeExp</CODE> representing the object
     * attribute for class <CODE><className></CODE> named 
     * by <CODE><attr></CODE>. 
     * @param className The class name of the qualified attribute.
     * @param attr      The attribute name which is qualified by the className.
     */
    public QualifiedAttributeExp(String className, String attr) {
	this.className = className;
	this.attr      = attr;
    }

    /**
     * Creates a new <CODE>QualifiedAttributeExp</CODE> representing the object
     * attribute for class <CODE><className></CODE> named 
     * by <CODE><attr></CODE>. 
     * @param className The class name of the qualified attribute.
     * @param alias     An alias for className. Currently aliases are not
     *                  supported, but this constructor is retained for future
     *                  enhancement.
     * @param attr      The attribute name which is qualified by the className.
     */
    public QualifiedAttributeExp(String className, String alias, String attr) {
	this(className, alias);
	this.attr      = attr;
    }

    public QualifiedAttributeExp() {
    }


    /**
     * Method which returns the actual value associated with the attribute
     * in the input 'row'.  Currently this method is not supported.
     * @exception CIMException CIM_ERR_NOT_SUPPORTED is thrown
     */
    public ValueExp apply(CIMElement obj) 
	throws CIMException {
	    
	throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
    }

    /**
     * Returns the SQL syntax string representing its value.
     *
     * @return	The expression's string value in SQL syntax.
     */
    public String toString() {
	if (alias != null) {
	    return alias + "." + attr;
	}
	
	if (className != null) {
	    String table = getAttrClassName();
	    if (attr != null) {
		return table + "." + attr;
	    } else {
		return table;
	    }
	} else {
	    return attr;
	}
    }

    /**
     * Accessor for className.
     */
    public String getAttrClassName() {
	return className;
    }

    /**
     * Accessor for attribute name.
     */
    public String getAttributeName() {
	return attr;
    }

    /**
     * Accessor for alias name.
     */
    public String getAlias() {
	return alias;
    }

}
