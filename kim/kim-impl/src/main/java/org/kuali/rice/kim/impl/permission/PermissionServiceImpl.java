/*
 * Copyright 2007-2008 The Kuali Foundation
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
package org.kuali.rice.kim.impl.permission;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.criteria.CriteriaLookupService;
import org.kuali.rice.core.api.criteria.GenericQueryResults;
import org.kuali.rice.core.api.criteria.LookupCustomizer;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.exception.RiceIllegalStateException;
import org.kuali.rice.core.api.resourceloader.GlobalResourceLoader;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.common.assignee.Assignee;
import org.kuali.rice.kim.api.common.delegate.DelegateType;
import org.kuali.rice.kim.api.common.template.Template;
import org.kuali.rice.kim.api.common.template.TemplateQueryResults;
import org.kuali.rice.kim.api.permission.Permission;
import org.kuali.rice.kim.api.permission.PermissionQueryResults;
import org.kuali.rice.kim.api.permission.PermissionService;
import org.kuali.rice.kim.api.role.Role;
import org.kuali.rice.kim.api.role.RoleMembership;
import org.kuali.rice.kim.api.role.RoleService;
import org.kuali.rice.kim.api.type.KimType;
import org.kuali.rice.kim.api.type.KimTypeInfoService;
import org.kuali.rice.kim.framework.permission.PermissionTypeService;
import org.kuali.rice.kim.impl.common.attribute.AttributeTransform;
import org.kuali.rice.kim.impl.common.attribute.KimAttributeDataBo;
import org.kuali.rice.kim.impl.role.RolePermissionBo;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.krad.util.KRADPropertyConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;

public class PermissionServiceImpl implements PermissionService {
	private RoleService roleService;
    private PermissionTypeService defaultPermissionTypeService;
    private KimTypeInfoService kimTypeInfoService;
	private BusinessObjectService businessObjectService;
	private CriteriaLookupService criteriaLookupService;

 	private final CopyOnWriteArrayList<Template> allTemplates = new CopyOnWriteArrayList<Template>();

    // --------------------
    // Authorization Checks
    // --------------------

    protected PermissionTypeService getPermissionTypeService( PermissionTemplateBo permissionTemplate ) {
    	if ( permissionTemplate == null ) {
    		throw new IllegalArgumentException( "permissionTemplate may not be null" );
    	}
    	KimType kimType = kimTypeInfoService.getKimType( permissionTemplate.getKimTypeId() );
    	String serviceName = kimType.getServiceName();
    	// if no service specified, return a default implementation
    	if ( StringUtils.isBlank( serviceName ) ) {
    		return defaultPermissionTypeService;
    	}
    	try {
	    	Object service = GlobalResourceLoader.getService(serviceName);
	    	// if we have a service name, it must exist
	    	if ( service == null ) {
				throw new RuntimeException("null returned for permission type service for service name: " + serviceName);
	    	}
	    	// whatever we retrieved must be of the correct type
	    	if ( !(service instanceof PermissionTypeService)  ) {
	    		throw new RuntimeException( "Service " + serviceName + " was not a PermissionTypeService.  Was: " + service.getClass().getName() );
	    	}
	    	return (PermissionTypeService)service;
    	} catch( Exception ex ) {
    		// sometimes service locators throw exceptions rather than returning null, handle that
    		throw new RuntimeException( "Error retrieving service: " + serviceName + " from the KimImplServiceLocator.", ex );
    	}
    }

    @Override
    public boolean hasPermission(String principalId, String namespaceCode, String permissionName, Map<String, String> permissionDetails) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }

        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }
        return isAuthorized( principalId, namespaceCode, permissionName, permissionDetails, Collections.<String, String>emptyMap() );
    }

    @Override
    public boolean isAuthorized(String principalId, String namespaceCode, String permissionName, Map<String, String> permissionDetails, Map<String, String> qualification ) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }

        if (qualification == null) {
            throw new RiceIllegalArgumentException("qualification is null");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }
        List<String> roleIds = getRoleIdsForPermission( namespaceCode, permissionName, permissionDetails );
    	if ( roleIds.isEmpty() ) {
    		return false;
    	}

		return roleService.principalHasRole( principalId, roleIds, qualification);

    }
    @Override
    public boolean hasPermissionByTemplateName(String principalId, String namespaceCode, String permissionTemplateName, Map<String, String> permissionDetails) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionTemplateName is null or blank");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }
        return isAuthorizedByTemplateName( principalId, namespaceCode, permissionTemplateName, permissionDetails, Collections.<String, String>emptyMap() );
    }
    @Override
    public boolean isAuthorizedByTemplateName(String principalId, String namespaceCode, String permissionTemplateName, Map<String, String> permissionDetails, Map<String, String> qualification ) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionTemplateName is null or blank");
        }
        if (qualification == null) {
            throw new RiceIllegalArgumentException("qualification is null");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }
        List<String> roleIds = getRoleIdsForPermissionTemplate( namespaceCode, permissionTemplateName, permissionDetails );
    	if ( roleIds.isEmpty() ) {
    		return false;
    	}
    	return roleService.principalHasRole( principalId, roleIds, qualification);
    }
    @Override
    public List<Permission> getAuthorizedPermissions( String principalId, String namespaceCode, String permissionName, Map<String, String> permissionDetails, Map<String, String> qualification ) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }
        if (qualification == null) {
            throw new RiceIllegalArgumentException("qualification is null");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }
        // get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByName( namespaceCode, permissionName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, permissionDetails );
    	return getPermissionsForUser(principalId, applicablePermissions, qualification);
    }
    @Override
    public List<Permission> getAuthorizedPermissionsByTemplateName( String principalId, String namespaceCode, String permissionTemplateName, Map<String, String> permissionDetails, Map<String, String> qualification ) {
        if (StringUtils.isBlank(principalId)) {
            throw new RiceIllegalArgumentException("principalId is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionTemplateName is null or blank");
        }
        if (qualification == null) {
            throw new RiceIllegalArgumentException("qualification is null");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }
        // get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, permissionDetails );
    	return getPermissionsForUser(principalId, applicablePermissions, qualification);
    }
    
    /**
     * Checks the list of permissions against the principal's roles and returns a subset of the list which match.
     */
    protected List<Permission> getPermissionsForUser( String principalId, List<Permission> permissions, Map<String, String> qualification ) {
    	ArrayList<Permission> results = new ArrayList<Permission>();
    	List<Permission> tempList = new ArrayList<Permission>(1);
    	for ( Permission perm : permissions ) {
    		tempList.clear();
    		tempList.add( perm );
    		List<String> roleIds = getRoleIdsForPermissions( tempList );
    		// TODO: This could be made a little better by collecting the role IDs into
    		// a set and then processing the distinct list rather than a check
    		// for every permission
    		if ( roleIds != null && !roleIds.isEmpty() ) {
    			if ( roleService.principalHasRole( principalId, roleIds, qualification) ) {
    				results.add( perm );
    			}
    		}
    	}
    	
    	return Collections.unmodifiableList(results);
    }

    protected Map<String,PermissionTypeService> getPermissionTypeServicesByTemplateId( Collection<PermissionBo> permissions ) {
    	Map<String,PermissionTypeService> permissionTypeServices = new HashMap<String, PermissionTypeService>( permissions.size() );
    	for ( PermissionBo perm : permissions ) {
    		permissionTypeServices.put(perm.getTemplateId(), getPermissionTypeService( perm.getTemplate() ) );    				
    	}
    	return permissionTypeServices;
    }
    
    @SuppressWarnings("static-access")
	protected Map<String,List<Permission>> groupPermissionsByTemplate( Collection<PermissionBo> permissions ) {
    	Map<String,List<Permission>> results = new HashMap<String,List<Permission>>();
    	for ( PermissionBo perm : permissions ) {
    		List<Permission> perms = results.get( perm.getTemplateId() );
    		if ( perms == null ) {
    			perms = new ArrayList<Permission>();
    			results.put( perm.getTemplateId(), perms );
    		}
    		perms.add( PermissionBo.to(perm) );
    	}
    	return results;
    }
    
	/**
     * Compare each of the passed in permissions with the given permissionDetails.  Those that
     * match are added to the result list.
     */
    protected List<Permission> getMatchingPermissions( List<PermissionBo> permissions, Map<String, String> permissionDetails ) {
    	List<Permission> applicablePermissions = new ArrayList<Permission>();    	
    	if ( permissionDetails == null || permissionDetails.isEmpty() ) {
    		// if no details passed, assume that all match
    		for ( PermissionBo perm : permissions ) {
    			applicablePermissions.add( PermissionBo.to(perm) );
    		}
    	} else {
    		// otherwise, attempt to match the permission details
    		// build a map of the template IDs to the type services
    		Map<String,PermissionTypeService> permissionTypeServices = getPermissionTypeServicesByTemplateId( permissions );
    		// build a map of permissions by template ID
    		Map<String,List<Permission>> permissionMap = groupPermissionsByTemplate( permissions );
    		// loop over the different templates, matching all of the same template against the type
    		// service at once
    		for ( Map.Entry<String,List<Permission>> entry : permissionMap.entrySet() ) {
    			PermissionTypeService permissionTypeService = permissionTypeServices.get( entry.getKey() );
    			List<Permission> permissionList = entry.getValue();
				applicablePermissions.addAll( permissionTypeService.getMatchingPermissions( permissionDetails, permissionList ) );    				
    		}
    	}
    	return applicablePermissions;
    }
    @Override
    public List<Assignee> getPermissionAssignees( String namespaceCode, String permissionName, Map<String, String> permissionDetails, Map<String, String> qualification ) {
    	if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }
        if (qualification == null) {
            throw new RiceIllegalArgumentException("qualification is null");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }


    	List<String> roleIds = getRoleIdsForPermission( namespaceCode, permissionName, permissionDetails);
    	if ( roleIds.isEmpty() ) {
    		return Collections.emptyList();
    	}
    	Collection<RoleMembership> roleMembers = roleService.getRoleMembers( roleIds,qualification );
    	List<Assignee> results = new ArrayList<Assignee>();
        for ( RoleMembership rm : roleMembers ) {
			List<DelegateType.Builder> delegateBuilderList = new ArrayList<DelegateType.Builder>();
			if (!rm.getDelegates().isEmpty()) {
    			for (DelegateType delegate : rm.getDelegates()){
                    delegateBuilderList.add(DelegateType.Builder.create(delegate));
    			}
			}
    		if ( rm.getMemberTypeCode().equals( Role.PRINCIPAL_MEMBER_TYPE ) ) {
    			results.add (Assignee.Builder.create(rm.getMemberId(), null, delegateBuilderList).build());
    		} else if ( rm.getMemberTypeCode().equals( Role.GROUP_MEMBER_TYPE ) ) {
    			results.add (Assignee.Builder.create(null, rm.getMemberId(), delegateBuilderList).build());
    		}
    	}
    	return Collections.unmodifiableList(results);
    }
    @Override
    public List<Assignee> getPermissionAssigneesByTemplateName(String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails, Map<String, String> qualification) {
    	if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionTemplateName is null or blank");
        }
        if (qualification == null) {
            throw new RiceIllegalArgumentException("qualification is null");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }

    	List<String> roleIds = getRoleIdsForPermissionTemplate( namespaceCode, permissionTemplateName, permissionDetails);
    	if ( roleIds.isEmpty() ) {
    		return Collections.emptyList();
    	}
    	Collection<RoleMembership> roleMembers = roleService.getRoleMembers( roleIds,qualification);
    	List<Assignee> results = new ArrayList<Assignee>();
        for ( RoleMembership rm : roleMembers ) {
			List<DelegateType.Builder> delegateBuilderList = new ArrayList<DelegateType.Builder>();
			if (!rm.getDelegates().isEmpty()) {
    			for (DelegateType delegate : rm.getDelegates()){
                    delegateBuilderList.add(DelegateType.Builder.create(delegate));
    			}
			}
    		if ( rm.getMemberTypeCode().equals( Role.PRINCIPAL_MEMBER_TYPE ) ) {
    			results.add (Assignee.Builder.create(rm.getMemberId(), null, delegateBuilderList).build());
    		} else { // a group membership
    			results.add (Assignee.Builder.create(null, rm.getMemberId(), delegateBuilderList).build());
    		}
    	}
    	return Collections.unmodifiableList(results);
    }

     @Override
    public boolean isPermissionDefined( String namespaceCode, String permissionName, Map<String, String> permissionDetails ) {
    	if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }

    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByName( namespaceCode, permissionName );
    	// now, filter the full list by the detail passed
    	return !getMatchingPermissions( permissions, permissionDetails ).isEmpty();
    }
    @Override
    public boolean isPermissionDefinedByTemplateName(String namespaceCode, String permissionTemplateName,
            Map<String, String> permissionDetails) {

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }

    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	// now, filter the full list by the detail passed
    	return !getMatchingPermissions( permissions, permissionDetails ).isEmpty();
    }
     @Override
    public List<String> getRoleIdsForPermission( String namespaceCode, String permissionName, Map<String, String> permissionDetails) {

    	if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }
        if (permissionDetails == null) {
            throw new RiceIllegalArgumentException("permissionDetails is null");
        }


    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByName( namespaceCode, permissionName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, permissionDetails );
        return getRoleIdsForPermissions( applicablePermissions );
    }

    protected List<String> getRoleIdsForPermissionTemplate( String namespaceCode, String permissionTemplateName, Map<String, String> permissionDetails ) {
    	// get all the permission objects whose name match that requested
    	List<PermissionBo> permissions = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	// now, filter the full list by the detail passed
    	List<Permission> applicablePermissions = getMatchingPermissions( permissions, permissionDetails );
    	return getRoleIdsForPermissions( applicablePermissions );
    }

    // --------------------
    // Permission Data
    // --------------------
    
    @Override
    public Permission getPermission(String permissionId) {

        if (StringUtils.isBlank(permissionId)) {
            throw new RiceIllegalArgumentException("permissionId is null or blank");
        }

        PermissionBo impl = getPermissionImpl( permissionId );
    	if ( impl != null ) {
    		return PermissionBo.to(impl);
    	}
    	return null;
    }
    
    @Override
    public List<Permission> findPermsByNamespaceCodeTemplateName(String namespaceCode, String permissionTemplateName) {

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionTemplateName is null or blank");
        }

        List<PermissionBo> impls = getPermissionImplsByTemplateName( namespaceCode, permissionTemplateName );
    	List<Permission> results = new ArrayList<Permission>(impls.size());
    	for (PermissionBo impl : impls) {
    	    results.add(PermissionBo.to(impl));
    	}
    	return Collections.unmodifiableList(results);
    }

	protected PermissionBo getPermissionImpl(String permissionId) {
    	if ( StringUtils.isBlank( permissionId ) ) {
    		return null;
    	}
        HashMap<String,Object> pk = new HashMap<String,Object>( 1 );
        pk.put( KimConstants.PrimaryKeyConstants.PERMISSION_ID, permissionId );
        return businessObjectService.findByPrimaryKey( PermissionBo.class, pk );
    }
    
    protected List<PermissionBo> getPermissionImplsByTemplateName( String namespaceCode, String permissionTemplateName ){
        HashMap<String,Object> pk = new HashMap<String,Object>( 3 );
        pk.put( "template.namespaceCode", namespaceCode );
        pk.put( "template.name", permissionTemplateName );
        pk.put( KRADPropertyConstants.ACTIVE, "Y" );
        return ((List<PermissionBo>) businessObjectService.findMatching( PermissionBo.class, pk ));
    }

    protected List<PermissionBo> getPermissionImplsByName( String namespaceCode, String permissionName ) {
        HashMap<String,Object> pk = new HashMap<String,Object>( 3 );
        pk.put( KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode );
        pk.put( KimConstants.UniqueKeyConstants.PERMISSION_NAME, permissionName );
        pk.put( KRADPropertyConstants.ACTIVE, "Y" );
        
        return ((List<PermissionBo>) businessObjectService.findMatching( PermissionBo.class, pk ));
    }
	
    @Override
	public Template getPermissionTemplate(String permissionTemplateId) {
		if (StringUtils.isBlank(permissionTemplateId)) {
            throw new RiceIllegalArgumentException("permissionTemplateId is null or blank");
        }

        PermissionTemplateBo impl = businessObjectService.findBySinglePrimaryKey( PermissionTemplateBo.class, permissionTemplateId );
		if ( impl != null ) {
			return PermissionTemplateBo.to(impl);
		}
		return null;
	}

    @Override
	public Template findPermTemplateByNamespaceCodeAndName(String namespaceCode, String permissionTemplateName) {
		if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionTemplateName)) {
            throw new RiceIllegalArgumentException("permissionTemplateName is null or blank");
        }


        Map<String,String> criteria = new HashMap<String,String>(2);
		criteria.put( KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode );
		criteria.put( KimConstants.UniqueKeyConstants.PERMISSION_TEMPLATE_NAME, permissionTemplateName );
		PermissionTemplateBo impl = businessObjectService.findByPrimaryKey( PermissionTemplateBo.class, criteria );
		if ( impl != null ) {
			return PermissionTemplateBo.to(impl);
		}
		return null;
	}
    @Override
	public List<Template> getAllTemplates() {
		if ( allTemplates.isEmpty() ) {
			Map<String,String> criteria = new HashMap<String,String>(1);
			criteria.put( KRADPropertyConstants.ACTIVE, "Y" );
			List<PermissionTemplateBo> impls = (List<PermissionTemplateBo>) businessObjectService.findMatching( PermissionTemplateBo.class, criteria );
			List<Template> infos = new ArrayList<Template>( impls.size() );
			for ( PermissionTemplateBo impl : impls ) {
				infos.add( PermissionTemplateBo.to(impl) );
			}
			Collections.sort(infos, new Comparator<Template>() {
				@Override public int compare(Template tmpl1,
						Template tmpl2) {

					int result = tmpl1.getNamespaceCode().compareTo(tmpl2.getNamespaceCode());
					if ( result != 0 ) {
						return result;
					}
					result = tmpl1.getName().compareTo(tmpl2.getName());
					return result;
				}
			});
			allTemplates.addAll(infos);
		}
		return Collections.unmodifiableList(allTemplates);
    }


	@Override
	public Permission createPermission(Permission permission)
			throws RiceIllegalArgumentException, RiceIllegalStateException {
        if (permission == null) {
            throw new RiceIllegalArgumentException("permission is null");
        }

        if (StringUtils.isNotBlank(permission.getId()) && getPermission(permission.getId()) != null) {
            throw new RiceIllegalStateException("the permission to create already exists: " + permission);
        }
        List<PermissionAttributeBo> attrBos = Collections.emptyList();
        if (permission.getTemplate() != null) {
            attrBos = KimAttributeDataBo.createFrom(PermissionAttributeBo.class, permission.getAttributes(), permission.getTemplate().getKimTypeId());
        }
        PermissionBo bo = PermissionBo.from(permission);
        if (bo.getTemplate() == null && bo.getTemplateId() != null) {
            bo.setTemplate(PermissionTemplateBo.from(getPermissionTemplate(bo.getTemplateId())));
        }
        bo.setAttributeDetails(attrBos);
        return PermissionBo.to(businessObjectService.save(bo));
	}

	@Override
	public Permission updatePermission(Permission permission)
			throws RiceIllegalArgumentException, RiceIllegalStateException {
        if (permission == null) {
            throw new RiceIllegalArgumentException("permission is null");
        }

        PermissionBo oldPermission = getPermissionImpl(permission.getId());
        if (StringUtils.isBlank(permission.getId()) || oldPermission == null) {
            throw new RiceIllegalStateException("the permission does not exist: " + permission);
        }

        //List<PermissionAttributeBo> attrBos = KimAttributeDataBo.createFrom(PermissionAttributeBo.class, permission.getAttributes(), permission.getTemplate().getKimTypeId());

        List<PermissionAttributeBo> oldAttrBos = oldPermission.getAttributeDetails();
        //put old attributes in map for easier updating
        Map<String, PermissionAttributeBo> oldAttrMap = new HashMap<String, PermissionAttributeBo>();
        for (PermissionAttributeBo oldAttr : oldAttrBos) {
            oldAttrMap.put(oldAttr.getKimAttribute().getAttributeName(), oldAttr);
        }
        List<PermissionAttributeBo> newAttrBos = new ArrayList<PermissionAttributeBo>();
        for (String key : permission.getAttributes().keySet()) {
            if (oldAttrMap.containsKey(key)) {
                PermissionAttributeBo updatedAttr = oldAttrMap.get(key);
                updatedAttr.setAttributeValue(permission.getAttributes().get(key));
                newAttrBos.add(updatedAttr);
            } else { //new attribute
                newAttrBos.addAll(KimAttributeDataBo.createFrom(PermissionAttributeBo.class, Collections.singletonMap(key, permission.getAttributes().get(key)), permission.getTemplate().getKimTypeId()));
            }
        }
        PermissionBo bo = PermissionBo.from(permission);
        if (CollectionUtils.isNotEmpty(newAttrBos)) {
            bo.setAttributeDetails(newAttrBos);
        }
        if (bo.getTemplate() == null && bo.getTemplateId() != null) {
            bo.setTemplate(PermissionTemplateBo.from(getPermissionTemplate(bo.getTemplateId())));
        }

        return PermissionBo.to(businessObjectService.save(bo));		
	}
	
    @Override
    public Permission findPermByNamespaceCodeAndName(String namespaceCode, String permissionName) {

        if (StringUtils.isBlank(namespaceCode)) {
            throw new RiceIllegalArgumentException("namespaceCode is null or blank");
        }

        if (StringUtils.isBlank(permissionName)) {
            throw new RiceIllegalArgumentException("permissionName is null or blank");
        }

        PermissionBo permissionBo = getPermissionBoByName(namespaceCode, permissionName);
        if (permissionBo != null) {
            return PermissionBo.to(permissionBo);
        }
        return null;
    }
    
    protected PermissionBo getPermissionBoByName(String namespaceCode, String permissionName) {
        if (StringUtils.isBlank(namespaceCode)
                || StringUtils.isBlank(permissionName)) {
            return null;
        }
        Map<String, String> criteria = new HashMap<String, String>();
        criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
        criteria.put(KimConstants.UniqueKeyConstants.NAME, permissionName);
        criteria.put(KRADPropertyConstants.ACTIVE, "Y");
        // while this is not actually the primary key - there will be at most one row with these criteria
        return businessObjectService.findByPrimaryKey(PermissionBo.class, criteria);
    }

    @Override
    public PermissionQueryResults findPermissions(final QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
            throw new RiceIllegalArgumentException("queryByCriteria is null");
        }

        LookupCustomizer.Builder<PermissionBo> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        GenericQueryResults<PermissionBo> results = criteriaLookupService.lookup(PermissionBo.class, queryByCriteria, lc.build());

        PermissionQueryResults.Builder builder = PermissionQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Permission.Builder> ims = new ArrayList<Permission.Builder>();
        for (PermissionBo bo : results.getResults()) {
            ims.add(Permission.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public TemplateQueryResults findPermissionTemplates(final QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
            throw new RiceIllegalArgumentException("queryByCriteria is null");
        }

        GenericQueryResults<PermissionTemplateBo> results = criteriaLookupService.lookup(PermissionTemplateBo.class, queryByCriteria);

        TemplateQueryResults.Builder builder = TemplateQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Template.Builder> ims = new ArrayList<Template.Builder>();
        for (PermissionTemplateBo bo : results.getResults()) {
            ims.add(Template.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    private List<String> getRoleIdsForPermissions( Collection<Permission> permissions ) {
        if (CollectionUtils.isEmpty(permissions)) {
            return Collections.emptyList();
        }
        List<String> ids = new ArrayList<String>();
        for (Permission p : permissions) {
            ids.add(p.getId());
        }

        QueryByCriteria query = QueryByCriteria.Builder.fromPredicates(equal("active", "true"), in("permissionId", ids.toArray(new String[]{})));

        GenericQueryResults<RolePermissionBo> results = criteriaLookupService.lookup(RolePermissionBo.class, query);
        List<String> roleIds = new ArrayList<String>();
        for (RolePermissionBo bo : results.getResults()) {
            roleIds.add(bo.getRoleId());
        }
        return Collections.unmodifiableList(roleIds);
    }
	
	/**
     * Sets the kimTypeInfoService attribute value.
     *
     * @param kimTypeInfoService The kimTypeInfoService to set.
     */
	public void setKimTypeInfoService(KimTypeInfoService kimTypeInfoService) {
		this.kimTypeInfoService = kimTypeInfoService;
	}
	
	/**
     * Sets the defaultPermissionTypeService attribute value.
     *
     * @param defaultPermissionTypeService The defaultPermissionTypeService to set.
     */
	public void setDefaultPermissionTypeService(PermissionTypeService defaultPermissionTypeService) {
    	this.defaultPermissionTypeService = defaultPermissionTypeService;
	}
	
	/**
     * Sets the roleService attribute value.
     *
     * @param roleService The roleService to set.
     */
	public void setRoleService(RoleService roleService) {
		this.roleService = roleService;
	}

    /**
     * Sets the businessObjectService attribute value.
     *
     * @param businessObjectService The businessObjectService to set.
     */
    public void setBusinessObjectService(final BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }

    /**
     * Sets the criteriaLookupService attribute value.
     *
     * @param criteriaLookupService The criteriaLookupService to set.
     */
    public void setCriteriaLookupService(final CriteriaLookupService criteriaLookupService) {
        this.criteriaLookupService = criteriaLookupService;
    }
}
