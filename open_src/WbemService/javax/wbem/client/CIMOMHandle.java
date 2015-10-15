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
 *Contributor(s): WBEM Solutions, Inc.
 */

package javax.wbem.client;

import java.util.Enumeration;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;

/**
 * Interface for any CIMOM Handle. The implementation must take care
 * of communicating with the CIMOM. 
 *
 * @author	Sun Microsystems, Inc.
 * @since   	WBEM 1.0
 */
public interface CIMOMHandle {

    /**
     * Creates a CIM namespace, a directory containing CIM classes and 
     * CIM instances.
     *
     * @param	ns	The CIMNameSpace object that specifies a string
     *			for the host and a string for the namespace
     * @exception CIMException	Throws a CIM exception if the namespace 
     *				already exists
     */
    public void createNameSpace(CIMNameSpace ns) throws CIMException;

    /**
     * Closes the connection to the CIM Object Manager, freeing any resources
     * used for the client session.
     *
     * @exception CIMException	Throws a CIM Exception if the client session 
     * 				does not exist 
     */
    public void close() throws CIMException;

    /**
     * Deletes the specified namespace.
     *
     * @param	ns	The CIMNameSpace object that identifies the namespace
     *			to be deleted
     * @exception CIMException	If unsuccessful, one of the following status 
     * 				codes <b>must</b> be returned. The ORDERED list
     * 				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (if namespace does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     * @deprecated	The Java WBEM Services deleteNameSpace API is a 
     *			convenience method. This API uses deleteInstance on 
     *			__Namespace class. The CIM Operations spec has 
     *			deprecated the __Namespace for CIM_Namespace. This is a
     *			better design, but it is more complex and doesn't lend 
     *			itself to the this simple convenience method. There is 
     *			no loss in functionality. Developers must now use 
     *			deleteInstance on the __Namespace or CIM_Namespace class
     *			to perform this task.
     */
    public void deleteNameSpace(CIMNameSpace ns) throws CIMException;

    /**
     * Deletes the CIM class for the object specified by the CIM object path.
     *
     * @param	path	The CIMObjectPath identifying the class to delete
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public void deleteClass(CIMObjectPath path) throws CIMException;

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
     * @param	path	The object path of the instance to be deleted. It must 
     * 			include all of the keys.
     * @exception CIMException  If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public void deleteInstance(CIMObjectPath path) throws CIMException;

    /**
     * Deletes the CIM qualfier type in the namespace specified by the 
     * CIM object path.
     *
     * @param path	the CIMObjectPath identifying the CIM qualifier
     *			to delete
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_NOT_FOUND (the Qualifier did not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre> 
     */
    public void deleteQualifierType(CIMObjectPath path) throws CIMException;

    /**
     * Enumerates the namespaces within the namespace specified by the CIM 
     * object path. 
     *
     * @param	path	The CIMObjectPath identifying the namespace to be 
     * 			enumerated.
     * @param	deep	If set to true, the enumeration returned will contain 
     * 			the entire hierarchy of namespaces present under the 
     *			enumerated namespace. If set to false the enumeration 
     *			will return only the first level children of the
     *			enumerated namespace.
     * @return	Enumeration of namespace names as CIMObjectPath. If none are 
     * 		found, <b>null</b> is returned.
     * @exception  CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     * @deprecated	The Java WBEM Services <code>enumNameSpace</code> API 
     * 			is a convenience method. This API uses 
     * 			<code>enumerateInstances</code> on __Namespace class. 
     * 			The CIM Operations spec has deprecated the __Namespace 
     * 			for CIM_Namespace. This is a better design, but it is 
     * 			more complex and doesn't lend itself to the this simple
     * 			convenience method. There is no loss in functionality. 
     * 			Developers must now use <code>enumerateInstance</code> 
     * 			on the __Namespace or CIM_Namespace class to perform 
     * 			this task.
     */
    public Enumeration enumNameSpace(CIMObjectPath path, boolean deep) 
	    throws CIMException;

