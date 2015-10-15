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
import java.util.List;

import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;

/**
 * The QueryExp class is an abstract class whose subclasses represent
 * conditional expressions which return a boolean value when a particular
 * 'row' is applied to them.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 *
 */
public abstract class QueryExp implements Serializable {

    /**
     * Constructor.
     */
    public QueryExp() {
    }

    /**
     * When a 'row' is applied to this method it returns true or false depending
     * on whether the row meets the conditions or not.
     * 
     * @param obj The 'row' which is being applied. The row in the case of
     *            WQL can be any CIMElement. Currently the only valid CIMElement
     *            is CIMInstance.
     * @return boolean true if the 'row' meets the conditions, 'false' otherwise
     *            In the case of QueryExp it is always true.
     *       
     * @see javax.wbem.cim.CIMInstance
     */
    public boolean apply(CIMElement obj) 
	throws CIMException {
	return true;
    }

    /**
     * Canonize the expression into a Disjunction of Conjunctions form.
     * (OR of ANDed comparison expressions). This enables handling of 
     * the expression as a List of Lists rather than a tree form, enabling
     * ease of evaluation.
     *
     * @return a List of Lists. The sub-lists each contain comparison 
     * expressions (presently BinaryRelQueryExp) and represent the logical AND 
     * of these expressions. The parent List represents the logical OR of each
     * of its sub-lists.
     */
    public List canonizeDOC() {
	return Canonize.doc(this);
    }

    /**
     * Canonize the expression into a Conjunction of Disjunctions form.
     * (AND of ORed comparison expressions). This enables handling of 
     * the expression as a List of Lists rather than a tree form, enabling
     * ease of evaluation.
     *
     * @return a List of Lists. The sub-lists each contain comparison 
     * expressions (presently BinaryRelQueryExp) and represent the logical OR 
     * of these expressions. The parent List represents the logical AND of each
     * of its sub-lists.
     */
    public List canonizeCOD() {
	return Canonize.cod(this);
    }

}
