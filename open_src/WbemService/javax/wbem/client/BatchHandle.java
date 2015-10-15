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
 *Contributor(s): _______________________________________
*/

package javax.wbem.client;

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
public interface BatchHandle {

    /**
     * Creates a CIM namespace, a directory containing CIM classes and 
     * CIM instances. When a client application connects to the CIM
     * Object Manager, it specifies a namespace. All subsequent 
     * operations occur within that namespace on the CIM Object Manager 
     * host.
     *
     * @param ns	The CIMNameSpace object that specifies a string
     *			for the host and a string for the namespace
     *
     * @return int - Operation ID. 
     *
     *
     */

    public int createNameSpace(CIMNameSpace ns) throws CIMException; 


    /**
     * Deletes the specified namespace on the current host.
     *
     * @param ns The CIMNameSpace object that identifies the namespace
     * 		to be deleted
     *
     * @return int - Operation ID. 
     *
     */

    public int deleteNameSpace(CIMNameSpace ns) throws CIMException; 

    /**
     * Deletes the CIM class for the object specified by the CIM object path,
     * a name that uniquely identifies a CIM object. The CIM object path
     * consists of two parts:  namespace + model path. The namespace portion
     * of the CIM object path identifies the host running the CIM Object
     * Manager and the namespace. A namespace is a directory-like structure
     * that contains CIM classes, instances, and qualifier types.
     * <p>
     * The model path portion of the CIM object path uniquely identifies a 
     * CIM object within the namespace on the host. The model path for a 
     * class is the class name.
     *
     * @param path   The CIMObjectPath identifying the class to delete
     *
     * @return int - Operation ID. 
     *
     */
    public int deleteClass(CIMObjectPath path)  throws CIMException;	 

    /**
     * Deletes the CIM instance specified by the CIM object path,
     * a name that uniquely identifies a CIM object. 
     * A CIM object path consists of two parts:  namespace + model path.
     * The model path is created by concatenating the properties of a class
     * that are qualified with the <code>KEY</code> qualifier.
     *
     * @param path    The CIMObjectPath identifying the CIM instance to delete
     *
     * @return int - Operation ID. 
     *
     *
     */

    public int deleteInstance(CIMObjectPath path) throws CIMException; 

    /**
     * Deletes the CIM qualfier for the object specified by the CIM object path,
     * a name that uniquely identifies a CIM object. 
     *
     * @param path    the CIMObjectPath identifying the CIM qualifier
     *	    		to delete
     *
     * @return int - Operation ID. 
     *
     *
     */
    public int deleteQualifierType(CIMObjectPath path) throws CIMException; 

    /**
     *
     * Gets a list of the namespaces within the namespace specified by the CIM 
     * object path. 
     *
     * @param path The CIMObjectPath identifying the namespace to be enumerated.
     * @param deep If set to true, the enumeration returned will
     *             contain the entire hierarchy of namespaces present under the 
     *             enumerated namespace. If set to false the
     *             enumeration will return only the first level children of the
     *             enumerated namespace.
     * 
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */

    public int enumNameSpace(CIMObjectPath path, boolean
    deep)  throws CIMException; 

    /**
     * Enumerates the class specified in the path. The entire class contents
     * (and not just the class names) are returned.
     *
     * @param path  The CIMObjectPath identifying the class to be enumerated.
     * @param deep  If set to true, the enumeration returned will
     *              contain the names of all classes derived from the enumerated
     *            class. If set to false the enumeration will return 
     *             only the names of the first level children of the enumerated 
     *             class.
     * @param localOnly   if true, only the non-inherited properties and 
     *                    methods are returned, otherwise all properties
     *                    and methods are returned.
     *
     * @param includeQualifiers If the IncludeQualifiers input parameter is true
     * this specifies that all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included.  
     * If false no Qualifiers are present in the returned Object.
     *
     * @param includeClassOrigin If the IncludeClassOrigin input parameter is 
     * true, this specifies that the CLASSORIGIN attribute MUST be present on 
     * all appropriate elements in each returned Object. If false, no 
     * CLASSORIGIN attributes are present in each returned Object.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     * @see CIMClient#enumerateClassNames(CIMObjectPath, boolean)
     * @see CIMClient#enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
     */
    public int enumerateClasses(CIMObjectPath path, 
					 boolean deep,
					 boolean localOnly,
					 boolean includeQualifiers,
					 boolean includeClassOrigin) 
	throws CIMException; 