    /**
     * Enumerates the class specified in the path. The class names are returned
     * as an enumeration of CIMObjectPath.
     *
     * @param	path	The CIMObjectPath identifying the class to be 
     * 			enumerated. If the class name in the object path 
     * 			specified is <b>null</b>, all base classes in the 
     * 			target namespace are returned.
     * @param	deep	If true, the enumeration returned will contain the 
     * 			names of all classes derived from the class being
     * 			enumerated. If false, the enumeration returned 
     * 			contains only the names of the first level children
     * 			of the class.
     * @return	Enumeration of class names as CIMObjectPaths.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public Enumeration enumerateClassNames(CIMObjectPath path,
	    boolean deep) throws CIMException;

    /**
     * Enumerates the class specified in the path. The entire class contents,
     * not just the class names, are returned.
     *
     * @param	path		The object path of the class to be enumerated. 
     * 				Only the name space and class name components 
     * 				are used. All other information (e.g. Keys) is 
     * 				ignored.
     * @param	deep		If true, the enumeration returned contains the 
     * 				specified class and all subclasses. If false, 
     * 				the enumeration returned contains only the 
     * 				contents of the first level children of the 
     * 				specified class.
     * @param	localOnly	If true, only elements (properties, methods and
     *				qualifiers) defined in, or overridden in the 
     *				class are included in the response. If false, 
     *				all elements of the class definition are 
     *				returned. 
     * @param	includeQualifiers If true, all Qualifiers for each Class and its
     *				elements (properties, methods, references). If
     *				false, no Qualifiers are present in the classes
     *				returned 
     * @param	includeClassOrigin If true, the CLASSORIGIN attribute MUST be 
     *				present on all appropriate elements in each 
     *				classes returned. If false, no CLASSORIGIN 
     *				attributes are present in each class returned.
     *				CLASSORIGIN is attached to an element to
     *				indicate the class in which it was first 
     *				defined.
     * @return	Enumeration of CIMClass. If none are found, <b>null</b> is 
     * 		returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public Enumeration enumerateClasses(CIMObjectPath path, 
	    boolean deep, 
	    boolean localOnly, 
	    boolean includeQualifiers, 
	    boolean includeClassOrigin) throws CIMException;

    /**
     * Returns an enumeration of all instances of the class specified by the 
     * CIMObjectPath argument. The entire instances, not just the object paths
     * to the instances, are returned. The enumeration returned could include
     * instances of all the classes in the specified class's hierarchy.
     *
     * @param	path	The object path of the class to be enumerated. Only the
     * 			name space and class name components are used. Any 
     * 			other information (e.g. Keys) is ignored.
     * @param	deep	If true, the enumeration returned contains all 
     * 			instances of the specified class and all classes 
     * 			derived from it. If false, only names of instances 
     * 			of the specified class are returned.
     * @param	localOnly	If true, only elements (properties, methods, 
     * 				references) overridden or defined in the class 
     * 				are included in the instances returned.
     * @param	includeQualifiers If true, all Qualifiers for each instance 
     * 				are included in the instances returned. If 
     * 				false, no Qualifier information is contained 
     * 				in the Instances returned.
     * @param	includeClassOrigin If true, the CLASSORIGIN attribute will be 
     * 				present on all appropriate elements in the 
     * 				instances returned. If false, no CLASSORIGIN 
     * 				attributes are present in the instances 
     * 				returned. CLASSORIGIN is attached to an element
     * 				(properties, methods, references) to indicate 
     * 				the class in which it was first defined.
     * @param	propertyList	An array of property names used to filter what 
     * 				is contained in the instances returned. Each 
     * 				instance returned <b>only</b> contains elements
     * 				for the properties of the names specified. 
     * 				Duplicate and invalid property names are ignored
     * 				and the request is otherwise processed normally.
     * 				An empty array indicates that no properties 
     * 				should be returned. A <b>null</b> value 
     * 				indicates that all properties should be 
     * 				returned.
     * @return	Enumeration of CIMInstance. Each instance is filtered as 
     * 		indicated by the parameters specified. If no CIMInstances of
     * 		the specified class are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * 	</pre>
     */
    public Enumeration enumerateInstances(CIMObjectPath path, 
	    boolean deep, 
	    boolean localOnly, 
	    boolean includeQualifiers, 
	    boolean includeClassOrigin, 
	    String propertyList[]) throws CIMException;

