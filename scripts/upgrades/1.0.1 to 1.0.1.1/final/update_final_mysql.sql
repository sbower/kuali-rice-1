--
-- Copyright 2010 The Kuali Foundation
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

alter table krim_attr_defn_t drop column appl_url
/ 
ALTER TABLE KRIM_ENTITY_ETHNIC_T 
add CONSTRAINT KRIM_ENTITY_ETHNIC_TR1
  FOREIGN KEY (entity_id)
  REFERENCES KRIM_ENTITY_T(entity_id)
/
ALTER TABLE KRIM_ENTITY_RESIDENCY_T  
add CONSTRAINT KRIM_ENTITY_RESIDENCY_TR1
  FOREIGN KEY (entity_id)
  REFERENCES KRIM_ENTITY_T(entity_id)
/
ALTER TABLE KRIM_ENTITY_VISA_T  
add CONSTRAINT KRIM_ENTITY_VISA_TR1
  FOREIGN KEY (entity_id)
  REFERENCES KRIM_ENTITY_T(entity_id)
/

DELETE FROM KRSB_SVC_DEF_T
/
ALTER TABLE KRSB_SVC_DEF_T DROP COLUMN SVC_DEF
/
ALTER TABLE KRSB_SVC_DEF_T ADD (
	FLT_SVC_DEF_ID DECIMAL(14, 0) NOT NULL,
	SVC_DEF_CHKSM VARCHAR(30) NOT NULL
)
/
CREATE UNIQUE INDEX KRSB_SVC_DEF_TI2 ON KRSB_SVC_DEF_T (FLT_SVC_DEF_ID)
/
CREATE TABLE KRSB_FLT_SVC_DEF_T (
	FLT_SVC_DEF_ID DECIMAL(14, 0),
	FLT_SVC_DEF LONGTEXT NOT NULL, 
	CONSTRAINT KRSB_FLT_SVC_DEF_TP1 PRIMARY KEY (FLT_SVC_DEF_ID)
)
/
CREATE TABLE KRSB_FLT_SVC_DEF_S
(
	id bigint(19) not null auto_increment, primary key (id) 
)
/
ALTER TABLE KRSB_FLT_SVC_DEF_S auto_increment = 1000;
/

UPDATE KRNS_PARM_T SET TXT='MM/dd/yyyy hh:mm a;MM/dd/yy;MM/dd/yyyy;MM-dd-yy;MMddyy;MMMM dd;yyyy;MM/dd/yy HH:mm:ss;MM/dd/yyyy HH:mm:ss;MM-dd-yy HH:mm:ss;MMddyy HH:mm:ss;MMMM dd HH:mm:ss;yyyy HH:mm:ss' WHERE NMSPC_CD='KR-NS' AND PARM_DTL_TYP_CD='All' AND PARM_NM='STRING_TO_TIMESTAMP_FORMATS' AND APPL_NMSPC_CD='KUALI' 
/

alter table KREW_DOC_TYP_T add DOC_SEARCH_HELP_URL VARCHAR(4000) DEFAULT NULL
/

UPDATE KRNS_PARM_T SET TXT='MM/dd/yyyy hh:mm a;MM/dd/yy;MM/dd/yyyy;MM-dd-yy;MMddyy;MMMM dd;yyyy;MM/dd/yy HH:mm:ss;MM/dd/yyyy HH:mm:ss;MM-dd-yy HH:mm:ss;MMddyy HH:mm:ss;MMMM dd HH:mm:ss;yyyy HH:mm:ss' WHERE NMSPC_CD='KR-NS' AND PARM_DTL_TYP_CD='All' AND PARM_NM='STRING_TO_DATE_FORMATS' AND APPL_NMSPC_CD='KUALI' 
/



