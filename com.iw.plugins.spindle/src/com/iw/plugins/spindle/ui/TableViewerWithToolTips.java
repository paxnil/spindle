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

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

public class TableViewerWithToolTips extends TableViewer {

  private ToolTipHandler toolTipHandler;
  private IToolTipProvider tipProvider;

  /**
   * Constructor for TreeViewerWithToolTips
   */
  public TableViewerWithToolTips(Composite parent) {
    super(parent);
  }

  /**
   * Constructor for TreeViewerWithToolTips
   */
  public TableViewerWithToolTips(Composite parent, int flags) {
    super(parent, flags);
  }

  public TableViewerWithToolTips(Table table) {
    super(table);
  };

  protected void hookControl(Control control) {
    super.hookControl(control);
    toolTipHandler = new TableViewerToolTipHandler(control.getShell());
    toolTipHandler.activateHoverHelp(control);
  }

  public void setTooltipsEnabled(boolean flag) {
    if (toolTipHandler != null) {
      toolTipHandler.setHandlerEnabled(flag);
    }
  }
  public boolean isTooltipsEnabled() {
    if (toolTipHandler != null) {
      return toolTipHandler.isHandlerEnabled();
    }
    return false;
  }

  /**
   * Constructor for TreeViewerWithToolTips
   */
  public TableViewerWithToolTips(Tree tree) {
    super(tree);
  }

  public void setToolTipProvider(IToolTipProvider tipProvider) {
    this.tipProvider = tipProvider;
  }

  public IToolTipProvider getToolTipProvider() {
    return this.tipProvider;
  }

  private String getTVToolTipText(Item item) {
    if (tipProvider != null) {
      return tipProvider.getToolTipText(item.getData());
    }
    return null;
  }


  protected class TableViewerToolTipHandler extends ToolTipHandler {

    public TableViewerToolTipHandler(Shell parent) {
      super(parent);
    }

    protected String getToolTipText(Object object) {
      String result = getTVToolTipText((Item) object);
      if (result == null) {
        result = super.getToolTipText(object);
      }
      return result;
    }

  }

}