    /**
     * Returns the object paths of all instances of the class specified. The 
     * object paths of all derived instances of the specified classes are also
     * returned.
     * 
     * @param	path	The CIMObjectPath identifying the class whose instances 
     * 			are to be enumerated. Only the name space and class 
     * 			name components are used. All other information (e.g. 
     * 			Keys) is ignored.
     * @return	Enumeration of instance names as CIMObjectPaths. If no instances
     * 		are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration enumerateInstanceNames(CIMObjectPath path)
	    throws CIMException;

    /**
     * Enumerates the qualifiers defined in the specified namespace.
     *
     * @param	path	The CIMObjectPath identifying the namespace whose
     *			qualifier definitions are to be enumerated.
     * @return	Enumeration of CIMQualifierType objects. If no qualifiers are
     * 		found, <b>null</b> is returned.
     * @exception CIMException If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration enumQualifierTypes(CIMObjectPath path)
	    throws CIMException;

    /**
     * Executes the specified method on the specified object. 
     *
     * @param	name		CIM object path of the object whose method must
     *				be invoked. It must include all of the keys.
     * @param	methodName	the name of the method to be invoked.
     * @param	inArgs		the CIMArgument array of method input 
     * 				parameters.
     * @param	outArgs		the CIMArgument array of method output 
     * 				parameters. The array should be allocated 
     * 				large enough to hold all returned parameters, 
     * 				but should not initialize any elements.
     * @return	The return value of the specified method.
     * @exception CIMException  If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public CIMValue invokeMethod(CIMObjectPath name,
	    String methodName,
	    CIMArgument[] inArgs,
	    CIMArgument[] outArgs) throws CIMException;

    /**
     * Gets the CIMQualifierType specified.
     *
     * @param	name	CIMObjectPath that identifies the CIMQualifierType
     * 			to return.
     * @return	The CIMQualifierType object
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public CIMQualifierType getQualifierType(CIMObjectPath name) 
	    throws CIMException;
    
    /**
     * Adds the specified CIMQualifierType to the specified namespace if it 
     * does not already exist. Otherwise, it sets the qualifier type to 
     * the value specified.
     *
     * @param	name	CIM object path that identifies the CIM qualifier type
     * @param	qt	the CIM qualifier type to be added
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public void setQualifierType(CIMObjectPath name, 
	    CIMQualifierType qt) throws CIMException;

    /**
     * Adds the specified CIMQualifierType to the specified namespace.
     *
     * @param	name	CIMObjectPath that identifies the CIMQualifierType
     * 			to create.
     * @param	qt	The CIMQualifierType to be created
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public void createQualifierType(CIMObjectPath name,
	    CIMQualifierType qt) throws CIMException;

    /**
     * Returns the CIMClass for the specified CIMObjectPath.
     *
     * @param	name		The object path of the class to be returned. 
     * 				Only the name space and class name components 
     * 				are used. All other information (e.g. keys) is 
     * 				ignored.
     * @param	localOnly	If true, only elements (properties, methods, 
     * 				references) overridden or defined in the class 
     * 				are included in the CIMClass returned. If false,
     * 				all elements of the class definition are 
     * 				returned. 
     * @param	includeQualifiers If true, all Qualifiers for the class and its
     * 				elements are included in the CIMClass returned.
     * 				If false, no Qualifier information is contained 
     * 				in the CIMClass returned.
     * @param	includeClassOrigin If true, the CLASSORIGIN attribute will be 
     * 				present on all appropriate elements in the 
     * 				CIMClass returned. If false, no CLASSORIGIN 
     * 				attributes are present in the CIMClass 
     * 				returned. CLASSORIGIN is attached to an element
     * 				(properties, methods, references) to indicate 
     * 				the class in which it was first defined.
     * @param	propertyList	An array of property names used to filter what 
     * 				is contained in the CIMClass returned. The 
     * 				CIMClass returned <b>only</b> contains elements
     * 				for the properties of the names specified. 
     * 				Duplicate and invalid property names are ignored
     * 				and the request is otherwise processed normally.
     * 				An empty array indicates that no properties 
     * 				should be returned. A <b>null</b> value 
     * 				indicates that all properties should be 
     * 				returned.
     * @return	The CIM class identified by the CIMObjectPath
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_NOT_FOUND (the request CIM Class does not exist in the
     *   specified namespace) 
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>                     
     */
    public CIMClass getClass(CIMObjectPath name, 
	    boolean localOnly,
	    boolean includeQualifiers,
	    boolean includeClassOrigin,
	    String propertyList[]) throws CIMException;

