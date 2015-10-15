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

import javax.wbem.cim.CIMNameSpace;



public abstract class CIMOperation implements java.io.Serializable {
    protected CIMNameSpace nameSpace = null;
    protected transient CIMNameSpace targetNS = null;
    protected Object result = null;

    private final static long serialVersionUID = -7846839483654038614L;

    public CIMOperation() {
    }

    public void setResult(Object result) {
	this.result = result;
    }

    public CIMNameSpace getNameSpace() {
	return nameSpace;
    }

    public Object getResult() {
	return result;
    }

    public void setNameSpace(CIMNameSpace ns) {
    	
	this.nameSpace = ns;
    }

    protected CIMNameSpace getTargetNS() {
	return this.targetNS;
    }

    protected void setTargetNS(CIMNameSpace tns) {
	this.targetNS = tns;
    }

}
