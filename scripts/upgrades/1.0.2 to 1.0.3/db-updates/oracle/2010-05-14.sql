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
-- DO NOT add comments before the blank line below, or they will disappear.
CREATE TABLE KREW_RIA_DOC_T (
	RIA_ID NUMBER NOT NULL,
    XML_CONTENT VARCHAR2(4000),
    RIA_DOC_TYPE_NAME VARCHAR2(100),
    VER_NBR NUMBER(8) DEFAULT 1 NOT NULL,
	OBJ_ID VARCHAR2(36) DEFAULT SYS_GUID() NOT NULL, 
    CONSTRAINT KREW_RIA_DOC_PK PRIMARY KEY (RIA_ID)
)
/
CREATE TABLE KREW_RIA_DOCTYPE_MAP_T (
	ID NUMBER NOT NULL,
	RIA_DOC_TYPE_NAME VARCHAR2(100),
 	UPDATED_AT DATE,
 	RIA_URL VARCHAR2(255),
 	HELP_URL VARCHAR2(255),
 	EDITABLE CHAR,
	INIT_GROUPS VARCHAR2(255),
  	VER_NBR NUMBER(8) DEFAULT 1 NOT NULL,	
  	OBJ_ID VARCHAR2(36) DEFAULT SYS_GUID() NOT NULL, 
    CONSTRAINT KREW_RIA_DOCTYPE_MAP_PK PRIMARY KEY (ID)
)
/
CREATE SEQUENCE KREW_RIA_DOCTYPE_MAP_ID_S INCREMENT BY 1 START WITH 1000
/
