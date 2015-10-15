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
 *are Copyright (c) 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package javax.wbem.client.adapter.http.transport;

/**
 * A <code>ServerEndpointListener</code> represents a
 * <code>ServerEndpoint</code> that is being listened on.
 *
 * <p>This object can be used to stop listening and to obtain an
 * <code>Endpoint</code> instance that can be used to send requests
 * to the communication endpoint being listened on.
 *
 * <p>A <code>ServerEndpointListener</code> is obtained by invoking
 * the <code>listen</code> method of a <code>ServerEndpoint</code>.
 *
 * @author	Peter Jones
 * @version	1.8, 00/10/30
 * @see		ServerEndpoint
 * @since	1.4
 */
public interface ServerEndpointListener
{
    
    /**
     * Stops listening for requests on the associated server endpoint,
     * freeing any resources associated with doing so.  After this
     * method has returned, no more requests will be dispatched to
     * the associated <code>RequestHandler</code>.
     *
     * <p>Invoking this method terminates any requests that have been
     * received on the associated <code>ServerEndpoint</code>
     * and dispatched to the associated <code>RequestHandler</code>
     * but have not yet had their request output stream closed
     * (see {@link InboundRequest#abort});
     * subsequent I/O operations on such requests will fail with an
     * <code>IOException</code>, except some operations that may succeed
     * because they only affect data in local I/O buffers.
     */
    void close();
    
    int getLocalPort();
    
    // begin modified by x00102290  add use SOCKS5 proxy support,need proxy IP
    String getLocalIP();
}
