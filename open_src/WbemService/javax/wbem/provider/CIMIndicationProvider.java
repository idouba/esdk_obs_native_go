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
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;

/**
 * This is the interface implemented by indication providers. If the interface
 * is not implemented, the CIM Object manager (CIMOM) may poll its instances for
 * life cycle indications (CIM_InstCreation, CIM_InstDeletion, etc).
 * <p>
 * Filters may be broken down by the CIMOM into sub filters. Using sub
 * filters, providers can process indications
 * without knowing about the class hierarchy. The sub filter information
 * is passed in as three arrays:
 * <ul>
 * <li>String[] subFilters;</li>
 * <li>CIMObjectPath[] classPath;</li>
 * <li>String[] eventType;</li>
 * </ul>
 * The following example illustrates the array contents where the
 * class hierarchy in root/cimv2 is:
 * <pre>
 * test_a
 * test_b : test_a
 * test_c : test_a
 * test_d : test_a
 * </pre>
 * If Provider P is the provider for test_b and test_c and the filter is
 * 'select * from CIM_InstIndication where sourceInstance ISA test_a
 * and sourceInstance.prop1 = 20', then P gets the following subfilter
 * information:
 * <pre>
 * subFilters[0] = "select * from CIM_InstCreation where sourceInstance.prop1=20"
 * subFilters[1] = "select * from CIM_Deletion where sourceInstance.prop1=20"
 * subFilters[2] = "select * from CIM_Modification where sourceInstance.prop1=20"
 * subFilters[3] = "select * from CIM_Read where sourceInstance.prop1=20"
 * subFilters[4] = "select * from CIM_Method where sourceInstance.prop1=20"
 * subFilters[5] = "select * from CIM_InstCreation where sourceInstance.prop1=20"
 * subFilters[6] = "select * from CIM_Deletion where sourceInstance.prop1=20"
 * subFilters[7] = "select * from CIM_Modification where sourceInstance.prop1=20"
 * subFilters[8] = "select * from CIM_Read where sourceInstance.prop1=20"
 * subFilters[9] = "select * from CIM_Method where sourceInstance.prop1=20"
 *
 * classPath[0] = root/cimv2:test_b
 * classPath[1] = root/cimv2:test_b
 * classPath[2] = root/cimv2:test_b
 * classPath[3] = root/cimv2:test_b
 * classPath[4] = root/cimv2:test_b
 * classPath[5] = root/cimv2:test_c
 * classPath[6] = root/cimv2:test_c
 * classPath[7] = root/cimv2:test_c
 * classPath[8] = root/cimv2:test_c
 * classPath[9] = root/cimv2:test_c
 *
 * eventType[0] = "CIM_InstCreation"
 * eventType[1] = "CIM_InstDeletion"
 * eventType[2] = "CIM_InstModification"
 * eventType[3] = "CIM_InstRead"
 * eventType[4] = "CIM_InstMethod"
 * eventType[5] = "CIM_InstCreation"
 * eventType[6] = "CIM_InstDeletion"
 * eventType[7] = "CIM_InstModification"
 * eventType[8] = "CIM_InstRead"
 * eventType[9] = "CIM_InstMethod"
 * </pre>
 * NOTE: eventType is not strictly needed as the Provider could
 * extract it from the subFilters string. It is provided as a convenience.
 *
 * <p>
 * Two event handling interfaces are defined. Simple event handlers should use 
 * the EventProvider interface. More sophisticated event handlers should use 
 * the CIMIndicationHandler interface. 
 * 
 * <p>
 * If a simple event handler is only interested in managing event filter 
 * activation/deactivation and does not intend to perform operations on the 
 * filters, handlers or subscriptions, then it should use the Event Provider 
 * interface. 
 * 
 * <p>
 * In contrast, if a more sophisticated event handler intends to perform 
 * operations on filter, handler and subscription instances, then it should 
 * use the CIMIndicationHandler interface. For example, an event handler may 
 * wish to examine all filters to determine which filter applies.
 *
 * @see    EventProvider
 * @author Sun Microsystems, Inc.
 * @since  WBEM 1.0
 */
