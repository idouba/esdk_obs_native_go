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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;

import javax.wbem.cim.CIMException;

/**
 * Utility methods for security
 *
 * @author 	Sun Microsystems, Inc.
 * @version	1.3 08/22/01
 * @since	WBEM 1.0 
 */
public class SecurityUtil {

    private static final int RANDOM_KEYSIZE = 20;

    public static final String ADMIN = "root";
    public static SecureRandom secrand = new SecureRandom(getSeed());
    public static void incByteArray(byte[] bArray) {
	if(bArray == null) {
	    return;
	}
	for(int i = 0; i < bArray.length; i++) {
	    byte temp = bArray[i];
	    bArray[i] = (byte)(temp+1);
	    // If incrementing makes it 0, that means there is a carry
	    if(bArray[i] != (byte)0) {
		break;
	    }
	}
    }

    // Method to generate a pseudo-random seed value for SecureRandom objects.
    // Copied this from seabreeze. This is just a temporary fix till Javas
    // SecureRandom is fixed for Windows.

    private static synchronized byte[] getSeed() {

	SecureRandom seeder;
	byte [] kb1 = new byte[RANDOM_KEYSIZE];
	byte [] kb2 = new byte[RANDOM_KEYSIZE];
	byte [] seed = new byte[RANDOM_KEYSIZE];
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
	for (i = 0; i < RANDOM_KEYSIZE; i++) {
	   test = time & mask;
	   if (test > 0)
		seed[i] = kb1[i];
	   else 
		seed[i] = kb2[i];
	   mask = mask << 1;   
	}
	return (seed);   

    }

    /** 
     * Encrypts data by hashing with some generated digest. I think its not 
     * terribly safe if the src is several times larger than the hash. In
     * that case we can slightly modify the algo to generate a new digest 
     * with a secret shared key for each successive hash. Ofcourse, a regular
     * encryption algo should be best.
     *
     * @param hash The digest with which to hash.
     * @param src  The data to hash.
     * @return byte[] The hashed data. Returns null if The hash is less than
     * 4 bytes. This is because it uses the hash to hash the length of src
     * stored as an int (32 bits).
     */
    static public byte [] hashData(byte[] src, byte[] hash) {

	if ((hash == null) || (src == null)) {
	    throw new IllegalArgumentException();
	}

	if(hash.length < 4) {
	    return null;
	}
	// the random Pad is actually longer then it needs to be.
	// we make it the same size as the dest for simplicity
	
	byte[] randomPad = new byte[hash.length];

	synchronized(secrand) {
	    secrand.nextBytes(randomPad);
	}

	// Find out how many times hash must be repeated
	int numRep = src.length/hash.length + 1;
	// We need one extra hash to hash the size
	byte[] dest = new byte[(numRep+1)*hash.length];

	// Hash in the size of the src
	int i;
	for(i=0;i<4;i++) {
	    dest[i] = (byte)(hash[i] ^ 
	    ((src.length & (0xff << (8*i))) >> (8*i)));
	}
	while(i< hash.length) {
	    dest[i] = randomPad[i];
	    i++;
	}

	synchronized(secrand) {
	    secrand.nextBytes(randomPad);
	}

	// Hash in the src
	int j;
	int k=0;
	for(i=0; i< numRep; i++) {
	    for(j=0; j< hash.length; j++) {
		if(k >= src.length) {
		    break;
		}
		dest[k+hash.length] = (byte)(hash[j] ^ src[k]);
		k++;
	    }
	}

	k = k+hash.length;
	i = 0;
	while(k< dest.length) {
	    dest[k] = randomPad[i];
	    i++;
	    k++;
	}
	return dest;
    }

    /** 
     * Extracts the data encrypted byt hashedData
     *
     * @param src The hashed data.
     * @param hash The hash used to extract the data.
     * @return byte[] The extracted data
     */
    public static byte[] extractHashedData(byte[] src, byte[] hash) {
	if((src == null) || (hash == null)) {
	    throw new IllegalArgumentException();
	}
	// Extract the size
	int i;
	int size=0;
	for(i=0; i< 4; i++) {
	    int tempByte = src[i] ^ hash[i];
	    size = size + (tempByte << (8 * i));
	}

	// size cannot be greater than the length of src excluding the
	// first hash.lenth bytes which contains the size information.
	if((size > (src.length - hash.length)) || 
	(size < (src.length - 2*hash.length))) {
	    return null;
	}
	if(size < 0) {
	    return null;
	}

	byte [] dest = new byte[size];
	i=hash.length;
	int fullsize = size + i;
	while(i < fullsize) {
	    for(int j=0; j< hash.length; j++) {
		if(i >= fullsize) {
		    break;
		}
		dest[i-hash.length] = (byte)(src[i] ^ hash[j]);
		i++;
	    }
	}
	return dest;
    }

