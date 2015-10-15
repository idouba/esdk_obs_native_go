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

import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;

/** 
 * This class represents values that can be passed as arguments to
 * SQL relational expressions.  Strings, numbers, and SQL attributes are
 * valid values and are represented respectively by instances of
 * <CODE>StringValueExp</CODE>, <CODE>AttributeExp</CODE>, and
 * <CODE>NumericValue</CODE>, all of which implement <CODE>ValueExp</CODE>.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 *
 * @see javax.wbem.query.StringValueExp
 * @see javax.wbem.query.AttributeExp
 * @see javax.wbem.query.NumericValue
 * @see javax.wbem.query.Query
 */
public abstract class ValueExp implements Serializable {

    /**
     * Constructor for ValueExp
     */
    public ValueExp() {
    }

    /**
     * Returns the value associated with this expression for the input 'row'.
     * In WQL any 'row' is a CIMElement. The only CIMElement supported is
     * CIMInstance. Depending on the subclass the value returned can be
     * a constant or changes according the input CIMElement.
     */
    public ValueExp apply(CIMElement obj)
	throws CIMException {
	return this;
    }
}
