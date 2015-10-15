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
 *Contributor(s): Brian Schlosser
*/

package javax.wbem.client.adapter.http.transport;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Class representing a client-side HTTP connection used to send HTTP requests.
 */
public class HttpConnection implements TimedConnection {
    
    protected static final int HTTP_MAJOR = 1;
    protected static final int HTTP_MINOR = 1;
    
    /* states */
    protected static final int UNSTARTED = 0;
    protected static final int IDLE      = 1;
    protected static final int BUSY      = 2;
    protected static final int CLOSED    = 3;

    protected int    state;
    protected Object stateLock = new Object();

    protected Socket       sock;
    protected OutputStream out;
    protected InputStream  in;

    public HttpConnection(int state) {
        this.state = state;
    }
    
    /**
     * Upcall indicating that connection has become idle.  Subclasses may
     * override this method to perform an appropriate action, such as
     * scheduling an idle timeout.
     */
    protected void idle() {
    }
    
    /**
     * Upcall indicating that connection is about to become busy.  Subclasses
     * may override this method to perform an appropriate action, such as
     * cancelling an idle timeout.
     */
    protected void busy() {
    }
    
    /**
     * Attempts to shut down connection, returning true if connection is
     * closed.  If force is true, connection is always shut down; if force is
     * false, connection is only shut down if idle.
     */
    public boolean shutdown(boolean force) {
	synchronized (stateLock) {
	    if (state == CLOSED) {
		return true;
	    }
	    if ((! force) && (state == BUSY)) {
		return false;
	    }
	    state = CLOSED;
	}
	disconnect();
	return true;
    }
    
    /**
     * Marks connection busy.  Throws IOException if connection closed.
     */
    protected void markBusy() throws IOException {
	synchronized (stateLock) {
	    if (state == CLOSED) {
		throw new IOException("connection closed");
	    }
	    state = BUSY;
	}
    }
    
    /**
     * Marks connection idle.  Does nothing if connection closed.
     */
    protected void markIdle() {
	synchronized (stateLock) {
	    if (state == CLOSED) {
		return;
	    }
	    state = IDLE;
	}
	idle();
    }

    /**
     * Closes and releases reference to underlying socket.
     */
    protected void disconnect() {
	if (sock != null) {
	    try { 
	        sock.close(); 
            } catch (IOException ex) {}
	    sock = null;
	    out = null;
	    in = null;
	}
    }

    /**
     * Returns true if the given response line and header indicate that the
     * connection can be persisted.
     */
    protected static boolean supportsPersist(StartLine sline, Header header) {
	if (header.containsValue("Connection", "close", true)) {
	    return false;
	} else if (header.containsValue("Connection", "Keep-Alive", true)) {
	    return true;
	} else {
	    int c = StartLine.compareVersions(sline.major, sline.minor, 1, 1);
	    return (c >= 0);
	}
    }
}
