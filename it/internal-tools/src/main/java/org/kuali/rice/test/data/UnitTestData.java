/*
 * Copyright 2007 The Kuali Foundation
 * 
 * Licensed under the Educational Community License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.opensource.org/licenses/ecl2.php
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific
 * language governing permissions and limitations under the License.
 */
package org.kuali.rice.test.data;

import java.lang.annotation.*;

/**
 * Annotation for loading/running data before unit test runs.
 * 
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
@Documented
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface UnitTestData {
   
    public enum Type { SQL_STATEMENTS, SQL_FILES }

    // add direct support for both styles 
    String value() default "";
    String filename() default "";
    String delimiter() default ";";

    UnitTestSql[] sqlStatements() default {};
    UnitTestFile[] sqlFiles() default {};
    Type[] order() default { Type.SQL_STATEMENTS, Type.SQL_FILES };
    
    // TODO: Come back and add the JPQL equivalents
}
