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
 *Contributor(s): WBEM Solutions, Inc. AppIQ, Inc.
 */

package javax.wbem.client.adapter.http;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMAuthenticator;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMDataType;
import javax.wbem.cim.CIMDateTime;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMProperty;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.cim.UnsignedInt16;
import javax.wbem.cim.UnsignedInt32;
import javax.wbem.cim.UnsignedInt64;
import javax.wbem.cim.UnsignedInt8;
import javax.wbem.cimxml.CIMXml;
import javax.wbem.cimxml.CIMXmlFactory;
import javax.wbem.client.CIMAssociatorNamesOp;
import javax.wbem.client.CIMAssociatorsOp;
import javax.wbem.client.CIMClient;
import javax.wbem.client.CIMClientAPI;
import javax.wbem.client.CIMCreateClassOp;
import javax.wbem.client.CIMCreateInstanceOp;
import javax.wbem.client.CIMCreateNameSpaceOp;
import javax.wbem.client.CIMCreateQualifierTypeOp;
import javax.wbem.client.CIMDeleteClassOp;
import javax.wbem.client.CIMDeleteInstanceOp;
import javax.wbem.client.CIMDeleteNameSpaceOp;
import javax.wbem.client.CIMDeleteQualifierTypeOp;
import javax.wbem.client.CIMEnumClassNamesOp;
import javax.wbem.client.CIMEnumClassOp;
import javax.wbem.client.CIMEnumInstanceNamesOp;
import javax.wbem.client.CIMEnumInstancesOp;
import javax.wbem.client.CIMEnumNameSpaceOp;
import javax.wbem.client.CIMEnumQualifierTypesOp;
import javax.wbem.client.CIMExecQueryOp;
import javax.wbem.client.CIMGetClassOp;
import javax.wbem.client.CIMGetInstanceOp;
import javax.wbem.client.CIMGetQualifierTypeOp;
import javax.wbem.client.CIMInvokeArgsMethodOp;
import javax.wbem.client.CIMListener;
import javax.wbem.client.CIMOperation;
import javax.wbem.client.CIMReferenceNamesOp;
import javax.wbem.client.CIMReferencesOp;
import javax.wbem.client.CIMSetClassOp;
import javax.wbem.client.CIMSetInstanceOp;
import javax.wbem.client.CIMSetPropertyOp;
import javax.wbem.client.CIMSetQualifierTypeOp;
import javax.wbem.client.CIMTransportException;
import javax.wbem.client.adapter.http.transport.HttpClientConnection;
import javax.wbem.client.adapter.http.transport.HttpClientSocketFactory;
import javax.wbem.client.adapter.http.transport.HttpSocketFactory;
import javax.wbem.client.adapter.http.transport.OutboundRequest;
import javax.wbem.security.BasicClientSecurity;
import javax.wbem.security.ClientSecurityContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * @author      WBEM Services
 * @since       WBEM 1.0
 * @exception   CIMException Throws a CIM Exception 
 * @exception   CIMTransportException the host is unknown
 * @exception   CIMTransportException HTTP connection fails because 
 *              no CIM Object Manager found
 * @exception   CIMTransportException Remote exception becasue no 
 *              CIM Object Manager found
 * @exception   CIMTransportException Malformed URL - no CIM Object 
 *              Manager found
 * 
 */
public class CIMClientXML extends XmlRpcClient implements CIMClientAPI
{
    
    /*
     *  The protocol name.  Must be consistent with CIM standard name.
     */
    private static final String protocol_name = CIMClient.CIM_XML;
    
    public final static String HTTPPORT = "5988";
    
    public final static String HTTPSPORT = "5989";
    
    private CIMNameSpace nameSpace;
    
    private CIMListener clientListener;
    
    private String version;
    
    private HttpClientConnection conn = null;
    
    private int debug = 3;
    
    private boolean useChunking = false;
    
    DtdResolver resolver = new DtdResolver();
    
    //    private final static String SYSTEMID =
    //  "http://www.dmtf.org/cim/mapping/xml/v2.0";
    private final static String PUBLICID = "-//DMTF//DTD CIM 2.0//EN";
    
    private String prefix = "";
    
    private static final String HANDLERCLASS = "CIM_IndicationHandlerCIMXML";
    
    private static final String LISTENERCLASS = "CIM_ListenerDestinationCIMXML";
    
    private CIMXml xmlImpl;
    
    private HttpEventListener listener = null;
    
    private ReadWriteLock _listenerLock = new ReentrantReadWriteLock(); // ������
    
    private Object connLock = new Object();
    
    private boolean isForceDisconnect = false;
    
    // The following enables XML tracing
    private static String LOG_NAME = "CIMClientXML_Trace";
    
    private static String LOG_DATE = null;
    
    private static int LOG_NUMBER = 1;
    
    private static int LOG_MAXBYTES;
    
    static
    {
        String logSize = System.getProperty("wbem.debug.size", "10");
        if (!logSize.equals("0"))
        {
            LOG_MAXBYTES = new Integer(logSize).intValue() * 1024 * 1024;
        }
        else
        {
            LOG_MAXBYTES = 10 * 1024 * 1024;
        }
        
        String path = System.getProperty("ISMPATH");
        if (null != path && 0 < path.trim().length())
        {
            LOG_NAME = path + "/logs/wbem/CIMClientXML_Trace";
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            LOG_DATE = sdf.format(new Date());
        }
    }
    
    private static FileOutputStream fout = null;
    
    private static PrintStream psout = null;
    
    private PasswordAuthentication passwordAuthentication = null;
    