public interface CIMIndicationProvider extends CIMProvider {

    /**
     * Invoked by a CIMOM to test if the given subscription is allowed. The
     * CIMOM will only call this method if the provider also implements the
     * Authorizable interface.
     * 
     * @param filter The CIM_IndicationFilter instance being subscribed to.
     * @param handler The CIM_IndicationHandler instance that is the
     *                destination of the indication.
     * @param subscription The subscription instance associating the filter and
     *                handler.
     * @param subFilters See description for CIMIndicationProvider above
     * @param classPath See description for CIMIndicationProvider above
     * @param eventType See description for CIMIndicationProvider above
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED
     *                    <li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     * @see javax.wbem.provider.Authorizable
     */
    public void authorizeFilter(CIMInstance filter,
                    CIMInstance handler,
                    CIMInstance subscription,
                    String[] subFilters,
                    CIMObjectPath[] classPath,
                    String[] eventType)
        throws CIMException;

    /**
     * Invoked by a CIMOM to test if a given filter expression is allowed by
     * the provider, and if it must be polled.
     * <p>
     * NOTE: For intrinsic events, this method is only called for
     * CIM_InstCreation, CIM_InstDeletion and CIM_InstModification. If this
     * method returns TRUE, then CIM object manager will not call
     * activateFilter or deActivateFilter.
     * 
     * @param filter The CIM_IndicationFilter instance being subscribed to.
     * @param handler The CIM_IndicationHandler instance that is the
     *                destination of the indication.
     * @param subscription The subscription instance associating the filter and
     *                handler.
     * @param subFilters See description for CIMIndicationProvider above
     * @param classPath See description for CIMIndicationProvider above
     * @param eventType See description for CIMIndicationProvider above
     * @return Each element represents its corresponding
     *         subFilter. If true, the corresponding subFilter must be polled
     *         by the CIMOM. If false, the corresponding subFilter is not
     *         polled.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider will not honor
     *                    this filter)</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public boolean[] mustPoll(CIMInstance filter, 
                    CIMInstance handler,
                    CIMInstance subscription,
                    String[] subFilters,
                    CIMObjectPath[] classPath,
                    String[] eventType)
    throws CIMException;
    
    /**
     * Invoked by the CIMOM to ask the Provider to check for events.
     * 
     * @param filter The CIM_IndicationFilter instance being subscribed to.
     * @param handler The CIM_IndicationHandler instance that is the
     *                destination of the indication.
     * @param subscription The subscription instance associating the filter and
     *                handler.
     * @param subFilters See description for CIMIndicationProvider above
     * @param classPath See description for CIMIndicationProvider above
     * @param eventType See description for CIMIndicationProvider above
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (Provider could not activat the
     *                    filter)</li>
     *                    </ul>
     */
    public void activateFilter(CIMInstance filter,
                    CIMInstance handler,
                    CIMInstance subscription,
                    String[] subFilters,
                    CIMObjectPath[] classPath,
                    String[] eventType)
        throws CIMException;
      
    /**
     * Invoked by the CIMOM to ask the Provider to deactivate an event filter.
     * 
     * @param filter The CIM_IndicationFilter instance being subscribed to.
     * @param handler The CIM_IndicationHandler instance that is the
     *                destination of the indications.
     * @param subscription The subscription instance associating the filter and
     *                handler.
     * @param subFilters See description for CIMIndicationProvider above
     * @param classPath See description for CIMIndicationProvider above
     * @param eventType See description for CIMIndicationProvider above
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (Provider could not deactivate
     *                    the filter)</li>
     *                    </ul>
     */
    public void deActivateFilter(CIMInstance filter,
                    CIMInstance handler,
                    CIMInstance subscription,
                    String[] subFilters,
                    CIMObjectPath[] classPath,
                    String[] eventType)
        throws CIMException;
}
