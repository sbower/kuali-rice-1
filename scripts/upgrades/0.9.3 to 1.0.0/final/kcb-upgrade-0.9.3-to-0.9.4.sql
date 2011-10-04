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
alter table kcb_msg_delivs modify DELIVERER_SYSTEM_ID varchar2(255)
/
alter table kcb_recip_delivs modify RECIPIENT_ID varchar2(255)
/
alter table kcb_recip_delivs modify CHANNEL varchar2(255)
/
alter table kcb_recip_delivs modify DELIVERER_NAME varchar2(255)
/
alter table KCB_RECIP_PREFS modify RECIPIENT_ID varchar2(255)
/
alter table KCB_RECIP_PREFS modify PROPERTY varchar2(255)
/
alter table KCB_RECIP_PREFS modify VALUE varchar2(255)
/

-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
-- !!! STOP!  Don't put anymore SQL in this file for Rice 1.0. Instead, create files in the !!!
-- !!! 'scripts/upgrades/0.9.3 to 0.9.4/db-updates-during-qa' directory                     !!!
-- !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