    /**
     * Enumerates the class specified in the path. The class NAMES are returned
     * as a vector of CIMObjectPaths.
     *
     * @param path  The CIMObjectPath identifying the class to be enumerated.
     * @param deep  If set to true, the enumeration returned will
     *              contain the names of all classes derived from the enumerated
     *           class. If set to false the enumeration will return 
     *             only the names of the first level children of the enumerated 
     *              class.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     *
     * 
     */
    public int enumerateClassNames(CIMObjectPath path, 
					    boolean deep) throws CIMException; 

    /**
     * Returns all instance NAMES belonging to the class specified in the path.
     * This could include instances names of all the classes in 
     * the specified class' hierarchy.
     *
     * @param path  The CIMObjectPath identifying the class whose instances are
     *              to be enumerated.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int enumerateInstanceNames(CIMObjectPath path) throws CIMException; 

    /**
     * Returns all instances (the whole instance and not just the names) 
     * belonging to the class specified in the path. This could 
     * include instances of all the classes in the specified class' hierarchy.
     *
     * @param path  The CIMObjectPath identifying the class whose instances are
     *              to be enumerated.
     * @param deep  If set to false, the enumeration returned will 
     *              contain only the properties defined for the class specified
     *              in the path parameter. If set to true all 
     *              properties are returned.
     * @param localOnly   if true, only the non-inherited properties
     *                    are returned, otherwise all properties are returned.
     * @param includeQualifiers If the IncludeQualifiers input parameter is true
     * this specifies that all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included.  
     * If false no Qualifiers are present in the returned Object.
     *
     * @param includeClassOrigin If the IncludeClassOrigin input parameter is 
     * true, this specifies that the CLASSORIGIN attribute MUST be present on 
     * all appropriate elements in each returned Object. If false, no 
     * CLASSORIGIN attributes are present in each returned Object.
     *
     * @param propertyList If the PropertyList input parameter is not NULL, the 
     * members of the array define one or more Property names.  Each returned 
     * Object MUST NOT include elements for any Properties missing from this 
     * list.  Note that if LocalOnly is specified as true (or DeepInheritance is
     * specified as false) this acts as an additional filter on the set of 
     * Properties returned (for example, if Property A is included in the 
     * PropertyList but LocalOnly is set to true and A is not local to a
     * returned Instance, then it will not be included in that Instance). If the
     * PropertyList input parameter is an empty array this signifies that no 
     * Properties are included in each returned Object. If the PropertyList 
     * input parameter is NULL this specifies that all Properties (subject to 
     * the conditions expressed by the other parameters) are included in each 
     * returned Object.  
     * If the PropertyList contains duplicate elements, the Server ignores the 
     * duplicates but otherwise process the request normally.  If the 
     * PropertyList contains elements which are invalid Property names for any 
     * target Object, the Server ignores such entries but otherwise process the 
     * request normally.  
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public  int enumerateInstances(CIMObjectPath path,
						boolean deep,
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin,
						String propertyList[]) 
	throws CIMException; 


    /**
     * Enumerates the qualifiers defined in a namespace.
     *
     * @param path  The CIMObjectPath identifying the namespace whose
     *              qualifier definitions are to be enumerated.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     *
     */
    public int enumQualifierTypes(CIMObjectPath path) throws CIMException; 

