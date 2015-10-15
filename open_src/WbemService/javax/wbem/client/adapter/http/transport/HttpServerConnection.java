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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Class representing a server-side HTTP connection used to receive and
 * dispatch incoming HTTP requests.
 */
public abstract class HttpServerConnection extends HttpConnection {
    
    private static final String serverString = 
	"Java/" + System.getProperty("java.version", "???") +
		       " " + HttpServerConnection.class.getName();

    private static final ExecutorService userThreadPool = (ExecutorService)
	java.security.AccessController.doPrivileged(
	    new GetThreadPoolAction());

    private RequestHandler handler;

    /**
     * Creates new HttpServerConnection on top of given socket.
     */
    public HttpServerConnection(Socket sock, 
				RequestHandler handler)
	    throws IOException {
        
        super(UNSTARTED);
        
	if (handler == null) {
	    throw new NullPointerException();
	}
	this.sock    = sock;
	this.handler = handler;
	in  = new BufferedInputStream(sock.getInputStream());
	out = new BufferedOutputStream(sock.getOutputStream());
    }

    /**
     * Starts request dispatch thread.  Throws IllegalStateException if
     * connection has already been started, or is closed.
     */
    protected void start() {
	synchronized (stateLock) {
	    if (state != UNSTARTED) {
		throw new IllegalStateException();
	    }
	    state = IDLE;
	    userThreadPool.execute(new Dispatcher());
	}
    }

    /**
     * Verifies that calling context has sufficient security permissions to
     * receive a request on this connection.
     */
    protected void checkPermissions() {
    }
    
    /**
     * Returns value to be returned by an invocation of getClientHost on an
     * InboundRequest dispatched by this HttpServerConnection.
     */
    protected abstract String getClientHost();
    
    /**
     * Incoming request dispatcher.
     */
    private final class Dispatcher implements Runnable {

	/**
	 * Dispatch loop.
	 */
	public void run() {
	    Thread.currentThread().setName("HTTP dispatcher");
	    try {
		for (;;) {
		    idle();

		    MessageReader reader = new MessageReader(in, false);
		    StartLine sline;
		    try {
			sline = reader.readStartLine();
		    } catch (IOException ex) {
			if (ex.getMessage().equals("empty start line")) 
			    continue;
			throw ex;
		    }

		    busy();
		    synchronized (stateLock) {
			if (state == CLOSED) {
			    return;
			}
			state = BUSY;
		    }

		    Header header = reader.readHeader();
		    if ("POST".equals(sline.method)) {
			handleRequest(sline, header, reader);
		    } else {
			handleBadRequest(sline, header, reader);
		    }
		    
		    synchronized (stateLock) {
			if (state == CLOSED) {
			    return;
			}
			state = IDLE;
		    }
		}
	    } catch (IOException ex) {
		// ex.printStackTrace();
	    } finally {
		shutdown(true);
	    }
	}

	/**
	 * Handles unacceptable HTTP request.
	 */
	private void handleBadRequest(StartLine inLine, Header inHeader,
				      MessageReader reader)
	    throws IOException
	{
	    inHeader.merge(reader.readTrailer());
	    boolean persist = supportsPersist(inLine, inHeader);

	    MessageWriter writer = new MessageWriter(out, false);
	    if ("M-POST".equals(inLine.method)) {
		writer.writeStartLine(new StartLine(HTTP_MAJOR, HTTP_MINOR,
				  // HttpURLConnection.HTTP_NOT_IMPLEMENTED, 
				      501,
				      "Not Implemented."));
	    } else {
		writer.writeStartLine(new StartLine(HTTP_MAJOR, HTTP_MINOR,
				      HttpURLConnection.HTTP_BAD_REQUEST, 
				      "Bad Request"));
	    }
	    writer.writeHeader(createResponseHeader(new Header(), persist));
	    writer.writeTrailer(null);
	    
	    if (! persist) {
		shutdown(true);
	    }
	}

