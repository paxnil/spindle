package com.iw.plugins.spindle.editors;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.swt.graphics.Image;


import com.iw.plugins.spindle.spec.IIdentifiable;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public abstract class AbstractIdentifiableLabelProvider implements ILabelProvider, ITableLabelProvider {

    ILabelProviderListener listener;

    public AbstractIdentifiableLabelProvider() {
      
    }
    
    public Image getImage(Object element) {
    	return null;
    }

    public String getText(Object element) {
      return ((IIdentifiable) element).getIdentifier();

    }

    public void addListener(ILabelProviderListener arg0) {
      listener = arg0;
    }

    public void dispose() {
      // shared image disposal handled by Plugin
    }

    public boolean isLabelProperty(Object element, String property) {
      return false;
    }

    public void removeListener(ILabelProviderListener arg0) {
      listener = null;
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(Object, int)
     */
    public Image getColumnImage(Object element, int columnIndex) {
      if (columnIndex != 1) {
        return null;
      }
      return getImage(element);
    }

    /**
     * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(Object, int)
     */
    public String getColumnText(Object element, int columnIndex) {
      if (columnIndex != 1) {
        return null;
      }
      return getText(element);
    }

  }
