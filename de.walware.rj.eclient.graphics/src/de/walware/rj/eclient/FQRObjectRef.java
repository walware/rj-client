/*=============================================================================#
 # Copyright (c) 2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.eclient;

import de.walware.ecommons.ts.ITool;

import de.walware.rj.data.RLanguage;
import de.walware.rj.data.RObject;
import de.walware.rj.data.RReference;
import de.walware.rj.services.IFQRObjectRef;


public class FQRObjectRef implements IFQRObjectRef {
	
	
	private static boolean isValidEnvObject(final RObject env) {
		switch (env.getRObjectType()) {
		case RObject.TYPE_REFERENCE:
			return (((RReference) env).getReferencedRObjectType() == RObject.TYPE_ENV);
		case RObject.TYPE_LANGUAGE:
			return (((RLanguage) env).getLanguageType() == RLanguage.CALL);
		default:
			return false;
		}
	}
	
	private static boolean isValidNameObject(final RObject env) {
		switch (env.getRObjectType()) {
		case RObject.TYPE_LANGUAGE:
			switch (((RLanguage) env).getLanguageType()) {
			case RLanguage.NAME:
			case RLanguage.CALL:
				return true;
			default:
				return false;
			}
		default:
			return false;
		}
	}
	
	
	private final ITool tool;
	
	private final RObject env;
	
	private final RObject name;
	
	
	public FQRObjectRef(final ITool tool, final RObject env, final RObject name) {
		if (tool == null) {
			throw new NullPointerException("tool"); //$NON-NLS-1$
		}
		if (env == null) {
			throw new NullPointerException("env"); //$NON-NLS-1$
		}
		if (!isValidEnvObject(env)) {
			throw new IllegalArgumentException("env"); //$NON-NLS-1$
		}
		if (name == null) {
			throw new NullPointerException("name"); //$NON-NLS-1$
		}
		if (!isValidNameObject(name)) {
			throw new IllegalArgumentException("name"); //$NON-NLS-1$
		}
		
		this.tool= tool;
		this.env= env;
		this.name= name;
	}
	
	
	@Override
	public ITool getRHandle() {
		return this.tool;
	}
	
	@Override
	public RObject getEnv() {
		return this.env;
	}
	
	@Override
	public RObject getName() {
		return this.name;
	}
	
	
	@Override
	public String toString() {
		return this.env + "\n" + this.name.toString();
	}
	
}
