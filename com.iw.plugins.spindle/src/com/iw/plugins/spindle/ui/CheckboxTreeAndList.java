package com.iw.plugins.spindle.ui;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.internal.misc.CheckboxTreeAndListGroup;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class CheckboxTreeAndList extends CheckboxTreeAndListGroup {

  /**
   * Constructor for CheckboxTreeAndList.
   * @param parent
   * @param rootObject
   * @param treeContentProvider
   * @param treeLabelProvider
   * @param listContentProvider
   * @param listLabelProvider
   * @param style
   * @param width
   * @param height
   */
  public CheckboxTreeAndList(
    Composite parent,
    Object rootObject,
    ITreeContentProvider treeContentProvider,
    ILabelProvider treeLabelProvider,
    IStructuredContentProvider listContentProvider,
    ILabelProvider listLabelProvider,
    int style,
    int width,
    int height) {
    	
    super(
      parent,
      rootObject,
      treeContentProvider,
      treeLabelProvider,
      listContentProvider,
      listLabelProvider,
      style,
      width,
      height);
  }

}
