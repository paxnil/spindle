package com.iw.plugins.spindle.ui.text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

/**
 * @author administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 */
public class ColorManager implements ISpindleColorManager {

	protected Map colors = new HashMap(7);

	public void dispose() {
		for (Iterator iterator = colors.values().iterator(); iterator.hasNext();) {
			Color element = (Color) iterator.next();
			element.dispose();
		}		
	}
	
	public Color getColor(RGB rgb) {
		Color color= (Color) colors.get(rgb);
		if (color == null) {
			color= new Color(Display.getCurrent(), rgb);
			colors.put(rgb, color);
		}
		return color;
	}

}
