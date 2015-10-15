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
 * Executor is an abstraction for a thread factory or thread pool for
 * executing actions asynchronously.
 *
 */
public interface Executor {

    /**
     * Executes the given Runnable action asynchronously in some thread.
     *
     * The implemention may create a new thread to execute the action,
     * or it may execute the action in an existing thread.
     *
     * The execution of a given action must not be delayed indefinitely
     * in order to complete execution of a different action passed to a
     * different invocation of this method.  In other words, the
     * implementation must assume that there may be arbitrary dependencies
     * between actions passed to this method, so it needs to be careful
     * to avoid potential deadlock by delaying execution of one action
     * indefinitely until another completes.
     *
     * Also, this method itself must not block, because it may be invoked
     * by code that is serially processing data to produce multiple such
     * arbitrarily-dependent actions that need to be executed.
     *
     * @param	runnable the Runnable action to execute
     *
     * @name	name string to include in the name of the thread used
     * to execute the action
     */
    void execute(Runnable runnable, String name);
}
