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
import javax.wbem.provider.CIMAssociatorProvider;
import javax.wbem.provider.CIMInstanceProvider;

/**
 * Interface for Provider specific callbacks to the CIMOM.
 *
 * @author	Sun Microsystems, Inc.
 * @since	WBEM 1.0
 **/
public interface ProviderCIMOMHandle extends CIMOMHandle {

    /**
     * Get the handle to an internal instance provider which can be used to
     * store static instance information for the provider.
     * @return The CIMInstanceProvider handle
     */
    public CIMInstanceProvider getInternalCIMInstanceProvider();
    /**
     * Get the handle to an internal instance provider which can be used to
     * traverse static association instances.
     * @return The CIMAssociatorProvider handle
     */
    public CIMAssociatorProvider getInternalCIMAssociatorProvider();

    /**
     * Returns the current user on whose behalf the provider has been invoked.
     */
    public String getCurrentUser();

    /**
     * Returns the current role assumed by the current authenticated user.
     * May be null if no role was assumed or no user authenticated.
     */
    public String getCurrentRole();

    /**
     * Returns the remote client host name for the current session.
     */
    public String getCurrentClientHost();

    /**
     * Returns a usually unique identifier for the session to be used
     * in auditing records to identify the remote client connection.
     */
    public int getCurrentAuditId();

    /**
     * Decrypts the specified string value using the authentication session
     * key, if the value is encrypted.  An encrypted string value is
     * indicated by a series of hexadecimal characters enclosed in brackets <>.
     * If the value is not encrypted, or no authentication exchange encryption
     * key is available, a null value is returned.  The size of the encrypted
     * data is restricted to 32 hexadecimal characters, representing at most
     * 16 uncrypted characters.  Only unlocalized ASCII characters are allowed.
     *
     * @param encryptedData	A bracketed string of hexadecimal characters
     * 				representing an encrypted ASCII string value.
     *
     * @return The decrypted string value
     */
    public String decryptData(String encryptedData);

    /**
     * Method used by event providers to deliver indications into the CIM object
     * manager. The CIM object manager event service subsequently takes care
     * of delivering the event to subscribers.
     * @param namespace The namespace to which this event pertains.
     * @param indication The actual indication
     */
    public void deliverEvent(String namespace, CIMInstance indication);

    /**
     * Method used by event providers to deliver indications into the CIM object
     * manager. The event provider specifies which filters matched, and the
     * CIMOM simply forwards the indications to the handlers subscribed to
     * the filters.
     * @param indication The actual indication
     * @param matchedFilterOps Array of object paths to the matched filters. If
     * an object path is null, it is ignored. If any filter is invalid, for
     * example, if it cannot be found, then the indication is not delivered to
     * any of the filters.
     */
    public void deliverEvent(CIMInstance indication, 
    CIMObjectPath[] matchedFilterOps);
}
