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

package javax.wbem.provider;

import javax.wbem.cim.CIMException;
import javax.wbem.client.CIMOMHandle;

/**
 * This is the base interface implemented by all providers.
 * 
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public interface CIMProvider {

    /**
     * Called by the CIMOM when the provider is loaded. This method will be
     * called only once; i.e. cleanup() must be called before initialize() is
     * called again.
     * 
     * @param handle the CIMOM handle
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public void initialize(CIMOMHandle handle) throws CIMException;

    /**
     * Called by the CIMOM when the provider is removed. Currently the CIMOM
     * does not remove providers, but this method is provided for future
     * versions.
     * 
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public void cleanup() throws CIMException;
}
