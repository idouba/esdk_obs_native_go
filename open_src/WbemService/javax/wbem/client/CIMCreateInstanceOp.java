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

package javax.wbem.client;

import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;

public class CIMCreateInstanceOp extends CIMElementOp {

    private CIMInstance ci = null;

    private final static long serialVersionUID = -3075044964829844272L;

    public CIMCreateInstanceOp(CIMObjectPath name, 
			CIMInstance ci) {

	super(name);
	this.ci = ci;
    }

    public CIMInstance getCIMInstance() {
	return ci;
    }

    public Object getResult() {
    	
        if ((result == null) || !(result instanceof CIMObjectPath)) {
	    // We do not need to return the relative namespace if the object
	    // path is null.
	    // OR - the result might be an exception.
	    return result;
	}

        return (CIMObjectPath)result;
    }

}
