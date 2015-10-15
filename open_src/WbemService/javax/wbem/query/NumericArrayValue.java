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

import java.util.Vector;

/**
 * 
 * @author t-fujita
 *
 * To change the template for this generated type comment go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
public class NumericArrayValue extends ValueExp
{
    private Vector vec = null;

    /**
     * Constructs a NumericArrayValue
     * 
     * @param val
     */
    public NumericArrayValue(Vector val)
    {
        if ( val != null ) {
            vec = val;
        }
    }

    /**
     * Constructs a NumericArrayValue corresponding to 0 length Vector.
     */
    public NumericArrayValue()
    {
        vec = new Vector(0);
    }

    /**
     * Return Vector
     * 
     * @return
     */
    public Vector getValue()
    {
        return vec;
    }
}

