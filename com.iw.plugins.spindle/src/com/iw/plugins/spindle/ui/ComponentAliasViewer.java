/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

public class ComponentAliasViewer extends HelpViewer {

  String id;
  String alias;
  TapestryComponentModel componentModel;

  public ComponentAliasViewer(String id, String alias, TapestryComponentModel componentModel) {
    this.id = id;
    this.alias = alias;
    this.componentModel = componentModel;
  }

  /**
   * @see HelpViewer#createClient(Composite)
   */
  public Control createClient(Composite parent) {
    Composite container = new Composite(parent, SWT.NULL);

    GridData gd;

    GridLayout layout = new GridLayout();
    layout.marginHeight = 8;
    layout.marginHeight = 8;
    container.setLayout(layout);
    container.setLayoutData(
      new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));


    Label dataLabel = new Label(container, SWT.BORDER);
    dataLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    
    StringBuffer buffer = new StringBuffer();
    buffer.append("Component: " + componentModel.getUnderlyingStorage().getFullPath());
    buffer.append("\n");
    ((PluginComponentSpecification) componentModel.getComponentSpecification()).getHelpText(
      id,
      buffer);
    dataLabel.setText(buffer.toString());
    
    Point hints = container.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
    
    gd = (GridData) container.getLayoutData();
    gd.widthHint = hints.x;
    gd.heightHint = hints.y;
    container.setLayoutData(gd);

    return container;
  }

  public String getViewerTitle() {
    return "Contained Component '" + id + "' using alias: '" + alias + "'";
  }



}