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
 * Represents a NotQuery. Negation of relational expressions.
 * <pre>
 *     ! query
 * </pre>
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 *
 */
public class NotQueryExp extends QueryExp {

    private QueryExp exp;

    /**
     * Empty constructor
     */
    public NotQueryExp() {
    }

    /**
     * Constructs a not query of the input QueryExp.
     * @param q is the QueryExp to be negated.
     */
    public NotQueryExp(QueryExp q) {
	exp = q;
    }

    /**
     * Accessor for the expression to be negated.
     */
    public QueryExp getNegatedExp() {
	return exp;
    }

    /**
     * Applies the expression to the input 'row'
     * @return true if the the expression satisfied, false otherwise.
     */
    public boolean apply(CIMElement obj)
	throws CIMException {
	return exp.apply(obj) == false;
    }

    /**
     * Returns the WQL string form.
     */
    public String toString() {
	return "not (" + exp + ")";
    }

}
