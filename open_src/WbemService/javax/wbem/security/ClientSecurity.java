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
 *Contributor(s): Brian Schlosser
*/

package javax.wbem.security;

import java.security.Principal;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.client.Debug;
import javax.wbem.client.PasswordCredential;

/**
 * This class represents an implementation of the client security interface for
 * the Basic Authentication mechanism of the HTTP protocol. Since the HTTP
 * protocol is essentially connectionless, the client principal identity is
 * authenticated for each request to the remote CIMOM server; that is, there is
 * no security session established and no initial authentication message
 * exchange.
 * 
 * @author Sun Microsystems, Inc.
 */
abstract public class ClientSecurity implements ClientSecurityContext {

    private final String mechanism;
    protected CIMNameSpace nameSpace;
    protected Principal principal;
    protected String userName;
    protected byte[] userPswd;
    protected String nsHost;
    protected String nsPath;
    protected boolean bEstablished;

    // Internal data
    private byte[] encryptKey = null;

    public ClientSecurity(String mechanism) {
        this.mechanism = mechanism;
        this.userPswd = new byte[0];
        this.nsPath = "";
    }

    public String getMechanism() {
        return mechanism;
    }

    public String getServerName() {
        return nsHost;
    }

    public CIMNameSpace getNameSpace() {
        return nameSpace;
    }

    public String getUserName() {
        return userName;
    }

    public Principal getPrincipal() {
        return principal;
    }

    public boolean isEstablished() {
        return bEstablished;
    }

    public void setNameSpace(CIMNameSpace namespace) throws CIMException {

        if (namespace == null) {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }
        this.nameSpace = namespace;
        this.nsHost = namespace.getHost();
        this.nsPath = namespace.getNameSpace();

    } // setNameSpace

    public void setPrincipal(Principal prin) throws CIMException {

        if ((prin == null) || 
            (! (prin instanceof javax.wbem.client.UserPrincipal))) {
            Debug.trace1("Invalid client user principal");
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }
        this.principal = prin;
        this.userName = prin.getName();
        if ((userName == null) || (userName.trim().length() == 0)) {
            Debug.trace1("Invalid client user principal");
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

    } // setPrincipal

    public void setCredential(Object cred) throws CIMException {

        String tPswd = null;
        if (cred != null) {
            if (cred instanceof javax.wbem.client.PasswordCredential) {
                tPswd =
                    ((PasswordCredential) cred).getUserPassword();
            } else if (cred instanceof java.lang.String) {
                tPswd = (String)cred;
            }
        }
        if (tPswd == null) {
            Debug.trace1("Invalid credential for user " + this.userName);
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }

        // Convert string characters to byte array explicitly
        // to avoid byte converter on local system. Only support
        // ISO-Latin (8-bit) password.
        int len = tPswd.length();
        userPswd = new byte[len];
        for (int i = 0; i < len; i++) {
            char c = tPswd.charAt(i);
            userPswd[i] = (byte) (c);
        }

    } // setCredential

    public void dispose() {

        this.bEstablished = false;
        this.nameSpace = null;
        this.principal = null;
        this.userName = null;
        java.util.Arrays.fill(this.userPswd, (byte) 0x00);
        this.userPswd = new byte[0];
        this.nsHost = null;
        this.nsPath = "";

    }

    // XXXX - Must return password for HTTP transport for now!
    public String getUserPassword() {

        // Only support passwords of ISO-Latin 8-bit characters.
        // We build the characters explicitly to avoid the default
        // character converter on the system.
        String tPswd = null;
        if (userPswd.length > 0) {
            char[] ca = new char[userPswd.length];
            for (int i = 0; i < userPswd.length; i++) {
                ca[i] = (char) (0 | (userPswd[i] & 0xff));
            }
            tPswd = new String(ca);
        }
        return tPswd;
    }

    // XXXX - Use this method to return each HTTP authenticator?
    public SecurityToken getSecurityToken(String[] sarray) throws CIMException {
        return null;
    }

    // Maximum size of an encrypted value in unencrypted characters
    // Should always be one less than the session key size!
    private static final int MAX_DATA_SIZE = 15;

    // XXXX - No session key! Fake a key or use 64-bit encoding
    public String trans51Format(String inData) throws CIMException {

        int i;

        // System.out.println("trans51Format: data = " + inData);

        // If the data size is too big, cannot encrypt data.
        // If the encrypt key is not available, cannot encrypt data.
        // XXXX - Put error message in resource file!
        if ((inData.length() > MAX_DATA_SIZE) || (encryptKey == null)) {
            Debug.trace1("Cannot encrypt data; invalid data or session key");
            throw new CIMException(
                CIMException.CIM_ERR_FAILED,
                "Bad data length or null encrypt key");
        }
        
        // Get the characters into bytes. Only works for ASCII!!!
        byte[] pwb = inData.getBytes();

        // Copy the key into the response byte array
        int len = encryptKey.length;
        byte[] rb = new byte[len];
        System.arraycopy(encryptKey, 0, rb, 0, len);

        // Get a random pad byte array for extending the password.
        byte[] pad = new byte[len];
        
        //修改coverity GUARDED_BY_VIOLATION
        synchronized(SecurityUtil.secrand)
        {
        	SecurityUtil.secrand.nextBytes(pad);
        }
        

        // XOR in the clear text password over the key
        for (i = 0; i < pwb.length; i++) {
            rb[i] ^= pwb[i];
        }

        // XOR in a zero to indicate the end of the password
        rb[i++] ^= 0;

        // XOR any remaining bytes in the result using the pad bytes
        while (i < rb.length) {
            rb[i] ^= pad[i];
            i++;
        }

        // Convert the encrypted bytes to a hexadecimal string
        // and return.
        String val = SecurityUtil.toHex(rb);
        // System.out.println("trans51Format: encr = " + val);
        return (val);
    }
}
