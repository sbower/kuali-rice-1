package org.kuali.rice.kew.api.rule;

import java.io.Serializable;
import java.util.Collection;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.kuali.rice.core.api.CoreConstants;
import org.kuali.rice.core.api.mo.AbstractDataTransferObject;
import org.kuali.rice.core.api.mo.ModelBuilder;
import org.kuali.rice.kew.api.KewApiConstants;
import org.w3c.dom.Element;

@XmlRootElement(name = RuleDelegation.Constants.ROOT_ELEMENT_NAME)
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = RuleDelegation.Constants.TYPE_NAME, propOrder = {
    RuleDelegation.Elements.DELEGATION_TYPE,
    RuleDelegation.Elements.DELEGATION_RULE,
    CoreConstants.CommonElements.FUTURE_ELEMENTS
})
public final class RuleDelegation
    extends AbstractDataTransferObject
    implements RuleDelegationContract
{

    @XmlElement(name = Elements.DELEGATION_TYPE, required = false)
    private final String delegationType;
    @XmlElement(name = Elements.DELEGATION_RULE, required = false)
    private final Rule delegationRule;
    @SuppressWarnings("unused")
    @XmlAnyElement
    private final Collection<Element> _futureElements = null;

    /**
     * Private constructor used only by JAXB.
     * 
     */
    private RuleDelegation() {
        this.delegationType = null;
        this.delegationRule = null;
    }

    private RuleDelegation(Builder builder) {
        this.delegationType = builder.getDelegationType();
        this.delegationRule = builder.getDelegationRule() == null ? null : builder.getDelegationRule().build();
    }

    @Override
    public String getDelegationType() {
        return this.delegationType;
    }

    @Override
    public Rule getDelegationRule() {
        return this.delegationRule;
    }


    /**
     * A builder which can be used to construct {@link RuleDelegation} instances.  Enforces the constraints of the {@link RuleDelegationContract}.
     * 
     */
    public final static class Builder
        implements Serializable, ModelBuilder, RuleDelegationContract
    {

        private String delegationType;
        private Rule.Builder delegationRule;

        private Builder() {
        }

        public static Builder create() {
            return new Builder();
        }

        public static Builder create(RuleDelegationContract contract) {
            if (contract == null) {
                throw new IllegalArgumentException("contract was null");
            }
            Builder builder = create();
            builder.setDelegationType(contract.getDelegationType());
            if (contract.getDelegationRule() != null) {
                builder.setDelegationRule(Rule.Builder.create(contract.getDelegationRule()));
            }
            return builder;
        }

        public RuleDelegation build() {
            return new RuleDelegation(this);
        }

        @Override
        public String getDelegationType() {
            return this.delegationType;
        }

        @Override
        public Rule.Builder getDelegationRule() {
            return this.delegationRule;
        }

        public void setDelegationType(String delegationType) {
            this.delegationType = delegationType;
        }

        public void setDelegationRule(Rule.Builder delegationRule) {
            this.delegationRule = delegationRule;
        }

    }


    /**
     * Defines some internal constants used on this class.
     * 
     */
    static class Constants {

        final static String ROOT_ELEMENT_NAME = "ruleDelegation";
        final static String TYPE_NAME = "RuleDelegationType";

    }


    /**
     * A private class which exposes constants which define the XML element names to use when this object is marshalled to XML.
     * 
     */
    static class Elements {

        final static String DELEGATION_TYPE = "delegationType";
        final static String DELEGATION_RULE = "delegationRule";

    }

    public static class Cache {
        public static final String NAME = KewApiConstants.Namespaces.KEW_NAMESPACE_2_0 + "/" + RuleDelegation.Constants.TYPE_NAME;
    }
}