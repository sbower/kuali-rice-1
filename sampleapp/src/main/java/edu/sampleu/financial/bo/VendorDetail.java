/*
 * Copyright 2007 The Kuali Foundation
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

package edu.sampleu.financial.bo;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.kuali.rice.core.api.util.type.KualiDecimal;
import org.kuali.rice.krad.bo.PersistableBusinessObjectBase;
import org.kuali.rice.krad.util.ObjectUtils;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

/**
 * Contains all data for a specific parent or division Vendor, including a link to the <code>VendorHeader</code>, which only
 * contains information about the parent company, but can be shared between division Vendors.
 *
 */
public class VendorDetail extends PersistableBusinessObjectBase { //implements VendorRoutingComparable {
    private static Logger LOG = Logger.getLogger(VendorDetail.class);

    private Integer vendorHeaderGeneratedIdentifier;
    private Integer vendorDetailAssignedIdentifier;
    private String vendorNumber; // not persisted in the db
    private boolean vendorParentIndicator;
    private String vendorName;
    private String vendorFirstName; // not persisted in the db
    private String vendorLastName; // not persisted in the db
    private String vendorStateForLookup; // not persisted in the db

    private boolean activeIndicator;
    private String vendorInactiveReasonCode;
    private String vendorDunsNumber;
    private String vendorPaymentTermsCode;
    private String vendorShippingTitleCode;
    private String vendorShippingPaymentTermsCode;
    private Boolean vendorConfirmationIndicator;
    private Boolean vendorPrepaymentIndicator;
    private Boolean vendorCreditCardIndicator;
    private KualiDecimal vendorMinimumOrderAmount;
    private String vendorUrlAddress;
    private String vendorRemitName;
    private Boolean vendorRestrictedIndicator;
    private String vendorRestrictedReasonText;
    private Date vendorRestrictedDate;
    //    private String vendorRestrictedPersonIdentifier;
    private String vendorSoldToNumber; // not persisted in the db
    private Integer vendorSoldToGeneratedIdentifier;
    private Integer vendorSoldToAssignedIdentifier;
    private String vendorSoldToName;
    private boolean vendorFirstLastNameIndicator;
    private boolean taxableIndicator;

    private List<VendorAddress> vendorAddresses;

//    private List<VendorAlias> vendorAliases;
//    private List<VendorContact> vendorContacts;
//    private List<VendorCustomerNumber> vendorCustomerNumbers;
//    private List<VendorPhoneNumber> vendorPhoneNumbers;
//    private List<VendorShippingSpecialCondition> vendorShippingSpecialConditions;
//    private List<VendorCommodityCode> vendorCommodities;

    private VendorHeader vendorHeader;
    //    private VendorInactiveReason vendorInactiveReason;
    private PaymentTermType vendorPaymentTerms;
    //    private ShippingTitle vendorShippingTitle;
    private ShippingPaymentTerms vendorShippingPaymentTerms;
    private VendorDetail soldToVendorDetail;
//    private Person vendorRestrictedPerson;

    private String vendorParentName; // not persisted in the db
    private String defaultAddressLine1; // not persisted in the db
    private String defaultAddressLine2; // not persisted in the db
    private String defaultAddressCity; // not persisted in the db
    private String defaultAddressStateCode; // not persisted in the db
    private String defaultAddressInternationalProvince; // not persisted in the db
    private String defaultAddressPostalCode; // not persisted in the db
    private String defaultAddressCountryCode; // not persisted in the db
    private String defaultFaxNumber; // not persisted in the db

