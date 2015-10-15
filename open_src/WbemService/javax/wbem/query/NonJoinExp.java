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



/**
 * The NonJoinExp represents From clauses which have only one table 
 * specified to apply the select operation to. In WQL CIM classes are mapped
 * into tables.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 *
 * @see javax.wbem.cim.CIMClass
 *
 */
public class NonJoinExp extends FromExp {

    QualifiedAttributeExp exp;

    /**
     * Constructor for NonJoinExp.
     * @param exp This is a QualifiedAttributeExp which represents the name
     *            of the appropriate 'table'
     */
    public NonJoinExp(QualifiedAttributeExp exp) {
	this.exp = exp;
    }

    /**
     * Accessor method for the 'table' name
     * @return QualifiedAtributeExp
     */
    public QualifiedAttributeExp getAttribute() {
	return exp;
    }

    public String toString() {
	return exp.toString();
    }
}
