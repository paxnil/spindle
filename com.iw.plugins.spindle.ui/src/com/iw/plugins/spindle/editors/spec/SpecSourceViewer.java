/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others. All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Common Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: IBM Corporation - initial API and implementation
 ******************************************************************************/

package com.iw.plugins.spindle.editors.spec;

import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.source.IOverviewRuler;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.text.source.IVerticalRuler;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.swt.widgets.Composite;

public class SpecSourceViewer extends SourceViewer
{

  /**
   * Text operation code for requesting the outline for the current input.
   */
  public static final int SHOW_OUTLINE = 51;

  /**
   * Text operation code for requesting the outline for the element at the
   * current position.
   */
  public static final int OPEN_STRUCTURE = 52;

  /**
   * Text operation (fake) code for requesting asset chooser.
   */
  public static final int OPEN_ASSET_CHOOSER = 53;

  private IInformationPresenter fOutlinePresenter;
  private IInformationPresenter fStructurePresenter;
  private IInformationPresenter fChooseAssetPresenter;

  public SpecSourceViewer(Composite parent, IVerticalRuler verticalRuler,
      IOverviewRuler overviewRuler, boolean showAnnotationsOverview, int styles)
  {
    super(parent, verticalRuler, overviewRuler, showAnnotationsOverview, styles);
  }

  /*
   * @see ITextOperationTarget#doOperation(int)
   */
  public void doOperation(int operation)
  {
    if (getTextWidget() == null)
      return;

    switch (operation)
    {
      case SHOW_OUTLINE :
        fOutlinePresenter.showInformation();
        return;
      case OPEN_STRUCTURE :
        fStructurePresenter.showInformation();
        return;
      case OPEN_ASSET_CHOOSER :
        fChooseAssetPresenter.showInformation();
        return;
    }

    super.doOperation(operation);
  }

  /*
   * @see ITextOperationTarget#canDoOperation(int)
   */
  public boolean canDoOperation(int operation)
  {
    if (operation == SHOW_OUTLINE)
      return fOutlinePresenter != null;
    if (operation == OPEN_STRUCTURE)
      return fStructurePresenter != null;
    if (operation == OPEN_ASSET_CHOOSER)
      return true;
    if (operation == ISourceViewer.FORMAT && getSelectedRange().y != 0)
      return false;
    return super.canDoOperation(operation);
  }

  /*
   * @see ISourceViewer#configure(SourceViewerConfiguration)
   */
  public void configure(SourceViewerConfiguration configuration)
  {
    super.configure(configuration);
    if (configuration instanceof SpecEditorConfiguration)
    {
      fOutlinePresenter = ((SpecEditorConfiguration) configuration)
          .getXMLOutlinePresenter(this);
      fOutlinePresenter.install(this);
    }
    if (configuration instanceof SpecEditorConfiguration)
    {
      fStructurePresenter = ((SpecEditorConfiguration) configuration)
          .getStructureOutlinePresenter(this);
      fStructurePresenter.install(this);
    }
    if (configuration instanceof SpecEditorConfiguration)
    {
      fChooseAssetPresenter = ((SpecEditorConfiguration) configuration)
          .getAssetChooserPresenter(this);
      fChooseAssetPresenter.install(this);
    }
  }

  /*
   * @see TextViewer#handleDispose()
   */
  protected void handleDispose()
  {
    if (fOutlinePresenter != null)
    {
      fOutlinePresenter.uninstall();
      fOutlinePresenter = null;
    }
    if (fStructurePresenter != null)
    {
      fStructurePresenter.uninstall();
      fStructurePresenter = null;
    }
    super.handleDispose();
  }
}