    /**
     * Returns the CIMInstance for the specified CIMObjectPath.
     *
     * @param	name		The object path of the instance to be returned.
     * 				The Keys in this CIMObjectPath must be 
     * 				populated.
     * @param	localOnly	If true, only elements (properties, methods, 
     * 				references) overridden or defined in the class 
     * 				are included in the CIMInstance returned. If 
     * 				false, all elements of the class definition are 
     * 				returned.
     * @param	includeQualifiers If true, all Qualifiers for the instance and 
     * 				its elements are included in the CIMInstance
     * 				returned. If false, no Qualifier information 
     * 				is contained in the CIMInstance returned.
     * @param	includeClassOrigin If true, the CLASSORIGIN attribute will be 
     * 				present on all appropriate elements in the 
     * 				CIMInstance returned. If false, no CLASSORIGIN 
     * 				attributes are present in the CIMInstance 
     * 				returned. CLASSORIGIN is attached to an element
     * 				(properties, methods, references) to indicate 
     * 				the class in which it was first defined.
     * @param	propertyList	An array of property names used to filter what 
     * 				is contained in the CIMClass returned. The 
     * 				CIMClass returned <b>only</b> contains elements
     * 				for the properties of the names specified. 
     * 				Duplicate and invalid property names are ignored
     * 				and the request is otherwise processed normally.
     * 				An empty array indicates that no properties 
     * 				should be returned. A <b>null</b> value 
     * 				indicates that all properties should be 
     * 				returned.
     * @return	The CIM instance identified by the CIMObjectPath specified.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (provider does not support this method),
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public CIMInstance getInstance(CIMObjectPath name,
	    boolean localOnly,
	    boolean includeQualifiers,
	    boolean includeClassOrigin,
	    String propertyList[]) throws CIMException;

    /**
     * Modifies the CIMClass in the specified namespace.
     *
     * @param	name	CIMObjectPath that identifies the CIM class to be 
     * 			modified 
     * @param	cc	CIMClass to be modified
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public void setClass(CIMObjectPath name, CIMClass cc)
	throws CIMException;

    /**
     * Adds the CIM class to the specified namespace.
     *
     * @param	name	CIMObjectPath that identifies the CIMClass to be added
     * @param	cc	CIMClass to be added
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public void createClass(CIMObjectPath name,
	    CIMClass cc) throws CIMException;

    /**
     * Modifies the specified CIMInstance in the specified namespace.
     *
     * <p>
     * <b>Example:</b>
     * <p>
     * The following code example loops through a container of CIMInstances
     * of class "CIM_Foo", sets property "bar" to a value of 10, and sets the
     * modified CIMInstance in the CIM Server:
     * <p>
     * <pre>
     * <code>Iterator iter = enumFooInst.iterator();</code>
     * <code>while (iter.hasNext()) {</code>
     * <code>    CIMInstance ci = (CIMInstance)iter.next();</code>
     * <code>    // <b>NOTE:</b> "bar" cannot be modified if it is a Key</code>
     * <code>    ci.setProperty("bar", new CIMValue(10));</code>
     * <code>    // Perform the operation in the current namespace</code>
     * <code>    try {
     * <code>        cimomHandle.setInstance(new CIMObjectPath(), ci);</code>
     * <code>    } catch (CIMException cex) {</code>
     * <code>    }</code>
     * <code>}</code>
     * <pre>
     * @param	name   	CIMObjectPath that identifies the namespace in which
     * 			the specified CIMInstance should be modified. All other
     * 			information (e.g. Keys) is ignored.
     * @param	ci	CIMInstance to be modified. All Keys must be populated.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public void setInstance(CIMObjectPath name,
	    CIMInstance ci) throws CIMException;

    /**
     * Modifies some or all of the properties of the specified CIMInstance
     * in the specified namespace.
     *
     * @param	name   	CIMObjectPath that identifies the namespace in which
     * 			the specified CIMInstance should be modified. All other
     * 			information (e.g. Keys) is ignored.
     * @param	ci	CIMInstance to be modified. All Keys must be populated.
     * @param	includeQualifiers This argument is ignored. Qualifiers cannot be
     *				modified on a per-Instance basis. It exists for
     *				backward compatibility only.
     * @param	propertyList	An array of property names used to specify 
     * 				which values from the CIMInstance specified 
     * 				to set. Properties not specified in this list 
     * 				but set in the CIMInstance specified are
     * 				<b>not</b> modified. Duplicate and invalid 
     * 				property names are ignored and the request is 
     * 				otherwise processed normally. An empty array 
     * 				indicates that no properties should be modified.
     * 				A <b>null</b> value indicates that all 
     * 				properties should be modified.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public void setInstance(CIMObjectPath name,
	    CIMInstance ci,
	    boolean includeQualifiers,
	    String[] propertyList) throws CIMException;

    /**
     * Adds the specified CIM Instance to the specified namespace.
     *
     * @param	name	CIM object path that identifies the CIM instance to 
     *			be added. Only the namespace component is used. All 
     *			other information (e.g. keys) is ignored.
     * @param	ci	CIM instance to be created. Its keys and properties may 
     * 			be initialized by either the client or server.
     * @return	CIMObjectPath of the instance created.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public CIMObjectPath createInstance(CIMObjectPath name,
	    CIMInstance ci) throws CIMException;
   
    /**
     * Returns the CIMValue of the specified property from the instance 
     * specified in the object path.
     *
     * @param	name	CIMObjectPath that identifies the instance from which 
     * 			to retrieve the property value. All Keys must be
     * 			populated.
     * @param	propertyName	The name of the property whose value is to be
     * 				returned.
     * @return	The CIMValue of the property specified.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_INVALID_NAMESPACE,
     *   CIM_ERR_INVALID_PARAMETER (for this method),
     *   CIM_ERR_INVALID_CLASS (in this namespace),
     *   CIM_ERR_NOT_FOUND (if instance does not exist),
     *   CIM_ERR_NO_SUCH_PROPERTY (in this instance),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>   
     */
    public CIMValue getProperty(CIMObjectPath name, String propertyName)
	throws CIMException;
     
