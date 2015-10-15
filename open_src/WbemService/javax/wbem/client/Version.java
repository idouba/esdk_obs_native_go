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

package javax.wbem.client;

// This file represents the version of the WBEM API. During the build, 
// Version.java is generated with the build date and time.
public class Version {
    // Disallow any instances from being created
    private Version() {
    }
    
    public final static int major = 1;    
    public final static int minor = 0;
    public final static int patch = 0;
	
    public final static String copyright =  "";
    public final static String companyName = "WBEM Services";
    public final static String companyContact = "http://wbemservices.sourceforge.net/";
    public final static String productName = "WBEM Services WBEM API";
    public final static String buildID = "11/01/04:10:48"; 
}
