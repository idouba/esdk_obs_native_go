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

import java.math.BigDecimal;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Vector;

import javax.wbem.cim.CIMDateTime;
import javax.wbem.cim.CIMElement;
import javax.wbem.cim.CIMException;
import javax.wbem.client.Debug;


/**
 *
 * Class representing binary relations between non-boolean operands.
 *
 * <pre>
 *     v1 relop v2, where
 *
 *     relop is <, >, <=, >=, ==, like, not like
 * </pre>
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 */
public class BinaryRelQueryExp extends QueryExp {

    private int      relOp;
    private ValueExp expOne;
    private ValueExp expTwo;

    /**
     * Constructor for binary relations.
     * @param op Represents the relation between operands.
     * @param v1  The left valueExp. It is assumed that the left value is
     *            always a attribute.
     * @param v2  The right valueExp. It is assumed that the right value is
     *            a constant value expression like StringValueExp.
     */
    public BinaryRelQueryExp(int op, ValueExp v1, ValueExp v2) {
	relOp = op;
	expOne  = v1;
	expTwo  = v2;
    }

    /**
     * Empty constructor.
     */
    public BinaryRelQueryExp() {
    }

    /**
     * Accessor for the relational operation
     */
    public int getOperator() {
	return relOp;
    }
    
    /**
     * Accessor for left value
     */
    public ValueExp getLeftValue() {
	return expOne;
    }
    
    /**
     * Accessor for right value
     */
    public ValueExp getRightValue() {
	return expTwo;
    }
    
    /**
     * Returns true if the 'columns' of the input 'row' satisfy the binary
     * relation. The only supported input element is a CIMInstance. This method
     * extracts the attribute in CIMInstance associated with the leftValue
     * attribute and applies the relation operation with the rightValue
     * 
     * @param obj the input row. Only CIMInstance is supported.
     * @return true if the input element satisfy the binary relation false
     *         otherwise.
     */
    public boolean apply(CIMElement obj)
	throws CIMException {
	return doApply(expOne.apply(obj), expTwo.apply(obj));
    }

    private boolean doApply(Object val1, Object val2) 
	throws CIMException {

	String sval1;
	String sval2;

	boolean numeric = val1 instanceof NumericValue;

	if ((val1 == null) || (val2 == null)) {
	    return false;
	}

	if (numeric) {
	    BigDecimal bdval1 = new BigDecimal(val1.toString());
	    BigDecimal bdval2 = new BigDecimal(val2.toString());
	    switch (relOp) {
		case Query.GT:
		    return (bdval1.compareTo(bdval2) > 0);
		case Query.LT:
		    return (bdval1.compareTo(bdval2) < 0);
		case Query.GE:
		    return (bdval1.compareTo(bdval2) >= 0);
		case Query.LE:
		    return (bdval1.compareTo(bdval2) <= 0);
		case Query.EQ:
		    return (bdval1.compareTo(bdval2) == 0);
		case Query.NE:
		    return (bdval1.compareTo(bdval2) != 0);
	    }
	}

	if (val1 instanceof NumericArrayValue && 
	    expOne instanceof AttributeExp &&  expTwo instanceof NumericValue) {
	    
	    Vector aval1 = ((NumericArrayValue)val1).getValue();
	    if ( aval1.size() > 0 && aval1.get(0) instanceof Number ) {
		Number v1;
		switch (relOp) {
                case Query.EQ:
                    for (int i = 0 ; i < aval1.size() ; i++) {
                        v1 = (Number) aval1.get(i);
                        if ( doApply(new NumericValue(v1), expTwo) ) {
                            return true;
                        }
                    }
                    return false;
                case Query.NE:
                    for (int i = 0 ; i < aval1.size() ; i++) {
                        v1 = (Number) aval1.get(i);
                        if ( doApply(new NumericValue(v1), expTwo) ) {
                            return false;
                        }
                    }
                    return true;
		default:
		    Debug.trace2("Numeric Array compare not supported for " +
				 relOp);
		    throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
		}
	    }
	}

	if (val1 instanceof NumericArrayValue && 
	    val2 instanceof NumericArrayValue &&
	    expOne instanceof AttributeExp &&  
	    expTwo instanceof AttributeExp) {
	    
	    Vector aval1 = ((NumericArrayValue)val1).getValue();
	    Vector aval2 = ((NumericArrayValue)val2).getValue();

	    if ((aval1 == null) || (aval2 == null)) {
		return false;
	    }
	    // check if vectors have same values
	    boolean eq = (aval1.size() == aval2.size()) &&
		          aval2.containsAll(aval1) &&
		          aval1.containsAll(aval2);
	    switch (relOp) {
	    case Query.EQ:
		return eq;
	    case Query.NE:
		return !eq;
	    default:
		Debug.trace2("Numeric Array compare not supported for " +
			     relOp);
		throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	    }
	}
	
	if ((val1 instanceof DateTimeExp) || (val2 instanceof DateTimeExp)) {
	    CIMDateTime dtval1, dtval2;

	    // Convert to datetimes
	    if (!(val1 instanceof DateTimeExp)) {
		try {
		    val1 = new DateTimeExp(((StringValueExp)val1).getValue());
		} catch (Exception e) {
		    throw new CIMException(CIMException.CIM_ERR_FAILED,
						val1.toString());
		}
	    }
	    dtval1 = ((DateTimeExp)val1).getValue();

	    if (!(val2 instanceof DateTimeExp)) {
		try {
		    val2 = new DateTimeExp(((StringValueExp)val2).getValue());
		} catch (Exception e) {
		    throw new CIMException(CIMException.CIM_ERR_FAILED,
						val2.toString());
		}
	    }

	    dtval2 = ((DateTimeExp)val2).getValue();

	    switch (relOp) {
	    case Query.GT:
		return dtval1.after(dtval2);
	    case Query.LT:
		return dtval1.before(dtval2);
	    case Query.GE:
		return dtval1.after(dtval2) || dtval1.equals(dtval2);
	    case Query.LE:
		return dtval1.before(dtval2) || dtval1.equals(dtval2);
	    case Query.EQ:
		return dtval1.equals(dtval2);
	    case Query.NE:
		return !dtval1.equals(dtval2);
	    }

	} else {
	    // We assume that sval1 is always the attribute exp value.
	    // if not, throw CIMException
	    try {
		sval1 = ((StringValueExp)val1).getValue();
		sval2 = ((StringValueExp)val2).getValue();
	    } catch (Exception exc) {
		Debug.trace2("Numeric Array compare not supported for " +
			     relOp);
		throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
	    }

	    switch (relOp) {
	    case Query.GT:
		return sval1.compareTo(sval2) > 0;
	    case Query.LT:
		return sval1.compareTo(sval2) < 0;
	    case Query.GE:
		return sval1.compareTo(sval2) >= 0;
	    case Query.LE:
		return sval1.compareTo(sval2) <= 0;
	    case Query.EQ:
		return sval1.compareTo(sval2) == 0;
	    case Query.NE:
		return sval1.compareTo(sval2) != 0;
	    case Query.LIKE:
		return like(sval1, sval2);
	    case Query.NLIKE:
		return !like(sval1, sval2);
		
	    }
	}

	return false;
    }


