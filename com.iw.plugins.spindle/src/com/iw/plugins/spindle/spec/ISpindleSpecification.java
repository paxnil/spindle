package com.iw.plugins.spindle.spec;

import java.beans.PropertyChangeListener;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface ISpindleSpecification extends IIdentifiable {
	
	public void addPropertyChangeListener(PropertyChangeListener l);
	public void removePropertyChangeListener(PropertyChangeListener l);

}
