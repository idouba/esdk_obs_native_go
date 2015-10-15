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

import java.util.HashMap;
import java.util.Map;

/**
 * Class for managing client-side functions shared among multiple connections
 * (e.g., tracking of unsent response acknowledgments, caching of information
 * about contacted HTTP servers).
 */
public class HttpClientManager {
    

    // REMIND: change to SoftCache once hotspot SoftReferences fixed
    private Map rolodex = new HashMap(5);

    /**
     * Creates new HttpClientManager which uses threads from given Executor,
     * and expires unsent acknowledgments after the specified timeout.
     */
    public HttpClientManager() {

    }
    
    /**
     * Forgets all cached information about contacted HTTP servers.
     */
    public void clearServerInfo() {
    	synchronized (rolodex)
    	{
    		rolodex.clear();
    	}
    }

    /**
     * Returns cached information about specified HTTP server, or ServerInfo
     * struct with default values if no entry found.
     */
    ServerInfo getServerInfo(String host, int port) {
	ServerKey key = new ServerKey(host, port);
	synchronized (rolodex) {
	    ServerInfo info = (ServerInfo) rolodex.get(key);
	    return (info != null) ? (ServerInfo) info.clone() :
				    new ServerInfo(host, port);
	}
    }

    /**
     * Caches HTTP server information, overwriting any previously registered
     * information for server if timestamp is more recent.
     */
    void cacheServerInfo(ServerInfo info) {
	if (info.timestamp == ServerInfo.NO_TIMESTAMP) {
	    return;
	}
	ServerKey key = new ServerKey(info.host, info.port);
	synchronized (rolodex) {
	    ServerInfo oldInfo = (ServerInfo) rolodex.get(key);
	    if ((oldInfo == null) || (info.timestamp > oldInfo.timestamp)) {
		rolodex.put(key, info.clone());
	    }
	}
    }
    
    /**
     * Server lookup key.
     */
    private static final class ServerKey {
	
	private String host;
	private int port;
	private int hash;
	
	ServerKey(String host, int port) {
	    this.host = host;
	    this.port = port;
	    hash = (host.hashCode() << 10) | (port & 0x3FF);
	}
	
	public int hashCode() {
	    return hash;
	}
	
	public boolean equals(Object obj) {
	    if (obj instanceof ServerKey) {
		ServerKey key = (ServerKey) obj;
		return (host.equals(key.host) && (port == key.port));
	    }
	    return false;
	}
    }
}
