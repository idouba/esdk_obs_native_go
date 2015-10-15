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
 *Contributor(s): WBEM Solutions, Inc.
 */

package javax.wbem.client.adapter.http;

import java.net.URL;

import org.xml.sax.EntityResolver;


/**
 * This class facilitates development of XML messaging clients which use
 * HTTP(S) POSTing to synchronously exchange XML documents.  This may
 * be used directly, or be subclassed to add application-specific behaviours
 * such as specialized processing for some element vocabularies found in
 * those documents.
 *
 * @version 1.9
 */
class XmlRpcClient {
    
    private URL url;
    private boolean checkTypes = true;
    private EntityResolver resolver;

    /**
     * Constructs a client; its URL must be set later.
     */
    XmlRpcClient() { }

    /**
     * Constructs a client, and sets its URL.
     */
    XmlRpcClient(URL url) {
	setUrl(url);
    }

    /**
     * Sets the URL for which this client is a proxy (stand-in); this is
     * a write-once attribute;
     *
     * @param url the URL to which requests will be sent.
     * @exception IllegalStateException if the URL is already assigned
     * @exception IllegalArgumentException if the URL scheme is not
     *	"http" or "https".
     */
    void setUrl(URL url)
    {
	if (this.url != null) {
	    throw new IllegalStateException("URL is already set");
	}

	String scheme = url.getProtocol();

	if (!("http".equals(scheme) || "https".equals(scheme))) {
	    throw new IllegalArgumentException("not an HTTP/HTTPS URL: "
					       + url);
        }
	this.url = url;
    }


    /**
     * Returns the URL for which this client is a proxy.
     */
    URL getUrl() { return url; }

    /**
     * This method is used to indicate whether servers are controlled well
     * enough that the data they provide doesn't need the XML analogue of
     * static type checking: <em>validation</em>.  This is not the default,
     * since it's hard to establish such a high level of control in large
     * open systems, and weak type checking is a major source of errors in
     * all systems.  Even if validation isn't needed, errors in the well
     * formedness of the XML response document will always cause fatal errors
     * in the RPC invocation.
     */
    protected void setCheckTypes(boolean value)
	{ checkTypes = value; }

    /**
     * Returns true if all documents must be validated (the default),
     * or false if the servers are trusted to provide correct data
     */
    protected boolean getCheckTypes()
	{ return checkTypes; }
    

    /**
     * Subclasses can provide a customized entity resolver to be used when
     * resolving external entities such as DTDs.  Typically, DTDs will be
     * cached locally (perhaps as Java resources or files).  In some cases,
     * this handler may know how to handle other sorts of URI; for example,
     * URIs which indicate the XML-formatted results of a database query.
     */
    protected void customizeResolver(EntityResolver r)
	{ resolver = r; }
}
