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

package javax.wbem.client;

import java.security.SecureRandom;


/** 
 * 
 * This class implements a password based credential for a user
 * login authentication.  It should be used in conjunction with the
 * UserPrincipal instance.  It contains a user login password and
 * the host name of the system where the password was used to
 * authenticate its associated UserPrincipal.
 * 
 * @author	Sun Microsystems, Inc. 
 * @version 	1.4 01/28/02
 * @since	WBEM 2.0
 */
 
public class PasswordCredential {

    /**
     * maximum size of a password
     */
    public static final int MAX_PASSWORD_SIZE = 16;
    /**
     * an empty stored password.
     */
    public static final byte[] NULL_PASS_WORD = {
	0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

    // Private instance attributes
    private byte[] junk;		// Key
    private byte[] userpswd;		// User password
    private String hostname;		// Scoping host name

    // Constructors

    /**
     * Empty constructor.
     */
    public PasswordCredential() {

	this.setup();
	userpswd = new byte[MAX_PASSWORD_SIZE];
	this.zap(userpswd);
	hostname = null;

    }

    /**
     * This constructor accepts the user login password.
     *
     * @param userPassword	The user login password in clear text.
     */
    public PasswordCredential(String userPassword) {

	this.setup();
	userpswd = new byte[MAX_PASSWORD_SIZE];
	if ((userPassword != null) && (userPassword.trim().length() > 0)) {
	    this.encode(userpswd, userPassword);
	} else {
	    this.zap(userpswd);
	}
	hostname = null;

    }

    // ==============================================================

    // Accessor methods

    /**
     * Return the user login password in clear text.
     *
     * @return	The user login password.
     */
    public String getUserPassword() {

	String str;
	if ((userpswd[0] == 0) && (userpswd[1] == 0)) {
	    str = "";
	} else {
	    str = this.decode(userpswd);
	}
	return (str);
    }

   /**
     * Return the server host name where this credential
     * was authenticated.
     *
     * @return  The server host name.
     */
    public String getHostName() {

	return (hostname);
    }

    // Mutator methods

    /**
     * Set the user login password.
     *
     * @param userPassword	The user login password in clear text.
     */
    public void setUserPassword(String userPassword) {

	if ((userPassword != null) && (userPassword.trim().length() > 0)) {
	    this.encode(userpswd, userPassword);
	} else {
	    this.zap(userpswd);
	}

    }

    /**
     * Clear the user login password.
     */
    public void clearUserPassword() {

	this.zap(userpswd);
	hostname = null;

    }

    /**
     * Set the server host name where this credential was authenticated.
     *
     * @param hostName  The server host name.
     */
    public void setHostName(String hostName) {

	hostname = hostName;

    }

    // Protected methods to encode and decode passwords.
    // Byte array parameter must be MAX_PASSWORD_SIZE bytes long.

    protected void encode(byte [] pswd, String password) {

	byte [] kb, data;
	int i, len;

	if (password != null) {
	    kb = new byte[MAX_PASSWORD_SIZE];
	    System.arraycopy(junk, 0, kb, 0, MAX_PASSWORD_SIZE);
	    data = password.getBytes();
	    len = data.length;
	    if (len > MAX_PASSWORD_SIZE)
		len = MAX_PASSWORD_SIZE;
	    for (i = 0; i < len; i++)
		kb[i] ^= data[i];
	    if (i < MAX_PASSWORD_SIZE)
		kb[i] ^= 0;
	    System.arraycopy(kb, 0, pswd, 0, MAX_PASSWORD_SIZE);
	} else {
	    this.zap(pswd);
	}

    }

    protected String decode(byte [] pswd) {

	String str;
	byte [] kb;
	int i, j, len;
	boolean sw;

	str = "";
	kb = new byte[MAX_PASSWORD_SIZE];
	System.arraycopy(junk, 0, kb, 0, MAX_PASSWORD_SIZE);
	len = MAX_PASSWORD_SIZE;
	sw = true;
	for (i = 0, j = len; i < len; i++) {
	    kb[i] ^= pswd[i];
	    if (sw && (kb[i] == 0)) {
		sw = false;
		j = i;
	     }
	}
	if (j > 0) {
	    str = new String(kb, 0, j);
	}
	return (str);

    }

    protected void zap(byte [] pswd) {

	System.arraycopy(NULL_PASS_WORD, 0, pswd, 0, MAX_PASSWORD_SIZE);
	pswd[0] = junk[0];

    }

    // Initial our key
    private void setup() {

    	SecureRandom seeder;
	byte [] kb1 = new byte[MAX_PASSWORD_SIZE];
	byte [] kb2 = new byte[MAX_PASSWORD_SIZE];
	this.junk = new byte[MAX_PASSWORD_SIZE];
	long time, mask, test;
	int  i;

	// Use the current time to init a random number generator
	// and retrieve two keys.
	time = System.currentTimeMillis();
	seeder = new SecureRandom();
	seeder.nextBytes(kb1);
	seeder.nextBytes(kb2);

	// Use the current time to choose bytes between the two
	// randomly generated key byte sequences.  Use the low
	// order bits of the current time (changes most rapidly).
	time = System.currentTimeMillis();
	mask = 1;
	test = 0;
	for (i = 0; i < MAX_PASSWORD_SIZE; i++) {
	    test = time & mask;
	    if (test > 0)
		this.junk[i] = kb1[i];
	    else
		this.junk[i] = kb2[i];
	    mask = mask << 1;
	}

    }
} // PasswordCredential
