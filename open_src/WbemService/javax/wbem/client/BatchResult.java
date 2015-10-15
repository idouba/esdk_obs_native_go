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

package javax.wbem.client;

import java.util.ArrayList;

import javax.wbem.cim.CIMException;

/**
 * The BatchResult class encapsulates the results from batch request.
 * This class allows the client to get the results of the individual
 * operations one by one. The class also allows the client to check how
 * many operations failed and the operation IDs of the ones that failed.
 * and the ones that succeeded.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 1.0
 */
public class BatchResult {

    private ArrayList failure_index = new ArrayList();
    private ArrayList success_index = new ArrayList();
    private Object[] results = null;

    /**
     * Constructor for the BatchResult Class.
     * 
     * @param results An array of objects that are the results of the
     *                corresponding operations that were invoked by the client
     *                using a BatchHandle.
     */
    public BatchResult(Object[] results) {
	
	this.results = results;
	// Analyze results
	if (results != null && results.length != 0) {
	    for (int i = 0; i < results.length; i++) {
		if ((results[i] == null) ||
			!(results[i] instanceof CIMException)) { 
		    success_index.add(new Integer(i));
		} else { 
		    failure_index.add(new Integer(i));
		}
	    }
	}
    }

    /**
     * Method to get the result of a particular operation. The ID identifies
     * the operation in question.
     * 
     * @param operationID The specific operation whose result is requested.
     * @return Object The result object which has the result of the operation.
     * @exception IllegalArgumentException Throws this exception if the ID
     *                    passed in is invalid, or if the result array is null.
     * @exception CIMException Throws this exception if the result of operation
     *                    is a CIMException.
     * @exception Exception Throws an exception if the operation resulted in an
     *                    exception other than CIMException
     */
    public Object getResult(int operationID) throws CIMException,
    						    Exception {

    	if ((results == null) || 
	    (operationID >= results.length) ||
	    (operationID < 0)) {
	    throw new IllegalArgumentException();
	}

	if (results[operationID] == null) {
	    return null;
	} else if (results[operationID] instanceof CIMException) {
	    throw(CIMException)results[operationID];
	} else if (results[operationID] instanceof Exception) {
	    throw(Exception)results[operationID];
	}
	return results[operationID];
    }
    	
    /**
     * Method to get the list of IDs of operations that failed.
     * 
     * @return An array of integers containing the IDs of operations that
     *         failed.
     */
    public int[] getFailureIds() {
    	
	return (array_traverse(false));
    }

    /**
     * Method to get the list of IDs of operations that succeeded.
     * 
     * @return An array of integers containing the IDs of operations that
     *         succeeded.
     */
    public int[] getSuccessIds() {
    	
	return (array_traverse(true));
    }

    private int[] array_traverse(boolean success) {

	int[] result_arr;
	ArrayList list = null;

	if (success == true) {
	    result_arr = new int[success_index.size()];
	    list = success_index;
	} else {
	    result_arr = new int[failure_index.size()];
	    list = failure_index;
	}

	for (int i = 0; i < result_arr.length; i++) {
	    result_arr[i] = ((Integer)list.get(i)).intValue();
	}

	return result_arr;
    }

}
