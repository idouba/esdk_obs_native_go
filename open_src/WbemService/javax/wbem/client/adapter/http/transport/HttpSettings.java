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

import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Properties;

/**
 * Utility class for querying HTTP-related system properties.
 */
public final class HttpSettings {
    

    private Properties props;

    /**
     * Samples the current values of HTTP-related system properies, returning
     * an HttpSettings instance which can be used to query property values.
     * Changes to property values that occur after the call to getHttpSettings
     * may or may not be reflected in the returned HttpSettings instance.
     */
    public static HttpSettings getHttpSettings() {
	return (HttpSettings) AccessController.doPrivileged(
	    new PrivilegedAction() {
		public Object run() {
		    return new HttpSettings(System.getProperties());
		}
	    });
    }
    
    /**
     * Creates new HttpSettings instance which returns values from the given
     * system properties list.
     */
    private HttpSettings(Properties props) {
	this.props = props;
    }

    /**
     * Returns http.proxyHost system property value if set; else returns
     * proxyHost system property value if set; else returns provided default
     * value.
     */
    public String getHttpProxyHost(String def) {
	String str;
	return (((str = props.getProperty("http.proxyHost")) != null) ||
		((str = props.getProperty("proxyHost")) != null)) ? str : def;
    }
    
    /**
     * Returns http.proxyPort system property value if set; else returns
     * proxyPort system property value if set; else returns provided default
     * value.
     */
    public int getHttpProxyPort(int def) {
	String str;
	if ((str = props.getProperty("http.proxyPort")) != null) {
	    try {
		return Integer.parseInt(str); 
	    } catch (Exception ex) {}
	}
	if ((str = props.getProperty("proxyPort")) != null) {
	    try { 
		return Integer.parseInt(str);
	    } catch (Exception ex) {}
	}
	return def;
    }

    /**
     * Returns https.proxyHost system property value if set; otherwise returns
     * provided default value.
     */
    public String getHttpsProxyHost(String def) {
	return props.getProperty("https.proxyHost", def);
    }
    
    /**
     * Returns https.proxyPort system property value if set; otherwise returns
     * provided default value.
     */
    public int getHttpsProxyPort(int def) {
	String str = props.getProperty("https.proxyPort");
	if (str != null) {
	    try {
		return Integer.parseInt(str); 
} catch (Exception ex) {}
	}
	return def;
    }

    /**
     * Returns sun.rmi.transport.http.ackTimeout system property value if set;
     * otherwise returns provided default value.
     */
    public long getResponseAckTimeout(long def) {
	String str = props.getProperty("sun.rmi.transport.http.ackTimeout");
	if (str != null) {
	    try {
		return Long.parseLong(str); } 
	    catch (Exception ex) {}
	}
	return def;
    }
    
    /**
     * Returns sun.rmi.transport.http.connectionTimeout system property value
     * if set; otherwise returns provided default value.
     */
    public long getConnectionTimeout(long def) {
	String str = 
	    props.getProperty("sun.rmi.transport.http.connectionTimeout");
	if (str != null) {
	    try {
		return Long.parseLong(str); 
	    } catch (Exception ex) {}
	}
	return def;
    }

    /**
     * If http.nonProxyHosts system property value is set, returns true iff
     * given host matches any regular expressions contained in value; if
     * http.nonProxyHosts is unset, returns provided default value.
     */
    public boolean nonProxied(String host, boolean def) {
	return false;
	// String str = props.getProperty("http.nonProxyHosts");
	// if (str == null) {
	//    return def;
	// }
	// synchronized (lastNonProxyLock) {
	//    RegexpPool pool;
	// if ((! str.equalsIgnoreCase(lastNonProxyHosts)) ||
	//	((pool = (RegexpPool) lastNonProxyPool.get()) == null))
	//    {
	//	try {
	//	    pool = new RegexpPool();
	//	    StringTokenizer tok = new StringTokenizer(str, "|");
	//	    while (tok.hasMoreTokens()) {
	//		String pattern = tok.nextToken().toLowerCase();
	//		pool.add(pattern, pattern);
	//	    }
	//	    lastNonProxyPool = new SoftReference(pool);
	//	} catch (REException ex) {
	//	    return def;
	//	}
	//   }
	//    return (pool.match(host) != null);
	// }
    }
}
