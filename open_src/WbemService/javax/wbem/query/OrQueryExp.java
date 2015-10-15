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

import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;

/**
 * It is used by the query building mechanism to represent
 * disjunctions of relational expressions.<P>
 * <pre>
 *     query1 || query2
 * </pre>
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 */
public class OrQueryExp extends QueryExp {

    private QueryExp expOne;
    private QueryExp expTwo;

    /**
     * Constructor with two QueryExp operands.
     */
    public OrQueryExp(QueryExp q1, QueryExp q2) {
	expOne = q1;
	expTwo = q2;
    }

    /**
     * Empty constructor
     */
    public OrQueryExp() {
    }

    /**
     * Accessor for left expression
     */
    public QueryExp getLeftExp() {
	return expOne;
    }
    
    /**
     * Accessor for right expression
     */
    public QueryExp getRightExp() {
	return expTwo;
    }

    /**
     * return boolean true if the either operand evaluates to true, false
     *        otherwise.
     */
    public boolean apply(CIMElement obj)
	throws CIMException {
	return expOne.apply(obj) || expTwo.apply(obj);
    }

    /**
     * Returns the WQL string form.
     */
    public String toString() {
	return "(" + expOne + ") or (" + expTwo + ")";
    }

}