    public String toString() {
	return expOne + " " + relOpString() + " " + expTwo;
    }

    private String relOpString() {
	switch (relOp) {
	    case Query.GT:
		return ">";
	    case Query.LT:
		return "<";
	    case Query.GE:
		return ">=";
	    case Query.LE:
		return "<=";
	    case Query.EQ:
		return "=";
	    case Query.NE:
		return "<>";
	    case Query.LIKE:
		return "LIKE";
	    case Query.NLIKE:
		return "NOT LIKE";
	    case Query.ISA:
		return "ISA";
	    case Query.NISA:
		return "NOT ISA";
	}

	return "=";
    }

    // this method gets rid of consecutive '%'
    private String trimPercent(String pattern) {
	CharacterIterator ci = new StringCharacterIterator(pattern);
	StringBuffer sb = new StringBuffer();
	char prevc = CharacterIterator.DONE;
	for (char c = ci.first(); c != CharacterIterator.DONE; c = ci.next()) {
	    if ((c != '%') || (prevc != '%')) {
		sb.append(c);
	    }
	    prevc = c;
	}
	return sb.toString();
    }

    // This method implements a finite automaton to parse the limited RE
    // suggested by the LIKE pattern. However, this is not the best 
    // implementation because it involves some brain dead backtracing, 
    // but I really dont want to get into a full fledged DFA.
    // Also we need to use CharacterIterator rather than charAt.
    private boolean like(String match, String pattern) {

	int prevState = -1;
	int currState = 0;
	int backtrack = -1;
	char backtrackChar = CharacterIterator.DONE;
	int length = match.length();
	int i;

	pattern = trimPercent(pattern);
	int endIndex = pattern.length() -1;

	for (i = 0; i < length; i++) {

	    if (currState > endIndex) {
		// we've reached the end of the pattern but still have more
		// characters to consume
		return false;
	    }

	    char pcs = pattern.charAt(currState);
	    if (pcs == '%') {
		// remember the last '%' sign.
		prevState = currState;
		if (currState == endIndex) {
		    return true;
		}
		char a = match.charAt(i);
		if (a == pattern.charAt(currState+1)) {
		    // lets move forward 
		    currState = currState+2;
		    // If we encounter a failure, we go back to the next
		    // available backtrackChar
		    backtrackChar = a;
		    backtrack = -1;
		}
	    } else {
		char a = match.charAt(i);

		if ((backtrack == -1) && (a == backtrackChar)) {
		    // we have found the point to backtrack to
		    backtrack = i;
		}

		if (a != pcs) {
		    // We have encountered a failure, we must backtrack
		    if (prevState == -1) {
			// Nowhere to backtrack to
			return false;
		    } else {
			// Going back to the last %
			currState = prevState;
			if (backtrack != -1) {
			    // if we have found a backtrack position,
			    // go back to it.
			    i = backtrack-1;
			    backtrack = -1;
			} 
		    }
		} else {
		    // we've found a match, lets move forward
		    currState = currState+1;
		}
	    }
	}


	if ((currState > endIndex) || 
	    (currState == endIndex && pattern.charAt(endIndex) == '%')) {
	    return true;
	} else {
	    return false;
	}
    }
}
