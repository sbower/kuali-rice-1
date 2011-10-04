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

package org.kuali.rice.shareddata.impl.postalcode;

import org.apache.commons.lang.StringUtils;
import org.kuali.rice.core.api.exception.RiceIllegalArgumentException;
import org.kuali.rice.krad.service.BusinessObjectService;
import org.kuali.rice.shareddata.api.postalcode.PostalCode;
import org.kuali.rice.shareddata.api.postalcode.PostalCodeService;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostalCodeServiceImpl implements PostalCodeService {

    private BusinessObjectService businessObjectService;

    @Override
    public PostalCode getPostalCode(String countryCode, String code) {
        if (StringUtils.isBlank(countryCode)) {
            throw new RiceIllegalArgumentException(("countryCode is null"));
        }

        if (StringUtils.isBlank(code)) {
            throw new RiceIllegalArgumentException(("code is null"));
        }

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("countryCode", countryCode);
        map.put("code", code);

        return PostalCodeBo.to(businessObjectService.findByPrimaryKey(PostalCodeBo.class, Collections.unmodifiableMap(map)));
    }

    @Override
    public List<PostalCode> findAllPostalCodesInCountry(String countryCode) {
        if (StringUtils.isBlank(countryCode)) {
            throw new RiceIllegalArgumentException(("countryCode is null"));
        }

        final Map<String, Object> map = new HashMap<String, Object>();
        map.put("countryCode", countryCode);
        map.put("active", Boolean.TRUE);

        final Collection<PostalCodeBo> bos = businessObjectService.findMatching(PostalCodeBo.class, Collections.unmodifiableMap(map));
        if (bos == null) {
            return Collections.emptyList();
        }

        final List<PostalCode> toReturn = new ArrayList<PostalCode>();
        for (PostalCodeBo bo : bos) {
            if (bo != null && bo.isActive()) {
                toReturn.add(PostalCodeBo.to(bo));
            }
        }

        return Collections.unmodifiableList(toReturn);
    }

    public void setBusinessObjectService(BusinessObjectService businessObjectService) {
        this.businessObjectService = businessObjectService;
    }
}
