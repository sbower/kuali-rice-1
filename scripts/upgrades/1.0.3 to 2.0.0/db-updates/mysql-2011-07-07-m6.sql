ALTER TABLE KREW_DOC_NTE_T CHANGE DOC_NTE_ID DOC_NTE_ID VARCHAR(40);

ALTER TABLE KREW_ATT_T CHANGE ATTACHMENT_ID ATTACHMENT_ID VARCHAR(40);
ALTER TABLE KREW_ATT_T CHANGE NTE_ID NTE_ID VARCHAR(40);

ALTER TABLE KREW_ACTN_ITM_T CHANGE ACTN_ITM_ID ACTN_ITM_ID VARCHAR(40);
ALTER TABLE KREW_ACTN_ITM_T CHANGE ACTN_RQST_ID ACTN_RQST_ID VARCHAR(40);
ALTER TABLE KREW_ACTN_ITM_T CHANGE RSP_ID RSP_ID VARCHAR(40);

ALTER TABLE KREW_ACTN_TKN_T CHANGE ACTN_TKN_ID ACTN_TKN_ID VARCHAR(40);

ALTER TABLE KREW_ACTN_RQST_T CHANGE ACTN_RQST_ID ACTN_RQST_ID VARCHAR(40); 
ALTER TABLE KREW_ACTN_RQST_T CHANGE PARNT_ID PARNT_ID VARCHAR(40);
ALTER TABLE KREW_ACTN_RQST_T CHANGE RSP_ID RSP_ID VARCHAR(40);
ALTER TABLE KREW_ACTN_RQST_T CHANGE ACTN_TKN_ID ACTN_TKN_ID VARCHAR(40);
ALTER TABLE KREW_ACTN_RQST_T CHANGE RULE_ID RULE_ID VARCHAR(40);
ALTER TABLE KREW_ACTN_RQST_T CHANGE RTE_NODE_INSTN_ID RTE_NODE_INSTN_ID VARCHAR(40);

ALTER TABLE KREW_RULE_TMPL_T CHANGE RULE_TMPL_ID RULE_TMPL_ID VARCHAR(40);
ALTER TABLE KREW_RULE_TMPL_T CHANGE DLGN_RULE_TMPL_ID DLGN_RULE_TMPL_ID VARCHAR(40);

ALTER TABLE KREW_RULE_TMPL_ATTR_T CHANGE RULE_TMPL_ATTR_ID RULE_TMPL_ATTR_ID VARCHAR(40);
ALTER TABLE KREW_RULE_TMPL_ATTR_T CHANGE RULE_TMPL_ID RULE_TMPL_ID VARCHAR(40);
ALTER TABLE KREW_RULE_TMPL_ATTR_T CHANGE RULE_ATTR_ID RULE_ATTR_ID VARCHAR(40);

ALTER TABLE KREW_RULE_T CHANGE RULE_ID RULE_ID VARCHAR(40);
ALTER TABLE KREW_RULE_T CHANGE RULE_TMPL_ID RULE_TMPL_ID VARCHAR(40);
ALTER TABLE KREW_RULE_T CHANGE RULE_EXPR_ID RULE_EXPR_ID VARCHAR(40);
ALTER TABLE KREW_RULE_T CHANGE PREV_RULE_VER_NBR PREV_RULE_VER_NBR VARCHAR(40);

ALTER TABLE KREW_DLGN_RSP_T CHANGE DLGN_RULE_ID DLGN_RULE_ID VARCHAR(40);
ALTER TABLE KREW_DLGN_RSP_T CHANGE RSP_ID RSP_ID VARCHAR(40);
ALTER TABLE KREW_DLGN_RSP_T CHANGE DLGN_RULE_BASE_VAL_ID DLGN_RULE_BASE_VAL_ID VARCHAR(40);

ALTER TABLE KREW_RULE_RSP_T CHANGE RULE_RSP_ID RULE_RSP_ID VARCHAR(40);
ALTER TABLE KREW_RULE_RSP_T CHANGE RSP_ID RSP_ID VARCHAR(40);
ALTER TABLE KREW_RULE_RSP_T CHANGE RULE_ID RULE_ID VARCHAR(40);

ALTER TABLE KREW_RULE_EXT_T CHANGE RULE_EXT_ID RULE_EXT_ID VARCHAR(40);
ALTER TABLE KREW_RULE_EXT_T CHANGE RULE_TMPL_ATTR_ID RULE_TMPL_ATTR_ID VARCHAR(40);
ALTER TABLE KREW_RULE_EXT_T CHANGE RULE_ID RULE_ID VARCHAR(40);

ALTER TABLE KREW_RTE_NODE_T CHANGE RTE_NODE_ID RTE_NODE_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_T CHANGE BRCH_PROTO_ID BRCH_PROTO_ID VARCHAR(40);

ALTER TABLE KREW_RTE_NODE_INSTN_T CHANGE RTE_NODE_INSTN_ID RTE_NODE_INSTN_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_INSTN_T CHANGE RTE_NODE_ID RTE_NODE_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_INSTN_T CHANGE BRCH_ID BRCH_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_INSTN_T CHANGE PROC_RTE_NODE_INSTN_ID PROC_RTE_NODE_INSTN_ID VARCHAR(40);

ALTER TABLE KREW_RTE_NODE_INSTN_LNK_T CHANGE TO_RTE_NODE_INSTN_ID TO_RTE_NODE_INSTN_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_INSTN_LNK_T CHANGE FROM_RTE_NODE_INSTN_ID FROM_RTE_NODE_INSTN_ID VARCHAR(40);

