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

package de.walware.rj.eclient.graphics;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.LineAttributes;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.graphics.Transform;

import de.walware.rj.graphic.RGraphicInstruction;
import de.walware.rj.server.client.RClientGraphic;

import de.walware.rj.eclient.internal.graphics.CircleElement;
import de.walware.rj.eclient.internal.graphics.ClipSetting;
import de.walware.rj.eclient.internal.graphics.ColorSetting;
import de.walware.rj.eclient.internal.graphics.FillSetting;
import de.walware.rj.eclient.internal.graphics.FontSetting;
import de.walware.rj.eclient.internal.graphics.GraphicInitialization;
import de.walware.rj.eclient.internal.graphics.LineElement;
import de.walware.rj.eclient.internal.graphics.LineSetting;
import de.walware.rj.eclient.internal.graphics.PathElement;
import de.walware.rj.eclient.internal.graphics.PolygonElement;
import de.walware.rj.eclient.internal.graphics.PolylineElement;
import de.walware.rj.eclient.internal.graphics.RasterElement;
import de.walware.rj.eclient.internal.graphics.RectElement;
import de.walware.rj.eclient.internal.graphics.TextElement;


public class DefaultGCRenderer {
	
	
	private static int swtLineJoin2Cap(final int join) {
		return (join == SWT.JOIN_ROUND) ? SWT.CAP_ROUND : SWT.CAP_FLAT;
	}
	
	
	private double scale= 1.0f;
	
	private final LineAttributes lineAttributes= new LineAttributes(1.0f);
	private Color lineColor;
	private int lineAlpha;
	private Color fillColor;
	private int fillAlpha;
	private final double[] fontProperties= new double[1];
	private int xMax;
	private int yMax;
	
	
	public void clear(final double scale) {
		this.scale= scale;
		this.lineColor= null;
		this.lineAlpha= 0xff;
		this.fillColor= null;
		this.fillAlpha= 0xff;
		this.lineAttributes.style= SWT.LINE_SOLID;
		this.lineAttributes.width= (float) scale;
		this.lineAttributes.cap= SWT.CAP_ROUND;
		this.lineAttributes.join= SWT.JOIN_ROUND;
		this.lineAttributes.miterLimit= (float) (10.0 * scale);
		this.xMax= 0;
		this.yMax= 0;
	}
	
