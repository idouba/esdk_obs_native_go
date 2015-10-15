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

import javax.wbem.cim.CIMException;

/** 
 * 
 * Exception class representing provider exceptions that occur in
 * the CIMOM's providers.
 * 
 * @author      Sun Microsystems, Inc.
 * @version 	1.6, 02/28/02
 * @since	WBEM 1.0
 */

public class CIMProviderException extends CIMException {

    final static long serialVersionUID = 200;


    /**
     * The provider class present in the class path does not implement 
     * the InstanceProvider interface - even though it has been declared
     * as an instance provider by setting the Provider qualifier in the MOF 
     * class definition. This error message uses two
     * parameters, the CIM class for which the provider is being defined, the 
     * name of the Java provider class. 
     */
    public final static String NOT_INSTANCE_PROVIDER = 
				"NOT_INSTANCE_PROVIDER";

    /**
     * The instance provider class is not found.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the CIM class for which the provider is being defined, the 
     * name of the Java provider class.
     */
    public final static String NO_INSTANCE_PROVIDER = 
				"NO_INSTANCE_PROVIDER";

    /**
     * The provider class does not implement the MethodProvider interface -
     * even though it has been declared as a method provider by setting the 
     * Provider qualifier in the MOF method definition.
     * This error message uses two
     * parameters, the method for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NOT_METHOD_PROVIDER = 
				"NOT_METHOD_PROVIDER";

    /**
     * The provider class does not implement the AssociatorProvider interface
     * even though it has been declared as a associator provider by setting 
     * the Provider qualifier in the MOF method definition.
     * This error message uses two
     * parameters, the method for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NOT_ASSOCIATOR_PROVIDER = 
				"NOT_ASSOCIATOR_PROVIDER";
    /**
     * The associator provider class is not found.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the CIM class for which the provider is being defined, the 
     * name of the Java provider class.
     */
    public final static String NO_ASSOCIATOR_PROVIDER = 
				"NOe_ASSOCIATOR_PROVIDER";

    /**
     * The provider class does not implement the AuthorizableProvider interface
     * even though it has been declared as a authorizable provider by setting 
     * the Provider qualifier in the MOF method definition.
     * This error message uses two
     * parameters, the method for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NOT_AUTHORIZABLE_PROVIDER = 
				"NOT_AUTHORIZABLE_PROVIDER";
    /**
     * The method provider class is not found.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the CIM class for which the provider is being defined, the 
     * name of the Java provider class.
     */
    public final static String NO_AUTHORIZABLE_PROVIDER = 
				"NO_AUTHORIZABLE_PROVIDER";


    /**
     * The method provider class is not found.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the CIM class for which the provider is being defined, the 
     * name of the Java provider class.
     */
    public final static String NO_METHOD_PROVIDER = 
				"NO_METHOD_PROVIDER";
    /**
     * The provider class present in the class
     * path does not implement the PropertyProvider interface - even though it
     * has been declared as a property provider by setting the Provider 
     * qualifier in the MOF property definition. This error 
     * message uses two
     * parameters, the property for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NOT_PROPERTY_PROVIDER = 
				"NOT_PROPERTY_PROVIDER";

    /**
     * The event provider is not found. Certain indications like Process 
     * Indications require an event provider to be present.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the CIM class for which the provider is being defined, the 
     * name of the Java provider class.
     */
    public final static String NO_PROPERTY_PROVIDER = 
				"NO_PROPERTY_PROVIDER";

    /**
     * The provider class present in the class
     * path does not implement the EventProvider interface.
     * parameters, the indication class for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NOT_EVENT_PROVIDER = 
				"NOT_EVENT_PROVIDER";

    /**
     * The provider class is not found.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the indication class for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NO_EVENT_PROVIDER = 
				"NO_EVENT_PROVIDER";

    /**
     * The provider class present in the class
     * path does not implement the CIMIndicationProvider interface.
     * parameters, the indication class for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NOT_INDICATION_PROVIDER = 
				"NOT_INDICATION_PROVIDER";

    /**
     * The provider class is not found.
     * This could be because the class path of the CIMOM does not contain
     * the provider class.
     * parameters, the indication class for which the provider is being defined,
     * the name of the Java provider class.
     */
    public final static String NO_INDICATION_PROVIDER = 
				"NO_INDICATION_PROVIDER";

    /**
     * The protocol specified in the providers qualifier is incorrect.
     * The CIMOM does not have a protocol adapter currently loaded which
     * matches the one specified by the provider
     */
    public final static String UNKNOWN_PROVIDER_ADAPTER = 
				"UNKNOWN_PROVIDER_ADAPTER";

    /**
     * There was some error within the provider implementation.
     * The remaining parameters in this exception are defined
     * by the provider itself.
     */
    public final static String GENERAL_EXCEPTION = "GENERAL_EXCEPTION";

    /**
     * Creates a CIMProviderException with no detail message.
     */
    public CIMProviderException() {
	super();
    }

    /**
     * Creates a CIMProviderException with the specified 
     * detail message.
     *
     * @param s		the detail message.
     */
    public CIMProviderException(String s) {
	super(s);
    }

    /**
     * Creates a CIMProviderException with the specified detail
     * message and one exception parameter.
     *
     * @param s		the detail message.
     * @param param     exception parameter.
     */
    public CIMProviderException(String s, Object param) {
	super(s, param);
    }

    /**
     * Creates a CIMProviderException with the specified detail
     * message and two exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     */
    public CIMProviderException(String s, Object param1, Object param2) {
	super(s, param1, param2);
    }

    /**
     * Creates a CIMProviderException with the specified message 
     * and three exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     * @param param3    third Exception parameter.
     *
     */
    public CIMProviderException(String s, 
				Object param1, 
				Object param2, 
				Object param3) {
	super(s, param1, param2, param3);
    }

    /**
     * Creates a CIMProviderException with the specified message 
     * and four exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     * @param param3    third Exception parameter.
     * @param param4    fourth Exception parameter.
     *
     */
    public CIMProviderException(String s, 
				Object param1, 
				Object param2, 
				Object param3, 
				Object param4) {
	super(s);
	Object params[] = new Object[4];
	params[0] = param1;
	params[1] = param2;
	params[2] = param3;
	params[3] = param4;
	setParams(params);
    }

    /**
     * Creates a CIMProviderException with the specified detail 
     * message and five exception parameters.
     *
     * @param s		the detail message.
     * @param param1    first Exception parameter.
     * @param param2    second Exception parameter.
     * @param param3    third Exception parameter.
     * @param param4    fourth Exception parameter.
     * @param param5    fifth Exception parameter.
     *
     */
    public CIMProviderException(String s, 
			        Object param1, 
				Object param2, 
				Object param3, 
				Object param4,
				Object param5) {
	super(s);
	Object params[] = new Object[5];
	params[0] = param1;
	params[1] = param2;
	params[2] = param3;
	params[3] = param4;
	params[4] = param5;
	setParams(params);
    }


    /**
     * Creates a CIMProviderException with the specified detial
     * message and an array of exception parameters.
     *
     * @param s	the detail message.
     * @param param     array of exception parameters.
     *
     */
    public CIMProviderException(String s, Object[] param) {
	super(s, param);
    }

}
