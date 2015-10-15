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

package javax.wbem.client.adapter.http;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import javax.wbem.cim.CIMException;
import javax.wbem.client.CIMListener;
import javax.wbem.client.ClientProperties;
import javax.wbem.client.adapter.http.transport.HttpServerEndpoint;
import javax.wbem.client.adapter.http.transport.ServerEndpointListener;

/* 
 * A simple HTTP server as a CIMListener to receive and process
 * CIM Export Message requests and issue CIM Export Message responses.
 */
class HttpEventListener
{
    
    private String servername;
    
    private String serverIP;
    
    private int port;
    
    private HttpServerEndpoint serverEndPoint;
    
    private ServerEndpointListener listener = null;
    
    private final String LOOPBACK_ADDRESS = "127.0.0.1";
    
    private static String localIP = null;
    
    CIMListener clientListener;
    
    List<CIMListener> clientListeners = new ArrayList<CIMListener>();
    
    private boolean isInit = false;
    
    public void init(CIMListener clientListener, int port) throws Exception
    {
        try
        {
            servername = InetAddress.getLocalHost().getHostName();
        }
        catch (Exception ex)
        {
            servername = "??";
        }
        
        try
        {
            serverEndPoint = new HttpServerEndpoint(servername, port);
        }
        catch (Exception ex)
        {
            throw new CIMException("XMLERROR", "HttpServerEndpoint", ex);
            
        }
        
        serverIP = getLocalIPAddress();
        
        synchronized (this)
        {
            this.clientListeners.add(clientListener);
        }
        this.start(port);
        
        isInit = true;
    }
    
    public boolean isInit()
    {
        return isInit;
    }
    
    public void addCIMListener(CIMListener listener)
    {
        synchronized (this)
        {
            this.clientListeners.add(listener);
            HttpEventRequestHandler handler = HttpEventRequestHandlerMgr.getHttpEventRequestHandler(this.port);
            handler.addCIMListener(listener);
        }
    }
    
    HttpEventListener()
    {
        
    }
    
    /**
     * Construct a server.  Use the start method to run it.
     *
     * @param clientListener the listener
     */
    HttpEventListener(CIMListener clientListener) throws Exception
    {
        this(clientListener, 0);
    }
    
    /**
     * Construct a server.  Use the start method to run it.
     *
     * @param clientListener the listener
     */
    HttpEventListener(CIMListener clientListener, int port) throws Exception
    {
        //        try
        //        {
        //            servername = InetAddress.getLocalHost().getHostName();
        //        }
        //        catch (Exception ex)
        //        {
        //            servername = "??";
        //        }
        //        
        //        try
        //        {
        //            serverEndPoint = new HttpServerEndpoint(servername, port);
        //        }
        //        catch (Exception ex)
        //        {
        //            throw new CIMException("XMLERROR", "HttpServerEndpoint", ex);
        //            
        //        }
        //        
        //        serverIP = getLocalIPAddress();
        //        
        //        this.clientListener = clientListener;
        //        this.start();
        this.init(clientListener, port);
    }
    
    private synchronized void start(int port) throws Exception
    {
        try
        {
            HttpEventRequestHandler handler = HttpEventRequestHandlerMgr.getHttpEventRequestHandler(port);
            if (!handler.isInit())
            {
                handler.init(clientListeners);
            }
            else
            {
                handler.addCIMListener(clientListeners);
            }
            listener = serverEndPoint.listen(handler);
            this.port = listener.getLocalPort();
            serverIP = listener.getLocalIP();
        }
        catch (IOException e)
        {
            listener = null;
            throw e;
        }
    }
    
    synchronized void  stop()
    {
        if (listener != null)
        {
            listener.close();
        }
    }
    
    String getHost()
    {
        return servername;
    }
    
    void setHost(String host)
    {
        servername = host;
    }
    
    String getHostIP()
    {
        return serverIP;
    }
    
    void setHostIP(String hostIP)
    {
        serverIP = hostIP;
    }
    
    int getPort()
    {
        return port;
    }
    
    void setPort(int port)
    {
        this.port = port;
    }
    
    // Get the IP address of the local machine. 
    private String getLocalIPAddress()
    {
        // if we've already found local IP, return it
        if (localIP != null)
        {
            return localIP;
        }
        try
        {
            // set the preferIPv4 variable is not already set
            if (System.getProperty("java.net.preferIPv4Stack") == null)
            {
                String preferIPv4 = ClientProperties.getProperty("java.net.preferIPv4Stack") == null ? "true"
                        : ClientProperties.getProperty("java.net.preferIPv4Stack");
                System.setProperty("java.net.preferIPv4Stack", preferIPv4);
            }
            // get all network interfaces
            Enumeration e = NetworkInterface.getNetworkInterfaces();
            while (e.hasMoreElements())
            {
                NetworkInterface ni = (NetworkInterface) e.nextElement();
                Enumeration e1 = ni.getInetAddresses();
                // get all InetAddresses for each NetworkInterface
                while (e1.hasMoreElements())
                {
                    InetAddress inetAddress = (InetAddress) e1.nextElement();
                    String address = inetAddress.getHostAddress();
                    // if the address is not null, empty or the loopback 
                    // address, set static localIP variable and return it
                    if ((address != null) && (address.trim().length() != 0)
                            && (!address.equalsIgnoreCase(LOOPBACK_ADDRESS)))
                    {
                        localIP = address;
                        return address;
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            // ignore, use loopback
        }
        // couldn't find valid local ip address.  Set static local ip address
        // to loopback address and return it.
        localIP = LOOPBACK_ADDRESS;
        return localIP;
    }
    
}
