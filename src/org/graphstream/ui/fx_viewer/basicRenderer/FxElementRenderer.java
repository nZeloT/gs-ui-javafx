/*
 * Copyright 2006 - 2016
 *     Stefan Balev     <stefan.balev@graphstream-project.org>
 *     Julien Baudry    <julien.baudry@graphstream-project.org>
 *     Antoine Dutot    <antoine.dutot@graphstream-project.org>
 *     Yoann Pigné      <yoann.pigne@graphstream-project.org>
 *     Guilhelm Savin   <guilhelm.savin@graphstream-project.org>
 * 
 * This file is part of GraphStream <http://graphstream-project.org>.
 * 
 * GraphStream is a library whose purpose is to handle static or dynamic
 * graph, create them from scratch, file or any source and display them.
 * 
 * This program is free software distributed under the terms of two licenses, the
 * CeCILL-C license that fits European law, and the GNU Lesser General Public
 * License. You can  use, modify and/ or redistribute the software under the terms
 * of the CeCILL-C license as circulated by CEA, CNRS and INRIA at the following
 * URL <http://www.cecill.info> or under the terms of the GNU LGPL as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C and LGPL licenses and that you accept their terms.
 */
package org.graphstream.ui.fx_viewer.basicRenderer;

import org.graphstream.graph.Element;
import org.graphstream.ui.fx_viewer.util.FxDefaultCamera;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.graphicGraph.GraphicSprite;
import org.graphstream.ui.graphicGraph.StyleGroup;
import org.graphstream.ui.graphicGraph.StyleGroup.ElementEvents;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants;
import org.graphstream.ui.graphicGraph.stylesheet.StyleConstants.Units;
import org.graphstream.ui.view.Camera;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.transform.Affine;

public abstract class FxElementRenderer {
	// Attribute

	/**
	 * Allow to know if an event began or ended.
	 */
	protected boolean hadEvents = false;

	protected Font textFont;

	protected Color textColor;

	protected int textSize;

	// Constructor

	/**
	 * New swing element renderer for the given style group.
	 */
	public FxElementRenderer() {
	}

	// Command

	/**
	 * Render all the (visible) elements of the group.
	 */
	public void render(StyleGroup group, GraphicsContext g, Camera camera) {
		setupRenderingPass(group, g, camera);
		pushStyle(group, g, camera);

		for (Element e : group.bulkElements()) {
			GraphicElement ge = (GraphicElement) e;

			if (camera.isVisible(ge))
				renderElement(group, g, camera, ge);
			else
				elementInvisible(group, g, camera, ge);
		}

		if (group.hasDynamicElements()) {
			for (Element e : group.dynamicElements()) {
				GraphicElement ge = (GraphicElement) e;

				if (camera.isVisible(ge)) {
					if (!group.elementHasEvents(ge)) {
						pushDynStyle(group, g, camera, ge);
						renderElement(group, g, camera, ge);
					}
				} else {
					elementInvisible(group, g, camera, ge);
				}
			}
		}

		if (group.hasEventElements()) {
			for (ElementEvents event : group.elementsEvents()) {
				GraphicElement ge = (GraphicElement) event.getElement();

				if (camera.isVisible(ge)) {
					event.activate();
					pushStyle(group, g, camera);
					renderElement(group, g, camera, ge);
					event.deactivate();
				} else {
					elementInvisible(group, g, camera, ge);
				}
			}

			hadEvents = true;
		} else {
			hadEvents = false;
		}
	}

	/**
	 * Called before the whole rendering pass for all elements.
	 * 
	 * @param g
	 *            The Swing graphics.
	 * @param camera
	 *            The camera.
	 */
	protected abstract void setupRenderingPass(StyleGroup group, GraphicsContext g,
			Camera camera);

	/**
	 * Called before the rendering of bulk and event elements.
	 * 
	 * @param g
	 *            The Swing graphics.
	 * @param camera
	 *            The camera.
	 */
	protected abstract void pushStyle(StyleGroup group, GraphicsContext g,
			Camera camera);

	/**
	 * Called before the rendering of elements on dynamic styles. This must only
	 * change the style properties that can change dynamically.
	 * 
	 * @param g
	 *            The Swing graphics.
	 * @param camera
	 *            The camera.
	 * @param element
	 *            The graphic element concerned by the dynamic style change.
	 */
	protected abstract void pushDynStyle(StyleGroup group, GraphicsContext g,
			Camera camera, GraphicElement element);

	/**
	 * Render a single element knowing the style is already prepared. Elements
	 * that are not visible are not drawn.
	 * 
	 * @param g
	 *            The Swing graphics.
	 * @param camera
	 *            The camera.
	 * @param element
	 *            The element to render.
	 */
	protected abstract void renderElement(StyleGroup group, GraphicsContext g,
			Camera camera, GraphicElement element);

