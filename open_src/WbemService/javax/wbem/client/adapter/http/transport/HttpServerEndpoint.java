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
 *Contributor(s):   WBEM Solutions, Inc.
 */

package javax.wbem.client.adapter.http.transport;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import javax.wbem.client.Debug;

import socks.SocksServerSocket;

public final class HttpServerEndpoint
{
    
    /**
     * pool of threads for executing tasks in system thread group:
     * used for socket accept threads
     */
    private static final ExecutorService systemThreadPool = (ExecutorService) java.security.AccessController.doPrivileged(new GetThreadPoolAction());
    
    /** idle connection timer */
    private static final ConnectionTimer connTimer;
    static
    {
        HttpSettings hs = HttpSettings.getHttpSettings();
        connTimer = new ConnectionTimer(hs.getConnectionTimeout(15000));
    }
    
    /** local host name to fill in to corresponding HttpEndpoints */
    private final String h;
    
    /** port to listen on */
    private int p;
    
    /** if we should use SSL */
    private boolean useSSL = false;
    
    /**
     * Constructs a new instance.
     */
    public HttpServerEndpoint(String host, int port)
    {
        this(host, port, false);
    }
    
    /**
     * Constructs a new instance.
     */
    public HttpServerEndpoint(String host, int port, boolean ssl)
    {
        useSSL = ssl;
        if (useSSL)
        {
            Debug.trace3("HttpServerEndpoint: using SSL");
        }
        if (host == null)
        {
            throw new NullPointerException();
        }
        if (port < 0 || port > 0xFFFF)
        {
            throw new IllegalArgumentException("port number out of range: "
                    + port);
        }
        h = host;
        p = port;
    }
    
    /**
     * Returns the host name that will be used for
     * <code>HttpEndpoint</code> instances created from this object.
     *
     * @return	the host name used by corresponding <code>HttpEndpoint</code>
     * instances
     */
    public String getHost()
    {
        return h;
    }
    
    /**
     * Returns the TCP port that this <code>HttpServerEndpoint</code>
     * listens on.
     *
     * @return	the TCP port that this endpoint listens on
     */
    public int getPort()
    {
        return p;
    }
    
    public ServerEndpointListener listen(RequestHandler handler)
            throws IOException
    {
        if (handler == null)
        {
            throw new NullPointerException();
        }
        
        ServerSocket ssock;
        // if useSSL is true, use secure sockets
        if (useSSL)
        {
            ServerSocketFactory ssf = SSLServerSocketFactory.getDefault();
            ssock = ssf.createServerSocket(p);
        }
        else
        {
            // begin modified by x00102290  add use SOCKS5 proxy support
            String userSOCKS5Proxy = System.getProperty("proxy.useSOCKS5",
                    "false");
            if (userSOCKS5Proxy.equalsIgnoreCase("true"))
            {
                String targetIP = System.getProperty("proxy.targetIP",
                        "0.0.0.0");
                try
                {
                    ssock = new SocksServerSocket(targetIP, p);
                }
                catch (Throwable e)
                {
                    ssock = new ServerSocket(p);
                }
            }
            else
            {
                
                ssock = new ServerSocket(p);
            }
            // end modified by x00102290
        }
        
        Listener listener = new Listener(handler, ssock,
                "HttpServerEndpoint Accept (port " + p + ")");
        systemThreadPool.execute(listener);
        return listener;
    }
    
    public boolean equals(Object obj)
    {
        if (obj instanceof HttpServerEndpoint)
        {
            HttpServerEndpoint other = (HttpServerEndpoint) obj;
            return h.equals(other.h) && p == other.p;
        }
        else
        {
            return false;
        }
    }
    
    /**
     * Returns a string representation for this HTTP server endpoint.
     *
     * @return a string representation for this HTTP server endpoint
     */
    public String toString()
    {
        return "HttpServerEndpoint[" + h + ":" + p + "]";
    }
    
    private final class Listener implements Runnable, ServerEndpointListener
    {
        
        private final RequestHandler handler;
        
        private final ServerSocket ssock;
        
        private boolean closed = false;
        
        private final Set connections = new HashSet(5);
        
        private String name = "";
        