    /**
     * Gets the CIM class for the specified CIM object path.
     *
     * @param name   the CIMObjectPath that identifies the CIM class
     * @param localOnly   if true, only the non-inherited properties and 
     *                    methods are returned, otherwise all properties
     *                    and methods are returned.
     * @param includeQualifiers If the IncludeQualifiers input parameter is true
     * this specifies that all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included.  
     * If false no Qualifiers are present in the returned Object.
     *
     * @param includeClassOrigin If the IncludeClassOrigin input parameter is 
     * true, this specifies that the CLASSORIGIN attribute MUST be present on 
     * all appropriate elements in each returned Object. If false, no 
     * CLASSORIGIN attributes are present in each returned Object.
     *
     * @param propertyList If the PropertyList input parameter is not NULL, the 
     * members of the array define one or more Property names.  Each returned 
     * Object MUST NOT include elements for any Properties missing from this 
     * list.  Note that if LocalOnly is specified as true (or DeepInheritance is
     * specified as false) this acts as an additional filter on the set of 
     * Properties returned (for example, if Property A is included in the 
     * PropertyList but LocalOnly is set to true and A is not local to a
     * returned Instance, then it will not be included in that Instance). If the
     * PropertyList input parameter is an empty array this signifies that no 
     * Properties are included in each returned Object. If the PropertyList 
     * input parameter is NULL this specifies that all Properties (subject to 
     * the conditions expressed by the other parameters) are included in each 
     * returned Object.  
     * If the PropertyList contains duplicate elements, the Server ignores the 
     * duplicates but otherwise process the request normally.  If the 
     * PropertyList contains elements which are invalid Property names for any 
     * target Object, the Server ignores such entries but otherwise process the 
     * request normally.  
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     * @see CIMClient#enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
     * @see CIMClient#enumerateClassNames(CIMObjectPath, boolean)
     */
    public int getClass(CIMObjectPath name, 
			      boolean localOnly,
			      boolean includeQualifiers,
			      boolean includeClassOrigin,
			      String propertyList[])  throws CIMException; 

    /**
     * Gets the CIM instance for the specified CIM object path.
     *
     * @param name   CIM Object Path that identifies this CIM instance
     * @param localOnly   if true, only the non-inherited properties 
     *                    are returned, otherwise all properties are returned.
     *
     * @param includeQualifiers If the IncludeQualifiers input parameter is true
     * this specifies that all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included.  
     * If false no Qualifiers are present in the returned Object.
     *
     * @param includeClassOrigin If the IncludeClassOrigin input parameter is 
     * true, this specifies that the CLASSORIGIN attribute MUST be present on 
     * all appropriate elements in each returned Object. If false, no 
     * CLASSORIGIN attributes are present in each returned Object.
     *
     * @param propertyList If the PropertyList input parameter is not NULL, the 
     * members of the array define one or more Property names.  Each returned 
     * Object MUST NOT include elements for any Properties missing from this 
     * list.  Note that if LocalOnly is specified as true (or DeepInheritance is
     * specified as false) this acts as an additional filter on the set of 
     * Properties returned (for example, if Property A is included in the 
     * PropertyList but LocalOnly is set to true and A is not local to a
     * returned Instance, then it will not be included in that Instance). If the
     * PropertyList input parameter is an empty array this signifies that no 
     * Properties are included in each returned Object. If the PropertyList 
     * input parameter is NULL this specifies that all Properties (subject to 
     * the conditions expressed by the other parameters) are included in each 
     * returned Object.  
     * If the PropertyList contains duplicate elements, the Server ignores the 
     * duplicates but otherwise process the request normally.  If the 
     * PropertyList contains elements which are invalid Property names for any 
     * target Object, the Server ignores such entries but otherwise process the 
     * request normally.  
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     * @see CIMClient#enumerateInstances(CIMObjectPath, boolean, boolean, boolean, boolean, String[])
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public int getInstance(CIMObjectPath name, 
				    boolean localOnly,
				    boolean includeQualifiers,
				    boolean includeClassOrigin,
				    String propertyList[])  throws CIMException;

    /**
     * Executes the specified method on the specified object. 
     * A method is a declaration containing the method name, return
     * type, and parameters in the method.
     *
     * @param name	 CIM object path that identifies the method
     * @param methodName the string name of the method to be invoked
     * @param inParams   the input parameters specified as a vector of CIMValue.
     * @param outParams  The output parameters, the CIMValue of these parameters
     *                   will be appended to the outParams vector. The
     *                   outParams vector is populated when the
     *                   performBatchOperations call is completed.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int invokeMethod(CIMObjectPath name,
			    String methodName,
			    CIMArgument[] inParams,
			    CIMArgument[] outParams) throws CIMException; 

    /**
     * Gets the CIM qualifier type specified in the CIM object path.
     *
     * @param name   CIM object path that identifies the CIM qualifier type
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int getQualifierType(CIMObjectPath name)  throws CIMException; 

    /**
     * Adds the specified CIM qualifier type to the specified namespace.
     *
     *
     * @return int - Operation ID. 
     *
     * @param name   CIM object path that identifies the CIM 
     *	qualifier type
     * @param qt   the CIM qualifier type to be added
     *
     */
    public int createQualifierType(CIMObjectPath name,
				     CIMQualifierType qt) throws CIMException; 
	
