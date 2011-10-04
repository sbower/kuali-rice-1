/*
 * Copyright 2006-2011 The Kuali Foundation
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

package org.kuali.rice.kim.impl.group;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.criteria.CriteriaLookupService;
import org.kuali.rice.core.api.criteria.GenericQueryResults;
import org.kuali.rice.core.api.criteria.LookupCustomizer;
import org.kuali.rice.core.api.criteria.QueryByCriteria;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.core.api.exception.RiceIllegalStateException;
import org.kuali.rice.core.api.exception.RiceRuntimeException;
import org.kuali.rice.kim.api.KimConstants;
import org.kuali.rice.kim.api.group.Group;
import org.kuali.rice.kim.api.group.GroupMember;
import org.kuali.rice.kim.api.group.GroupMemberQueryResults;
import org.kuali.rice.kim.api.group.GroupQueryResults;
import org.kuali.rice.kim.api.group.GroupService;
import org.kuali.rice.kim.api.services.KimApiServiceLocator;
import org.kuali.rice.kim.impl.KIMPropertyConstants;
import org.kuali.rice.kim.impl.common.attribute.AttributeTransform;
import org.kuali.rice.kim.impl.common.attribute.KimAttributeDataBo;
import org.kuali.rice.kim.impl.services.KimImplServiceLocator;
import org.kuali.rice.krad.service.BusinessObjectService;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.kuali.rice.core.api.criteria.PredicateFactory.*;

public class GroupServiceImpl extends GroupServiceBase implements GroupService {
    private static final Logger LOG = Logger.getLogger(GroupServiceImpl.class);

    protected BusinessObjectService businessObjectService;
    private CriteriaLookupService criteriaLookupService;

    @Override
    public Group getGroup(String groupId) {
        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}
		return GroupBo.to(getGroupBo(groupId));
    }

    @Override
    public List<Group> getGroupsByPrincipalId(String principalId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(principalId) ) {
			 throw new RiceIllegalArgumentException("principalId is blank");
		}
        return getGroupsByPrincipalIdAndNamespaceCodeInternal(principalId, null);
    }

    @Override
    public List<Group> getGroupsByPrincipalIdAndNamespaceCode(String principalId, String namespaceCode) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(principalId) ) {
			 throw new RiceIllegalArgumentException("principalId is blank");
		}

        if ( StringUtils.isEmpty(namespaceCode) ) {
			 throw new RiceIllegalArgumentException("namespaceCode is blank");
		}

		return getGroupsByPrincipalIdAndNamespaceCodeInternal(principalId, namespaceCode);
    }

    protected List<Group> getGroupsByPrincipalIdAndNamespaceCodeInternal(String principalId, String namespaceCode) throws RiceIllegalArgumentException {

        Collection<Group> directGroups = getDirectGroupsForPrincipal( principalId, namespaceCode );
		Set<Group> groups = new HashSet<Group>();
        groups.addAll(directGroups);
		for ( Group group : directGroups ) {
			groups.add( group );
			groups.addAll( getParentGroups( group.getId() ) );
		}
		return Collections.unmodifiableList(new ArrayList<Group>( groups ));
    }

    @Override
    public List<String> findGroupIds(final QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
			 throw new RiceIllegalArgumentException("queryByCriteria is null");
		}

        GroupQueryResults results = this.findGroups(queryByCriteria);
        List<String> result = new ArrayList<String>();

        for (Group group : results.getResults()) {
            result.add(group.getId());
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public boolean isDirectMemberOfGroup(String principalId, String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
        if ( StringUtils.isEmpty(principalId) ) {
			throw new RiceIllegalArgumentException("principalId is blank");
		}
		Map<String,String> criteria = new HashMap<String,String>();
		criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, principalId);
		criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE, KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
		criteria.put(KIMPropertyConstants.GroupMember.GROUP_ID, groupId);

		Collection<GroupMemberBo> groupMembers = businessObjectService.findMatching(GroupMemberBo.class, criteria);
		for ( GroupMemberBo gm : groupMembers ) {
			if ( gm.isActive(new Timestamp(System.currentTimeMillis())) ) {
				return true;
			}
		}
		return false;
    }

    @Override
    public List<String> getGroupIdsByPrincipalId(String principalId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(principalId) ) {
			throw new RiceIllegalArgumentException("principalId is blank");
		}
        return getGroupIdsByPrincipalIdAndNamespaceCodeInternal(principalId, null);
    }

    @Override
    public List<String> getGroupIdsByPrincipalIdAndNamespaceCode(String principalId, String namespaceCode) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(principalId) ) {
			 throw new RiceIllegalArgumentException("principalId is blank");
		}

        if ( StringUtils.isEmpty(namespaceCode) ) {
			 throw new RiceIllegalArgumentException("namespaceCode is blank");
		}

        List<String> result = new ArrayList<String>();

        if (principalId != null) {
            List<Group> groupList = getGroupsByPrincipalIdAndNamespaceCode(principalId, namespaceCode);

            for (Group group : groupList) {
                result.add(group.getId());
            }
        }

        return result;
    }

    protected List<String> getGroupIdsByPrincipalIdAndNamespaceCodeInternal(String principalId, String namespaceCode) throws RiceIllegalArgumentException {

        List<String> result = new ArrayList<String>();

        if (principalId != null) {
            List<Group> groupList = getGroupsByPrincipalIdAndNamespaceCodeInternal(principalId, namespaceCode);

            for (Group group : groupList) {
                result.add(group.getId());
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public List<String> getDirectGroupIdsByPrincipalId(String principalId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(principalId) ) {
			throw new RiceIllegalArgumentException("principalId is blank");
		}
        List<String> result = new ArrayList<String>();

        if (principalId != null) {
        	Collection<Group> groupList = getDirectGroupsForPrincipal(principalId);

            for (Group g : groupList) {
                result.add(g.getId());
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public List<String> getMemberPrincipalIds(String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
		return getMemberPrincipalIdsInternal(groupId, new HashSet<String>());
    }

    @Override
    public List<String> getDirectMemberPrincipalIds(String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
        return this.getMemberIdsByType(getMembersOfGroup(groupId), KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
    }

    @Override
    public List<String> getMemberGroupIds(String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
		List<GroupBo> groups = getMemberGroupBos( groupId );
		ArrayList<String> groupIds = new ArrayList<String>( groups.size() );
		for ( GroupBo group : groups ) {
			if ( group.isActive() ) {
				groupIds.add( group.getId() );
			}
		}
		return Collections.unmodifiableList(groupIds);
    }


	protected List<GroupBo> getMemberGroupBos(String groupId) {
		if ( groupId == null ) {
			return Collections.emptyList();
		}
		Set<GroupBo> groups = new HashSet<GroupBo>();

		GroupBo group = getGroupBo(groupId);
		getMemberGroupsInternal(group, groups);

		return new ArrayList<GroupBo>(groups);
	}

    protected void getMemberGroupsInternal( GroupBo group, Set<GroupBo> groups ) {
		if ( group == null ) {
			return;
		}
		List<String> groupIds = group.getMemberGroupIds();

		for (String id : groupIds) {
			GroupBo memberGroup = getGroupBo(id);
			// if we've already seen that group, don't recurse into it
			if ( memberGroup.isActive() && !groups.contains( memberGroup ) ) {
				groups.add(memberGroup);
				getMemberGroupsInternal(memberGroup,groups);
			}
		}

	}

    @Override
	public boolean isGroupMemberOfGroup(String groupMemberId, String groupId) {
        if ( StringUtils.isEmpty(groupMemberId) ) {
			 throw new RiceIllegalArgumentException("groupMemberId is blank");
		}

        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}
		return isMemberOfGroupInternal(groupMemberId, groupId, new HashSet<String>(), KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);
	}

    @Override
    public boolean isMemberOfGroup(String principalId, String groupId) {
        if ( StringUtils.isEmpty(principalId) ) {
			 throw new RiceIllegalArgumentException("principalId is blank");
		}

        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}

		Set<String> visitedGroupIds = new HashSet<String>();
		return isMemberOfGroupInternal(principalId, groupId, visitedGroupIds, KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
    }

    @Override
    public List<String> getDirectMemberGroupIds(String groupId) {
        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}

        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}
        return this.getMemberIdsByType(getMembersOfGroup(groupId), KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);
    }

    @Override
    public List<String> getParentGroupIds(String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
        List<String> result = new ArrayList<String>();
        if (groupId != null) {
            List<Group> groupList = getParentGroups(groupId);

            for (Group group : groupList) {
                result.add(group.getId());
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public List<String> getDirectParentGroupIds(String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
        List<String> result = new ArrayList<String>();
        if (groupId != null) {
            List<Group> groupList = getDirectParentGroups(groupId);
            for (Group group : groupList) {
                result.add(group.getId());
            }
        }

        return Collections.unmodifiableList(result);
    }

    @Override
    public Map<String, String> getAttributes(String groupId) throws RiceIllegalArgumentException {
        if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}

        if (groupId == null) {
            return Collections.emptyMap();
        }

        Group group = getGroup(groupId);
        if (group != null) {
            return group.getAttributes();
        }
        return Collections.emptyMap();
    }

    @Override
    public List<GroupMember> getMembers(List<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            throw new RiceIllegalArgumentException("groupIds is empty");
		}

        //TODO: PRIME example of something for new Criteria API
        List<GroupMember> groupMembers = new ArrayList<GroupMember>();
        for (String groupId : groupIds) {
              groupMembers.addAll(getMembersOfGroup(groupId));
        }
        return Collections.unmodifiableList(groupMembers);
    }

    @Override
    public List<Group> getGroups(Collection<String> groupIds) {
        if (CollectionUtils.isEmpty(groupIds)) {
            throw new RiceIllegalArgumentException("groupIds is empty");
		}

        final QueryByCriteria.Builder builder = QueryByCriteria.Builder.create();
        builder.setPredicates(and(in("id", groupIds.toArray()), equal("active", "Y")));
        GroupQueryResults qr = findGroups(builder.build());

        return qr.getResults();
    }

    @Override
    public Group getGroupByNameAndNamespaceCode(String namespaceCode, String groupName) {
        if ( StringUtils.isEmpty(namespaceCode) ) {
			 throw new RiceIllegalArgumentException("namespaceCode is blank");
		}

        if ( StringUtils.isEmpty(groupName) ) {
			 throw new RiceIllegalArgumentException("groupName is blank");
		}

		Map<String,String> criteria = new HashMap<String,String>();
		criteria.put(KimConstants.UniqueKeyConstants.NAMESPACE_CODE, namespaceCode);
		criteria.put(KimConstants.UniqueKeyConstants.GROUP_NAME, groupName);
		Collection<GroupBo> groups = businessObjectService.findMatching(GroupBo.class, criteria);
		if ( !groups.isEmpty() ) {
			return GroupBo.to(groups.iterator().next());
		}
		return null;
    }

    @Override
    public GroupQueryResults findGroups(final QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
            throw new RiceIllegalArgumentException("queryByCriteria is null");
        }

        LookupCustomizer.Builder<GroupBo> lc = LookupCustomizer.Builder.create();
        lc.setPredicateTransform(AttributeTransform.getInstance());

        GenericQueryResults<GroupBo> results = criteriaLookupService.lookup(GroupBo.class, queryByCriteria, lc.build());

        GroupQueryResults.Builder builder = GroupQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<Group.Builder> ims = new ArrayList<Group.Builder>();
        for (GroupBo bo : results.getResults()) {
            ims.add(Group.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }

    @Override
    public GroupMemberQueryResults findGroupMembers(final QueryByCriteria queryByCriteria) {
        if (queryByCriteria == null) {
            throw new RiceIllegalArgumentException("queryByCriteria is null");
        }

        GenericQueryResults<GroupMemberBo> results = criteriaLookupService.lookup(GroupMemberBo.class, queryByCriteria);

        GroupMemberQueryResults.Builder builder = GroupMemberQueryResults.Builder.create();
        builder.setMoreResultsAvailable(results.isMoreResultsAvailable());
        builder.setTotalRowCount(results.getTotalRowCount());

        final List<GroupMember.Builder> ims = new ArrayList<GroupMember.Builder>();
        for (GroupMemberBo bo : results.getResults()) {
            ims.add(GroupMember.Builder.create(bo));
        }

        builder.setResults(ims);
        return builder.build();
    }


    protected boolean isMemberOfGroupInternal(String memberId, String groupId, Set<String> visitedGroupIds, String memberType) {
		if ( memberId == null || groupId == null ) {
			return false;
		}

		// when group traversal is not needed
		Group group = getGroup(groupId);
		if ( group == null || !group.isActive() ) {
			return false;
		}

        List<GroupMember> members = getMembersOfGroup(group.getId());
		// check the immediate group
		for (String groupMemberId : getMemberIdsByType(members, memberType)) {
			if (groupMemberId.equals(memberId)) {
				return true;
			}
		}

		// check each contained group, returning as soon as a match is found
		for ( String memberGroupId : getMemberIdsByType(members, KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE) ) {
			if (!visitedGroupIds.contains(memberGroupId)){
				visitedGroupIds.add(memberGroupId);
				if ( isMemberOfGroupInternal( memberId, memberGroupId, visitedGroupIds, memberType ) ) {
					return true;
				}
			}
		}

		// no match found, return false
		return false;
	}

    protected void getParentGroupsInternal( String groupId, Set<Group> groups ) {
		List<Group> parentGroups = getDirectParentGroups( groupId );
		for ( Group group : parentGroups ) {
			if ( !groups.contains( group ) ) {
				groups.add( group );
				getParentGroupsInternal( group.getId(), groups );
			}
		}
	}

    protected List<Group> getDirectParentGroups(String groupId) {
		if ( groupId == null ) {
			return Collections.emptyList();
		}
		Map<String,String> criteria = new HashMap<String,String>();
		criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, groupId);
		criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE, KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);

		List<GroupMemberBo> groupMembers = (List<GroupMemberBo>)businessObjectService.findMatching(GroupMemberBo.class, criteria);
		Set<String> matchingGroupIds = new HashSet<String>();
		// filter to active groups
		for ( GroupMemberBo gm : groupMembers ) {
			if ( gm.isActive(new Timestamp(System.currentTimeMillis())) ) {
				matchingGroupIds.add(gm.getGroupId());
			}
		}
		if (CollectionUtils.isNotEmpty(matchingGroupIds)) {
            return getGroups(matchingGroupIds);
        }
        return Collections.emptyList();
	}

    @Override
    public List<GroupMember> getMembersOfGroup(String groupId) {
        if (groupId == null) {
            throw new RiceIllegalArgumentException("groupId is blank");
		}
        Map<String,String> criteria = new HashMap<String,String>();
		criteria.put(KIMPropertyConstants.GroupMember.GROUP_ID, groupId);

		Collection<GroupMemberBo> groupMembersBos = businessObjectService.findMatching(GroupMemberBo.class, criteria);
        List<GroupMember> groupMembers = new ArrayList<GroupMember>();
        for (GroupMemberBo groupBo : groupMembersBos) {
            if (groupBo.isActive(new Timestamp(System.currentTimeMillis()))){
                groupMembers.add(GroupMemberBo.to(groupBo));
            }
        }
        return Collections.unmodifiableList(groupMembers);
    }

    protected List<String> getMemberIdsByType(Collection<GroupMember> members, String memberType) {
        List<String> membersIds = new ArrayList<String>();
        if (members != null) {
            for (GroupMember member : members) {
                if (member.getTypeCode().equals(memberType)) {
                    membersIds.add(member.getMemberId());
                }
            }
        }
        return Collections.unmodifiableList(membersIds);
    }

    protected GroupBo getGroupBo(String groupId) {
        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}
        return businessObjectService.findBySinglePrimaryKey(GroupBo.class, groupId);

    }


	protected List<Group> getParentGroups(String groupId) throws RiceIllegalArgumentException {
		if ( StringUtils.isEmpty(groupId) ) {
			throw new RiceIllegalArgumentException("groupId is blank");
		}
		Set<Group> groups = new HashSet<Group>();
		getParentGroupsInternal( groupId, groups );
		return new ArrayList<Group>( groups );
	}

    protected List<String> getMemberPrincipalIdsInternal(String groupId, Set<String> visitedGroupIds) {
		if ( groupId == null ) {
			return Collections.emptyList();
		}
		Set<String> ids = new HashSet<String>();
		GroupBo group = getGroupBo(groupId);
		if ( group == null || !group.isActive()) {
			return Collections.emptyList();
		}

        //List<String> memberIds = getMemberIdsByType(group, memberType);
        //List<GroupMember> members = new ArrayList<GroupMember>(getMembersOfGroup(group.getId()));
		ids.addAll( group.getMemberPrincipalIds());
		visitedGroupIds.add(group.getId());

		for (String memberGroupId : group.getMemberGroupIds()) {
			if (!visitedGroupIds.contains(memberGroupId)){
				ids.addAll(getMemberPrincipalIdsInternal(memberGroupId, visitedGroupIds));
			}
		}

		return Collections.unmodifiableList(new ArrayList<String>(ids));
	}

    protected Collection<Group> getDirectGroupsForPrincipal( String principalId ) {
		return getDirectGroupsForPrincipal( principalId, null );
	}

	protected Collection<Group> getDirectGroupsForPrincipal( String principalId, String namespaceCode ) {
		if ( principalId == null ) {
			return Collections.emptyList();
		}
		Map<String,Object> criteria = new HashMap<String,Object>();
		criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, principalId);
		criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE, KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
		Collection<GroupMemberBo> groupMembers = businessObjectService.findMatching(GroupMemberBo.class, criteria);
		Set<String> groupIds = new HashSet<String>( groupMembers.size() );
		// only return the active members
		for ( GroupMemberBo gm : groupMembers ) {
			if ( gm.isActive(new Timestamp(System.currentTimeMillis())) ) {
				groupIds.add( gm.getGroupId() );
			}
		}
		// pull all the group information for the matching members
		List<Group> groups = CollectionUtils.isEmpty(groupIds) ? Collections.<Group>emptyList() : getGroups(groupIds);
		List<Group> result = new ArrayList<Group>( groups.size() );
		// filter by namespace if necessary
		for ( Group group : groups ) {
			if ( group.isActive() ) {
				if ( StringUtils.isBlank(namespaceCode) || StringUtils.equals(namespaceCode, group.getNamespaceCode() ) ) {
					result.add(group);
				}
			}
		}
		return result;
	}

    @Override
    public boolean addGroupToGroup(String childId, String parentId) {
        if ( StringUtils.isEmpty(childId) ) {
			 throw new RiceIllegalArgumentException("childId is blank");
		}

        if ( StringUtils.isEmpty(parentId) ) {
			 throw new RiceIllegalArgumentException("parentId is blank");
		}

        if(childId.equals(parentId)) {
            throw new RiceIllegalArgumentException("Can't add group to itself.");
        }

        if(isGroupMemberOfGroup(parentId, childId)) {
            throw new RiceIllegalArgumentException("Circular group reference.");
        }

        GroupMemberBo groupMember = new GroupMemberBo();
        groupMember.setGroupId(parentId);
        groupMember.setTypeCode(KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);
        groupMember.setMemberId(childId);

        this.businessObjectService.save(groupMember);
        return true;
    }

    @Override
    public boolean addPrincipalToGroup(String principalId, String groupId) {
        if ( StringUtils.isEmpty(principalId) ) {
			 throw new RiceIllegalArgumentException("principalId is blank");
		}

        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}

        GroupMemberBo groupMember = new GroupMemberBo();
        groupMember.setGroupId(groupId);
        groupMember.setTypeCode(KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);
        groupMember.setMemberId(principalId);

        groupMember = this.businessObjectService.save(groupMember);
        KimImplServiceLocator.getGroupInternalService().updateForUserAddedToGroup(groupMember.getMemberId(),
                groupMember.getGroupId());
        return true;
    }

     @Override
    public Group createGroup(Group group) {
        if (group == null) {
            throw new RiceIllegalArgumentException(("group is null"));
        }
        if (StringUtils.isNotBlank(group.getId()) && getGroup(group.getId()) != null) {
            throw new RiceIllegalStateException("the group to create already exists: " + group);
        }
        List<GroupAttributeBo> attrBos = KimAttributeDataBo
                .createFrom(GroupAttributeBo.class, group.getAttributes(), group.getKimTypeId());
        if (StringUtils.isNotEmpty(group.getId())) {
            for (GroupAttributeBo attr : attrBos) {
                attr.setAssignedToId(group.getId());
            }
        }
        GroupBo bo = GroupBo.from(group);
        bo.setAttributeDetails(attrBos);

        bo = saveGroup(bo);

        return GroupBo.to(bo);
    }

     @Override
    public Group updateGroup(Group group) {
        if (group == null) {
            throw new RiceIllegalArgumentException("group is null");
        }
        GroupBo origGroup = getGroupBo(group.getId());
        if (StringUtils.isBlank(group.getId()) || origGroup == null) {
            throw new RiceIllegalStateException("the group does not exist: " + group);
        }
        List<GroupAttributeBo> attrBos = KimAttributeDataBo.createFrom(GroupAttributeBo.class, group.getAttributes(), group.getKimTypeId());
        GroupBo bo = GroupBo.from(group);
        bo.setMembers(origGroup.getMembers());
        bo.setAttributeDetails(attrBos);

        bo = saveGroup(bo);

        return GroupBo.to(bo);
    }

    @Override
	public Group updateGroup(String groupId, Group group) {
        if (group == null) {
            throw new RiceIllegalArgumentException(("group is null"));
        }
        if (StringUtils.isEmpty(groupId)) {
            throw new RiceIllegalArgumentException(("groupId is empty"));
        }

        if (StringUtils.equals(groupId, group.getId())) {
            return updateGroup(group);
        }

        //if group Ids are different, inactivate old group, and create new with new id based off old
        GroupBo groupBo = getGroupBo(groupId);

        if (StringUtils.isBlank(group.getId()) || groupBo == null) {
            throw new RiceIllegalStateException("the group does not exist: " + group);
        }

        //create and save new group
        GroupBo newGroup = GroupBo.from(group);
        newGroup.setMembers(groupBo.getMembers());
        List<GroupAttributeBo> attrBos = KimAttributeDataBo.createFrom(GroupAttributeBo.class, group.getAttributes(), group.getKimTypeId());
        newGroup.setAttributeDetails(attrBos);
        newGroup = saveGroup(newGroup);

        //inactivate and save old group
        groupBo.setActive(false);
        saveGroup(groupBo);

        return GroupBo.to(newGroup);
    }

 @Override
   public void removeAllMembers(String groupId) {
	    if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}


       GroupService groupService = KimApiServiceLocator.getGroupService();
       List<String> memberPrincipalsBefore = groupService.getMemberPrincipalIds(groupId);

       Collection<GroupMemberBo> toDeactivate = getActiveGroupMembers(groupId, null, null);
       java.sql.Timestamp today = new java.sql.Timestamp(System.currentTimeMillis());

       // Set principals as inactive
        for (GroupMemberBo aToDeactivate : toDeactivate) {
            aToDeactivate.setActiveToDateValue(today);
        }

       // Save
       this.businessObjectService.save(new ArrayList<GroupMemberBo>(toDeactivate));
       List<String> memberPrincipalsAfter = groupService.getMemberPrincipalIds(groupId);

       if (!CollectionUtils.isEmpty(memberPrincipalsAfter)) {
    	   // should never happen!
    	   LOG.warn("after attempting removal of all members, group with id '" + groupId + "' still has principal members");
       }

       // do updates
       KimImplServiceLocator.getGroupInternalService().updateForWorkgroupChange(groupId, memberPrincipalsBefore,
               memberPrincipalsAfter);
   }

    @Override
    public boolean removeGroupFromGroup(String childId, String parentId) {
    	if ( StringUtils.isEmpty(childId) ) {
			 throw new RiceIllegalArgumentException("childId is blank");
		}

        if ( StringUtils.isEmpty(parentId) ) {
			 throw new RiceIllegalArgumentException("parentId is blank");
		}

        java.sql.Timestamp today = new java.sql.Timestamp(System.currentTimeMillis());

    	List<GroupMemberBo> groupMembers =
    		getActiveGroupMembers(parentId, childId, KimConstants.KimGroupMemberTypes.GROUP_MEMBER_TYPE);

        if(groupMembers.size() == 1) {
        	GroupMemberBo groupMember = groupMembers.get(0);
        	groupMember.setActiveToDateValue(today);
            this.businessObjectService.save(groupMember);
            return true;
        }

        return false;
    }

    @Override
    public boolean removePrincipalFromGroup(String principalId, String groupId) {
    	if ( StringUtils.isEmpty(principalId) ) {
			 throw new RiceIllegalArgumentException("principalId is blank");
		}

        if ( StringUtils.isEmpty(groupId) ) {
			 throw new RiceIllegalArgumentException("groupId is blank");
		}

        List<GroupMemberBo> groupMembers =
    		getActiveGroupMembers(groupId, principalId, KimConstants.KimGroupMemberTypes.PRINCIPAL_MEMBER_TYPE);

        if(groupMembers.size() == 1) {
        	GroupMemberBo member = groupMembers.iterator().next();
        	member.setActiveToDateValue(new java.sql.Timestamp(System.currentTimeMillis()));
        	this.businessObjectService.save(member);
            KimImplServiceLocator.getGroupInternalService().updateForUserRemovedFromGroup(member.getMemberId(),
                    member.getGroupId());
            return true;
        }

        return false;
    }

	protected GroupBo saveGroup(GroupBo group) {
		if ( group == null ) {
			return null;
		} else if (group.getId() != null) {
			// Get the version of the group that is in the DB
			GroupBo oldGroup = getGroupBo(group.getId());

			if (oldGroup != null) {
				// Inactivate and re-add members no longer in the group (in order to preserve history).
				java.sql.Timestamp activeTo = new java.sql.Timestamp(System.currentTimeMillis());
				List<GroupMemberBo> toReAdd = null;

				if (oldGroup.getMembers() != null) {
                    for (GroupMemberBo member : oldGroup.getMembers()) {
                        // if the old member isn't in the new group
                        if (group.getMembers() == null || !group.getMembers().contains(member)) {
                            // inactivate the member
                            member.setActiveToDateValue(activeTo);
                            if (toReAdd == null) {
                                toReAdd = new ArrayList<GroupMemberBo>();
                            }
                            // queue it up for re-adding
                            toReAdd.add(member);
                        }
                    }
				}

				// do the re-adding
				if (toReAdd != null) {
					List<GroupMemberBo> groupMembers = group.getMembers();
					if (groupMembers == null) {
                        groupMembers = new ArrayList<GroupMemberBo>(toReAdd.size());
                    }
					group.setMembers(groupMembers);
				}
			}
		}

		return KimImplServiceLocator.getGroupInternalService().saveWorkgroup(group);
	}


	/**
	 * This helper method gets the active group members of the specified type (see {@link org.kuali.rice.kim.api.KimConstants.KimGroupMemberTypes}).
	 * If the optional params are null, it will return all active members for the specified group regardless
	 * of type.
	 *
	 * @param parentId
	 * @param childId optional, but if provided then memberType must be too
	 * @param memberType optional, but must be provided if childId is
     * @return a list of group members
	 */
	private List<GroupMemberBo> getActiveGroupMembers(String parentId,
			String childId, String memberType) {
    	final java.sql.Date today = new java.sql.Date(System.currentTimeMillis());

    	if (childId != null && memberType == null) {
            throw new RiceRuntimeException("memberType must be non-null if childId is non-null");
        }

		Map<String,Object> criteria = new HashMap<String,Object>(4);
        criteria.put(KIMPropertyConstants.GroupMember.GROUP_ID, parentId);

        if (childId != null) {
        	criteria.put(KIMPropertyConstants.GroupMember.MEMBER_ID, childId);
        	criteria.put(KIMPropertyConstants.GroupMember.MEMBER_TYPE_CODE, memberType);
        }

        Collection<GroupMemberBo> groupMembers = this.businessObjectService.findMatching(GroupMemberBo.class, criteria);

        CollectionUtils.filter(groupMembers, new Predicate() {
			@Override public boolean evaluate(Object object) {
				GroupMemberBo member = (GroupMemberBo) object;
				// keep in the collection (return true) if the activeToDate is null, or if it is set to a future date
				return member.getActiveToDate() == null || today.before(member.getActiveToDate().toDate());
			}
		});

        return new ArrayList<GroupMemberBo>(groupMembers);
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
