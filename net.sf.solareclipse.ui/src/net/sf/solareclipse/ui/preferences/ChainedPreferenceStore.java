/**********************************************************************
Copyright (c) 2003  Widespace, OU  and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v1.0
which accompanies this distribution, and is available at
http://solareclipse.sourceforge.net/legal/cpl-v10.html

Contributors:
	Igor Malinin - initial contribution

$Id$
**********************************************************************/
package net.sf.solareclipse.ui.preferences;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;


/**
 * 
 * 
 * @author Igor Malinin
 */
public class ChainedPreferenceStore {
	public static void startPropagating(
		IPreferenceStore source, IPreferenceStore target, String[] keys
	) {
		startPropagating(source, target, new HashSet(Arrays.asList(keys)));
	}

	public static void startPropagating(
		final IPreferenceStore source,
		final IPreferenceStore target,
		final Set keys
	) {
		for (Iterator i = keys.iterator(); i.hasNext();) {
			String key = (String) i.next();
			target.setDefault(key, source.getString(key));
		}

		source.addPropertyChangeListener(new IPropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent event) {
				String key = event.getProperty();

				if (keys.contains(key)) {						
					target.setDefault(key, source.getString(key));
					if (target.isDefault(key)) {
						target.firePropertyChangeEvent(key,
							event.getOldValue(), event.getNewValue());
					}
				}
			}
		});
	}
}
