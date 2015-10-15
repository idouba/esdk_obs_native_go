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


import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import javax.wbem.client.Version;

/**
 * Class representing a client-side HTTP connection used to send HTTP requests.
 */
public class HttpClientConnection extends HttpConnection {
    
    private static final String clientString = Version.productName + " " + 
    Version.major + "." + Version.minor + "." + Version.patch;

    /* modes */
    private static final int DIRECT   = 0;
    private static final int PROXIED  = 1;
    private static final int TUNNELED = 2;
    
    private final int mode;

    private ServerInfo targetInfo;
    private ServerInfo proxyInfo;
    private final HttpClientManager manager;

    private final boolean useMPost;
    private final boolean chunkingDisabled;

    /**
     * Creates HttpClientConnection which sends requests directly to given
     * host/port through a socket obtained from the given socket factory.
     */
    public HttpClientConnection(String host, int port,
                HttpClientSocketFactory factory,
                boolean useMPost)
        throws IOException {
        
        this(host, port, factory, useMPost, false);
    }
    
    /**
     * Creates HttpClientConnection which sends requests directly to given
     * host/port through a socket obtained from the given socket factory.
     */
    public HttpClientConnection(String host, int port,
                HttpClientSocketFactory factory,
                boolean useMPost, boolean useChunking)
        throws IOException {

        super(IDLE);
        
    this.useMPost = useMPost;
        this.chunkingDisabled = !useChunking;
    this.manager = new HttpClientManager();
    mode = DIRECT;
    targetInfo = manager.getServerInfo(host, port);
    } 
    /**
     * Creates HttpClientConnection which sends requests to given target
     * host/port through the indicated HTTP proxy over a socket provided by the
     * specified socket factory.  If tunnel is true, requests are tunneled
     * through the proxy over an additional layered socket (also provided by
     * the socket factory).
     */
    public HttpClientConnection(String targetHost, int targetPort,
                String proxyHost, int proxyPort,
                boolean tunnel,
                HttpClientSocketFactory factory,
                HttpClientManager manager)
        throws IOException {
        
        super(IDLE);

        this.manager = manager;
        this.chunkingDisabled = false;
        this.useMPost = false;
    mode = tunnel ? TUNNELED : PROXIED;
    targetInfo = manager.getServerInfo(targetHost, targetPort);
    proxyInfo = manager.getServerInfo(proxyHost, proxyPort);
    setupConnection(factory);
    }
    
    /**
     * Pings target.  Returns true if ping succeeded, false if it fails
     * "cleanly" (i.e., if a valid HTTP response indicating request failure is
     * received).
     */
    public boolean ping() throws IOException {
    markBusy();
    fetchServerInfo();
    try {
        return ping(false);
    } finally {
        markIdle();
    }
    }
    
    /**
     * Initiates new request to connection target.  Throws an IOException if
     * the connection is currently busy.
     */
    public OutboundRequest newRequest(String URLPath) throws IOException {
    OutboundRequest req = null;
    markBusy();
    fetchServerInfo();
    try {
        req = new OutboundRequestImpl(URLPath);
        return req;
    } finally {
        if (req == null) {
        markIdle();
        }
    }
    }
    
    /**
     * Fetches latest server/proxy HTTP information from cache.
     */
    private void fetchServerInfo() {
    ServerInfo sinfo;

    sinfo = manager.getServerInfo(targetInfo.host, targetInfo.port);
    if (sinfo.timestamp > targetInfo.timestamp) {
        targetInfo = sinfo;
    }
    
    if (mode != DIRECT) {
        sinfo = manager.getServerInfo(proxyInfo.host, proxyInfo.port);
        if (sinfo.timestamp > proxyInfo.timestamp) {
        proxyInfo = sinfo;
        }
    }
    }
    
    /**
     * Flushes current copy of server/proxy HTTP information to cache.
     */
    private void flushServerInfo() {
    manager.cacheServerInfo(targetInfo);
    if (mode != DIRECT) {
        manager.cacheServerInfo(proxyInfo);
    }
    }
    
