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

package javax.wbem.client.adapter.http.transport;

import java.io.DataOutputStream;
import java.io.IOException;

import javax.wbem.client.Debug;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


abstract public class HttpServerRequestHandler implements RequestHandler {

    protected void doErrorOutput(DataOutputStream pOut, String pTitle,
				 String pBody)
    throws IOException {
	pOut.writeBytes("<HTML><HEAD><TITLE>");
	pOut.writeBytes(pTitle);
	pOut.writeBytes("</TITLE></HEAD><BODY>\n");
	pOut.writeBytes(pTitle + " - " + pBody);
	pOut.writeBytes("</BODY></HTML>");
    }


    protected void do501Error(DataOutputStream pOut) {
	try {
	    String errorType = "501 Not Implemented";
	    pOut.writeBytes("HTTP/1.0 " + errorType + "\r\n\r\n");
	    doErrorOutput(pOut, errorType, "The requested " +
                                        "METHOD is not supported");
	} catch (IOException e) {
	    Debug.trace1("IOException: " + e.getMessage());
	    e.printStackTrace();	    
	}
    }

    protected void do500Error(DataOutputStream pOut, String pErrorString) {
	try {
	    String errorType = "500 Internal Server Error";
	    pOut.writeBytes("HTTP/1.0 " + errorType + "\r\n\r\n");
	    doErrorOutput(pOut, errorType, "An internal "+
			      "error occurred while processing the request" +
				"<br><b>" + pErrorString + "</b>");
	} catch (IOException e) {
	    // generally this indicates the client is not listening 
	    // for a response.
	    Debug.trace1("IOException: " + e.getMessage());
	    e.printStackTrace();	    
	}
    }

    protected void do404Error(DataOutputStream pOut, String pRequestURI) {
	try {
	    String errorType = "404 Not Found";
	    pOut.writeBytes("HTTP/1.0 " + errorType + "\r\n\r\n");
	    doErrorOutput(pOut, errorType,
			      "The requested URI was not found: " +
			      pRequestURI);
	} catch (IOException e)
	    {
		Debug.trace1("IOException: " + e.getMessage());
		e.printStackTrace();
	    }
    }

    protected Document getXmlDocument(InboundRequest request)
    throws IOException, SAXException {
        DocumentBuilder builder = null;
            
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            builder = dbf.newDocumentBuilder();
            // TODO:  Set a resolver??
        } catch (Exception e) {
        }

    //for fortify
    if (null == builder){
    	throw new NullPointerException();
    }
	InputSource in = new InputSource();
        dbf.setNamespaceAware(true);
	//builder.setDisableNamespaces(false);
	//parser.setDocumentHandler(builder);
	in.setByteStream(request.getRequestInputStream());
	
	if(null == builder)
	{
		throw new NullPointerException();
	}
        Document d = builder.parse(in);
	return d;
        
    }
}
