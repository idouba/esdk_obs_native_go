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

import java.util.EventObject;

import javax.wbem.cim.CIMInstance;

/**
 * 
 * This class represents the CIM indication that is delivered to the subscriber.
 * as a result of client subscriptions.
 *
 * @author      Sun Microsystems, Inc.
 * @version     1.1 03/01/01
 * @since       WBEM 1.0
 */
public class CIMEvent extends EventObject {

    final static long serialVersionUID = 9088104815922399641L;

    protected CIMInstance indication;
    private final static String SOURCEINSTANCE = "sourceInstance";

    /**
     * Constructs a CIMEvent.
     * @param indication The actual cimIndication. This may be a life cycle
     * indication, meta schema indication or process indication as specified
     * by the CIM Events spec. The source property is populated using the 
     * source instance from indication, if present.
     */
    public CIMEvent(CIMInstance indication) {
	super(indication);
	CIMInstance si = null;
	try {
	    si = (CIMInstance)indication.getProperty(SOURCEINSTANCE).getValue().
	    getValue();
	} catch (Exception e) {
	    // no source instance value;
	}

	this.source = si;
	this.indication = indication;
    }

    /**
     * Returns the embedded indication.
     * @return CIMInstance representing the indication.
     */
    public CIMInstance getIndication() {
	return indication;
    }
    
    public String toString()
    {
        return this.indication.toString();
    }
}
