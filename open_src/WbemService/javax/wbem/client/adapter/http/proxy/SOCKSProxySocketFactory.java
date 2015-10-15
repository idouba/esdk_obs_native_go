/*
 * 项 目 名:  ISM V100R002
 * 文 件 名:  javax.wbem.client.adapter.http.proxy.SOCKSProxySocketFactory.java
 * 版    权:  HuaweiSymantec Technologies Co., Ltd. Copyright 2009,  All rights reserved.
 * 描    述:  HuaweiSymantec PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 * 修 改 人:  x00102290
 * 修改时间:  2009-4-16
 * 修改内容:  创建
 */
package javax.wbem.client.adapter.http.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import socks.Proxy;
import socks.SocksException;
import socks.SocksSocket;

/**
 * 使用SOCKS5代理的SockeyFactory
 * 
 * @author  x00102290
 * @version  ISM V100R002, 2009-4-16
 * @see  
 * @since  
 */
public class SOCKSProxySocketFactory extends javax.net.SocketFactory
{
    Proxy proxy = null;
    
    public SOCKSProxySocketFactory(Proxy proxy)
    {
        if (proxy == null)
        {
            throw new NullPointerException();
        }
        this.proxy = proxy;
    }
    
    public Socket createSocket() throws IOException
    {
    	// 从socks包中抽离出方法
    	if (proxy == null)
    	{
    	    throw new SocksException(Proxy.SOCKS_NO_PROXY);
    	}
    	
        return new WbemSocksSocket(proxy);
    }
    
    public Socket createSocket(String s, int i) throws IOException,
            UnknownHostException
    {
        return new SocksSocket(proxy, s, i);
    }
    
    public Socket createSocket(InetAddress inetaddress, int i)
            throws IOException
    {
        return new SocksSocket(proxy, inetaddress, i);
    }
    
    public Socket createSocket(String s, int i, InetAddress inetaddress, int j)
            throws IOException, UnknownHostException
    {
        throw new UnsupportedOperationException();
    }
    
    public Socket createSocket(InetAddress inetaddress, int i,
            InetAddress inetaddress1, int j) throws IOException
    {
        throw new UnsupportedOperationException();
    }
    
}
