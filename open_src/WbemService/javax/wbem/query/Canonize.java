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


import java.util.ArrayList;
import java.util.List;

/**
 *
 * Utility class to perform canonizing. This logic actually belongs in each
 * of the QueryExp classes that are being dealt with - we can make use of 
 * polymorphism. I'm putting it this way so that we dont have to update each
 * of those classes for now.
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 2.0
 *
 */
class Canonize {

    // Should be in BinaryRel
    private static BinaryRelQueryExp negate(BinaryRelQueryExp qe) {
	int op = Query.GT - 1;
	int relOp = qe.getOperator();
	switch (relOp) {
	    case Query.GT:
		op = Query.LE;
		break;
	    case Query.LT:
		op = Query.GE;
		break;
	    case Query.GE:
		op = Query.LT;
		break;
	    case Query.LE:
		op = Query.GT;
		break;
	    case Query.EQ:
		op = Query.NE;
		break;
	    case Query.NE:
		op = Query.EQ;
		break;
	    case Query.LIKE:
		op = Query.NLIKE;
		break;
	    case Query.NLIKE:
		op = Query.LIKE;
		break;
	    case Query.ISA:
		op = Query.NISA;
		break;
	    case Query.NISA:
		op = Query.ISA;
		break;
	}

	return new BinaryRelQueryExp(op, qe.getLeftValue(), qe.getRightValue());
    }

    // This output List is in the conjunction of disjunctions canonical form.
    // In other words, gives a List which is an AND of a List of ORs
    static List cod(QueryExp qe) {
	// There must be a better way of doing this.
	// For now, I negate the input expression, find its doc and
	// negate it back, since the negation of a doc is a cod
	// e.g (a.b + c.d + e.f)' = (a'+b').(c'+d').(e'+f')
	List out = doc(new NotQueryExp(qe));
	int osize = out.size();
	List fout = new ArrayList();
	for (int i = 0; i < osize; i++) {
	    List al = (List)(out.get(i));
	    int asize = al.size();
	    List fol = new ArrayList();
	    for (int j = 0; j < asize; j++) {
		fol.add(negate((BinaryRelQueryExp)(al.get(j))));
	    }
	    fout.add(fol);
	}
	return fout;
    }

    // This output List is in the disjunction of conjuntions canonical form.
    // In other words, gives a List which is an OR of a List of ANDs
    static List doc(QueryExp qe) {
	return intdoc(removeNots(qe));
    }

    // ANDs two disjunctions of conjunctions and outputs one
    // disjunction of conjunctions.
    private static List crossProduct(List l1, List l2) {
	int l1size = l1.size();
	int l2size = l2.size();
	List cp = new ArrayList();

        // AND each conjunction of the first list with each conjunction of the 
	// second list
	for (int i = 0; i < l1size; i++) {
	    for (int j = 0; j < l2size; j++) {
		List al = new ArrayList();
		al.addAll((List)(l1.get(i)));
		al.addAll((List)(l2.get(j)));
		cp.add(al);
	    }
	}

	return cp;
    }

    // Since nots are removed before calling this, we need to
    // only deal with BinaryRel, And, Or
    private static List intdoc(QueryExp qe) {

	if (qe instanceof BinaryRelQueryExp) {
	    // No canonizing required
	    List ol = new ArrayList();
	    List al = new ArrayList();
	    al.add(qe);
	    ol.add(al);
	    return ol;
	}

	if (qe instanceof AndQueryExp) {
	    AndQueryExp aqe = (AndQueryExp)qe;
	    List l1 = intdoc(aqe.getLeftExp());
	    List l2 = intdoc(aqe.getRightExp());
	    return crossProduct(l1, l2);
	}

	if (qe instanceof OrQueryExp) {
	    // Concatenate the lists
	    OrQueryExp oqe = (OrQueryExp)qe;
	    List l1 = intdoc(oqe.getLeftExp());
	    List l2 = intdoc(oqe.getRightExp());
	    l1.addAll(l2);
	    return l1;
	}
	return null;
    }

    static QueryExp removeNots(QueryExp qe) {

	int i = 0;
	while (qe instanceof NotQueryExp) {
	    qe = ((NotQueryExp)qe).getNegatedExp();
	    i++;
	}

	if (i%2 == 0) {
	    if (qe instanceof BinaryRelQueryExp) {
		return qe;
	    }

	    if (qe instanceof AndQueryExp) {
		AndQueryExp aqe = (AndQueryExp) qe;
		return new AndQueryExp(removeNots(aqe.getLeftExp()),
					removeNots(aqe.getRightExp()));
	    }

	    if (qe instanceof OrQueryExp) {
		OrQueryExp oqe = (OrQueryExp) qe;
		return new OrQueryExp(removeNots(oqe.getLeftExp()),
					removeNots(oqe.getRightExp()));
	    }
	} else {
	    if (qe instanceof BinaryRelQueryExp) {
		// We should find inverse here
		return negate((BinaryRelQueryExp)qe);
	    }

	    // (a.b)' = a' + b'
	    if (qe instanceof AndQueryExp) {
		AndQueryExp aqe = (AndQueryExp) qe;
		NotQueryExp leftInverse = new NotQueryExp(aqe.getLeftExp());
		NotQueryExp rightInverse = new NotQueryExp(aqe.getRightExp());
		return new OrQueryExp(removeNots(leftInverse),
					removeNots(rightInverse));
	    }

	    // (a+b)' = a'.b'
	    if (qe instanceof OrQueryExp) {
		OrQueryExp oqe = (OrQueryExp) qe;
		NotQueryExp leftInverse = new NotQueryExp(oqe.getLeftExp());
		NotQueryExp rightInverse = new NotQueryExp(oqe.getRightExp());
		return new AndQueryExp(removeNots(leftInverse),
					removeNots(rightInverse));
	    }
	}

	// Should not come here
	return null;
    }
}