	/**
	 * Called during rendering in place of
	 * {@link #renderElement(StyleGroup, GraphicsContext, Camera, GraphicElement)}
	 * to signal that the given element is not inside the view. The
	 * renderElement() method will be called as soon as the element becomes
	 * visible anew.
	 * 
	 * @param g
	 *            The Swing graphics.
	 * @param camera
	 *            The camera.
	 * @param element
	 *            The element to render.
	 */
	protected abstract void elementInvisible(StyleGroup group, GraphicsContext g,
			Camera camera, GraphicElement element);

	// Utility

	protected void configureText(StyleGroup group, Camera camera) {
		String fontName = group.getTextFont();
		//StyleConstants.TextStyle textStyle = group.getTextStyle();
		
		textSize = (int) group.getTextSize().value;
		textColor = Color.rgb(group.getTextColor(0).getRed(), group.getTextColor(0).getGreen(), group.getTextColor(0).getBlue());
		textFont = Font.font(fontName, textSize);
	}
	
	/**
	 * Get awt color in group and convert to javafx Color
	 * @param group
	 * @param id
	 * @return javafx.scene.paint.Color
	 */
	public static Color getFillColor(StyleGroup group, int id) {
		return  Color.rgb(group.getFillColor(id).getRed(), group.getFillColor(id).getGreen(), group.getFillColor(id).getBlue());
	}
	
	protected void renderText(StyleGroup group, GraphicsContext g, Camera camera,
			GraphicElement element) {
		String label = element.getLabel();
		
		if (label != null && group.getTextMode() != StyleConstants.TextMode.HIDDEN
				&& group.getTextVisibilityMode() != StyleConstants.TextVisibilityMode.HIDDEN) {

			Point3 p = null;
			GraphicSprite s = null;
			Point2D pos = null;

			if (element instanceof GraphicSprite) {
				s   = (GraphicSprite) element;
				pos = ((FxDefaultCamera) camera).getSpritePosition(s,
					new Point2D(0, 0), StyleConstants.Units.GU);
			}

			if (pos != null && s.getUnits() == Units.PX) {
				double w = camera.getMetrics().lengthToPx(group.getSize(),
						0);
				p = camera.transformGuToPx(pos.getX(), pos.getY(), 0);
				p.x += w/2;
			} else if (s != null && s.getUnits() == Units.PERCENTS) {
				double w = camera.getMetrics().lengthToPx(group.getSize(),
						0);
				p = camera.transformGuToPx(camera.getMetrics().viewport[2] * pos.getX(),
					camera.getMetrics().viewport[3] *  pos.getY(), 0);
				p.x += (w/2);
			} else {
				double w = camera.getMetrics().lengthToGu(group.getSize(),
						0);
				p = camera.transformGuToPx(element.getX() + (w / 2), element
						.getY(), 0);
			}

			Affine Tx = g.getTransform();
			Paint fill = g.getFill();
			Paint stroke = g.getStroke();
			
			g.setFill(textColor);
	        g.setStroke(textColor);
			
			g.setFont(textFont);
			g.setTransform(new Affine());
			g.fillText(label, (float) p.x, (float) (p.y + textSize / 3)); // approximation
			// to gain time.
			g.setTransform(Tx);
			
			g.setFill(fill);
			g.setStroke(stroke);
		}
	}

	protected Color interpolateColor(StyleGroup group, GraphicElement element) {
		Color color = getFillColor(group, 0);

		int n = group.getFillColorCount();

		if (n > 1) {
			if (element.hasNumber("ui.color") && n > 1) {
				double value = element.getNumber("ui.color");

				if (value < 0)
					value = 0;
				else if (value > 1)
					value = 1;

				if (value == 1) {
					color = getFillColor(group, n - 1); // Simplification,
					// faster.
				} else if (value != 0) // If value == 0, color is already set
				// above.
				{
					double div = 1f / (n - 1);
					int col = (int) (value / div);

					div = (value - (div * col)) / div;
					// div = value / div - col;

					Color color0 = getFillColor(group, col);
					Color color1 = getFillColor(group, col + 1);
					double red = (((color0.getRed()*255) * (1 - div)) + ((color1
							.getRed()*255) * div)) / 255f;
					double green = (((color0.getGreen()*255) * (1 - div)) + ((color1
							.getGreen()*255) * div)) / 255f;
					double blue = (((color0.getBlue()*255) * (1 - div)) + ((color1
							.getBlue()*255) * div)) / 255f;
					double alpha = (((color0.getOpacity()*255) * (1 - div)) + ((color1
							.getOpacity()*255) * div)) / 255f;
					
					color = Color.color(red, green, blue, alpha);
				}
			} else if (element.hasAttribute("ui.color", Color.class)) {
				color = element.getAttribute("ui.color", Color.class);
			}
		} else if (element.hasAttribute("ui.color", Color.class)) {
			color = element.getAttribute("ui.color", Color.class);
		}

		return color;
	}
}