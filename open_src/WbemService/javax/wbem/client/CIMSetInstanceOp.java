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

import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;


public class CIMSetInstanceOp extends CIMQualifiedPropertiedElementOp {

    private final CIMInstance ci;

    private final static long serialVersionUID = -6912683679896999964L;

    public CIMSetInstanceOp(CIMObjectPath name, CIMInstance ci) {
        this(name, ci, true, null);
    }

    public CIMSetInstanceOp(CIMObjectPath name, CIMInstance ci, 
                            boolean includeQualifier, String[] propertyList) {

        super(name, includeQualifier, propertyList);
        this.ci = ci;
    }

    public CIMInstance getCIMInstance() {
	return ci;
    }
}