    /**
     * Establishes connection using sockets from the given socket factory.
     * Throws IOException if connection setup fails.
     */
    public void setupConnection(HttpClientSocketFactory factory)
           throws IOException {
        try {
            /*
             * 4 cycles required in worst-case (proxied) scenario:
             * i = 0: send OPTIONS request to proxy
             * i = 1: send ping, fails with 407 (proxy auth required)
             * i = 2: send ping, fails with 401 (unauthorized)
             * i = 3: return
             */
            for (int i = 0; i < 4; i++) {
                if (sock == null) {
                    connect(factory);
                }
                if ((mode == PROXIED) &&
                        (proxyInfo.timestamp == ServerInfo.NO_TIMESTAMP)) {
                    requestProxyOptions();
                } else if (targetInfo.timestamp == ServerInfo.NO_TIMESTAMP) {
                    ping(true);
                } else {
                    return;
                }
            }
        } catch (UnknownHostException nex) {
            disconnect();
            throw new ConnectException("Unknown host: " + nex.getMessage());
        } catch (ConnectException nex) {
            disconnect();
            ServerInfo sinfo = (mode == DIRECT) ? targetInfo : proxyInfo;
            throw new ConnectException(nex.getMessage() + " - " + sinfo.host + ":" + sinfo.port);
        } catch (Throwable th) {
            disconnect();
            throw new ConnectException(th.getClass().getName() + " - " + th.getMessage());
        }
    }
    
    /**
     * Opens underlying connection.  If tunneling through an HTTP proxy,
     * attempts CONNECT request.
     */
    private void connect(HttpClientSocketFactory factory) throws IOException {
    disconnect();
    for (int i = 0; i < 2; i++) {
        if (sock == null) {
        ServerInfo sinfo = (mode == DIRECT) ? targetInfo : proxyInfo;
        sock = factory.createSocket(sinfo.host, sinfo.port);
        
        // begin modified by l90002863 If the socket is not connected to specified host connect to it. 
        if(!sock.isConnected())
        {
            sock.connect(new InetSocketAddress(sinfo.host,sinfo.port));
        }
        // end modified
        
        /* add by fujunguang set timeout 30 seconds*/
        /* close because deal with time out in ism connet,
         * if don't close wbem can not deal with command more than 30 seconds */
        //sock.setSoTimeout(30000);
        /* end by fujunguang set timeout 30 seconds*/
        out = new BufferedOutputStream(sock.getOutputStream());
        in = new BufferedInputStream(sock.getInputStream());
        }
        if (mode != TUNNELED) {
        return;
        }
        if (requestProxyConnect()) {
        sock = factory.createTunnelSocket(sock);
        /* add by fujunguang set timeout 30 seconds*/
        /* close because deal with time out in ism connet,
         * if don't close wbem can not deal with command more than 30 seconds */
        //sock.setSoTimeout(30000);
        /* end by fujunguang set timeout 30 seconds*/
        out = new BufferedOutputStream(sock.getOutputStream());
        in = new BufferedInputStream(sock.getInputStream());
        return;
        }
    }
    }
    
    /**
     * Pings target.  Returns true if succeeded, false if failed "cleanly".
     */
    private boolean ping(boolean setup) throws IOException {
    StartLine outLine = createPostLine(null);
    Header outHeader = createPostHeader(outLine);
    MessageWriter writer = new MessageWriter(out, false);
    

    writer.writeStartLine(outLine);
    writer.writeHeader(outHeader);
    writer.flush();

    //NOTE: DO NOT send any payload with this POST.
    //  This is used to determine server support
        
    writer.writeTrailer(null);

    MessageReader reader = new MessageReader(in, false);
    StartLine inLine = reader.readStartLine();
    Header inHeader = reader.readHeader();
    inHeader.merge(reader.readTrailer());
        
    analyzePostResponse(inLine, inHeader);
    if (! supportsPersist(inLine, inHeader)) {
        if (setup) { 
        disconnect();
        } else {
        shutdown(true);
        }
    }
    return ((inLine.status / 100) == 2);
    }
    
    /**
     * Sends OPTIONS request to proxy.  Returns true if OPTIONS succeeded,
     * false otherwise.
     */
    private boolean requestProxyOptions() throws IOException {
    MessageWriter writer = new MessageWriter(out, false);
    writer.writeStartLine(
        new StartLine(HTTP_MAJOR, HTTP_MINOR, "OPTIONS", "*"));
    writer.writeHeader(createProxyHeader());

    writer.writeTrailer(null);

    MessageReader reader = new MessageReader(in, false);
    StartLine inLine = reader.readStartLine();
    Header inHeader = reader.readHeader();
    inHeader.merge(reader.readTrailer());
    analyzeProxyResponse(inLine, inHeader);

    if (! supportsPersist(inLine, inHeader)) {
        disconnect();
    }
    return ((inLine.status / 100) == 2);
    }
    
