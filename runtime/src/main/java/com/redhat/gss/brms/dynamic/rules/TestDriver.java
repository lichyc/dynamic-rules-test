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

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieModule;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieBaseModel;
import org.kie.api.builder.model.KieModuleModel;
import org.kie.api.builder.model.KieSessionModel.KieSessionType;
import org.kie.api.definition.KiePackage;
import org.kie.api.definition.rule.Rule;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;

import com.redhat.gss.brms.dynamic.rules.db.KieBaseEntity;

/**
 * Intention of this Class is to show how rules can get dynamically and
 * temporary injected to extend an existing kBase.
 * 
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$ $Date$: Date of last commit
 *
 */
public class TestDriver {

	private static final Logger log = Logger.getLogger(TestDriver.class
			.getName());

	private static KieContainer kieContainer = null;

	private static KieServices kieServices = KieServices.Factory.get();

	/**
	 * Builds a <code>Drools</code> {@link KieContainer} from the resources
	 * found on the classpath.
	 * 
	 * @return a <code>Drools</code> {@link KieContainer}
	 */
	public static synchronized KieContainer getKieContainer() {
		if (kieContainer == null) {
			kieContainer = kieServices.getKieClasspathContainer();
		}
		return kieContainer;
	}

	/**
	 * Get a stateless kSession for a given kBase Name. Just used here as
	 * reference.
	 * 
	 * @param kBaseName
	 *            name of the kBase as defined in the kModule.
	 * @return a stateless kSession.
	 */
	public StatelessKieSession getStatelessKieSession(String kBaseName) {
		return getKieContainer().getKieBase(kBaseName).newStatelessKieSession();
	}

	/**
	 * Create a stateless kSession for a given kBase extended by dynamically
	 * injected rules. A current trade-off is that the GAV coordinates of the
	 * kJar including the kBase need to get provided.
	 * 
	 * <br/>
	 * <b>Note:</b> As we share the same ReleaseId and KieBaseModel name this
	 * operation is synchronized to avoid race conditions.
	 * 
	 * @param group
	 *            of GAV coordinates of the kJar including the kBase to used.
	 * @param artifact
	 *            of GAV coordinates of the kJar including the kBase to used.
	 * @param version
	 *            of GAV coordinates of the kJar including the kBase to used.
	 * @param kBaseName
	 *            name of the kBase as defined in the kModule.
	 * @param dynamicRules
	 *            the dynamic rule to inject must be a complete DRL resource
	 *            incl. package and imports.
	 * @return a stateless kSession, including rules of kJar and dynamicRules.
	 */
	public synchronized StatelessKieSession getExtendedStatelessKieSession(
			String group, String artifact, String version, String kBaseName,
			String dynamicRules) {

		KieModule baseKieModule = kieServices.getRepository().getKieModule(
				KieServices.Factory.get()
						.newReleaseId(group, artifact, version));

		// Note: this is an in-memory file-system
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

		ReleaseId newRid = kieServices.newReleaseId(group, artifact + ".test",
				version);
		kieFileSystem.generateAndWritePomXML(newRid);

		KieModuleModel kModuleModel = kieServices.newKieModuleModel();

		KieBaseModel newKieBaseModel = kModuleModel.newKieBaseModel(
				"kiemodulemodel").setDefault(true).addInclude(kBaseName);

		// add all the packages of the KieModule we base on also to the new one
		Collection<KiePackage> kiePackages = getKieContainer(KieServices.Factory.get()
				.newReleaseId(group,artifact,version) ).getKieBase(
				kBaseName).getKiePackages();
		Iterator<KiePackage> kiePackagesIter = kiePackages.iterator();
		while (kiePackagesIter.hasNext()) {
			KiePackage kiePackage = (KiePackage) kiePackagesIter.next();
			newKieBaseModel.addPackage(kiePackage.getName());

		}
		newKieBaseModel.newKieSessionModel("test-session").setDefault(true)
				.setType(KieSessionType.STATELESS);

		log.log(Level.FINE, kModuleModel.toXML());
		kieFileSystem.writeKModuleXML(kModuleModel.toXML());

		// even if a KieFileSystem is used the path need to follow rules
		// prefix: "src/main/resources/" extended by KieBaseModel name
		// path itself must be equal one of the package names added to the
		// KieBaseModel
		// postfix: a valid drl filename
		String drlFileName = "src/main/resources/kiemodulemodel/"
				+ ((KiePackage) kiePackages.toArray()[0]).getName()
				+ "/test-rules.drl";

		kieFileSystem.write(drlFileName, dynamicRules);

		KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

		// add the KieModule we extend as dependency
		kieBuilder.setDependencies(baseKieModule);

		kieBuilder.buildAll(); // kieModule is automatically deployed to
								// KieRepository if successfully built.
		if (kieBuilder.getResults().hasMessages(
				org.kie.api.builder.Message.Level.ERROR)) {
			throw new RuntimeException("Build Errors:\n"
					+ kieBuilder.getResults().toString());
		}

		// return a stateless kSession based on the definitions above.
		return kieServices.newKieContainer(newRid).newStatelessKieSession(
				"test-session");
	}

