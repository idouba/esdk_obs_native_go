/*
 * 文 件 名:  CIM_Authenticator.java
 * 版    权:  Huawei Technologies Co., Ltd. Copyright 2007-2008,  All rights reserved
 * 描    述:  <描述>
 * 修 改 人:  f00102803
 * 修改时间:  Feb 21, 2009
 * 跟踪单号:  <跟踪单号>
 * 修改单号:  <修改单号>
 * 修改内容:  <修改内容>
 */
package javax.wbem.cim;

import java.net.InetAddress;
import java.net.NetPermission;
import java.net.PasswordAuthentication;
import java.net.URL;

/**
 * <一句话功能简述>
 * <功能详细描述>
 * 
 * @author  f00102803
 * @version  V100R001, Feb 21, 2009
 * @see  [相关类/方法]
 * @since  ISM Server:模块
 */
public abstract class CIMAuthenticator
{
    
    // The system-wide authenticator object.  See setDefault().
    private static CIMAuthenticator theAuthenticator;
    
    private String requestingHost;
    
    private InetAddress requestingSite;
    
    private int requestingPort;
    
    private String requestingProtocol;
    
    private String requestingPrompt;
    
    private String requestingScheme;
    
    private URL requestingURL;
    
    private RequestorType requestingAuthType;
    
    /**
     * The type of the entity requesting authentication.
     *
     * @since 1.5
     */
    public enum RequestorType
    {
        /**
         * Entity requesting authentication is a HTTP proxy server.
         */
        PROXY,
        /**
         * Entity requesting authentication is a HTTP origin server.
         */
        SERVER
    }
    
    private void reset()
    {
        requestingHost = null;
        requestingSite = null;
        requestingPort = -1;
        requestingProtocol = null;
        requestingPrompt = null;
        requestingScheme = null;
        requestingURL = null;
        requestingAuthType = RequestorType.SERVER;
    }
    
    /**
     * Sets the authenticator that will be used by the networking code
     * when a proxy or an HTTP server asks for authentication.
     * <p>
     * First, if there is a security manager, its <code>checkPermission</code> 
     * method is called with a 
     * <code>NetPermission("setDefaultAuthenticator")</code> permission.
     * This may result in a java.lang.SecurityException. 
     *
     * @param   a   The authenticator to be set. If a is <code>null</code> then
     *          any previously set authenticator is removed.
     *
     * @throws SecurityException
     *        if a security manager exists and its 
     *        <code>checkPermission</code> method doesn't allow 
     *        setting the default authenticator.
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     */
    public synchronized static void setDefault(CIMAuthenticator a)
    {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            NetPermission setDefaultPermission = new NetPermission(
                    "setDefaultAuthenticator");
            sm.checkPermission(setDefaultPermission);
        }
        
