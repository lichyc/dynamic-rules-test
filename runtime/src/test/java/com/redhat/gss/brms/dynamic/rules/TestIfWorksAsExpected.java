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
package com.redhat.gss.brms.dynamic.rules;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.StatelessKieSession;

import com.acme.brms.domain.AcmeFactA;
import com.redhat.gss.brms.dynamic.rules.db.GlobalEntity;
import com.redhat.gss.brms.dynamic.rules.db.KieBaseEntity;
import com.redhat.gss.brms.dynamic.rules.db.RuleEntity;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$ $Date$: Date of last commit
 *
 */
public class TestIfWorksAsExpected {

	private static final Logger log = Logger
			.getLogger(TestIfWorksAsExpected.class.getName());

	private TestDriver testDriver = new TestDriver();

	// the dynamic rule to inject must be a complete DRL resource incl. package
	// and imports
	private static final String PACKAGE = "package com.acme.test\n ";

	private static final String IMPORTS = "import com.acme.brms.domain.AcmeFactA \n";

	private static final String GLOBALS = "global java.util.logging.Logger rulesLogger; \n\n";

	private static final String RULE1 = "rule \"Your Dynamic Rule\" \n "
			+ " when \n" + "    fact : AcmeFactA (something == null) \n"
			+ " then \n"
			+ "    rulesLogger.info(\"Something is unset, will set it.\"); \n"
			+ "    fact.setSomething(\"Nothing\");\n" + "end \n";

	private static final String RULE2 = "rule \"Your Other Dynamic Rule\" \n "
			+ " when \n" + "    fact : AcmeFactA (counter == 0) \n"
			+ " then \n"
			+ "    rulesLogger.info(\"Counter is 0, will increase \"); \n"
			+ "    fact.setCounter(1);\n" + "end \n";

