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

package javax.wbem.cimxml;

/**
 * This class is a factory for creating a new instance of CIMXmlUtil
 * object.
 *
 * @author	Sun Microsystems, Inc.
 * @version	1.0 01/17/02
 * @since	WBEM 2.5
 */
public class CIMXmlUtilFactory {

    private static CIMXmlImpl xmlImpl = new CIMXmlImpl();

    private CIMXmlUtilFactory() {
    }

    /**
     * Return an instance of CIMXmlUtil object.
     *
     * @return an instance of CIMXmlUtil object.
     */
    public static CIMXmlUtil getCIMXmlUtil() {
	return (CIMXmlUtil)xmlImpl;
    }
}
