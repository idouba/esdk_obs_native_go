/*
 * "@(#)CIMInvokeArgsMethodOp.java	1.2 1.2 01/30/02 SMI"
 *
 * Copyright 1998-2001 Sun Microsystems, Inc.,
 * All Rights Reserved.
 *
 * This software is the confidential and proprietary information of Sun
 * Microsystems, Inc. ("Confidential Information").  You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Sun.
 */

package javax.wbem.client;

import java.util.Vector;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMObjectPath;

public class CIMInvokeArgsMethodOp extends CIMElementOp {

    private String methodName = null;
    private CIMArgument[] inArgs = null;
    private CIMArgument[] outArgs = null;

    private final static long serialVersionUID = -5429498123944661662L;

    public CIMInvokeArgsMethodOp(CIMObjectPath name,
			String methodName,
			CIMArgument[] inArgs,
			CIMArgument[] outArgs) {

	super(name);
	this.methodName = methodName;
	this.inArgs = inArgs;
	this.outArgs = outArgs;

    }

    public String getMethodName() {
	return methodName;
    }

    public CIMArgument[] getInArgs() {
	return inArgs;
    }

    public CIMArgument[] getOutArgs() {
	return outArgs;
    }

    public Object getResult() {

	if ((result != null) && (result instanceof Vector)) {
	    Vector vResult = (Vector)result;
	    for (int i = 1; i < vResult.size(); i++) {
		try {
		    outArgs[i-1] = (CIMArgument)vResult.elementAt(i);
		} catch (ArrayIndexOutOfBoundsException oobe) {
		    // Ok the argument array is not big enough.
		    return oobe;
		}
	    }
	    return (vResult).elementAt(0);
	} else {
	    return result;
	}
    }
}
