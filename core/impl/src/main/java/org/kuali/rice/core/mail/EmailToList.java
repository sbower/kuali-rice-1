/*
 * Copyright 2005-2010 The Kuali Foundation
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
package org.kuali.rice.core.mail;
import java.util.List;
import javax.mail.Address;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

/**
 * The to addresses of an email message.
 *
 * @author Kuali Rice Team (kuali-rice@googlegroups.com)
 */
public class EmailToList {

    private List<String> toAddresses;
    
    public EmailToList(List<String> toAddresses) {
        this.toAddresses = toAddresses;
    }

    public List<String> getToAddresses() {
        return toAddresses;
    }
    
    public Address[] getToAddressesAsAddressArray() throws AddressException {
	Address[] recipientAddresses = new Address[this.toAddresses.size()];
	for (int i = 0; i < recipientAddresses.length; i++) {
	    recipientAddresses[i] = new InternetAddress((String) this.toAddresses.get(i));
	 }
        return recipientAddresses;
    }

    public void setToAddress(List<String> toAddresses) {
        this.toAddresses = toAddresses;
    }

}
