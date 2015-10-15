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

package javax.wbem.client.adapter.http;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;


/**
 * Used to treat validation errors as fatal.
 *
 * @version 1.1 
 */
class Errors implements ErrorHandler
{
    // Only need to allocate one of these, ever!
    static Errors instance = new Errors();

    private Errors() { 
	/* nothing */ 
    }

    /** Warnings are ignored */
    public void warning(SAXParseException e) throws SAXException { 
	/* ignore these */ 
    }

    /* This class treats validation errors as fatal, too */
    public void error(SAXParseException e) throws SAXException { 
	throw e; 
    }

    /* Fatal errors are always, well, fatal! */
    public void fatalError(SAXParseException e) throws SAXException { 
	throw e; 
    }
}

