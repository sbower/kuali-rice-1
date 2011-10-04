/*
 * Copyright 2008-2009 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.kim.api.identity;

import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.exception.RiceIllegalStateException;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.identity.address.EntityAddress;
import org.kuali.rice.kim.api.identity.affiliation.EntityAffiliation;
import org.kuali.rice.kim.api.identity.affiliation.EntityAffiliationType;
import org.kuali.rice.kim.api.identity.citizenship.EntityCitizenship;
import org.kuali.rice.kim.api.identity.email.EntityEmail;
import org.kuali.rice.kim.api.identity.employment.EntityEmployment;
import org.kuali.rice.kim.api.identity.entity.Entity;
import org.kuali.rice.kim.api.identity.entity.EntityDefault;
import org.kuali.rice.kim.api.identity.entity.EntityDefaultQueryResults;
import org.kuali.rice.kim.api.identity.entity.EntityQueryResults;
import org.kuali.rice.kim.api.identity.external.EntityExternalIdentifier;
import org.kuali.rice.kim.api.identity.external.EntityExternalIdentifierType;
import org.kuali.rice.kim.api.identity.name.EntityName;
import org.kuali.rice.kim.api.identity.name.StringToKimEntityNameInfoMapAdapter;
import org.kuali.rice.kim.api.identity.personal.EntityBioDemographics;
import org.kuali.rice.kim.api.identity.personal.EntityEthnicity;
import org.kuali.rice.kim.api.identity.phone.EntityPhone;
import org.kuali.rice.kim.api.identity.principal.EntityNamePrincipalName;
import org.kuali.rice.kim.api.identity.principal.Principal;
import org.kuali.rice.kim.api.identity.principal.StringToKimEntityNamePrincipalInfoMapAdapter;
import org.kuali.rice.kim.api.identity.privacy.EntityPrivacyPreferences;
import org.kuali.rice.kim.api.identity.residency.EntityResidency;
import org.kuali.rice.kim.api.identity.type.EntityTypeContactInfo;
import org.kuali.rice.kim.api.identity.visa.EntityVisa;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.List;
import java.util.Map;

/**
 * This service provides operations to query for principal and identity data.
 * 
 * <p>A principal represents an identity that can authenticate.  In essence, a principal can be
 * thought of as an "account" or as an identity's authentication credentials.  A principal has
 * an id which is used to uniquely identify it.  It also has a name which represents the
 * principal's username and is typically what is entered when authenticating.  All principals
 * are associated with one and only one identity.
 * 
 * <p>An identity represents a person or system.  Additionally, other "types" of entities can
 * be defined in KIM.  Information like name, phone number, etc. is associated with an identity.
 * It is the representation of a concrete person or system.  While an identity will typically
 * have a single principal associated with it, it is possible for an identity to have more than
 * one principal or even no principals at all (in the case where the identity does not actually
 * authenticate).
 * 
 * <p>This service also provides operations for querying various pieces of reference data, such as 
 * address types, affiliation types, phone types, etc.
 *
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 *
 */
@WebService(name = "identityServiceSoap", targetNamespace = KimConstants.Namespaces.KIM_NAMESPACE_2_0)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT, use = SOAPBinding.Use.LITERAL, parameterStyle = SOAPBinding.ParameterStyle.WRAPPED)
public interface IdentityService {

    /**
     * This method finds EntityDefault data based on a query criteria.  The criteria cannot be null.
     *
     * @param query the criteria.  Cannot be null.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if the queryByCriteria is null
     */
    @WebMethod(operationName = "findEntityDefaults")
    @WebResult(name = "results")
	EntityDefaultQueryResults findEntityDefaults(@WebParam(name = "query") QueryByCriteria query)  throws RiceIllegalArgumentException;

    /**
     * This method finds Entities based on a query criteria.  The criteria cannot be null.
     *
     * @param query the criteria.  Cannot be null.
     * @return query results.  will never return null.
     * @throws IllegalArgumentException if the queryByCriteria is null
     */
    @WebMethod(operationName = "findEntities")
    @WebResult(name = "results")
	EntityQueryResults findEntities(@WebParam(name = "query") QueryByCriteria query)  throws RiceIllegalArgumentException;

	
    /**
     * Gets a {@link org.kuali.rice.kim.api.identity.entity.Entity} from an id.
     *
     * <p>
     *   This method will return null if the Entity does not exist.
     * </p>
     *
     * @param id the unique id to retrieve the entity by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.entity.Entity} or null
     * @throws IllegalArgumentException if the id is blank
     */
    @WebMethod(operationName = "getEntity")
    @WebResult(name = "entity")
    @Cacheable(value= Entity.Cache.NAME, key="'id=' + #id")
	Entity getEntity( @WebParam(name="id") String id )  throws RiceIllegalArgumentException;