    /**
     * Sends CONNECT request to proxy.  Returns true if CONNECT succeeded,
     * false otherwise.
     */
    private boolean requestProxyConnect() throws IOException {
    StartLine outLine = new StartLine(
        HTTP_MAJOR, HTTP_MINOR, "CONNECT",
        targetInfo.host + ":" + targetInfo.port);

    // REMIND: eliminate hardcoded protocol string
    Header outHeader = createProxyHeader();
    String auth = 
        proxyInfo.getAuthString("http", outLine.method, outLine.uri);
    if (auth != null) {
        outHeader.setField("Proxy-Authorization", auth);
    }

    MessageWriter writer = new MessageWriter(out, false);
    writer.writeStartLine(outLine);
    writer.writeHeader(outHeader);
    writer.writeTrailer(null);
    
    MessageReader reader = new MessageReader(in, true);
    StartLine inLine = reader.readStartLine();
    Header inHeader = reader.readHeader();
    inHeader.merge(reader.readTrailer());
    analyzeProxyResponse(inLine, inHeader);
    
    if ((inLine.status / 100) == 2) {
        return true;
    }
    if (! supportsPersist(inLine, inHeader)) {
        disconnect();
    }
    return false;
    }
    
    /**
     * Creates start line for outbound HTTP M-POST or POST message.
     */
    private StartLine createPostLine(String URLPath) {
    String methodName = useMPost ? "M-POST" : "POST";
        String path = URLPath;
        if (path == null || path.length() == 0) {
            path = "/";
        }
    String uri = (mode == PROXIED) ?
        "http://" + targetInfo.host + ":" + targetInfo.port + path : path;
    return new StartLine(HTTP_MAJOR, HTTP_MINOR, methodName, uri);
    }
    
    /**
     * Creates base header containing fields common to all HTTP messages sent
     * by this connection.
     */
    private Header createBaseHeader() {
    Header header = new Header();
    long now = System.currentTimeMillis();
    header.setField("Date", Header.getDateString(now));
    header.setField("User-Agent", clientString);
    return header;
    }

    /**
     * Creates header for use in OPTIONS and CONNECT messages sent to proxies.
     */
    private Header createProxyHeader() {
    Header header = createBaseHeader();
    header.setField("Host", proxyInfo.host + ":" + proxyInfo.port);
    return header;
    }
    
    /**
     * Creates header for outbound HTTP POST message with given start line.
     */
    private Header createPostHeader(StartLine sline) {
    Header header = createBaseHeader();
    header.setField("Host", targetInfo.host + ":" + targetInfo.port);
    header.setField("Connection", "Keep-Alive");
    // header.setField("TE", "trailers");
    
    // REMIND: eliminate hardcoded protocol string
    String auth = 
        targetInfo.getAuthString("http", sline.method, sline.uri);
    if (auth != null) {
        header.setField("Authorization", auth);
    }
    if (mode == PROXIED) {
        auth = proxyInfo.getAuthString("http", sline.method, sline.uri);
        if (auth != null) {
        header.setField("Proxy-Authorization", auth);
        }
    }

    return header;
    }
    
    /**
     * Analyzes POST response message start line and header, updating cached
     * target/proxy server information if necessary.
     */
    private void analyzePostResponse(StartLine inLine, Header inHeader) {
    String str;
    long now = System.currentTimeMillis();
    
    if ((str = inHeader.getField("WWW-Authenticate")) != null) {
        try {
        targetInfo.setAuthInfo(str);
        } catch (HttpParseException ex) {
        }
        targetInfo.timestamp = now;
    } else if ((str = inHeader.getField("Authentication-Info")) != null) {
        try {
        targetInfo.updateAuthInfo(str);
        } catch (HttpParseException ex) {
        }
        targetInfo.timestamp = now;
    }
    
    if (mode != DIRECT) {
        if ((str = inHeader.getField("Proxy-Authenticate")) != null) {
        try {
            proxyInfo.setAuthInfo(str);
        } catch (HttpParseException ex) {
        }
        proxyInfo.timestamp = now;
        } else if ((str = inHeader.getField(
                "Proxy-Authentication-Info")) != null)
        {
        try {
            proxyInfo.updateAuthInfo(str);
        } catch (HttpParseException ex) {
        }
        proxyInfo.timestamp = now;
        }
    }

    if (mode != PROXIED) {
        targetInfo.major = inLine.major;
        targetInfo.minor = inLine.minor;
        targetInfo.timestamp = now;
    } else {
        // Return message was sent by proxy; however, since some proxies
        // incorrectly relay the target server's version numbers instead of
        // their own, we can only rely on version numbers which could not
        // have been sent from target server.
        //
        if (inLine.status == HttpURLConnection.HTTP_PROXY_AUTH) {
        proxyInfo.major = inLine.major;
        proxyInfo.minor = inLine.minor;
        }
        proxyInfo.timestamp = now;
    }
    
    if ((inLine.status / 100) == 2) {
        targetInfo.timestamp = now;
    }

    flushServerInfo();
    }
    
