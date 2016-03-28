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
package com.redhat.gss.brms.dynamic.rules.db;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class KieBaseEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7748784647296840293L;

	private String kieBaseName;
	
	private String packageName;
	
	private Date lastUpdated;
	
	private Date lastBuild;
	
	private Collection<String> imports;
	
	private Collection<GlobalEntity> globals;
	
	private Collection<RuleEntity> rules;
	
	public String getKieBaseName() {
		return kieBaseName;
	}

	public void setKieBaseName(String kieBaseName) {
		this.kieBaseName = kieBaseName;
		setLastUpdated();
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
		setLastUpdated();
	}

	public Date getLastUpdated() {
		return lastUpdated;
	}

	private void setLastUpdated() {
		this.lastUpdated = new Date();
	}
	
	public String getGroup() {
		return packageName;
	}
	
	public String getArtifact() {
		return kieBaseName;
	}
	
	public String getVersion() {
		return  new Long(getLastUpdated().getTime()).toString();
	}

	public Date getLastBuild() {
		return lastBuild;
	}

	public void setLastBuild(Date lastBuild) {
		this.lastBuild = lastBuild;
	}

	private Collection<String> getImports() {
		return imports;
	}

	public void createImport(String importPackage) {
		if(null == imports) imports = new ArrayList<String>();
		this.imports.add(importPackage);
		setLastUpdated();
	}

	private Collection<GlobalEntity> getGlobals() {
		return globals;
	}

	public void createGlobal(GlobalEntity global) {
		if(null == globals) globals = new ArrayList<GlobalEntity>();
		this.globals.add(global);
		setLastUpdated();
	}

	private Collection<RuleEntity> getRules() {
		return rules;
	}

	public void createRule(RuleEntity rule) {
		if(null == rules) rules = new ArrayList<RuleEntity>();
		this.rules.add(rule);
		setLastUpdated();
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("package ").append(getPackageName()).append("\n\n");
		
		sb.append("//list any import classes here.\n");
		Iterator<String> importsIterator = getImports().iterator();
		while (importsIterator.hasNext()) {
			String ImportString = (String) importsIterator.next();
			sb.append("import ").append(ImportString).append(";\n");
		}
		
		sb.append("//declare any global variables here.\n");
		Iterator<GlobalEntity> globalsIter = getGlobals().iterator();
		while (globalsIter.hasNext()) {
			GlobalEntity globalEntity = (GlobalEntity) globalsIter.next();
			sb.append(globalEntity.toString()).append(";\n");
		}
		
		Iterator<RuleEntity> rulesIter = getRules().iterator();
		while (rulesIter.hasNext()) {
			RuleEntity ruleEntity = (RuleEntity) rulesIter.next();
			sb.append(ruleEntity.toString());
		}
		
		return sb.toString();
	}

	
	
	

}