    /**
     * Default constructor.
     */
    public VendorDetail() {
        super();

        vendorHeader = new VendorHeader();
        vendorAddresses = new ArrayList();
        /*vendorAliases = new TypedArrayList(VendorAlias.class);
        vendorContacts = new TypedArrayList(VendorContact.class);
        vendorContracts = new TypedArrayList(VendorContract.class);
        vendorCustomerNumbers = new TypedArrayList(VendorCustomerNumber.class);
        vendorPhoneNumbers = new TypedArrayList(VendorPhoneNumber.class);
        vendorShippingSpecialConditions = new TypedArrayList(VendorShippingSpecialCondition.class);
        vendorCommodities = new TypedArrayList(VendorCommodityCode.class);*/
        vendorParentIndicator = true;

    }

    public Integer getVendorHeaderGeneratedIdentifier() {
        return vendorHeaderGeneratedIdentifier;
    }

    public void setVendorHeaderGeneratedIdentifier(Integer vendorHeaderGeneratedIdentifier) {
        this.vendorHeaderGeneratedIdentifier = vendorHeaderGeneratedIdentifier;
    }

    public Integer getVendorDetailAssignedIdentifier() {
        return vendorDetailAssignedIdentifier;
    }

    public void setVendorDetailAssignedIdentifier(Integer vendorDetailAssignedIdentifier) {
        this.vendorDetailAssignedIdentifier = vendorDetailAssignedIdentifier;
    }

    /**
     * A concatenation of the vendorHeaderGeneratedIdentifier, a dash, and the vendorDetailAssignedIdentifier
     *
     * @return Returns the vendorNumber.
     */
    public String getVendorNumber() {
        String headerId = "";
        String detailId = "";
        String vendorNumber = "";
        if (ObjectUtils.isNotNull(this.vendorHeaderGeneratedIdentifier)) {
            headerId = this.vendorHeaderGeneratedIdentifier.toString();
        }
        if (ObjectUtils.isNotNull(this.vendorDetailAssignedIdentifier)) {
            detailId = this.vendorDetailAssignedIdentifier.toString();
        }
        if (!StringUtils.isEmpty(headerId) && !StringUtils.isEmpty(detailId)) {
            vendorNumber = headerId + "-" + detailId;
        }

        return vendorNumber;
    }

