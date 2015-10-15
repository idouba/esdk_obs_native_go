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

package javax.wbem.client;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;
import javax.wbem.security.ClientSecurityContext;
import javax.wbem.security.ClientSecurityFactory;

/**
 * Constructs a CIM client on the local host (default)
 * or the specified host. This class connect to the WBEM Server
 * for this client session to perform WBEM operations,
 * such as, adding, modifying, or deleting a CIM class, CIM instance, 
 * and CIM qualifier type in a namespace.  
 * <p>
 * A WBEM client application connects to a WBEM Server to establish
 * an initial connection when it needs to perform WBEM operations.
 *
 * @author      Sun Microsystems, Inc.
 * @since       WBEM 1.0
 */
public class CIMClient implements CIMOMHandle
{
    
    private String version;
    
    private CIMClientAPI api;
    
    private CIMNameSpace emptyNameSpace;
    
    private CIMNameSpace nameSpace;
    
    protected Hashtable subIdListenerMap;
    
    private ClientSecurityContext csc = null;
    
    private static String protocol;
    
    /**
     * WBEM Query Language level 1 is currently supported 
     *
     * @see CIMClient#execQuery(CIMObjectPath, String, String)
     */
    public final static String WQL = "WQL";
    
    /**
     * Constant identifying the CIM operations over XML protocol.
     */
    public static final String CIM_XML = "cim-xml";
    
    /**
     * Constant identifying the CIM operations over RMI protocol.
     * @deprecated
     */
    public static final String CIM_RMI = "cim-rmi";
    
    private static ClientListener clientListener = new ClientListener();
    
    //使用阻塞对表，控制最大的并发数
    private static Executor executor = Executors.newFixedThreadPool(5);
    
    /**
     * This class behaves as a multiplexer for application level listeners.
     * It aggreagates all the application listeners, and delivers events from
     * the underlying transport to them. That way the underlying transport
     * deals with just one listener.
     */
    private static class ClientListener implements CIMListener
    {
        /*******fengwenliang �޸��ϱ�����˿�**start****/
        private ReadWriteLock _listLock = new ReentrantReadWriteLock(); // ������
        
        //private List listenerList = new ArrayList();
        
        private Set<CIMListener> listenerList = new HashSet<CIMListener>();
        
        public void addCIMListener(CIMListener l)
        {
            
            if (l == null)
            {
                return;
            }
            try
            {
                _listLock.writeLock().lock();
                
//                if (!listenerList.contains(l))
//                {
//                    listenerList.clear();
//                    listenerList.add(l);
//                }
                listenerList.add(l);
            }
            finally
            {
                _listLock.writeLock().unlock();
            }
            
        } // addCIMListener
        
        public void removeCIMListener(CIMListener l)
        {
            try
            {
                _listLock.writeLock().lock();
                
                listenerList.remove(l);
            }
            finally
            {
                _listLock.writeLock().unlock();
            }
            
        } // removeCIMListener
        
        private class IndicationDeliverer implements Runnable
        {
            
            CIMEvent e;
            
            IndicationDeliverer(CIMEvent e)
            {
                this.e = e;
            } // constructor
            
            public void run()
            {
                Thread.currentThread().setName("WBEM IndicationDeliverer"); // add by l90003110 20090228
                
                // clone to avoid concurrent access problems.
                List<CIMListener> l = new ArrayList<CIMListener>();
                try
                {
                    _listLock.readLock().lock();
                    l.addAll(listenerList);
                }
                finally
                {
                    _listLock.readLock().unlock();
                }
                Iterator i = l.iterator();
                while (i.hasNext())
                {
                    CIMListener listener = (CIMListener) i.next();
                    listener.indicationOccured(e);
                }
            } // run
            /*******fengwenliang �޸��ϱ�����˿�**end****/
        } // IndicationDeliverer
        
        // Event transport delivers events through this.
        public void indicationOccured(CIMEvent e)
        {
            // Step through the list and deliver. I'd say we must pass the
            // event on to another thread so that the event transport can
            // immediately return to the CIMOM without blocking.
            executor.execute(new IndicationDeliverer(e));
        } // indicationOccured
        
    } // ClientListener
    
    /**
     * Creates a new client connection to the WBEM Server on the host and
     * namespace specified in the namespace object, using the specified 
     * principal and credential to authenticate the client user identity to the 
     * WBEM Server. 
     *
     * @param name The namespace in which operations will be performed.  This
     *             can be a URL that includes the protocol scheme and optional
     *             port information. See the DMTF WBEM URI Specification for the
     *             format. Example: https://192.168.0.4:5989/interop. This will
     *             use the https protocol to connect on port 5989 
     * @param principal The client user principal identity.
     * @param credential The client user credential for authentication.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   NO_SUCH_PRINCIPAL (user account does not exist),
     *   INVALID_CREDENTIAL (invalid password),
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>  
     */
    public CIMClient(CIMNameSpace name, Principal principal, Object credential)
            throws CIMException
    {
        
        this(name, principal, credential, CIM_XML);
        
    } // constructor
    
    /**
     * Creates a new client connection to the WBEM Server on the host and
     * namespace specified in the namespace object, using the specified 
     * principal and credential to authenticate the client user identity to the 
     * WBEM Server.  This connection uses the specified protocol to send 
     * messages to the WBEM Server.
     *
     * @param name The namespace in which operations will be performed
     * @param principal The client user principal identity
     * @param credential The client user credential for authentication
     * @param protocol The protocol to use for sending messages; e.g. CIM_XML
     * @exception CIMException   If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   NO_SUCH_PRINCIPAL (user account does not exist),
     *   INVALID_CREDENTIAL (invalid password),
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated   
     */
    public CIMClient(CIMNameSpace name, Principal principal, Object credential,
            String protocol) throws CIMException
    {
        
        initTrace();
        
        setProtocol(protocol);
        
        version = Version.major + "";
        
        // Make sure we have a CIM name space; default to local system.
        if (name == null)
        {
            name = new CIMNameSpace();
        }
        
        // Must check for a null Principal or a Principal with
        // a null name.  Either one croaks the authentication
        // exchange.  Return an invalid principal error.
        if (principal == null)
        {
            throw new CIMSecurityException(
                    CIMSecurityException.NO_SUCH_PRINCIPAL);
        }
        String str = principal.getName();
        if ((str == null) || (str.trim().length() == 0))
        {
            throw new CIMSecurityException(
                    CIMSecurityException.NO_SUCH_PRINCIPAL);
        }
        
        // Must have a non-null credential for the authentication exchange.
        // Leave testing for specific types of credentials to each
        // authentication mechanism.
        if (credential == null)
        {
            throw new CIMSecurityException(
                    CIMSecurityException.INVALID_CREDENTIAL);
        }
        
        name.setNameSpace("/" + name.getNameSpace());
        nameSpace = name;
        
        // create an empty name space
        emptyNameSpace = new CIMNameSpace("", "");
        
        //see if the protocol is set in the NameSpace class
        try
        {
            if (nameSpace.getScheme() != null)
            {
                String p = nameSpace.getScheme();
                if (p == null)
                {
                    
                }
                else
                {
                    //we should probably require them to set cim-xml and cim-rmi 
                    //but for now we will handle both...
                    if ((p.equalsIgnoreCase("http"))
                            || (p.equalsIgnoreCase("https")))
                    {
                        p = CIM_XML;
                    }
                    if (p.equalsIgnoreCase("rmi"))
                    {
                        p = CIM_RMI;
                    }
                }

                setProtocol(p);
            }
        }
        catch (Exception e)
        {
        }
        
        //Get DEBUG flag from system property        
        int debug = 0;
        try
        {
            String dbg = System.getProperty("wbem.debug.xml", "0");
            if (!dbg.equals("0"))
            {
                debug = new Integer(dbg).intValue();
            }
        }
        catch (Exception e)
        {
            //throw away on purpose....
        }
        // Get the transport service.  Transport factory will validate
        // the protocol and load right client implementation class.
        api = CIMClientFactory.getClientAPI(version,
                nameSpace,
                this.getProtocol(),
                debug,
                clientListener);
        
        // Reset the protocol name; it may have been null in the
        // constructor parameter indicating the default.  At this
        // point, we have selected a transport protocol.
        setProtocol(api.getProtocol());
        
        // Get the client security context; assume default mechanism.
        // Security factory will figure out right default for the protocol.
        csc = ClientSecurityFactory.createClientSecurity(nameSpace,
                principal,
                credential,
                null,
                this.getProtocol());
        
        // Ask the transport to initialize the security context (authenticate)
        api.initSecurityContext(version, csc);
        
    } // constructor
    
    /**
     * Creates a CIM namespace, a directory containing CIM classes and 
     * CIM instances. When a client application connects to the WBEM 
     * Server, it specifies a namespace. All subsequent 
     * operations occur within that namespace on the WBEM Server 
     * host.
     *
     * @param ns    The CIMNameSpace object that specifies a string
     *               for the host and a string for the namespace
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_ALREADY_EXISTS (if namespace already exists),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated
     * The Java WBEM Services createNameSpace API is a convenience method. 
     * This API uses createInstance on __Namespace class. The CIM 
     * Operations spec has deprecated the __Namespace for CIM_Namespace.  This is 
     * a better design, but it is more complex and doesn't lend itself to the 
     * this simple convenience method. There is no loss in functionality. 
     * Developers must now use createInstance on the __Namespace or 
     * CIM_Namespace class to perform this task.
     */
    public synchronized void createNameSpace(CIMNameSpace ns)
            throws CIMException
    {
        
        api.createNameSpace(version, checkNameSpace(ns), ns);
        
    } // createNameSpace
    
