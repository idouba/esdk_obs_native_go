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
 *Contributor(s): WBEM Solutions, Inc.
 */

package javax.wbem.client.adapter.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.wbem.cimxml.CIMXml;
import javax.wbem.cimxml.CIMXmlFactory;
import javax.wbem.client.CIMEvent;
import javax.wbem.client.CIMListener;
import javax.wbem.client.Debug;
import javax.wbem.client.adapter.http.transport.HttpServerRequestHandler;
import javax.wbem.client.adapter.http.transport.InboundRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

class HttpEventRequestHandler extends HttpServerRequestHandler
{
    
    CIMListener clientListener;
    
    private List<CIMListener> clientListeners = new ArrayList<CIMListener>();
    
    private CIMXml xmlInst;
    
    private volatile boolean isInterrupted = false;
    
    private BlockingQueue<CIMEvent> events = new LinkedBlockingQueue<CIMEvent>();
    
    private boolean isInit = false;
    
    public HttpEventRequestHandler()
    {
        
    }
    
    public boolean isInit()
    {
        return isInit;
    }
    
    public void init(List<CIMListener> clientListeners)
    {
        addCIMListener(clientListeners);
        this.xmlInst = CIMXmlFactory.getCIMXmlImpl();
        Thread ep = new EventProcessor();
        ep.setName("WBEM HttpEventRequestHandler");
        ep.setDaemon(true);
        ep.start();
        
        isInit = true;
    }
    
    public void addCIMListener(List<CIMListener> clientListeners)
    {
        synchronized (this)
        {
            this.clientListeners.addAll(clientListeners);
        }
    }
    
    public void addCIMListener(CIMListener clientListener)
    {
        synchronized (this)
        {
            this.clientListeners.add(clientListener);
        }
    }
    
    public HttpEventRequestHandler(List<CIMListener> clientListeners)
    {
        //        this.clientListener = clientListener;
        //        this.xmlInst = CIMXmlFactory.getCIMXmlImpl();
        //        Thread ep = new EventProcessor();
        //        ep.setName("WBEM HttpEventRequestHandler");
        //        ep.setDaemon(true);
        //        ep.start();
        
        this.init(clientListeners);
    }
    
    public void close()
    {
        isInterrupted = true;
    }
    
    public boolean checkAuthentication(InboundRequest request)
    {
        return true;
    }
    
    public void handleRequest(InboundRequest request)
    {
        Document xmlDoc;
        
        try
        {
            xmlDoc = getXmlDocument(request);
            
            try
            {
                Debug.trace3(xmlDoc);
            }
            catch (Throwable t)
            {
                //eat the exception
            }
            
            DataOutputStream out = new DataOutputStream(
                    request.getResponseOutputStream());
            String str = request.getHeaderField("CIMExportMethod");
            if (str == null || !str.equals("ExportIndication"))
            {
                do501Error(out);
                return;
            }
            
            // deliver indication
            //	    clientListener.indicationOccured(
            //		new CIMEvent(xmlInst.getCIMInstance(xmlDoc)));
            /*synchronized (events)
             {
             events.add(new CIMEvent(xmlInst.getCIMInstance(xmlDoc)));
             events.notifyAll();
             }*/
            try
            {
                events.put(new CIMEvent(xmlInst.getCIMInstance(xmlDoc)));
            }
            catch (InterruptedException e1)
            {
                
            }
            // send back response
            xmlDoc = xmlInst.getXMLResponse(null, xmlDoc, null);
            // Use a Transformer for output
            TransformerFactory tFactory = TransformerFactory.newInstance();
            Transformer transformer;
            try
            {
                transformer = tFactory.newTransformer();
                DocumentType docType = xmlDoc.getDoctype();
                if (docType != null)
                {
                    String systemId = docType.getSystemId();
                    if (systemId != null)
                    {
                        String systemValue = (new File(systemId)).getName();
                        transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM,
                                systemValue);
                    }
                }
                
                DOMSource source = new DOMSource(xmlDoc);
                StreamResult result = new StreamResult(out);
                transformer.transform(source, result);
            }
            catch (TransformerConfigurationException e)
            {
                Debug.trace3("HttpEventRequestHandler", e);
            }
            catch (TransformerException e)
            {
                Debug.trace3("HttpEventRequestHandler", e);
            }
        }
        catch (SAXException e)
        {
            DataOutputStream out = new DataOutputStream(
                    request.getResponseOutputStream());
            Exception x = e.getException();
            String message = "Parse Error with Request:\n";
            if (x == null)
            {
                x = e;
            }
            if (e instanceof SAXParseException)
            {
                SAXParseException spe = (SAXParseException) e;
                message += "** URI: " + spe.getSystemId() + "\n";
                message += "** Line: " + spe.getLineNumber() + "\n";
            }
            
            // Output an error to the client
            do500Error(out, "XML Parsing error: <b>" + message + "</b>");
            Debug.trace3(message, e);
        }
        catch (IOException e)
        {
            Debug.trace3("HttpEventRequestHandler", e);
        }
        catch (ParserConfigurationException e)
        {
            Debug.trace3("HttpEventRequestHandler", e);
        }
    }
    
    public void addResponseHeaderFields(InboundRequest request)
    {
        request.setRespondHeaderField("CIMOperation", "ExportMethodResponse");
    }
    
    private class EventProcessor extends Thread
    {
        private ReadWriteLock _listLock = new ReentrantReadWriteLock();
        public void run()
        {
            while (true)
            {
                //modify by f90002221, 此处捕获所有异常，防止线程终止
                try
                {
                    if (isInterrupted)
                    {
                        return;
                    }
                    
                    //clientListener.indicationOccured((CIMEvent) (events.remove(0)));�յ�������
                    CIMEvent cimEvent = (CIMEvent) events.take();
                    Set<CIMListener> l = new HashSet<CIMListener>();
                    try
                    {
                        _listLock.readLock().lock();
                        l.addAll(clientListeners);
                    }
                    finally
                    {
                        _listLock.readLock().unlock();
                    }
                    
                    Iterator<?> i = l.iterator();
                    while (i.hasNext())
                    {
                        CIMListener listener = (CIMListener) i.next();
                        listener.indicationOccured(cimEvent);
                    }
                    Debug.trace3(cimEvent.toString());
                }
                catch (Throwable t)
                {
                }
            }
        }
    }
}
