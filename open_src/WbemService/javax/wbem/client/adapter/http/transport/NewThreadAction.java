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

/**
 * NewThreadAction is a java.security.PrivilegedAction for creating new
 * threads conveniently with a java.security.AccessController.doPrivileged
 * construct.
 *
 * All constructors allow the choice of the Runnable for the new thread
 * to execute and the name of the new thread (which will be prefixed with
 * "HTTP "). NOTE: It will always be a daemon thread.
 *
 */
public final class NewThreadAction implements java.security.PrivilegedAction {

    /** cached reference to the system (root) thread group */
    static final ThreadGroup systemThreadGroup = (ThreadGroup)
        java.security.AccessController.doPrivileged(
		new java.security.PrivilegedAction() {
		    public Object run() {
			ThreadGroup group = Thread.currentThread().getThreadGroup();
			ThreadGroup parent;
			while ((parent = group.getParent()) != null) {
			    group = parent;
			}
			return group;
		    }
		}
	);

    private final ThreadGroup group;
    private final Runnable runnable;
    private final String name;

    NewThreadAction(ThreadGroup group, Runnable runnable, String name) {
	this.group = group;
	this.runnable = runnable;
	this.name = name;
    }

    public Object run() {
	Thread t = new Thread(group, runnable, "HTTP " + name);
	t.setDaemon(true);
	return t;
    }
}