    public CIMClientXML(String version, CIMNameSpace name,
            CIMListener clientListener, Integer dbg) throws CIMException
    {
        
        nameSpace = name;
        this.clientListener = clientListener;
        String scheme = nameSpace.getScheme();
        if ((scheme == null) || (scheme.length() == 0))
        {
            scheme = "http";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append(scheme);
        buffer.append("://");
        buffer.append(nameSpace.getHost());
        buffer.append(':');
        
        String port = nameSpace.getPort();
        if (port == null)
        {
            if (scheme.toLowerCase().equals("https"))
            {
                port = HTTPSPORT;
            }
            else
            {
                port = HTTPPORT;
            }
        }
        buffer.append(port);
        buffer.append("/CIMOM");
        buffer.append(version);
        
        try
        {
            //            URL ns = new URL(buffer.toString());  支持IPV6时这样调用会抛异常  xiongxiaoyong 2011-3.5
            URL ns = new URL(scheme, nameSpace.getHost(),
                    Integer.valueOf(port), "/CIMOM" + version);
            setUrl(ns);
        }
        catch (java.net.MalformedURLException ex)
        {
            throw new CIMTransportException(CIMTransportException.NO_CIMOM,
                    buffer.toString());
        }
        this.debug = dbg.intValue();
        setCheckTypes(System.getProperty("checktypes") != null ? "true".equalsIgnoreCase(System.getProperty("checktypes"))
                : false);
        setupResolver(resolver);
        xmlImpl = CIMXmlFactory.getCIMXmlImpl();
    }
    
    public void setupResolver(DtdResolver resolver)
    {
        resolver.registerCatalogEntry(PUBLICID,
                "javax/wbem/client/adapter/http/cim20.dtd",
                getClass().getClassLoader());
        customizeResolver(resolver);
    }
    
    private static class HttpAuthenticator extends CIMAuthenticator
    {
        static HttpAuthenticator instance = new HttpAuthenticator();
        
        ThreadLocal clientPerThread = new ThreadLocal();
        
        public void setClient(CIMClientXML client)
        {
            clientPerThread.set(client);
        }
        
        public CIMClientXML getClient()
        {
            return (CIMClientXML) clientPerThread.get();
        }
        
        protected PasswordAuthentication getPasswordAuthentication()
        {
            return getClient().getPasswordAuthentication();
        }
    }
    
    private PasswordAuthentication getPasswordAuthentication()
    {
        return passwordAuthentication;
    }
    
    /**
     * Return the name of the CIM protocol being used.
     *
     * @return  The name of the CIM protocol.
     */
    public String getProtocol()
    {
        return (protocol_name);
    }
    
    public synchronized void initSecurityContext(String version,
            ClientSecurityContext csc) throws CIMException
    {
        
        // Extract the user name and password from the security context.
        // Must cast to specific subclass to get the password.
        BasicClientSecurity cs = null;
        try
        {
            cs = (BasicClientSecurity) csc;
        }
        catch (Exception ex)
        {
            throw new CIMException(CIMException.CIM_ERR_FAILED,
                    "Bad credential");
        }
        
        String username = new String(cs.getUserName());
        String password = cs.getUserPassword();
        
        if(null == password)
        {
        	throw new NullPointerException();
        }
        
        passwordAuthentication = new PasswordAuthentication(username,
                password.toCharArray());
        
        // Now set up the special Authenticator class.
        // Once this is set, cannot be reset!
        CIMAuthenticator.setDefault(HttpAuthenticator.instance);
    }
    
    Document call(Document request) throws CIMException, IOException
    {
        URL url = getUrl();
        Document d = null;
        DocumentBuilder builder = null;
        
        if (url == null)
        {
            throw new IllegalStateException("URL is not set");
        }
        
        HttpAuthenticator.instance.setClient(this);
        
        OutputStream requestos = null;
        PrintStream out = null;
        String host = url.getHost();
        int port = url.getPort();
        HttpClientSocketFactory factory = new HttpSocketFactory(
                url.getProtocol());
        OutboundRequest outRequest = null;
        for (int i = 0; i < 2; i++)
        {
            try
            {
                if (conn == null)
                {
                    conn = new HttpClientConnection(host, port, factory, false,
                            useChunking);
                    conn.setupConnection(factory);
                    boolean isInter = Thread.interrupted();
                    if (isInter)
                    {
                        this.shutdown();
                        
                        throw new RuntimeException(
                                "This thread is interrupted!");
                    }
                }
                outRequest = conn.newRequest(url.getPath());
                requestos = outRequest.getRequestOutputStream();
                out = new PrintStream(requestos,
                        false, "UTF8");
                setRequestHeaders(outRequest,
                        xmlImpl.getXmlRequestHeaders(request));
                outRequest.endWriteHeader();
                writeDocumentToOutputStream(request, out);
                out.flush();
                break;
            }
            catch (IOException e)
            {
                this.shutdown();
                // begin modified l90002863
                synchronized (this.connLock)
                {
                    // 如果连接因超时被强制关闭则不继续尝试
                    if (this.isForceDisconnect || (i == 1))
                    {
                        this.isForceDisconnect = false;
                        throw e;
                    }
                }
                // end modified
            }
            finally
            {
            	if(null != out)
            	{
            		out.close();
            	}
				try
				{
            		if(null != requestos)
            		{
            			requestos.close();
            		}
				}
				catch (IOException e2)
				{
				}
            }
        }
        
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        
        try
        {
            builder = dbf.newDocumentBuilder();
        }
        catch (Exception e)
        {
        }
        
        //for fority
        if (null == builder)
        {
        	throw new NullPointerException();
        }
        InputSource in;
        
        if (getCheckTypes())
        {
            dbf.setValidating(true);
            builder.setErrorHandler(Errors.instance);
        }
        
        if (resolver == null)
        {
            resolver = new DtdResolver();
            setupResolver(resolver);
        }
        
        //修改coverity forward null
        if(null == builder)
        {
        	throw new NullPointerException();
        }
        
        builder.setEntityResolver(resolver);
        
        try
        {
        	if (outRequest == null)
            {
                throw new NullPointerException();
            }
            InputStream contentInput = outRequest.getResponseInputStream();
            contentInput.available();
            in = new InputSource(contentInput);
            if (debug == 1 || debug == 3)
            {
                dumpRequest(request, outRequest);
            }
            if (outRequest.getResponseCode() >= HttpURLConnection.HTTP_BAD_REQUEST)
            {
                try
                {
                    /* Delete by l90003110 20090204 */
                    //fout = new FileOutputStream(TRACEFILE, true);
                    //psout = new PrintStream(fout);
                    /* Delete by l90003110 20090204 */
                    updateLogFile();
                    psout.println("outRequest.getResponseCode() error ,the reponseCode is"
                            + outRequest.getResponseCode()
                            + " the thread is "
                            + Thread.currentThread().getName()
                            + " logged by fujunguang");
                }
                catch (Exception e)
                {
                    System.err.println("when logging occur exception ");
                }
                finally
                {
                    /* Delete by l90003110 20090204 */
                    //                    fout.close();
                    //                    psout.close();
                    /* Delete by l90003110 20090204 */
                }
                throw new IOException();
            }
            
            try
            {
                d = builder.parse(in);
                contentInput.close();
                
            }
            catch (SAXException e)
            {
                if (debug > 0)
                {
                    StringBuffer buf = new StringBuffer();
                    Reader r = in.getCharacterStream();
                    for (int i = 0; i >= 0; i = r.read())
                    {
                        buf.append((char) i);
                    }
                    if (debug >= 2)
                    {
                        dumpResponse(null, outRequest);
                    }
                }
                else
                {
                    throw new CIMException("XMLERROR", e);
                }
            }
        }
        catch (IOException e)
        {
            //Since we now support persistent connections
            //if the server times out and closes the stream
            //the client has to recognize this and reset the connection            
            if ((e.getMessage() != null)
                    && (e.getMessage().startsWith("stream invalid")))
            {
                if (conn != null)
                {
                    conn.shutdown(true);
                }
                conn = null;
                return call(request);
            }
            
            if (outRequest.getResponseCode() == HttpURLConnection.HTTP_UNAUTHORIZED)
            {
                throw new CIMException(CIMException.CIM_ERR_ACCESS_DENIED);
            }
            if (debug >= 2)
            {
                dumpResponse(null, outRequest);
            }
            // 修改coverity
            if (conn != null && outRequest.getResponseCode() == HttpURLConnection.HTTP_LENGTH_REQUIRED
                    && conn.supportsChunking())
            {
                conn.shutdown(true);
                conn = null;
                useChunking = false;
                return call(request);
            }
            throw new CIMException("XMLERROR", e);
        }
        if (debug >= 2)
        {
            dumpResponse(d, outRequest);
        }
        return d;
    }
    
    private void writeDocumentToOutputStream(Document request, PrintStream out)
    {
        // Use a Transformer for output
        TransformerFactory tFactory = TransformerFactory.newInstance();
        Transformer transformer;
        try
        {
            transformer = tFactory.newTransformer();
            DocumentType docType = request.getDoctype();
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
            DOMSource source = new DOMSource(request);
            StreamResult result = new StreamResult(out);
            transformer.transform(source, result);
        }
        catch (TransformerConfigurationException e)
        {
            //e.printStackTrace();
        }
        catch (TransformerException e)
        {
            //e.printStackTrace();
        }
    }
    
    private void setRequestHeaders(OutboundRequest outRequest, Map headers)
    {
        prefix = "";
        Iterator it = headers.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry entry = (Map.Entry) it.next();
            outRequest.addHeaderField(prefix + (String) entry.getKey(),
                    (String) entry.getValue());
        }
    }
    