	/**
	 * Handles "standard" (i.e., dispatchable) request.
	 */
	private void handleRequest(StartLine inLine, Header inHeader,
				   MessageReader reader)
        throws IOException {

	    boolean persist = supportsPersist(inLine, inHeader);
	    boolean chunk = supportsChunking(inLine, inHeader);
	    MessageWriter writer = new MessageWriter(out, chunk);
	    InboundRequestImpl req = new InboundRequestImpl(reader, 
						       writer, inHeader);
	    if (!handler.checkAuthentication(req)) {
		writer.writeStartLine(new StartLine(inLine.major, 
						    inLine.minor,
			  HttpURLConnection.HTTP_UNAUTHORIZED, "Unauthorized"));
		Header hd = req.getRespondHeader();
		writer.writeHeader(hd);	
		writer.writeTrailer(null);
		return;		
	    }

	    handler.addResponseHeaderFields(req);

	    writer.writeStartLine(new StartLine(inLine.major, inLine.minor,
				      HttpURLConnection.HTTP_OK, "OK"));
	    writer.writeHeader(createResponseHeader(req.getRespondHeader(),
						    persist));
	   
	    try {
		handler.handleRequest(req); 
	    } catch (Throwable th) {
		System.out.println("throwale th:");
	    }
	    req.finish();
	    
	    if ((! persist) || req.streamCorrupt()) {
		shutdown(true);
	    }
	}

    }

    /**
     * HTTP-based implementation of InboundRequest abstraction.
     */
    private final class InboundRequestImpl 
	extends Request implements InboundRequest 
    {
	private MessageReader reader;
	private MessageWriter writer;
	private boolean corrupt = false;
	private Header header;
	private Header respondHeader = new Header();
	/**
	 * Creates new InboundRequestImpl which uses given MessageReader and
	 * MessageWriter instances to read/write request content.
	 */
	InboundRequestImpl(MessageReader reader, MessageWriter writer,
			   Header header) {
	    this.reader = reader;
	    this.writer = writer;
	    this.header = header;
	}
	
	public void checkPermissions() {
	    HttpServerConnection.this.checkPermissions();
	}
	
	public String getClientHost() {
	    return HttpServerConnection.this.getClientHost();
	}
	
	public InputStream getRequestInputStream() {
	    return getInputStream();
	}
	
	public OutputStream getResponseOutputStream() {
	    return getOutputStream();
	}
	
	/**
	 * Returns value associated with named field, or null
	 * if field not present in this header.
	 */
	public String getHeaderField(String name) {

	    return header.getField(name);

	}	    
	
	/**
	 * set the value associated with named field, or null
	 * if field not present in the respond header.
	 */
	public void setRespondHeaderField(String name, String value) {
	    respondHeader.setField(name, value);

	}

	Header getRespondHeader() {
	    return respondHeader;

	}
	/**
	 * Returns true if stream corrupted, false if stream ok.
	 */
	boolean streamCorrupt() {
	    return corrupt;
	}

	void startOutput() throws IOException {
	    // start line, header already written
	}

	void write(byte[] b, int off, int len) throws IOException {
	    writer.writeContent(b, off, len);
	}

	void endOutput() throws IOException {
		writer.writeTrailer(null);
	}

	boolean startInput() throws IOException {
	    return true;	// header already read
	}

	int read(byte[] b, int off, int len) throws IOException {
	    return reader.readContent(b, off, len);
	}

	int available() throws IOException {
	    return reader.availableContent();
	}

	void endInput() throws IOException {

	}

	void done(boolean corrupt) {
	    this.corrupt = corrupt;
	}
	

    }
    
    /**
     * Returns true if the received message start line indicates that the
     * sender understands chunked transfer coding.
     */
    private static boolean supportsChunking(StartLine sline, Header header) {
	int c = StartLine.compareVersions(sline.major, sline.minor, 1, 1);
	// REMIND: is requiring "TE: trailers" too strict?
	return ((c >= 0) && (header.containsValue("TE", "trailers", true)));
    }
    
    /**
     * Creates base header to be used in response message.  If persist is true,
     * adds fields indicating a persistent connection.
     */
    private static Header createResponseHeader(Header header, boolean persist) {
	long now = System.currentTimeMillis();
	header.setField("Date", Header.getDateString(now));
	header.setField("Server", serverString);
	header.setField("Connection", persist ? "Keep-Alive" : "close");
	return header;
    }
}
