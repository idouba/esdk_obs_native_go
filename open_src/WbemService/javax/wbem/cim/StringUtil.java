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
 *are Copyright 2002 Sun Microsystems, Inc.
 *
 *All Rights Reserved.
 *
 *Contributor(s): Brian Schlosser
 */
package javax.wbem.cim;

/*package*/ class StringUtil {

    // All method are static so keep the class from being instantiated.
    private StringUtil() {
    }

    /**
     * Escapes special characters in a string
     * 
     * @param inp the string to process
     * @return The string with all of the special characters escaped.
     */
    public static String quote(String inp) {
        StringBuffer sb = new StringBuffer(inp.length());
        sb.append('\"');
        sb.append(escape(inp));
        sb.append('\"');
        return sb.toString();
    }

    /**
     * Escapes special characters in a string
     * 
     * @param str the string to process
     * @return The string with all of the special characters escaped.
     */
    public static String escape(String str) {
        int size = str.length();
        StringBuffer sb = new StringBuffer(size);
        for (int i = 0; i < size; i++) {
            char ch = str.charAt(i);
            switch (ch) {
                case 0 :
                    continue;
                case '\n': sb.append("\\n");
                    break;
                case '\t': sb.append("\\t");
                    break;
                case '\b': sb.append("\\b");
                    break;
                case '\r': sb.append("\\r");
                    break;
                case '\f': sb.append("\\f");
                    break;
                case '\\': sb.append("\\\\");
                    break;
                case '\'': sb.append("\\\'");
                    break;
                case '\"': sb.append("\\\"");
                    break;
                default :
                    if ((ch = str.charAt(i)) < 0x20 || ch > 0x7e) {
                        String s = Integer.toString(ch, 16);
                        sb.append(
                            "\\x" + "0000".substring(s.length() - 4) + s);
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Removes the first level of escapes from a string
     *
     * @param inp the string to unescape
     * @return the unescaped string 
     */
    public static String unescapeString(String inp)
    {
        StringBuffer sb = new StringBuffer();
        int size = inp.length();
        for (int i = 0; i < size; i++) {
            char ch = inp.charAt(i);
            if(ch == '\\') {
                i++;
                if(i >= size) {
                    throw new IllegalArgumentException(
                        "String ended with an escape, but there was no subsequent character to escape");
                }
                ch = inp.charAt(i);
                switch (ch) {
                    case 'n': sb.append('\n');
                        break;
                    case 't': sb.append('\t');
                        break;
                    case 'b': sb.append('\b');
                        break;
                    case 'r': sb.append('\r');
                        break;
                    case 'f': sb.append('\f');
                        break;
                    case '\\': 
                    case '\'': 
                    case '\"': sb.append(ch);
                        break;
                    case 'X': 
                    case 'x':
                        sb.append("\\x"); 
//?? Finish this
//                        int j = i;
//                        while(Character.digit(inp.charAt(j), 16) >= 0 && 
//                              j < (i + 4))
//                        {
//                            j++;
//                        }
//                        int value = Integer.parseInt(inp.substring(i, j), 16);
//                        i = j;  
//                        sb.append((char)value);
                        break;
                    default : throw new IllegalArgumentException(
                        "Invalid escape sequence '" + ch + 
                        "' (valid sequences are  \\b  \\t  \\n  \\f  \\r  \\\"  \\\'  \\\\ \\x0000 \\X0000 )");
                }
            }
            else {
                sb.append(ch);
            }
        }
        return sb.toString();
    }
    
    /**
     * Removes the first level of quotes and escapes from a string
     *
     * @param value the string to unquote
     * @return the unquoted string 
     */
    public static String unquote(String value) {
        if(value.startsWith("\"")) {
            if(value.endsWith("\"")) {
                value = unescapeString(value.substring(1, value.length() - 1));
            } else {
                throw new IllegalArgumentException("String literal " + value + " is not properly closed by a double quote.");
            }
        }
        return value;
    }
}
