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
 *are Copyright (c) 2001 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): _______________________________________
*/

package javax.wbem.security;

import java.io.Serializable;

/**
 * Security token which is passed beteen the clients and CIMOM.
 *
 * @author	Sun Microsystems, Inc.
 * @version	1.1 03/01/01
 * @since	WBEM 1.0
 */
public class SecurityToken implements Serializable {

    static final long serialVersionUID = 200L;

    private byte[] checksum; 
    private byte[] signature; 
    private byte[] sessionId; 

    public byte[] getChecksum() {
	return checksum;
    }

    public byte[] getSignature() {
	return signature;
    }

    public byte[] getSessionId() {
	return sessionId;
    }

    public void setChecksum(byte[] checksum) {
	this.checksum = checksum;
    }

    public void setSignature(byte[] signature) {
	this.signature = signature;
    }

    public void setSessionId(byte[] sessionId) {
	this.sessionId = sessionId;
    }

    public String toString() {
	return "[" + new String(sessionId) + "," +
		SecurityUtil.toHex(checksum) + "," +
		SecurityUtil.toHex(signature) + "]";
    }
}
