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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TimeZone;

import javax.wbem.client.Debug;

/**
 * Class representing HTTP message header.
 */
class Header {
    
    private static final SimpleDateFormat dateFormat;
    static {
        /*modified by fujunguang  restrict the locale to English */
    dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'",Locale.ENGLISH);
    /*end by fujunguang */
    dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
    }
    
    private HashMap fields = new HashMap(5);

    // text store all the text of the header for print out only
    private String text;
    /**
     * Creates new header with no field entries.
     */
    Header() {
    }
    
    /**
     * Reads in new header from the given input stream.
     */
    Header(InputStream in) throws IOException {
    DataInputStream din = new DataInputStream(in);
    String line = din.readLine();
    text = "";
    while ((line != null) && (line.length() > 0)) {
        String next = din.readLine();
        while ((next != null) && (next.length() > 0) && 
           isSpaceOrTab(next.charAt(0)))
        {
        line += next;
        next = din.readLine();
        }
        
        int sepidx = line.indexOf(':');
        if (sepidx < 0) {
        throw new HttpParseException("header line missing separator");
        }
        String name = line.substring(0, sepidx).trim();
        String value = line.substring(sepidx + 1).trim();
        if (name.length() == 0) {
        throw new HttpParseException("invalid header field name");
        }
        addField(name, value);
        text += name + " : " + value + "\n";
        line = next;
    }

    // if (line == null) {
    //    throw new HttpParseException("unexpected EOF in message header");
    // }

    Debug.trace1(text);
    }
    
    /**
     * Returns value associated with named field, or null if field not present
     * in this header.
     */
    String getField(String name) {
    return (String) fields.get(new FieldKey(name));
    }
    
    /**
     * If given value is non-null, enters it as value of named field;
     * otherwise, removes field (if present) from this header.
     */
    void setField(String name, String value) {
    FieldKey key = new FieldKey(name);
    if (value != null) {
        fields.put(key, value);
    } else {
        fields.remove(key);
    }
    }
    
    /**
     * Returns true if named field's associated value either contains (as an
     * element of a comma-separated list) or is equal to the given value.
     */
    boolean containsValue(String name, String value, boolean ignoreCase) {
    String vlist = getField(name);
    if (vlist != null) {
        value = value.trim();
        StringTokenizer tok = new StringTokenizer(vlist, ",");
        while (tok.hasMoreTokens()) {
        String v = tok.nextToken().trim();
        if (ignoreCase ? value.equalsIgnoreCase(v) : value.equals(v)) {
            return true;
        }
        }
    }
    return false;
    }
    
    /**
     * Returns number of field entries in header.
     */
    int size() {
    return fields.size();
    }
    
    /**
     * If given header is non-null, adds its field entries to this header.  Any
     * overlapping field values are appended to the values in this header with
     * a comma in between.
     */
    void merge(Header header) {
    if (header != null) {
        Iterator ents = header.fields.entrySet().iterator();
        while (ents.hasNext()) {
        Map.Entry e = (Map.Entry) ents.next();
        addField(((FieldKey) e.getKey()).name, (String) e.getValue());
        }
    }
    }

    /**
     * Writes header to given output stream.
     */
    void write(OutputStream out) throws IOException {
    DataOutputStream dout = new DataOutputStream(out);
    Iterator ents = fields.entrySet().iterator();
    while (ents.hasNext()) {
        Map.Entry e = (Map.Entry) ents.next();
        dout.writeBytes(((FieldKey) e.getKey()).name + ": " +
                (String) e.getValue() + "\r\n");
    }
    dout.writeBytes("\r\n");
    }
    
    /**
     * Returns formatted date string for given time.
     */
    static String getDateString(long time) 
    {
        synchronized (dateFormat)
        {
            String date = dateFormat.format(new Date(time));
            return date;
        }

    }

    private static boolean isSpaceOrTab(char c) {
    return ((c == ' ') || (c == '\t'));
    }
    
    /**
     * Associates additional value with named field.  If the field is already
     * present in this header, the field's value is set to the given value
     * appended to the old value with a comma in between.
     */
    private void addField(String name, String value) {
    if (value != null) {
        FieldKey key = new FieldKey(name);
        String oldv = (String) fields.get(key);
        String newv = (oldv != null) ? (oldv + ", " + value) : value;
        fields.put(key, newv);
    }
    }
    
    /**
     * Field lookup key.  Field name comparisons are case-insensitive; however,
     * the original field name string is retained for use when writing the
     * header to a stream.
     */
    private static final class FieldKey {
    
    final String name;
    private int hash;
    
    FieldKey(String name) {
        this.name = name;
        hash = name.toLowerCase().hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj instanceof FieldKey) {
        return name.equalsIgnoreCase(((FieldKey) obj).name);
        }
        return false;
    }
    
    public int hashCode() {
        return hash;
    }
    }
}
