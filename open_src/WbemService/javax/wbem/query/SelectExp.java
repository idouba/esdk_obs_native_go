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
 *Contributor(s): WBEM Solutions, Inc.
 */

package javax.wbem.query;


import java.io.ByteArrayInputStream;

/**
 * This class represents all 'select' clauses that are constructed in the 
 * WQL.
 * <pre>
 *     select qualifiedAttributeExp where queryExp
 * </pre>
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 1.0
 * @see         javax.wbem.client.CIMClient
 * 
 */
public class SelectExp extends WQLExp {

    private SelectList attList;
    private QueryExp whereClause;
    private FromExp  fromExp;

    /**
     * Constructor for select clause. It parses the input WQL query and returns
     * a select expression.
     * @param query WQL select query string
     * @exception IllegalArgumentException if the input query is not well
     * formed. The exception message contains string details of the specific 
     * parsing exception.
     *
     */
    public SelectExp(String query) {
        WQLParser parser = new WQLParser(
                    new ByteArrayInputStream(query.getBytes()));
        SelectExp exp = null;
        try {
            exp = (SelectExp)parser.querySpecification();
        } catch (Exception e) {
            throw new IllegalArgumentException(e.toString());
        }
	attList = exp.attList;
	whereClause = exp.whereClause;
	fromExp = exp.fromExp;
    }

    /** 
     * Constructor for select clauses. Used by the WQL parser to construct
     * select clauses.
     *
     * @param attList List of attributes which are projected from the returned
     *                rows.
     * @param fromExp The from clause which specifies which 'table' the
     *                selection is made from. In the WQL mapping, classes
     *                are mapped into tables, instances into rows and properties
     *                into columns.
     * 
     */
    public SelectExp(SelectList attList, FromExp fromExp) {
	this.attList = attList;
	this.fromExp = fromExp;
    }

    /** 
     * Constructor for select clauses. Used by the WQL parser to construct
     * select clauses.
     *
     * @param attList List of attributes which are projected from the returned
     *                rows.
     * @param fromExp The from clause which specifies which 'table' the
     *                selection is made from. In the WQL mapping, classes
     *                are mapped into tables, instances into rows and properties
     *                into columns.
     * @param whereClause The where clause contains conditional expressions
     *                    which evaluate to true or false for a given row.
     *                    Those rows which evaluate to true are selected.
     * 
     */
    public SelectExp(SelectList attList, FromExp fromExp, 
				QueryExp whereClause) 
    {
	this.attList	 = attList;
	this.fromExp = fromExp;
	this.whereClause = whereClause;
    }

    public SelectExp() {
    }

    /**
     * Returns the selectList.
     * @return SelectList.
     */
    public SelectList getSelectList() {
	return attList;
    }
    
    /**
     * Returns the where clause.
     * @return whereClause.
     */
    public QueryExp getWhereClause() {
	return whereClause;
    }

    
    /**
     * Returns the from clause.
     * @return fromClause.
     */
    public FromExp getFromClause() {
	return fromExp;
    }

    public String toString() {
      	StringBuffer s = new StringBuffer();
      	s.append("select ").append(attList).append(" from ")
	  .append(fromExp);
      	if (whereClause != null) {
	    s.append(" where ").append(whereClause);
	}
      	return s.toString();
    }

}
