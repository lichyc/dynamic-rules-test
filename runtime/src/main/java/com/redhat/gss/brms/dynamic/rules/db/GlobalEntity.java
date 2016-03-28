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
public class GlobalEntity implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -3368750792753822287L;

	public GlobalEntity() {
		super();
	}
	
	public GlobalEntity(String absoluteClassName, String objectName) {
		super();
		setObjectName(objectName);
		setAbsoluteClassName(absoluteClassName);
	}
	
	private String objectName;
	
	private String absoluteClassName;

	public String getObjectName() {
		return objectName;
	}

	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public String getAbsouteClassName() {
		return absoluteClassName;
	}

	public void setAbsoluteClassName(String absouteClassName) {
		this.absoluteClassName = absouteClassName;
	}

	@Override
	public String toString() {
		return "global " + getAbsouteClassName() + " "
				+ getObjectName();
	}

}