ALTER TABLE KREW_RTE_BRCH_T CHANGE RTE_BRCH_ID RTE_BRCH_ID VARCHAR(40);
ALTER TABLE KREW_RTE_BRCH_T CHANGE PARNT_ID PARNT_ID VARCHAR(40);
ALTER TABLE KREW_RTE_BRCH_T CHANGE INIT_RTE_NODE_INSTN_ID INIT_RTE_NODE_INSTN_ID VARCHAR(40);
ALTER TABLE KREW_RTE_BRCH_T CHANGE JOIN_RTE_NODE_INSTN_ID JOIN_RTE_NODE_INSTN_ID VARCHAR(40);
ALTER TABLE KREW_RTE_BRCH_T CHANGE SPLT_RTE_NODE_INSTN_ID SPLT_RTE_NODE_INSTN_ID VARCHAR(40);

ALTER TABLE KREW_RTE_BRCH_ST_T CHANGE RTE_BRCH_ST_ID RTE_BRCH_ST_ID VARCHAR(40);
ALTER TABLE KREW_RTE_BRCH_ST_T CHANGE RTE_BRCH_ID RTE_BRCH_ID VARCHAR(40);

ALTER TABLE KREW_RTE_NODE_INSTN_ST_T CHANGE RTE_NODE_INSTN_ST_ID RTE_NODE_INSTN_ST_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_INSTN_ST_T CHANGE RTE_NODE_INSTN_ID RTE_NODE_INSTN_ID VARCHAR(40);

ALTER TABLE KREW_DOC_TYP_ATTR_T CHANGE DOC_TYP_ATTRIB_ID DOC_TYP_ATTRIB_ID VARCHAR(40);
ALTER TABLE KREW_DOC_TYP_ATTR_T CHANGE DOC_TYP_ID DOC_TYP_ID VARCHAR(40);
ALTER TABLE KREW_DOC_TYP_ATTR_T CHANGE RULE_ATTR_ID RULE_ATTR_ID VARCHAR(40);

ALTER TABLE KREW_RULE_ATTR_T CHANGE RULE_ATTR_ID RULE_ATTR_ID VARCHAR(40);

ALTER TABLE KREW_RULE_TMPL_OPTN_T CHANGE RULE_TMPL_OPTN_ID RULE_TMPL_OPTN_ID VARCHAR(40);
ALTER TABLE KREW_RULE_TMPL_OPTN_T CHANGE RULE_TMPL_ID RULE_TMPL_ID VARCHAR(40);

ALTER TABLE KREW_OUT_BOX_ITM_T CHANGE ACTN_ITM_ID ACTN_ITM_ID VARCHAR(40);
ALTER TABLE KREW_OUT_BOX_ITM_T CHANGE ACTN_RQST_ID ACTN_RQST_ID VARCHAR(40);
ALTER TABLE KREW_OUT_BOX_ITM_T CHANGE RSP_ID RSP_ID VARCHAR(40);

ALTER TABLE KREW_RTE_NODE_CFG_PARM_T CHANGE RTE_NODE_ID RTE_NODE_ID VARCHAR(40);
ALTER TABLE KREW_RTE_NODE_CFG_PARM_T CHANGE RTE_NODE_CFG_PARM_ID RTE_NODE_CFG_PARM_ID VARCHAR(40);

ALTER TABLE KREW_DOC_TYP_PROC_T CHANGE DOC_TYP_PROC_ID DOC_TYP_PROC_ID VARCHAR(40);
ALTER TABLE KREW_DOC_TYP_PROC_T CHANGE INIT_RTE_NODE_ID INIT_RTE_NODE_ID VARCHAR(40);

ALTER TABLE KREW_DOC_LNK_T CHANGE DOC_LNK_ID DOC_LNK_ID VARCHAR(40);

ALTER TABLE KREW_RTE_BRCH_PROTO_T CHANGE RTE_BRCH_PROTO_ID RTE_BRCH_PROTO_ID VARCHAR(40);

ALTER TABLE KREW_HLP_T CHANGE HLP_ID HLP_ID VARCHAR(40);

ALTER TABLE KREW_RULE_EXT_VAL_T CHANGE RULE_EXT_VAL_ID RULE_EXT_VAL_ID VARCHAR(40);
ALTER TABLE KREW_RULE_EXT_VAL_T CHANGE RULE_EXT_ID RULE_EXT_ID VARCHAR(40);

ALTER TABLE KREW_RULE_EXPR_T CHANGE RULE_EXPR_ID RULE_EXPR_ID VARCHAR(40);

ALTER TABLE KREW_APP_DOC_STAT_TRAN_T CHANGE APP_DOC_STAT_TRAN_ID APP_DOC_STAT_TRAN_ID VARCHAR(40);

ALTER TABLE KREW_DOC_HDR_EXT_DT_T CHANGE DOC_HDR_EXT_DT_ID DOC_HDR_EXT_DT_ID VARCHAR(40);

ALTER TABLE KREW_DOC_HDR_EXT_LONG_T CHANGE DOC_HDR_EXT_LONG_ID DOC_HDR_EXT_LONG_ID VARCHAR(40);

ALTER TABLE KREW_DOC_HDR_EXT_FLT_T CHANGE DOC_HDR_EXT_FLT_ID DOC_HDR_EXT_FLT_ID VARCHAR(40);

ALTER TABLE KREW_DOC_HDR_EXT_T CHANGE DOC_HDR_EXT_ID DOC_HDR_EXT_ID VARCHAR(40);
