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

import java.io.PrintStream;

/**
 * An <code>OutboundRequest</code> represents a request that is being
 * sent and the corresponding response received in reply.
 *
 * This object can be used to write out the contents of the request
 * and to read in the response.
 *
 * <p>The communication protocol used by the implementation of this
 * interface must guarantee that for each instance of this interface,
 * any request data must only be delivered to the recipient (in the
 * form of an <code>InboundRequest</code> instance passed to the
 * <code>handleRequest</code> method of a <code>RequestHandler</code>)
 * <i>at most once</i>.
 *
 * <p>When finished using an <code>OutboundRequest</code>, in order to
 * allow the implementation to free resources associated with the request,
 * users should either invoke <code>close</code> on the streams
 * returned by the <code>getRequestOutputStream</code> and
 * <code>getResponseInputStream</code> methods, or invoke the
 * <code>abort</code> method.
 *
 * @author	Peter Jones
 * @version	1.11, 00/11/17
 * @see		InboundRequest
 * @since	1.4
 */
public interface OutboundRequest {

    /**
     * Returns an <code>OutputStream</code> to write the request data to.
     * The sequence of bytes written to the returned stream will be the
     * sequence of bytes sent as the body of this request.
     *
     * <p>After the entirety of the request has been written to the stream,
     * the stream's <code>close</code> method must be invoked to ensure
     * complete delivery of the request.  It is possible that none of the
     * data written to the returned stream will be delivered before
     * <code>close</code> has been invoked (even if the stream's
     * <code>flush</code> method had been invoked at any time).  Note,
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
     * @return	the output stream to write request data to
     */
    java.io.OutputStream getRequestOutputStream();

    /**
     * Returns an <code>InputStream</code> to read the response data from.
     * The sequence of bytes produced by reading from the returned stream
     * will be the sequence of bytes received as the response data.
     * When the entirety of the response has been successfully read,
     * reading from the stream will indicate an EOF.
     *
     * <p>Users of an <code>OutboundRequest</code> must not expect any
     * data to be available from the returned stream before the
     * <code>close</code> method has been invoked on the stream returned
     * by <code>getRequestOutputStream</code>; in other words,
     * the user's request/response protocol must not require any part of a
     * request to be a function of any part of its response.
     *
     * <p>It is possible, however, for data to be available from the
     * returned stream before the <code>close</code> method has been
     * invoked on, or even before the entirety of the request has been
     * written to, the stream returned by
     * <code>getRequestOutputStream</code>.  Because
     * such an early response might indicate, depending on the user's
     * request/response protocol, that the recipient will not consider
     * the entirety of the request, perhaps due to an error or other
     * abnormal condition, the user may wish to process it expeditiously,
     * rather than continuing to write the remainder of the request.
     *
     * <p>Invoking the <code>close</code> method of the returned stream
     * will cause any subsequent read operations on the stream to fail
     * with an <code>IOException</code>, although it will not terminate
     * this request as a whole; in particular, the request may still be
     * subsequently written to the stream returned by the
     * <code>getRequestOutputStream</code> method.  After
     * <code>close</code> has been invoked on both the returned stream
     * and the stream returned by <code>getRequestOutputStream</code>,
     * the implementation may free all resources associated with this
     * request.
     * 
     * <p>If this method is invoked more than once, it will always return
     * the identical stream object that it returned the first time, which
     * may be in a different state than it was upon return from the
     * first invocation.
     *
     * @return	the input stream to read response data from
     */
    java.io.InputStream getResponseInputStream();

    /**
     * Returns <code>true</code> if data written for this request
     * may have been at least partially processed by the recipient
     * (the <code>RequestHandler</code> receiving the corresponding
     * <code>InboundRequest</code> instance).
     *
     * If this method returns <code>false</code>, then it is
     * guaranteed that no data written for this request has been
     * processed by the recipient; this guarantee is valid until
     * any subsequent I/O operation has been attempted on this
     * request.
     *
     * @return	<code>true</code> if data written for this request may
     * have been processed by the recipient, and <code>false</code> if
     * data written for this request has definitely not been processed
     * by the recipient
     */
    boolean getDeliveryStatus();

    /**
     * Terminates this request, freeing all associated resources.
     *
     * <p>This method may be invoked at any stage of the processing
     * of the request.
     *
     * After this method has been invoked, I/O operations on the streams
     * returned by the <code>getRequestOutputStream</code> and
     * <code>getResponseInputStream</code> methods will fail with an
     * <code>IOException</code>, except some operations that may succeed
     * because they only affect data in local I/O buffers.
     *
     * <p>If this method is invoked before the <code>close</code> method
     * has been invoked on the stream returned by
     * <code>getRequestOutputStream</code>, there is no guarantee
     * that any or none of the data written to the stream so far will be
     * delivered; the implication of such an invocation of this method is
     * that the user is no longer interested in the successful delivery
     * of the request.
     */
    void abort();

   


     /**
     * adds a value associated with named field to output header
     *
     * @param	name the name of the field.
     * @return	the value associated with the named field, or null if field not
     * present in this header
     *
     */
    void addHeaderField(String name, String value);

    /**
     * call it to indicate end of add or midify output header
     *
     */
    void endWriteHeader() throws java.io.IOException;
    
     /**
     * returns the response code of this header
     *
     */
    int getResponseCode();

    /**
     * debug funtion to dump the output haeader header
     *
     */
    void dumpOutHeader(PrintStream psout) throws java.io.IOException;

    /**
     * debug funtion to dump the input haeader header
     *
     */
    void dumpInHeader(PrintStream psout) throws java.io.IOException;
}
