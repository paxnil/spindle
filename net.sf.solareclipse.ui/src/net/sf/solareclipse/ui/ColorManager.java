package net.sf.solareclipse.ui;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


/**
 * Color Manager.
 * 
 * @author Igor Malinin
 */
public class ColorManager {
	protected Map colors = new HashMap( 10 );

	public void bindColor( String key, RGB rgb ) {
		Object value = colors.get( key );
		if ( value != null ) {
			throw new UnsupportedOperationException();
		}

		Color color = new Color( Display.getCurrent(), rgb );

		colors.put( key, color );
	}

	public void unbindColor( String key ) {
		Color color = (Color) colors.remove( key );
		if ( color != null ) {
			color.dispose();
		}
	}

	public Color getColor( String key ) {
		return (Color) colors.get( key );
	}

	public void dispose() {
		Iterator i = colors.values().iterator();
		while ( i.hasNext() ) {
			((Color) i.next()).dispose();
		}
	}
}