    private void dumpRequest(Document request, OutboundRequest outRequest)
            throws IOException
    {
        // The following appends XML trace to a file
        /* Delete by l90003110 20090204 */
        //fout = new FileOutputStream(TRACEFILE, true);
        //psout = new PrintStream(fout);
        /* Delete by l90003110 20090204 */
        updateLogFile();
        psout.println(">>>>>>>>>>>>>>>>>>>>>>>" + " START OF REQUEST  "
                + ">>>>>>>>>>>>>>>>>>>>>>>");
        outRequest.dumpOutHeader(psout);
        if (request != null)
        {
            writeDocumentToOutputStream(request, psout);
        }
        psout.println(">>>>>>>>>>>>>>>>>>>>>>>" + " END OF REQUEST  "
                + ">>>>>>>>>>>>>>>>>>>>>>>");
        /* Delete by l90003110 20090204 */
        //        fout.close();
        //        psout.close();
        /* Delete by l90003110 20090204 */
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>" + " START OF REQUEST  "
                + ">>>>>>>>>>>>>>>>>>>>>>>");
        outRequest.dumpOutHeader(System.out);
        if (request != null)
        {
            writeDocumentToOutputStream(request, System.out);
        }
        System.out.println(">>>>>>>>>>>>>>>>>>>>>>>" + " END OF REQUEST  "
                + ">>>>>>>>>>>>>>>>>>>>>>>");
    }
    
    private void dumpResponse(Document response, OutboundRequest outRequest)
            throws IOException
    {
        // The following appends XML trace to a file
        /* Delete by l90003110 20090204 */
        //fout = new FileOutputStream(TRACEFILE, true);
        //psout = new PrintStream(fout);
        /* Delete by l90003110 20090204 */
        updateLogFile();
        psout.println("<<<<<<<<<<<<<<<<<<<<<<<" + " START OF RESPONSE  "
                + "<<<<<<<<<<<<<<<<<<<<<<<");
        outRequest.dumpInHeader(psout);
        psout.println("\n");
        if (response != null)
        {
            writeDocumentToOutputStream(response, psout);
        }
        psout.println("<<<<<<<<<<<<<<<<<<<<<<<" + " END OF RESPONSE  "
                + "<<<<<<<<<<<<<<<<<<<<<<<");
        /* Delete by l90003110 20090204 */
        //        fout.close();
        //        psout.close();
        /* Delete by l90003110 20090204 */
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<" + " START OF RESPONSE  "
                + "<<<<<<<<<<<<<<<<<<<<<<<");
        outRequest.dumpInHeader(System.out);
        System.out.println("\n");
        if (response != null)
        {
            writeDocumentToOutputStream(response, System.out);
        }
        System.out.println("<<<<<<<<<<<<<<<<<<<<<<<" + " END OF RESPONSE  "
                + "<<<<<<<<<<<<<<<<<<<<<<<");
    }
    
    private Vector getResponse(Document request) throws MalformedURLException,
            CIMException, IOException
    {
        if (request == null)
        {
            throw new CIMException("XMLERROR", "getResponse");
        }
        
        Document response = call(request);
        
        return xmlImpl.getCIMResponse(response);
    }
    
    /**
     * TODO: This is a temp workaround.
     *  The issue is that for any operation that returns an InstanceName only 
     * the host/namespace information does not come back in the payload - we have
     * to populate it for them.  I need to come back and fix it so that the object 
     * path is populated before fixing the keys. 
     * We want to wait until we fix the key issues in the XML first as then we may not
     * need to do this here.
     * @param method
     * @param request
     * @return
     * @throws MalformedURLException
     * @throws CIMException
     * @throws IOException
     */
    private Vector getCIFirstResponse(String method, Document request)
            throws MalformedURLException, CIMException, IOException
    {
        Vector multi = getResponse(request);
        Hashtable simple = (Hashtable) multi.firstElement();
        Vector v = (Vector) simple.get(method);
        if (v.size() > 0 && v.elementAt(0) instanceof CIMException)
        {
            throw (CIMException) v.elementAt(0);
        }
        Iterator iter = v.iterator();
        while (iter.hasNext())
        {
            fixKeytype(iter.next());
        }
        
        return v;
    }
    
    private Vector getFirstResponse(String method, Document request)
            throws MalformedURLException, CIMException, IOException
    {
        Vector multi = getResponse(request);
        Hashtable simple = (Hashtable) multi.firstElement();
        Vector v = (Vector) simple.get(method);
        if (v.size() > 0 && v.elementAt(0) instanceof CIMException)
        {
            throw (CIMException) v.elementAt(0);
        }
        /*
         * @@@Hack: We need to fix the key type here since the CIM-XML 
         * spec 3.2.4.13 does not give us the true type. We hack the
         * code in XMLParser to set the cimtype to null and leave the
         * value as a string. Here we use the meta data on the class
         * to figure the true type.
         */
        Iterator iter = v.iterator();
        Object obj = null;
        try
        {
            while (iter.hasNext())
            {
                obj = iter.next();
                fixKeytype(obj);
            }
        }
        catch (Throwable e)
        {
            try
            {
                /* Delete by l90003110 20090204 */
                //fout = new FileOutputStream(TRACEFILE, true);
                //psout = new PrintStream(fout);
                /* Delete by l90003110 20090204 */
                updateLogFile();
                psout.println("Invoking method(" + method + ") error!"
                        + "fixKeytype:" + obj);
                e.printStackTrace(psout);
            }
            catch (Throwable ee)
            {
                System.err.println("when logging " + method
                        + "'s fixKeytype occur exception ");
            }
            finally
            {
                /* Delete by l90003110 20090204 */
                //                try
                //                {
                //                    fout.close();
                //                }
                //                catch (Throwable r)
                //                {
                //                }
                //                try
                //                {
                //                    psout.close();
                //                }
                //                catch (Throwable r)
                //                {
                //                }
                /* Delete by l90003110 20090204 */
            }
        }
        return v;
    }
    
    private Vector getMultiResponse(Document request)
            throws MalformedURLException, CIMException, IOException
    {
        Vector v = new Vector();
        Vector multi = getResponse(request);
        Vector resp = null;
        for (int i = 0; i < multi.size(); i++)
        {
            Hashtable simple = (Hashtable) multi.elementAt(i);
            String method = (String) simple.keySet().iterator().next();
            resp = (Vector) simple.get(method);
            if (method.startsWith("Enum") || method.startsWith("Asso")
                    || method.startsWith("Refe") || method.startsWith("ExecQ"))
            {
                if (resp.size() > 0
                        && resp.firstElement() instanceof CIMException)
                {
                    v.addElement(resp.firstElement());
                    continue;
                }
                v.addElement(resp);
            }
            else
            {
                try
                {
                    v.addElement(resp.firstElement());
                }
                catch (Exception e)
                {
                    v.addElement(null);
                }
            }
        }
        return v;
    }
    
    /**
     * @exception CIMException the namespace already exists or the user does
     *      not have write permission to create a namespace.
     *      
     */
    public synchronized void createNameSpace(String version,
            CIMNameSpace currNs, CIMNameSpace relNs) throws CIMException
    {
        
        try
        {
            CIMCreateNameSpaceOp cimop = new CIMCreateNameSpaceOp(relNs);
            cimop.setNameSpace(currNs);
            
            getFirstResponse("CreateInstance", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "createNameSpace", e);
        }
    }
    
    /**
     * @exception CIMException This client does not have the correct
     *  security token to close the session.  This is most likely
     *  a security breach.
     *
     * @exception CIMException XML error
     */
    public synchronized void close(String version) throws CIMException
    {
        shutdown();
        /*******fengwenliang �޸��ϱ�����˿�?*start****/
        //        try
        //        {
        //            _listenerLock.readLock().lock();
        //            if (listener != null)
        //            {
        //                listener.stop();
        //            }
        //        }
        //        finally
        //        {
        //            _listenerLock.readLock().unlock();
        //        }
        /*******fengwenliang �޸��ϱ�����˿�?*end****/
    }
    
    public synchronized void closeEventListener(int port) throws CIMException
    {
        HttpEventListenerMgr.getHttpEventListener(port).stop();
        HttpEventRequestHandlerMgr.getHttpEventRequestHandler(port).close();
    }
    
    public void shutdown()
    {
        synchronized (connLock)
        {
            if (conn != null)
            {
                conn.shutdown(true);
                conn = null;
            }
        }
    }
    
    public void forceShutdown(boolean isForce)
    {
        synchronized (connLock)
        {
            if (conn != null)
            {
                conn.shutdown(true);
                conn = null;
            }
            this.isForceDisconnect = isForce;
        }
    }
    
    /**
     * @exception CIMException the namespace does not exist or the user
     *      does not have permission to delete the namespace.
     * @exception CIMException XML error
     */
    public synchronized void deleteNameSpace(String version,
            CIMNameSpace currNs, CIMNameSpace ns) throws CIMException
    {
        
        try
        {
            CIMDeleteNameSpaceOp cimop = new CIMDeleteNameSpaceOp(ns);
            cimop.setNameSpace(currNs);
            getFirstResponse("DeleteInstance", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "deleteNameSpace", e);
        }
    }
    
    /**
     * @exception CIMException the class cannot be found or the user does
     *      not have permission to delete the class.
     * @exception CIMException XML error
     */
    public synchronized void deleteClass(String version, CIMNameSpace currNs,
            CIMObjectPath path) throws CIMException
    {
        try
        {
            CIMDeleteClassOp cimop = new CIMDeleteClassOp(path);
            cimop.setNameSpace(currNs);
            getFirstResponse("DeleteClass", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "deleteClass", e);
        }
    }
    
    /**
     * @exception CIMException the instance cannot be found in the namespace or
     *      the user does not have permission to delete the namespace.
     * @exception CIMException XML error
     */
    public synchronized void deleteInstance(String version,
            CIMNameSpace currNs, CIMObjectPath path) throws CIMException
    {
        try
        {
            CIMDeleteInstanceOp cimop = new CIMDeleteInstanceOp(path);
            cimop.setNameSpace(currNs);
            getFirstResponse("DeleteInstance", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "deleteInstance", e);
        }
    }
    
    /**
     * @exception CIMException the qualifier type cannot be found or the 
     *      user does not have permission to delete the qualifier type.
     * @exception CIMException XML error
     */
    public synchronized void deleteQualifierType(String version,
            CIMNameSpace currNs, CIMObjectPath path) throws CIMException
    {
        try
        {
            CIMDeleteQualifierTypeOp cimop = new CIMDeleteQualifierTypeOp(path);
            cimop.setNameSpace(currNs);
            getFirstResponse("DeleteQualifier", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "deleteQualifierType", e);
        }
    }
    
    /**
     * @exception CIMException the class cannot be found or the user does
     *          not have read permission to the namespace.
     * @exception CIMException XML error
     *
     */
    public synchronized Vector enumerateClasses(String version,
            CIMNameSpace currNs, CIMObjectPath path, boolean deep,
            boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin) throws CIMException
    {
        try
        {
            CIMEnumClassOp cimop = new CIMEnumClassOp(path, deep, localOnly,
                    includeQualifiers, includeClassOrigin);
            cimop.setNameSpace(currNs);
            return getFirstResponse("EnumerateClasses",
                    xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "enumerateClasses", e);
        }
    }
    
    /**
     * @exception CIMException the class cannot be found or the user does
     *          not have read permission to the namespace.
     * @exception CIMException XML error
     */
    public synchronized Vector enumerateClassNames(String version,
            CIMNameSpace currNs, CIMObjectPath path, boolean deep)
            throws CIMException
    {
        
        try
        {
            CIMEnumClassNamesOp cimop = new CIMEnumClassNamesOp(path, deep);
            cimop.setNameSpace(currNs);
            return getFirstResponse("EnumerateClassNames",
                    xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "enumerateClassNames", e);
        }
    }
    
    /**
     * @exception CIMException the namespace cannot be found or the user 
     *      does not have read permission to the namespace.
     * @exception CIMException XML error
     */
    public synchronized Vector enumNameSpace(String version,
            CIMNameSpace currNs, CIMObjectPath path, boolean deep)
            throws CIMException
    {
        try
        {
            path.setObjectName("__Namespace");
            CIMEnumNameSpaceOp cimop = new CIMEnumNameSpaceOp(path, deep);
            cimop.setNameSpace(currNs);
            return getFirstResponse("EnumerateInstanceNames",
                    xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "enumNameSpace", e);
        }
    }
    
    /**
     * @exception   CIMException the namespace cannot be found or the user
     *          does not have read permission to the namespace.
     * @exception   CIMException XML error
     */
    public Vector enumerateInstances(String version, CIMNameSpace currNs,
            CIMObjectPath path, boolean deep, boolean localOnly,
            boolean includeQualifiers, boolean includeClassOrigin,
            String propertyList[]) throws CIMException
    {
        
        try
        {
            CIMEnumInstancesOp cimop = new CIMEnumInstancesOp(path, deep,
                    localOnly, includeQualifiers, includeClassOrigin,
                    propertyList);
            cimop.setNameSpace(currNs);
            Document doc = xmlImpl.getXmlRequest(cimop);
            return getFirstResponse("EnumerateInstances", doc);
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "enumerateInstances", e);
        }
    }
    
    /**
     * @exception   CIMException the namespace cannot be found or the user
     *          does not have read permission to the namespace.
     * @exception   CIMException XML error
     */
    public Vector enumerateInstanceNames(String version, CIMNameSpace currNs,
            CIMObjectPath path) throws CIMException
    {
        try
        {
            CIMEnumInstanceNamesOp cimop = new CIMEnumInstanceNamesOp(path);
            cimop.setNameSpace(currNs);
            return getFirstResponse("EnumerateInstanceNames",
                    xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "enumerateInstanceNames", e);
        }
    }
    
    /**
     * @exception CIMException the namespace cannot be found or the user 
     *      does not have read permission to the namespace.
     * @exception CIMException XML error
     */
    public synchronized Vector enumQualifierTypes(String version,
            CIMNameSpace currNs, CIMObjectPath path) throws CIMException
    {
        Vector retval = new Vector();
        try
        {
            CIMEnumQualifierTypesOp cimop = new CIMEnumQualifierTypesOp(path);
            cimop.setNameSpace(currNs);
            for (Enumeration e = getFirstResponse("EnumerateQualifiers",
                    xmlImpl.getXmlRequest(cimop)).elements(); e.hasMoreElements();)
            {
                retval.addElement(e.nextElement());
            }
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "enumQualifierTypes", e);
        }
        return retval;
    }
    
    /**
     * @exception CIMException the class or namespace cannot be found or the
     *      user does not have read permission to the namespace.
     * @exception CIMException XML error
     */
    public synchronized CIMClass getClass(String version, CIMNameSpace currNs,
            CIMObjectPath name, boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin, String propertyList[])
            throws CIMException
    {
        try
        {
            CIMGetClassOp cimop = new CIMGetClassOp(name, localOnly,
                    includeQualifiers, includeClassOrigin, propertyList);
            cimop.setNameSpace(currNs);
            Vector rsp = getFirstResponse("GetClass",
                    xmlImpl.getXmlRequest(cimop));
            return (CIMClass) (rsp.firstElement());
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "getClass", e);
        }
    }
    
    /**
     * @exception CIMException the instance or class cannot be found or the 
     *      user does not have read permission to the namespace.
     * @exception CIMException XML error.
     */
    public synchronized CIMInstance getInstance(String version,
            CIMNameSpace currNs, CIMObjectPath name, boolean localOnly,
            boolean includeQualifiers, boolean includeClassOrigin,
            String propertyList[]) throws CIMException
    {
        try
        {
            CIMGetInstanceOp cimop = new CIMGetInstanceOp(name, localOnly,
                    includeQualifiers, includeClassOrigin, propertyList);
            cimop.setNameSpace(currNs);
            Vector rsp = getFirstResponse("GetInstance",
                    xmlImpl.getXmlRequest(cimop));
            CIMInstance inst = (CIMInstance) (rsp.firstElement());
            //Since the getInstance method only returns the instance and not the
            //name we have to fix the REFS
            Enumeration e = inst.getProperties().elements();
            while (e.hasMoreElements())
            {
                CIMProperty p = (CIMProperty) e.nextElement();
                if (p != null && p.isReference() && p.getValue() != null)
                {
                    fixCIMOjbectPath((CIMObjectPath) p.getValue().getValue());
                }
            }
            return inst;
            
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "getInstance", e);
        }
    }
    
    /**
     * @exception CIMException The invokeMethod method throws a CIM exception.
     */
    public synchronized CIMValue invokeMethod(String version,
            CIMNameSpace currNs, CIMObjectPath name, String methodName,
            CIMArgument[] inArgs, CIMArgument[] outArgs) throws CIMException
    {
        try
        {
            CIMInvokeArgsMethodOp cimop = new CIMInvokeArgsMethodOp(name,
                    methodName, inArgs, null);
            cimop.setNameSpace(currNs);
            Vector v = (Vector) (getFirstResponse(methodName,
                    xmlImpl.getXmlRequest(cimop)).firstElement());
            if (null != v && v.size() == 1
                    && v.elementAt(0) instanceof CIMException)
            {
                throw (CIMException) v.elementAt(0);
            }
            /*
             ArrayList ao = new ArrayList(v.size() - 1);
             for (int i = 1; i < v.size(); i++) {
             ao.add((CIMArgument)v.elementAt(i));
             }
             outArgs = (CIMArgument[])ao.toArray(new CIMArgument[ao.size()]);
             */
            //修改coverity forward null
            if(null == v)
            {
            	throw new NullPointerException();
            }
            
            for (int i = 1; i < v.size(); i++)
            {
                //ao.add((CIMArgument)v.elementAt(i));
                if ((outArgs != null) && (outArgs.length >= i))
                {
                    outArgs[i - 1] = (CIMArgument) v.elementAt(i);
                }
            }
            return (CIMValue) v.firstElement();
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "invokeMethod", e);
        }
    }
    
    /**
     * @exception CIMException the qualifier type cannot be found in the
     *      namespace or the user does not have read permission to the
     *      namespace.
     */
    public synchronized CIMQualifierType getQualifierType(String version,
            CIMNameSpace currNs, CIMObjectPath name) throws CIMException
    {
        try
        {
            CIMGetQualifierTypeOp cimop = new CIMGetQualifierTypeOp(name);
            cimop.setNameSpace(currNs);
            Vector rsp = getFirstResponse("GetQualifier",
                    xmlImpl.getXmlRequest(cimop));
            return (CIMQualifierType) (rsp.firstElement());
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "getQualifierType", e);
        }
    }
    
    /**
     * @exception CIMException the namespace cannot be found, the qualifier
     *      type already exists, or the user does not have write
     *      permission to the namespace.
     * @exception CIMException XML error
     */
    public synchronized void createQualifierType(String version,
            CIMNameSpace currNs, CIMObjectPath name, CIMQualifierType qt)
            throws CIMException
    {
        try
        {
            CIMCreateQualifierTypeOp cimop = new CIMCreateQualifierTypeOp(name,
                    qt);
            cimop.setNameSpace(currNs);
            getFirstResponse("SetQualifier", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "createQualifierType", e);
        }
    }
    
    /**
     * @exception CIMException The createClass method throws a CIM exception.
     */
    public synchronized void createClass(String version, CIMNameSpace currNs,
            CIMObjectPath name, CIMClass cc) throws CIMException
    {
        try
        {
            CIMCreateClassOp cimop = new CIMCreateClassOp(name, cc);
            cimop.setNameSpace(currNs);
            getFirstResponse("CreateClass", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "createClass", e);
        }
    }
    
    /**
     * @exception CIMException The createInstance method throws a CIM exception.
     */
    public synchronized CIMObjectPath createInstance(String version,
            CIMNameSpace currNs, CIMObjectPath name, CIMInstance ci)
            throws CIMException
    {
        try
        {
            CIMCreateInstanceOp cimop = new CIMCreateInstanceOp(name, ci);
            cimop.setNameSpace(currNs);
            Vector rsp = getCIFirstResponse("CreateInstance",
                    xmlImpl.getXmlRequest(cimop));
            CIMObjectPath cop = (CIMObjectPath) (rsp.firstElement());
            if (name.getNameSpace() != null && name.getNameSpace().length() > 0
                    && name.getNameSpace().startsWith("/"))
            {
                cop.setNameSpace(name.getNameSpace());
                cop.setHost(currNs.getHost());
            }
            return cop;
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "createInstance", e);
        }
    }
    
    /**
     * @exception CIMException The setQualifierType method throws a CIMException
     *      if the qualifier type already exists in the namespace.
     * @exception CIMException The setQualifierType method throws a CIMxception
     *      if an XML error is returned.
     */
    public synchronized void setQualifierType(String version,
            CIMNameSpace currNs, CIMObjectPath name, CIMQualifierType qt)
            throws CIMException
    {
        try
        {
            CIMSetQualifierTypeOp cimop = new CIMSetQualifierTypeOp(name, qt);
            cimop.setNameSpace(currNs);
            getFirstResponse("SetQualifier", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "setQualifierType", e);
        }
    }
    
    /**
     * @exception CIMException The setClass method throws a CIM exception
     *      if the class does not exist in the namespace.
     * @exception CIMException The setClass method throws a CIM exception
     *      if an XML error is returned.
     *
     */
    public synchronized void setClass(String version, CIMNameSpace currNs,
            CIMObjectPath name, CIMClass cc) throws CIMException
    {
        try
        {
            CIMSetClassOp cimop = new CIMSetClassOp(name, cc);
            cimop.setNameSpace(currNs);
            getFirstResponse("ModifyClass", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "setClass", e);
        }
    }
    
    /**
     * @exception CIMException The setInstance method throws a CIM exception
     *      if the instance does not exist in the namespace.
     * @exception CIMException The setInstance method throws a CIM exception
     *      if an XML error is returned.
     */
    public synchronized void setInstance(String version, CIMNameSpace currNs,
            CIMObjectPath name, CIMInstance ci, boolean includeQualifier,
            String[] propertyList) throws CIMException
    {
        
        try
        {
            CIMSetInstanceOp cimop = new CIMSetInstanceOp(name, ci,
                    includeQualifier, propertyList);
            cimop.setNameSpace(currNs);
            getFirstResponse("ModifyInstance", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "setInstance", e);
        }
    }
    
    /**
     * @exception CIMException The getProperty method throws a CIM exception
     *      if the property cannot be found in the namespace.
     * @exception CIMException The getProperty method throws a CIM exception
     *      if an XML error is returned.
     */
    public synchronized CIMValue getProperty(String version,
            CIMNameSpace currNs, CIMObjectPath name, String propertyName)
            throws CIMException
    {
        //WSI Bug175: No data type information from getProperty call
        // This is a bug in the specification, they only allow for 
        // a value to be returned - so no datatype info. This now
        // calls getCIMInstance with the propList including only the one
        // property.
        String[] propList = new String[1];
        propList[0] = propertyName;
        CIMInstance inst = getInstance(version,
                currNs,
                name,
                false,
                false,
                false,
                propList);
        CIMProperty prop = inst.getProperty(propertyName);
        if (prop == null)
        {
            throw new CIMException(CIMException.CIM_ERR_NO_SUCH_PROPERTY,
                    propertyName);
        }
        if (prop.getValue() != null)
        {
            return prop.getValue();
        }
        return null;
    }
    
    /**
     * @exception CIMException The setProperty method throws a CIM exception
     *      if the property cannot be found in the namespace.
     * @exception CIMException The setProperty method throws a CIM exception
     *      if an XML error is returned.
     */
    public synchronized void setProperty(String version, CIMNameSpace currNs,
            CIMObjectPath name, String propertyName, CIMValue cv)
            throws CIMException
    {
        try
        {
            CIMSetPropertyOp cimop = new CIMSetPropertyOp(name, propertyName,
                    cv);
            cimop.setNameSpace(currNs);
            getFirstResponse("SetProperty", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "setProperty", e);
        }
    }
    
    public synchronized Vector execQuery(String version, CIMNameSpace currNs,
            CIMObjectPath relNS, String query, String ql) throws CIMException
    {
        try
        {
            CIMExecQueryOp cimop = new CIMExecQueryOp(relNS, query, ql);
            cimop.setNameSpace(currNs);
            return getFirstResponse("ExecQuery", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "execQuery", e);
        }
    }
    
    public Vector associators(String version, CIMNameSpace currNs,
            CIMObjectPath objectName, String assocClass, String resultClass,
            String role, String resultRole, boolean includeQualifiers,
            boolean includeClassOrigin, String[] propertyList)
            throws CIMException
    {
        
        try
        {
            CIMAssociatorsOp cimop = new CIMAssociatorsOp(objectName,
                    assocClass, resultClass, role, resultRole,
                    includeQualifiers, includeClassOrigin, propertyList);
            cimop.setNameSpace(currNs);
            return getFirstResponse("Associators", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "associators", e);
        }
    }
    
    public Vector associatorNames(String version, CIMNameSpace currNs,
            CIMObjectPath objectName, String assocClass, String resultClass,
            String role, String resultRole) throws CIMException
    {
        try
        {
            CIMAssociatorNamesOp cimop = new CIMAssociatorNamesOp(objectName,
                    assocClass, resultClass, role, resultRole);
            cimop.setNameSpace(currNs);
            return getFirstResponse("AssociatorNames",
                    xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "associatorNames", e);
        }
    }
    
    public Vector references(String version, CIMNameSpace currNs,
            CIMObjectPath objectName, String resultClass, String role,
            boolean includeQualifiers, boolean includeClassOrigin,
            String[] propertyList) throws CIMException
    {
        try
        {
            CIMReferencesOp cimop = new CIMReferencesOp(objectName,
                    resultClass, role, includeQualifiers, includeClassOrigin,
                    propertyList);
            cimop.setNameSpace(currNs);
            return getFirstResponse("References", xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "references", e);
        }
        
    }
    
    public Vector referenceNames(String version, CIMNameSpace currNs,
            CIMObjectPath objectName, String resultClass, String role)
            throws CIMException
    {
        try
        {
            CIMReferenceNamesOp cimop = new CIMReferenceNamesOp(objectName,
                    resultClass, role);
            cimop.setNameSpace(currNs);
            return getFirstResponse("ReferenceNames",
                    xmlImpl.getXmlRequest(cimop));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "referenceNames", e);
        }
    }
    
    public Vector performOperations(String version,
            CIMOperation[] batchedOperations) throws CIMException
    {
        try
        {
            return getMultiResponse(xmlImpl.getXmlRequest(batchedOperations));
        }
        catch (CIMException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "performOperations", e);
        }
    }
    
    public void setListener(String version) throws CIMException
    {
        setListener(version, 0);
    }
    
    public void setListener(String version, int port) throws CIMException
    {
        /*******fengwenliang*********start****/
        try
        {
            _listenerLock.writeLock().lock();
            //listener = new HttpEventListener(clientListener, port);
            listener = HttpEventListenerMgr.getHttpEventListener(port);
            if (listener.isInit())
            {
                listener.addCIMListener(clientListener);
            }
            else
            {
                listener.init(clientListener, port);
            }
        }
        catch (Exception e)
        {
            throw new CIMException("XMLERROR", "setListener", e);
        }
        finally
        {
            _listenerLock.writeLock().unlock();
        }
        /*******fengwenliang**************end****/
    }
    
    /**
     * @deprecated 
     */
    public CIMInstance getIndicationHandler(CIMListener cl) throws CIMException
    {
        
        String host;
        int port;
        
        if (cl != null)
        {
            // We dont support this right now
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        
        // make sure listener is initialized
        /*******fengwenliang*************start****/
        try
        {
            _listenerLock.readLock().lock();
            host = listener.getHostIP();
            port = listener.getPort();
        }
        catch (Exception e)
        {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
        finally
        {
            _listenerLock.readLock().unlock();
        }
        /*******fengwenliang*********end****/
        
        CIMClass cc = getClass(version, nameSpace, new CIMObjectPath(
                HANDLERCLASS), false, false, false, null);
        CIMInstance ci = cc.newInstance();
        
        ci.setProperty("SystemName", new CIMValue(host));
        ci.setProperty("Name", new CIMValue(getUniqueString()));
        ci.setProperty("SystemCreationClassName", new CIMValue(
                "wbemsolutions_computersystem"));
        ci.setProperty("CreationClassName", new CIMValue(HANDLERCLASS));
        ci.setProperty("Destination", new CIMValue("http://" + host + ":"
                + port));
        //ci.setProperty("Owner", new CIMValue(""));
        return ci;
    }
    
    /**
     * getIndicationListener returns a populated CIM_IndicationListener
     *  instance to be used by clients
     */
    public CIMInstance getIndicationListener(CIMListener cl)
            throws CIMException
    {
        
        String host;
        int port;
        
        if (cl != null)
        {
            // We dont support this right now
            throw new CIMException(CIMException.CIM_ERR_NOT_SUPPORTED);
        }
        
        // make sure listener is initialized
        /*******fengwenliang**********start****/
        try
        {
            _listenerLock.readLock().lock();
            host = listener.getHostIP();
            port = listener.getPort();
        }
        catch (Exception e)
        {
            throw new CIMException(CIMException.CIM_ERR_FAILED, e);
        }
        finally
        {
            _listenerLock.readLock().unlock();
        }
        /*******fengwenliang******************end****/
        
        CIMClass cc = getClass(version, nameSpace, new CIMObjectPath(
                LISTENERCLASS), false, false, false, null);
        CIMInstance ci = cc.newInstance();
        ci.setProperty("Name", new CIMValue(getUniqueString()));
        ci.setProperty("SystemName", new CIMValue(host));
        ci.setProperty("SystemCreationClassName", new CIMValue(
                "wbemsolutions_computersystem"));
        ci.setProperty("CreationClassName", new CIMValue(LISTENERCLASS));
        ci.setProperty("Destination", new CIMValue("http://" + host + ":"
                + port));
        return ci;
    }
    
    // Must be synchronized because we need exclusive access to uniqueInt.
    private static synchronized String getUniqueString()
    {
        // I'm assuming that there will not be more than 4 billion requests
        // for a unique string in one second. And when daylight savings time
        // is turned off, there should not be more than 4 billion requests
        // in that extra hour. Unique string is required during session set
        // up and during event registration.
        return new String((new Date()).toString() + System.currentTimeMillis());
    }
    
    /** remove the following class when dmtf fixed the spec for keyvalue */
    private class ClassCache
    {
        private HashMap classCache = new HashMap();
        
        private ArrayList classCacheNames = new ArrayList();
        
        private static final int CACHE_SIZE = 200;
        
        public CIMProperty[] get(CIMObjectPath op) throws CIMException
        {
            String className = op.getObjectName();
            CIMProperty[] metakeys = (CIMProperty[]) classCache.get(className);
            if (metakeys == null)
            {
                metakeys = add(op);
            }
            return metakeys;
        }
        
        private CIMProperty[] add(CIMObjectPath op) throws CIMException
        {
            CIMClass cs = CIMClientXML.this.getClass(version, new CIMNameSpace(
                    "", op.getNameSpace()), new CIMObjectPath(
                    op.getObjectName()), false, true, false, null);
            Vector kv = cs.getKeys();
            if (kv == null)
            {
                kv = new Vector(0);
            }
            CIMProperty[] metakeys = new CIMProperty[kv.size()];
            kv.toArray(metakeys);
            if (classCacheNames.size() == CACHE_SIZE)
            {
                // max size has been met, remove the oldest class 
                // from the cache 
                String csName = (String) classCacheNames.remove(0);
                classCache.remove(csName);
            }
            classCache.put(op.getObjectName(), metakeys);
            classCacheNames.add(op.getObjectName());
            return metakeys;
        }
    }
    
    /** remove cache variable when dmtf fixes the spec for keyvalue */
    private ClassCache cache = new ClassCache();
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    private void fixKeytype(Object o) throws CIMException
    {
        if (o == null)
        {
            return;
        }
        if (o instanceof CIMObjectPath)
        {
            fixCIMOjbectPath((CIMObjectPath) o);
        }
        else if (o instanceof CIMInstance)
        {
            fixCIMInstance((CIMInstance) o);
        }
        else if (o instanceof CIMClass)
        {
            fixCIMClass((CIMClass) o);
        }
        else if (o instanceof Vector)
        {
            fixCIMArgument((Vector) o);
        }
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    private void fixCIMArgument(Vector v) throws CIMException
    {
        Enumeration e = v.elements();
        while (e.hasMoreElements())
        {
            Object o = e.nextElement();
            if (o instanceof CIMArgument)
            {
                CIMArgument argument = (CIMArgument) o;
                if ((argument.getType() != null)
                        && (argument.getType().isReferenceType()))
                {
                    if (argument.getType().isArrayType())
                    {
                        Vector args = (Vector) argument.getValue().getValue();
                        Enumeration eArgs = args.elements();
                        while (eArgs.hasMoreElements())
                        {
                            fixCIMOjbectPath((CIMObjectPath) eArgs.nextElement());
                        }
                    }
                    else
                    {
                        fixCIMOjbectPath((CIMObjectPath) argument.getValue()
                                .getValue());
                    }
                }
            }
        }
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    private void fixCIMInstance(CIMInstance inst) throws CIMException
    {
        CIMObjectPath path = inst.getObjectPath();
        fixCIMOjbectPath(path);
        inst.setObjectPath(path);
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    private void fixCIMClass(CIMClass cs) throws CIMException
    {
        CIMObjectPath path = cs.getObjectPath();
        if (path.getNameSpace() == null || path.getNameSpace().length() == 0)
        {
            path.setNameSpace(nameSpace.getNameSpace());
        }
        if (path.getHost() == null || path.getHost().length() == 0)
        {
            path.setHost(nameSpace.getHost());
        }
        cs.setObjectPath(path);
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    private static boolean isFixNeeded(CIMObjectPath path)
    {
        Vector keys = path.getKeys();
        if (keys == null || keys.size() == 0)
        {
            return false;
        }
        Iterator iter = keys.iterator();
        while (iter.hasNext())
        {
            CIMProperty cp = (CIMProperty) iter.next();
            if (cp.getType().equals(new CIMDataType(CIMDataType.INVALID)))
            {
                return true;
            }
            if (cp.isReference())
            {
                if (isFixNeeded((CIMObjectPath) cp.getValue().getValue()))
                {
                    return true;
                }
            }
        }
        return false;
        
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    private void fixCIMOjbectPath(CIMObjectPath path) throws CIMException
    {
        if (path.getNameSpace() == null || path.getNameSpace().length() == 0)
        {
            path.setNameSpace(nameSpace.getNameSpace());
        }
        if (path.getHost() == null || path.getHost().length() == 0)
        {
            path.setHost(nameSpace.getHost());
        }
        if (isFixNeeded(path))
        {
            CIMProperty[] metakeys = cache.get(path);
            Vector v = path.getKeys();
            CIMProperty keys[] = new CIMProperty[v.size()];
            v.toArray(keys);
            for (int i = 0; i < keys.length; i++)
            {
                if (keys[i].isReference())
                {
                    CIMObjectPath refpath = (CIMObjectPath) keys[i].getValue()
                            .getValue();
                    CIMProperty[] refmetakeys = cache.get(refpath);
                    fixInstanceName(refpath, refmetakeys);
                }
                else
                {
                    fixInstancekeyProperty(keys[i], metakeys);
                }
            }
        }
    }
    
    /*
     * remove this method when dmtf fixes the spec for keyvalue. 
     * Make this public since XmlResponder.java use it 
     *
     */
    static public void fixInstanceName(CIMObjectPath path,
            CIMProperty[] metakeys)
    {
        Vector v = path.getKeys();
        if (v == null || v.size() == 0)
        {
            return;
        }
        CIMProperty keys[] = new CIMProperty[v.size()];
        v.toArray(keys);
        for (int i = 0; i < keys.length; i++)
        {
            fixInstancekeyProperty(keys[i], metakeys);
        }
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    static private void fixInstancekeyProperty(CIMProperty p,
            CIMProperty[] metakeys)
    {
        for (int i = 0; i < metakeys.length; i++)
        {
            if (p.getName().equalsIgnoreCase(metakeys[i].getName()))
            {
                convertType(p, (metakeys[i]).getType());
            }
        }
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    static private void convertType(CIMProperty p, CIMDataType type)
    {
        if (p == null)
        {
            return;
        }
        String cimtype = type.toString();
        if (p != null && p.getValue() != null)
        {
            Object value = p.getValue().getValue();
            if (value instanceof String)
            {
                Object o = valueObject((String) value, cimtype);
                p.setValue(new CIMValue(o));
                p.setType(type);
            }
        }
    }
    
    /** remove this method when dmtf fixes the spec for keyvalue */
    static private Object valueObject(String value, String type)
    {
        Object o = null;
        
        type = type.length() > 0 ? type : "string";
        int radix = 10;
        if ((type.startsWith("sint") && (value.startsWith("0x")
                || value.startsWith("+0x") || value.startsWith("-0x")
                || value.startsWith("0X") || value.startsWith("+0X") || value.startsWith("-0X")))
                || (type.startsWith("uint") && (value.startsWith("0x") || value.startsWith("0X"))))
        {
            radix = 16;
            int dot = (value.indexOf("x") > 0 ? value.indexOf("x")
                    : value.indexOf("X")) + 1;
            value = (value.startsWith("-") ? "-" + value.substring(dot)
                    : value.substring(dot));
        }
        if (type.equals("boolean"))
        {
            o = Boolean.valueOf(value);
        }
        else if (type.equals("char16"))
        {
            o = new Character(value.charAt(0));
        }
        else if (type.equals("datetime"))
        {
            o = new CIMDateTime(value);
        }
        else if (type.equals("real32"))
        {
            o = new Float(value);
        }
        else if (type.equals("real64"))
        {
            o = new Double(value);
        }
        else if (type.equals("sint16"))
        {
            o = Short.valueOf(value, radix);
        }
        else if (type.equals("sint32"))
        {
            o = Integer.valueOf(value, radix);
        }
        else if (type.equals("sint64"))
        {
            o = Long.valueOf(value, radix);
        }
        else if (type.equals("sint8"))
        {
            o = Byte.valueOf(value, radix);
        }
        else if (type.equals("string"))
        {
            if (value == null)
            {
                o = "";
            }
            else
            {
                o = value;
            }
        }
        else if (type.equals("uint16"))
        {
            o = new UnsignedInt16((Integer.valueOf(value, radix)).intValue());
        }
        else if (type.equals("uint32"))
        {
            o = new UnsignedInt32((Long.valueOf(value, radix)).longValue());
        }
        else if (type.equals("uint64"))
        {
            o = new UnsignedInt64(new java.math.BigInteger(value, radix));
        }
        else if (type.equals("uint8"))
        {
            o = new UnsignedInt8((Short.valueOf(value, radix)).shortValue());
        }
        else if (value.indexOf("e") > 0 || value.indexOf("E") > 0)
        {
            o = new Double(value);
        }
        else if (value.startsWith("+") || value.startsWith("-"))
        {
            o = new Long(value);
        }
        return o;
    }
    
    synchronized private static void updateLogFile()
    {
        File f = null;
        try
        {
            if ((null != psout) && (null != fout))
            {
                psout.flush();
            }
            else
            {
                f = new File(LOG_NAME + "_" + LOG_DATE + "_" + LOG_NUMBER
                        + ".log");
                fout = new FileOutputStream(f, true);
                psout = new PrintStream(fout);
            }
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            String trace_sufx = sdf.format(new Date());
            if (!LOG_DATE.equals(trace_sufx))
            {
                LOG_DATE = trace_sufx;
                LOG_NUMBER = 1;
            }
            
            int logNumCurr = LOG_NUMBER;
            while (true)
            {
                f = new File(LOG_NAME + "_" + LOG_DATE + "_" + LOG_NUMBER
                        + ".log");
                if (f.exists())
                {
                    if (LOG_MAXBYTES < f.length())
                    {
                        LOG_NUMBER = LOG_NUMBER + 1;
                        continue;
                    }
                    else
                    {
                        if (logNumCurr == LOG_NUMBER)
                        {
                            return;
                        }
                        else
                        {
                            break;
                        }
                    }
                }
                else
                {
                    f.createNewFile();
                    break;
                }
            }
            
            if (null != f)
            {
                psout.close();
                fout.close();
                
                fout = new FileOutputStream(f, true);
                psout = new PrintStream(fout);
            }
        }
        catch (Exception ex)
        {
        }
    }
}
