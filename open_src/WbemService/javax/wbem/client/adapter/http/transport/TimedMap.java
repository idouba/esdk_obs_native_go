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

import java.util.HashMap;
import java.util.concurrent.ExecutorService;

/**
 * Simple hash map which evicts entries after a fixed timeout.  All operations
 * which modify a TimedMap synchronize on the TimedMap instance itself,
 * including the thread which evicts expired entries.
 */
class TimedMap {
    
    private ExecutorService executor;
    private long timeout;
    private HashMap map = new HashMap();
    private Queue evictQueue = new Queue();
    private boolean evictorActive = false;

    /**
     * Creates empty TimedMap which uses threads from the given Executor to
     * evict entries after the specified timeout.
     */
    TimedMap(ExecutorService executor, long timeout) {
	if (executor == null) {
	    throw new NullPointerException();
	}
	if (timeout < 0) {
	    throw new IllegalArgumentException();
	}
	this.executor = executor;
	this.timeout = timeout;
    }
    
    /**
     * Associates the given key with the given value, resetting the key's
     * timeout.  Returns value (if any) previously associated with key.
     */
    synchronized Object put(Object key, Object value) {
	if (! evictorActive) {
	    executor.execute(new Evictor()/*, "TimedMap evictor"*/);
	    evictorActive = true;
	}
	long now = System.currentTimeMillis();
	Mapping mapping = new Mapping(key, value, now + timeout);
	evictQueue.append(mapping);

	if ((mapping = (Mapping) map.put(key, mapping)) == null) {
	    return null;
	}
	evictQueue.remove(mapping);
	return mapping.value;
    }
    
    /**
     * Returns value associated with given key, or null if no mapping for key
     * is found.  Resets timeout for key if it is present in map.
     */
    synchronized Object get(Object key) {
	Mapping mapping = (Mapping) map.get(key);
	if (mapping == null) {
	    return null;
	}
	evictQueue.remove(mapping);
	mapping.expiry = System.currentTimeMillis() + timeout;
	evictQueue.append(mapping);
	return mapping.value;
    }
    
    /**
     * Removes mapping for key from map, returning the value associated with
     * the key (or null if no mapping present).
     */
    synchronized Object remove(Object key) {
	Mapping mapping = (Mapping) map.remove(key);
	if (mapping == null) {
	    return null;
	}
        evictQueue.remove(mapping);
	return mapping.value;
    }
    
    /**
     * Upcall invoked after key's timeout has expired and key has been removed
     * from the map.
     */
    void evicted(Object key, Object value) {
    }

    /**
     * Key/value mapping.
     */
    private static final class Mapping extends Queue.Node {
	Object key;
	Object value;
	long expiry;
	
	Mapping(Object key, Object value, long expiry) {
	    this.key = key;
	    this.value = value;
	    this.expiry = expiry;
	}
    }
    
    /**
     * Expired mapping eviction thread.
     */
    private final class Evictor implements Runnable {

        public void run()
        {   
            // 修改coverity
            synchronized (TimedMap.this)
            {
                Mapping mapping;
                while ((mapping = nextEvicted()) != null)
                {
                    evicted(mapping.key, mapping.value);
                }
            }
	   
	}
	
	private Mapping nextEvicted() {
	    synchronized (TimedMap.this) {
		Mapping mapping;
		while ((mapping = (Mapping) evictQueue.getHead()) != null) {
		    long now = System.currentTimeMillis();
		    if (mapping.expiry <= now) {
			evictQueue.remove(mapping);
			map.remove(mapping.key);
			return mapping;
		    } else {
			try {
			    TimedMap.this.wait(mapping.expiry - now);
			} catch (InterruptedException ex) {
			}
		    }
		}
		evictorActive = false;
		return null;
	    }
	}
    }

    /**
     * Lightweight doubly-linked queue supporting constant-time manipulation.
     */
    private static final class Queue {
	
	static class Node {
	    private Queue owner;
	    private Node prev;
	    private Node next;
	}
	
	private Node head;
	private Node tail;

	Queue() {
	    head = new Node();
	    tail = new Node();
	    head.next = tail;
	    tail.prev = head;
	}
	
	Node getHead() {
	    return (head.next != tail) ? head.next : null;
	}
	
	void remove(Node node) {
	    if (node.owner != this) {
		throw new IllegalArgumentException();
	    }
	    node.prev.next = node.next;
	    node.next.prev = node.prev;
	    node.prev = node.next = null;
	    node.owner = null;
	}
	
	void append(Node node) {
	    if (node.owner != null) {
		throw new IllegalArgumentException();
	    }
	    node.owner = this;
	    node.prev = tail.prev;
	    node.next = tail;
	    tail.prev.next = node;
	    tail.prev = node;
	}
    }
}