	/**
	 * simple test to check if the static rules can get accessed. As some kind
	 * of regression test for the default rules execution.
	 */
	@Test
	public void simpleRuleFunctionTest() {

		AcmeFactA factOne = new AcmeFactA();

		StatelessKieSession ksession = testDriver
				.getStatelessKieSession("test-rules-a");
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factOne));
		BatchExecutionCommand batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		assertEquals(factOne.getName(), "Unknown");

	}

	/**
	 * Lets see what happens if we dynamically extend the rule base. Scenario:
	 * test the static rules, than test the static rules extended by a dynamic
	 * finally re-test the static rules again.
	 */
	@Test
	public void dynamicRuleFunctionTest() {

		// in a real business scenario you would clone the facts first to see on
		// how the different rule set behave.
		// short cut here: create multiple facts with the same content.
		AcmeFactA factOne = new AcmeFactA();
		AcmeFactA factTwo = new AcmeFactA();
		AcmeFactA factThree = new AcmeFactA();

		// test production rules first
		StatelessKieSession ksession = testDriver
				.getStatelessKieSession("test-rules-a");

		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factOne));
		BatchExecutionCommand batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		// check if expected rule(s) where fired
		assertEquals("Unknown", factOne.getName());

		// OK, now the frog has to jump into the pod
		ksession = testDriver.getExtendedStatelessKieSession(
				"com.redhat.gss.brms", "dynamic-rules-test-static-rules",
				"0.0.1-SNAPSHOT", "test-rules-a", PACKAGE + IMPORTS + GLOBALS
						+ RULE1);
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factTwo));
		batchCommand = new BatchExecutionCommandImpl(commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		// check if expected rule(s) where fired
		assertEquals("Nothing", factTwo.getSomething());
		assertEquals(factTwo.getName(), factOne.getName());

		// re-test production rules
		ksession = testDriver.getStatelessKieSession("test-rules-a");
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factThree));
		batchCommand = new BatchExecutionCommandImpl(commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		// check if expected rule(s) where fired, but not more
		assertEquals("Unknown", factThree.getName());
		assertEquals(null, factThree.getSomething());

	}

	/**
	 * What works once mustn't work twice. So lets test. Scenario: test with one
	 * dynamic rule, than test with another one.
	 */
	@Test
	public void dynamicRuleFunctionTestDifferentDynamicRules() {

		AcmeFactA factOne = new AcmeFactA();
		AcmeFactA factTwo = new AcmeFactA();

		StatelessKieSession ksession = testDriver
				.getExtendedStatelessKieSession("com.redhat.gss.brms",
						"dynamic-rules-test-static-rules", "0.0.1-SNAPSHOT",
						"test-rules-a", PACKAGE + IMPORTS + GLOBALS + RULE1);
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factOne));
		BatchExecutionCommand batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		ksession = testDriver.getExtendedStatelessKieSession(
				"com.redhat.gss.brms", "dynamic-rules-test-static-rules",
				"0.0.1-SNAPSHOT", "test-rules-a", PACKAGE + IMPORTS + GLOBALS
						+ RULE2);
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factTwo));
		batchCommand = new BatchExecutionCommandImpl(commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		// check if result meets the expectations
		// static rules should get fired
		assertEquals("Unknown", factOne.getName());
		assertEquals(factTwo.getName(), factOne.getName());
		// the dynamic rule should get fired
		assertEquals("Nothing", factOne.getSomething());
		// just the latest dynamic rule should get fired
		assertEquals(1, factTwo.getCounter());
		assertEquals(null, factTwo.getSomething());
	}

	/**
	 * Simple test using {@link KieBaseEntity}.
	 */
	@Test
	public void simpleEntityBasedRulesTest() {

		AcmeFactA factOne = new AcmeFactA();

		KieBaseEntity kieBaseEntity = createTestKieBaseEntity();
		KieContainer kieContainer = testDriver.getKieContainer(kieBaseEntity);

		StatelessKieSession ksession = kieContainer.newStatelessKieSession();
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factOne));
		BatchExecutionCommand batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		assertEquals(factOne.getName(), "Unknown");

	}

	/**
	 * Test life-cycle using {@link KieBaseEntity}.
	 */
	@Test
	public void entityBasedRulesLifeCycleTest() throws Exception {

		AcmeFactA factOne = new AcmeFactA();
		AcmeFactA factTwo = new AcmeFactA();
		AcmeFactA factThree = new AcmeFactA();

		KieBaseEntity kieBaseEntity = createTestKieBaseEntity();

		// check if the first Container
		KieContainer kieContainerOne = testDriver
				.getKieContainer(kieBaseEntity);

		StatelessKieSession ksession = kieContainerOne.newStatelessKieSession();
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factOne));
		BatchExecutionCommand batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		assertEquals(factOne.getName(), "Unknown");

		// change the kBase
		RuleEntity rule = new RuleEntity();
		rule.setRuleName("Your Third Rule");
		rule.setLeftHandSide("fact : AcmeFactA (something == null)");
		rule.setRightHandSide("rulesLogger.info(\"Something is unset, will set it.\"); \n"
				+ "    fact.setSomething(\"Nothing\");");
		kieBaseEntity.createRule(rule);

		// check if the second Container
		KieContainer kieContainerTwo = testDriver
				.getKieContainer(kieBaseEntity);

		ksession = kieContainerTwo.newStatelessKieSession();
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factTwo));
		batchCommand = new BatchExecutionCommandImpl(commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		assertEquals(factTwo.getName(), factOne.getName());
		assertEquals("Nothing", factTwo.getSomething());
		
		// check if we can get the first one again
		KieContainer kieContainerThree = testDriver
				.getKieContainer(kieContainerOne.getReleaseId());

		ksession = kieContainerThree.newStatelessKieSession();
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factThree));
		batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		assertEquals(kieContainerOne.getReleaseId().getVersion(), kieContainerThree.getReleaseId().getVersion());
		assertEquals(factThree.getName(), "Unknown");
		assertEquals(null, factThree.getSomething());

	}
	
	/**
	 * Combine {@link KieBaseEntity} with dynamic injected rules.
	 */
	@Test
	public void entityBasedRulesAndDynamicRuleTest() throws Exception {
		
		AcmeFactA factOne = new AcmeFactA();
		AcmeFactA factTwo = new AcmeFactA();

		KieBaseEntity kieBaseEntity = createTestKieBaseEntity();
		KieContainer kieContainer = testDriver.getKieContainer(kieBaseEntity);

		StatelessKieSession ksession = kieContainer.newStatelessKieSession();
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		List<GenericCommand<?>> commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factOne));
		BatchExecutionCommand batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);

		assertEquals(factOne.getName(), "Unknown");
		
		// Now the dynamically added rule
		ksession = testDriver
				.getExtendedStatelessKieSession(kieContainer.getReleaseId().getGroupId(),
						kieContainer.getReleaseId().getArtifactId(), kieContainer.getReleaseId().getVersion(),
						kieBaseEntity.getKieBaseName(), "package com.acme.test.datasource\n " + IMPORTS + GLOBALS + RULE1);
		if (log.isLoggable(Level.FINE))
			testDriver.logRules(ksession);

		commandList = new ArrayList<GenericCommand<?>>();
		commandList.add(new InsertObjectCommand(factTwo));
		batchCommand = new BatchExecutionCommandImpl(
				commandList);
		ksession.setGlobal("rulesLogger", log);
		ksession.execute(batchCommand);
		
		assertEquals(factTwo.getName(), factOne.getName());
		// the dynamic rule should get fired
		assertEquals("Nothing", factTwo.getSomething());
		
	}

	private KieBaseEntity createTestKieBaseEntity() {
		KieBaseEntity kieBaseEntity = new KieBaseEntity();

		kieBaseEntity.setKieBaseName("DatabaseRules");
		kieBaseEntity.setPackageName("com.acme.test.datasource");
		kieBaseEntity.createImport("com.acme.brms.domain.AcmeFactA");
		kieBaseEntity.createGlobal(new GlobalEntity("java.util.logging.Logger",
				"rulesLogger"));

		RuleEntity rule = new RuleEntity();
		rule.setRuleName("Your First Rule");
		rule.setLeftHandSide("fact : AcmeFactA (name == null)");
		rule.setRightHandSide("rulesLogger.info(\"Name is unset, will set it.\");\n"
				+ "        fact.setName(\"Unknown\");");
		kieBaseEntity.createRule(rule);

		rule = new RuleEntity();
		rule.setRuleName("Your Second Rule");
		rule.setLeftHandSide("fact : AcmeFactA (name == null)");
		rule.setRightHandSide("rulesLogger.info(\"Hurra, the name is set to: \" + fact.getName());");
		kieBaseEntity.createRule(rule);

		return kieBaseEntity;
	}

}
