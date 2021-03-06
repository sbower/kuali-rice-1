--
-- Copyright 2008-2009 The Kuali Foundation
--
-- Licensed under the Educational Community License, Version 2.0 (the "License");
-- you may not use this file except in compliance with the License.
-- You may obtain a copy of the License at
--
-- http://www.opensource.org/licenses/ecl2.php
--
-- Unless required by applicable law or agreed to in writing, software
-- distributed under the License is distributed on an "AS IS" BASIS,
-- WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
-- See the License for the specific language governing permissions and
-- limitations under the License.
--
CREATE TABLE KR_KNS_SESN_DOC_T (
        SESSION_ID                     VARCHAR2(40) CONSTRAINT KR_KNS_SESN_DOC_TN1 NOT NULL,
        FDOC_NBR                       VARCHAR2(14) CONSTRAINT KR_KNS_SESN_DOC_TN2 NOT NULL,
        SERIALIZED_DOC_FRM             BLOB,
        LST_UPDATE_DT                  DATE,
     CONSTRAINT KR_KNS_SESN_DOC_TP1 PRIMARY KEY (SESSION_ID, FDOC_NBR)
)
/

CREATE INDEX KR_KNS_SESN_DOC_TI1 ON KR_KNS_SESN_DOC_T ( LST_UPDATE_DT )
/

CREATE TABLE KR_COUNTRY_T
    (POSTAL_CNTRY_CD VARCHAR2(2) NOT NULL,
    OBJ_ID VARCHAR2(36) NOT NULL,
    VER_NBR NUMBER(8,0) DEFAULT 1 NOT NULL,
    POSTAL_CNTRY_NM VARCHAR2(40),
    PSTL_CNTRY_RSTRC_IND VARCHAR2(1) NOT NULL,
    ACTV_IND VARCHAR2(1) DEFAULT 'Y' NOT NULL)
/

CREATE TABLE KR_COUNTY_T
    (COUNTY_CD VARCHAR2(10) NOT NULL,
    STATE_CD VARCHAR2(2) NOT NULL,
    POSTAL_CNTRY_CD VARCHAR2(2) DEFAULT 'US' NOT NULL,
    OBJ_ID VARCHAR2(36) NOT NULL,
    VER_NBR NUMBER(8,0) DEFAULT 1 NOT NULL,
    COUNTY_NM VARCHAR2(100),
    ACTV_IND VARCHAR2(1))
/


CREATE TABLE KR_POSTAL_CODE_T
    (POSTAL_CD VARCHAR2(20) NOT NULL,
    POSTAL_CNTRY_CD VARCHAR2(2) DEFAULT 'US' NOT NULL,
    OBJ_ID VARCHAR2(36) NOT NULL,
    VER_NBR NUMBER(8,0) DEFAULT 1 NOT NULL,
    POSTAL_STATE_CD VARCHAR2(2),
    COUNTY_CD VARCHAR2(10),
    POSTAL_CITY_NM VARCHAR2(30),
    ACTV_IND VARCHAR2(1) DEFAULT 'Y' NOT NULL)
/

CREATE TABLE KR_STATE_T
    (POSTAL_STATE_CD VARCHAR2(2) NOT NULL,
    POSTAL_CNTRY_CD VARCHAR2(2) DEFAULT 'US' NOT NULL,
    OBJ_ID VARCHAR2(36) NOT NULL,
    VER_NBR NUMBER(8,0) DEFAULT 1 NOT NULL,
    POSTAL_STATE_NM VARCHAR2(40),
    ACTV_IND VARCHAR2(1) DEFAULT 'Y' NOT NULL)
/

