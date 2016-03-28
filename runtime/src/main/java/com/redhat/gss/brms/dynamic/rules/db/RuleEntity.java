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

/**
 *
 * @author <a href="mailto:clichybi@redhat.com">Carsten Lichy-Bittendorf</a>
 * @version $Revision$
 * $Date$:    Date of last commit
 *
 */
public class RuleEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5715297383078681645L;

	private String ruleName;
	
	private String ruleMetaData;
	
	private String rightHandSide;
	
	private String leftHandSide;

	
	public String getRuleName() {
		return ruleName;
	}

	public void setRuleName(String ruleName) {
		this.ruleName = ruleName;
	}

	public String getRuleMetaData() {
		return ruleMetaData;
	}

	public void setRuleMetaData(String ruleMetaData) {
		this.ruleMetaData = ruleMetaData;
	}

	public String getRightHandSide() {
		return rightHandSide;
	}

	public void setRightHandSide(String rightHandSide) {
		this.rightHandSide = rightHandSide;
	}

	public String getLeftHandSide() {
		return leftHandSide;
	}

	public void setLeftHandSide(String leftHandSide) {
		this.leftHandSide = leftHandSide;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		sb.append("rule \"").append(getRuleName()).append("\"\n");
		
		if (null != getRuleMetaData()) sb.append(getRuleMetaData()).append("\n");
		sb.append("    when \n");
		sb.append("    ").append(getLeftHandSide()).append("\n");
		sb.append("    then \n");
		sb.append("    ").append(getRightHandSide()).append("\n");
		sb.append("end \n");
		
		return sb.toString();
	}
}