	public void paint(final GC gc, final List<? extends IERGraphicInstruction> instructions) {
		final Transform defaultTransform= null;
		final Transform tempTransform= new Transform(gc.getDevice());
		final double scale= this.scale;
		int currentAlpha= -1;
		int currentInterpolation= -1;
		int currentFillRule= -1;
		Color lineColor= this.lineColor;
		int lineAlpha= this.lineAlpha;
		Color fillColor= this.fillColor;
		int fillAlpha= this.fillAlpha;
		
		try {
			gc.setAdvanced(true);
			gc.setAntialias(SWT.ON);
			gc.setTextAntialias(SWT.ON);
			gc.setLineAttributes(this.lineAttributes);
			gc.setTransform(defaultTransform);
			gc.setAlpha(currentAlpha);
			if (this.lineColor != null) {
				gc.setForeground(this.lineColor);
			}
			if (this.fillColor != null) {
				gc.setBackground(this.fillColor);
			}
			int ixmax= this.xMax;
			int iymax= this.yMax;
			
			for (final IERGraphicInstruction instr : instructions) {
				switch (instr.getInstructionType()) {
				case RGraphicInstruction.INIT: {
						final GraphicInitialization init= (GraphicInitialization) instr;
						ixmax= (int) (init.width * scale + 0.5);
						iymax= (int) (init.height * scale + 0.5);
						gc.setBackground(init.swtCanvasColor);
						gc.setAlpha(currentAlpha= 0xff);
						gc.setClipping((Rectangle) null);
						gc.fillRectangle(0, 0, ixmax, iymax);
	//					gc.setBackground(fillColor= gc.getDevice().getSystemColor(SWT.COLOR_WHITE));
	//					gc.setForeground(lineColor= gc.getDevice().getSystemColor(SWT.COLOR_BLACK));
						gc.setClipping(0, 0, ixmax, iymax);
						continue;
					}
				
				case RGraphicInstruction.SET_CLIP: {
						final ClipSetting setting= (ClipSetting) instr;
						
						final int ix0= (int) (setting.x0 * scale + 1.5);
						final int iy0= (int) (setting.y0 * scale + 1.5);
						gc.setClipping(ix0, iy0,
								(int) Math.min((setting.x1 * scale + 0.5), ixmax) - ix0,
								(int) Math.min((setting.y1 * scale + 0.5), iymax) - iy0 );
						continue;
					}
				
				case RGraphicInstruction.SET_COLOR: {
						final ColorSetting setting= (ColorSetting) instr;
						
						lineAlpha= setting.getAlpha();
						gc.setForeground(lineColor= setting.swtColor);
						continue;
					}
				
				case RGraphicInstruction.SET_FILL: {
						final FillSetting setting= (FillSetting) instr;
						
						fillAlpha= setting.getAlpha();
						gc.setBackground(fillColor= setting.swtColor);
						continue;
					}
				
				case RGraphicInstruction.SET_LINE: {
						final LineSetting setting= (LineSetting) instr;
						
						this.lineAttributes.cap= setting.swtCap();
						this.lineAttributes.join= setting.swtJoin();
						this.lineAttributes.miterLimit= (float) (setting.joinMiterLimit * scale);
						switch (setting.type) {
						case LineSetting.TYPE_SOLID:
							this.lineAttributes.style= SWT.LINE_SOLID;
							this.lineAttributes.width= (float) (setting.width * scale);
							gc.setLineAttributes(this.lineAttributes);
							continue;
						case LineSetting.TYPE_BLANK:
							this.lineAttributes.style= SWT.LINE_SOLID;
							this.lineAttributes.width= 0.0f;
							gc.setLineAttributes(this.lineAttributes);
							continue;
//						case 0x44:
//							fTempLineAttributes.style= SWT.LINE_DASH;
//							fTempLineAttributes.width= (float) (setting.width * scale);
//							gc.setLineAttributes(fTempLineAttributes);
//							continue;
//						case 0x13:
//							fTempLineAttributes.style= SWT.LINE_DOT;
//							fTempLineAttributes.width= (float) (setting.width * scale);
//							gc.setLineAttributes(fTempLineAttributes);
//							continue;
//						case 0x1343:
//							fTempLineAttributes.style= SWT.LINE_DASHDOT;
//							fTempLineAttributes.width= (float) (setting.width * scale);
//							gc.setLineAttributes(fTempLineAttributes);
//							continue;
						default:
							this.lineAttributes.style= SWT.LINE_SOLID;
							this.lineAttributes.width= (float) (setting.width * scale);
							gc.setLineAttributes(this.lineAttributes);
							gc.setLineDash(setting.swtDashes());
							continue;
						}
					}
				
				case RGraphicInstruction.SET_FONT: {
						final FontSetting setting= (FontSetting) instr;
						
						gc.setFont(setting.swtFont);
						this.fontProperties[0]= setting.swtProperties[0];
						continue;
					}
				
				case RGraphicInstruction.DRAW_LINE: {
						final LineElement element= (LineElement) instr;
						
						if (lineAlpha != currentAlpha) {
							gc.setAlpha(currentAlpha= lineAlpha);
						}
						gc.drawLine(
								(int) (element.x0 * scale + 0.5),
								(int) (element.y0 * scale + 0.5),
								(int) (element.x1 * scale + 0.5),
								(int) (element.y1 * scale + 0.5) );
						continue;
					}
				
				case RGraphicInstruction.DRAW_RECTANGLE: {
						final RectElement element= (RectElement) instr;
						
						// small dots: priority on size / center
						// default: priority on edges
						final int ix0, iy0;
						final int iw, ih;
						if ((element.x1 - element.x0) < 5.1111 && (element.y1 - element.y0) < 5.1111) {
							iw= (int) ((element.x1 - element.x0) * scale + 0.5);
							if (lineAlpha == 0) {
								ix0= (int) (((element.x0 + element.x1) * scale - iw) / 2.0 + 0.5);
//								System.out.println("(a==0) ix0= " + ix0 + " (" + (float) (((element.x0 + element.x1) * scale - iw) / 2.0) + ")"
//										+ ", x0= " + (float) (element.x0 * scale) + ", iw= " + iw + " (" + (float) ((element.x1 - element.x0) * scale) + ")"
//										+ ", c= " + (float) ((element.x0 + element.x1) * scale / 2.0));
							}
							else {
								ix0= (int) (((element.x0 + element.x1) * scale - (iw + 1)) / 2.0 + 0.5);
//								System.out.println("(a!=0) ix0= " + ix0 + " (" + (float) (((element.x0 + element.x1) * scale - (iw + 1)) / 2.0) + ")"
//										+ ", x0= " + (float) (element.x0 * scale) + ", iw= " + iw + "+1 (" + (float) ((element.x1 - element.x0) * scale) + ")"
//										+ ", c= " + (float) ((element.x0 + element.x1) * scale / 2.0));
							}
							ih= (int) ((element.y1 - element.y0) * scale + 0.5);
							if (lineAlpha == 0) {
								iy0= (int) (((element.y0 + element.y1) * scale - ih) / 2.0 + 0.5);
							}
							else {
								iy0= (int) (((element.y0 + element.y1) * scale - (ih + 1)) / 2.0 + 0.5);
							}
						}
						else {
							ix0= (int) (element.x0 * scale + 0.5);
							if (lineAlpha == 0) {
								iw= (int) (element.x1 * scale + 0.5) - ix0 + 1;
							}
							else {
								iw= (int) (element.x1 * scale + 0.5) - ix0;
							}
							iy0= (int) (element.y0 * scale + 0.5);
							if (lineAlpha == 0) {
								ih= (int) (element.y1 * scale + 0.5) - iy0 + 1;
							}
							else {
								ih= (int) (element.y1 * scale + 0.5) - iy0;
							}
						}
						
						if (iw == 0 || ih == 0) {
							if (lineAlpha == 0) {
								continue;
							}
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.setLineCap(swtLineJoin2Cap(this.lineAttributes.join));
							gc.drawLine(ix0, iy0, ix0 + iw, iy0 + ih);
							gc.setLineCap(this.lineAttributes.cap);
							continue;
						}
						if (fillAlpha != 0) {
							if (lineAlpha == 0) {
								if (fillAlpha != currentAlpha) {
									gc.setAlpha(currentAlpha= fillAlpha);
								}
								gc.fillRectangle(ix0, iy0, iw, ih);
								continue;
							}
							if (iw > 1 && ih > 1) {
								if (fillAlpha != currentAlpha) {
									gc.setAlpha(currentAlpha= fillAlpha);
								}
								gc.fillRectangle(ix0 + 1, iy0 + 1, iw - 1, ih - 1);
							}
						}
						if (lineAlpha != 0) {
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.drawRectangle(ix0, iy0, iw, ih);
						}
						continue;
					}
				
				case RGraphicInstruction.DRAW_POLYLINE: {
						final PolylineElement element= (PolylineElement) instr;
						
						final int[] icoord;
						{	final int n= element.x.length;
							icoord= new int[n * 2];
							for (int i= 0, j= 0; j < n; j++) {
								icoord[i++]= (int) (element.x[j] * scale + 0.5);
								icoord[i++]= (int) (element.y[j] * scale + 0.5);
							}
						}
						
						if (lineAlpha != currentAlpha) {
							gc.setAlpha(currentAlpha= lineAlpha);
						}
						gc.drawPolyline(icoord);
						continue;
					}
				
				case RGraphicInstruction.DRAW_POLYGON: {
						final PolygonElement element= (PolygonElement) instr;
						
						final int[] icoord;
						{	final int n= element.x.length;
							icoord= new int[n * 2];
							for (int i= 0, j= 0; j < n; j++) {
								icoord[i++]= (int) (element.x[j] * scale + 0.5);
								icoord[i++]= (int) (element.y[j] * scale + 0.5);
							}
						}
						
						if (fillAlpha != 0) {
							if (fillAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= fillAlpha);
							}
							if (SWT.FILL_EVEN_ODD != currentFillRule) {
								gc.setFillRule(currentFillRule= SWT.FILL_EVEN_ODD);
							}
							gc.fillPolygon(icoord);
						}
						if (lineAlpha != 0) {
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.drawPolygon(icoord);
						}
						continue;
					}
				
				case RGraphicInstruction.DRAW_PATH: {
						final PathElement element= (PathElement) instr;
						
						{	final int fillRule= ((element.mode & RClientGraphic.MASK_FILL_RULE) == RClientGraphic.FILL_WIND_NON_ZERO) ?
									SWT.FILL_WINDING : SWT.FILL_EVEN_ODD;
							if (fillRule != currentFillRule) {
								gc.setFillRule(currentFillRule= fillRule);
							}
						}
						if (fillAlpha != 0) {
							if (fillAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= fillAlpha);
							}
							gc.fillPath(element.swtPath);
						}
						if (lineAlpha != 0) {
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.drawPath(element.swtPath);
						}
						continue;
					}
				
				case RGraphicInstruction.DRAW_CIRCLE: {
						final CircleElement element= (CircleElement) instr;
						
						final int id= (int) (element.r * 2.0 + 0.5);
						tempTransform.setElements(1, 0, 0, 1,
								(float) (element.x - id / 2.0),
								(float) (element.y - id / 2.0) );
						gc.setTransform(tempTransform);
						
						if (fillAlpha != 0) {
							if (fillAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= fillAlpha);
							}
							gc.fillOval(1, 1, id-1, id-1);
						}
						if (lineAlpha != 0) {
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.drawOval(0, 0, id, id);
						}
						
						gc.setTransform(defaultTransform);
						continue;
					}
				
				case RGraphicInstruction.DRAW_TEXT: {
						final TextElement element= (TextElement) instr;
						
						final double hShift;
						if (element.horizontalAdjust != 0.0) {
							hShift= element.horizontalAdjust * element.swtStrWidth;
						}
						else {
							hShift= 0.0;
						}
						
						if (element.rotateDegree != 0.0) {
							tempTransform.setElements(1, 0, 0, 1,
									(float) (element.x * scale),
									(float) (element.y * scale) );
							tempTransform.rotate((float) -element.rotateDegree);
							tempTransform.translate(
									(float) Math.floor(1.1111 - hShift),
									(float) Math.floor(0.0511 - this.fontProperties[0]) );
							gc.setTransform(tempTransform);
							
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.drawString(element.text, 0, 0, true);
							
							gc.setTransform(defaultTransform);
							continue;
						}
						else {
							if (lineAlpha != currentAlpha) {
								gc.setAlpha(currentAlpha= lineAlpha);
							}
							gc.drawString(element.text,
									(int) Math.floor(1.1111 + element.x - hShift),
									(int) Math.floor(0.6111 + element.y - this.fontProperties[0]),
									true );
							
							continue;
						}
					}
				
				case RGraphicInstruction.DRAW_RASTER: {
						final RasterElement element= (RasterElement) instr;
						
						if (0xff != currentAlpha) {
							gc.setAlpha(currentAlpha= 0xff);
						}
						{	final int interpolation= (element.interpolate) ? SWT.LOW : SWT.NONE;
							if (interpolation != currentInterpolation) {
								gc.setInterpolation(currentInterpolation= interpolation);
							}
						}
						
						final int ix, iy;
						final int ih, iw;
						if (element.width >= 0) {
							ix= (int) Math.floor(element.x * scale + 0.5);
							iw= (int) (element.width * scale + 0.5);
						}
						else {
							ix= (int) Math.floor((element.x + element.height) * scale + 0.5);
							iw= (int) (-element.width * scale + 0.5);
						}
						if (element.height >= 0) {
							iy= (int) Math.floor(element.y * scale + 0.5);
							ih= (int) (element.height * scale + 0.5);
						}
						else {
							iy= (int) Math.floor((element.y + element.height) * scale + 0.5);
							ih= (int) (-element.height * scale + 0.5);
						}
						
						if (element.rotateDegree != 0.0) {
							tempTransform.setElements(1, 0, 0, 1,
									(float) (element.x * scale),
									(float) (element.y * scale) );
							tempTransform.rotate(-(float) element.rotateDegree);
							if (element.width < 0 || element.height < 0) {
								tempTransform.translate(
										(element.width < 0) ? -iw : 0,
										(element.height < 0) ? -ih : 0 );
							}
							gc.setTransform(tempTransform);
							
							gc.drawImage(element.swtImage, 0, 0, element.imgWidth, element.imgHeight,
									0, 0, iw, ih );
							
							gc.setTransform(defaultTransform);
							continue;
						}
						else {
							gc.drawImage(element.swtImage, 0, 0, element.imgWidth, element.imgHeight,
									ix, iy, iw, ih );
							
							continue;
						}
					}
				}
			}
			
			this.lineColor= lineColor;
			this.lineAlpha= lineAlpha;
			this.fillColor= fillColor;
			this.fillAlpha= fillAlpha;
			this.xMax= ixmax;
			this.yMax= iymax;
		}
		finally {
			tempTransform.dispose();
		}
	}
	
}
