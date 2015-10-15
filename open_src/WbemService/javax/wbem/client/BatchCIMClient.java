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

import java.util.ArrayList;

import javax.wbem.cim.CIMArgument;
import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMNameSpace;
import javax.wbem.cim.CIMObjectPath;
import javax.wbem.cim.CIMQualifierType;
import javax.wbem.cim.CIMValue;

/**
 * The BatchCIMClient allows a client application to batch multiple requests
 * into one remote call. The methods in this class correspond to CIM operation.
 * A method when invoked, gives rise to a CIMOperation object. As multiple
 * methods are invoked, a list of such CIMOperation objects is built. The
 * client application executes the batch of requests by invoking the
 * performBatchOperations() method on the CIMClient object and passing a
 * BatchCIMClient object as a parameter.
 * 
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public class BatchCIMClient implements BatchHandle {

    private ArrayList opList = null;
    int index = 0;

    /**
     * Constructor to create a BatchCIMClient object. Creates a new
     * BatchCIMClient object that can be used to build a list of CIM Operations
     * to be executed as a batched request.
     */
    public BatchCIMClient() {
	
	opList = new ArrayList();
	index = 0;
    }

    /**
     * Creates a CIM namespace, a directory containing CIM classes and CIM
     * instances. When a client application connects to the CIM Object Manager,
     * it specifies a namespace. All subsequent operations occur within that
     * namespace on the CIM Object Manager host.
     * 
     * @param ns The CIMNameSpace object that specifies a string for the host
     *                and a string for the namespace
     * @return the Operation ID.
     * @deprecated
     */
    public synchronized int createNameSpace(CIMNameSpace ns) 
	    throws CIMException {

	CIMCreateNameSpaceOp op = new CIMCreateNameSpaceOp(ns);
	op.setTargetNS(ns);
	opList.add(op);
	
	return index++;
    }


    /**
     * Deletes the specified namespace on the current host.
     * 
     * @param ns The CIMNameSpace object that identifies the namespace to be
     *                deleted
     * @return the Operation ID.
     * @deprecated
     */
    public synchronized int deleteNameSpace(CIMNameSpace ns) 
	    throws CIMException {

	CIMDeleteNameSpaceOp op = new CIMDeleteNameSpaceOp(ns);
	op.setTargetNS(ns);
	opList.add(op);

	return index++;
    }

    /**
     * Deletes the CIM class for the object specified by the CIM object path, a
     * name that uniquely identifies a CIM object. The CIM object path consists
     * of two parts: namespace + model path. The namespace portion of the CIM
     * object path identifies the host running the CIM Object Manager and the
     * namespace. A namespace is a directory-like structure that contains CIM
     * classes, instances, and qualifier types.
     * <p>
     * The model path portion of the CIM object path uniquely identifies a CIM
     * object within the namespace on the host. The model path for a class is
     * the class name.
     * 
     * @param path The CIMObjectPath identifying the class to delete
     * @return the Operation ID.
     */
    public synchronized int deleteClass(CIMObjectPath path) 
	    throws CIMException {

	CIMDeleteClassOp op = new CIMDeleteClassOp(path);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }	 

    /**
     * Deletes the CIM instance specified by the CIM object path, a name that
     * uniquely identifies a CIM object. A CIM object path consists of two
     * parts: namespace + model path. The model path is created by
     * concatenating the properties of a class that are qualified with the
     * <code>KEY</code> qualifier.
     * 
     * @param path The CIMObjectPath identifying the CIM instance to delete
     * @return the Operation ID.
     */
    public synchronized int deleteInstance(CIMObjectPath path) 
	    throws CIMException {

	CIMDeleteInstanceOp op = new CIMDeleteInstanceOp(path);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Deletes the CIM qualfier to the specified namespace.
     * 
     * @param path the CIMObjectPath identifying the CIM qualifier to delete
     * @return the Operation ID.
     */
    public synchronized int deleteQualifierType(CIMObjectPath path) 
	    throws CIMException {

	CIMDeleteQualifierTypeOp op = new CIMDeleteQualifierTypeOp(path);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Gets a list of the namespaces within the namespace specified by the CIM
     * object path.
     * 
     * @param path The CIMObjectPath identifying the namespace to be
     *                enumerated.
     * @param deep If set to true, the enumeration returned will contain the
     *                entire hierarchy of namespaces present under the
     *                enumerated namespace. If set to false the enumeration
     *                will return only the first level children of the
     *                enumerated namespace.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     * @deprecated
     */
    public synchronized int enumNameSpace(CIMObjectPath path, boolean deep) 
	    throws CIMException {

	CIMEnumNameSpaceOp op = new CIMEnumNameSpaceOp(path, deep);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

   /**
    * Enumerates the class specified in the path. The entire class contents
    * (and not just the class names) are returned.
    * 
    * @param path The CIMObjectPath identifying the class to be enumerated.
    * @param deep If true, the enumeration returned will contain the contents
    *                of all subclasses derived from the specified class. If
    *                false, the enumeration will return only the contents of
    *                the first level children of the specified class.
    * @param localOnly If true, only elements (properties, methods and
    *                qualifiers) defined or overridden within the class are
    *                included in the response. If false, all elements of the
    *                class definition are returned.
    * @param includeQualifiers If true, all Qualifiers for each Object
    *                (including Qualifiers on the Object and on any returned
    *                Properties) MUST be included. If false, no Qualifiers are
    *                present in the returned Object.
    * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
    *                present on all appropriate elements in each returned
    *                Object. If false, no CLASSORIGIN attributes are present in
    *                each returned Object. The CLASSORIGIN attribute is defined
    *                in the DMTF's Specification for the Representation of CIM
    *                in XML. CLASSORIGIN is an XML tag identifying the
    *                following text as a class name. It is attached to a
    *                property or method (when specified in XML), to indicate
    *                the class where that property or method is first defined.
    *                Where the same property name is locally defined in another
    *                superclass or subclass, the Server will return the value
    *                for the property in the lowest subclass.
    * @return the Operation ID. Result list from the performBatch method
    *         contains the actual return value.
    * @see CIMClient#enumerateClassNames(CIMObjectPath, boolean)
    * @see CIMClient#enumerateClasses(CIMObjectPath, boolean, boolean, boolean, boolean)
    */
    public synchronized int enumerateClasses(CIMObjectPath path, 
					 boolean deep,
					 boolean localOnly,
					 boolean includeQualifiers,
					 boolean includeClassOrigin) 
	    throws CIMException {


	CIMEnumClassOp op = 
	    new CIMEnumClassOp(path, deep, localOnly, includeQualifiers, 
			       includeClassOrigin);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Enumerates the class specified in the path. The class NAMES are returned
     * as a vector of CIMObjectPaths. If none are found, then NULL is returned.
     * 
     * @param path The CIMObjectPath identifying the class to be enumerated.
     * @param deep If true, the enumeration returned will contain the names of
     *                all subclasses derived from the specified class. If false
     *                the enumeration will return only the names of the first
     *                level children of the specified class.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int enumerateClassNames(CIMObjectPath path, 
					    boolean deep) 
	    throws CIMException {
	
	CIMEnumClassNamesOp op = new CIMEnumClassNamesOp(path, deep);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Returns the object paths to all instances belonging to the class
     * specified in the path. The objectpaths of all derived classes in the
     * specified class heirarchy will also be returned. If none are found, then
     * NULL is returned.
     * 
     * @param path The CIMObjectPath identifying the class whose instances are
     *                to be enumerated. Only the name space and class name
     *                components are used. Any other information (e.g. keys) is
     *                ignored.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int enumerateInstanceNames(CIMObjectPath path) 
	    throws CIMException {

	CIMEnumInstanceNamesOp op = new CIMEnumInstanceNamesOp(path);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Returns all instances of the class specified in the path. The method
     * also returns all instances of classes derived from the specified class.
     * 
     * @param path The CIMObjectPath identifying the class whose instances are
     *                to be enumerated. Only the name space and class name
     *                components are used. Any other information (e.g. keys) is
     *                ignored.
     * @param deep If true, all elements of the returned Instance are returned
     *                (subject to the contraints imposed by the other
     *                parameters). If false, each returned Instance includes
     *                only properties defined for the specified Class.
     * @param localOnly If true, only properties and qualifiers overridden or
     *                defined in the returned Instance are included in the
     *                response. If false, all elements of the returned Instance
     *                are returned.
     * @param includeQualifiers If the IncludeQualifiers input parameter is
     *                true this specifies that all Qualifiers for each Object
     *                (including Qualifiers on the Object and on any returned
     *                Properties) MUST be included. If false no Qualifiers are
     *                present in the returned Object.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Object. If false, no CLASSORIGIN attributes are present
     *                in each returned Object. The CLASSORIGIN attribute is
     *                defined in the DMTF's Specification for the
     *                Representation of CIM in XML. CLASSORIGIN is an XML tag
     *                identifying the following text as a class name. It is
     *                attached to a property or method (when specified in XML),
     *                to indicate the class where that property or method is
     *                first defined. Where the same property name is locally
     *                defined in another superclass or subclass, the Server
     *                will return the value for the property in the lowest
     *                subclass.
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                Each returned Object MUST NOT include elements for any
     *                Properties missing from this list. Note that if LocalOnly
     *                is specified as true (or Deep is specified as false) this
     *                acts as an additional filter on the set of Properties
     *                returned (for example, if Property A is included in the
     *                PropertyList but LocalOnly is set to true and A is not
     *                local to a returned Instance, then it will not be
     *                included in that Instance). If the PropertyList input
     *                parameter is an empty array this signifies that no
     *                Properties are included in each returned Object. If the
     *                PropertyList input parameter is NULL this specifies that
     *                all Properties (subject to the conditions expressed by
     *                the other parameters) are included in each returned
     *                Object. If the PropertyList contains duplicate elements,
     *                the Server ignores the duplicates but otherwise process
     *                the request normally. If the PropertyList contains
     *                elements which are invalid Property names for any target
     *                Object, the Server ignores such entries but otherwise
     *                process the request normally.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public  synchronized int enumerateInstances(CIMObjectPath path,
						boolean deep,
						boolean localOnly,
						boolean includeQualifiers,
						boolean includeClassOrigin,
						String propertyList[]) 
	    throws CIMException {

	CIMEnumInstancesOp op = new CIMEnumInstancesOp(path, deep, localOnly,
			includeQualifiers, includeClassOrigin, propertyList);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }


    /**
     * Enumerates the qualifiers defined in a namespace. If none are found,
     * then NULL is returned.
     * 
     * @param path The CIMObjectPath identifying the namespace whose qualifier
     *                definitions are to be enumerated.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int enumQualifierTypes(CIMObjectPath path) 
	    throws CIMException {

	CIMEnumQualifierTypesOp op = new CIMEnumQualifierTypesOp(path);
	CIMNameSpace cns = new CIMNameSpace("", path.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Gets the CIM class for the specified CIM object path. The entire class
     * contents (and not just the class names) are returned.
     * 
     * @param name the CIMObjectPath that identifies the CIM class
     * @param localOnly If true, only elements (properties, methods and
     *                qualifiers) defined or overridden within the class are
     *                included in the response. If false, all elements of the
     *                class definition are returned.
     * @param includeQualifiers If true, all Qualifiers for each Object
     *                (including Qualifiers on the Object and on any returned
     *                Properties) MUST be included. If false, no Qualifiers are
     *                present in the returned Object.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Object. If false, no CLASSORIGIN attributes are present
     *                in each returned Object. The CLASSORIGIN attribute is
     *                defined in the DMTF's Specification for the
     *                Representation of CIM in XML. CLASSORIGIN is an XML tag
     *                identifying the following text as a class name. It is
     *                attached to a property or method (when specified in XML),
     *                to indicate the class where that property or method is
     *                first defined. Where the same property name is locally
     *                defined in another superclass or subclass, the Server
     *                will return the value for the property in the lowest
     *                subclass.
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                Each returned Object MUST NOT include elements for any
     *                Properties missing from this list. Note that if LocalOnly
     *                is specified as true this acts as an additional filter on
     *                the set of Properties returned (for example, if Property
     *                A is included in the PropertyList but LocalOnly is set to
     *                true and A is not local to a returned class, then it will
     *                not be included in that class). If the PropertyList input
     *                parameter is an empty array this signifies that no
     *                Properties are included in each returned Object. If the
     *                PropertyList input parameter is NULL this specifies that
     *                all Properties (subject to the conditions expressed by
     *                the other parameters) are included in each returned
     *                Object. If the PropertyList contains duplicate elements,
     *                the Server ignores the duplicates but otherwise process
     *                the request normally. If the PropertyList contains
     *                elements which are invalid Property names for any target
     *                Object, the Server ignores such entries but otherwise
     *                process the request normally.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     * @see CIMClient#enumerateClasses(CIMObjectPath, boolean, boolean, boolean)
     * @see CIMClient#enumerateClassNames(CIMObjectPath, boolean)
     */
    public synchronized int getClass(CIMObjectPath name, 
			      boolean localOnly,
			      boolean includeQualifiers,
			      boolean includeClassOrigin,
			      String propertyList[]) 
	    throws CIMException {

	CIMGetClassOp op = new CIMGetClassOp(name, localOnly,
			includeQualifiers, includeClassOrigin,
			propertyList);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Gets the CIM instance for the specified CIM object path.
     *
     *
     * @param name   CIM Object Path that identifies this CIM instance.  This
     * must include all of the keys.
     *
     * @param localOnly If true, only properties and qualifiers overridden or 
     * defined in the returned Instance are included in the response.
     * If false, all elements of the returned Instance are returned.
     *
     * @param includeQualifiers If true,
     * all Qualifiers for each Object (including Qualifiers
     * on the Object and on any returned Properties) MUST be included.  
     * If false no Qualifiers are present in the returned Object.
     *
     *
     * @param includeClassOrigin If true, CLASSORIGIN
     * attribute MUST be present on all appropriate elements in each returned
     * Object. If false, no CLASSORIGIN attributes are present in each returned
     * Object.
     * The CLASSORIGIN attribute is defined in the DMTF's Specification for
     * the Representation of CIM in XML.  CLASSORIGIN is an XML tag identifying
     * the following text as a class name.  It is attached to a property or
     * method (when specified in XML), to indicate the class where that
     * property or method is first defined.
     * Where the
     * same property name is locally defined in another superclass or subclass,
     * the Server will return the value for the property in the lowest subclass.        *
     *
     * @param propertyList If the PropertyList input parameter is not NULL, the 
     * members of the array define one or more Property names.  Each returned 
     * Object MUST NOT include elements for any Properties missing from this 
     * list.  Note that if LocalOnly is specified as true 
     * this acts as an additional filter on the set of 
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
     *
     * @return the Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     * @see CIMClient#enumerateInstances(CIMObjectPath, boolean, boolean, boolean, boolean)
     * @see CIMClient#enumerateInstanceNames(CIMObjectPath)
     */
    public synchronized int getInstance(CIMObjectPath name, 
				    boolean localOnly,
				    boolean includeQualifiers,
				    boolean includeClassOrigin,
				    String propertyList[]) 
	    throws CIMException {

	CIMGetInstanceOp op = new CIMGetInstanceOp(name, localOnly,
				includeQualifiers, includeClassOrigin,
				propertyList);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Executes the specified method on the specified object. 
     *
     *
     * @param name	 CIM object path of the object whose method must
     *                   be invoked. It must include all of the keys.
     *
     * @param methodName the string name of the method to be invoked
     *
     * @param inArgs   the input parameters specified as a array of CIMArgument.
     *
     * @param outArgs  The output parameters specified as a array of CIMArgument.
     * The caller should allocate an array large enough to hold all returned
     * parameters, but should not initialize any array element.
     *
     * @return the Operation ID. Result list from the performBatch method 
     *               contains the actual return value.
     *
     */
    public synchronized int invokeMethod(CIMObjectPath name,
			    String methodName,
			    CIMArgument[] inArgs,
			    CIMArgument[] outArgs) 
	    throws CIMException {

	CIMInvokeArgsMethodOp op = new CIMInvokeArgsMethodOp(name, methodName,
				inArgs, outArgs);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Gets the CIM qualifier type specified in the CIM object path.
     * 
     * @param name CIM object path that identifies the CIM qualifier type
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int getQualifierType(CIMObjectPath name) 
	    throws CIMException {

	CIMGetQualifierTypeOp op = new CIMGetQualifierTypeOp(name);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Adds the specified CIM qualifier type to the specified namespace.
     * 
     * @param name CIM object path that identifies the CIM qualifier type
     * @param qt the CIM qualifier type to be added
     * @return the Operation ID.
     */
    public int createQualifierType(CIMObjectPath name,
					     CIMQualifierType qt) 
	    throws CIMException {

	CIMCreateQualifierTypeOp op = new CIMCreateQualifierTypeOp(name, qt);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }
	
   /**
    * Adds the specified CIM qualifier type to the specified namespace if it
    * does not already exist. Otherwise, it sets the value qualifier type to
    * the one passed in.
    * 
    * @param name CIM object path that identifies the CIM qualifier type
    * @param qt the CIM qualifier type to be added
    * @return the Operation ID.
    */
    public  synchronized int setQualifierType(CIMObjectPath name,
					  CIMQualifierType qt) 
	    throws CIMException {

	CIMSetQualifierTypeOp op = new CIMSetQualifierTypeOp(name, qt);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Adds the CIM class to the specified namespace.
     * 
     * @param name CIM object path that identifies the CIM class to be added
     * @param cc CIMClass to be added
     * @return the Operation ID.
     */
    public  int createClass(CIMObjectPath name, CIMClass cc) 
	    throws CIMException {

	CIMCreateClassOp op = new CIMCreateClassOp(name, cc);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

       
    /**
     * Modifies the CIM class in the specified namespace.
     * 
     * @param name CIM object path that identifies the CIM class to be modified
     * @param cc CIMClass to be modified
     * @return the Operation ID.
     */
    public  synchronized int setClass(CIMObjectPath name, CIMClass cc) 
	    throws CIMException {

	CIMSetClassOp op = new CIMSetClassOp(name, cc);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Invokes the object manager on this client to add the specified CIM
     * instance to the specified namespace.
     * 
     * @param name CIM object path that identifies the CIM instance to be
     *                added. Only the name space component is used. Any other
     *                information (e.g. keys) is ignored.
     * @param ci CIM instance to be added. Its keys and properties may be
     *                initialized by either the client or server.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int createInstance(CIMObjectPath name, 
    					    CIMInstance ci) 
	    throws CIMException {

	CIMCreateInstanceOp op = new CIMCreateInstanceOp(name, ci);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Modifies the specified CIM instance in the specified namespace. No
     * Qualifiers will be modified, created or removed. All Properties in the
     * specified CIM instance are updated.
     * 
     * @param name CIM object path that identifies the CIM instance to be
     *                modified. It must include all of the keys.
     * @param ci CIM instance to be modified. Its properties may be initialized
     *                by either the client or server.
     * @return the Operation ID.
     */
    public int setInstance(CIMObjectPath name, CIMInstance ci) 
	    throws CIMException {

	CIMSetInstanceOp op = new CIMSetInstanceOp(name, ci);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Modifies some or all of the properties of the specified CIM instance in
     * the specified namespace.
     * 
     * @param name CIM object path that identifies the CIM instance to be
     *                modified. It must include all of the keys.
     * @param ci CIM instance to be modified. Its properties may be initialized
     *                by either the client or server.
     * @param includeQualifiers If true, the Qualifiers are modified or created
     *                as specified in CIM Instance ci. A Qualifier is created
     *                if found in ci but not in its class definition. If the
     *                Qualifier is not found in ci, the previously created
     *                Qualifier (i.e. one not found in its class definition) is
     *                removed. If false, no Qualifiers are modified, created or
     *                removed.
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                The Provider will modify or initialize only the values
     *                for the Property names in PropertyList. The Server will
     *                not update any other Property. Order is not important.
     *                The value of the specified Property is found in
     *                CIMInstance ci. If the specified Property value is not
     *                found in ci, then its value will be reinitialized by the
     *                Server so that any previous changes to the Property in ci
     *                are lost. If PropertyList contains invalid Property
     *                names, the entire operation fails (i.e. no Property
     *                values of ci are modified). PropertyList may contain
     *                duplicate Property names. The Server must ignore them but
     *                otherwise process the request normally. If PropertyList
     *                is an empty array, then no Property values in ci are
     *                updated. If PropertyList is NULL, then ci contains all
     *                Property values to be updated by the client or Server.
     *                This argument can be used to perform the SetProperty
     *                operation as defined by the CIM operations over HTTP spec
     *                at http://www.dmtf.org/.
     * @return the Operation ID.
     */
    public int setInstance(CIMObjectPath name, CIMInstance ci, 
                           boolean includeQualifiers, String[] propertyList) 
	    throws CIMException {

	CIMSetInstanceOp op = new CIMSetInstanceOp(name, ci, includeQualifiers,
						   propertyList);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    }

    /**
     * Gets the value of the specified property for the instance specified in
     * the object path.
     * 
     * @param name CIM object path that identifies the CIM instance to be
     *                accessed. It must include all of the keys.
     * @param propertyName Property whose value is to be returned.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public int getProperty(CIMObjectPath name, String propertyName) 
	    throws CIMException {

	CIMGetPropertyOp op = new CIMGetPropertyOp(name, propertyName);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Modifies the value of the specified property for the instance specified
     * in the object path.
     * 
     * @param name CIM object path that identifies the CIM instance to be
     *                accessed. It must include all of the keys.
     * @param propertyName Property whose value is to be set.
     * @param newValue The value for property propertyName.
     * @return the Operation ID.
     */
    public synchronized int setProperty(CIMObjectPath name, 
				String propertyName, 
				CIMValue newValue) 
	    throws CIMException {

	CIMSetPropertyOp op = new CIMSetPropertyOp(name, propertyName, 
						   newValue);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Modifies the value of the specified property for the instance specified
     * in the object path. The property value is set to NULL or initialized by
     * the Server.
     * 
     * @param name CIM object path that identifies the CIM instance to be
     *                accessed. It must include all of the keys.
     * @param propertyName Property whose value is to be set.
     * @return the Operation ID.
     */
    public int setProperty(CIMObjectPath name, 
				String propertyName) throws CIMException {
	return setProperty(name, propertyName, null);
    }

    /**
     * Executes a query to retrieve objects. If none are found, then NULL is
     * returned. The WBEM Query Language is a subset of standard American
     * National Standards Institute Structured Query Language (ANSI SQL) with
     * semantic changes to support WBEM. Unlike SQL, in this release, it is a
     * retrieval-only language. You cannot use the WBEM Query Language to
     * modify, insert, or delete information. NOTE: This method operates as if
     * localOnly is false, includeQualifiers is true, includeClassOrigin is
     * true and propertyList is false; i.e. all qualifiers and all local and
     * inherited properties with CLASSORIGIN attributes of each instance are
     * returned.
     * <p>
     * Only queries on class instances are supported.
     * 
     * @param name CIMObjectPath that identifies the class in which to query.
     *                Only the name space and class name components are used.
     *                Any other information (e.g. keys) is ignored.
     * @param query A string containing the text of the query. This parameter
     *                cannot be null.
     * @param ql String that identifies the query language to use for parsing
     *                the query string (e.g. WQL). WQL level 1 is the only
     *                currently supported query language.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     *         <ul>
     *         <li>The user does not have permission to view the result.
     *         <li>The requested query language is not supported.
     *         <li>The query specifies a class that does not exist.
     *         </ul>
     *         <p>
     *         <b>Example:</b>
     *         <p>
     *         The following API call returns an enumeration of all instances
     *         of the <code>device_class</code>.
     *         <p>
     *         <code>cc.execQuery(new CIMObjectPath(),
     *                    SELECT * FROM device_class, cc.WQL)</code>
     */
    public synchronized int execQuery(CIMObjectPath name, 
				  String query,
				  String ql) 
	    throws CIMException {
	
	CIMExecQueryOp op = new CIMExecQueryOp(name, query, ql);
	CIMNameSpace cns = new CIMNameSpace("", name.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 
	

    /**
     * Enumerate CIM Objects that are associated to a particular source CIM
     * Object. If the source CIM object is a class, then associated class names
     * are returned. If the target CIM object is an instance, then entire
     * instances of associated classes are returned.
     * 
     * @param objectName - Defines the source CIM Object whose associated
     *                Objects are to be returned. This may be either a Class
     *                name or Instance name (modelpath).
     * @param assocClass The AssocClass input parameter, if not NULL, MUST be a
     *                valid CIM Association Class name. It acts as a filter on
     *                the returned set of Objects by mandating that each
     *                returned Object MUST be associated to the source Object
     *                via an Instance of this Class or one of its subclasses.
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *                a valid CIM Class name. It acts as a filter on the
     *                returned set of Objects by mandating that each returned
     *                Object MUST be either an Instance of this Class (or one
     *                of its subclasses) or be this Class (or one of its
     *                subclasses).
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Objects by mandating that each returned Object MUST be
     *                associated to the source Object via an Association in
     *                which the source Object plays the specified role (i.e.
     *                the name of the Property in the Association Class that
     *                refers to the source Object MUST match the value of this
     *                parameter). Thus, if the Role input parameter is set to
     *                Antecedent, then only Associations where the source
     *                Object is the Antecedent reference are examined.
     * @param resultRole The ResultRole input parameter, if not NULL, MUST be a
     *                valid Property name. It acts as a filter on the returned
     *                set of Objects by mandating that each returned Object
     *                MUST be associated to the source Object via an
     *                Association in which the returned Object plays the
     *                specified role (i.e. the name of the Property in the
     *                Association Class that refers to the returned Object MUST
     *                match the value of this parameter). Thus, if the
     *                resultRole input parameter is set to Dependent, then only
     *                Associations where the returned Object is the Dependent
     *                reference are examined.
     * @param includeQualifiers If true, all Qualifiers for each Object
     *                (including Qualifiers on the Object and on any returned
     *                Properties) MUST be included in the response. If false,
     *                no Qualifiers are present in each returned Object.
     * @param includeClassOrigin If true, this specifies that the CLASSORIGIN
     *                attribute MUST be present on all appropriate elements in
     *                each returned Object. If false, no CLASSORIGIN attributes
     *                are present in each returned Object. The CLASSORIGIN
     *                attribute is defined in the DMTF's Specification for the
     *                Representation of CIM in XML. CLASSORIGIN is an XML tag
     *                identifying the following text as a class name. It is
     *                attached to a property or method (when specified in XML),
     *                to indicate the class where that property or method is
     *                first defined. Where the same property name is locally
     *                defined in another superclass or subclass, the Server
     *                will return the value for the property in the lowest
     *                subclass. *
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                Each returned Object MUST NOT include elements for any
     *                Properties missing from this list. Note that if LocalOnly
     *                is specified as true this acts as an additional filter on
     *                the set of Properties returned (for example, if Property
     *                A is included in the PropertyList but LocalOnly is set to
     *                true and A is not local to a returned Instance, then it
     *                will not be included in that Instance). If the
     *                PropertyList input parameter is an empty array this
     *                signifies that no Properties are included in each
     *                returned Object. If the PropertyList input parameter is
     *                NULL this specifies that all Properties (subject to the
     *                conditions expressed by the other parameters) are
     *                included in each returned Object. If the PropertyList
     *                contains duplicate elements, the Server ignores the
     *                duplicates but otherwise process the request normally. If
     *                the PropertyList contains elements which are invalid
     *                Property names for any target Object, the Server ignores
     *                such entries but otherwise process the request normally.
     *                Clients SHOULD NOT explicitly specify properties in the
     *                PropertyList parameter unless they have specified a
     *                non-NULL value for the ResultClass parameter.
     * @return the Operation ID. Result array from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int associators(CIMObjectPath objectName,
				    String assocClass,
				    String resultClass,
				    String role,
				    String resultRole,
				    boolean includeQualifiers,
				    boolean includeClassOrigin,
				    String propertyList[]) 
	    throws CIMException {

	CIMAssociatorsOp op = new CIMAssociatorsOp(objectName, assocClass,
			resultClass, role, resultRole, includeQualifiers,
			includeClassOrigin, propertyList);
	CIMNameSpace cns = new CIMNameSpace("", objectName.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Enumerate CIM Objects that are associated to a particular source CIM
     * Object. If the source CIM object is a class, then associated class names
     * are returned. If the target CIM object is an instance, then entire
     * instances of associated classes are returned. This method behaves as if
     * assocClass=null, resultClass=null, role=null, resultRole=null,
     * propertyList=null, includeQualifiers=true, includeClassOrigin=true. All
     * properties of each instance will be returned. Qualifiers will not be
     * present in each returned instance. The CLASSORIGIN attribute will not be
     * present on each returned instance. No instance filtering is performed.
     * 
     * @param objectName - Defines the source CIM Object whose associated
     *                Objects are to be returned. This may be either a Class
     *                name or Instance name (modelpath).
     * @return the Operation ID. Result array from the performBatch method
     *         contains the actual return value.
     */
    public int associators(CIMObjectPath objectName) throws CIMException {
	return associators(objectName, null, null, null, null, true, true, 
	null);
    }

    /**
     * Enumerates the object paths to CIM Objects that are associated to a
     * particular source CIM Object. If the source CIM object is a class, then
     * associated class names are returned. If the target CIM object is an
     * instance, then object paths to the instances of associated classes are
     * returned.
     * 
     * @param objectName - Defines the source CIM Object whose associated
     *                Objects are to be returned. This may be either a Class
     *                name or Instance name (modelpath).
     * @param assocClass The AssocClass input parameter, if not NULL, MUST be a
     *                valid CIM Association Class name. It acts as a filter on
     *                the returned set of Objects by mandating that each
     *                returned Object MUST be associated to the source Object
     *                via an Instance of this Class or one of its subclasses.
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *                a valid CIM Class name. It acts as a filter on the
     *                returned set of Objects by mandating that each returned
     *                Object MUST be either an Instance of this Class (or one
     *                of its subclasses) or be this Class (or one of its
     *                subclasses).
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Objects by mandating that each returned Object MUST be
     *                associated to the source Object via an Association in
     *                which the source Object plays the specified role (i.e.
     *                the name of the Property in the Association Class that
     *                refers to the source Object MUST match the value of this
     *                parameter). Thus, if the Role input parameter is set to
     *                Antecedent, then only Associations where the source
     *                Object is the Antecedent reference are examined.
     * @param resultRole The ResultRole input parameter, if not NULL, MUST be a
     *                valid Property name. It acts as a filter on the returned
     *                set of Objects by mandating that each returned Object
     *                MUST be associated to the source Object via an
     *                Association in which the returned Object plays the
     *                specified role (i.e. the name of the Property in the
     *                Association Class that refers to the returned Object MUST
     *                match the value of this parameter). Thus, if the
     *                resultRole input parameter is set to Dependent, then only
     *                Associations where the returned Object is the Dependent
     *                reference are examined.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int associatorNames(CIMObjectPath objectName,
				    String assocClass,
				    String resultClass,
				    String role,
				    String resultRole) 
	    throws CIMException {

	CIMAssociatorNamesOp op = 
	    new CIMAssociatorNamesOp(objectName, assocClass, resultClass, 
				     role, resultRole);
	CIMNameSpace cns = new CIMNameSpace("", objectName.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Enumerates the object paths to CIM Objects that are associated to a
     * particular source CIM Object. If the source CIM object is a class, then
     * associated class names are returned. If the target CIM object is an
     * instance, then object paths to the instances of associated classes are
     * returned. This method behaves as if assocClass=null, resultClass=null,
     * role=null, resultRole=null. No instance filtering is performed
     * 
     * @param objectName - Defines the source CIM Object whose associated
     *                Objects are to be returned. This may be either a Class
     *                name or Instance name (modelpath).
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public int associatorNames(CIMObjectPath objectName) throws CIMException {
	return associatorNames(objectName, null, null, null, null);
    }

    /**
     * Enumerates the association objects that refer to a particular target CIM
     * Object. If the target CIM object is a class, association class names are
     * returned. If the target CIM object is an instance, then association
     * instances are returned.
     * 
     * @param objectName The ObjectName input parameter defines the target CIM
     *                Object whose referring Objects are to be returned. This
     *                is either a Class name or Instance name (model path).
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *                a valid CIM Class name. It acts as a filter on the
     *                returned set of Objects by mandating that each returned
     *                Object MUST be an Instance of this Class (or one of its
     *                subclasses), or this Class (or one of its subclasses).
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Objects by mandating that each returned Objects MUST
     *                refer to the target Object via a Property whose name
     *                matches the value of this parameter. Thus, if the Role
     *                input parameter is set to Antecedent, then only
     *                Association instances where the target Object is the
     *                Antecedent reference are returned.
     * @param includeQualifiers If true, all Qualifiers for each Object
     *                (including Qualifiers on the Object and on any returned
     *                Properties) MUST be included in the response. If false,
     *                no Qualifiers are present in each returned Object.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Object. If false, no CLASSORIGIN attributes are present
     *                in each returned Object. The CLASSORIGIN attribute is
     *                defined in the DMTF's Specification for the
     *                Representation of CIM in XML. CLASSORIGIN is an XML tag
     *                identifying the following text as a class name. It is
     *                attached to a property or method (when specified in XML),
     *                to indicate the class where that property or method is
     *                first defined. Where the same property name is locally
     *                defined in another superclass or subclass, the Server
     *                will return the value for the property in the lowest
     *                subclass. *
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                Each returned Object MUST NOT include elements for any
     *                Properties missing from this list. Note that if LocalOnly
     *                is specified as true this acts as an additional filter on
     *                the set of Properties returned (for example, if Property
     *                A is included in the PropertyList but LocalOnly is set to
     *                true and A is not local to a returned Instance, then it
     *                will not be included in that Instance). If the
     *                PropertyList input parameter is an empty array this
     *                signifies that no Properties are included in each
     *                returned Object. If the PropertyList input parameter is
     *                NULL this specifies that all Properties (subject to the
     *                conditions expressed by the other parameters) are
     *                included in each returned Object. If the PropertyList
     *                contains duplicate elements, the Server ignores the
     *                duplicates but otherwise process the request normally. If
     *                the PropertyList contains elements which are invalid
     *                Property names for any target Object, the Server ignores
     *                such entries but otherwise process the request normally.
     *                Clients SHOULD NOT explicitly specify properties in the
     *                PropertyList parameter unless they have specified a
     *                non-NULL value for the ResultClass parameter.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int references(CIMObjectPath objectName,
				    String resultClass,
				    String role,
				    boolean includeQualifiers,
				    boolean includeClassOrigin,
				    String propertyList[]) 
	    throws CIMException {

	CIMReferencesOp op = 
	    new CIMReferencesOp(objectName, resultClass, role, 
				includeQualifiers, includeClassOrigin,
				propertyList);
	CIMNameSpace cns = new CIMNameSpace("", objectName.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

	return index++;
    } 

    /**
     * Enumerates the association objects that refer to a particular target CIM
     * Object. If the target CIM object is a class, association class names are
     * returned. If the target CIM object is an instance, then association
     * instances are returned. This method behaves as if resultClass=null,
     * role=null, resultRole=null, propertyList=null, includeQualifiers=false,
     * includeClassOrigin=false, propertyList=null. All properties of each
     * instance will be returned. Qualifiers will not be present in each
     * returned instance. The CLASSORIGIN attribute will not be present on each
     * returned instance. No instance filtering is performed
     * 
     * @param objectName The ObjectName input parameter defines the target CIM
     *                Object whose referring Objects are to be returned. This
     *                is either a Class name or Instance name (model path).
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */

    public int references(CIMObjectPath objectName) throws CIMException {
	return references(objectName, null, null, true, true, null);
    }

    /**
     * Enumerates the object paths to association objects that refer to a
     * particular target CIM Object. If the target CIM object is a class,
     * association class names are returned. If the target CIM object is an
     * instance, then object paths to the association instances are returned.
     * 
     * @param objectName The ObjectName input parameter defines the target CIM
     *                Object whose referring Objects are to be returned. This
     *                is either a Class name or Instance name (model path).
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *                a valid CIM Class name. It acts as a filter on the
     *                returned set of Objects by mandating that each returned
     *                Object MUST be an Instance of this Class (or one of its
     *                subclasses), or this Class (or one of its subclasses).
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Objects by mandating that each returned Objects MUST
     *                refer to the target Object via a Property whose name
     *                matches the value of this parameter. Thus, if the Role
     *                input parameter is set to Antecedent, then only
     *                Association instances where the target Object is the
     *                Antecedent reference are returned.
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public synchronized int referenceNames(CIMObjectPath objectName,
				    String resultClass,
				    String role) 
	    throws CIMException {

	CIMReferenceNamesOp op = new CIMReferenceNamesOp(objectName,
							 resultClass, role);
	CIMNameSpace cns = new CIMNameSpace("", objectName.getNameSpace());
	op.setTargetNS(cns);
	opList.add(op);

        return index++;
    } 

    /**
     * Enumerates the object paths to association objects that refer to a
     * particular target CIM Object. If the target CIM object is a class,
     * association class names are returned. If the target CIM object is an
     * instance, then object paths to the association instances are returned.
     * This method behaves as if resultClass and role are null. That is, no
     * filtering is performed on the returned Objects.
     * 
     * @param objectName The ObjectName input parameter defines the target CIM
     *                Object whose referring Objects are to be returned. This
     *                is either a Class name or Instance name (model path).
     * @return the Operation ID. Result list from the performBatch method
     *         contains the actual return value.
     */
    public int referenceNames(CIMObjectPath objectName) throws CIMException {
	return referenceNames(objectName, null, null);
    }

    /**
     * Returns an array of CIMOperation objects each member of which represents
     * an operation that the entity invoking it wants to execute in batch mode.
     * 
     * @return Array of CIMOperation objects
     */
    protected synchronized CIMOperation[] getOperationList() 
	    throws CIMException {
    	
	CIMOperation opArray[] = new CIMOperation[opList.size()];
	opArray = (CIMOperation[])opList.toArray(opArray);
	return opArray;
    }
}