        /**
         * Creates new listener.
         */
        private Listener(RequestHandler handler, ServerSocket ssock, String name)
        {
            this.handler = handler;
            this.ssock = ssock;
            this.name = name;
        }
        
        /**
         * Closes listener.
         */
        public void close()
        {
            Object[] conns;
            synchronized (connections)
            {
                if (closed)
                {
                    return;
                }
                closed = true;
                conns = connections.toArray();
            }
            
            try
            {
                ssock.close();
            }
            catch (IOException ex)
            {
            }
            for (int i = 0; i < conns.length; i++)
            {
                ((Connection) conns[i]).shutdown(true);
            }
            
            this.handler.close();
        }
        
        /**
         * Runs accept loop in listener's access control context.
         */
        public void run()
        {
            Thread.currentThread().setName(name);
            executeAcceptLoop();
        }
        
        /**
         * ServerSocket accept loop.
         */
        private void executeAcceptLoop()
        {
            for (;;)
            {
            	Socket sock = null;
                try
                {
                    sock = ssock.accept();
                    try
                    {
                        sock.setTcpNoDelay(true);
                    }
                    catch (SocketException e)
                    {
                        // ignore possible failures and proceed anyway
                    }
                    try
                    {
                        sock.setKeepAlive(true);
                    }
                    catch (SocketException e)
                    {
                        // ignore possible failures and proceed anyway
                    }
                    
                    synchronized (connections)
                    {
                        if (closed)
                        {
                            try
                            {
                                sock.close();
                            }
                            catch (IOException ex)
                            {
                            }
                        }
                        else
                        {
                            try
                            {
                                connections.add(new Connection(sock, handler,
                                        this));
                            }
                            catch (Exception e)
                            {
                            }
                        }
                    }
                    
                }
                catch (Throwable th)
                {
                	if (null != sock)
                	{
                		try
						{
							sock.close();
						}
						catch (IOException e)
						{
						}
                	}
                    break; // NYI: accept failure strategy
                }
            }
        }
        
        /**
         * Connection shutdown callback.
         */
        void connectionClosed(Connection conn)
        {
            // System.out.println("calling connectionClosed: " + 
            // connections.size());
            synchronized (connections)
            {
                connections.remove(conn);
            }
        }
        
        public int getLocalPort()
        {
            return ssock.getLocalPort();
        }
        
        public String getLocalIP()
        {
            if (null == ssock.getInetAddress())
            {
                throw new NullPointerException("ssock.getInetAddress() is null");
            }
            
            return ssock.getInetAddress().getHostAddress();
        }
    }
    
    /**
     * HTTP connection for receiving requests.
     */
    private final class Connection extends HttpServerConnection
    {
        
        Listener listener;
        
        /**
         * Creates new HTTP connection on given socket that dispatches incoming
         * requests to the provided RequestHandler.
         */
        Connection(Socket sock, RequestHandler handler, Listener listener)
                throws IOException
        {
            super(sock, handler);
            this.listener = listener;
            start();
        }
        
        /**
         * Permission check for dispatched InboundRequest instances.
         */
        protected void checkPermissions()
        {
            SecurityManager sm = System.getSecurityManager();
            if (sm != null)
            {
                String host = null;
                if (sock.getInetAddress() != null)
                {
                    host = sock.getInetAddress().getHostAddress();
                }
                sm.checkAccept(host, sock.getPort());
            }
        }
        
        /**
         * Returns client host string for dispatched InboundRequest instances.
         */
        protected String getClientHost()
        {
            InetAddress addr = sock.getInetAddress();
            return (addr != null) ? addr.getHostAddress() : "0.0.0.0";
        }
        
        /**
         * Schedules idle connection timeout.
         */
        protected void idle()
        {
            connTimer.scheduleTimeout(this, false);
        }
        
        /**
         * Cancels idle connection timeout.
         */
        protected void busy()
        {
            connTimer.cancelTimeout(this);
        }
        
        /**
         * Attempts to close connection.  If successful, removes it from
         * listener's connection set.
         */
        public boolean shutdown(boolean force)
        {
            if (super.shutdown(force))
            {
                connTimer.cancelTimeout(this);
                listener.connectionClosed(this);
            }
            return false;
        }
    }
}
