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

import java.util.LinkedList;

/**
 * ThreadPool is a simple thread pool implementation of the Executor
 * interface.
 *
 * A new task is always given to an idle thread, if one is available;
 * otherwise, a new thread is always created.  There is no minimum
 * warm thread count, nor is there a maximum thread count (tasks are
 * never queued unless there are sufficient idle threads to execute
 * them).
 *
 * New threads are created as daemon threads in the thread group that
 * was passed to the ThreadPool instance's constructor.  Each thread's
 * name is the prefix "RMI " followed by the name of the task it is
 * currently executing, or "Idle" if it is currently idle.
 *
 */
final class ThreadPool implements Executor {

    /** thread waits 5 minutes in the idle state before passing away */
    private static final long idleTimeout = 1*60*1000;

    /** thread group that this pool's threads execute in */
    private ThreadGroup threadGroup;

    /** lock guarding all mutable instance state (below) */
    private final Object lock = new Object();

    /** total threads running in this pool (not currently used) */
    private int totalThreads = 0;

    /** threads definitely available to take new tasks */
    private int idleThreads = 0;

    /** queues of tasks to execute */
    private final LinkedList queue = new LinkedList();

    /**
     * Creates a new thread group that executes tasks in threads of
     * the given thread group.
     */
    ThreadPool(ThreadGroup threadGroup) {
	this.threadGroup = threadGroup;
    }

    public void execute(Runnable runnable, String name) {
	Task task = new Task(runnable, name);
	synchronized (lock) {
	    if (queue.size() < idleThreads) {
		queue.addLast(task);
		lock.notify();
	    } else {
		Thread t = (Thread) java.security.AccessController.doPrivileged(
			new NewThreadAction(threadGroup, new Worker(task), name));
		t.start();
		totalThreads++;
	    }
	}
    }

    /**
     * Task simply encapsulates a task's Runnable object with its name.
     */
    private static class Task {

	final Runnable runnable;
	final String name;

	Task(Runnable runnable, String name) {
	    this.runnable = runnable;
	    this.name = name;
	}
    }

    /**
     * Worker executes an initial task, and then it executes tasks from the
     * queue, passing away if ever idle for more than the idle timeout value.
     */
    private class Worker implements Runnable {

	private Task first;

	Worker(Task first) {
	    this.first = first;
	}

	public void run() {
	    try {
		Task task = first;
		first = null;

		while (true) {
		    task.runnable.run();
		    /*
		     * REMIND: What if the task changed this thread's
		     * priority? or context class loader?
		     */

		    synchronized (lock) {
			if (queue.isEmpty()) {
			    Thread.currentThread().setName("HTTP Idle");
			    idleThreads++;
			    try {
				lock.wait(idleTimeout);
			    } catch (InterruptedException e) {
				// ignore interrupts at this level
			    } finally {
				idleThreads--;
			    }
			    if (queue.isEmpty()) {
				break;		// timed out
			    }
			}
			task = (Task) queue.removeFirst();
			Thread.currentThread().setName("HTTP " + task.name);
		    }
		}
		
	    } finally {
		synchronized (lock) {
		    totalThreads--;
		}
	    }
	}
    }
}
