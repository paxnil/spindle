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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;

public abstract class SectionWidget implements IPropertyChangeListener {
  private String headerText;
  private Control client;
  protected Label header;
  protected Control separator;
  private String description;
  protected Label descriptionLabel;
  private boolean addSeparator = true;
  private boolean descriptionPainted = true;
  private boolean headerPainted = true;
  private int widthHint = SWT.DEFAULT;
  private int heightHint = SWT.DEFAULT;
  private Composite control;

  class SectionLayout extends Layout {
    int vspacing = 3;
    int sepHeight = 2;

    protected Point computeSize(Composite parent, int wHint, int hHint, boolean flush) {
      int width = 0;
      int height = 0;
      int cwidth = 0;
      int collapsedHeight = 0;

      if (wHint != SWT.DEFAULT)
        width = wHint;
      if (hHint != SWT.DEFAULT)
        height = hHint;

      cwidth = width;

      if (client != null) {
        Point csize = client.computeSize(SWT.DEFAULT, SWT.DEFAULT, flush);
        if (width == 0) {
          width = csize.x;
          cwidth = width;
        }
        if (height == 0)
          height = csize.y;
      }

      Point toggleSize = null;

      if (hHint == SWT.DEFAULT && headerPainted && header != null) {
        int hwidth = cwidth;
        if (toggleSize != null)
          hwidth = cwidth - toggleSize.x - 5;
        Point hsize = header.computeSize(hwidth, SWT.DEFAULT, flush);
        height += hsize.y;
        collapsedHeight = hsize.y;
        height += vspacing;
      }

      if (hHint == SWT.DEFAULT && addSeparator) {
        height += sepHeight;
        height += vspacing;
        collapsedHeight += vspacing + sepHeight;
      }
      if (hHint == SWT.DEFAULT && descriptionPainted && descriptionLabel != null) {
        Point dsize = descriptionLabel.computeSize(cwidth, SWT.DEFAULT, flush);
        height += dsize.y;
        height += vspacing;
      }
      return new Point(width, height);
    }
    protected void layout(Composite parent, boolean flush) {
      int width = parent.getClientArea().width;
      int height = parent.getClientArea().height;
      int y = 0;
      Point toggleSize = null;

      if (headerPainted && header != null) {
        Point hsize;

        int availableWidth = width;
        if (toggleSize != null)
          availableWidth = width - toggleSize.x - 5;
        hsize = header.computeSize(availableWidth, SWT.DEFAULT, flush);
        int hx = 0;
        header.setBounds(hx, y, availableWidth, hsize.y);

        y += hsize.y + vspacing;
      }
      if (addSeparator && separator != null) {
        separator.setBounds(0, y, width, 2);
        y += sepHeight + vspacing;
      }
      if (descriptionPainted && descriptionLabel != null) {
        Point dsize = descriptionLabel.computeSize(width, SWT.DEFAULT, flush);
        descriptionLabel.setBounds(0, y, width, dsize.y);
        y += dsize.y + vspacing;
      }
      if (client != null) {
        client.setBounds(0, y, width, height - y);
      }
    }
  }

  public SectionWidget() {

    JFaceResources.getFontRegistry().addListener(this);

  }

  public void commitChanges(boolean onSave) {
  }

  public abstract Composite createClient(Composite parent);

  public final Control createControl(Composite parent) {

    Composite section = new Composite(parent, SWT.NULL);
    SectionLayout slayout = new SectionLayout();
    section.setLayout(slayout);
    section.setData(this);

    if (headerPainted) {

      header = new Label(section, SWT.WRAP);
      header.setText(getHeaderText());
      header.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));

    }

    if (addSeparator) {

      separator = new Composite(section, SWT.NONE);
      separator.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_BLACK));

    }

    if (descriptionPainted && description != null) {

      descriptionLabel = new Label(section, SWT.WRAP);
      descriptionLabel.setText(description);

    }
    client = createClient(section);
    section.setData(this);
    control = section;
    return section;
  }

  protected void reflow() {
    control.setRedraw(false);
    control.getParent().setRedraw(false);
    control.layout(true);
    control.getParent().layout(true);
    control.setRedraw(true);
    control.getParent().setRedraw(true);
  }

  public void dispose() {

    JFaceResources.getFontRegistry().removeListener(this);

  }

  public String getDescription() {

    return description;

  }
  public String getHeaderText() {

    return headerText;

  }
  public int getHeightHint() {

    return heightHint;

  }
  public int getWidthHint() {

    return widthHint;

  }

  public void initialize(Object input) {

  }

  public boolean isAddSeparator() {

    return addSeparator;

  }

  public boolean isDescriptionPainted() {

    return descriptionPainted;

  }

  public boolean isHeaderPainted() {

    return headerPainted;

  }

  public void setAddSeparator(boolean value) {

    addSeparator = value;

  }

  private String trimNewLines(String text) {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < text.length(); i++) {

      char c = text.charAt(i);
      if (c == '\n') {

        buf.append(' ');

      } else {

        buf.append(c);

      }
    }
    return buf.toString();
  }

  public void setDescription(String value) {
    // trim the new lines 
    description = trimNewLines(value);

    if (descriptionLabel != null) {

      descriptionLabel.setText(description);

    }

  }
  public void setDescriptionPainted(boolean value) {

    descriptionPainted = value;

  }

  public void setHeaderPainted(boolean value) {

    headerPainted = value;

  }
  public void setHeaderText(String value) {

    headerText = value;

    if (header != null) {

      header.setText(headerText);

    }
  }
  public void setHeightHint(int value) {

    heightHint = value;

  }

  public void setWidthHint(int value) {

    widthHint = value;

  }

  public void propertyChange(PropertyChangeEvent arg0) {

    if (control != null && header != null) {

      header.setFont(JFaceResources.getBannerFont());
      control.layout(true);

    }
  }

}