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


import java.io.IOException;
import java.net.Socket;

/**
 * Abstraction for objects which provide/configure sockets used by
 * HttpClientConnection instances.
 */
public interface HttpClientSocketFactory {
    
    /**
     * Creates client socket connected to the given host and port.
     */
    Socket createSocket(String host, int port) throws IOException;
    
    /**
     * Creates layered socket on top of given base socket, for use when
     * tunneling HTTP messages through a proxy.
     */
    Socket createTunnelSocket(Socket s) throws IOException;
}
