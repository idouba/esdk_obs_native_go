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

package javax.wbem.query;

import javax.wbem.cim.UnsignedInt16;
import javax.wbem.cim.UnsignedInt32;
import javax.wbem.cim.UnsignedInt64;
import javax.wbem.cim.UnsignedInt8;

/**
 * This class represents numbers that are arguments to SQL relational
 * constraints.  A <CODE>NumericValue</CODE> may be used anywhere
 * a <CODE>ValueExp</CODE> is required.  
 *
 * @author      Sun Microsystems, Inc.
 * @version 	1.1 03/01/01
 * @since       WBEM 1.0
 */
public class NumericValue extends ValueExp {

    private long longVal;
    private double doubleVal;
    private UnsignedInt64 uint64;
    private boolean uint = false;
    private boolean sint = false;
    private boolean dbl = false;

    /**
     * Creates a new <CODE>NumericValue</CODE> representing the numeric
     * literal <VAR>val</VAR>.
     */
    public NumericValue(Number val) {
	if (val instanceof Long ||
	    val instanceof Integer ||
	    val instanceof Short ||
	    val instanceof Byte) {
	    this.longVal = val.longValue();
	    sint	 = true;
	    
	} else {
	    if (val instanceof UnsignedInt64 ||
	        val instanceof UnsignedInt8 ||
		val instanceof UnsignedInt16 ||
		val instanceof UnsignedInt32) {

		if (val instanceof UnsignedInt64) {
		    this.uint64 = (UnsignedInt64)val;
		} else {
		    this.uint64 = new UnsignedInt64(val.toString());
		}
		uint = true;
	    } else {
		// Doing this to make up for precision difference in
		// float and double
		this.doubleVal = (new Double(val+"")).doubleValue();
		dbl = true;
	    }
	}
    }

    /**
     * Constructs a NumericValue corresponding to 0.
     */
    public NumericValue() {
	longVal = 0;
	sint = true;
    }

    /**
     * Returns the double value associated with this value.
     * @return double value of this numeric value.
     */
    public double doubleValue() {
	if (sint) {
	    return longVal;
	}
	if (uint) {
	    return uint64.doubleValue();
	} else {
	    return doubleVal;
	}
    }

    /**
     * Returns the long value associated with this value.
     * @return long value of this numeric value.
     */
    public long longValue() {
	if (sint) {
	    return longVal;
	} 

	if (uint) {
	    return uint64.longValue();
	} else {
	    return (long)doubleVal;
	}
    }

    /**
     * Returns the UnsignedInt64 value associated with this value.
     * @return UnsignedInt64 value of this numeric value.
     */
    public UnsignedInt64 uint64Value() {
	if (sint) {
	    return new UnsignedInt64(String.valueOf(longVal));
	} 

	if (uint) {
	    return uint64;
	} else {
	    return new UnsignedInt64(String.valueOf((long)doubleVal));
	}
    }

    /**
     * Check if the value is an unsigned int.
     * @return boolean true if the the value is an unsigned int, false otherwise
     *
     */
    public boolean isUint() {
	return uint;
    }

    /**
     * Check if the value is a signed int.
     * @return boolean true if the the value is a signed int, false otherwise
     *
     */
    public boolean isSint() {
	return sint;
    }

    /**
     * Check if the value is a real.
     * @return boolean true if the the value is a real, false otherwise
     *
     */
    public boolean isReal() {
	return dbl;
    }

    /**
     * Returns a string representing the numeric value.
     *
     *@return  The numeric value as a string.
     */
    public String toString() {
	if (uint) {
	    return uint64.toString();
	} 

	if (sint) {
	    return String.valueOf(longVal);
	}

	return String.valueOf(doubleVal);
	
    }

}