    /**
     * Closes the client connection to the WBEM Server. This will
     * enable the WBEM Server to free resources related to the client 
     * session. Additionally local indication listeners will be stopped.
     *
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public synchronized void close() throws CIMException
    {
        
        api.close(version);
        
    } // close
    
    public synchronized void closeEventListener(int port) throws CIMException
    {
        api.closeEventListener(port);
    }
    /**
     * Deletes the specified namespace.
     *
     * @param   ns  The CIMNameSpace object that identifies the namespace
     *          to be deleted
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (if namespace does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated  The Java WBEM Services deleteNameSpace API is a 
     * convenience method. This API uses deleteInstance on __Namespace class. 
     * The CIM Operations spec has deprecated the __Namespace for CIM_Namespace.
     * This is a better design, but it is more complex and doesn't lend itself 
     * to the this simple convenience method. There is no loss in functionality.
     * Developers must now use deleteInstance on the __Namespace or 
     * CIM_Namespace class to perform this task.
     */
    public synchronized void deleteNameSpace(CIMNameSpace ns)
            throws CIMException
    {
        
        api.deleteNameSpace(version, checkNameSpace(ns), ns);
        
    } // deleteNameSpace
    
    /**
     * Deletes the CIM class for the object specified by the CIM object path.
     *
     * @param   path    The CIMObjectPath identifying the class to delete
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the CIM Class to be deleted does not exisT),
     *   CIM_ERR_CLASS_HAS_CHILDREN (the CIM Class has one or more subclasses
     *   which cannot be deleted),
     *   CIM_ERR_CLASS_HAS_INSTANCES (the CIM Class has one or more instances
     *   which cannot be deleted),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public synchronized void deleteClass(CIMObjectPath path)
            throws CIMException
    {
        
        api.deleteClass(version, checkNameSpace(path), path);
        
    } // deleteClass
    
    /**
     * Deletes the CIM instance specified by the CIM object path. The 
     * following code sample uses instance name enumeration to retrieve the
     * names of all instances of the specified class, prints the name of each 
     * instance, then deletes the instance.
     * <p>
     * <pre>
     * <code>CIMObjectPath cop = new CIMObjectPath("CIM_FooClass");</code>
     * <code>Enumeration e = cimomHandle.enumerateInstanceNames(cop);</code>
     * <code>while(e.hasMoreElements()) {</code>
     * <code>     CIMObjectPath op = (CIMObjectPath)e.nextElement();</code>
     * <code>     System.out.println(op.toString());</code>
     * <code>     cimomHandle.deleteInstance(op);</code>
     * <code>}</code>
     * </pre>
     * @param   path    The object path of the instance to be deleted. It must 
     *          include all of the keys.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if the instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public synchronized void deleteInstance(CIMObjectPath path)
            throws CIMException
    {
        
        api.deleteInstance(version, checkNameSpace(path), path);
        
    } // deleteInstance
    
    /**
     * Deletes the CIM qualfier type in the namespace specified by the 
     * CIM object path.
     *
     * @param path  the CIMObjectPath identifying the CIM qualifier
     *          to delete
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the requested Qualifier declaration did not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre> 
     */
    public synchronized void deleteQualifierType(CIMObjectPath path)
            throws CIMException
    {
        
        api.deleteQualifierType(version, checkNameSpace(path), path);
        
    } // deleteQualifierType
    
    /**
     * Enumerates the namespaces within the namespace specified by the CIM 
     * object path. 
     *
     * @param   path    The CIMObjectPath identifying the namespace to be 
     *          enumerated.
     * @param   deep    If set to true, the enumeration returned will contain 
     *          the entire hierarchy of namespaces present under the 
     *          enumerated namespace. If set to false the enumeration 
     *          will return only the first level children of the
     *          enumerated namespace.
     * @return  Enumeration of namespace names as CIMObjectPath. If none are 
     *      found, NULL is returned.
     * @exception  CIMException If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     * @deprecated  The Java WBEM Services enumNameSpace API is a 
     *          convenience method. This API uses enumerateInstances 
     *          on __Namespace class. The CIM Operations spec has 
     *          deprecated the __Namespace for CIM_Namespace. This is
     *          a better design, but it is more complex and doesn't 
     *          lend itself to the this simple convenience method. 
     *          There is no loss in functionality. Developers must now 
     *          use enumerateInstance on the __Namespace or 
     *          CIM_Namespace class to perform this task.
     */
    public synchronized Enumeration enumNameSpace(CIMObjectPath path,
            boolean deep) throws CIMException
    {
        
        return (api.enumNameSpace(version, checkNameSpace(path), path, deep)).elements();
    } // enumNameSpace
    
    /**
     * Enumerates the first level children of the class specified in the path.
     * The entire class contents, not just the class names, are returned. This 
     * method behaves as if:
     * <pre>
     *   deep=false, 
     *   localOnly=true,
     *   includeQualifiers=true,
     *   includeClassOrigin=false.
     * </pre>
     * The classes returned will include all the qualifiers. CLASSORIGIN
     * information, indicating where the class element (property, method, 
     * reference) is first defined, will not be included.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          namespace and class name components are used. All
     *          other information (e.g. Keys) is ignored.
     * @return  Enumeration of CIMClass. If none are found, NULL is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public Enumeration enumerateClasses(CIMObjectPath path) throws CIMException
    {
        
        return enumerateClasses(path, false, true, true, false);
        
    } // enumerateClasses
    
    /**
     * Enumerates the first level children of the class specified in the path.
     * The entire class contents, not just the class names, are returned. This 
     * method behaves as if:
     * <pre>
     *   localOnly=true,
     *   includeQualifiers=true,
     *   includeClassOrigin=false.
     * </pre>
     * The classes returned will include all the qualifiers. CLASSORIGIN
     * information, indicating where the class element (property, method, 
     * reference) is first defined, will not be included.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          namespace and class name components are used. All
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned will contain the 
     *          contents of all subclasses derived from the specified 
     *          class. If false, the enumeration will return only the 
     *          contents of the first level children of the specified 
     *          class.
     * @return  Enumeration of CIMClass. If none are found, NULL is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public Enumeration enumerateClasses(CIMObjectPath path, boolean deep)
            throws CIMException
    {
        
        return enumerateClasses(path, deep, true, true, false);
        
    } // enumerateClasses
    
    /**
     * Enumerates the first level children of the class specified in the path.
     * The entire class contents, not just the class names, are returned. This 
     * method behaves as if:
     * <pre>
     *   includeQualifiers=true,
     *   includeClassOrigin=false.
     * </pre>
     * The classes returned will include all the qualifiers. CLASSORIGIN
     * information, indicating where the class element (property, method, 
     * reference) is first defined, will not be included.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          namespace and class name components are used. All
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned will contain the 
     *          contents of all subclasses derived from the specified 
     *          class. If false, the enumeration will return only the 
     *          contents of the first level children of the specified 
     *          class.
     * @param   localOnly   If true, only elements (properties, methods and
     *              qualifiers) defined or overridden within the 
     *              class are included in the enumeration returned.
     *              If false, all elements of the class definition 
     *              are returned. 
     * @return  Enumeration of CIMClass. If none are found, NULL is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public Enumeration enumerateClasses(CIMObjectPath path, boolean deep,
            boolean localOnly) throws CIMException
    {
        
        return enumerateClasses(path, deep, localOnly, true, false);
        
    } // enumerateClasses
    
    /**
     * Enumerates the first level children of the class specified in the path.
     * The entire class contents, not just the class names, are returned. This 
     * method behaves as if:
     * <pre>
     *   includeClassOrigin=false.
     * </pre>
     * The classes returned will include all the qualifiers. CLASSORIGIN
     * information, indicating where the class element (property, method, 
     * reference) is first defined, will not be included.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          namespace and class name components are used. All
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned will contain the 
     *          contents of all subclasses derived from the specified 
     *          class. If false, the enumeration will return only the 
     *          contents of the first level children of the specified 
     *          class.
     * @param   localOnly   If true, only elements (properties, methods and
     *              qualifiers) defined or overridden within the 
     *              class are included in the enumeration returned.
     *              If false, all elements of the class definition 
     *              are returned. 
     * @param   includeQualifiers   If true, all Qualifiers for each class
     *                  (including Qualifiers on the Class and 
     *                  and its elements) MUST be included. If 
     *                  false, no Qualifiers are present in the
     *                  classes returned.
     * @return  Enumeration of CIMClass. If none are found, NULL is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public Enumeration enumerateClasses(CIMObjectPath path, boolean deep,
            boolean localOnly, boolean includeQualifiers) throws CIMException
    {
        
        return (enumerateClasses(path,
                deep,
                localOnly,
                includeQualifiers,
                false));
        
    } // enumerateClasses
    
    /**
     * Enumerates the class specified in the path. The entire class contents,
     * not just the class names, are returned.
     *
     * @param   path        The object path of the class to be enumerated. 
     *              Only the name space and class name components 
     *              are used. Any other information (e.g. Keys) is 
     *              ignored.
     * @param   deep        If true, the enumeration returned contains the 
     *              specified class and all subclasses. If false, 
     *              the enumeration returned contains only the 
     *              contents of the first level children of the 
     *              specified class.
     * @param   localOnly   If true, only elements (properties, methods and
     *              qualifiers) defined in, or overridden in the 
     *              class are included in the response. If false, 
     *              all elements of the class definition are 
     *              returned. 
     * @param   includeQualifiers If true, all Qualifiers for each Class and its
     *              elements (properties, methods, references). If
     *              false, no Qualifiers are present in the classes
     *              returned 
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute MUST be 
     *              present on all appropriate elements in each 
     *              classes returned. If false, no CLASSORIGIN 
     *              attributes are present in each class returned.
     *              CLASSORIGIN is attached to an element to
     *              indicate the class in which it was first 
     *              defined.
     * @return  Enumeration of CIMClass. If none are found, NULL is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre> 
     * @see CIMClient#enumerateClassNames(CIMObjectPath)
     * @see CIMClient#enumerateClasses(CIMObjectPath)
     */
    public synchronized Enumeration enumerateClasses(CIMObjectPath path,
            boolean deep, boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin) throws CIMException
    {
        
        Vector v = api.enumerateClasses(version,
                checkNameSpace(path),
                path,
                deep,
                localOnly,
                includeQualifiers,
                includeClassOrigin);
        return v.elements();
        
    } // enumerateClasses
    
