package javax.wbem.client.adapter.http;

import java.util.concurrent.ConcurrentHashMap;

public class HttpEventRequestHandlerMgr
{
    //private static HttpEventRequestHandler handler = null;
    private static ConcurrentHashMap<Integer, HttpEventRequestHandler> handlers = new ConcurrentHashMap<Integer, HttpEventRequestHandler>();
    
    public synchronized static HttpEventRequestHandler getHttpEventRequestHandler(
            int port)
    {
        HttpEventRequestHandler handler = handlers.get(port);
        if (null == handler)
        {
            handler = new HttpEventRequestHandler();
            handlers.put(port, handler);
        }
        return handler;
    }
}
