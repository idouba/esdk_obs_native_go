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
import java.security.PublicKey;
 
/**
 * Implements the SecurityMessages, which are serializable objects
 * sent back and forth between the client/server.
 *
 * @author 	Roland Schemers
 * @since 	WBEM 1.0
 */

public class SecurityMessage implements Serializable {

    static final long serialVersionUID = 200L;

    private static final int HELLO = 0;
    private static final int CHALLENGE = 1;
    private static final int RESPONSE = 2;
    private static final int RESULT = 3;
    private static final int ERROR = 4;

    private int type;
    private byte[] userDigest;
    private byte[] salt;
    private byte[] challenge;
    private byte[] response;
    private byte[] authenticator;
    private byte[] checksum;
    private byte[] sessionId;
    private byte[] nameSpace;
    private PublicKey pubkey;
    private String message;


    public boolean isHello() { return type == HELLO; }
    public boolean isChallenge() { return type == CHALLENGE; }
    public boolean isResponse() { return type == RESPONSE; }
    public boolean isResult() { return type == RESULT; }
    public boolean isError() { return type == ERROR; }

    public byte[] getUserDigest() { return userDigest; }
    public byte[] getSalt() { return salt; }
    public byte[] getChallenge() { return challenge; }
    public byte[] getResponse() { return response; }
    public byte[] getAuthenticator() { return authenticator; }
    public String getMessage() { return message; }
    public byte[] getChecksum() { return checksum; }
    public byte[] getSessionId() { return sessionId; }
    public byte[] getNameSpace() { return nameSpace; }
    public PublicKey getPublicKey() { return pubkey; }

    /**
     * Generate a new Ucrp error message.
     *
     * @param message the error message.
     */

    public static SecurityMessage error(String message)
    {
	SecurityMessage m = new SecurityMessage();
	m.type = ERROR;
	m.message = message;
	return m;
    }

    /**
     * Generate a new Security result message.
     *
     */

    public static SecurityMessage result(byte[] sessionId, PublicKey pubkey,
				     byte[] response, byte[] checksum)
    {
	SecurityMessage m = new SecurityMessage();
	m.type = RESULT;
	m.sessionId = sessionId;
	m.response = response;
	m.pubkey = pubkey;
	m.checksum = checksum;
	return m;
    }

    /**
     * Generate a new Security response message.
     *
     * @param response the response to the server's challenge.
     */

    public static SecurityMessage response(byte[] response,
					PublicKey pubkey,	
					byte[] sessionId,
					byte[] checksum)
    {
	SecurityMessage m = new SecurityMessage();
	m.type = RESPONSE;
	m.response = response;
	m.sessionId = sessionId;
	m.pubkey = pubkey;
	m.checksum = checksum;
	return m;
    }


    /**
     * Generate a new Ucrp challenge message.
     *
     * @param salt the salt that the client should use.
     *
     * @param challenge the challenge that the client should use.
     */

    public static SecurityMessage challenge(byte[] challenge, byte[] salt, 
    					byte[] sessionId, byte[] checksum)
    {
	SecurityMessage m = new SecurityMessage();
	m.type = CHALLENGE;
	m.salt = salt;
	m.challenge = challenge;
	m.checksum = checksum;
	m.sessionId = sessionId;
	return m;
    }

    /**
     * Generate a new Ucrp hello message.
     *
     * @param challenge The challenge to the CIMOM.
     * @param userDigest The hashed userinfo.
     */

    public static SecurityMessage hello(byte[] challenge, byte[] userDigest,
    					byte[] nameSpace, byte[] checksum)
    {
	SecurityMessage m = new SecurityMessage();
	m.type = HELLO;
	m.userDigest = userDigest;
	m.challenge = challenge;
	m.nameSpace = nameSpace;
	m.checksum = checksum;
	return m;
    }

    /**
     * Converts a message to a String.
     */

    public String toString()
    {
	String s;

	switch(type) {
	case HELLO:
	    s = "c->s  user("+SecurityUtil.toHex(userDigest)+
		")\n, challenge(" +SecurityUtil.toHex(challenge)+")"+
		")\n, namespace(" +SecurityUtil.toHex(nameSpace)+")";
	    break;
	case CHALLENGE:
	    s = "s->c  salt("+SecurityUtil.toHex(salt)+
		")\n, sessionId(" +SecurityUtil.toHex(sessionId)+")"+
		")\n, challenge(" +SecurityUtil.toHex(challenge)+")";
	    break;
	case RESPONSE:
	    s = "c->s  response("+SecurityUtil.toHex(response)+
		"),\n      sessionId(" +SecurityUtil.toHex(sessionId)+")";
	    break;
	case RESULT:
	    s = "s->c  session("+SecurityUtil.toHex(sessionId)+")" +
		",\n response("+SecurityUtil.toHex(response)+")";
	    break;
	case ERROR:
	    s = "s->c  error("+message+")";
	    break;
	default:
	    throw new IllegalArgumentException("unknown type: "+type);
	    //break;
	}

	s = s + "\nchecksum("+SecurityUtil.toHex(checksum)+")";
	return s;

    }
}
