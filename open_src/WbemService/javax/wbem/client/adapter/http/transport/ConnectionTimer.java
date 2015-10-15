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

/**
 * Utility class for timing out connections.
 */
public class ConnectionTimer {
    //修改人：杨锋 ISMV1R3-2147 
    private static final ExecutorService systemThreadPool = (ExecutorService)
	java.security.AccessController.doPrivileged(
	    new GetThreadPoolAction());

    private TimeoutMap timeouts;

    /**
     * Creates new ConnectionTimer which uses threads spawned by the given
     * executor to shut down overdue connections.
     */
    public ConnectionTimer(long timeout) {
	timeouts = new TimeoutMap(timeout);
    }

    /**
     * Schedules timeout for given connection.  If timeout is already scheduled
     * for given connection, renews timeout.  When the timeout occurs, the
     * connection's shutdown method will be called with the given force value.
     */
    public void scheduleTimeout(TimedConnection conn, boolean force) {
	if (conn == null) {
	    throw new NullPointerException();
	}
	timeouts.put(conn, new Boolean(force));
    }

    /**
     * Attempts to cancel timeout for the given connection.  Returns true if a
     * timeout was successfully cancelled, false otherwise (e.g. if connection
     * was never scheduled for a timeout, has already been timed out, or is
     * already in the midst of being timed out).
     */
    public boolean cancelTimeout(TimedConnection conn) {
	if (conn == null) {
	    throw new NullPointerException();
	}
	return (timeouts.remove(conn) != null);
    }

    /**
     * Map for tracking idle connection timeouts.
     */
    private static final class TimeoutMap extends TimedMap {
	
	TimeoutMap(long timeout) {
	    super(systemThreadPool, timeout);
	}
	
	void evicted(Object key, Object value) {
	    boolean force = ((Boolean) value).booleanValue();
	    ((TimedConnection) key).shutdown(force);
	}
    }
}
