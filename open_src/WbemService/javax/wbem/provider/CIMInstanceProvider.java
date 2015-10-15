/*
 * EXHIBIT A - Sun Industry Standards Source License "The contents of this file
 * are subject to the Sun Industry Standards Source License Version 1.2 (the
 * "License"); You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://wbemservices.sourceforge.net/license.html Software distributed under
 * the License is distributed on an "AS IS" basis, WITHOUT WARRANTY OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing rights and limitations under the License. The Original Code is
 * WBEM Services. The Initial Developer of the Original Code is: Sun
 * Microsystems, Inc. Portions created by: Sun Microsystems, Inc. are Copyright (c)
 * 2001 Sun Microsystems, Inc. All Rights Reserved. 
 * Contributor(s):WBEM Solutions, Inc.
 */

package javax.wbem.provider;

import javax.wbem.cim.CIMClass;
import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;

/**
 * This is the interface implemented by instance providers. Instance providers
 * are used to serve up dynamic instances of classes.
 * 
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public interface CIMInstanceProvider extends CIMProvider {

    /**
     * This method enumerates all of object paths to the instances of the class
     * which is specified by the CIMObjectPath argument. Just the object paths
     * to the instances are returned, not the entire instances. This method is
     * invoked in order to perform the EnumerateInstanceNames operation as
     * defined by the CIM operations over HTTP spec at http://www.dmtf.org/.
     * 
     * @param op The object path of the class to be enumerated. Only the name
     *                space and class name components are used. Any other
     *                information (e.g. keys) is ignored.
     * @param cc The CIM class object
     * @return An array of CIMObjectPaths. Each element is an object path to an
     *         enumerated instance. If none found, then the Provider returns
     *         NULL.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_INVALID_CLASS (in this namespace)</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMObjectPath[] enumerateInstanceNames(
        CIMObjectPath op,
        CIMClass cc)
        throws CIMException;

    /**
     * This method enumerates all instances of the class specified by the
     * CIMObjectPath argument. The entire instances and not just the object
     * paths to them are returned. This method is invoked in order to perform
     * the EnumerateInstances operation as defined by the CIM operations over
     * HTTP spec at http://www.dmtf.org/.
     * 
     * @param op The object path of the class to be enumerated. Only the name
     *                space and class name components are used. Any other
     *                information (e.g. keys) is ignored.
     * @param localOnly If true, only properties and qualifiers overridden or
     *                defined in the returned Instance are included in the
     *                response. If false, all elements of the returned Instance
     *                are returned.
     * @param includeQualifiers If true, all Qualifiers for each Instance
     *                (including Qualifiers on the Instance and on any returned
     *                Properties) MUST be included as elements in the response.
     *                If false, no Qualifier elements are present in each
     *                returned Instance.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Instance. If false, no CLASSORIGIN attributes are present
     *                in each returned Instance. It is attached to a property
     *                to indicate the class where that property is first
     *                defined.
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                Each returned Instance MUST NOT include elements for any
     *                Properties missing from this list. Note that if LocalOnly
     *                is specified as true this acts as an additional filter on
     *                the set of Properties returned (e.g. if Property A is
     *                included in the PropertyList but LocalOnly is set to true
     *                and A is not local to a returned Instance, then it will
     *                not be included in that Instance). If the PropertyList
     *                input parameter is an empty array this signifies that no
     *                Properties are included in each returned Instance. If the
     *                PropertyList input parameter is NULL this specifies that
     *                all Properties (subject to the conditions expressed by
     *                the other parameters) are included in each returned
     *                Instance. The PropertyList may contain duplicate or
     *                invalid Property names. The Provider must ignore them but
     *                otherwise process the request normally; i.e. the returned
     *                Instance(s) will not contain duplicate or invalid
     *                Property Names.
     * @param cc The CIM class object
     * @return An array of CIMInstances. Each element is the actual filtered
     *         instance. The Provider must ensure that no duplicate instances
     *         are returned. If none found, then the Provider returns NULL.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_INVALID_CLASS (in this namespace)</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMInstance[] enumerateInstances(CIMObjectPath op, 
        boolean localOnly, boolean includeQualifiers, 
        boolean includeClassOrigin, String[] propertyList, CIMClass cc) throws CIMException;

    /**
     * This method retrieves the instance specified by the CIMObjectPath
     * argument. This method is invoked in order to perform the GetInstance
     * operation as defined by the CIM operations over HTTP spec at
     * http://www.dmtf.org/. It can also be used to perform the GetProperty
     * operation.
     * 
     * @param op The object path of the instance to be retrieved. This must
     *                include all of the keys for the instance.
     * @param localOnly If true, only properties and qualifiers overridden or
     *                defined in the returned Instance are included in the
     *                response. If false, all elements of the returned Instance
     *                are returned.
     * @param includeQualifiers If true, all Qualifiers for each Instance
     *                (including Qualifiers on the Instance and on any returned
     *                Properties) MUST be included as elements in the response.
     *                If false, no Qualifier elements are present in each
     *                returned Instance.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Instance. If false, no CLASSORIGIN attributes are present
     *                in each returned Instance. It is attached to a property
     *                to indicate the class where that property is first
     *                defined.
     * @param propertyList If the PropertyList input parameter is not NULL, the
     *                members of the array define one or more Property names.
     *                Each returned Instance MUST NOT include elements for any
     *                Properties missing from this list. Note that if LocalOnly
     *                is specified as true this acts as an additional filter on
     *                the set of Properties returned (e.g. if Property A is
     *                included in the PropertyList but LocalOnly is set to true
     *                and A is not local to a returned Instance, then it will
     *                not be included in that Instance). If the PropertyList
     *                input parameter is an empty array this signifies that no
     *                Properties are included in each returned Instance. If the
     *                PropertyList input parameter is NULL this specifies that
     *                all Properties (subject to the conditions expressed by
     *                the other parameters) are included in each returned
     *                Instance. The PropertyList may contain duplicate or
     *                invalid Property names. The Provider must ignore them but
     *                otherwise process the request normally; i.e. the returned
     *                Instance(s) will not contain duplicate or invalid
     *                Property Names. This argument can be used to perform the
     *                GetProperty operation as defined by the CIM operations
     *                over HTTP spec at http://www.dmtf.org/.
     * @param cc The CIM class object
     * @return The retrieved filtered instance.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_INVALID_CLASS (in this namespace)</li>
     *                    <li>CIM_ERR_NOT_FOUND (if instance does not exist)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMInstance getInstance(
        CIMObjectPath op,
        boolean localOnly,
        boolean includeQualifiers,
        boolean includeClassOrigin,
        String[] propertyList,
        CIMClass cc)
        throws CIMException;

    /**
     * This method creates the instance specified by the CIMInstance argument
     * in the namespace specified by the CIMObjectPath argument. This method is
     * invoked in order to perform the CreateInstance operation as defined by
     * the CIM operations over HTTP spec at http://www.dmtf.org/.
     * 
     * @param op The object path of the instance to be created. Only the name
     *                space component is used. Any other information (e.g.
     *                keys) is ignored.
     * @param ci The instance to be created. Its keys and properties may be
     *                initialized by either the client or provider.
     * @return the CIMObjectPath of the created instance. The provider should
     *         return non-NULL ONLY IF it initializes or changes one or more
     *         keys.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_INVALID_CLASS (in this namespace)</li>
     *                    <li>CIM_ERR_ALREADY_EXISTS</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMObjectPath createInstance(CIMObjectPath op, CIMInstance ci)
        throws CIMException;

    /**
     * This method modifies some or all of the properties of the instance
     * specified by the CIMInstance argument in the namespace specified by the
     * CIMObjectPath argument. This method is invoked in order to perform the
     * ModifyInstance operation as defined by the CIM operations over HTTP spec
     * at http://www.dmtf.org/. It can also be used to perform the SetProperty
     * operation.
     * 
     * @param op The object path of the instance to be set. It must include all
     *                of the keys.
     * @param ci The instance to be set. Its properties may be initialized by
     *                either the client or Provider.
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
     *                for the Property names in PropertyList. The Provider will
     *                not affect any other Property. Order is not important.
     *                The value of the specified Property is found in
     *                CIMInstance ci. If the specified Property value is not
     *                found in ci, then it will be ignored by the Provider. If
     *                PropertyList contains invalid Property names, the entire
     *                operation fails (i.e. no Property values of ci are
     *                modified). PropertyList may contain duplicate Property
     *                names. The Provider must ignore them but otherwise
     *                process the request normally. If PropertyList is an empty
     *                array, then no Property values in ci are updated. If
     *                PropertyList is NULL, then ci contains all Property
     *                values to be updated by the client or Provider. This
     *                argument can be used to perform the SetProperty operation
     *                as defined by the CIM operations over HTTP spec at
     *                http://www.dmtf.org/.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_INVALID_CLASS (in this namespace)</li>
     *                    <li>CIM_ERR_NOT_FOUND (if instance does not exist)
     *                    </li>
     *                    <li>CIM_ERR_NO_SUCH_PROPERTY (in this instance)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public void setInstance(
        CIMObjectPath op,
        CIMInstance ci,
        boolean includeQualifiers,
        String[] propertyList)
        throws CIMException;

    /**
     * This method deletes the instance specified by the CIMObjectPath
     * argument. This method is invoked in order to perform the DeleteInstance
     * operation as defined by the CIM operations over HTTP spec at
     * http://www.dmtf.org/.
     * 
     * @param op The object path of the instance to be deleted. It must include
     *                all of the keys.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_INVALID_CLASS (in this namespace)</li>
     *                    <li>CIM_ERR_NOT_FOUND (if instance does not exist)
     *                    </li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public void deleteInstance(CIMObjectPath op) throws CIMException;

    /**
     * This method enumerates all instances of the class specified by the
     * CIMObjectpath argument that satisfy the conditions defined by the query
     * expression (query) in query language type (ql). The entire instances are
     * returned, not just the object paths to them. NOTE: This method operates
     * as if localOnly is false, includeQualifiers is true, includeClassOrigin
     * is true and propertyList is NULL; i.e. all qualifiers and all local and
     * inherited properties with CLASSORIGIN attributes of each instance are
     * returned.
     * <p>
     * Typically, a provider parses the query string, enumerates the instances
     * in the class (op), and then filters those instances to match the query
     * string (query) expressed using the specified query language (ql). The
     * provider then returns an array containing the instances that match the
     * query string.
     * <p>
     * This method is invoked in order to perform the ExecQuery operation as
     * defined by the CIM operations over HTTP spec at http://www.dmtf.org/.
     * NOTE: In the CIM operations over HTTP spec at http://www.dmtf.org/,
     * ExecQuery is its own functional group. For convenience, it is included
     * as part of the Instance Provider, instead of in a separate Query
     * Provider.
     * <p>
     * Providers with access to an entity that handles indexing can pass the
     * query string to that entity for parsing. For those providers who do not
     * have any indexing/query filtering capabilites, the provider may ask the
     * CIM object manager to handle the filtering. A return array of all
     * CIMInstance which have a possibility of matching the filter may be
     * returned (this can be the same result as an enumerate instances call),
     * but with the first element of the array set to NULL. This NULL value
     * indicates to the CIM object manager that it needs to handle the
     * filtering. The filter is applied by the object manager and the filtered
     * result is returned to the client.
     * 
     * @param op The object path of the class to enumerate and apply the query
     *                to. Only the name space and class name components are
     *                used. Any other information (e.g. keys) is ignored.
     * @param query The CIM query expression (e.g. SELECT *)
     * @param ql The CIM query language type (e.g. WQL)
     * @param cc The CIM class object.
     * @return An array of CIMInstances that met the specified criteria. Each
     *         element is an actual instance. The Provider must ensure that no
     *         duplicate instances are returned. If none found, then the
     *         Provider returns NULL.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED (provider does not support
     *                    this method)</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (for this method)
     *                    </li>
     *                    <li>CIM_ERR_QUERY_LANGUAGE_NOT_SUPPORTED</li>
     *                    <li>CIM_ERR_INVALID_QUERY</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMInstance[] execQuery(
        CIMObjectPath op,
        String query,
        String ql,
        CIMClass cc)
        throws CIMException;
}