    /**
     * Enumerates the class specified in the path. The class names are returned
     * as an enumeration of CIMObjectPath.
     *
     * @param   path    The CIMObjectPath identifying the class to be 
     *          enumerated. If the class name in the object path 
     *          specified is null, all base classes in the target 
     *          namespace are returned.
     * @return  Enumeration of class names (CIMObjectPaths).    
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public Enumeration enumerateClassNames(CIMObjectPath path)
            throws CIMException
    {
        
        return enumerateClassNames(path, false);
        
    } // enumerateClassNames
    
    /**
     * Enumerates the class specified in the path. The class names are returned
     * as an enumeration of CIMObjectPath.
     *
     * @param   path    The CIMObjectPath identifying the class to be 
     *          enumerated. If the class name in the object path 
     *          specified is null, all base classes in the target 
     *          namespace are returned.
     * @param   deep    If true, the enumeration returned will contain the 
     *          names of all classes derived from the class being
     *          enumerated. If false, the enumeration returned 
     *          contains only the names of the first level children
     *          of the class.
     * @return  Enumeration of class names as CIMObjectPaths.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class that is the basis for this
     *   enumeration does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public synchronized Enumeration enumerateClassNames(CIMObjectPath path,
            boolean deep) throws CIMException
    {
        
        return (api.enumerateClassNames(version,
                checkNameSpace(path),
                path,
                deep)).elements();
    } // enumerateClassNames
    
    /**
     * Returns the object paths of all instances of the class specified. The 
     * object paths of all derived instances of the specified classes are also
     * returned.
     * 
     * @param   path    The CIMObjectPath identifying the class whose instances 
     *          are to be enumerated. Only the name space and class 
     *          name components are used. All other information (e.g. 
     *          Keys) is ignored.
     * @return  Enumeration of instance names as CIMObjectPaths. If no instances
     *      are found, NULL is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public synchronized Enumeration enumerateInstanceNames(CIMObjectPath path)
            throws CIMException
    {
        
        return (api.enumerateInstanceNames(version, checkNameSpace(path), path)).elements();
    } // enumerateInstanceNames
    
    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. The enumeration also includes all 
     * instances of all the classes in the specified class's hierarchy. This
     * method behaves as if:
     *
     * <pre>
     * deep=true,
     * localOnly=true,
     * includeQualifiers=false,
     * includeClassOrigin=false,
     * propertyList=null.
     * </pre>
     * Qualifiers information is not present in the instances returned.
     * The CLASSORIGIN attribute is also not present on each returned instance.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          name space and class name components are used. Any 
     *          other information (e.g. Keys) is ignored.
     * @return  Enumeration of CIMInstance. Each instance is filtered as 
     *      indicated by the parameters specified. If no CIMInstances of
     *      the specified class are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *  </pre>
     */
    public Enumeration enumerateInstances(CIMObjectPath path)
            throws CIMException
    {
        
        return enumerateInstances(path, true, true, false, false, null);
        
    } // enumerateInstances
    
    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. This method behaves as if:
     *
     * <pre>
     *   localOnly=true,
     *   includeQualifiers=false,
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * Qualifiers information is not present in the instances returned.
     * The CLASSORIGIN attribute is also not present on each returned instance.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          name space and class name components are used. Any 
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned contains all 
     *          instances of the specified class and all classes 
     *          derived from it. If false, only names of instances 
     *          of the specified class are returned.
     * @return  Enumeration of CIMInstance. Each instance is filtered as 
     *      indicated by the parameters specified. If no CIMInstances of
     *      the specified class are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *  </pre>
     */
    public Enumeration enumerateInstances(CIMObjectPath path, boolean deep)
            throws CIMException
    {
        
        return enumerateInstances(path, deep, true, false, false, null);
        
    } // enumerateInstances
    
    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. The enumeration returned also includes
     * instances of all the classes in the specified class's hierarchy. This 
     * method behaves as if:
     *
     * <pre>
     *   includeQualifiers=false,
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * Qualifiers information is not present in the instances returned.
     * The CLASSORIGIN attribute is also not present on each returned instance.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          name space and class name components are used. Any 
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned contains all 
     *          instances of the specified class and all classes 
     *          derived from it. If false, only names of instances 
     *          of the specified class are returned.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the instances returned.
     * @return  Enumeration of CIMInstance. Each instance is filtered as 
     *      indicated by the parameters specified. If no CIMInstances of
     *      the specified class are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *  </pre>
     */
    public Enumeration enumerateInstances(CIMObjectPath path, boolean deep,
            boolean localOnly) throws CIMException
    {
        
        return enumerateInstances(path, deep, localOnly, false, false, null);
        
    } // enumerateInstances
    
    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. The enumeration returned also includes
     * instances of all the classes in the specified class's hierarchy. This 
     * method behaves as if:
     *
     * <pre>
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * The CLASSORIGIN attribute is not present on each returned instance.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          name space and class name components are used. Any 
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned contains all 
     *          instances of the specified class and all classes 
     *          derived from it. If false, only names of instances 
     *          of the specified class are returned.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the instances returned.
     * @param   includeQualifiers If true, all Qualifiers for each instance 
     *              are included in the instances returned. If 
     *              false, no Qualifier information is contained 
     *              in the Instances returned.
     * @return  Enumeration of CIMInstance. Each instance is filtered as 
     *      indicated by the parameters specified. If no CIMInstances of
     *      the specified class are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *  </pre>
     */
    public Enumeration enumerateInstances(CIMObjectPath path, boolean deep,
            boolean localOnly, boolean includeQualifiers) throws CIMException
    {
        
        return enumerateInstances(path,
                deep,
                localOnly,
                includeQualifiers,
                false,
                null);
        
    } // enumerateInstances
    
    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. The enumeration returned also includes
     * instances of all the classes in the specified class's hierarchy. This 
     * method behaves as if:
     *
     * <pre>
     *   propertyList=null.
     * </pre>
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          name space and class name components are used. Any 
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned contains all 
     *          instances of the specified class and all classes 
     *          derived from it. If false, only names of instances 
     *          of the specified class are returned.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the instances returned.
     * @param   includeQualifiers If true, all Qualifiers for each instance 
     *              are included in the instances returned. If 
     *              false, no Qualifier information is contained 
     *              in the Instances returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              instances returned. If false, no CLASSORIGIN 
     *              attributes are present in the instances 
     *              returned. CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @return  Enumeration of CIMInstance. Each instance is filtered as 
     *      indicated by the parameters specified. If no CIMInstances of
     *      the specified class are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *  </pre>
     */
    public Enumeration enumerateInstances(CIMObjectPath path, boolean deep,
            boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin) throws CIMException
    {
        
        return enumerateInstances(path,
                deep,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                null);
        
    } // enumerateInstances
    
    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. The enumeration returned could include
     * instances of all the classes in the specified class's hierarchy.
     *
     * @param   path    The object path of the class to be enumerated. Only the
     *          name space and class name components are used. Any 
     *          other information (e.g. Keys) is ignored.
     * @param   deep    If true, the enumeration returned contains all 
     *          instances of the specified class and all classes 
     *          derived from it. If false, only names of instances 
     *          of the specified class are returned.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the instances returned.
     * @param   includeQualifiers If true, all Qualifiers for each instance 
     *              are included in the instances returned. If 
     *              false, no Qualifier information is contained 
     *              in the Instances returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              instances returned. If false, no CLASSORIGIN 
     *              attributes are present in the instances 
     *              returned. CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @param   propertyList    An array of property names used to filter what 
     *              is contained in the instances returned. Each 
     *              instance returned <b>only</b> contains elements
     *              for the properties of the names specified. 
     *              Duplicate and invalid property names are ignored
     *              and the request is otherwise processed normally.
     *              An empty array indicates that no properties 
     *              should be returned. A <b>null</b> value 
     *              indicates that all properties should be 
     *              returned.
     * @return  Enumeration of CIMInstance. Each instance is filtered as 
     *      indicated by the parameters specified. If no CIMInstances of
     *      the specified class are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *  </pre>
     */
    public synchronized Enumeration enumerateInstances(CIMObjectPath path,
            boolean deep, boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin, String propertyList[])
            throws CIMException
    {
        
        Vector v = api.enumerateInstances(version,
                checkNameSpace(path),
                path,
                deep,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        return v.elements();
        
    } // enumerateInstances
    
    /**
     * Enumerates the qualifiers defined in the specified namespace.
     *
     * @param   path    The CIMObjectPath identifying the namespace whose
     *          qualifier definitions are to be enumerated.
     * @return  Enumeration of CIMQualifierType objects. If no qualifiers are
     *      found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public synchronized Enumeration enumQualifierTypes(CIMObjectPath path)
            throws CIMException
    {
        
        Vector v = api.enumQualifierTypes(version, checkNameSpace(path), path);
        return v.elements();
        
    } // enumQualifierTypes
    
    /**
     * Returns the CIMClass for the specified CIMObjectPath. The entire class
     * contents, not just the class name, is returned. This method behaves as
     * if:
     * <pre>
     *   localOnly=true,
     *   includeQualifiers=true,
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * The CLASSORIGIN attribute is not present on the CIMClass returned. All
     * Qualifier information be present in the CIMClass returned. Only locally
     * defined and overridden properties will be contained in the CIMClass 
     * returned.
     *
     * @param   name        The object path of the class to be returned. 
     *              Only the name space and class name components 
     *              are used. All other information (e.g. keys) is 
     *              ignored.
     * @return  The CIM class identified by the CIMObjectPath
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the request CIM Class does not exist in the
     *   specified namespace) 
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     * @see CIMClient#enumerateClasses(CIMObjectPath)
     * @see CIMClient#enumerateClassNames(CIMObjectPath)
     */
    public CIMClass getClass(CIMObjectPath name) throws CIMException
    {
        return new CIMClass();
        //        return getClass(name, true, true, false, null);
        
    } // getClass
    
    /**
     * Returns the CIMClass for the specified CIMObjectPath. The entire class
     * contents, not just the class name, is returned. This method behaves as
     * if:
     * <pre>
     *   includeQualifiers=true,
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * The CLASSORIGIN attribute is not present on the CIMClass returned. All
     * Qualifier information and all properties will also be contained in the 
     * CIMClass returned.
     *
     * @param   name        The object path of the class to be returned. 
     *              Only the name space and class name components 
     *              are used. All other information (e.g. keys) is 
     *              ignored.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMClass returned. If false,
     *              all elements of the class definition are 
     *              returned. 
     * @return  The CIM class identified by the CIMObjectPath
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the request CIM Class does not exist in the
     *   specified namespace) 
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     * @see CIMClient#enumerateClasses(CIMObjectPath)
     * @see CIMClient#enumerateClassNames(CIMObjectPath)
     */
    public CIMClass getClass(CIMObjectPath name, boolean localOnly)
            throws CIMException
    {
        
        return getClass(name, localOnly, true, false, null);
        
    } // getClass
    
    /**
     * Returns the CIMClass for the specified CIMObjectPath. The entire class
     * contents, not just the class name, is returned. This method behaves as
     * if:
     * <pre>
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * The CLASSORIGIN attribute is not present on the CIMClass returned. All
     * properties will also be contained in the CIMClass returned.
     *
     * @param   name        The object path of the class to be returned. 
     *              Only the name space and class name components 
     *              are used. All other information (e.g. keys) is 
     *              ignored.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMClass returned. If false,
     *              all elements of the class definition are 
     *              returned. 
     * @param   includeQualifiers If true, all Qualifiers for the class and its
     *              elements are included in the CIMClass returned.
     *              If false, no Qualifier information is contained 
     *              in the CIMClass returned.
     * @return  The CIM class identified by the CIMObjectPath
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the request CIM Class does not exist in the
     *   specified namespace) 
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     * @see CIMClient#enumerateClasses(CIMObjectPath)
     * @see CIMClient#enumerateClassNames(CIMObjectPath)
     */
    public CIMClass getClass(CIMObjectPath name, boolean localOnly,
            boolean includeQualifiers) throws CIMException
    {
        
        return getClass(name, localOnly, includeQualifiers, false, null);
        
    } // getClass
    
    /**
     * Returns the CIMClass for the specified CIMObjectPath. The entire class
     * contents, not just the class name, is returned. This method behaves as
     * if:
     * <pre>
     *   propertyList=null.
     * </pre>
     *
     * @param   name        The object path of the class to be returned. 
     *              Only the name space and class name components 
     *              are used. All other information (e.g. keys) is 
     *              ignored.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMClass returned. If false,
     *              all elements of the class definition are 
     *              returned. 
     * @param   includeQualifiers If true, all Qualifiers for the class and its
     *              elements are included in the CIMClass returned.
     *              If false, no Qualifier information is contained 
     *              in the CIMClass returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              CIMClass returned. If false, no CLASSORIGIN 
     *              attributes are present in the CIMClass 
     *              returned. CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @return  The CIM class identified by the CIMObjectPath
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the request CIM Class does not exist in the
     *   specified namespace) 
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     * @see CIMClient#enumerateClasses(CIMObjectPath)
     * @see CIMClient#enumerateClassNames(CIMObjectPath)
     */
    public CIMClass getClass(CIMObjectPath name, boolean localOnly,
            boolean includeQualifiers, boolean includeClassOrigin)
            throws CIMException
    {
        
        return getClass(name,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                null);
        
    } // getClass
    
    /**
     * Returns the CIMClass for the specified CIMObjectPath.
     *
     * @param   name        The object path of the class to be returned. 
     *              Only the name space and class name components 
     *              are used. All other information (e.g. keys) is 
     *              ignored.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMClass returned. If false,
     *              all elements of the class definition are 
     *              returned.
     * @param   includeQualifiers If true, all Qualifiers for the class and its
     *              elements are included in the CIMClass returned.
     *              If false, no Qualifier information is contained 
     *              in the CIMClass returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              CIMClass returned. If false, no CLASSORIGIN 
     *              attributes are present in the CIMClass 
     *              returned. CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @param   propertyList    An array of property names used to filter what 
     *              is contained in the CIMClass returned. The 
     *              CIMClass returned <b>only</b> contains elements
     *              for the properties of the names specified. 
     *              Duplicate and invalid property names are ignored
     *              and the request is otherwise processed normally.
     *              An empty array indicates that no properties 
     *              should be returned. A <b>null</b> value 
     *              indicates that all properties should be 
     *              returned.
     * @return  The CIM class identified by the CIMObjectPath
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the request CIM Class does not exist in the
     *   specified namespace) 
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     * @see CIMClient#enumerateClasses(CIMObjectPath)
     * @see CIMClient#enumerateClassNames(CIMObjectPath)
     */
    public synchronized CIMClass getClass(CIMObjectPath name,
            boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin, String propertyList[])
            throws CIMException
    {
        
        return api.getClass(version,
                checkNameSpace(name),
                name,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        
    } // getClass
    
    /**
     * Returns the CIMInstance for the specified CIMObjectPath. This method 
     * behaves as if:
     *
     * <pre>
     *   localOnly=true,
     *   includeQualifiers=false,
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * <p>
     * Qualifiers information is not present in the instance returned.
     * The CLASSORIGIN attribute is also not present on the returned instance.
     *
     * @param   name        The object path of the instance to be returned.
     *              The Keys in this CIMObjectPath must be 
     *              populated.
     * @return  The CIM instance identified by the CIMObjectPath specified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#enumerateInstances(CIMObjectPath)
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public CIMInstance getInstance(CIMObjectPath name) throws CIMException
    {
        
        return getInstance(name, true, false, false, null);
        
    } // getInstance
    
    /**
     * Returns the CIMInstance for the specified CIMObjectPath. This method 
     * behaves as if:
     *
     * <pre>
     *   includeQualifiers=false,
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * <p>
     * Qualifiers information is not present in the instance returned.
     * The CLASSORIGIN attribute is also not present on the returned instance.
     *
     * @param   name        The object path of the instance to be returned.
     *              The Keys in this CIMObjectPath must be 
     *              populated.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMInstance returned. If 
     *              false, all elements of the class definition are 
     *              returned.
     * @return  The CIM instance identified by the CIMObjectPath specified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#enumerateInstances(CIMObjectPath)
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public CIMInstance getInstance(CIMObjectPath name, boolean localOnly)
            throws CIMException
    {
        
        return getInstance(name, localOnly, false, false, null);
        
    } // getInstance
    
    /**
     * Returns the CIMInstance for the specified CIMObjectPath. This method 
     * behaves as if:
     *
     * <pre>
     *   includeClassOrigin=false,
     *   propertyList=null.
     * </pre>
     * <p>
     * The CLASSORIGIN attribute is not present on the returned instance.
     *
     * @param   name        The object path of the instance to be returned.
     *              The Keys in this CIMObjectPath must be 
     *              populated.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMInstance returned. If 
     *              false, all elements of the class definition are 
     *              returned.
     * @param   includeQualifiers If true, all Qualifiers for the instance and 
     *              its elements are included in the CIMInstance
     *              returned. If false, no Qualifier information 
     *              is contained in the CIMInstance returned.
     * @return  The CIM instance identified by the CIMObjectPath specified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#enumerateInstances(CIMObjectPath)
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public CIMInstance getInstance(CIMObjectPath name, boolean localOnly,
            boolean includeQualifiers) throws CIMException
    {
        
        return getInstance(name, localOnly, includeQualifiers, false, null);
        
    } // getInstance
    
    /**
     * Returns the CIMInstance for the specified CIMObjectPath. This method 
     * behaves as if:
     *
     * <pre>
     *   propertyList=null.
     * </pre>
     *
     * @param   name        The object path of the instance to be returned.
     *              The Keys in this CIMObjectPath must be 
     *              populated.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMInstance returned. If 
     *              false, all elements of the class definition are 
     *              returned.
     * @param   includeQualifiers If true, all Qualifiers for the instance and 
     *              its elements are included in the CIMInstance
     *              returned. If false, no Qualifier information 
     *              is contained in the CIMInstance returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              CIMInstance returned. If false, no CLASSORIGIN 
     *              attributes are present in the CIMInstance 
     *              returned. CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @return  The CIM instance identified by the CIMObjectPath specified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#enumerateInstances(CIMObjectPath)
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public CIMInstance getInstance(CIMObjectPath name, boolean localOnly,
            boolean includeQualifiers, boolean includeClassOrigin)
            throws CIMException
    {
        
        return getInstance(name,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                null);
        
    } // getInstance
    
    /**
     * Returns the CIMInstance for the specified CIMObjectPath.
     *
     * @param   name        The object path of the instance to be returned.
     *              The Keys in this CIMObjectPath must be 
     *              populated.
     * @param   localOnly   If true, only elements (properties, methods, 
     *              references) overridden or defined in the class 
     *              are included in the CIMInstance returned. If 
     *              false, all elements of the class definition are 
     *              returned.
     * @param   includeQualifiers If true, all Qualifiers for the instance and 
     *              its elements are included in the CIMInstance
     *              returned. If false, no Qualifier information 
     *              is contained in the CIMInstance returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              CIMInstance returned. If false, no CLASSORIGIN 
     *              attributes are present in the CIMInstance 
     *              returned. CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @param   propertyList    An array of property names used to filter what 
     *              is contained in the CIMClass returned. The 
     *              CIMClass returned <b>only</b> contains elements
     *              for the properties of the names specified. 
     *              Duplicate and invalid property names are ignored
     *              and the request is otherwise processed normally.
     *              An empty array indicates that no properties 
     *              should be returned. A <b>null</b> value 
     *              indicates that all properties should be 
     *              returned.
     * @return  The CIM instance identified by the CIMObjectPath specified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#enumerateInstances(CIMObjectPath)
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public synchronized CIMInstance getInstance(CIMObjectPath name,
            boolean localOnly, boolean includeQualifiers,
            boolean includeClassOrigin, String propertyList[])
            throws CIMException
    {
        
        return api.getInstance(version,
                checkNameSpace(name),
                name,
                localOnly,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
    } // getInstance
    
    /**
     * Executes the specified method on the specified object. 
     *
     * @param   name        CIM object path of the object whose method must
     *              be invoked. It must include all of the keys.
     * @param   methodName  the name of the method to be invoked.
     * @param   inArgs      the CIMArgument array of method input 
     *              parameters.
     * @param   outArgs     the CIMArgument array of method output 
     *              parameters. The array should be allocated 
     *              large enough to hold all returned parameters, 
     *              but should not initialize any elements.
     * @return  The return value of the specified method.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre> 
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (CIM Server <b>DOES NOT</b> support <b>ANY</b>
     *   Extrinsic Method Invocation),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_METHOD_NOT_FOUND,
     *   CIM_ERR_METHOD_NOT_AVAILABLE,
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public CIMValue invokeMethod(CIMObjectPath name, String methodName,
            CIMArgument[] inArgs, CIMArgument[] outArgs) throws CIMException
    {
        
        return api.invokeMethod(version,
                checkNameSpace(name),
                name,
                methodName,
                inArgs,
                outArgs);
        
    } // invokeMethod
    
    /**
     * Gets the CIMQualifierType specified.
     *
     * @param   name    CIMObjectPath that identifies the CIMQualifierType
     *          to return.
     * @return  The CIMQualifierType object
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the requested Qualifier declaration did not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public synchronized CIMQualifierType getQualifierType(CIMObjectPath name)
            throws CIMException
    {
        
        return api.getQualifierType(version, checkNameSpace(name), name);
        
    } // getQualifierType
    
    /**
     * Adds the specified CIMQualifierType to the specified namespace.
     *
     * @param   name    CIMObjectPath that identifies the CIMQualifierType
     *          to create.
     * @param   qt  The CIMQualifierType to be created
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     *   </pre>
     */
    public synchronized void createQualifierType(CIMObjectPath name,
            CIMQualifierType qt) throws CIMException
    {
        
        api.createQualifierType(version, checkNameSpace(name), name, qt);
        
    } // createQualifierType
    
    /**
     * Adds the specified CIMQualifierType to the specified namespace if it 
     * does not already exist. Otherwise, it sets the qualifier type to 
     * the value specified.
     *
     * @param   name    CIM object path that identifies the CIM qualifier type
     * @param   qt  the CIM qualifier type to be added
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public synchronized void setQualifierType(CIMObjectPath name,
            CIMQualifierType qt) throws CIMException
    {
        
        api.setQualifierType(version, checkNameSpace(name), name, qt);
        
    } // setQualifierType
    
    /**
     * Adds the CIM class to the specified namespace.
     *
     * @param   name    CIMObjectPath that identifies the CIMClass to be added
     * @param   cc  CIMClass to be added
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_ALREADY_EXISTS (the CIM Class already exists)
     *   CIM_ERR_INVALID_SUPERCLASS (the putative CIM Class declares a
     *   non-existent superclass),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public synchronized void createClass(CIMObjectPath name, CIMClass cc)
            throws CIMException
    {
        
        api.createClass(version, checkNameSpace(name), name, cc);
        
    } // createClass
    
    /**
     * Modifies the CIMClass in the specified namespace.
     *
     * @param   name    CIMObjectPath that identifies the CIM class to be 
     *          modified 
     * @param   cc  CIMClass to be modified
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_SUPERCLASS (the putative CIM Class declares a
     *   non-existent superclass),
     *   CIM_ERR_CLASS_HAS_CHILDREN (the modification could not be performed
     *   because it was not possible to update the subclasses of the Class
     *   in a consistent fashion),
     *   CIM_ERR_CLASS_HAS_INSTANCES (the modification could not be performed
     *   because it was not possible to update the instances of the Class in
     *   a consistent fashion)  
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public synchronized void setClass(CIMObjectPath name, CIMClass cc)
            throws CIMException
    {
        
        api.setClass(version, checkNameSpace(name), name, cc);
        
    } // setClass
    
    /**
     * Adds the specified CIM Instance to the specified namespace.
     *
     * @param   name    CIM object path that identifies the CIM instance to 
     *          be added. Only the namespace component is used. All 
     *          other information (e.g. keys) is ignored.
     * @param   ci  CIM instance to be created. Its keys and properties may 
     *          be initialized by either the client or server.
     * @return  CIMObjectPath of the instance created.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_ALREADY_EXISTS,
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public synchronized CIMObjectPath createInstance(CIMObjectPath name,
            CIMInstance ci) throws CIMException
    {
        return api.createInstance(version, checkNameSpace(name), name, ci);
    } // createInstance
    
    /**
     * Modifies the specified CIMInstance in the specified namespace.
     *
     * <p>
     * <b>Example:</b>
     * <p>
     * The following code example loops through a container of CIMInstances
     * of class "CIM_Foo", sets property "bar" to a value of 10, and sets the
     * the modified CIMInstance in the CIM Server:
     * <p>
     * <pre>
     * <code>Iterator iter = enumFooInst.iterator();</code>
     * <code>while (iter.hasNext()) {</code>
     * <code>    CIMInstance ci = (CIMInstance)iter.next();</code>
     * <code>    // <b>Note:</b> "bar" cannot be modified if it is a Key</code>
     * <code>    ci.setProperty("bar", new CIMValue(10));</code>
     * <code>    // Perform the operation in the curren namespace</code>
     * <code>    try {
     * <code>        cimomHandle.setInstance(new CIMObjectPath(), ci);</code>
     * <code>    } catch (CIMException cex) {</code>
     * <code>    }</code>
     * <code>}</code>
     * <pre>
     * @param   name    CIMObjectPath that identifies the namespace in which
     *          the specified CIMInstance should be modified. All other
     *          information (e.g. Keys) is ignored.
     * @param   ci  CIMInstance to be modified. All Keys must be populated.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_NO_SUCH_PROPERTY (in this instance),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public synchronized void setInstance(CIMObjectPath name, CIMInstance ci)
            throws CIMException
    {
        
        api.setInstance(version, checkNameSpace(name), name, ci, true, null);
        
    } // setInstance
    
    /**
     * Modifies some or all of the properties of the specified CIMInstance
     * in the specified namespace.
     *
     * @param   name    CIMObjectPath that identifies the namespace in which
     *          the specified CIMInstance should be modified. All other
     *          information (e.g. Keys) is ignored.
     * @param   ci  CIMInstance to be modified. Its properties may be
     *          initialized by either the client or server.
     * @param   includeQualifiers This argument is ignored. Qualifiers cannot be
     *              modified on a per-Instance basis. It exists for
     *              backward compatibility only.
     * @param   propertyList    An array of property names used to specify 
     *              which values from the CIMInstance specified 
     *              to set. Properties not specified in this list 
     *              but set in the CIMInstance specified are
     *              <b>not</b> modified. Duplicate and invalid 
     *              property names are ignored and the request is 
     *              otherwise processed normally. An empty array 
     *              indicates that no properties should be modified.
     *              A <b>null</b> value indicates that all 
     *              properties should be modified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_NO_SUCH_PROPERTY (in this instance),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public void setInstance(CIMObjectPath name, CIMInstance ci,
            boolean includeQualifiers, String[] propertyList)
            throws CIMException
    {
        
        api.setInstance(version,
                checkNameSpace(name),
                name,
                ci,
                includeQualifiers,
                propertyList);
        
    } // setInstance
    
    /**
     * Returns the CIMValue of the specified property from the instance 
     * specified in the object path.
     *
     * @param   name    CIMObjectPath that identifies the instance from which 
     *          to retrieve the property value. All Keys must be
     *          populated.
     * @param   propertyName    The name of the property whose value is to be
     *              returned.
     * @return  The CIMValue of the property specified.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_NO_SUCH_PROPERTY (in this instance),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated
     */
    public synchronized CIMValue getProperty(CIMObjectPath name,
            String propertyName) throws CIMException
    {
        
        return api.getProperty(version,
                checkNameSpace(name),
                name,
                propertyName);
        
    } // getProperty
    
    /**
     * Modifies the value of the specified property in the instance specified.
     *
     * @param   name    CIMObjectPath that identifies the instance whose 
     *          property is to be set. All Keys must be populated.
     * @param   propertyName    Name of the property whose value is to be set.
     * @param   newValue    The value for property propertyName. The value
     *              specified may be <b>null</b>.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class does not exist in the specified 
     *   namespace),
     *   CIM_ERR_NOT_FOUND (the CIM Class exists, but the requested CIMInstance
     *   does not exist in the specified namespace),
     *   CIM_ERR_NO_SUCH_PROPERTY (the CIMInstance exists, but the property 
     *   specified does not),
     *   CIM_ERR_TYPE_MISMATCH (the specified CIMValue is incompatible with the
     *   property type),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated   
     */
    public synchronized void setProperty(CIMObjectPath name,
            String propertyName, CIMValue newValue) throws CIMException
    {
        
        api.setProperty(version,
                checkNameSpace(name),
                name,
                propertyName,
                newValue);
        
    } // setProperty
    
    /**
     * Sets the value of the specified property to <b>null</b> in the instance 
     * specified.
     *
     * @param   name    CIMObjectPath that identifies the instance whose 
     *          property is to be set. All Keys must be populated.
     * @param   propertyName    Name of the property whose value is to be set.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_INVALID_CLASS (the CIM Class does not exist in the specified 
     *   namespace),
     *   CIM_ERR_NOT_FOUND (the CIM Class exists, but the requested CIMInstance
     *   does not exist in the specified namespace),
     *   CIM_ERR_NO_SUCH_PROPERTY (the CIMInstance exists, but the property 
     *   specified does not),
     *   CIM_ERR_TYPE_MISMATCH (the specified CIMValue is incompatible with the
     *   property type),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated   
     */
    public synchronized void setProperty(CIMObjectPath name, String propertyName)
            throws CIMException
    {
        
        setProperty(name, propertyName, null);
        
    } // setProperty
    
    /**
     * Executes a query to retrieve objects.
     *
     * The WBEM Query Language (WQL) is a subset of standard American
     * National Standards Institute Structured Query Language (ANSI SQL)
     * with semantic changes to support WBEM. In this release, unlike SQL, 
     * WQL is a retrieval-only language. WQL cannot be used to modify, insert, 
     * or delete information. For more information on WQL and constructing a 
     * query, see the Developer's Guide.
     * <p>
     * <b>NOTE:</b> The CIMInstances returned by this method contain all 
     * Qualifier information, all local and inherited elements (properties, 
     * methods, references), and CLASSORIGIN information.
     * <p>
     * @param   path    CIMObjectPath identifying the class to query.
     *              Only the namespace and class name components
     *              are used. All other information (e.g. Keys) is
     *              ignored.
     * @param   query   A string containing the text of the query. The value
     *          specified cannot be <b>null</b>.
     * @param   ql  A string that identifies the query language to use to
     *          parse the query string specified. (e.g. "WQL") WQL
     *          Level 1 is currently the only supported query language.
     * @return  An enumeration of all CIMInstances of the specified class and 
     *      instances of all classes derived from the specified class, 
     *      that match the query string.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *    CIM_ERR_ACCESS_DENIED,
     *    CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *    CIM_ERR_INVALID_NAMESPACE,
     *    CIM_ERR_INVALID_PARAMETER (including missing, duplicate, 
     *    unrecognized or otherwise incorrect parameters),
     *    CIM_ERR_QUERY_LANGUAGE_NOT_SUPPORTED (the requested query language is
     *    not recognized),
     *    CIM_ERR_INVALID_QUERY (the query is not a valid query in the specified
     *    query language),
     *    CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public synchronized Enumeration execQuery(CIMObjectPath path, String query,
            String ql) throws CIMException
    {
        
        if (ql == null || ql.length() == 0)
        {
            ql = CIMClient.WQL;
        }
        Vector v = api.execQuery(version, checkNameSpace(path), path, query, ql);
        return v.elements();
        
    } // execQuery
    
    /**
     * Enumerate CIM Objects that are associated to a specified source CIM 
     * Object. If the source object is a CIM Class, then an Enumeration of
     * CIMClass objects is returned containing the classes associated to the
     * source Object. If the source object is a CIM Instance, then an
     * Enumertion of CIMInstance objects is returned containing the instances
     * associated to the source Object. This method behaves as if:
     *
     * <p>
     * <pre>
     *   assocClass=null,
     *   resultClass=null,
     *   role=null,
     *   resultRole=null,
     *   includeQualifiers=true,
     *   includeClassOrigin=true,
     *   propertyList=null.
     * </pre>
     * All elements (properties, methods, references) are contained in the
     * Objects returned. The qualifier and CLASSORIGIN information are also
     * contained in each Object returned.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose associated Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @return  If successful, an Enumeration containing zero or more CIMClass 
     *      or CIMInstance Objects meeting the specified criteria is 
     *      returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration associators(CIMObjectPath objectName)
            throws CIMException
    {
        
        return associators(objectName, null, null, null, null, true, true, null);
        
    } // associators
    
    /**
     * Enumerate CIM Objects that are associated to a specified source CIM 
     * Object. If the source object is a CIM Class, then an Enumeration of
     * CIMClass objects is returned containing the classes associated to the
     * source Object. If the source object is a CIM Instance, then an
     * Enumertion of CIMInstance objects is returned containing the instances
     * associated to the source Object.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose associated Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @param   assocClass  This string <b>MUST</b> either contain a valid 
     *              CIM Association class name or be <b>null</b>.
     *              It filters the Objects returned to contain only
     *              Objects associated to the source Object via 
     *              this CIM Association class or one of its 
     *              subclasses.
     * @param   resultClass This string <b>MUST</b> either contain a valid
     *              CIM Class name or be <b>null</b>. It filters the
     *              Objects returned to contain only the Objects
     *              of this Class name or one of its subclasses.
     * @param   role        This string <b>MUST</b> either contain a valid
     *              Property name or be <b>null</b>. It filters the
     *              Objects returned to contain only Objects
     *              associated to the source Object via an 
     *              Association class in which the <i>source </i>
     *              <i>Object</i> plays the specified role. (i.e. 
     *              the Property name in the Association class that 
     *              refers to the source Object matches this value) 
     *              If "Antecedent" is specified, then only
     *              Associations in which the <i>source Object</i>
     *              is the "Antecedent" reference are examined.
     * @param   resultRole  This string <b>MUST</b> either contain a valid
     *              Property name or be <b>null</b>. It filters the
     *              Objects returned to contain only Objects 
     *              associated to the source Object via an
     *              Association class in which the <i>Object </i>
     *              <i>returned</i> plays the specified role. (i.e.
     *              the Property name in the Association class that
     *              refers to the <i>Object returned</i> matches 
     *              this value) If "Dependent" is specified, then
     *              only Associations in which the <i>Object </i>
     *              <i>returned</i> is the "Dependent" reference
     *              are examined.
     * @param   includeQualifiers If true, all Qualifiers for each Object
     *              (including Qualifiers on the Object and on any 
     *              returned Properties) MUST be included in the 
     *              Objects returned. If false, no Qualifiers are
     *              present in each Object returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              Objects returned. If false, no CLASSORIGIN 
     *              attributes are present in the Objects returned. 
     *              CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @param   propertyList    An array of property names used to filter what 
     *              is contained in the Objects returned. Each 
     *              CIMClass or CIMInstance returned <b>only</b> 
     *              contains elements for the properties of the 
     *              names specified. Duplicate and invalid property 
     *              names are ignored and the request is otherwise 
     *              processed normally. An empty array indicates 
     *              that no properties should be included in the
     *              Objects returned. A <b>null</b> value indicates
     *              that all properties should be contained in the
     *              Objects returned. <b>NOTE:</b> Properties 
     *              should <b>not</b> be specified in this parameter
     *              unless a <b>non-null</b> value is specified in 
     *              the <code>resultClass</code> parameter.
     * @return  If successful, an Enumeration containing zero or more CIMClass 
     *      or CIMInstance Objects meeting the specified criteria is 
     *      returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public synchronized Enumeration associators(CIMObjectPath objectName,
            String assocClass, String resultClass, String role,
            String resultRole, boolean includeQualifiers,
            boolean includeClassOrigin, String propertyList[])
            throws CIMException
    {
        
        Vector v = api.associators(version,
                checkNameSpace(objectName),
                objectName,
                assocClass,
                resultClass,
                role,
                resultRole,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        return v.elements();
        
    } // associators
    
    /**
     * Enumerates the CIMObjectPaths of CIM Objects that are associated to a 
     * particular source CIM Object. If the source Object is a CIM Class, then
     * an Enumeration of CIMObjectPaths of the classes associated to the source
     * Object is returned. If the source Object is a CIM Instance, then an
     * Enumeration of CIMObjectPaths of the CIMInstance objects associated to
     * the source Object is returned. This method behaves as if:
     *
     * <p>
     * <pre>
     *   assocClass=null,
     *   resultClass=null,
     *   role=null,
     *   resultRole=null.
     * </pre>
     * No instance filtering is performed.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose associated Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @return  If successful, an Enumeration containing zero or more 
     *      CIMObjectPath objects of the CIM Classes or CIM Instances 
     *      meeting the specified criteria is returned. If no such Objects
     *      are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration associatorNames(CIMObjectPath objectName)
            throws CIMException
    {
        
        return associatorNames(objectName, null, null, null, null);
        
    } // associatorNames
    
    /**
     * Enumerates the CIMObjectPaths of CIM Objects that are associated to a 
     * particular source CIM Object. If the source Object is a CIM Class, then
     * an Enumeration of CIMObjectPaths of the classes associated to the source
     * Object is returned. If the source Object is a CIM Instance, then an
     * Enumeration of CIMObjectPaths of the CIMInstance objects associated to
     * the source Object is returned.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose associated Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @param   assocClass  This string <b>MUST</b> either contain a valid 
     *              CIM Association class name or be <b>null</b>.
     *              It filters the Objects returned to contain only
     *              Objects associated to the source Object via 
     *              this CIM Association class or one of its 
     *              subclasses.
     * @param   resultClass This string <b>MUST</b> either contain a valid
     *              CIM Class name or be <b>null</b>. It filters the
     *              Objects returned to contain only the Objects
     *              of this Class name or one of its subclasses.
     * @param   role        This string <b>MUST</b> either contain a valid
     *              Property name or be <b>null</b>. It filters the
     *              Objects returned to contain only Objects
     *              associated to the source Object via an 
     *              Association class in which the <i>source </i>
     *              <i>Object</i> plays the specified role. (i.e. 
     *              the Property name in the Association class that 
     *              refers to the source Object matches this value) 
     *              If "Antecedent" is specified, then only
     *              Associations in which the <i>source Object</i>
     *              is the "Antecedent" reference are examined.
     * @param   resultRole  This string <b>MUST</b> either contain a valid
     *              Property name or be <b>null</b>. It filters the
     *              Objects returned to contain only Objects 
     *              associated to the source Object via an
     *              Association class in which the <i>Object </i>
     *              <i>returned</i> plays the specified role. (i.e.
     *              the Property name in the Association class that
     *              refers to the <i>Object returned</i> matches 
     *              this value) If "Dependent" is specified, then
     *              only Associations in which the <i>Object </i>
     *              <i>returned</i> is the "Dependent" reference
     *              are examined.
     * @return  If successful, an Enumeration containing zero or more 
     *      CIMObjectPath objects of the CIM Classes or CIM Instances 
     *      meeting the specified criteria is returned. If no such Objects
     *      are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration associatorNames(CIMObjectPath objectName,
            String assocClass, String resultClass, String role,
            String resultRole) throws CIMException
    {
        
        return (api.associatorNames(version,
                checkNameSpace(objectName),
                objectName,
                assocClass,
                resultClass,
                role,
                resultRole)).elements();
    } // associatorNames
    
    /**
     * Enumerates the Association Objects that refer to a specified source CIM
     * Object. If the source Object is a CIM Class, an Enumeration of
     * CIMClass objects is returned containing the Association classes that
     * refer to the source Object. If the source Object is a CIM Instance, an
     * Enumeration of CIMInstance objects is returned containing the 
     * Association class instances that refer to the source Object.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose referring Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @param   resultClass This string <b>MUST</b> either contain a valid
     *              CIM Class name or be <b>null</b>. It filters the
     *              Objects returned to contain only the Objects
     *              of this Class name or one of its subclasses.
     * @param   role        This string <b>MUST</b> either contain a valid
     *              Property name or be <b>null</b>. It filters the
     *              Objects returned to contain only Objects
     *              referring to the source Object via a Property
     *              with the specified name. If "Antecedent" is 
     *              specified, then only Associations in which the
     *              source Object is the "Antecedent" reference are
     *              returned.
     * @param   includeQualifiers If true, all Qualifiers for each Object
     *              (including Qualifiers on the Object and on any 
     *              returned Properties) MUST be included in the 
     *              Objects returned. If false, no Qualifiers are
     *              present in each Object returned.
     * @param   includeClassOrigin If true, the CLASSORIGIN attribute will be 
     *              present on all appropriate elements in the 
     *              Objects returned. If false, no CLASSORIGIN 
     *              attributes are present in the Objects returned. 
     *              CLASSORIGIN is attached to an element
     *              (properties, methods, references) to indicate 
     *              the class in which it was first defined.
     * @param   propertyList    An array of property names used to filter what 
     *              is contained in the Objects returned. Each 
     *              CIMClass or CIMInstance returned <b>only</b> 
     *              contains elements for the properties of the 
     *              names specified. Duplicate and invalid property 
     *              names are ignored and the request is otherwise 
     *              processed normally. An empty array indicates 
     *              that no properties should be included in the
     *              Objects returned. A <b>null</b> value indicates
     *              that all properties should be contained in the
     *              Objects returned. <b>NOTE:</b> Properties 
     *              should <b>not</b> be specified in this parameter
     *              unless a <b>non-null</b> value is specified in 
     *              the <code>resultClass</code> parameter.
     * @return  If successful, an Enumeration containing zero or more CIMClass 
     *      or CIMInstance Objects meeting the specified criteria is 
     *      returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration references(CIMObjectPath objectName, String resultClass,
            String role, boolean includeQualifiers, boolean includeClassOrigin,
            String propertyList[]) throws CIMException
    {
        
        Vector v = api.references(version,
                checkNameSpace(objectName),
                objectName,
                resultClass,
                role,
                includeQualifiers,
                includeClassOrigin,
                propertyList);
        return v.elements();
        
    } // references
    
    /**
     * Enumerates the Association Objects that refer to a specified source CIM
     * Object. If the source Object is a CIM Class, an Enumeration of
     * CIMClass objects is returned containing the Association classes that
     * refer to the source Object. If the source Object is a CIM Instance, an
     * Enumeration of CIMInstance objects is returned containing the 
     * Association class instances that refer to the source Object. This method
     * behaves as if:
     *
     * <p>
     * <pre>
     *   resultClass=null,
     *   role=null,
     *   includeQualifiers=true,
     *   includeClassOrigin=true,
     *   propertyList=null.
     * </pre>
     * <p>
     *
     * All elements (properties, methods, references) are contained in the
     * Objects returned. The qualifier and CLASSORIGIN information are also
     * contained in each Object returned.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose referring Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @return  If successful, an Enumeration containing zero or more CIMClass 
     *      or CIMInstance Objects meeting the specified criteria is 
     *      returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration references(CIMObjectPath objectName) throws CIMException
    {
        
        return references(objectName, null, null, true, true, null);
        
    } // references
    
    /**
     * Enumerates the CIMObjectPaths of Association Objects that are refer to a 
     * particular source CIM Object. If the source Object is a CIM Class, then
     * an Enumeration of CIMObjectPaths of the Association classes that refer to
     * the source Object is returned. If the source Object is a CIM Instance, 
     * then an Enumeration of CIMObjectPaths of the CIMInstance objects that
     * refer to the source Object is returned.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose referring Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @param   resultClass This string <b>MUST</b> either contain a valid
     *              CIM Class name or be <b>null</b>. It filters the
     *              Objects returned to contain only the Objects
     *              of this Class name or one of its subclasses.
     * @param   role        This string <b>MUST</b> either contain a valid
     *              Property name or be <b>null</b>. It filters the
     *              Objects returned to contain only Objects
     *              referring to the source Object via a Property
     *              with the specified name. If "Antecedent" is 
     *              specified, then only Associations in which the
     *              source Object is the "Antecedent" reference are
     *              returned.
     * @return  If successful, an Enumeration containing zero or more 
     *      CIMObjectPath objects of the CIM Classes or CIM Instances 
     *      meeting the specified criteria is returned. If no such Objects
     *      are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration referenceNames(CIMObjectPath objectName,
            String resultClass, String role) throws CIMException
    {
        
        return (api.referenceNames(version,
                checkNameSpace(objectName),
                objectName,
                resultClass,
                role)).elements();
    } // referenceNames
    
    /**
     * Enumerates the CIMObjectPaths of Association Objects that refer to a 
     * particular source CIM Object. If the source Object is a CIM Class, then
     * an Enumeration of CIMObjectPaths of the Association classes that refer to
     * the source Object is returned. If the source Object is a CIM Instance, 
     * then an Enumeration of CIMObjectPaths of the CIMInstance objects that
     * refer to the source Object is returned. This method behaves as if:
     *
     * <p>
     * <pre>
     *   resultClass=null,
     *   role=null.
     * </pre>
     * <p>
     * No instance filtering is performed.
     *
     * @param   objectName  CIMObjectPath defining the source CIM Object 
     *              whose referring Objects are to be returned. 
     *              This argument may contain either a Class name 
     *              or the modelpath of an Instance. (i.e. Keys 
     *              populated)
     * @return  If successful, an Enumeration containing zero or more 
     *      CIMObjectPath objects of the CIM Classes or CIM Instances 
     *      meeting the specified criteria is returned. If no such Objects
     *      are found, <b>null</b> is returned.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration referenceNames(CIMObjectPath objectName)
            throws CIMException
    {
        
        return referenceNames(objectName, null, null);
        
    } // referenceNames
    
    /**
     * Adds the specified CIMListener to receive Indications from the CIM 
     * Server. In order to force the client to stop listening for Indications, 
     * <code>close()</code> must be called. If <code>close()</code> is not
     * called, listening for Indications may still continue. The calling
     * client program will not be able to exit without calling
     * <code>System.exit()</code>. Any duplicate listeners are ignored.
     * 
     * @param   l   The CIMListener to add to receive Indications from the
     *          CIM Server.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#close()
     */
    public void addCIMListener(CIMListener l) throws CIMException
    {
        addCIMListener(l, 0);
    }
    
    /**
     * Adds the specified CIMListener to receive Indications from the CIM 
     * Server. In order to force the client to stop listening for Indications, 
     * <code>close()</code> must be called. If <code>close()</code> is not
     * called, listening for Indications may still continue. The calling
     * client program will not be able to exit without calling
     * <code>System.exit()</code>. Any duplicate listeners are ignored.
     * 
     * @param   l   The CIMListener to add to receive Indications from the
     *          CIM Server.
     * @param   p   The port to receive Indications on
     *          
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @see CIMClient#close()
     */
    public void addCIMListener(CIMListener l, int p) throws CIMException
    {
        /*******fengwenliang �޸��ϱ�����˿�**start****/
        // delegate to the multiplexer
        if (protocol != null)
        {
            synchronized (this)
            {
                // check if somebody beat us to listener set                 
                api.setListener(version, p);
            }
            // if trying to set the port and it is already set, throw exception
        }
        /*******fengwenliang �޸��ϱ�����˿�**end****/
        clientListener.addCIMListener(l);
        
    } // addCIMIListener
    
    /**
     * Removes the specified CIMListener that is receiving Indications from 
     * the CIM Server. No exception is thrown if the specified CIMListener is
     * null or if it is not receiving Indications from the CIM Server.
     * 
     * @param   l   The CIMListener to remove from the CIM Server.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public void removeCIMListener(CIMListener l) throws CIMException
    {
        
        // delegate to the multiplexer
        clientListener.removeCIMListener(l);
        
    } // removeCIMListener
    
    /**
     * 
     * Close the timeout connection
     */
    public void closeTimeOutOperation()
    {
        api.forceShutdown(true);
    }
    
    /**
     * This method returns an Instance of the subclass of
     * <code>CIM_IndicationHandler</code> defined for the client's protocol. 
     * Thus, for HTTP, an Instance of <code>CIM_IndicationHandlerCIMXML</code> 
     * is returned. For RMI, an Instance of the subclass of
     * <code>CIM_IndicationHandler</code> defined for RMI is returned. The 
     * returned Instance of the subclass of <code>CIM_IndicationHandler</code>
     * should then be passed to <code>createInstance()</code> to establish a
     * handler in the WBEM Server. Using this method, a client can 
     * create a protocol independent implementation for creating a 
     * <code>CIM_IndicationHandler</code> subclass Instance.
     *
     * @param   cl  CIMListener for which a 
     *          <code>CIM_IndicationHandler</code> subclass is being
     *          returned. If <b>null</b>, the Instance of the 
     *          <code>CIM_IndicationHandler</code> subclass returned
     *          can be used to deliver Indications to all listeners that
     *          have been added by the <code>addCIMListener()</code> 
     *          call. If <b>non-null</b>, the returned handler can be 
     *          used to deliver Indications to a specific listener. 
     *          <b>NOTE:</b> Unique listeners should return unique 
     *          values in their hashCode methods in order to be 
     *          differentiated. Also note that some implementations 
     *          may have partial support. For example, they may set 
     *          <code>cl</code> to null or non-null.
     * @return  Instance of the <code>CIM_IndicationHandler</code> subclass 
     *      defined for the client's protocol.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated use getIndicationListener(CIMListener) instead
     */
    public CIMInstance getIndicationHandler(CIMListener cl) throws CIMException
    {
        
        return api.getIndicationHandler(cl);
        
    } // getIndicationHandler
    
    /**
     * This method returns an Instance of the subclass of
     * <code>CIM_ListenerDestination</code> defined for the client's protocol. 
     * Thus, for HTTP, an Instance of <code>CIM_ListenerDestinationCIMXML</code> 
     * is returned. For RMI, an Instance of the subclass of
     * <code>CIM_ListenerDestination</code> defined for RMI is returned. The 
     * returned Instance of the subclass of <code>CIM_ListenerDestination</code>
     * should then be passed to <code>createInstance()</code> to establish a
     * handler in the WBEM Server. Using this method, a client can 
     * create a protocol independent implementation for creating a 
     * <code>CIM_ListenerDestination</code> subclass Instance.
     *
     * @param   cl  CIMListener for which a 
     *          <code>CIM_ListenerDestination</code> subclass is being
     *          returned. If <b>null</b>, the Instance of the 
     *          <code>CIM_ListenerDestination</code> subclass returned
     *          can be used to deliver Indications to all listeners that
     *          have been added by the <code>addCIMListener()</code> 
     *          call. If <b>non-null</b>, the returned handler can be 
     *          used to deliver Indications to a specific listener. 
     *          <b>NOTE:</b> Unique listeners should return unique 
     *          values in their hashCode methods in order to be 
     *          differentiated. Also note that some implementations 
     *          may have partial support. For example, they may set 
     *          <code>cl</code> to null or non-null.
     * @return  Instance of the <code>CIM_ListenerDestination</code> subclass 
     *      defined for the client's protocol.
     * @exception CIMException  If unsuccessful, one of the following status 
     *              codes <b>must</b> be returned. The ORDERED list
     *              is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public CIMInstance getIndicationListener(CIMListener cl)
            throws CIMException
    {
        
        return api.getIndicationListener(cl);
    } // getIndicationListener
    
    protected void finalize()
    {
    } // finalize
    
    // Initialize debug tracing if enabled through system properties
    private void initTrace()
    {
        
        // Get debug level and device; pass to trace open.
        // We set the trace file base name for the server side.
        String level = System.getProperty("wbem.debug.level");
        String device = System.getProperty("wbem.debug.device");
        if ((device != null) && (device.equalsIgnoreCase("file")))
        {
            device = "wbem_client";
        }
        Debug.traceOpen(level, device);
        Debug.trace1("Starting CIMClient version " + Version.major + "."
                + Version.minor + "." + Version.patch + " " + Version.buildID);
        
    } // initTrace
    
    private String getProtocol()
    {
        return CIMClient.protocol;
    } // getProtocol
    
    private void setProtocol(String protocol)
    {
        CIMClient.protocol = protocol;
    } // setProtocol
    
    /**
     * Executes the batch operations contained in the specified BatchHandle 
     * object. A BatchResult object is returned that contains the return value
     * or CIMException result from the execution of each operation. The
     * operations are performed in the order in which they were specified in
     * the BatchHandle argument.
     *
     * <p>
     * <b>Example:</b>
     * <p>
     * The following code example demonstrates how to enumerate instances of 
     * Classes CIM_Foo and CIM_Bar as batch operations.
     * <p>
     * <pre>
     * <code>BatchCIMClient batchClient = new BatchCIMClient();</code>
     * <code>CIMObjectPath fooCOP = new CIMObjectPath("CIM_Foo"); </code>
     * <code>CIMObjectPath barCOP = new CIMObjectPath("CIM_Bar"); </code>
     * <code>int enumFooID = batchClient.enumerateInstances(fooCOP,</code>
     * <code>        false, true, false, false, null); </code>
     * <code>int enumBarID = batchClient.enumerateInstances(barCOP,</code>
     * <code>        false, true, false, false, null);</code>
     * <code>// perform the Batch operations on an existing CIMOMHandle</code>
     * <code>BatchResult batchRes = cimomHandle.performBatchOperations(</code>
     * <code>        batchClient);</code>
     * </pre>
     * @param   bc  The BatchHandle that contains the list of operations
     *          to be performed.
     * @return  BatchResult containing the return value or CIMException result
     *      from the execution of each operation.
     * @exception CIMException  If batch mode is turned off or the list of
     *              CIM operations is null.
     */
    public BatchResult performBatchOperations(BatchHandle bc)
            throws CIMException
    {
        
        CIMOperation[] oplist = null;
        
        try
        {
            oplist = ((BatchCIMClient) bc).getOperationList();
        }
        catch (Exception ex)
        {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER, ex);
        }
        
        if (oplist != null && oplist.length > 0)
        {
            for (int i = 0; i < oplist.length; i++)
            {
                oplist[i].setNameSpace(checkNameSpace(oplist[i].getTargetNS()));
                //      oplist[i].setNameSpace(this.getNameSpace());
            }
            Vector v = api.performOperations(version, oplist);
            
            // if the sizes of the input and output vectors don't match,
            // we have a problem...
            if ((v == null) || (oplist.length != v.size()))
            {
                // need to throw a more descriptive error here.
                throw new CIMException(CIMException.CIM_ERR_FAILED);
            }
            
            Object arr[] = new Object[v.size()];
            
            // The returned vector contains the result objects
            // In some cases, result the result objects are themselves
            // vectors, which in *some* cases need to be returned as 
            // enumerations. This post-processing knowledge is built
            // into each of the CIMOperation objects, so feed the result
            // back into the corresponding operation objects
            // and get the post-processed result back from the operation
            // into the return array.
            for (int i = 0; i < v.size(); i++)
            {
                oplist[i].setResult(v.elementAt(i));
                arr[i] = oplist[i].getResult();
            }
            BatchResult br = new BatchResult(arr);
            return br;
        }
        else
        {
            throw new CIMException(CIMException.CIM_ERR_INVALID_PARAMETER);
        }
    } // performBatchOperations
    
    /**
     * Returns the CIMNameSpace object.
     * 
     * @return CIMNamespace object.
     */
    protected CIMNameSpace getNameSpace()
    {
        return this.nameSpace;
    }
    
    /**
     * If a path's namespace begins with "/" (absolute path), then return an
     * empty namespace. Otherwise, return the client's namespace.
     * 
     * @param path the path to check
     * @return CIMNamespace object.
     */
    private CIMNameSpace checkNameSpace(CIMObjectPath path)
    {
        
        if (path != null && path.getNameSpace() != null
                && path.getNameSpace().startsWith("/"))
        {
            return emptyNameSpace;
        }
        return nameSpace;
    }
    
    /**
     * If a path's namespace begins with "/" (absolute path), then return an
     * empty namespace. Otherwise, return the client's namespace.
     * 
     * @param inNs the namespace to check
     * @return CIMNamespace object.
     */
    private CIMNameSpace checkNameSpace(CIMNameSpace inNs)
    {
        
        if (inNs != null && inNs.getNameSpace() != null
                && inNs.getNameSpace().startsWith("/"))
        {
            return emptyNameSpace;
        }
        return nameSpace;
    }
}
