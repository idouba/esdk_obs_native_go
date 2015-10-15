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

package javax.wbem.provider;

import javax.wbem.cim.CIMException;
import javax.wbem.cim.CIMInstance;
import javax.wbem.cim.CIMObjectPath;

/**
 * This interface is implemented by providers of dynamic association classes.
 * The CIMOM invokes these methods when it performs association traversal.
 * NOTE: The CIM operations over HTTP spec at http://www.dmtf.org specify that
 * the CIM Object can either be a Class or Instance. If a Class, the operation
 * is solely performed by the CIMOM. If an Instance, then the CIMOM invokes
 * these provider methods. Therefore, this interface only operates on CIM
 * Instances.
 * 
 * @author Sun Microsystems, Inc.
 * @since WBEM 1.0
 */
public interface CIMAssociatorProvider extends CIMProvider {

    /**
     * This method is used to enumerate CIM Instances that are associated to a
     * particular source CIM Instance. The entire instances associated to the
     * specified instance are returned. This method is invoked in order to
     * perform the Associators operation as defined by the CIM operations over
     * HTTP spec at http://www.dmtf.org/
     * 
     * @param assocName Defines the association that the objectName Object
     *                should be associated to. The provider uses this
     *                information to identify which association must be
     *                traversed in case it supports more than one association.
     * @param objectName Defines the source CIM Object whose associated Objects
     *                are to be returned. This is an instance name (modelpath).
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *                a valid CIM Class name. It acts as a filter on the
     *                returned set of Objects by mandating that each returned
     *                Object MUST be an Instance of this Class or one of its
     *                subclasses.
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Objects by mandating that each returned Object MUST be
     *                associated to the source Object via an Association in
     *                which the source Object plays the specified role (i.e.
     *                the name of the Property in the Association Class that
     *                refers to the source Object MUST match the value of this
     *                parameter). Thus, if the Role input parameter is set to
     *                Antecedent, then only Associations where the source
     *                Object is the Antecedent reference are examined. NOTE:
     *                The source Object is the objectName input parameter.
     * @param resultRole The resultRole input parameter, if not NULL, MUST be a
     *                valid Property name. It acts as a filter on the returned
     *                set of Objects by mandating that each returned Object
     *                MUST be associated to the source Object via an
     *                Association in which the returned Object plays the
     *                specified role (i.e. the name of the Property in the
     *                Association Class that refers to the returned Object MUST
     *                match the value of this parameter). Thus, if the
     *                resultRole input parameter is set to Dependent, then only
     *                Associations where the returned Object is the Dependent
     *                reference are examined. NOTE: The source Object is the
     *                objectName input parameter.
     * @param includeQualifiers If true, all Qualifiers for each Object
     *                (including Qualifiers on the Object and on any returned
     *                Properties) MUST be included in the response. If false,
     *                no Qualifiers are present in each returned Object.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Object. If false, no CLASSORIGIN attributes are present
     *                in each returned Object. It is attached to a property to
     *                indicate the class where that property is first defined.
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
     *                Property Names. Clients SHOULD NOT explicitly specify
     *                properties in the PropertyList parameter unless they have
     *                specified a non-NULL value for the ResultClass parameter.
     * @return If successful, the method returns the CIM Instances meeting the
     *         requested criteria. If no Instances are found, then the Provider
     *         returns NULL.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (including missing,
     *                    duplicate, unrecognized or otherwise incorrect
     *                    parameters)</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMInstance[] associators(CIMObjectPath assocName,
				CIMObjectPath objectName,
				String resultClass,
				String role,
				String resultRole,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String[] propertyList) throws CIMException;

    /**
     * This method is used to enumerate the names of CIM Instances that are
     * associated to a particular source CIM Instance. The object paths to the
     * instances associated to the specified instance are returned. This method
     * is invoked in order to perform the AssociatorNames operation as defined
     * by the CIM operations over HTTP spec at http://www.dmtf.org/
     * 
     * @param assocName Defines the association that the objectName Object
     *                should be associated to. The provider uses this
     *                information to identify which association must be
     *                traversed in case it supports more than one association.
     * @param objectName Defines the source CIM Object whose associated Objects
     *                are to be returned. This is an Instance name (modelpath).
     * @param resultClass The ResultClass input parameter, if not NULL, MUST be
     *                a valid CIM Class name. It acts as a filter on the
     *                returned set of Objects by mandating that each returned
     *                Object MUST be an object path to each Instance of this
     *                Class or one of its subclasses.
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Objects by mandating that each returned Object MUST be
     *                associated to the source Object via an Association in
     *                which the source Object plays the specified role (i.e.
     *                the name of the Property in the Association Class that
     *                refers to the source Object MUST match the value of this
     *                parameter). Thus, if the Role input parameter is set to
     *                Antecedent, then only Associations where the source
     *                Object is the Antecedent reference are examined. NOTE:
     *                The source Object is the objectName input parameter.
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
     *                reference are examined. NOTE: The source Object is the
     *                objectName input parameter.
     * @return If successful, the method returns the CIMObjectPaths to the
     *         Instances meeting the requested criteria. If no Instances are
     *         found, then the Provider returns NULL.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (including missing,
     *                    duplicate, unrecognized or otherwise incorrect
     *                    parameters)</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMObjectPath[] associatorNames(CIMObjectPath assocName,
					   CIMObjectPath objectName,
					   String resultClass,
					   String role,
					   String resultRole) 
    throws CIMException;

    /**
     * This method is used to enumerate the association objects that refer to a
     * particular target CIM Instance. Entire association instances are
     * returned. This method is invoked in order to perform the References
     * operation as defined by the CIM operations over HTTP spec at
     * http://www.dmtf.org/
     * 
     * @param assocName Defines the association that the objectName Object
     *                should be associated to. The provider uses this
     *                information to identify which association must be
     *                traversed in case it supports more than one association.
     * @param objectName Defines the source CIM Object whose associated Objects
     *                are to be returned. This is an Instance name (modelpath).
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Association Instances by mandating that each returned
     *                Association Instance MUST refer to the target Instance in
     *                which the target Instance plays the specified role (i.e.
     *                the name of the Property in the Association Instance that
     *                refers to the target Instance MUST match the value of
     *                this parameter). Thus, if Role input parameter is set to
     *                Antecedent, only Association Instances where target
     *                Instance is the Antecedent reference are returned. NOTE:
     *                The target Instance is the objectName input parameter.
     * @param includeQualifiers If true, all Qualifiers for each Object
     *                (including Qualifiers on the Object and on any returned
     *                Properties) MUST be included in the response. If false,
     *                no Qualifiers are present in each returned Object.
     * @param includeClassOrigin If true, the CLASSORIGIN attribute MUST be
     *                present on all appropriate elements in each returned
     *                Object. If false, no CLASSORIGIN attributes are present
     *                in each returned Object. It is attached to a property to
     *                indicate the class where that property is first defined.
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
     *                Property Names. Clients SHOULD NOT explicitly specify
     *                properties in the PropertyList parameter unless they have
     *                specified a non-NULL value for the ResultClass parameter.
     * @return If successful, the method returns the CIM Instances meeting the
     *         requested criteria. If no Instances are found, then the provider
     *         returns NULL. NOTE: Unless the Association Instance contains
     *         non-reference Properties, references effectively returns the
     *         same information as referenceNames.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (including missing,
     *                    duplicate, unrecognized or otherwise incorrect
     *                    parameters)</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */

    public CIMInstance[] references(CIMObjectPath assocName,
				CIMObjectPath objectName,
				String role,
				boolean includeQualifiers,
				boolean includeClassOrigin,
				String[] propertyList) throws CIMException;

    /**
     * This method is used to enumerate the association objects that refer to a
     * particular target CIM Instance. The object paths to association
     * instances are returned. This method is invoked in order to perform the
     * ReferenceNames operation as defined by the CIM operations over HTTP spec
     * at http://www.dmtf.org/
     * 
     * @param assocName Defines the association that the objectName object
     *                should be associated to. The provider uses this
     *                information to identify which association must be
     *                traversed in case it supports more than one association.
     * @param objectName Defines the target CIM Object whose associated Objects
     *                are to be returned. This is an Instance name (modelpath).
     * @param role The Role input parameter, if not NULL, MUST be a valid
     *                Property name. It acts as a filter on the returned set of
     *                Association Instances by mandating that each returned
     *                Association Instance MUST refer to the target Instance in
     *                which the target Instance plays the specified role (i.e.
     *                the name of the Property in the Association Instance that
     *                refers to the target Instance MUST match the value of
     *                this parameter). Thus, if Role input parameter is set to
     *                Antecedent, only Association Instances where target
     *                Instance is the Antecedent reference are returned. NOTE:
     *                The target Instance is the objectName input parameter.
     * @return If successful, the method returns the object paths to the CIM
     *         Instances meeting the requested criteria. If no Instances are
     *         found, then the Provider returns NULL. NOTE: Unless the
     *         Association Instance contains non-reference Properties,
     *         referenceNames effectively returns the same information as
     *         references.
     * @exception CIMException If unsuccessful, one of the following status
     *                    codes may be returned. The ORDERED list is:
     *                    <ul>
     *                    <li>CIM_ERR_ACCESS_DENIED</li>
     *                    <li>CIM_ERR_NOT_SUPPORTED</li>
     *                    <li>CIM_ERR_INVALID_NAMESPACE</li>
     *                    <li>CIM_ERR_INVALID_PARAMETER (including missing,
     *                    duplicate, unrecognized or otherwise incorrect
     *                    parameters)</li>
     *                    <li>CIM_ERR_FAILED (some other unspecified error
     *                    occurred)</li>
     *                    </ul>
     */
    public CIMObjectPath[] referenceNames(CIMObjectPath assocName,
					  CIMObjectPath objectName,
					  String role) 
        throws CIMException;

}
