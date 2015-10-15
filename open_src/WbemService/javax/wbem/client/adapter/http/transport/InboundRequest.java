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
 * An <code>InboundRequest</code> represents a request that is being
 * received and the corresponding response to be sent in reply.
 *
 * This object can be used to read in the contents of the request and
 * write out the response.
 *
 * @author	Peter Jones
 * @version	1.11, 00/11/20
 * @see		java.rmi.transport
 * @see		OutboundRequest
 * @see		RequestHandler
 * @since 1.4
 */
public interface InboundRequest
{

    /**
     * Verifies that the calling context has all of the security
     * permissions necessary to receive this request.
     *
     * <p>This method should be used when a particular shared mechanism
     * is used to receive requests for a variety of interested parties,
     * each with potentially different security permissions possibly more
     * limited than those granted to the code managing the shared
     * mechanism, so that the managing code can verify proper access
     * control for each party.
     *
     * @throws	SecurityException if the calling context does not have the
     * permissions necessary to receive this request
     */
    void checkPermissions();

    /**
     * Returns a string identifying the client host that this request
     * was received from, or <code>null</code> if no such string is
     * available.
     *
     * The exact format of the returned string, if non-<code>null</code>,
     * is defined by the implementation of this interface.
     *
     * @return	string identifying the host that this request
     * was received from, or <code>null</code>
     */
    String getClientHost();

    /**
     * Returns an <code>InputStream</code> to read the request data from.
     * The sequence of bytes produced by reading from the returned stream
     * will be the sequence of bytes received as the request data.
     * When the entirety of the request has been successfully read,
     * reading from the stream will indicate an EOF.
     *
     * <p>Invoking the <code>close</code> method of the returned stream
     * will cause any subsequent read operations on the stream to fail
     * with an <code>IOException</code>, although it will not terminate
     * this request as a whole; in particular, the response may still be
     * subsequently written to the stream returned by the
     * <code>getResponseOutputStream</code> method.  After
     * <code>close</code> has been invoked on both the returned stream
     * and the stream returned by <code>getResponeOutputStream</code>,
     * the implementation may free all resources associated with this
     * request.
     * 
     * <p>If this method is invoked more than once, it will always return
     * the identical stream object that it returned the first time, which
     * may be in a different state than it was upon return from the
     * first invocation.
     *
     * @return	the input stream to read request data from
     */
    java.io.InputStream getRequestInputStream();

    /**
     * Returns an <code>OutputStream</code> to write the response data to.
     * The sequence of bytes written to the returned stream will be the
     * sequence of bytes sent as the response.
     *
     * <p>After the entirety of the response has been written to the stream,
     * the stream's <code>close</code> method must be invoked to ensure
     * complete delivery of the response.  It is possible that none of the
     * data written to the returned stream will be delivered before
     * <code>close</code> has been invoked (even if the stream's
     * <code>flush</code> method has been invoked at any time).  Note,
     * however, that some or all of the data written to the stream may be
     * delivered to (and processed by) the recipient before the stream's
     * <code>close</code> method has been invoked.
     *
     * <p>After the stream's <code>close</code> method has been invoked,
     * no more data may be written to the stream; writes subsequent to a
     * <code>close</code> invocation will fail with an
     * <code>IOException</code>.
     *
     * <p>If this method is invoked more than once, it will always return
     * the identical stream object that it returned the first time, which
     * may be in a different state than it was upon return from the
     * first invocation.
     *
     * @return	the output stream to write response data to
     */
    java.io.OutputStream getResponseOutputStream();

    /**
     * Terminates this request, freeing all associated resources.
     *
     * <p>This method may be invoked at any stage of the processing
     * of the request.
     *
     * After this method has been invoked, I/O operations on the streams
     * returned by the <code>getRequestInputStream</code> and
     * <code>getResponseOutputStream</code> methods will fail with an
     * <code>IOException</code>, except some operations that may succeed
     * because they only affect data in local I/O buffers.
     *
     * <p>If this method is invoked before the <code>close</code> method
     * has been invoked on the stream returned by
     * <code>getResponseOutputStream</code>, there is no guarantee
     * that any or none of the data written to the stream so far will be
     * delivered; the implication of such an invocation of this method is
     * that the user is no longer interested in the successful delivery
     * of the response.
     */
    void abort();

    /**
     * Returns value associated with named field, or null if field not present
     * in this header.
     *
     * @param	name the name of the field.
     * @return	the value associated with the named field, or null if field not
     * present in this header
     *
     */
    String getHeaderField(String name);


    /**
     *
     * @param	name the name of the field.
     * @param	the value associated with the named field, or null if field not
     * present in the header
     *
     */
    void setRespondHeaderField(String name, String value);


}