    /**
     * Modifies the value of the specified property in the instance specified.
     *
     * @param	name	CIMObjectPath that identifies the instance whose 
     * 			property is to be set. All Keys must be populated.
     * @param	propertyName	Name of the property whose value is to be set.
     * @param	newValue	The value for property propertyName. The value
     * 				specified may be <b>null</b>.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
     */
    public void setProperty(CIMObjectPath name,
	    String propertyName,
	    CIMValue newValue) throws CIMException;

    /**
     * Sets the value of the specified property to <b>null</b> in the instance 
     * specified.
     *
     * @param	name	CIMObjectPath that identifies the instance whose 
     * 			property is to be set. All Keys must be populated.
     * @param	propertyName	Name of the property whose value is to be set.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
     */
    public void setProperty(CIMObjectPath name,
	    String propertyName) throws CIMException;

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
     * @param	path	CIMObjectPath identifying the class to query.
     * 				Only the namespace and class name components
     * 				are used. All other information (e.g. Keys) is
     * 				ignored.
     * @param	query	A string containing the text of the query. The value
     *			specified cannot be <b>null</b>.
     * @param	queryLanguage	A string that identifies the query language to use to
     *			parse the query string specified. (e.g. "WQL") WQL
     *			Level 1 is currently the only supported query language.
     * @return	An enumeration of all CIMInstances of the specified class and 
     * 		instances of all classes derived from the specified class, 
     * 		that match the query string.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
    public Enumeration execQuery(CIMObjectPath path,
	    String query,
	    String queryLanguage) throws CIMException;

