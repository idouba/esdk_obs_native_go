package javax.wbem.client.adapter.http;

import java.util.concurrent.ConcurrentHashMap;

public class HttpEventListenerMgr
{
    //private static HttpEventListener listener = null;
    private static ConcurrentHashMap<Integer, HttpEventListener> eventListeners = new ConcurrentHashMap<Integer, HttpEventListener>();
    
    public synchronized static HttpEventListener getHttpEventListener(int port)
    {
        HttpEventListener listener = eventListeners.get(port);
        if (null == listener)
        {
            listener = new HttpEventListener();
            eventListeners.put(port, listener);
        }
        return listener;
    }
}
