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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.iw.plugins.spindle.model.TapestryApplicationModel;
import com.iw.plugins.spindle.model.TapestryComponentModel;
import com.iw.plugins.spindle.spec.PluginComponentSpecification;

public class ComponentAliasViewer extends HelpViewer {

  String id;
  String alias;
  HashMap precomputedMap;
  Composite container;
  UneditableComboBox combo;
  Label dataLabel;
  String [] precomputedText;

  public ComponentAliasViewer(String id, String alias, HashMap precomputedMap) {
    this.id = id;
    this.alias = alias;
    this.precomputedMap = precomputedMap;
  }

  /**
   * @see HelpViewer#createClient(Composite)
   */
  public Control createClient(Composite parent) {
    container = new Composite(parent, SWT.NULL);

	GridData gd;

    GridLayout layout = new GridLayout();
    layout.marginHeight = 8;
    layout.marginHeight = 8;
    container.setLayout(layout); 
    container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
    

    combo = new UneditableComboBox(container, SWT.NULL);
    combo.addSelectionListener(new SelectionListener() {
    	public void widgetSelected(SelectionEvent event) {
    		dataLabel.setText(precomputedText[combo.getSelectionIndex()]);
    		combo.getShell().pack(true);
    	}
    	
    	public void widgetDefaultSelected(SelectionEvent event) {
    	}
    });
    gd = new GridData(GridData.FILL_HORIZONTAL);
    combo.setLayoutData(gd);

    dataLabel = new Label(container, SWT.BORDER);
    dataLabel.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_INFO_BACKGROUND));
    gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
    createItems();
    
    Point hints = computeHints();
 
 	gd = (GridData)container.getLayoutData();
 	gd.widthHint = hints.x;
 	gd.heightHint = hints.y;
 	container.setLayoutData(gd);
 	
 	combo.select(0);
 	dataLabel.setText(precomputedText[0]);

    return container;
  }

  public String getViewerTitle() {
    return "Contained Component '"+id+"' using alias: '" + alias + "'";
  }
  
  private Point computeHints() {
  	String [] items = combo.getItems();
  	int longestIndex = 0;
  	for (int i=0; i<items.length; i++) {
  		if (items[i].length() > items[longestIndex].length()) {
  			longestIndex = i;
  		}
  	}
  	String longestPrecomputedText = "";  	
  	for (int i=0; i<precomputedText.length; i++) {
  		if (precomputedText[i].length() > longestPrecomputedText.length()) {
  			longestPrecomputedText = precomputedText[i];
  		}
  	} 
  	
  	combo.select(longestIndex);
  	dataLabel.setText(longestPrecomputedText);
  	
  	return container.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
  }
    		

  private void createItems() {
    ArrayList items = new ArrayList();
    ArrayList precomputed = new ArrayList();
    Iterator iter = precomputedMap.keySet().iterator();
    while (iter.hasNext()) {
      TapestryApplicationModel appModel = (TapestryApplicationModel) iter.next();
      TapestryComponentModel componentModel = (TapestryComponentModel) precomputedMap.get(appModel);
      StringBuffer buffer = new StringBuffer();
      buffer.append("Component: " + componentModel.getUnderlyingStorage().getFullPath());
      buffer.append("\n");
      ((PluginComponentSpecification) componentModel.getComponentSpecification()).getHelpText(id, buffer);
      items.add(appModel.getUnderlyingStorage().getFullPath().toString());
      precomputed.add(buffer.toString());
    }
    combo.setItems((String[]) items.toArray(new String[items.size()]));
    precomputedText = ((String []) precomputed.toArray(new String[precomputed.size()]));
  }

}