    /**
     * Enumerates CIM Objects that are associated to a specified source CIM 
     * Object. If the source Object is a CIM Class, then an Enumeration of
     * CIMClass objects is returned containing the classes associated to the
     * source Object. If the source Object is a CIM Instance, then an
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
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose associated Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @return	If successful, an Enumeration containing zero or more CIMClass 
     * 		or CIMInstance Objects meeting the specified criteria is 
     * 		returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
	    throws CIMException;

    /**
     * Enumerates CIM Objects that are associated to a specified source CIM 
     * Object. If the source Object is a CIM Class, then an Enumeration of
     * CIMClass objects is returned containing the classes associated to the
     * source Object. If the source Object is a CIM Instance, then an
     * Enumertion of CIMInstance objects is returned containing the instances
     * associated to the source Object.
     *
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose associated Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @param	assocClass	This string <b>MUST</b> either contain a valid 
     *				CIM Association class name or be <b>null</b>.
     *				It filters the Objects returned to contain only
     *				Objects associated to the source Object via 
     *				this CIM Association class or one of its 
     *				subclasses.
     * @param	resultClass	This string <b>MUST</b> either contain a valid
     *				CIM Class name or be <b>null</b>. It filters the
     *				Objects returned to contain only the Objects
     *				of this Class name or one of its subclasses.
     * @param	role		This string <b>MUST</b> either contain a valid
     *				Property name or be <b>null</b>. It filters the
     *				Objects returned to contain only Objects
     *				associated to the source Object via an 
     *				Association class in which the <i>source </i>
     *				<i>Object</i> plays the specified role. (i.e. 
     *				the Property name in the Association class that 
     *				refers to the source Object matches this value) 
     *				If "Antecedent" is specified, then only
     *				Associations in which the <i>source Object</i>
     *				is the "Antecedent" reference are examined.
     * @param	resultRole	This string <b>MUST</b> either contain a valid
     *				Property name or be <b>null</b>. It filters the
     *				Objects returned to contain only Objects 
     *				associated to the source Object via an
     *				Association class in which the <i>Object </i>
     *				<i>returned</i> plays the specified role. (i.e.
     *				the Property name in the Association class that
     *				refers to the <i>Object returned</i> matches 
     *				this value) If "Dependent" is specified, then
     *				only Associations in which the <i>Object </i>
     *				<i>returned</i> is the "Dependent" reference
     *				are examined.
     * @param	includeQualifiers If true, all Qualifiers for each Object
     *				(including Qualifiers on the Object and on any 
     *				returned Properties) MUST be included in the 
     *				Objects returned. If false, no Qualifiers are
     *				present in each Object returned.
     * @param	includeClassOrigin If true, the CLASSORIGIN attribute will be 
     * 				present on all appropriate elements in the 
     * 				Objects returned. If false, no CLASSORIGIN 
     * 				attributes are present in the Objects returned. 
     * 				CLASSORIGIN is attached to an element
     * 				(properties, methods, references) to indicate 
     * 				the class in which it was first defined.
     * @param	propertyList	An array of property names used to filter what 
     * 				is contained in the Objects returned. Each 
     * 				CIMClass or CIMInstance returned <b>only</b> 
     * 				contains elements for the properties of the 
     * 				names specified. Duplicate and invalid property 
     * 				names are ignored and the request is otherwise 
     * 				processed normally. An empty array indicates 
     * 				that no properties should be included in the
     * 				Objects returned. A <b>null</b> value indicates
     * 				that all properties should be contained in the
     * 				Objects returned. <b>NOTE:</b> Properties 
     * 				should <b>not</b> be specified in this parameter
     * 				unless a <b>non-null</b> value is specified in 
     * 				the <code>resultClass</code> parameter.
     * @return	If successful, an Enumeration containing zero or more CIMClass 
     * 		or CIMInstance Objects meeting the specified criteria is 
     * 		returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration associators(CIMObjectPath objectName,
	    String assocClass,
	    String resultClass,
	    String role,
	    String resultRole,
	    boolean includeQualifiers,
	    boolean includeClassOrigin,
	    String propertyList[]) throws CIMException;

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
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose associated Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @return	If successful, an Enumeration containing zero or more 
     *		CIMObjectPath objects of the CIM Classes or CIM Instances 
     *		meeting the specified criteria is returned. If no such Objects
     *		are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
	    throws CIMException;

    /**
     * Enumerates the CIMObjectPaths of CIM Objects that are associated to a 
     * particular source CIM Object. If the source Object is a CIM Class, then
     * an Enumeration of CIMObjectPaths of the classes associated to the source
     * Object is returned. If the source Object is a CIM Instance, then an
     * Enumeration of CIMObjectPaths of the CIMInstance objects associated to
     * the source Object is returned.
     *
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose associated Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @param	assocClass	This string <b>MUST</b> either contain a valid 
     *				CIM Association class name or be <b>null</b>.
     *				It filters the Objects returned to contain only
     *				Objects associated to the source Object via 
     *				this CIM Association class or one of its 
     *				subclasses.
     * @param	resultClass	This string <b>MUST</b> either contain a valid
     *				CIM Class name or be <b>null</b>. It filters the
     *				Objects returned to contain only the Objects
     *				of this Class name or one of its subclasses.
     * @param	role		This string <b>MUST</b> either contain a valid
     *				Property name or be <b>null</b>. It filters the
     *				Objects returned to contain only Objects
     *				associated to the source Object via an 
     *				Association class in which the <i>source </i>
     *				<i>Object</i> plays the specified role. (i.e. 
     *				the Property name in the Association class that 
     *				refers to the source Object matches this value) 
     *				If "Antecedent" is specified, then only
     *				Associations in which the <i>source Object</i>
     *				is the "Antecedent" reference are examined.
     * @param	resultRole	This string <b>MUST</b> either contain a valid
     *				Property name or be <b>null</b>. It filters the
     *				Objects returned to contain only Objects 
     *				associated to the source Object via an
     *				Association class in which the <i>Object </i>
     *				<i>returned</i> plays the specified role. (i.e.
     *				the Property name in the Association class that
     *				refers to the <i>Object returned</i> matches 
     *				this value) If "Dependent" is specified, then
     *				only Associations in which the <i>Object </i>
     *				<i>returned</i> is the "Dependent" reference
     *				are examined.
     * @return	If successful, an Enumeration containing zero or more 
     *		CIMObjectPath objects of the CIM Classes or CIM Instances 
     *		meeting the specified criteria is returned. If no such Objects
     *		are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
	    String assocClass,
	    String resultClass,
	    String role,
	    String resultRole) throws CIMException;

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
     * All elements (properties, methods, references) are contained in the
     * Objects returned. The qualifier and CLASSORIGIN information are also
     * contained in each Object returned.
     *
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose referring Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @return	If successful, an Enumeration containing zero or more CIMClass 
     * 		or CIMInstance Objects meeting the specified criteria is 
     * 		returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration references(CIMObjectPath objectName) throws CIMException;

    /**
     * Enumerates the Association Objects that refer to a specified source CIM
     * Object. If the source Object is a CIM Class, an Enumeration of
     * CIMClass objects is returned containing the Association classes that
     * refer to the source Object. If the source Object is a CIM Instance, an
     * Enumeration of CIMInstance objects is returned containing the 
     * Association class instances that refer to the source Object.
     *
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose referring Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @param	resultClass	This string <b>MUST</b> either contain a valid
     *				CIM Class name or be <b>null</b>. It filters the
     *				Objects returned to contain only the Objects
     *				of this Class name or one of its subclasses.
     * @param	role		This string <b>MUST</b> either contain a valid
     *				Property name or be <b>null</b>. It filters the
     *				Objects returned to contain only Objects
     *				referring to the source Object via a Property
     *				with the specified name. If "Antecedent" is 
     *				specified, then only Associations in which the
     *				source Object is the "Antecedent" reference are
     *				returned.
     * @param	includeQualifiers If true, all Qualifiers for each Object
     *				(including Qualifiers on the Object and on any 
     *				returned Properties) MUST be included in the 
     *				Objects returned. If false, no Qualifiers are
     *				present in each Object returned.
     * @param	includeClassOrigin If true, the CLASSORIGIN attribute will be 
     * 				present on all appropriate elements in the 
     * 				Objects returned. If false, no CLASSORIGIN 
     * 				attributes are present in the Objects returned. 
     * 				CLASSORIGIN is attached to an element
     * 				(properties, methods, references) to indicate 
     * 				the class in which it was first defined.
     * @param	propertyList	An array of property names used to filter what 
     * 				is contained in the Objects returned. Each 
     * 				CIMClass or CIMInstance returned <b>only</b> 
     * 				contains elements for the properties of the 
     * 				names specified. Duplicate and invalid property 
     * 				names are ignored and the request is otherwise 
     * 				processed normally. An empty array indicates 
     * 				that no properties should be included in the
     * 				Objects returned. A <b>null</b> value indicates
     * 				that all properties should be contained in the
     * 				Objects returned. <b>NOTE:</b> Properties 
     * 				should <b>not</b> be specified in this parameter
     * 				unless a <b>non-null</b> value is specified in 
     * 				the <code>resultClass</code> parameter.
     * @return	If successful, an Enumeration containing zero or more CIMClass 
     * 		or CIMInstance Objects meeting the specified criteria is 
     * 		returned. If no such Objects are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED
     *   CIM_ERR_NOT_SUPPORTED
     *   CIM_ERR_INVALID_NAMESPACE
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters)
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public Enumeration references(CIMObjectPath objectName,
	    String resultClass,
	    String role,
	    boolean includeQualifiers,
	    boolean includeClassOrigin,
	    String propertyList[]) throws CIMException;

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
     * No instance filtering is performed.
     *
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose referring Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @return	If successful, an Enumeration containing zero or more 
     *		CIMObjectPath objects of the CIM Classes or CIM Instances 
     *		meeting the specified criteria is returned. If no such Objects
     *		are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
	    throws CIMException;

    /**
     * Enumerates the CIMObjectPaths of Association Objects that are refer to a 
     * particular source CIM Object. If the source Object is a CIM Class, then
     * an Enumeration of CIMObjectPaths of the Association classes that refer to
     * the source Object is returned. If the source Object is a CIM Instance, 
     * then an Enumeration of CIMObjectPaths of the CIMInstance objects that
     * refer to the source Object is returned.
     *
     * @param	objectName	CIMObjectPath defining the source CIM Object 
     *				whose referring Objects are to be returned. 
     *				This argument may contain either a Class name 
     *				or the modelpath of an Instance. (i.e. Keys 
     *				populated)
     * @param	resultClass	This string <b>MUST</b> either contain a valid
     *				CIM Class name or be <b>null</b>. It filters the
     *				Objects returned to contain only the Objects
     *				of this Class name or one of its subclasses.
     * @param	role		This string <b>MUST</b> either contain a valid
     *				Property name or be <b>null</b>. It filters the
     *				Objects returned to contain only Objects
     *				referring to the source Object via a Property
     *				with the specified name. If "Antecedent" is 
     *				specified, then only Associations in which the
     *				source Object is the "Antecedent" reference are
     *				returned.
     * @return	If successful, an Enumeration containing zero or more 
     *		CIMObjectPath objects of the CIM Classes or CIM Instances 
     *		meeting the specified criteria is returned. If no such Objects
     *		are found, <b>null</b> is returned.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
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
	    String resultClass,
	    String role) throws CIMException;

    /**
     * Adds the specified CIMListener to receive Indications from the CIM 
     * Server. In order to force the client to stop listening for Indications, 
     * <code>close()</code> must be called. If <code>close()</code> is not
     * called, listening for Indications may still continue. The calling
     * client program will not be able to exit without calling
     * <code>System.exit()</code>. Any duplicate listeners are ignored.
     * 
     * @param	l	The CIMListener to add to receive Indications from the
     * 			CIM Server.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_INVALID_PARAMETER (including missing, duplicate, unrecognized 
     *   or otherwise incorrect parameters),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public void addCIMListener(CIMListener l) throws CIMException;

    /**
     * Removes the specified CIMListener that is receiving Indications from 
     * the CIM Server. No exception is thrown if the specified CIMListener is
     * null or if it is not receiving Indications from the CIM Server.
     * 
     * @param	l	The CIMListener to remove from the CIM Server.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public void removeCIMListener(CIMListener l) throws CIMException;

    
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
     * @param	bc	The BatchHandle that contains the list of operations
     * 			to be performed.
     * @return	BatchResult containing the return value or CIMException result
     * 		from the execution of each operation.
     * @exception CIMException	If batch mode is turned off or the list of
     *				CIM operations is null.
     */
    public BatchResult performBatchOperations(BatchHandle bc) 
	throws CIMException; 

