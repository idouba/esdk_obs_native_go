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

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;

import javax.net.SocketFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.wbem.client.adapter.http.proxy.SOCKSProxySocketFactory;

import socks.Proxy;
import socks.Socks5Proxy;
import socks.UserPasswordAuthentication;

/**
 *
 * @author  mh127215
 * @version 
 */
public class HttpSocketFactory implements HttpClientSocketFactory
{
    
    private static SocketFactory fact = null;
    
    private static SocketFactory sslfact = null;
    
    private boolean useSSL = false;
    
    // Create a trust manager that does not validate certificate chains
    /**
     * Modified by f90002221 所有的受信任证书都可用,目前对来自Agent的证书不做检测
     */
    static TrustManager[] trusstManager = new TrustManager[] { new X509TrustManager()
    {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }
        
        public void checkClientTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
        {
        }
        
        public void checkServerTrusted(
                java.security.cert.X509Certificate[] certs, String authType)
        {
            //            //所有的受信任证书都可用,目前对来自Agent的证书不做检测
            //            for (int i = 0; i < certs.length; i++)
            //            {
            //                System.out.println(certs[i]);
            //            }
        }
    } };
    
    /** Creates new socket factory */
    public HttpSocketFactory(String protocol)
    {
        // if protocol is https, create secure socket factory.  Otherwise
        // create regular socket factory
        if (protocol.toLowerCase().equals("https"))
        {
            useSSL = true;
            synchronized (this)
            {
                if (sslfact == null)
                {
                    try
                    {
                        //Modified by f90002221
                        SSLContext ctx = SSLContext.getInstance("SSLv3");
                        
                        // Install the all-trusting trust manager
                        ctx.init(null,
                                trusstManager,
                                new java.security.SecureRandom());
                        
                        // get the socket factory
                        sslfact = ctx.getSocketFactory();
                        if (null == sslfact)
                        {
                            buildSSLFactory();
                        }
                    }
                    catch (Exception ex)
                    {
                        if (sslfact == null)
                        {
                            sslfact = SSLSocketFactory.getDefault();
                        }
                    }
                }
            }
        }
        else
        {
            useSSL = false;
            synchronized (this)
            {
                // first in creates the factory. Others just use it.
                if (fact == null)
                {
                    SimpleDateFormat sDateFormat = new SimpleDateFormat(
                            "yyyy-MM-dd hh:mm:ss");
                    String date = sDateFormat.format(new java.util.Date());
                    System.out.println("Log create SOCKSProxySocketFactory start: "
                            + date);
                    
                    // begin modified by x00102290 add use SOCKS5 proxy support
                    String userSOCKS5Proxy = System.getProperty("proxy.useSocks5",
                            "false");
                    if (userSOCKS5Proxy.equalsIgnoreCase("true"))
                    {
                        String proxyIP = System.getProperty("proxy.IP");
                        String proxyPort = System.getProperty("proxy.Port");
                        String proxyConnUser = System.getProperty("proxy.connUser");
                        String proxyConnPass = System.getProperty("proxy.connPass");
                        
                        try
                        {
                            // modified by h90002262, add user authentication:
                            Socks5Proxy proxy = new Socks5Proxy(proxyIP,
                                    Integer.valueOf(proxyPort).intValue());
                            
                            if (proxyConnUser != null && proxyConnPass != null)
                            {
                                proxy.setAuthenticationMethod(2,
                                        new UserPasswordAuthentication(
                                                proxyConnUser, proxyConnPass));
                            }
                            Proxy.setDefaultProxy(proxy);
                            
                            fact = new SOCKSProxySocketFactory(
                                    Proxy.getDefaultProxy());
                        }
                        catch (Throwable e)
                        {
                            fact = SocketFactory.getDefault();
                        }
                    }
                    else
                    {
                        
                        fact = SocketFactory.getDefault();
                    }
                    // end modified by x00102290
                }
            }
        }
    }
    
    /*
     * Modified by f90002221 所有的受信任证书都可用,目前对来自Agent的证书不做检测,提取该方法：
     * 如果不使用Socket5代理，则构建默认的SSL安全Socket工厂
     */
    private void buildSSLFactory()
    {
        String userSOCKS5Proxy = System.getProperty("proxy.useSOCKS5", "false");
        if (userSOCKS5Proxy.equalsIgnoreCase("true"))
        {
            String proxyIP = System.getProperty("proxy.IP");
            String proxyPort = System.getProperty("proxy.Port");
            String proxyConnUser = System.getProperty("proxy.connUser");
            String proxyConnPass = System.getProperty("proxy.connPass");
            
            try
            {
                Socks5Proxy proxy = new Socks5Proxy(proxyIP,
                        Integer.valueOf(proxyPort).intValue());
                
                if (proxyConnUser != null && proxyConnPass != null)
                {
                    proxy.setAuthenticationMethod(2,
                            new UserPasswordAuthentication(proxyConnUser,
                                    proxyConnPass));
                }
                Proxy.setDefaultProxy(proxy);
                //                              sslfact = new SocksProxySSLSocketFactory(Proxy
                //                                      .getDefaultProxy());
            }
            catch (Throwable e)
            {
                sslfact = SSLSocketFactory.getDefault();
            }
        }
        else
        {
            sslfact = SSLSocketFactory.getDefault();
        }
    }
    
    /**
     * Creates client socket connected to the given host and port.
     */
    public Socket createSocket(String host, int port) throws IOException
    {
        // if useSSL is true, return secure socket.  If not, return
        // regular socket
        if (useSSL)
        {
            //���host��ip��ַ��������ȡ����������ip��Ӧ���Ӷ��ʡ���ⲿ��ʱ�� by fujunguang
            //to see if the host is an IP address
            Socket socket = null;
            if (Character.digit(host.charAt(0), 16) != -1
                    || (host.charAt(0) == ':'))
            {
                InetAddress temp = InetAddress.getByName(host);
                //                  to get the byte  form of an IP address with  host property   known 
                byte[] address = temp.getAddress();
                //                  use another construct " Inet4Address.getByAddress(host,address)" 
                //                  to get a InetAddress Object  with host parameter initialized.
                InetAddress inetAddress = InetAddress.getByAddress(host,
                        address);
                socket = sslfact.createSocket(inetAddress, port);
            }
            else
            {
                socket = sslfact.createSocket(host, port);
            }
            
            if (socket instanceof SSLSocket)
            {
                SSLSocket sslSocket = (SSLSocket) socket;
                sslSocket.setEnabledCipherSuites(sslSocket.getEnabledCipherSuites());
            }
            
            return socket;
        }
        else
        {
            // begin modified by l90002863 Create a socket but not connected
            Socket socket = fact.createSocket();
            return socket;
            // end modified
            
            //return fact.createSocket(host,port);
        }
    }
    
    /**
     * Creates layered socket on top of given base socket, for use when
     * tunneling HTTP messages through a proxy.
     */
    public Socket createTunnelSocket(Socket s) throws IOException
    {
        throw new IOException("not supported");
    }
}