    /**
     * Adds the specified CIM qualifier type to the specified namespace.
     *
     * @param name   CIM object path that identifies the CIM 
     *	qualifier type
     * @param qt   the CIM qualifier type to be added
     *
     * @return int - Operation ID. 
     *
     *
     */
    public int setQualifierType(CIMObjectPath name,
					  CIMQualifierType qt)
    throws CIMException; 

    /**
     * Adds the CIM class to the specified namespace.
     *
     * @param name   CIM object path that identifies the CIM 
     *	class to be added
     * @param cc   CIMClass to be added
     *
     * @return int - Operation ID. 
     *
     */
    public int createClass(CIMObjectPath name, CIMClass cc) 
    throws CIMException; 

       
    /**
     * Modifies the CIM class in the specified namespace.
     *
     * @param name 	CIM object path that identifies the CIM 
     *		  	class to be modified 
     * @param cc   	CIMClass to be modified
     *
     * @return int - Operation ID. 
     *
     *
     */
    public  int setClass(CIMObjectPath name, CIMClass cc) throws CIMException; 

    /**
     * Invokes the object manager on this client to add
     * the specified CIM instance to the specified namespace.
     *
     * @param name   CIM object path that identifies the CIM 
     *	instance to be added
     * @param ci   CIM instance to be added
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int createInstance(CIMObjectPath name, 
    			    CIMInstance ci)  throws CIMException; 

    /**
     * Invokes the object manager on this client to modify
     * the specified CIM instance in the specified namespace.
     *
     * @param name   	CIM object path that identifies the CIM 
     *			instance to be modified
     * @param ci   	CIM instance to be modified
     *
     * @return int - Operation ID. 
     *
     */
    public int setInstance(CIMObjectPath name, 
    			    CIMInstance ci) throws CIMException; 

    /**
     * Invokes the object manager on this client to get
     * the specified CIM instance property.
     *
     * @param name   CIM object path that identifies the CIM 
     *	instance to be accessed
     * @param propertyName   Property whose value is required.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int getProperty(CIMObjectPath name, String propertyName) 
    throws CIMException; 

    /**
     * Invokes the object manager on this client to set
     * the specified CIM instance property value.
     *
     * @param name   CIM object path that identifies the CIM 
     *	instance to be accessed
     * @param propertyName   Property whose value is to be set.
     * @param newValue  The value for property propertyName.
     *
     * @return int - Operation ID. 
     *
     */
    public int setProperty(CIMObjectPath name, 
				String propertyName, 
				CIMValue newValue)  throws CIMException; 