    /**
     * Sets the vendorNumber attribute value.
     * If vendorNumber is empty, clears header and detail IDs.
     * If vendorNumber is invalid, leaves header and detail IDs as were.
     *
     * @param vendorNumber The vendorNumber to set.
     */
    public void setVendorNumber(String vendorNumber) {
        this.vendorNumber = vendorNumber;

        if (StringUtils.isEmpty(vendorNumber)) {
            vendorHeaderGeneratedIdentifier = null;
            vendorDetailAssignedIdentifier = null;
            return;
        }

        int dashInd = vendorNumber.indexOf('-');
        // make sure there's at least one char before and after '-'
        if (dashInd > 0 && dashInd < vendorNumber.length() - 1) {
            try {
                vendorHeaderGeneratedIdentifier = new Integer(vendorNumber.substring(0, dashInd));
                vendorDetailAssignedIdentifier = new Integer(vendorNumber.substring(dashInd + 1));
            } catch (NumberFormatException e) {
                // in case of invalid number format
            }
        }
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public Integer getVendorSoldToGeneratedIdentifier() {
        return vendorSoldToGeneratedIdentifier;
    }

    public void setVendorSoldToGeneratedIdentifier(Integer vendorSoldToGeneratedIdentifier) {
        this.vendorSoldToGeneratedIdentifier = vendorSoldToGeneratedIdentifier;
    }

    public Integer getVendorSoldToAssignedIdentifier() {
        return vendorSoldToAssignedIdentifier;
    }

    public void setVendorSoldToAssignedIdentifier(Integer vendorSoldToAssignedIdentifier) {
        this.vendorSoldToAssignedIdentifier = vendorSoldToAssignedIdentifier;
    }

    /**
     * Gets the vendorSoldToNumber attribute.
     *
     * @return Returns the vendorSoldToNumber.
     */
    public String getVendorSoldToNumber() {
        String headerId = "";
        String detailId = "";
        String vendorSoldToNumber = "";

        if (this.vendorSoldToGeneratedIdentifier != null) {
            headerId = this.vendorSoldToGeneratedIdentifier.toString();
        }
        if (this.vendorSoldToAssignedIdentifier != null) {
            detailId = this.vendorSoldToAssignedIdentifier.toString();
        }
        if (!StringUtils.isEmpty(headerId) && !StringUtils.isEmpty(detailId)) {
            vendorSoldToNumber = headerId + "-" + detailId;
        }

        return vendorSoldToNumber;
    }

    /**
     * Sets the vendorSoldToNumber attribute value.
     * If vendorSoldToNumber is empty, clears soldToVendor header and detail IDs.
     * If vendorSoldToNumber is invalid, leaves soldToVendor header and detail IDs as were.
     *
     * @param vendorSoldToNumber The vendorSoldToNumber to set.
     */
    public void setVendorSoldToNumber(String vendorSoldToNumber) {
        this.vendorSoldToNumber = vendorSoldToNumber;

        if (StringUtils.isEmpty(vendorSoldToNumber)) {
            vendorSoldToGeneratedIdentifier = null;
            vendorSoldToAssignedIdentifier = null;
            return;
        }

        int dashInd = vendorSoldToNumber.indexOf('-');
        // make sure there's at least one char before and after '-'
        if (dashInd > 0 && dashInd < vendorSoldToNumber.length() - 1) {
            try {
                vendorSoldToGeneratedIdentifier = new Integer(vendorSoldToNumber.substring(0, dashInd));
                vendorSoldToAssignedIdentifier = new Integer(vendorSoldToNumber.substring(dashInd + 1));
            } catch (NumberFormatException e) {
                // in case of invalid number format
            }
        }
    }

    /**
     * Gets the vendorSoldToName attribute.
     *
     * @return Returns the vendorSoldToName
     */
    public String getVendorSoldToName() {
        return this.vendorSoldToName;
    }

    public void setVendorSoldToName(String vendorSoldToName) {
        this.vendorSoldToName = vendorSoldToName;
    }

    public String getAltVendorName() {
        return vendorName;
    }

    public void setAltVendorName(String altVendorName) {
        this.vendorName = altVendorName;
    }

    public String getVendorRemitName() {
        return vendorRemitName;
    }

    public void setVendorRemitName(String vendorRemitName) {
        this.vendorRemitName = vendorRemitName;
    }

    public boolean isVendorParentIndicator() {
        return vendorParentIndicator;
    }

    public void setVendorParentIndicator(boolean vendorParentIndicator) {
        this.vendorParentIndicator = vendorParentIndicator;
    }

    public boolean isTaxableIndicator() {
        return taxableIndicator;
    }

    public void setTaxableIndicator(boolean taxableIndicator) {
        this.taxableIndicator = taxableIndicator;
    }

    public boolean isVendorDebarred() {
        return (ObjectUtils.isNotNull(getVendorHeader().getVendorDebarredIndicator()) && getVendorHeader().getVendorDebarredIndicator());
    }

    public boolean isActiveIndicator() {
        return activeIndicator;
    }

    public void setActiveIndicator(boolean activeIndicator) {
        this.activeIndicator = activeIndicator;
    }

//    public String getVendorInactiveReasonCode() {
//        return vendorInactiveReasonCode;
//    }
//
//    public void setVendorInactiveReasonCode(String vendorInactiveReasonCode) {
//        this.vendorInactiveReasonCode = vendorInactiveReasonCode;
//    }

    public String getVendorPaymentTermsCode() {
        return vendorPaymentTermsCode;
    }

    public void setVendorPaymentTermsCode(String vendorPaymentTermsCode) {
        this.vendorPaymentTermsCode = vendorPaymentTermsCode;
    }

//    public String getVendorShippingTitleCode() {
//        return vendorShippingTitleCode;
//    }
//
//    public void setVendorShippingTitleCode(String vendorShippingTitleCode) {
//        this.vendorShippingTitleCode = vendorShippingTitleCode;
//    }

    public String getVendorShippingPaymentTermsCode() {
        return vendorShippingPaymentTermsCode;
    }

    public void setVendorShippingPaymentTermsCode(String vendorShippingPaymentTermsCode) {
        this.vendorShippingPaymentTermsCode = vendorShippingPaymentTermsCode;
    }

    public Boolean getVendorConfirmationIndicator() {
        return vendorConfirmationIndicator;
    }

    public void setVendorConfirmationIndicator(Boolean vendorConfirmationIndicator) {
        this.vendorConfirmationIndicator = vendorConfirmationIndicator;
    }

    public Boolean getVendorPrepaymentIndicator() {
        return vendorPrepaymentIndicator;
    }

    public void setVendorPrepaymentIndicator(Boolean vendorPrepaymentIndicator) {
        this.vendorPrepaymentIndicator = vendorPrepaymentIndicator;
    }

    public Boolean getVendorCreditCardIndicator() {
        return vendorCreditCardIndicator;
    }

    public void setVendorCreditCardIndicator(Boolean vendorCreditCardIndicator) {
        this.vendorCreditCardIndicator = vendorCreditCardIndicator;
    }

    public KualiDecimal getVendorMinimumOrderAmount() {
        return vendorMinimumOrderAmount;
    }

    public void setVendorMinimumOrderAmount(KualiDecimal vendorMinimumOrderAmount) {
        this.vendorMinimumOrderAmount = vendorMinimumOrderAmount;
    }

    public String getVendorUrlAddress() {
        return vendorUrlAddress;
    }

    public void setVendorUrlAddress(String vendorUrlAddress) {
        this.vendorUrlAddress = vendorUrlAddress;
    }

    public Boolean getVendorRestrictedIndicator() {
        return vendorRestrictedIndicator;
    }

    public void setVendorRestrictedIndicator(Boolean vendorRestrictedIndicator) {
        this.vendorRestrictedIndicator = vendorRestrictedIndicator;
    }

    public String getVendorRestrictedReasonText() {
        return vendorRestrictedReasonText;
    }

    public void setVendorRestrictedReasonText(String vendorRestrictedReasonText) {
        this.vendorRestrictedReasonText = vendorRestrictedReasonText;
    }

    public Date getVendorRestrictedDate() {
        return vendorRestrictedDate;
    }

    public void setVendorRestrictedDate(Date vendorRestrictedDate) {
        this.vendorRestrictedDate = vendorRestrictedDate;
    }

    /*public String getVendorRestrictedPersonIdentifier() {
        return vendorRestrictedPersonIdentifier;
    }

    public void setVendorRestrictedPersonIdentifier(String vendorRestrictedPersonIdentifier) {
        this.vendorRestrictedPersonIdentifier = vendorRestrictedPersonIdentifier;
    }*/

    public String getVendorDunsNumber() {
        return vendorDunsNumber;
    }

    public void setVendorDunsNumber(String vendorDunsNumber) {
        this.vendorDunsNumber = vendorDunsNumber;
    }

    public VendorHeader getVendorHeader() {
        return vendorHeader;
    }

    public void setVendorHeader(VendorHeader vendorHeader) {
        this.vendorHeader = vendorHeader;
    }

    public PaymentTermType getVendorPaymentTerms() {
        return vendorPaymentTerms;
    }

    /**
     * Sets the vendorPaymentTerms attribute.
     *
     * @param vendorPaymentTerms The vendorPaymentTerms to set.
     * @deprecated
     */
    public void setVendorPaymentTerms(PaymentTermType vendorPaymentTerms) {
        this.vendorPaymentTerms = vendorPaymentTerms;
    }

//    public ShippingTitle getVendorShippingTitle() {
//        return vendorShippingTitle;
//    }

    /**
     * Sets the vendorShippingTitle attribute.
     *
     * @param vendorShippingTitle The vendorShippingTitle to set.
     * @deprecated
     */
//    public void setVendorShippingTitle(ShippingTitle vendorShippingTitle) {
//        this.vendorShippingTitle = vendorShippingTitle;
//    }
    public ShippingPaymentTerms getVendorShippingPaymentTerms() {
        return vendorShippingPaymentTerms;
    }

    /**
     * Sets the vendorShippingPaymentTerms attribute.
     *
     * @param vendorShippingPaymentTerms The vendorShippingPaymentTerms to set.
     * @deprecated
     */
    public void setVendorShippingPaymentTerms(ShippingPaymentTerms vendorShippingPaymentTerms) {
        this.vendorShippingPaymentTerms = vendorShippingPaymentTerms;
    }

//    public VendorInactiveReason getVendorInactiveReason() {
//        return vendorInactiveReason;
//    }

    /**
     * Sets the vendorInactiveReason attribute value.
     *
     * @param vendorInactiveReason The vendorInactiveReason to set.
     * @deprecated
     */
//    public void setVendorInactiveReason(VendorInactiveReason vendorInactiveReason) {
//        this.vendorInactiveReason = vendorInactiveReason;
//    }
    public List<VendorAddress> getVendorAddresses() {
        return vendorAddresses;
    }

    public void setVendorAddresses(List<VendorAddress> vendorAddresses) {
        this.vendorAddresses = vendorAddresses;
    }

    /*public List<VendorContact> getVendorContacts() {
        return vendorContacts;
    }

    public void setVendorContacts(List<VendorContact> vendorContacts) {
        this.vendorContacts = vendorContacts;
    }

    public List<VendorContract> getVendorContracts() {
        return vendorContracts;
    }

    public void setVendorContracts(List<VendorContract> vendorContracts) {
        this.vendorContracts = vendorContracts;
    }

    public List<VendorCustomerNumber> getVendorCustomerNumbers() {
        return vendorCustomerNumbers;
    }

    public void setVendorCustomerNumbers(List<VendorCustomerNumber> vendorCustomerNumbers) {
        this.vendorCustomerNumbers = vendorCustomerNumbers;
    }

    public List<VendorShippingSpecialCondition> getVendorShippingSpecialConditions() {
        return vendorShippingSpecialConditions;
    }

    public void setVendorShippingSpecialConditions(List<VendorShippingSpecialCondition> vendorShippingSpecialConditions) {
        this.vendorShippingSpecialConditions = vendorShippingSpecialConditions;
    }

    public List<VendorCommodityCode> getVendorCommodities() {
        return vendorCommodities;
    }

    public void setVendorCommodities(List<VendorCommodityCode> vendorCommodities) {
        this.vendorCommodities = vendorCommodities;
    }

    public List<VendorAlias> getVendorAliases() {
        return vendorAliases;
    }

    public void setVendorAliases(List<VendorAlias> vendorAliases) {
        this.vendorAliases = vendorAliases;
    }

    public List<VendorPhoneNumber> getVendorPhoneNumbers() {
        return vendorPhoneNumbers;
    }

    public void setVendorPhoneNumbers(List<VendorPhoneNumber> vendorPhoneNumbers) {
        this.vendorPhoneNumbers = vendorPhoneNumbers;
    }*/

    public String getVendorFirstName() {
        return vendorFirstName;
    }

    public void setVendorFirstName(String vendorFirstName) {
        this.vendorFirstName = vendorFirstName;
    }

    public String getVendorLastName() {
        return vendorLastName;
    }

    public void setVendorLastName(String vendorLastName) {
        this.vendorLastName = vendorLastName;
    }

    public VendorDetail getSoldToVendorDetail() {
        return soldToVendorDetail;
    }

    public void setSoldToVendorDetail(VendorDetail soldToVendorDetail) {
        this.soldToVendorDetail = soldToVendorDetail;
    }

    public boolean isVendorFirstLastNameIndicator() {
        return vendorFirstLastNameIndicator;
    }

    public void setVendorFirstLastNameIndicator(boolean vendorFirstLastNameIndicator) {
        this.vendorFirstLastNameIndicator = vendorFirstLastNameIndicator;
    }

    public String getVendorStateForLookup() {
        return vendorStateForLookup;
    }

    public void setVendorStateForLookup(String vendorStateForLookup) {
        this.vendorStateForLookup = vendorStateForLookup;
    }

//    public Person getVendorRestrictedPerson() {
//        vendorRestrictedPerson = SpringContext.getBean(org.kuali.rice.kim.api.identity.PersonService.class).updatePersonIfNecessary(vendorRestrictedPersonIdentifier, vendorRestrictedPerson);
//        return vendorRestrictedPerson;
//    }

    /**
     * Sets the vendorRestrictedPerson attribute.
     *
     * @param vendorRestrictedPerson The vendorRestrictedPerson to set.
     * @deprecated
     */
//    public void setVendorRestrictedPerson(Person vendorRestrictedPerson) {
//        this.vendorRestrictedPerson = vendorRestrictedPerson;
//    }
    public String getDefaultAddressLine1() {
        return defaultAddressLine1;
    }

    public void setDefaultAddressLine1(String defaultAddressLine1) {
        this.defaultAddressLine1 = defaultAddressLine1;
    }

    public String getDefaultAddressCity() {
        return defaultAddressCity;
    }

    public void setDefaultAddressCity(String defaultAddressCity) {
        this.defaultAddressCity = defaultAddressCity;
    }

    public String getDefaultAddressLine2() {
        return defaultAddressLine2;
    }

    public void setDefaultAddressLine2(String defaultAddressLine2) {
        this.defaultAddressLine2 = defaultAddressLine2;
    }

    public String getDefaultAddressPostalCode() {
        return defaultAddressPostalCode;
    }

    public void setDefaultAddressPostalCode(String defaultAddressPostalCode) {
        this.defaultAddressPostalCode = defaultAddressPostalCode;
    }

    public String getDefaultAddressStateCode() {
        return defaultAddressStateCode;
    }

    public void setDefaultAddressStateCode(String defaultAddressStateCode) {
        this.defaultAddressStateCode = defaultAddressStateCode;
    }

    public String getDefaultAddressInternationalProvince() {
        return defaultAddressInternationalProvince;
    }

    public void setDefaultAddressInternationalProvince(String defaultAddressInternationalProvince) {
        this.defaultAddressInternationalProvince = defaultAddressInternationalProvince;
    }

    public String getDefaultAddressCountryCode() {
        return defaultAddressCountryCode;
    }

    public void setDefaultAddressCountryCode(String defaultAddressCountryCode) {
        this.defaultAddressCountryCode = defaultAddressCountryCode;
    }

    public String getDefaultFaxNumber() {
        return defaultFaxNumber;
    }

    public void setDefaultFaxNumber(String defaultFaxNumber) {
        this.defaultFaxNumber = defaultFaxNumber;
    }

    public void setVendorParentName(String vendorParentName) {
        this.vendorParentName = vendorParentName;
    }


    public String getVendorInactiveReasonCode() {
        return this.vendorInactiveReasonCode;
    }

    public void setVendorInactiveReasonCode(String vendorInactiveReasonCode) {
        this.vendorInactiveReasonCode = vendorInactiveReasonCode;
    }

    public String getVendorShippingTitleCode() {
        return this.vendorShippingTitleCode;
    }

    public void setVendorShippingTitleCode(String vendorShippingTitleCode) {
        this.vendorShippingTitleCode = vendorShippingTitleCode;
    }

    public String getVendorParentName() {
        return this.vendorParentName;
    }


}

