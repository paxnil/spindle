package com.iw.plugins.spindle.editors;

import org.eclipse.jface.text.ITextDoubleClickStrategy;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.pde.internal.ui.editor.XMLConfiguration;
import org.eclipse.pde.internal.ui.editor.text.IColorManager;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class SpindleXMLConfiguration extends XMLConfiguration{

  private ITextDoubleClickStrategy strategy;
  
  public SpindleXMLConfiguration(IColorManager colorManager) {
    super(colorManager);
  }

  /**
   * @see org.eclipse.jface.text.source.SourceViewerConfiguration#getDoubleClickStrategy(ISourceViewer, String)
   */
  public ITextDoubleClickStrategy getDoubleClickStrategy(
    ISourceViewer sourceViewer,
    String contentType) {
		if (strategy == null)
			strategy = new SpindleXMLDoubleClickStrategy();
		return strategy;
  }

}