        theAuthenticator = a;
    }
    
    /**
     * Ask the authenticator that has been registered with the system
     * for a password.
     * <p>
     * First, if there is a security manager, its <code>checkPermission</code> 
     * method is called with a 
     * <code>NetPermission("requestPasswordAuthentication")</code> permission.
     * This may result in a java.lang.SecurityException. 
     *
     * @param addr The InetAddress of the site requesting authorization,
     *             or null if not known.
     * @param port the port for the requested connection
     * @param protocol The protocol that's requesting the connection
     *          ({@link java.net.Authenticator#getRequestingProtocol()})
     * @param prompt A prompt string for the user
     * @param scheme The authentication scheme
     *
     * @return The username/password, or null if one can't be gotten.
     *
     * @throws SecurityException
     *        if a security manager exists and its 
     *        <code>checkPermission</code> method doesn't allow 
     *        the password authentication request.
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     */
    public static PasswordAuthentication requestPasswordAuthentication(
            InetAddress addr, int port, String protocol, String prompt,
            String scheme)
    {
        
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            NetPermission requestPermission = new NetPermission(
                    "requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        
        CIMAuthenticator a = theAuthenticator;
        if (a == null)
        {
            return null;
        }
        else
        {
            synchronized (a)
            {
                a.reset();
                a.requestingSite = addr;
                a.requestingPort = port;
                a.requestingProtocol = protocol;
                a.requestingPrompt = prompt;
                a.requestingScheme = scheme;
                return a.getPasswordAuthentication();
            }
        }
    }
    
    /**
     * Ask the authenticator that has been registered with the system
     * for a password. This is the preferred method for requesting a password
     * because the hostname can be provided in cases where the InetAddress
     * is not available.
     * <p>
     * First, if there is a security manager, its <code>checkPermission</code> 
     * method is called with a 
     * <code>NetPermission("requestPasswordAuthentication")</code> permission.
     * This may result in a java.lang.SecurityException. 
     *
     * @param host The hostname of the site requesting authentication.
     * @param addr The InetAddress of the site requesting authentication,
     *             or null if not known. 
     * @param port the port for the requested connection.
     * @param protocol The protocol that's requesting the connection
     *          ({@link java.net.Authenticator#getRequestingProtocol()})
     * @param prompt A prompt string for the user which identifies the authentication realm.
     * @param scheme The authentication scheme
     *
     * @return The username/password, or null if one can't be gotten.
     *
     * @throws SecurityException
     *        if a security manager exists and its 
     *        <code>checkPermission</code> method doesn't allow 
     *        the password authentication request.
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     * @since 1.4
     */
    public static PasswordAuthentication requestPasswordAuthentication(
            String host, InetAddress addr, int port, String protocol,
            String prompt, String scheme)
    {
        
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            NetPermission requestPermission = new NetPermission(
                    "requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        
        CIMAuthenticator a = theAuthenticator;
        if (a == null)
        {
            return null;
        }
        else
        {
            synchronized (a)
            {
                a.reset();
                a.requestingHost = host;
                a.requestingSite = addr;
                a.requestingPort = port;
                a.requestingProtocol = protocol;
                a.requestingPrompt = prompt;
                a.requestingScheme = scheme;
                return a.getPasswordAuthentication();
            }
        }
    }
    
    /**
     * Ask the authenticator that has been registered with the system
     * for a password.
     * <p>
     * First, if there is a security manager, its <code>checkPermission</code> 
     * method is called with a 
     * <code>NetPermission("requestPasswordAuthentication")</code> permission.
     * This may result in a java.lang.SecurityException. 
     *
     * @param host The hostname of the site requesting authentication.
     * @param addr The InetAddress of the site requesting authorization,
     *             or null if not known.
     * @param port the port for the requested connection
     * @param protocol The protocol that's requesting the connection
     *          ({@link java.net.Authenticator#getRequestingProtocol()})
     * @param prompt A prompt string for the user
     * @param scheme The authentication scheme
     * @param url The requesting URL that caused the authentication
     * @param reqType The type (server or proxy) of the entity requesting
     *          authentication.
     *
     * @return The username/password, or null if one can't be gotten.
     *
     * @throws SecurityException
     *        if a security manager exists and its 
     *        <code>checkPermission</code> method doesn't allow 
     *        the password authentication request.
     *
     * @see SecurityManager#checkPermission
     * @see java.net.NetPermission
     *
     * @since 1.5
     */
    public static PasswordAuthentication requestPasswordAuthentication(
            String host, InetAddress addr, int port, String protocol,
            String prompt, String scheme, URL url, RequestorType reqType)
    {
        
        SecurityManager sm = System.getSecurityManager();
        if (sm != null)
        {
            NetPermission requestPermission = new NetPermission(
                    "requestPasswordAuthentication");
            sm.checkPermission(requestPermission);
        }
        
        CIMAuthenticator a = theAuthenticator;
        if (a == null)
        {
            return null;
        }
        else
        {
            synchronized (a)
            {
                a.reset();
                a.requestingHost = host;
                a.requestingSite = addr;
                a.requestingPort = port;
                a.requestingProtocol = protocol;
                a.requestingPrompt = prompt;
                a.requestingScheme = scheme;
                a.requestingURL = url;
                a.requestingAuthType = reqType;
                return a.getPasswordAuthentication();
            }
        }
    }
    
    /**
     * Gets the <code>hostname</code> of the
     * site or proxy requesting authentication, or <code>null</code>
     * if not available.
     * 
     * @return the hostname of the connection requiring authentication, or null
     *      if it's not available.
     * @since 1.4
     */
    protected final String getRequestingHost()
    {
        return requestingHost;
    }
    
    /**
     * Gets the <code>InetAddress</code> of the
     * site requesting authorization, or <code>null</code>
     * if not available.
     * 
     * @return the InetAddress of the site requesting authorization, or null
     *      if it's not available.
     */
    protected final InetAddress getRequestingSite()
    {
        return requestingSite;
    }
    
    /**
     * Gets the port number for the requested connection.
     * @return an <code>int</code> indicating the 
     * port for the requested connection.
     */
    protected final int getRequestingPort()
    {
        return requestingPort;
    }
    
    /**
     * Give the protocol that's requesting the connection.  Often this
     * will be based on a URL, but in a future JDK it could be, for
     * example, "SOCKS" for a password-protected SOCKS5 firewall.
     *
     * @return the protcol, optionally followed by "/version", where
     *      version is a version number.
     *
     * @see java.net.URL#getProtocol()
     */
    protected final String getRequestingProtocol()
    {
        return requestingProtocol;
    }
    
    /**
     * Gets the prompt string given by the requestor.
     *
     * @return the prompt string given by the requestor (realm for
     *      http requests)
     */
    protected final String getRequestingPrompt()
    {
        return requestingPrompt;
    }
    
    /**
     * Gets the scheme of the requestor (the HTTP scheme
     * for an HTTP firewall, for example).
     *
     * @return the scheme of the requestor
     *        
     */
    protected final String getRequestingScheme()
    {
        return requestingScheme;
    }
    
    /**
     * Called when password authorization is needed.  Subclasses should
     * override the default implementation, which returns null.
     * @return The PasswordAuthentication collected from the
     *      user, or null if none is provided.
     */
    protected PasswordAuthentication getPasswordAuthentication()
    {
        return null;
    }
    
    /**
     * Returns the URL that resulted in this
     * request for authentication.
     *
     * @since 1.5
     *
     * @return the requesting URL
     *        
     */
    protected URL getRequestingURL()
    {
        return requestingURL;
    }
    
    /**
     * Returns whether the requestor is a Proxy or a Server.
     *
     * @since 1.5
     *
     * @return the authentication type of the requestor
     *        
     */
    protected RequestorType getRequestorType()
    {
        return requestingAuthType;
    }
}
