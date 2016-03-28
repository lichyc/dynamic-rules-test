/**
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */ 
package com.redhat.gss.brms.test;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.Message;
import org.kie.api.builder.Results;
import org.kie.api.runtime.KieContainer;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class TestCompileRules {
	
	private static final Logger log = Logger.getLogger(TestCompileRules.class
			.getName());
	
	private static KieContainer kieContainer = null;
	
	/**
	 * Builds a <code>Drools</code> {@link KieContainer} from the resources found on the classpath.
	 * 
	 * @return a <code>Drools</code> {@link KieContainer}
	 */
	private static synchronized KieContainer getKieContainer() {
		if (kieContainer == null) {
			KieServices kieServices = KieServices.Factory.get();
			kieContainer = kieServices.getKieClasspathContainer();
		}
		
		return kieContainer;
	}
	
	@Test
	public void compileRules() throws Exception {
		
		if (kieContainer == null) kieContainer = getKieContainer();
		
		Results bulderResults = kieContainer.verify();
		
		List<Message> messageList = bulderResults.getMessages();
		
		Iterator<Message> messageListIter = messageList.iterator();
		
		while (messageListIter.hasNext()) {
			Message message = (Message) messageListIter.next();
			log.log(Level.INFO, "Builder-Message: "+message.toString());
		}
		
		assertTrue(messageList.size() == 0);
		
	}

}
