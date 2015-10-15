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
 * This class contains all the constants associated with conditional 
 * relations.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 * @see         javax.wbem.client.CIMClient
 *
 * @see javax.wbem.query.BinaryRelQueryExp
 */
public class Query {

    /**
     * Represents the greater than conditional relation
     */
    public final static int GT	 = 0;

    /**
     * Represents the less than conditional relation
     */
    public final static int LT	 = 1;

    /**
     * Represents the >= conditional relation
     */
    public final static int GE	 = 2;

    /**
     * Represents the <= conditional relation
     */
    public final static int LE	 = 3;

    /**
     * Represents the equals conditional relation
     */
    public final static int EQ	 = 4;

    /**
     * Represents the not equals conditional relation
     */
    public final static int NE	 = 5;

    /**
     * Represents the 'like' conditional relation as defined in the SQL-92
     * standard. Currently only the % matching is supported without support
     * for ESCAPE or _
     */
    public final static int LIKE = 6;

    /**
     * Represents the 'not like' conditional relation as defined in the SQL-92
     * standard. Currently only the % matching is supported without support
     * for ESCAPE or _
     */
    public final static int NLIKE = 7;

    /**
     * Represents the ISA conditional relation for class membership. This is
     * not part of SQL
     */
    public final static int ISA = 8;

    // used internally to evaluate isa for canonical form
    public final static int NISA = 9;
}
