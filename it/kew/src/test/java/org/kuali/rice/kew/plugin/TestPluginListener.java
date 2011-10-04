/*
 * Copyright 2005-2007 The Kuali Foundation
 * 
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
package org.kuali.rice.kew.plugin;


/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class TestPluginListener implements PluginListener {

    private boolean initialized = false;
    private boolean destroyed = true;
    
    public void pluginInitialized(Plugin plugin) {
        initialized = true;
    }

    public void pluginDestroyed(Plugin plugin) {
        destroyed = true;
    }
    
    public boolean isDestroyed() {
        return destroyed;
    }
    
    public boolean isInitialized() {
        return initialized;
    }
    
}