    /**
     * This method returns an Instance of the subclass of
     * <code>CIM_IndicationHandler</code> defined for the client's protocol. 
     * Thus, for HTTP, an Instance of <code>CIM_IndicationHandlerCIMXML</code> 
     * is returned. For RMI, an Instance of the subclass of
     * <code>CIM_IndicationHandler</code> defined for RMI is returned. The 
     * returned Instance of the subclass of <code>CIM_IndicationHandler</code>
     * should then be passed to <code>createInstance()</code> to establish a
     * handler in the CIM object manager.* Using this method, a client can 
     * create a protocol independent implementation for creating a 
     * <code>CIM_IndicationHandler</code> subclass Instance.
     *
     * @param	cl	CIMListener for which a 
     * 			<code>CIM_IndicationHandler</code> subclass is being
     * 			returned. If <b>null</b>, the Instance of the 
     * 			<code>CIM_IndicationHandler</code> subclass returned
     *			can be used to deliver Indications to all listeners that
     *			have been added by the <code>addCIMListener()</code> 
     *			call. If <b>non-null</b>, the returned handler can be 
     *			used to deliver Indications to a specific listener. 
     *			<b>NOTE:</b> Unique listeners should return unique 
     *			values in their hashCode methods in order to be 
     *			differentiated. Also note that some implementations 
     *			may have partial support. For example, they may set 
     *			<code>cl</code> to null or non-null.
     * @return	Instance of the <code>CIM_IndicationHandler</code> subclass 
     * 		defined for the client's protocol.
     * @exception CIMException	If unsuccessful, one of the following status 
     *				codes <b>must</b> be returned. The ORDERED list
     *				is: 
     * <pre>
     *   CIM_ERR_ACCESS_DENIED,
     *   CIM_ERR_NOT_SUPPORTED (if CIMOM does not support events),
     *   CIM_ERR_FAILED (some other unspecified error occurred)
     * </pre>
     */
    public CIMInstance getIndicationHandler(CIMListener cl) throws CIMException;

} // CIMOMHandle