    /**
     * Invokes the object manager on this client to set
     * the specified CIM instance property value. This is equivalent
     * to calling setProperty with a null value.
     *
     * @param name   CIM object path that identifies the CIM 
     *	instance to be accessed
     * @param propertyName   Property whose value is to be set.
     *
     * @return int - Operation ID. 
     *
     */
    public int setProperty(CIMObjectPath name, 
				String propertyName) throws CIMException;
    /**
     * Executes a query to retrieve objects.
     *
     * The WBEM Query Language is a subset of standard American
     * National Standards Institute Structured Query Language (ANSI SQL)
     * with semantic changes to support WBEM. Unlike SQL,
     * in this release, it is a retrieval-only language. You cannot
     * use the WBEM Query Language to modify, insert, or delete information. 
     * <p>
     * Only queries on class instances are supported.  
     * <p>
     * @param name 	CIMObjectPath that identifies the class in which
     *			to query.
     * @param query	A string containing the text of the query.
     *                  This parameter cannot be null.
     * @param ql        String that identifies the query language to use
     *			for parsing the query string. WQL level 1 is 
     *			the only currently supported query language.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     * <ulist>
     * <li>The user does not have permission to view the result.
     * <li>The requested query language is not supported.
     * <li>The query specifies a class that does not exist.
     * </ulist>			
     * <p>
     * <b>Example:</b>
     * <p>
     * The following API call returns an enumeration of all instances of
     * the <code>device_class</code>.
     * <p>
     * <code>cc.execQuery(new CIMObjectPath(),
     *                    SELECT * FROM device_class, cc.WQL)</code>
     * 
     */
    public int execQuery(CIMObjectPath name, 
				  String query,
				  String ql)  throws CIMException; 
	