	/**
	 * Builds a <code>Drools</code> {@link KieContainer} from {@KieBaseEntity
	 * }
	 * 
	 * @return a <code>Drools</code> {@link KieContainer}
	 */
	public synchronized KieContainer getKieContainer(KieBaseEntity kieBaseEntity) {
		KieFileSystem kieFileSystem = kieServices.newKieFileSystem();

		ReleaseId newRid = kieServices.newReleaseId(kieBaseEntity.getGroup(),
				kieBaseEntity.getArtifact(), kieBaseEntity.getVersion());

		// check if we need to build or already exists
		if (kieBaseEntity.getLastBuild() == null
				|| kieBaseEntity.getLastBuild().before(
						kieBaseEntity.getLastUpdated())) {

			kieFileSystem.generateAndWritePomXML(newRid);

			KieModuleModel kModuleModel = kieServices.newKieModuleModel();

			KieBaseModel newKieBaseModel = kModuleModel
					.newKieBaseModel(kieBaseEntity.getKieBaseName());
			newKieBaseModel.addPackage(kieBaseEntity.getPackageName()).setDefault(true);

			newKieBaseModel.newKieSessionModel("stateless-session")
					.setDefault(true).setType(KieSessionType.STATELESS);
			newKieBaseModel.newKieSessionModel("stateful-session")
					.setDefault(true).setType(KieSessionType.STATEFUL);

			log.log(Level.FINE, kModuleModel.toXML());
			kieFileSystem.writeKModuleXML(kModuleModel.toXML());

			// even if a KieFileSystem is used the path need to follow rules
			// prefix: "src/main/resources/" extended by KieBaseModel name
			// path itself must be equal one of the package names added to the
			// KieBaseModel
			// postfix: a valid drl filename
			String drlFileName = "src/main/resources/"
					+ kieBaseEntity.getKieBaseName() + "/"
					+ kieBaseEntity.getPackageName() + "/"
					+ kieBaseEntity.getKieBaseName() + ".drl";

			kieFileSystem.write(drlFileName, kieBaseEntity.toString());

			KieBuilder kieBuilder = kieServices.newKieBuilder(kieFileSystem);

			kieBuilder.buildAll(); // kieModule is automatically deployed to
									// KieRepository if successfully built.
			if (kieBuilder.getResults().hasMessages(
					org.kie.api.builder.Message.Level.ERROR)) {
				throw new RuntimeException("Build Errors:\n"
						+ kieBuilder.getResults().toString());
			}

			kieBaseEntity.setLastBuild(new Date());
		}

		// return a KieContainer based on the definitions above.
		return kieServices.newKieContainer(newRid);
	}
	
	public KieContainer getKieContainer(ReleaseId rid) {
		return kieServices.newKieContainer(rid);
	}

	public static void logRules(KieSession kieSession) {
		logRules(kieSession.getKieBase().getKiePackages());
	}

	public static void logRules(StatelessKieSession kieSession) {
		logRules(kieSession.getKieBase().getKiePackages());
	}

	private static void logRules(Collection<KiePackage> kiePackages) {
		for (KiePackage nextPackage : kiePackages) {
			Collection<Rule> rules = nextPackage.getRules();
			for (Rule nextRule : rules) {
				log.log(Level.INFO, "Rule: " + nextRule.getName());
			}
		}
	}

}
