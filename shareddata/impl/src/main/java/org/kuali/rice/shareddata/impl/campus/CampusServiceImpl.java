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

package org.kuali.rice.shareddata.impl.campus;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.shareddata.api.campus.Campus;
import org.kuali.rice.shareddata.api.campus.CampusService;
import org.kuali.rice.shareddata.api.campus.CampusType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.singletonMap;

public class CampusServiceImpl implements CampusService {

	private BusinessObjectService boService;
	
	/**
     * @see org.kuali.rice.shareddata.api.campus.CampusService#getCampus(String code)
     */
	@Override
	public Campus getCampus(String code) {
		if (StringUtils.isBlank(code)) {
            throw new RiceIllegalArgumentException("code is blank");
        }

        CampusBo campusBo = boService.findByPrimaryKey(CampusBo.class, singletonMap("code", code));

        return CampusBo.to(campusBo);
	}

	/**
     * @see org.kuali.rice.shareddata.api.campus.CampusService#findAllCampuses
     */
	@Override
	public List<Campus> findAllCampuses() {
		List<CampusBo> campusBos = (List<CampusBo>) boService.findMatching(CampusBo.class, singletonMap("active", "true"));

        return this.convertListOfCampusBosToImmutables(campusBos);
	}

	/**
     * @see org.kuali.rice.shareddata.api.campus.CampusService#getCampusType(String code)
     */
	@Override
	public CampusType getCampusType(String code) {
		if (StringUtils.isBlank(code)) {
            throw new RiceIllegalArgumentException("code is blank");
        }

		CampusTypeBo campusTypeBo = boService.findByPrimaryKey(CampusTypeBo.class, singletonMap("code", code));

        return CampusTypeBo.to(campusTypeBo);
	}

	/**
     * @see org.kuali.rice.shareddata.api.campus.CampusService#findAllCampusTypes
     */
	@Override
	public List<CampusType> findAllCampusTypes() {
		List<CampusTypeBo> campusTypeBos = (List<CampusTypeBo>)boService.findMatching(CampusTypeBo.class, singletonMap("active", "true"));
		return this.convertListOfCampusTypesBosToImmutables(campusTypeBos);
	}
    
    public void setBusinessObjectService(BusinessObjectService boService) {
        this.boService = boService;
    }

    private List<Campus> convertListOfCampusBosToImmutables(List<CampusBo> campusBos) {
        ArrayList<Campus> campuses = new ArrayList<Campus>();
        for (CampusBo bo : campusBos) {
            Campus campus = CampusBo.to(bo);
            campuses.add(campus) ;
        }
        return Collections.unmodifiableList(campuses);
    }
    
    private List<CampusType> convertListOfCampusTypesBosToImmutables(List<CampusTypeBo> campusTypeBos) {
        ArrayList<CampusType> campusTypes = new ArrayList<CampusType>();
        for (CampusTypeBo bo : campusTypeBos) {
            CampusType campusType = CampusTypeBo.to(bo);
            campusTypes.add(campusType) ;
        }
        return Collections.unmodifiableList(campusTypes);
    }
}
