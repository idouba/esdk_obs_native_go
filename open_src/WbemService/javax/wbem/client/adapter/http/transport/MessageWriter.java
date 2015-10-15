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

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class for writing HTTP messages.  Each instance writes a single HTTP
 * message.
 */
final class MessageWriter {
    
    /* state values */
    private static final int START   = 0;
    private static final int HEADER  = 1;
    private static final int CONTENT = 2;
    private static final int DONE    = 3;
    
    private int state = START;
    private OutputStream out;
    private OutputStream cout;
    private Header header;

    /**
     * Creates new writer on top of given output stream.
     */
    MessageWriter(OutputStream out, boolean chunked) {
	this.out = out;
	cout = chunked ? (OutputStream) new ChunkedOutputStream(out, 512)
		       : (OutputStream) new ByteArrayOutputStream();
    }
    
    /**
     * Writes HTTP message start line.
     */
    void writeStartLine(StartLine line) throws IOException {
	updateState(START, HEADER);
	line.write(out);
    }
    
    /**
     * "Writes" HTTP message header (the header may not actually be written
     * until after the message content length is known).  The caller should
     * avoid using the passed header after invoking this method.
     */
    void writeHeader(Header header) throws IOException {
	updateState(HEADER, CONTENT);
	if (cout instanceof ChunkedOutputStream) {
	    header.setField("Transfer-Encoding", "chunked");
	    header.setField("Content-Length", null);
	    header.write(out);
	} else {
	    this.header = header;
	}
    }
    
    /**
     * Writes message content.
     */
    void writeContent(byte[] b, int off, int len) throws IOException {
	updateState(CONTENT, CONTENT);
	cout.write(b, off, len);
    }
    
    /**
     * Writes message trailer (if not using chunked output, merges trailer with
     * header before writing), completing message output.  Flushes underlying
     * output stream once trailer has been written.
     */
    void writeTrailer(Header trailer) throws IOException {
	updateState(CONTENT, DONE);
	cout.close();
	if (cout instanceof ChunkedOutputStream) {
	    if (trailer != null) {
		trailer.write(out);
	    } else {
		new DataOutputStream(out).writeBytes("\r\n");
	    }
	} else {
	    ByteArrayOutputStream bout = (ByteArrayOutputStream) cout;
	    header.merge(trailer);
	    header.setField("Content-Length", Integer.toString(bout.size()));
	    header.setField("Transfer-Encoding", null);
	    header.write(out);
	    bout.writeTo(out);
	}
	out.flush();
    }
    
    /**
     * Flushes written data to underlying output stream.  Throws
     * IllegalStateException if called after message has been fully written.
     */
    void flush() throws IOException {
	if (state == DONE) {
	    throw new IllegalStateException();
	}
	if (state == CONTENT) {
	    cout.flush();
	}
	out.flush();
    }
    
    private void updateState(int oldState, int newState) {
	// System.out.println("state: " + state + "  oldState: " + oldState);
	if (state != oldState) {
	    throw new IllegalStateException();
	}
	state = newState;
    }

    /**
     * Output stream for writing chunked transfer-coded content.
     */
    private static final class ChunkedOutputStream extends OutputStream {

	private DataOutputStream dout;
	private byte[] buf;
	private int pos = 0;
	
	ChunkedOutputStream(OutputStream out, int chunkSize) {
	    dout = new DataOutputStream(out);
	    buf = new byte[chunkSize];
	}
	
	public void write(int val) throws IOException {
	    write(new byte[] { (byte) val }, 0, 1);
	}
	
	public void write(byte[] b, int off, int len) throws IOException {
	    while (len > 0) {
		int avail = buf.length - pos;
		if (avail > 0) {
		    int ncopy = Math.min(len, avail);
		    System.arraycopy(b, off, buf, pos, ncopy);
		    pos += ncopy;
		    off += ncopy;
		    len -= ncopy;
		} else {
		    flush();
		}
	    }
	}
	
	public void flush() throws IOException {
	    if (pos > 0) {
		dout.writeBytes(Integer.toString(pos, 16) + "\r\n");
		dout.write(buf, 0, pos);
		dout.writeBytes("\r\n");
		pos = 0;
	    }
	}

	public void close() throws IOException {
	    flush();
	    dout.writeBytes("0\r\n");
	}
    }
}
