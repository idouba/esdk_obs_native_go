/*
 * EXHIBIT A - Sun Industry Standards Source License "The contents of this file
 * are subject to the Sun Industry Standards Source License Version 1.2 (the
 * "License"); You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://wbemservices.sourceforge.net/license.html Software distributed under
 * the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * WBEM Services. The Initial Developer of the Original Code is: Sun
 * Microsystems, Inc. Portions created by: Sun Microsystems, Inc. are Copyright (c)
 * 2001 Sun Microsystems, Inc. All Rights Reserved. Contributor(s):
 * _______________________________________
 */
package javax.wbem.provider;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMValue;

/**
 * This provides the means to invoke the Extrinsic Method Invocation operation
 * as defined by the CIM operations over HTTP spec at http://www.dmtf.org/.
 * 
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public interface CIMMethodProvider extends CIMProvider {
    /**
     * This method is invoked in order to perform the Extrinsic Method
     * Invocation operation as defined by the CIM operations over HTTP spec at
     * http://www.dmtf.org/. The CIMOM calls this method when the extrinsic
     * method specified in the parameters is to be invoked.
     * 
     * @param op Contains the path to the instance whose extrinsic method must
     *                be invoked. It must include all of the keys.
     * @param methodName The name of the extrinsic method even if only one
     *                extrinsic method is supported.
     * @param inArgs An array of CIMArgument which are the input parameters for
     *                methodName. If methodName has no input parameters then
     *                inArgs will be a zero-length array.
     * @param outArgs An array of CIMArgument which are the output values for
     *                methodName returned by the Provider. If methodName has no
     *                output parameters then it is assumed that the client has
     *                allocated a zero-length array that the Provider need not
     *                modify.
     * @return The return value of the extrinsic method. If the
     *         extrinsic method has no return value, it must return NULL. NOTE:
     *         The actual data the extrinsic method may return is found in
     *         outArgs.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_NOT_FOUND (if instance does not exist)
     *                    </li>
     *                    <li>CIM_ERR_METHOD_NOT_FOUND</li>
     *                    <li>CIM_ERR_METHOD_NOT_AVAILABLE</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMValue invokeMethod(CIMObjectPath op, String methodName,
                                 CIMArgument[] inArgs, CIMArgument[] outArgs)
        throws CIMException;
}
