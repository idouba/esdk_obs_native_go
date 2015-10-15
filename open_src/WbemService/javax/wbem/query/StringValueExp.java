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
 * This class represents strings that are arguments to SQL
 * relational constraints.  A <CODE>StringValueExp</CODE>
 * may be used anywhere a <CODE>ValueExp</CODE> is required.
 * Instances of <CODE>StringValueExp</CODE> print using the
 * SQL syntax for string literals.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 *
 */

public class StringValueExp extends ValueExp {

    private String val = "";

    /**
     * Creates a new <CODE>StringValueExp</CODE> representing the
     * string literal <VAR>val</VAR>.
     */
    public StringValueExp(String val) {
	this.val = val;
    }

    /**
     * Creates a new <CODE>StringValueExp</CODE> representing the
     * string literal "".
     */
    public StringValueExp() {
    }

    /**
     * Accessor for the actual String value.
     */
    public String getValue() {
	return val;
    }

    /**
     * Returns the SQL syntax string representing its value.
     *
     *@return   The expression's string value in SQL syntax.
     */
    public String toString() {
	StringBuffer t = new StringBuffer(); // target string

	// convert each ' to '' and surround with '
	t.append("'");
	int i = 0;		// index of sequence of non-quotes
	int j;			// index of '
	while (-1 != (j = val.indexOf("'", i))) {
	    t.append(val.substring(i, j)).append("''");
 	    i = j+1;
	}
	t.append(val.substring(i));
	t.append("'");
	return t.toString();
    }

}
