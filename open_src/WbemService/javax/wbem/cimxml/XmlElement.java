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

package javax.wbem.cimxml;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Stack;

class XmlElement implements Serializable {

    protected boolean SHOW_IMPLIED = false;
    protected boolean SHOW_DEFAULT = false;
    protected boolean SHOW_HOST = false;
    protected boolean SHOW_NAMESPACE = false;
    protected boolean IncludeQualifiers = true;
    protected boolean IncludeClassOrigin = false;
    protected String [] PropertyList = null;

    private static int uniqueInt = 0;
    private Hashtable attrs = new Hashtable(11);
    private Stack gis = new Stack();

    public XmlElement() {
    }
 
    public XmlElement(boolean showImplied) {
	this.SHOW_IMPLIED = showImplied;
    }

    public XmlElement(boolean includeQualifiers,
                        boolean includeClassOrigin,
                        String [] propertyList,
                        boolean showImplied) {
	this.IncludeQualifiers = includeQualifiers;
	this.IncludeClassOrigin = includeClassOrigin;
	this.PropertyList = propertyList;
	this.SHOW_IMPLIED = showImplied;
    }

    public XmlElement(boolean includeQualifiers,
                        boolean includeClassOrigin,
                        String [] propertyList,
                        boolean showImplied,
                        boolean showDefault,
                        boolean showHost,
                        boolean showNamespace) {
	this(includeQualifiers, includeClassOrigin,
		propertyList, showImplied);
	this.SHOW_HOST = showHost;
	this.SHOW_NAMESPACE = showNamespace;
	this.SHOW_DEFAULT = showDefault;
    }

    String push(String gi) {
        gis.push(gi);
        return gi;
    }
 
    String pop() {
        return (String)gis.pop();
    }
 
    boolean empty() {
        return gis.empty();
    }

    void setShowDefault(boolean show) {
	SHOW_DEFAULT = show;
    }

    boolean getShowDefault() {
	return SHOW_DEFAULT;
    }

    void setShowImplied(boolean show) {
	SHOW_IMPLIED = show;
    }

    boolean getShowImplied() {
	return SHOW_IMPLIED;
    }

    String getAttrs() {
	StringBuffer s = new StringBuffer();
	String key = "";
	if (attrs.size() > 0) {
	    for (Enumeration e = attrs.keys(); e.hasMoreElements(); ) {
		key = e.nextElement().toString();
		s.append(" " + key + "=\"" + attrs.get(key) + "\"");
	    }
	    attrs.clear();
	}
	return s.toString();
    }

    Object setAttr(String name, String value) {
	if (name == null || value == null) {
	    return null;
	}
	attrs.put(name, value);
	return attrs.get(name);
    }

    String stag(String gi) {
	gis.push(gi);
	return "<" + gi + getAttrs() + ">";
    }

    String etag() {
	return "</" + pop() + ">";
    }

    String etag(String gi) {
	StringBuffer s = new StringBuffer();
	while (gis.search(gi) > 0) {
	    s.append(etag());
	}
	return s.toString();
    }

    String otag(String gi) {
	return "<" + gi + getAttrs() + "/>";
    }

    boolean getIncludeQualifiers() {
	return this.IncludeQualifiers;
    }

    boolean getIncludeClassOrigin() {
	return this.IncludeClassOrigin;
    }

    void setIncludeQualifiers(boolean include) {
	IncludeQualifiers = include;
    }

    void setIncludeClassOrigin(boolean include) {
	IncludeClassOrigin = include;
    }

    String [] getPropertyList() {
	return this.PropertyList;
    }

    void setPropertyList(String [] list) {
	this.PropertyList = list;
    }

    boolean getShowHost() {
	return this.SHOW_HOST;
    }

    void setShowHost(boolean assume) {
	this.SHOW_HOST = assume;
    }

    boolean getShowNamespace() {
	return this.SHOW_NAMESPACE;
    }

    void setShowNamespace(boolean assume) {
	this.SHOW_NAMESPACE = assume;
    }

    /*
     * Stolen from CIMOMUtils
     */

    synchronized String getUniqueString() {
	Calendar c = Calendar.getInstance();
	uniqueInt++;
	return c.get(Calendar.YEAR)+":"+
		c.get(Calendar.MONTH)+":"+
		c.get(Calendar.DATE)+":"+
		c.get(Calendar.HOUR)+":"+
		c.get(Calendar.AM_PM)+":"+
		c.get(Calendar.MINUTE)+":"+
		c.get(Calendar.SECOND)+":"+
		uniqueInt;
    }
}