    /**
     * Analyzes CONNECT or OPTIONS response message start line and header sent
     * by proxy, updating proxy server information if necessary.
     */
    private void analyzeProxyResponse(StartLine inLine, Header inHeader) {
    proxyInfo.major = inLine.major;
    proxyInfo.minor = inLine.minor;
    proxyInfo.timestamp = System.currentTimeMillis();
    
    String str;
    if ((str = inHeader.getField("Proxy-Authenticate")) != null) {
        try {
        proxyInfo.setAuthInfo(str);
        } catch (HttpParseException ex) {
        }
    } else if ((str = inHeader.getField(
            "Proxy-Authentication-Info")) != null)
    {
        try {
        proxyInfo.updateAuthInfo(str);
        } catch (HttpParseException ex) {
        }
    }

    flushServerInfo();
    }

    /**
     * Returns true if requests sent over this connection should chunk output.
     */
    public boolean supportsChunking() {
        if (chunkingDisabled) {
            return false;
        }
    ServerInfo si = (mode == PROXIED) ? proxyInfo : targetInfo;
    return (StartLine.compareVersions(si.major, si.minor, 1, 1) >= 0);
    }
    
    /**
     * HTTP-based implementation of OutboundRequest abstraction.
     */
    private final class OutboundRequestImpl 
    extends Request implements OutboundRequest
    {
    private MessageWriter writer;
    private MessageReader reader;
    private StartLine inLine;
    private Header inHeader;
    private boolean persist = false;
    private Header outHeader;
    private StartLine outLine;
    OutboundRequestImpl(String URLPath) {
        writer = new MessageWriter(out, supportsChunking());
        reader = new MessageReader(in, false);
        outLine = createPostLine(URLPath);
        outHeader = createPostHeader(outLine);
        outHeader.setField("Content-Type", "text/xml;charset=UTF-8");
        outHeader.setField("Accept", "text/xml, application/xml");
    }

    public int getResponseCode() {
        return inLine.status;
    }

    public void addHeaderField(String name, String value) {
        outHeader.setField(name, value);

    }

    public void endWriteHeader() throws IOException {
        writer.writeStartLine(outLine);
        writer.writeHeader(outHeader);
        // writer.flush();
    }

    public OutputStream getRequestOutputStream() {
        return getOutputStream();
    }
    
    public InputStream getResponseInputStream() {
        return getInputStream();
    }

    public void dumpOutHeader(PrintStream psout) throws java.io.IOException {
        outLine.write(psout);
        outHeader.write(psout);
    }

    public void dumpInHeader(PrintStream psout) throws java.io.IOException {
        inLine.write(psout);
        inHeader.write(psout);
    }


    void startOutput() {
        // start line, header already written
    }

    void write(byte[] b, int off, int len) throws IOException {
        writer.writeContent(b, off, len);
    }

    void endOutput() throws IOException {
        writer.writeTrailer(null);
    }

    boolean startInput() throws IOException {
        inLine = reader.readStartLine();
        inHeader = reader.readHeader();
        return ((inLine.status / 100) == 2);
    }

    int read(byte[] b, int off, int len) throws IOException {
        return reader.readContent(b, off, len);
    }

    int available() throws IOException {
        return reader.availableContent();
    }

    void endInput() throws IOException {
        inHeader.merge(reader.readTrailer());
        analyzePostResponse(inLine, inHeader);
        persist = supportsPersist(inLine, inHeader);
    }

    void done(boolean corrupt) {
        if (corrupt || (! persist)) {
        shutdown(true);
        } else {
        markIdle();
        }
    }
    }
}