ALTER TABLE KR_COUNTRY_T
ADD CONSTRAINT KR_COUNTRY_TP1
PRIMARY KEY (POSTAL_CNTRY_CD)
/
ALTER TABLE KR_COUNTY_T
ADD CONSTRAINT KR_COUNTY_TP1
PRIMARY KEY (COUNTY_CD, STATE_CD, POSTAL_CNTRY_CD)
/
ALTER TABLE KR_STATE_T
ADD CONSTRAINT KR_STATE_TP1
PRIMARY KEY (POSTAL_STATE_CD, POSTAL_CNTRY_CD)
/
ALTER TABLE KR_COUNTRY_T
ADD CONSTRAINT KR_COUNTRY_TC0
UNIQUE (OBJ_ID)
/
ALTER TABLE KR_COUNTY_T
ADD CONSTRAINT KR_COUNTY_TC0
UNIQUE (OBJ_ID)
/
ALTER TABLE KR_COUNTY_T
ADD CONSTRAINT KR_COUNTY_TR1
FOREIGN KEY (STATE_CD, POSTAL_CNTRY_CD)
REFERENCES KR_STATE_T
/
ALTER TABLE KR_STATE_T
ADD CONSTRAINT KR_STATE_TC0
UNIQUE (OBJ_ID)
/
ALTER TABLE KR_STATE_T
ADD CONSTRAINT KR_STATE_TR1
FOREIGN KEY (POSTAL_CNTRY_CD)
REFERENCES KR_COUNTRY_T
/
ALTER TABLE KR_POSTAL_CODE_T
ADD CONSTRAINT KR_POSTAL_CODE_TP1
PRIMARY KEY (POSTAL_CD, POSTAL_CNTRY_CD)
/
ALTER TABLE KR_POSTAL_CODE_T
ADD CONSTRAINT KR_POSTAL_CODE_TC0
UNIQUE (OBJ_ID)
/
ALTER TABLE KR_POSTAL_CODE_T
ADD CONSTRAINT KR_POSTAL_CODE_TR1
FOREIGN KEY (POSTAL_CNTRY_CD)
REFERENCES KR_COUNTRY_T
/
ALTER TABLE KR_POSTAL_CODE_T
ADD CONSTRAINT KR_POSTAL_CODE_TR2
FOREIGN KEY (POSTAL_STATE_CD, POSTAL_CNTRY_CD)
REFERENCES KR_STATE_T
/
ALTER TABLE KR_POSTAL_CODE_T
ADD CONSTRAINT KR_POSTAL_CODE_TR3
FOREIGN KEY (COUNTY_CD, POSTAL_STATE_CD, POSTAL_CNTRY_CD)
REFERENCES KR_COUNTY_T
/

-- ALTER TABLE SH_PARM_T DROP COLUMN GRP_NM
/

INSERT INTO SH_PARM_T
(SH_PARM_NMSPC_CD,SH_PARM_DTL_TYP_CD,SH_PARM_NM,OBJ_ID,VER_NBR,SH_PARM_TYP_CD,SH_PARM_TXT,SH_PARM_DESC,SH_PARM_CONS_CD,WRKGRP_NM)
VALUES
('KR-NS','State','STATE', SYS_GUID(), 1,'HELP','default.htm?turl=WordDocuments%2Fstatemaintenancedocument.htm','Help URL for State document.','A','FP_OPERATIONS')
/
INSERT INTO SH_PARM_T
(SH_PARM_NMSPC_CD,SH_PARM_DTL_TYP_CD,SH_PARM_NM,OBJ_ID,VER_NBR,SH_PARM_TYP_CD,SH_PARM_TXT,SH_PARM_DESC,SH_PARM_CONS_CD,WRKGRP_NM)
VALUES
('KR-NS','Country','COUNTRY', SYS_GUID(), 1,'HELP','default.htm?turl=WordDocuments%2Fcountrymaintenancedocument.htm','Help URL for Chart Country.','A','FP_OPERATIONS')
/
INSERT INTO SH_PARM_T
(SH_PARM_NMSPC_CD,SH_PARM_DTL_TYP_CD,SH_PARM_NM,OBJ_ID,VER_NBR,SH_PARM_TYP_CD,SH_PARM_TXT,SH_PARM_DESC,SH_PARM_CONS_CD,WRKGRP_NM)
VALUES
('KR-NS','PostalCode','POSTAL_CODE', SYS_GUID(),1,'HELP','default.htm?turl=WordDocuments%2Fpostalcodemaintenancedocument.htm','Help URL for Postal Code document.','A','FP_OPERATIONS')
/

INSERT INTO SH_PARM_T
(SH_PARM_NMSPC_CD,SH_PARM_DTL_TYP_CD,SH_PARM_NM,OBJ_ID,VER_NBR,SH_PARM_TYP_CD,SH_PARM_TXT,SH_PARM_DESC,SH_PARM_CONS_CD,WRKGRP_NM)
VALUES
('KR-NS','All','DEFAULT_COUNTRY',  SYS_GUID(),1,'CONFG','US','Used as the default country code when relating records that do not have a country code to records that do have a country code, e.g. validating a zip code where the country is not collected.','A','FP_OPERATIONS')
/

INSERT INTO FP_DOC_TYPE_T
VALUES
('ZIPC', SYS_GUID(),1,'Postal Code','Y')
/
INSERT INTO FP_DOC_TYPE_T
VALUES
('STAT', SYS_GUID(),1,'State','Y')
/
INSERT INTO FP_DOC_TYPE_T
VALUES
('CNTY', SYS_GUID(),1,'County','Y')
/
INSERT INTO FP_DOC_TYPE_T
VALUES
('CTRY',SYS_GUID(),1,'Country','Y')
/
