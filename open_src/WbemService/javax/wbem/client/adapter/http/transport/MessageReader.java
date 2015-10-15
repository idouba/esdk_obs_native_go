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
 *Contributor(s): WBEM Solutions, Inc.
*/

package javax.wbem.client.adapter.http.transport;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.util.StringTokenizer;

/**
 * Class for reading HTTP messages.  Each instance reads a single HTTP message.
 */
final class MessageReader {
    
    /* state values */
    private static final int START   = 0;
    private static final int HEADER  = 1;
    private static final int CONTENT = 2;
    private static final int DONE    = 3;
    
    private int state = START;
    private boolean noContent;
    private StartLine sline;
    private InputStream in;
    private InputStream cin;

    /**
     * Creates new reader on top of given input stream.  If noContent is true,
     * incoming message is assumed to be bodiless (e.g., a HEAD response).
     */
    MessageReader(InputStream in, boolean noContent) {
	this.in = in;
	this.noContent = noContent;
    }
    
    /**
     * Reads in HTTP message start line.
     */
    StartLine readStartLine() throws IOException {
	updateState(START, HEADER);
	sline = new StartLine(in);
	return sline;
    }
    
    /**
     * Reads in HTTP message header.
     */
    Header readHeader() throws IOException {
	updateState(HEADER, CONTENT);
	Header header = new Header(in);

	if ((! noContent) && contentIndicated(sline, header)) {
	    String clen;
	    if (header.containsValue("Transfer-Encoding", "chunked", true)) {
		cin = new ChunkedInputStream(in);
	    } else if ((clen = header.getField("Content-Length")) != null) {
		int len;
		try {
		    len = Integer.parseInt(clen);
		} catch (Exception ex) {
		    throw new HttpParseException("invalid content length");
		}
		if (len < 0) {
		    throw new HttpParseException("invalid content length");
		}
		cin = new BoundedInputStream(in, len);
	    } else if (sline.isRequest) {
		throw new HttpParseException("request length undeclared");
	    } else {
		cin = in;
	    }
	} else {
	    cin = new BoundedInputStream(null, 0);
	}

	return header;
    }
    
    /**
     * Reads message content.
     */
    int readContent(byte[] b, int off, int len) throws IOException {
	updateState(CONTENT, CONTENT);
	return cin.read(b, off, len);
    }
    
    /**
     * Returns count of available message content.
     */
    int availableContent() throws IOException {
	updateState(CONTENT, CONTENT);
	return cin.available();
    }
    
    /**
     * Reads in message trailer after consuming any unread content data.
     * Returns null if message doesn't have a trailer.
     */
    Header readTrailer() throws IOException {
       updateState(CONTENT, CONTENT);


       /* 624872: IndexArray error 
          Trailers are usaully small, but to be sure we 
               will read the number of avail bytes */
       int contentSize = availableContent();
       if (contentSize > -1)
       {
          byte[] sink = new byte[contentSize];
          cin.read(sink,0,availableContent());

          updateState(CONTENT, DONE);
          if (cin instanceof ChunkedInputStream) {
              Header trailer = new Header(in);
              return (trailer.size() > 0) ? trailer : null;
          }
       }
       // there was no trailer data available in the stream
       // or we were not an instanceof ChunkedInputStream
       return null;
    }

    private void updateState(int oldState, int newState) {
	if (state != oldState) {
	    throw new IllegalStateException();
	}
	state = newState;
    }
    
    /**
     * Returns true if given start line and header indicate a content body.
     */
    private static boolean contentIndicated(StartLine sline, Header header) {
	if ((! sline.isRequest) && 
	    (((sline.status / 100) == 1) ||
	    (sline.status == HttpURLConnection.HTTP_NO_CONTENT) ||
	    (sline.status == HttpURLConnection.HTTP_NOT_MODIFIED)))
	{
	    return false;
	}
	
	if ((header.getField("Transfer-Encoding") != null) ||
	    (header.getField("Content-Length") != null))
	{
	    return true;
	}

	return (! sline.isRequest);
    }
    
    /**
     * Input stream for reading bounded content data.
     */
    private static final class BoundedInputStream extends InputStream {
	
	private InputStream in;
	private int bound;
	
	BoundedInputStream(InputStream in, int bound) {
	    this.in = in;
	    this.bound = bound;
	}
	
	public int read() throws IOException {
	    byte[] b = new byte[1];
	    return (read(b, 0, 1) != -1) ? b[0] & 0xFF : -1;
	}
	
	public int read(byte[] b, int off, int len) throws IOException {
	    if (bound == 0) {
		return -1;
	    } else {
		int n = in.read(b, off, Math.min(bound, len));
		if (n != -1) {
		    bound -= n;
		}
		return n;
	    }
	}
	
	public int available() throws IOException {
	    return Math.min(bound, in.available());
	}
    }
    
    /**
     * Input stream for reading chunked content data.
     */
    private static final class ChunkedInputStream extends InputStream {

	private DataInputStream din;
	private byte[] buf;
	private int pos = 0;
	private int lim = 0;
	
	ChunkedInputStream(InputStream in) {
	    din = new DataInputStream(in);
	}

	public int read() throws IOException {
	    byte[] b = new byte[1];
	    return (read(b, 0, 1) != -1) ? b[0] & 0xFF : -1;
	}

	public int read(byte[] b, int off, int len) throws IOException {
	    while (pos >= lim) {
		refill();
	    }
	    if (pos < 0) {
		return -1;
	    }
	    int n = Math.min(lim - pos, len);
	    System.arraycopy(buf, pos, b, off, n);
	    pos += n;
	    return n;
	}
	
	public int available() throws IOException {
	    while (pos >= lim) {
		refill();
	    }
	    return (pos >= 0) ? (lim - pos) : 0;
	}

	private void refill() throws IOException {
	    int newlim = 0;
	    try {
		String line = din.readLine();
		
		// 修改coverity
		if (line == null)
		{
		    throw new HttpParseException("error parsing chunk line");
		}
		StringTokenizer tok = new StringTokenizer(line, " ;\t");
		newlim = Integer.parseInt(tok.nextToken(), 16);
	    } catch (Exception ex) {
		throw new HttpParseException("error parsing chunk length");
	    }
	    
	    if (newlim < 0) {
		throw new HttpParseException("illegal chunk length");
	    } else if (newlim == 0) {
		pos = -1;
	    } else {
		if ((buf == null) || (newlim > buf.length)) {
		    buf = new byte[newlim];
		}
		din.readFully(buf, 0, newlim);
		String blank = din.readLine();
		if ((blank == null) || (blank.length() > 0)) {
		    throw new HttpParseException("illegal chunk tail");
		}
		pos = 0;
		lim = newlim;
	    }
	}
    }
}
