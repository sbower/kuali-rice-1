<%--
  ~ Copyright 2006-2011 The Kuali Foundation
  ~
  ~ Licensed under the Educational Community License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~ http://www.opensource.org/licenses/ecl2.php
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
  --%>

<%@ include file="/rice-portal/jsp/sys/riceTldHeader.jsp"%>

<channel:portalChannelTop channelTitle="Sample Travel Application" />
<div class="body">
  
  <ul class="chan">
	 <li><portal:portalLink displayTitle="true" title="Create New Sample Application Travel Request (KualiDocumentActionBase)" url="${ConfigProperties.application.url}/travelDocument2.do?methodToCall=docHandler&command=initiate&docTypeName=TravelRequest" /></li>
  	 <li><portal:portalLink displayTitle="true" title="Travel Account" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=edu.sampleu.travel.bo.TravelAccount&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&docFormKey=88888888" /></li>
  	 <li><portal:portalLink displayTitle="true" title="Travel Fiscal Officer" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=edu.sampleu.travel.bo.FiscalOfficer&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&docFormKey=88888888" /></li>
  	 <li><portal:portalLink displayTitle="true" title="Travel Account Type" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=edu.sampleu.travel.bo.TravelAccountType&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&docFormKey=88888888" /></li>
  	 <li><portal:portalLink displayTitle="true" title="Travel Account Use Rate" url="${ConfigProperties.kr.url}/lookup.do?methodToCall=start&businessObjectClassName=edu.sampleu.travel.bo.TravelAccountUseRate&returnLocation=${ConfigProperties.application.url}/portal.do&hideReturnLink=true&docFormKey=88888888" /></li>
  </ul>

  
</div>
<channel:portalChannelBottom />
