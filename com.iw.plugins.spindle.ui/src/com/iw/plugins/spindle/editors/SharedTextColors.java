
package com.iw.plugins.spindle.editors;

import java.util.Iterator;
import org.eclipse.swt.widgets.Display;
import java.util.Map;
import java.util.HashMap;
import org.eclipse.swt.graphics.Color;
import org.eclipse.jface.text.source.ISharedTextColors;
import org.eclipse.swt.graphics.RGB;


public class SharedTextColors implements ISharedTextColors {

	private Map fColorMap;

	public SharedTextColors() {
		super();
	}

	public Color getColor(RGB rgb) {
		if (rgb == null)
			return null;
			
		if (fColorMap == null)
			fColorMap= new HashMap(2);
		
		Display display= Display.getCurrent();
		
		Map colorTable= (Map) fColorMap.get(display);
		if (colorTable == null) {
			colorTable= new HashMap(10);
			fColorMap.put(display, colorTable);
		}
			
		Color color= (Color) colorTable.get(rgb);
		if (color == null || color.isDisposed()) {
			color= new Color(display, rgb);
			colorTable.put(rgb, color);
		}
			
		return color;
	}

	/*
	 * @see ISharedTextColors#dispose()
	 */
	public void dispose() {
		if (fColorMap != null) {
			Iterator j= fColorMap.values().iterator();
			while (j.hasNext()) {
				Iterator i= ((Map) j.next()).values().iterator();
				while (i.hasNext())
					((Color) i.next()).dispose();
			}
		}
	}
	
}
