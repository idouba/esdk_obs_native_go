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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * GetThreadPoolAction provides security-checked access to the runtime's
 * internal thread pools as a java.security.PrivilegedAction, to be used
 * conveniently with a java.security.AccessController.doPrivileged
 * construct.
 *
 * There are two internal thread pools: one of threads in the system
 * thread group, for executing tasks to be guarded by the security policy
 * for the system thread group, and one of threads in a non-system thread
 * group, for executing tasks with user code that should not be restricted
 * by that policy.
 *
 */
public final class GetThreadPoolAction
    implements java.security.PrivilegedAction {
    /** pool of threads for executing tasks in system thread group */
    //修改人：杨锋 ISMV1R3-2147 
    private static final ExecutorService systemThreadPool = new ThreadPoolExecutor(2, 200, 60,
            TimeUnit.SECONDS, new SynchronousQueue<Runnable>());

    /**
     * Creates an action that will obtain an internal thread pool.
     * When run, this action verifies that the current access control
     * context has permission to access the thread group used by the
     * indicated pool. NOTE: It will always obtain a system thread
     * group pool.
     *
     */
    public GetThreadPoolAction() {
    }

    public Object run() {
	SecurityManager sm = System.getSecurityManager();
	if (sm != null) {
	    sm.checkAccess(NewThreadAction.systemThreadGroup);
	}
	return systemThreadPool;
    }
}
