-- 
-- Copyright 2009 The Kuali Foundation
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
CREATE SEQUENCE TRV_FO_ID_S INCREMENT BY 1 START WITH 1000
/

DELETE FROM KRIM_ENTITY_EMAIL_T where EMAIL_ADDR='KULUSER@indiana.edu'
/

CREATE TABLE KR_KIM_TEST_BO (
    pk VARCHAR2(40),
    prncpl_id VARCHAR2(40)
)
/
