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
 * <code>RequestHandler</code> is the callback interface for an object
 * that will process an incoming request in the form of an
 * <code>InboundRequest</code> instance.
 *
 * <p>For example, requests received on a {@link ServerEndpoint}
 * instance will be dispatched to the implementation of this interface
 * that was passed to the endpoint's <code>listen</code> method.
 *
 * @author	Peter Jones
 * @version	1.6, 00/10/30
 * @since	1.4
 */
public interface RequestHandler {

    /**
     * Handles an incoming request.  The supplied
     * <code>InboundRequest</code> object can be used to
     * read the request data and to send the associated response.
     *
     * <p>After the invocation of this method completes (either by
     * returning normally or by throwing an exception), the supplied
     * <code>InboundRequest</code> will be automatically terminated
     * (see {@link InboundRequest#abort}).
     *
     * If this method completes before the <code>close</code> method
     * has been invoked on the stream returned by the request's
     * <code>getResponseOutputStream</code> method, there is no guarantee
     * that any or none of the data written to the stream so far will be
     * delivered; the implication is that the implementation of this
     * method is no longer interested in the successful delivery
     * of the response.
     *
     * @param	request the <code>InboundRequest</code> instance to be used
     * for reading the request and writing the response
     *
     * @throws	IllegalStateException if invoked unexpectedly, such as
     * before being registered with an <code>InboundRequest</code>
     * source or after being unregistered from one
     * (for example, after an invocation of <code>close</code> on a
     * {@link ServerEndpointListener} associated with this
     * <code>RequestHandler</code> has completed)
     */
    void handleRequest(InboundRequest request);
    
    /** 
     *  close handle
     */
    void close();
    
    /* returns false if authentication failed. */
    boolean checkAuthentication(InboundRequest request);

    /* add fields to the response Header */
    void addResponseHeaderFields(InboundRequest request);
}
