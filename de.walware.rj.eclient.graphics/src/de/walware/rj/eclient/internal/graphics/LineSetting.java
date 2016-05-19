/*=============================================================================#
 # Copyright (c) 2009-2016 Stephan Wahlbrink (WalWare.de) and others.
 # All rights reserved. This program and the accompanying materials
 # are made available under the terms of the Eclipse Public License v1.0
 # which accompanies this distribution, and is available at
 # http://www.eclipse.org/legal/epl-v10.html
 # 
 # Contributors:
 #     Stephan Wahlbrink - initial API and implementation
 #=============================================================================*/

package de.walware.rj.eclient.internal.graphics;

import org.eclipse.swt.SWT;

import de.walware.rj.graphic.RLineSetting;

import de.walware.rj.eclient.graphics.IERGraphicInstruction;


public final class LineSetting extends RLineSetting implements IERGraphicInstruction {
	
	
	public LineSetting(final int type, final float width, final byte cap, final byte join,
			final float joinMiterLimit) {
		super(type, width, cap, join, joinMiterLimit);
	}
	
	
	public int swtCap() {
		switch(this.cap) {
		case CAP_ROUND:
			return SWT.CAP_ROUND;
		case CAP_BUTT:
			return SWT.CAP_FLAT;
		case CAP_SQUARE:
			return SWT.CAP_SQUARE;
		default:
			assert (false);
			return SWT.CAP_ROUND;
		}
	}
	
	public int swtJoin() {
		switch (this.join) {
		case JOIN_ROUND:
			return SWT.JOIN_ROUND;
		case JOIN_MITER:
			return SWT.JOIN_MITER;
		case JOIN_BEVEL:
			return SWT.JOIN_BEVEL;
		default:
			assert (false);
			return SWT.JOIN_ROUND;
		}
	}
	
	public int[] swtDashes() {
		int rPattern= this.type;
		int length= 0;
		while (rPattern != 0) {
			length++;
			rPattern>>>= 4;
		}
		final int[] dashes= new int[length];
		rPattern= this.type;
		for (int i = 0; i < length; i++) {
			dashes[i]= (rPattern & 0xf);
			rPattern>>>= 4;
		}
		return dashes;
	}
	
}