	/**
     * Gets a {@link org.kuali.rice.kim.api.identity.entity.Entity} from a principalId.
     *
     * <p>
     *   This method will return null if the Entity does not exist.
     * </p>
     *
     * @param principalId the unique id to retrieve the entity by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.entity.Entity} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    @WebMethod(operationName = "getEntityByPrincipalId")
    @WebResult(name = "entity")
    @Cacheable(value= Entity.Cache.NAME, key="'principalId=' + #principalId")
	Entity getEntityByPrincipalId(@WebParam(name = "principalId") String principalId)  throws RiceIllegalArgumentException;

	/**
     * Gets a {@link org.kuali.rice.kim.api.identity.entity.Entity} from a principalName.
     *
     * <p>
     *   This method will return null if the Entity does not exist.
     * </p>
     *
     * @param principalName the unique id to retrieve the entity by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.entity.Entity} or null
     * @throws IllegalArgumentException if the id is blank
     */
    @WebMethod(operationName = "getEntityByPrincipalName")
    @WebResult(name = "entity")
    @Cacheable(value= Entity.Cache.NAME, key="'principalName=' + #principalName")
	Entity getEntityByPrincipalName(@WebParam(name = "principalName") String principalName)  throws RiceIllegalArgumentException;


    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.entity.Entity} exactly like the entity passed in.
     *
     * @param entity the entity to create
     * @return the newly created Entity object.
     * @throws IllegalArgumentException if the entity is null
     * @throws IllegalStateException if the entity already exists in the system
     */
    @WebMethod(operationName="createEntity")
    @WebResult(name = "entity")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Entity createEntity(@WebParam(name = "entity") Entity entity)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.entity.Entity}.
     *
     * @param entity the entity to update
     * @return the updated Entity object.
     * @throws IllegalArgumentException if the entity is null
     * @throws IllegalStateException if the entity does not already exist in the system
     */
    @WebMethod(operationName="updateEntity")
    @WebResult(name = "entity")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Entity updateEntity(@WebParam(name = "entity") Entity entity)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.entity.Entity}.
     *
     * @param id the unique id of the entity to inactivate
     * @return the inactivated Entity object.
     * @throws IllegalArgumentException if the entity is null
     * @throws IllegalStateException if the entity does not already exist in the system
     */
    @WebMethod(operationName="inactivateEntity")
    @WebResult(name = "entity")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Entity inactivateEntity(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    
    
    /**
     * Gets a {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} from an id.
     * {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} is a condensed version of {@link org.kuali.rice.kim.api.identity.entity.Entity} that contains
     * default values of its subclasses
     *
     * <p>
     *   This method will return null if the Entity does not exist.
     * </p>
     *
     * @param id the unique id to retrieve the entity by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} or null
     * @throws IllegalArgumentException if the id is blank
     */
    @WebMethod(operationName = "getEntityDefault")
    @WebResult(name = "entityDefault")
    @Cacheable(value= EntityDefault.Cache.NAME, key="'id=' + #id")
	EntityDefault getEntityDefault(@WebParam(name = "id") String id)  throws RiceIllegalArgumentException;

	/**
     * Gets a {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} from an principalId.
     * {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} is a condensed version of {@link org.kuali.rice.kim.api.identity.entity.Entity} that contains
     * default values of its subclasses
     *
     * <p>
     *   This method will return null if the Entity does not exist.
     * </p>
     *
     * @param principalId the unique id to retrieve the entity by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    @WebMethod(operationName = "getEntityDefaultByPrincipalId")
    @WebResult(name = "entityDefault")
    @Cacheable(value= EntityDefault.Cache.NAME, key="'principalId=' + #principalId")
	EntityDefault getEntityDefaultByPrincipalId(@WebParam(name = "principalId") String principalId)  throws RiceIllegalArgumentException;

	/**
     * Gets a {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} from an principalName.
     * {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} is a condensed version of {@link org.kuali.rice.kim.api.identity.entity.Entity} that contains
     * default values of its subclasses
     *
     * <p>
     *   This method will return null if the Entity does not exist.
     * </p>
     *
     * @param principalName the unique id to retrieve the entity by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.entity.EntityDefault} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    @WebMethod(operationName = "getEntityDefaultByPrincipalName")
    @WebResult(name = "entityDefault")
    @Cacheable(value= EntityDefault.Cache.NAME, key="'principalName=' + #principalName")
	EntityDefault getEntityDefaultByPrincipalName(@WebParam(name = "principalName") String principalName)  throws RiceIllegalArgumentException;
    
    

    /**
     * Gets a {@link org.kuali.rice.kim.api.identity.principal.Principal} from an principalId.
     *
     * <p>
     *   This method will return null if the Principal does not exist.
     * </p>
     *
     * @param principalId the unique id to retrieve the principal by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.principal.Principal} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    @WebMethod(operationName = "getPrincipal")
    @WebResult(name = "principal")
    @Cacheable(value= Principal.Cache.NAME, key="'principalId=' + #principalId")
    Principal getPrincipal( @WebParam(name="principalId") String principalId )  throws RiceIllegalArgumentException;

    /**
     * Gets a {@link org.kuali.rice.kim.api.identity.principal.Principal} from an principalName.
     *
     * <p>
     *   This method will return null if the Principal does not exist.
     * </p>
     *
     * @param principalName the unique id to retrieve the principal by. cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.principal.Principal} or null
     * @throws IllegalArgumentException if the principalId is blank
     */
    @WebMethod(operationName = "getPrincipalByPrincipalName")
    @WebResult(name = "principal")
    @Cacheable(value= Principal.Cache.NAME, key="'principalName=' + #principalName")
    Principal getPrincipalByPrincipalName( @WebParam(name="principalName") String principalName )  throws RiceIllegalArgumentException;

    /**
     * Gets a {@link org.kuali.rice.kim.api.identity.principal.Principal} from an principalName and password.
     *
     * <p>
     *   This method will return null if the Principal does not exist or the password is incorrect.
     * </p>
     *
     * @param principalName the unique id to retrieve the principal by. cannot be null.
     * @param password the password for the principal
     * @return a {@link org.kuali.rice.kim.api.identity.principal.Principal} or null
     * @throws IllegalArgumentException if the principalName is blank
     */
    @WebMethod(operationName = "getPrincipalByPrincipalNameAndPassword")
    @WebResult(name = "principal")
    @Cacheable(value= Principal.Cache.NAME, key="'principalName=' + #principalName + '|' + 'password=' + #password")
    Principal getPrincipalByPrincipalNameAndPassword( @WebParam(name="principalName") String principalName,  @WebParam(name="password") String password )  throws RiceIllegalArgumentException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.principal.Principal} exactly like the principal passed in.
     *
     * The principal object passed in must be populated with an entityId and a principalName
     *
     * @param principal the principal to create
     * @return the newly created Principal object.
     * @throws IllegalArgumentException if the principal is null
     * @throws IllegalStateException if the principal already exists in the system or the principal object is not populated with entityId and principalName
     */
    @WebMethod(operationName="addPrincipalToEntity")
    @WebResult(name = "principal")
    @CacheEvict(value = {Principal.Cache.NAME, Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Principal addPrincipalToEntity(@WebParam(name = "principal") Principal principal)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.principal.Principal} exactly like the principal passed in.
     *
     *
     * @param principal the principal to update
     * @return the updated Principal object.
     * @throws IllegalArgumentException if the principal is null
     * @throws IllegalStateException if the principal does not exist in the system.
     */
    @WebMethod(operationName="updatePrincipal")
    @WebResult(name = "principal")
    @CacheEvict(value = {Principal.Cache.NAME, Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Principal updatePrincipal(@WebParam(name = "principal") Principal principal)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.principal.Principal}.
     *
     *
     * @param principalId the unique id of the principal to inactivate
     * @return the inactivated Principal object.
     * @throws IllegalArgumentException if the principal is null
     * @throws IllegalStateException if the principal does not exist in the system.
     */
    @WebMethod(operationName="inactivatePrincipal")
    @WebResult(name = "principal")
    @CacheEvict(value = {Principal.Cache.NAME, Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Principal inactivatePrincipal(@WebParam(name = "principalId") String principalId)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.principal.Principal}.
     *
     *
     * @param principalName the unique principalName of the principal to inactivate
     * @return the inactivated Principal object.
     * @throws IllegalArgumentException if the principal is null
     * @throws IllegalStateException if the principal does not exist in the system.
     */
    @WebMethod(operationName="inactivatePrincipalByName")
    @WebResult(name = "principal")
    @CacheEvict(value = {Principal.Cache.NAME, Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    Principal inactivatePrincipalByName(@WebParam(name = "principalName") String principalName)
        throws RiceIllegalArgumentException, RiceIllegalStateException;


    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.type.EntityTypeContactInfo} exactly like the entityTypeContactInfo passed in.
     *
     * The EntityTypeContactInfo object passed in must be populated with an entityId and a entityTypeCode
     *
     * @param entityTypeContactInfo the EntityTypeContactInfo to create
     * @return the newly created EntityTypeContactInfo object.
     * @throws IllegalArgumentException if the entityTypeContactInfo is null
     * @throws IllegalStateException if the entityTypeContactInfo already exists in the system or the EntityTypeContactInfo object is not populated with entityId and entityTypeCode
     */
    @WebMethod(operationName="addEntityTypeContactInfoToEntity")
    @WebResult(name = "entityTypeContactInfo")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityTypeContactInfo addEntityTypeContactInfoToEntity(
            @WebParam(name = "entityTypeContactInfo") EntityTypeContactInfo entityTypeContactInfo)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.type.EntityTypeContactInfo} exactly like the entityTypeContactInfo passed in.
     *
     *
     * @param entityTypeContactInfo the EntityTypeContactInfo to update
     * @return the updated EntityTypeContactInfo object.
     * @throws IllegalArgumentException if the entityTypeContactInfo is null
     * @throws IllegalStateException if the entityTypeContactInfo does not exist in the system.
     */
    @WebMethod(operationName="updateEntityTypeContactInfo")
    @WebResult(name = "entityTypeContactInfo")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityTypeContactInfo updateEntityTypeContactInfo(@WebParam(name = "entityTypeContactInfo") EntityTypeContactInfo entityTypeContactInfo)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.type.EntityTypeContactInfo} with the passed in parameters.
     *
     *
     * @param entityId the entityId of the EntityTypeContactInfo to inactivate
     * @param entityTypeCode the entityTypeCode of the EntityTypeContactInfo to inactivate
     * @return the inactivated EntityTypeContactInfo object.
     * @throws IllegalArgumentException if the entityId or entityTypeCode passed in is null
     * @throws IllegalStateException if the EntityTypeContactInfo does not exist in the system.
     */
    @WebMethod(operationName="inactivateEntityTypeContactInfo")
    @WebResult(name = "entityTypeContactInfo")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityTypeContactInfo inactivateEntityTypeContactInfo(@WebParam(name = "entityId") String entityId,
            @WebParam(name = "entityTypeCode") String entityTypeCode)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.address.EntityAddress} exactly like the address passed in.
     *
     * The EntityAddress object passed in must be populated with an entityId and a entityTypeCode
     *
     * @param address the EntityAddress to create
     * @return the newly created EntityAddress object.
     * @throws IllegalArgumentException if the address is null
     * @throws IllegalStateException if the address already exists in the system or address is not populated with entityId and entityTypeCode
     */
    @WebMethod(operationName="addAddressToEntity")
    @WebResult(name = "address")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityAddress addAddressToEntity(@WebParam(name = "address") EntityAddress address)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.address.EntityAddress} exactly like the address passed in.
     *
     *
     * @param address the EntityAddress to update
     * @return the updated EntityAddress object.
     * @throws IllegalArgumentException if the address is null
     * @throws IllegalStateException if the address does not exist in the system.
     */
    @WebMethod(operationName="updateAddress")
    @WebResult(name = "address")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityAddress updateAddress(@WebParam(name = "address")EntityAddress address)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.address.EntityAddress} with the id passed in.
     *
     *
     * @param id the unique id of the EntityAddress to inactivate
     * @return the updated EntityAddress object.
     * @throws IllegalArgumentException if the id is null
     * @throws IllegalStateException if the address does not exist in the system.
     */
    @WebMethod(operationName="inactivateAddress")
    @WebResult(name = "address")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityAddress inactivateAddress(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.email.EntityEmail} exactly like the email passed in.
     *
     * The EntityEmail object passed in must be populated with an entityId and a entityTypeCode
     *
     * @param email the EntityEmail to create
     * @return the newly created EntityEmail object.
     * @throws IllegalArgumentException if the email is null
     * @throws IllegalStateException if the email already exists in the system or email is not populated with entityId and entityTypeCode
     */
    @WebMethod(operationName="addEmailToEntity")
    @WebResult(name = "email")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEmail addEmailToEntity(@WebParam(name = "email") EntityEmail email)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.email.EntityEmail} exactly like the email passed in.
     *
     *
     * @param email the EntityEmail to update
     * @return the updated EntityEmail object.
     * @throws IllegalArgumentException if the email is null
     * @throws IllegalStateException if the email does not exist in the system.
     */
    @WebMethod(operationName="updateEmail")
    @WebResult(name = "email")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEmail updateEmail(@WebParam(name = "email") EntityEmail email)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate the {@link org.kuali.rice.kim.api.identity.email.EntityEmail} with the passed in id.
     *
     *
     * @param id the unique id of the EntityEmail to inactivate
     * @return the inactivated EntityEmail object.
     * @throws IllegalArgumentException if the id is null
     * @throws IllegalStateException if the email does not exist in the system.
     */
    @WebMethod(operationName="inactivateEmail")
    @WebResult(name = "email")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEmail inactivateEmail(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.phone.EntityPhone} exactly like the phone passed in.
     *
     * The EntityPhone object passed in must be populated with an entityId and a entityTypeCode
     *
     * @param phone the EntityPhone to create
     * @return the newly created EntityPhone object.
     * @throws IllegalArgumentException if the phone is null
     * @throws IllegalStateException if the phone already exists in the system or phone is not populated with entityId and entityTypeCode
     */
    @WebMethod(operationName="addPhoneToEntity")
    @WebResult(name = "phone")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityPhone addPhoneToEntity(@WebParam(name = "phone") EntityPhone phone)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.phone.EntityPhone} exactly like the phone passed in.
     *
     *
     * @param phone the EntityPhone to update
     * @return the updated EntityPhone object.
     * @throws IllegalArgumentException if the phone is null
     * @throws IllegalStateException if the phone does not exist in the system.
     */
    @WebMethod(operationName="updatePhone")
    @WebResult(name = "phone")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityPhone updatePhone(@WebParam(name = "phone") EntityPhone phone)
            throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate the {@link org.kuali.rice.kim.api.identity.phone.EntityPhone} with the passed in id.
     *
     *
     * @param id the unique id of the EntityPhone to inactivate
     * @return the inactivated EntityPhone object.
     * @throws IllegalArgumentException if the id is null
     * @throws IllegalStateException if the phone does not exist in the system.
     */
    @WebMethod(operationName="inactivatePhone")
    @WebResult(name = "phone")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityPhone inactivatePhone(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;


    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.external.EntityExternalIdentifier} exactly like the externalId passed in.
     *
     * The EntityExternalIdentifier object passed in must be populated with an entityId and a externalIdentifierTypeCode
     *
     * @param externalId the EntityExternalIdentifier to create
     * @return the newly created EntityExternalIdentifier object.
     * @throws IllegalArgumentException if the externalId is null
     * @throws IllegalStateException if the externalId already exists in the system or externalId is not populated with entityId and externalIdentifierTypeCode
     */
    @WebMethod(operationName="addExternalIdentifierToEntity")
    @WebResult(name = "externalId")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityExternalIdentifier addExternalIdentifierToEntity(@WebParam(name = "externalId") EntityExternalIdentifier externalId)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.external.EntityExternalIdentifier} exactly like the externalId passed in.
     *
     *
     * @param externalId the EntityExternalIdentifier to update
     * @return the updated EntityExternalIdentifier object.
     * @throws IllegalArgumentException if the externalId is null
     * @throws IllegalStateException if the externalId does not exist in the system.
     */
    @WebMethod(operationName="updateExternalIdentifier")
    @WebResult(name = "externalId")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityExternalIdentifier updateExternalIdentifier(@WebParam(name = "externalId") EntityExternalIdentifier externalId)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.affiliation.EntityAffiliation} exactly like the affiliation passed in.
     *
     * The EntityAffiliation object passed in must be populated with an entityId and a affiliationType
     *
     * @param affiliation the EntityAffiliation to create
     * @return the newly created EntityAffiliation object.
     * @throws IllegalArgumentException if the affiliation is null
     * @throws IllegalStateException if the affiliation already exists in the system or affiliation is not populated with entityId and affiliationType
     */
    @WebMethod(operationName="addAffiliationToEntity")
    @WebResult(name = "affiliation")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityAffiliation addAffiliationToEntity(@WebParam(name = "affiliation") EntityAffiliation affiliation)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.affiliation.EntityAffiliation} exactly like the affiliation passed in.
     *
     *
     * @param affiliation the EntityAffiliation to update
     * @return the updated EntityAffiliation object.
     * @throws IllegalArgumentException if the affiliation is null
     * @throws IllegalStateException if the affiliation does not exist in the system.
     */
    @WebMethod(operationName="updateAffiliation")
    @WebResult(name = "affiliation")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityAffiliation updateAffiliation(@WebParam(name = "affiliation") EntityAffiliation affiliation)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.affiliation.EntityAffiliation} with the id passed in.
     *
     *
     * @param id the unique id of the  EntityAffiliation to inactivate
     * @return the updated EntityAffiliation object.
     * @throws IllegalArgumentException if the affiliation is null
     * @throws IllegalStateException if the affiliation does not exist in the system.
     */
    @WebMethod(operationName="inactivateAffiliation")
    @WebResult(name = "affiliation")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityAffiliation inactivateAffiliation(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.name.EntityName} exactly like the name passed in.
     *
     * The EntityName object passed in must be populated with an entityId and a nameType
     *
     * @param name the EntityName to create
     * @return the newly created EntityName object.
     * @throws IllegalArgumentException if the name is null
     * @throws IllegalStateException if the name already exists in the system or name is not populated with entityId and nameType
     */
    @WebMethod(operationName="addNameToEntity")
    @WebResult(name = "name")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityName addNameToEntity(@WebParam(name = "name") EntityName name)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.name.EntityName} exactly like the name passed in.
     *
     *
     * @param name the EntityName to update
     * @return the updated EntityName object.
     * @throws IllegalArgumentException if the name is null
     * @throws IllegalStateException if the name does not exist in the system.
     */
    @WebMethod(operationName="updateName")
    @WebResult(name = "name")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityName updateName(@WebParam(name = "name") EntityName name)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.name.EntityName} with the passed in id.
     *
     *
     * @param id the unique id of the EntityName to inactivate
     * @return the inactivated EntityName object.
     * @throws IllegalArgumentException if the id is null
     * @throws IllegalStateException if the name with the id does not exist in the system.
     */
    @WebMethod(operationName="inactivateName")
    @WebResult(name = "name")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityName inactivateName(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.employment.EntityEmployment} exactly like the employment passed in.
     *
     * The EntityEmployment object passed in must be populated with an entityId and a employmentType
     *
     * @param employment the EntityEmployment to create
     * @return the newly created EntityName object.
     * @throws IllegalArgumentException if the employment is null
     * @throws IllegalStateException if the employment already exists in the system or employment is not populated with entityId and employmentType
     */
    @WebMethod(operationName="addEmploymentToEntity")
    @WebResult(name = "employment")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEmployment addEmploymentToEntity(@WebParam(name = "employment") EntityEmployment employment)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.employment.EntityEmployment} exactly like the employment passed in.
     *
     *
     * @param employment the EntityEmployment to update
     * @return the updated EntityEmployment object.
     * @throws IllegalArgumentException if the employment is null
     * @throws IllegalStateException if the employment does not exist in the system.
     */
    @WebMethod(operationName="updateEmployment")
    @WebResult(name = "employment")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEmployment updateEmployment(@WebParam(name = "employment") EntityEmployment employment)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.employment.EntityEmployment} with the passed in id.
     *
     *
     * @param id the unique id of the EntityEmployment to inactivate
     * @return the inactivated EntityEmployment object.
     * @throws IllegalArgumentException if the id is null
     * @throws IllegalStateException if the employment with the id does not exist in the system.
     */
    @WebMethod(operationName="inactivateEmployment")
    @WebResult(name = "employment")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEmployment inactivateEmployment(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.personal.EntityBioDemographics} exactly like the bioDemographics passed in.
     *
     * The EntityBioDemographics object passed in must be populated with an entityId
     *
     * @param bioDemographics the EntityBioDemographics to create
     * @return the newly created EntityBioDemographics object.
     * @throws IllegalArgumentException if the bioDemographics is null
     * @throws IllegalStateException if the bioDemographics already exists in the system or bioDemographics is not populated with entityId
     */
    @WebMethod(operationName="addBioDemographicsToEntity")
    @WebResult(name = "bioDemographics")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityBioDemographics addBioDemographicsToEntity(@WebParam(name = "bioDemographics") EntityBioDemographics bioDemographics)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.personal.EntityBioDemographics} exactly like the bioDemographics passed in.
     *
     *
     * @param bioDemographics the EntityBioDemographics to update
     * @return the updated EntityBioDemographics object.
     * @throws IllegalArgumentException if the bioDemographics is null
     * @throws IllegalStateException if the bioDemographics does not exist in the system.
     */
    @WebMethod(operationName="updateBioDemographics")
    @WebResult(name = "bioDemographics")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityBioDemographics updateBioDemographics(@WebParam(name = "bioDemographics") EntityBioDemographics bioDemographics)
        throws RiceIllegalArgumentException, RiceIllegalStateException;
    
    /**
     * Gets a {@link org.kuali.rice.kim.api.identity.privacy.EntityPrivacyPreferences} for a given id.
     *
     * <p>
     *   This method will return null if the EntityPrivacyPreferences does not exist.
     * </p>
     *
     * @param id the unique id to retrieve the EntityPrivacyPreferences by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.privacy.EntityPrivacyPreferences} or null
     * @throws IllegalArgumentException if the entityId is blank
     */
    @WebMethod(operationName = "getEntityPrivacyPreferences")
    @WebResult(name = "privacyPreferences")
    @Cacheable(value= EntityPrivacyPreferences.Cache.NAME, key="'id=' + #id")
	EntityPrivacyPreferences getEntityPrivacyPreferences( @WebParam(name="id") String id )  throws RiceIllegalArgumentException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.privacy.EntityPrivacyPreferences} exactly like the privacyPreferences passed in.
     *
     * The EntityPrivacyPreferences object passed in must be populated with an entityId
     *
     * @param privacyPreferences the EntityPrivacyPreferences to create
     * @return the newly created EntityPrivacyPreferences object.
     * @throws IllegalArgumentException if the privacyPreferences is null
     * @throws IllegalStateException if the privacyPreferences already exists in the system or privacyPreferences is not populated with entityId
     */
    @WebMethod(operationName="addPrivacyPreferencesToEntity")
    @WebResult(name = "privacyPreferences")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME, EntityPrivacyPreferences.Cache.NAME}, allEntries = true)
    EntityPrivacyPreferences addPrivacyPreferencesToEntity(@WebParam(name = "privacyPreferences") EntityPrivacyPreferences privacyPreferences)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.privacy.EntityPrivacyPreferences} exactly like the privacyPreferences passed in.
     *
     *
     * @param privacyPreferences the EntityPrivacyPreferences to update
     * @return the updated EntityPrivacyPreferences object.
     * @throws IllegalArgumentException if the privacyPreferences is null
     * @throws IllegalStateException if the privacyPreferences does not exist in the system.
     */
    @WebMethod(operationName="updatePrivacyPreferences")
    @WebResult(name = "privacyPreferences")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME, EntityPrivacyPreferences.Cache.NAME}, allEntries = true)
    EntityPrivacyPreferences updatePrivacyPreferences(@WebParam(name = "privacyPreferences") EntityPrivacyPreferences privacyPreferences)
        throws RiceIllegalArgumentException, RiceIllegalStateException;


    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.citizenship.EntityCitizenship} exactly like the citizenship passed in.
     *
     * The EntityCitizenship object passed in must be populated with an entityId and a status
     *
     * @param citizenship the EntityCitizenship to create
     * @return the newly created EntityCitizenship object.
     * @throws IllegalArgumentException if the citizenship is null
     * @throws IllegalStateException if the citizenship already exists in the system or citizenship is not populated with entityId and status
     */
    @WebMethod(operationName="addCitizenshipToEntity")
    @WebResult(name = "citizenship")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityCitizenship addCitizenshipToEntity(@WebParam(name = "citizenship") EntityCitizenship citizenship)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.citizenship.EntityCitizenship} exactly like the citizenship passed in.
     *
     *
     * @param citizenship the EntityCitizenship to update
     * @return the updated EntityCitizenship object.
     * @throws IllegalArgumentException if the citizenship is null
     * @throws IllegalStateException if the citizenship does not exist in the system.
     */
    @WebMethod(operationName="updateCitizenship")
    @WebResult(name = "citizenship")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityCitizenship updateCitizenship(@WebParam(name = "citizenship") EntityCitizenship citizenship)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will inactivate a {@link org.kuali.rice.kim.api.identity.citizenship.EntityCitizenship} with the unique id passed in.
     *
     *
     * @param id the id of the EntityCitizenship to inactivate
     * @return the inactivated EntityCitizenship object.
     * @throws IllegalArgumentException if the citizenship is null
     * @throws IllegalStateException if the citizenship does not exist in the system.
     */
    @WebMethod(operationName="inactivateCitizenship")
    @WebResult(name = "citizenship")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityCitizenship inactivateCitizenship(@WebParam(name = "id") String id)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link EntityEthnicity} exactly like the ethnicity passed in.
     *
     * The EntityEthnicity object passed in must be populated with an entityId and a ethnicity code
     *
     * @param ethnicity the EntityEthnicity to create
     * @return the newly created EntityEthnicity object.
     * @throws IllegalArgumentException if the ethnicity is null
     * @throws IllegalStateException if the ethnicity already exists in the system or ethnicity is not populated with entityId and ethnicity code
     */
    @WebMethod(operationName="addEthnicityToEntity")
    @WebResult(name = "ethnicity")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEthnicity addEthnicityToEntity(@WebParam(name = "ethnicity") EntityEthnicity ethnicity)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link EntityEthnicity} exactly like the ethnicity passed in.
     *
     *
     * @param ethnicity the EntityEthnicity to update
     * @return the updated EntityEthnicity object.
     * @throws IllegalArgumentException if the ethnicity is null
     * @throws IllegalStateException if the ethnicity does not exist in the system.
     */
    @WebMethod(operationName="updateEthnicity")
    @WebResult(name = "ethnicity")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityEthnicity updateEthnicity(@WebParam(name = "ethnicity") EntityEthnicity ethnicity)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.residency.EntityResidency} exactly like the residency passed in.
     *
     * The EntityResidency object passed in must be populated with an entityId
     *
     * @param residency the EntityResidency to create
     * @return the newly created EntityResidency object.
     * @throws IllegalArgumentException if the residency is null
     * @throws IllegalStateException if the residency already exists in the system or residency is not populated with entityId
     */
    @WebMethod(operationName="addResidencyToEntity")
    @WebResult(name = "residency")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityResidency addResidencyToEntity(@WebParam(name = "residency") EntityResidency residency)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.residency.EntityResidency} exactly like the residency passed in.
     *
     *
     * @param residency the EntityResidency to update
     * @return the updated EntityResidency object.
     * @throws IllegalArgumentException if the residency is null
     * @throws IllegalStateException if the residency does not exist in the system.
     */
    @WebMethod(operationName="updateResidency")
    @WebResult(name = "residency")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityResidency updateResidency(@WebParam(name = "residency") EntityResidency residency)
        throws RiceIllegalArgumentException, RiceIllegalStateException;


    /**
     * This will create a {@link org.kuali.rice.kim.api.identity.visa.EntityVisa} exactly like the visa passed in.
     *
     * The EntityVisa object passed in must be populated with an entityId and a visaTypeKey
     *
     * @param visa the EntityVisa to create
     * @return the newly created EntityVisa object.
     * @throws IllegalArgumentException if the visa is null
     * @throws IllegalStateException if the visa already exists in the system or visa is not populated with entityId and a visaTypeKey
     */
    @WebMethod(operationName="addVisaToEntity")
    @WebResult(name = "visa")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityVisa addVisaToEntity(@WebParam(name = "visa") EntityVisa visa)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * This will update a {@link org.kuali.rice.kim.api.identity.visa.EntityVisa} exactly like the visa passed in.
     *
     *
     * @param visa the EntityVisa to update
     * @return the updated EntityVisa object.
     * @throws IllegalArgumentException if the visa is null
     * @throws IllegalStateException if the visa does not exist in the system.
     */
    @WebMethod(operationName="updateVisa")
    @WebResult(name = "visa")
    @CacheEvict(value={Entity.Cache.NAME, EntityDefault.Cache.NAME}, allEntries = true)
    EntityVisa updateVisa(@WebParam(name = "visa") EntityVisa visa)
        throws RiceIllegalArgumentException, RiceIllegalStateException;

    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityType code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getEntityType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{EntityType}", key="'code=' + #code")
	CodedAttribute getEntityType( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;


    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityAddressType code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getAddressType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{AddressType}", key="'code=' + #code")
	CodedAttribute getAddressType( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;

    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.affiliation.EntityAffiliationType} for a given EntityAffiliationType code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the EntityAffiliationType by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.affiliation.EntityAffiliationType} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getAffiliationType")
    @WebResult(name = "affiliationType")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{AffiliationType}", key="'code=' + #code")
	EntityAffiliationType getAffiliationType( @WebParam(name="code") String code )  throws RiceIllegalArgumentException;

    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityCitizenship status code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getCitizenshipStatus")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{CitizenshipStatus}", key="'code=' + #code")
	CodedAttribute getCitizenshipStatus( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;

    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityEmployment type code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getEmploymentType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{EmploymentType}", key="'code=' + #code")
	CodedAttribute getEmploymentType( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;
    
    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityEmployment status code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getEmploymentStatus")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{EmploymentStatus}", key="'code=' + #code")
	CodedAttribute getEmploymentStatus( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;
    
    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.external.EntityExternalIdentifierType} for a given type code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the EntityExternalIdentifierType by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getExternalIdentifierType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{ExternalIdentifierType}", key="'code=' + #code")
	EntityExternalIdentifierType getExternalIdentifierType( @WebParam(name="code") String code )  throws RiceIllegalArgumentException;
    
    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityName type code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getNameType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{NameType}", key="'code=' + #code")
	CodedAttribute getNameType(@WebParam(name = "code") String code) throws RiceIllegalArgumentException;
    
    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityPhone type code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getPhoneType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{PhoneType}", key="'code=' + #code")
	CodedAttribute getPhoneType( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;
    
    /**
     * Gets the {@link org.kuali.rice.kim.api.identity.CodedAttribute} for a given EntityEmail type code.
     *
     * <p>
     *   This method will return null if the code does not exist.
     * </p>
     *
     * @param code the unique id to retrieve the Type by. Cannot be null.
     * @return a {@link org.kuali.rice.kim.api.identity.CodedAttribute} or null
     * @throws IllegalArgumentException if the code is blank
     */
    @WebMethod(operationName = "getEmailType")
    @WebResult(name = "type")
    @Cacheable(value= CodedAttribute.Cache.NAME + "{EmailType}", key="'code=' + #code")
	CodedAttribute getEmailType( @WebParam(name="code") String code ) throws RiceIllegalArgumentException;

}
