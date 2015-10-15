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
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.query.SelectExp;

/**
 * This is the interface implemented by event providers. If the interface is
 * not implemented by a provider, the CIM object manager assumes that it must
 * be polled.
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
 * @see CIMIndicationProvider
 * @author    Sun Microsystems, Inc.
 * @since     WBEM 1.0
 */
public interface EventProvider extends CIMProvider {
    /**
     * Invoked by a CIM object manager to test if a given filter expression is
     * allowed. The object manager will only call this method if the provider
     * also implements the Authorizable interface.
     * 
     * @param filter The filter that must be authorized. (e.g. SELECT * FROM
     *                CIM_InstCreation)
     * @param eventType The type of event, which can also be extracted from the
     *                FROM clause of expression (e.g. CIM_InstCreation). The
     *                CIM object manager provides this value as a convenience
     *                for the provider.
     * @param classPath The name of the class for which the event is required.
     *                Only the namespace component may be populated if
     *                eventType is a process indication.
     * @param owner The user id for the owner of the event destination. Note
     *                that the destination owner may be different from the
     *                actual user making the request for the filter activation.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     * @see javax.wbem.provider.Authorizable
     */
    public void authorizeFilter(SelectExp filter, String eventType,
    CIMObjectPath classPath, String owner) throws CIMException;

    /**
     * Invoked by a CIM object manager to test if a given filter expression is
     * allowed by the provider, and if it must be polled. NOTE: For intrinsic
     * events, this method is only called for CIM_InstCreation,
     * CIM_InstDeletion and CIM_InstModification. If this method returns TRUE,
     * then CIM object manager will not call activateFilter or
     * deActivateFilter.
     * 
     * @param filter The filter that needs to be checked (e.g. SELECT * FROM
     *                CIM_InstCreation)
     * @param eventType The type of event, which can also be extracted from the
     *                FROM clause of expression (e.g. CIM_InstCreation). The
     *                CIM object manager provides this value as a convenience
     *                for the provider.
     * @param classPath The name of the class for which the event is required.
     *                Only the namespace component may be populated if
     *                eventType is a process indication.
     * @return boolean true if the provider expects the object manager to poll
     *         it, false otherwise.
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
    public boolean mustPoll(SelectExp filter, String eventType,
    CIMObjectPath classPath) throws CIMException;

    /**
     * Invoked by the CIM object manager to ask the provider to check for
     * events.
     * 
     * @param filter The filter that needs to be checked (e.g. SELECT * FROM
     *                CIM_InstRead)
     * @param eventType The type of event, which can also be extracted from the
     *                FROM clause of expression (e.g. CIM_InstRead). The CIM
     *                object manager provides this value as a convenience for
     *                the provider.
     * @param classPath The name of the class for which the event is required.
     *                Only the namespace component may be populated if
     *                eventType is a process indication.
     * @param firstActivation A convenience variable passed in by the CIM
     *                object manager. If true, then this is the first filter
     *                for eventType.
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
    public void activateFilter(SelectExp filter, String eventType, 
    CIMObjectPath classPath, boolean firstActivation) throws CIMException;

    /**
     * Invoked by the CIM object manager to ask the provider to deactivate an
     * event filter.
     * 
     * @param filter The filter that needs to be checked (e.g. SELECT * FROM
     *                CIM_InstMethodCall)
     * @param eventType The type of event, which can also be extracted from the
     *                FROM clause of expression (e.g. CIM_InstMethodCall). The
     *                CIM object manager provides this value as a convenience
     *                for the provider.
     * @param classPath The name of the class for which the event is required.
     *                Only the namespace component may be populated if
     *                eventType is a process indication.
     * @param lastActivation A convenience variable passed in by the CIM object
     *                manager. If true, then this is the last filter for
     *                eventType.
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
    public void deActivateFilter(SelectExp filter, String eventType, 
    CIMObjectPath classPath, boolean lastActivation) throws CIMException;
}