    /**
     * Copied from seabreeze.
     * This generateDigest method is a static method that returns
     * a serialized byte array of the input object array.
     *
     * @param    objs    Array of serializable objects for the digest
     *
     * @return    The serialized object stream.
     *
     * @version    1.4    11/19/98
     * @author    Sun Microsystems, Inc.
     */
    public static byte [] generateSerialized(Object [] objs)
	throws CIMException {

	ByteArrayOutputStream bas;
	ObjectOutputStream oos;
	byte [] bytes;
	int i;

	// Open an object output stream for serializing objects
	// on top of a byte array output stream.
	try {
	    bas = new ByteArrayOutputStream(2048);
	    oos = new ObjectOutputStream(bas);
	} catch (IOException ex) {
	    throw new CIMException("Cannot initialize output stream");
	}

	// Serialize each object in the array onto the object stream
	for (i = 0; i < objs.length; i++) {
	    try {
		oos.writeObject(objs[i]);
	    } catch (Exception ex) {
		throw new CIMException("Cannot write to output stream");
	    }
	}				    // End of for loop

	// Flush the object stream and return a byte array
	try {
	    oos.flush();
	    bytes = bas.toByteArray();
	} catch (Exception ex) {
	    throw new CIMException("Cannot write to byte array");
	}

	// Close down the streams
	try {
	    oos.close();
	    bas.close();
	} catch (Exception ex) {
	    // Eat this exception
	}

	return bytes;
    }

    /**
     * Copied from Seabreeze.
     * The signDigest method is a static method that digitally signs
     * the specified message digest using the secret key that was
     * generated for this management entity.  It uses the Signature
     * engine object from the configured security provider.
     *
     * @param    digest    The message digest to be signed.
     * @param    privkey   The private key to sign with.
     * @param    signer    The signer.
     *
     * @return    The signature of the message digest
     *
     * @version    1.4    11/19/98
     * @author    Sun Microsystems, Inc.
     */
    public static synchronized byte [] signDigest(byte [] digest, 
					PrivateKey privkey, Signature signer)
	throws CIMException {

	byte [] result = null;

	try {
	    signer.initSign(privkey);
	    signer.update(digest);
	    result = signer.sign();
	} catch (Exception x) {
	    throw new CIMException("Cannot sign");
	}

	return (result);
    }

    /**
     * The verifyDigest method is a static method that verifies a digital
     * signature for a message digest using the specified public key.  It uses 
     * the Signature object from the configured security provider.
     *
     * @param    digest	The message digest that was signed
     * @param    signer    The signer.
     * @param    signature    The digital signature of the message digest
     * @param    publicKey    The public key of the entity that signed the 
     *                        digest
     *
     * @return    boolean true if the digest has been verified, false otherwise
     *
     * @version    1.4    11/19/98
     * @author    Sun Microsystems, Inc.
     */
    public static synchronized boolean verifyDigest(byte [] digest, 
    		Signature signer, byte [] signature, PublicKey publicKey) 
		throws CIMException {

	boolean result = false;

	try {
	    signer.initVerify(publicKey);
	    signer.update(digest);
	    result = signer.verify(signature);
	} catch (Exception x) {
	    throw new CIMException("Cannot verify");
	}

	return (result);

    }

    // Used for hexadecimal conversions
    private static final char[] hex =
        {'0', '1', '2', '3', '4', '5', '6', '7',
         '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * convert a byte array to a hex string
     * @param data the binary data to be converted to a hex string
     * @return an ASCII hex string
     */
    public static String toHex(byte[] data) {

        if (data == null)
            return null;
        if (data.length == 0)
            return "";
        
        StringBuffer sb = new StringBuffer(data.length * 2);
        for (int i = 0; i < data.length; i++) {
            sb.append(hex[(data[i] >> 4) & 0x0f]);
            sb.append(hex[data[i] & 0x0f]);
        }
        return sb.toString();
    }

    /**
     * hexadecimal string to byte array conversion
     */
    public static byte[] fromHex(String str) {

        byte[] ba;
        int i, j, k, len;
        char c;
        boolean ok;

        if (str == null) {
            return null;
        }
        len = str.length();
        ba = new byte[len / 2];
        for (i = 0, j = 0; i < len; i++) {
            ba[j] = 0x00;
            ok = false;
            c = str.charAt(i);
            for (k = 0; k < hex.length; k++) {
                if (c == hex[k]) {
                    ba[j] = (byte) ((k << 4) & 0x000000f0);
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                return null;
            }
            i++;
            ok = false;
            c = str.charAt(i);
            for (k = 0; k < hex.length; k++) {
                if (c == hex[k]) {
                    ba[j] = (byte) (ba[j] | (byte) (k & 0x0000000f));
                    ok = true;
                    break;
                }
            }
            if (!ok) {
                return null;
            }
            j++;
        }
        return ba;
    }
}
