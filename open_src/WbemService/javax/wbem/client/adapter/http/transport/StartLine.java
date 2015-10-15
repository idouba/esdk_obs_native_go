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

package javax.wbem.client.adapter.http.transport;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.StringTokenizer;

/**
 * Class representing first line of an HTTP message.
 */
final class StartLine
{
    
    /** major version number */
    final int major;
    
    /** minor version number */
    final int minor;
    
    /** request or response line? */
    final boolean isRequest;
    
    /** request method */
    final String method;
    
    /** request URI */
    final String uri;
    
    /** response status code */
    final int status;
    
    /** response status reason */
    final String reason;
    
    /**
     * Creates start line for HTTP request message.
     */
    StartLine(int major, int minor, String method, String uri)
    {
        this.major = major;
        this.minor = minor;
        this.method = method;
        this.uri = uri;
        status = -1;
        reason = null;
        isRequest = true;
    }
    
    /**
     * Creates start line for HTTP response message.
     */
    StartLine(int major, int minor, int status, String reason)
    {
        this.major = major;
        this.minor = minor;
        this.status = status;
        this.reason = reason;
        method = null;
        uri = null;
        isRequest = false;
    }
    
    /**
     * Reads start line from given input stream.
     */
    StartLine(InputStream in) throws IOException
    {
        String line = (new DataInputStream(in)).readLine();
        if (line == null)
        {
            throw new HttpParseException("unexpected EOF in start line");
        }
        if (line.length() == 0)
        {
            throw new HttpParseException("empty start line");
        }
        try
        {
            StringTokenizer tok = new StringTokenizer(line, "", true);
            if (line.startsWith("HTTP"))
            {
                if (!tok.nextToken("/").equals("HTTP"))
                {
                    System.err.println("error first" + line);
                    throw new HttpParseException();
                }
                tok.nextToken();
                major = Integer.parseInt(tok.nextToken("."));
                tok.nextToken();
                minor = Integer.parseInt(tok.nextToken(" "));
                tok.nextToken();
                status = Integer.parseInt(tok.nextToken());
                tok.nextToken();
                reason = tok.nextToken("\n");
                
                method = null;
                uri = null;
                isRequest = false;
            }
            else
            {
                method = tok.nextToken(" ");
                tok.nextToken();
                uri = tok.nextToken();
                tok.nextToken();
                if (!tok.nextToken("/").equals("HTTP"))
                {
                    System.err.println("error second" + line);
                    throw new HttpParseException();
                }
                tok.nextToken();
                major = Integer.parseInt(tok.nextToken("."));
                tok.nextToken();
                minor = Integer.parseInt(tok.nextToken("\n"));
                
                status = -1;
                reason = null;
                isRequest = true;
            }
        }
        catch (Exception ex)
        {
            throw new HttpParseException("invalid start line");
        }
    }
    
    /**
     * Writes start line to given output stream.
     */
    void write(OutputStream out) throws IOException
    {
        DataOutputStream dout = new DataOutputStream(out);
        String version = "HTTP/" + major + "." + minor;
        if (isRequest)
        {
            dout.writeBytes(method + " " + uri + " " + version + "\r\n");
        }
        else
        {
            dout.writeBytes(version + " " + status + " " + reason + "\r\n");
        }
    }
    
    /**
     * Compares two sets of major/minor version numbers.  Returns -1 if
     * major1/minor1 is less than major2/minor2, 1 if major1/minor1 is more
     * than major2/minor2, and 0 if the two pairs are equal.
     */
    static int compareVersions(int major1, int minor1, int major2, int minor2)
    {
        if (major1 != major2)
        {
            return (major1 > major2) ? 1 : -1;
        }
        else if (minor1 != minor2)
        {
            return (minor1 > minor2) ? 1 : -1;
        }
        else
        {
            return 0;
        }
    }
    
}