    /**
     * This operation is used to enumerate CIM Objects (Classes or Instances) 
     * that are associated to a particular source CIM Object.
     *
     * @param objectName - Defines the source CIM Object whose associated 
     * 			   Objects are to be returned.  This may be either 
     *			   a Class name or Instance name (modelpath).
     *
     * @param assocClass The AssocClass input parameter, if not NULL, MUST be a 
     *   valid CIM Association Class name. It acts as a filter on the 
     *   returned set of Objects by mandating that each returned Object MUST be
     *   associated to the source Object via an Instance of this
     *   Class or one of its subclasses. 
     *
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *   a valid CIM Class name. It acts as a filter on the returned set of 
     *   Objects by mandating that each returned Object MUST be either an 
     *   Instance of this Class (or one of its subclasses) or be this
     *   Class (or one of its subclasses).
     *
     * @param role The Role input parameter, if not NULL, MUST be a valid 
     * Property name. It acts as a filter on the returned set of Objects by 
     * mandating that each returned Object MUST be associated to the source 
     * Object via an Association in which the source Object plays the specified
     * role (i.e. the name of the Property in the Association Class that refers
     * to the source Object MUST match the value of this parameter).
     *
     * @param resultRole  The ResultRole input parameter, if not NULL, MUST be a
     * valid Property name. It acts as a filter on the returned set of Objects 
     * by mandating that each returned Object MUST be associated to the source 
     * Object via an Association in which the returned Object plays the 
     * specified role (i.e. the name of the Property in the Association Class 
     * that refers to the returned Object MUST match the value of this 
     * parameter).
     *
     * @param includeQualifiers If the IncludeQualifiers input parameter is true
     * this specifies that all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included as 
     * <QUALIFIER> elements in the response.  If false no <QUALIFIER> elements 
     * are present in each returned Object.
     * @param includeClassOrigin If the IncludeClassOrigin input parameter is 
     * true, this specifies that the CLASSORIGIN attribute MUST be present on 
     * all appropriate elements in each returned Object. If false, no 
     * CLASSORIGIN attributes are present in each returned Object.
     *
     * @param propertyList If the PropertyList input parameter is not NULL, the 
     * members of the array define one or more Property names.  Each returned 
     * Object MUST NOT include elements for any Properties missing from this 
     * list.  Note that if LocalOnly is specified as true (or DeepInheritance is
     * specified as false) this acts as an additional filter on the set of 
     * Properties returned (for example, if Property A is included in the 
     * PropertyList but LocalOnly is set to true and A is not local to a
     * returned Instance, then it will not be included in that Instance). If the
     * PropertyList input parameter is an empty array this signifies that no 
     * Properties are included in each returned Object. If the PropertyList 
     * input parameter is NULL this specifies that all Properties (subject to 
     * the conditions expressed by the other parameters) are included in each 
     * returned Object.  
     * If the PropertyList contains duplicate elements, the Server ignores the 
     * duplicates but otherwise process the request normally.  If the 
     * PropertyList contains elements which are invalid Property names for any 
     * target Object, the Server ignores such entries but otherwise process the 
     * request normally.  
     * Clients SHOULD NOT explicitly specify properties in the PropertyList 
     * parameter unless they have specified a non-NULL value for the 
     * ResultClass parameter.
     *
     * @return int - Operation ID. Result array from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int associators(CIMObjectPath objectName,
				    String assocClass,
				    String resultClass,
				    String role,
				    String resultRole,
				    boolean includeQualifiers,
				    boolean includeClassOrigin,
				    String propertyList[])  throws CIMException;

    /**
     * This operation is used to enumerate CIM Objects (Classes or Instances) 
     * that are associated to a particular source CIM Object.
     *
     * @param objectName - Defines the source CIM Object whose associated 
     * 			   Objects are to be returned.  This may be either 
     *			   a Class name or Instance name (modelpath).
     *
     * @return int - Operation ID. Result array from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int associators(CIMObjectPath objectName) throws CIMException;


    /**
     * This operation is used to enumerate the names of CIM Objects (Classes or 
     * Instances) that are associated to a particular source CIM Object.
     * @param objectName - Defines the source CIM Object whose associated 
     * 			   Objects are to be returned.  This may be either 
     *			   a Class name or Instance name (modelpath).
     *
     * @param assocClass The AssocClass input parameter, if not NULL, MUST be a 
     *   valid CIM Association Class name. It acts as a filter on the 
     *   returned set of Objects by mandating that each returned Object MUST be
     *   associated to the source Object via an Instance of this
     *   Class or one of its subclasses. 
     *
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *   a valid CIM Class name. It acts as a filter on the returned set of 
     *   Objects by mandating that each returned Object MUST be either an 
     *   Instance of this Class (or one of its subclasses) or be this
     *   Class (or one of its subclasses).
     *
     * @param role The Role input parameter, if not NULL, MUST be a valid 
     * Property name. It acts as a filter on the returned set of Objects by 
     * mandating that each returned Object MUST be associated to the source 
     * Object via an Association in which the source Object plays the specified
     * role (i.e. the name of the Property in the Association Class that refers
     * to the source Object MUST match the value of this parameter).
     *
     * @param resultRole  The ResultRole input parameter, if not NULL, MUST be a
     * valid Property name. It acts as a filter on the returned set of Objects 
     * by mandating that each returned Object MUST be associated to the source 
     * Object via an Association in which the returned Object plays the 
     * specified role (i.e. the name of the Property in the Association Class 
     * that refers to the returned Object MUST match the value of this 
     * parameter).
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int associatorNames(CIMObjectPath objectName,
				    String assocClass,
				    String resultClass,
				    String role,
				    String resultRole)  throws CIMException; 

    /**
     * This operation is used to enumerate the names of CIM Objects (Classes or 
     * Instances) that are associated to a particular source CIM Object.
     * @param objectName - Defines the source CIM Object whose associated 
     * 			   Objects are to be returned.  This may be either 
     *			   a Class name or Instance name (modelpath).
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int associatorNames(CIMObjectPath objectName) throws CIMException;

    /**
     * This operation is used to enumerate the association objects that refer to
     * a particular target CIM Object (Class or Instance).
     *
     * @param objectName The ObjectName input parameter defines the target CIM 
     * Object whose referring Objects are to be returned. This is either a Class
     * name or Instance name (model path).  
     *
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be 
     * a valid CIM Class name. It acts as a filter on the returned set of 
     * Objects by mandating that each returned Object MUST be an Instance of 
     * this Class (or one of its subclasses), or this Class (or one of its 
     * subclasses).
     *
     * @param role The Role input parameter, if not NULL, MUST be a valid 
     * Property name. It acts as a filter on the returned set of Objects by 
     * mandating that each returned Objects MUST refer to the target Object via 
     * a Property whose name matches the value of this parameter.
     *
     * @param includeQualifiers If the IncludeQualifiers input parameter is true
     * this specifies that all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included as 
     * <QUALIFIER> elements in the response.  If false no <QUALIFIER> elements 
     * are present in each returned Object.
     *
     * @param includeClassOrigin If the IncludeClassOrigin input parameter is 
     * true, this specifies that the CLASSORIGIN attribute MUST be present on 
     * all appropriate elements in each returned Object. If false, no 
     * CLASSORIGIN attributes are present in each returned Object.
     *
     * @param propertyList If the PropertyList input parameter is not NULL, the 
     * members of the array define one or more Property names.  Each returned 
     * Object MUST NOT include elements for any Properties missing from this 
     * list.  Note that if LocalOnly is specified as true (or DeepInheritance is
     * specified as false) this acts as an additional filter on the set of 
     * Properties returned (for example, if Property A is included in the 
     * PropertyList but LocalOnly is set to true and A is not local to a
     * returned Instance, then it will not be included in that Instance). If the
     * PropertyList input parameter is an empty array this signifies that no 
     * Properties are included in each returned Object. If the PropertyList 
     * input parameter is NULL this specifies that all Properties (subject to 
     * the conditions expressed by the other parameters) are included in each 
     * returned Object.  
     * If the PropertyList contains duplicate elements, the Server ignores the 
     * duplicates but otherwise process the request normally.  If the 
     * PropertyList contains elements which are invalid Property names for any 
     * target Object, the Server ignores such entries but otherwise process the 
     * request normally.  
     * Clients SHOULD NOT explicitly specify properties in the PropertyList 
     * parameter unless they have specified a non-NULL value for the 
     * ResultClass parameter.
     * 
     * Instances meeting the requested criteria.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */

    public int references(CIMObjectPath objectName,
				    String resultClass,
				    String role,
				    boolean includeQualifiers,
				    boolean includeClassOrigin,
				    String propertyList[])  throws CIMException;

    /**
     * This operation is used to enumerate the association objects that refer to
     * a particular target CIM Object (Class or Instance).
     *
     * @param objectName The ObjectName input parameter defines the target CIM 
     * Object whose referring Objects are to be returned. This is either a Class
     * name or Instance name (model path).  
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */

    public int references(CIMObjectPath objectName) throws CIMException;

    /**
     * This operation is used to enumerate the association objects that refer to
     * a particular target CIM Object (Class or Instance).
     * 
     * @param objectName The ObjectName input parameter defines the target CIM 
     * Object whose referring Objects are to be returned. This is either a Class
     * name or Instance name (model path).  
     *
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be 
     * a valid CIM Class name. It acts as a filter on the returned set of 
     * Objects by mandating that each returned Object MUST be an Instance of 
     * this Class (or one of its subclasses), or this Class (or one of its 
     * subclasses).
     * @param role The Role input parameter, if not NULL, MUST be a valid 
     * Property name. It acts as a filter on the returned set of Objects by 
     * mandating that each returned Objects MUST refer to the target Object via 
     * a Property whose name matches the value of this parameter.
     *
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int referenceNames(CIMObjectPath objectName,
				    String resultClass,
				    String role)  throws CIMException; 

    /**
     * This operation is used to enumerate the association objects that refer to
     * a particular target CIM Object (Class or Instance).
     * 
     * @param objectName The ObjectName input parameter defines the target CIM 
     * Object whose referring Objects are to be returned. This is either a Class
     * name or Instance name (model path).  
     * @return int - Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public int referenceNames(CIMObjectPath objectName) throws CIMException;
}
