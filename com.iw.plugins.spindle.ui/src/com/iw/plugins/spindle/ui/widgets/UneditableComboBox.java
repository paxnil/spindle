/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.ui.widgets;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TypedListener;

public class UneditableComboBox extends Composite
{

  static final int ITEMS_SHOWING = 5;

  Label label;
  List list;
  Shell popup;
  Button arrow;
  boolean hasFocus;

  public UneditableComboBox(Composite parent, int style)
  {
    super(parent, checkStyle(style));

    style = getStyle();

    int labelStyle = SWT.SINGLE;
    if ((style & SWT.FLAT) != 0)
      labelStyle |= SWT.FLAT;
    label = new Label(this, labelStyle);
    label.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_LIST_BACKGROUND));

    popup = new Shell(getShell(), SWT.NO_TRIM);

    int listStyle = SWT.SINGLE | SWT.V_SCROLL;
    if ((style & SWT.FLAT) != 0)
      listStyle |= SWT.FLAT;
    list = new List(popup, listStyle);

    int arrowStyle = SWT.ARROW | SWT.DOWN;
    if ((style & SWT.FLAT) != 0)
      arrowStyle |= SWT.FLAT;
    arrow = new Button(this, arrowStyle);

    Listener listener = new Listener()
    {
      public void handleEvent(Event event)
      {
        if (popup == event.widget)
        {
          popupEvent(event);
          return;
        }
        if (list == event.widget)
        {
          listEvent(event);
          return;
        }
        if (arrow == event.widget)
        {
          arrowEvent(event);
          return;
        }
        if (UneditableComboBox.this == event.widget)
        {
          comboEvent(event);
          return;
        }

      }
    };

    int[] comboEvents = {SWT.Dispose, SWT.Move, SWT.Resize};
    for (int i = 0; i < comboEvents.length; i++)
      this.addListener(comboEvents[i], listener);

    int[] popupEvents = {SWT.Close, SWT.Paint, SWT.Deactivate};
    for (int i = 0; i < popupEvents.length; i++)
      popup.addListener(popupEvents[i], listener);

    int[] labelEvents = {SWT.KeyDown, SWT.KeyUp, SWT.Modify, SWT.MouseDown, SWT.MouseUp,
        SWT.Traverse, SWT.FocusIn, SWT.FocusOut};
    for (int i = 0; i < labelEvents.length; i++)
      label.addListener(labelEvents[i], listener);

    int[] listEvents = {SWT.MouseUp, SWT.Selection, SWT.Traverse, SWT.KeyDown, SWT.KeyUp,
        SWT.FocusIn, SWT.FocusOut};
    for (int i = 0; i < listEvents.length; i++)
      list.addListener(listEvents[i], listener);

    int[] arrowEvents = {SWT.MouseDown, SWT.FocusIn, SWT.FocusOut};
    for (int i = 0; i < arrowEvents.length; i++)
      arrow.addListener(arrowEvents[i], listener);

  }
  /**
   * Adds an item.
   * <p>
   * The item is placed at the end of the list. Indexing is zero based.
   * 
   * @param string the new item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when the string is null
   * @exception SWTError(ERROR_ITEM_NOT_ADDED) when the item cannot be added
   */
  public void add(String string)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    list.add(string);
  }
  /**
   * Adds an item at an index.
   * <p>
   * The item is placed at an index in the list. Indexing is zero based.
   * 
   * This operation will fail when the index is out of range.
   * 
   * @param string the new item
   * @param index the index for the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when the string is null
   * @exception SWTError(ERROR_ITEM_NOT_ADDED) when the item cannot be added
   */
  public void add(String string, int index)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    list.add(string, index);
  }
  /**
   * Adds the listener to receive events.
   * <p>
   * 
   * @param listener the listener
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when listener is null
   */
  public void addModifyListener(ModifyListener listener)
  {
    ;
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    TypedListener typedListener = new TypedListener(listener);
    addListener(SWT.Modify, typedListener);
  }
  /**
   * Adds the listener to receive events.
   * <p>
   * 
   * @param listener the listener
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when listener is null
   */
  public void addSelectionListener(SelectionListener listener)
  {
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    TypedListener typedListener = new TypedListener(listener);
    addListener(SWT.Selection, typedListener);
    addListener(SWT.DefaultSelection, typedListener);
  }
  void arrowEvent(Event event)
  {
    switch (event.type)
    {
      case SWT.FocusIn :
      {
        if (hasFocus)
          return;
        hasFocus = true;
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.FocusIn, e);
        break;
      }
      case SWT.FocusOut :
      {
        Control focusControl = getDisplay().getFocusControl();
        if (focusControl == list || focusControl == label)
          return;
        hasFocus = false;
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.FocusOut, e);
        break;
      }
      case SWT.MouseDown :
      {
        if (event.button != 1)
          return;
        dropDown(!isDropped());
        break;
      }
    }
  }
  static int checkStyle(int style)
  {
    int mask = SWT.BORDER | SWT.READ_ONLY | SWT.FLAT;
    return style & mask;
  }
  /**
   * Clears the current selection.
   * <p>
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public void clearSelection()
  {
    list.deselectAll();
  }
  void comboEvent(Event event)
  {
    switch (event.type)
    {
      case SWT.Dispose :
        if (popup != null && !popup.isDisposed())
          popup.dispose();
        popup = null;
        list = null;
        arrow = null;
        break;
      case SWT.Move :
        dropDown(false);
        break;
      case SWT.Resize :
        internalLayout();
        break;
    }
  }
  public Point computeSize(int wHint, int hHint, boolean changed)
  {
    int width = 0, height = 0;
    Point labelSize = label.computeSize(wHint, SWT.DEFAULT, changed);
    Point arrowSize = arrow.computeSize(SWT.DEFAULT, SWT.DEFAULT, changed);
    Point listSize = list.computeSize(wHint, SWT.DEFAULT, changed);
    int borderWidth = getBorderWidth();

    height = Math.max(hHint, Math.max(labelSize.y, arrowSize.y) + 2 * borderWidth);
    width = Math.max(wHint, Math.max(
        labelSize.x + arrowSize.x + 2 * borderWidth,
        listSize.x + 2));
    return new Point(width, height);
  }
  /**
   * Deselects an item.
   * <p>
   * If the item at an index is selected, it is deselected. If the item at an
   * index is not selected, it remains deselected. Indices that are out of range
   * are ignored. Indexing is zero based.
   * 
   * @param index the index of the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public void deselect(int index)
  {
    list.deselect(index);
  }
  /**
   * Deselects all items.
   * <p>
   * 
   * If an item is selected, it is deselected. If an item is not selected, it
   * remains unselected.
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public void deselectAll()
  {
    list.deselectAll();
  }
  void dropDown(boolean drop)
  {
    if (drop == isDropped())
      return;
    if (!drop)
    {
      popup.setVisible(false);
      label.setFocus();
      return;
    }
    int index = list.getSelectionIndex();
    if (index != -1)
      list.setTopIndex(index);
    Rectangle listRect = list.getBounds();
    int borderWidth = getBorderWidth();
    Point point = toDisplay(new Point(0 - borderWidth, 0 - borderWidth));
    Point comboSize = getSize();
    popup.setBounds(point.x, point.y + comboSize.y, comboSize.x, listRect.height + 2);
    popup.setVisible(true);
    list.setFocus();
  }
  public Control[] getChildren()
  {
    return new Control[0];
  }

  /**
   * Gets an item at an index.
   * <p>
   * Indexing is zero based.
   * 
   * This operation will fail when the index is out of range or an item could
   * not be queried from the OS.
   * 
   * @param index the index of the item
   * @return the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_CANNOT_GET_ITEM) when the operation fails
   */
  public String getItem(int index)
  {
    return list.getItem(index);
  }
  /**
   * Gets the number of items.
   * <p>
   * This operation will fail if the number of items could not be queried from
   * the OS.
   * 
   * @return the number of items in the widget
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_CANNOT_GET_COUNT) when the operation fails
   */
  public int getItemCount()
  {
    return list.getItemCount();
  }
  /**
   * Gets the height of one item.
   * <p>
   * This operation will fail if the height of one item could not be queried
   * from the OS.
   * 
   * @return the height of one item in the widget
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_CANNOT_GET_ITEM_HEIGHT) when the operation fails
   */
  public int getItemHeight()
  {
    return list.getItemHeight();
  }
  /**
   * Gets the items.
   * <p>
   * This operation will fail if the items cannot be queried from the OS.
   * 
   * @return the items in the widget
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_CANNOT_GET_ITEM) when the operation fails
   */
  public String[] getItems()
  {
    return list.getItems();
  }

  /**
   * Gets the index of the selected item.
   * <p>
   * Indexing is zero based. If no item is selected -1 is returned.
   * 
   * @return the index of the selected item.
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public int getSelectionIndex()
  {
    return list.getSelectionIndex();
  }
  /**
   * Gets the widget label.
   * <p>
   * If the widget has no label, an empty string is returned.
   * 
   * @return the widget label
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public String getText()
  {
    return label.getText();
  }
  /**
   * Gets the index of an item.
   * <p>
   * The list is searched starting at 0 until an item is found that is equal to
   * the search item. If no item is found, -1 is returned. Indexing is zero
   * based.
   * 
   * @param string the search item
   * @return the index of the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when string is null
   */
  public int indexOf(String string)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    return list.indexOf(string);
  }
  /**
   * Gets the index of an item.
   * <p>
   * The widget is searched starting at start including the end position until
   * an item is found that is equal to the search itenm. If no item is found, -1
   * is returned. Indexing is zero based.
   * 
   * @param string the search item
   * @param index the starting position
   * @return the index of the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when string is null
   */
  public int indexOf(String string, int start)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    return list.indexOf(string, start);
  }
  void internalLayout()
  {
    if (isDropped())
      dropDown(false);

    Rectangle rect = getClientArea();
    int width = rect.width;
    int height = rect.height;
    Point arrowSize = arrow.computeSize(SWT.DEFAULT, height);
    label.setBounds(0, 0, width - arrowSize.x, height);
    arrow.setBounds(width - arrowSize.x, 0, arrowSize.x, arrowSize.y);

    Point size = getSize();
    int listHeight = list.getItemHeight() * ITEMS_SHOWING;
    Rectangle trim = list.computeTrim(0, 0, size.x - 2, listHeight);
    list.setBounds(1, 1, size.x - 2, trim.height);
  }
  boolean isDropped()
  {
    return popup.getVisible();
  }
  public boolean isFocusControl()
  {
    if (label.isFocusControl() || arrow.isFocusControl() || list.isFocusControl()
        || popup.isFocusControl())
    {
      return true;
    } else
    {
      return super.isFocusControl();
    }
  }
  void listEvent(Event event)
  {
    switch (event.type)
    {
      case SWT.FocusIn :
      {
        if (hasFocus)
          return;
        hasFocus = true;

        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.FocusIn, e);
        break;
      }
      case SWT.FocusOut :
      {
        Control focusControl = getDisplay().getFocusControl();
        if (focusControl == label || focusControl == arrow)
          return;
        hasFocus = false;
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.FocusOut, e);
        break;
      }
      case SWT.MouseUp :
      {
        if (event.button != 1)
          return;
        dropDown(false);
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.DefaultSelection, e);
        break;
      }
      case SWT.Selection :
      {
        int index = list.getSelectionIndex();
        if (index == -1)
          return;
        label.setText(list.getItem(index));
        list.setSelection(index);
        Event e = new Event();
        e.time = event.time;
        e.stateMask = event.stateMask;
        e.doit = event.doit;
        notifyListeners(SWT.Selection, e);
        event.doit = e.doit;
        break;
      }
      case SWT.Traverse :
      {
        switch (event.detail)
        {
          case SWT.TRAVERSE_TAB_NEXT :
          case SWT.TRAVERSE_RETURN :
          case SWT.TRAVERSE_ESCAPE :
          case SWT.TRAVERSE_ARROW_PREVIOUS :
          case SWT.TRAVERSE_ARROW_NEXT :
            event.doit = false;
            break;
        }
        Event e = new Event();
        e.time = event.time;
        e.detail = event.detail;
        e.doit = event.doit;
        e.keyCode = event.keyCode;
        notifyListeners(SWT.Traverse, e);
        event.doit = e.doit;
        break;
      }
      case SWT.KeyUp :
      {
        Event e = new Event();
        e.time = event.time;
        e.character = event.character;
        e.keyCode = event.keyCode;
        e.stateMask = event.stateMask;
        notifyListeners(SWT.KeyUp, e);
        break;
      }
      case SWT.KeyDown :
      {
        if (event.character == SWT.ESC)
        {
          // escape key cancels popup list
          dropDown(false);
        }
        if (event.character == SWT.CR || event.character == '\t')
        {
          // Enter and Tab cause default selection
          dropDown(false);
          Event e = new Event();
          e.time = event.time;
          e.stateMask = event.stateMask;
          notifyListeners(SWT.DefaultSelection, e);
        }

        Event e = new Event();
        e.time = event.time;
        e.character = event.character;
        e.keyCode = event.keyCode;
        e.stateMask = event.stateMask;
        notifyListeners(SWT.KeyDown, e);
        break;

      }
    }
  }
  void popupEvent(Event event)
  {
    switch (event.type)
    {
      case SWT.Paint :
        // draw black rectangle around list
        Rectangle listRect = list.getBounds();
        Color black = getDisplay().getSystemColor(SWT.COLOR_BLACK);
        event.gc.setForeground(black);
        event.gc.drawRectangle(0, 0, listRect.width + 1, listRect.height + 1);
        break;
      case SWT.Close :
        event.doit = false;
        dropDown(false);
        break;
      case SWT.Deactivate :
        dropDown(false);
        break;
    }
  }
  public void redraw(int x, int y, int width, int height, boolean all)
  {
    if (!all)
      return;
    Point location = label.getLocation();
    label.redraw(x - location.x, y - location.y, width, height, all);
    location = list.getLocation();
    list.redraw(x - location.x, y - location.y, width, height, all);
    if (arrow != null)
    {
      location = arrow.getLocation();
      arrow.redraw(x - location.x, y - location.y, width, height, all);
    }
  }
  /**
   * Removes an item at an index.
   * <p>
   * Indexing is zero based.
   * 
   * This operation will fail when the index is out of range or an item could
   * not be removed from the OS.
   * 
   * @param index the index of the item
   * @return the selection state
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_ITEM_NOT_REMOVED) when the operation fails
   */
  public void remove(int index)
  {
    list.remove(index);
  }
  /**
   * Removes a range of items.
   * <p>
   * Indexing is zero based. The range of items is from the start index up to
   * and including the end index.
   * 
   * This operation will fail when the index is out of range or an item could
   * not be removed from the OS.
   * 
   * @param start the start of the range
   * @param end the end of the range
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_ITEM_NOT_REMOVED) when the operation fails
   */
  public void remove(int start, int end)
  {
    list.remove(start, end);
  }
  /**
   * Removes an item.
   * <p>
   * This operation will fail when the item could not be removed from the OS.
   * 
   * @param string the search item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when string is null
   * @exception SWTError(ERROR_ITEM_NOT_REMOVED) when the operation fails
   */
  public void remove(String string)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    list.remove(string);
  }
  /**
   * Removes all items.
   * <p>
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public void removeAll()
  {
    label.setText("");
    list.removeAll();
  }
  /**
   * Removes the listener.
   * <p>
   * 
   * @param listener the listener
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when listener is null
   */
  public void removeModifyListener(ModifyListener listener)
  {
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    removeListener(SWT.Modify, listener);
  }
  /**
   * Removes the listener.
   * <p>
   * 
   * @param listener the listener
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when listener is null
   */
  public void removeSelectionListener(SelectionListener listener)
  {
    if (listener == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    removeListener(SWT.Selection, listener);
    removeListener(SWT.DefaultSelection, listener);
  }
  /**
   * Selects an item.
   * <p>
   * If the item at an index is not selected, it is selected. Indices that are
   * out of range are ignored. Indexing is zero based.
   * 
   * @param index the index of the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   */
  public void select(int index)
  {
    if (index == -1)
    {
      list.deselectAll();
      label.setText("");
      return;
    }
    if (0 <= index && index < list.getItemCount())
    {
      if (index != getSelectionIndex())
      {
        label.setText(list.getItem(index));
        list.select(index);
        list.showSelection();
      }
    }
  }

  public void select(String value)
  {
    int index = indexOf(value);
    if (index == -1)
    {
      list.add(value, 0);
      select(0);
    } else
    {
      select(index);
    }
  }

  public void setBackground(Color color)
  {
    super.setBackground(color);
    if (label != null)
      label.setBackground(color);
    if (list != null)
      list.setBackground(color);
    if (arrow != null)
      arrow.setBackground(color);
  }
  /**
   * Sets the focus.
   */
  public boolean setFocus()
  {
    return label.setFocus();
  }
  /**
   * Sets the widget font.
   */
  public void setFont(Font font)
  {
    super.setFont(font);
    label.setFont(font);
    list.setFont(font);
    internalLayout();
  }
  public void setForeground(Color color)
  {
    super.setForeground(color);
    if (label != null)
      label.setForeground(color);
    if (list != null)
      list.setForeground(color);
    if (arrow != null)
      arrow.setForeground(color);
  }
  /**
   * Sets the label of an item.
   * <p>
   * Indexing is zero based.
   * 
   * This operation will fail when the index is out of range or an item could
   * not be changed in the OS.
   * 
   * @param index the index for the item
   * @param string the item
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when items is null
   * @exception SWTError(ERROR_ITEM_NOT_MODIFIED) when the operation fails
   */
  public void setItem(int index, String string)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    list.setItem(index, string);
  }
  /**
   * Sets all items.
   * <p>
   * 
   * @param items the array of items
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when items is null
   * @exception SWTError(ERROR_ITEM_NOT_ADDED) when the operation fails
   */
  public void setItems(String[] items)
  {
    if (items == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    int style = getStyle();
    if ((style & SWT.READ_ONLY) != 0)
      label.setText("");
    list.setItems(items);
  }
  /**
   * Sets the widget label
   * <p>
   * 
   * @param string the widget label
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_NULL_ARGUMENT) when string is null
   */
  public void setText(String string)
  {
    if (string == null)
      SWT.error(SWT.ERROR_NULL_ARGUMENT);
    int index = list.indexOf(string);
    if (index == -1)
    {
      label.setText(string);
      return;
    }
    label.setText(string);
    list.setSelection(index);
    list.showSelection();
  }
  /**
   * Sets the label limit
   * <p>
   * 
   * @param limit new label limit
   * 
   * @exception SWTError(ERROR_THREAD_INVALID_ACCESS) when called from the wrong
   *              thread
   * @exception SWTError(ERROR_WIDGET_DISPOSED) when the widget has been
   *              disposed
   * @exception SWTError(ERROR_CANNOT_BE_ZERO) when limit is 0
   */
  public void setVisible(boolean visible)
  {
    super.setVisible(visible);
    if (!getVisible())
    {
      Composite parent = getParent();
      while (parent != null && !parent.isVisible())
      {
        parent = parent.getParent();
      }
      if (parent != null)
        parent.setFocus();
    }
  }

  public void setEnabled(boolean flag)
  {
    label.setEnabled(flag);
    arrow.setEnabled(flag);
  }

  void labelEvent(Event event)
  {
    switch (event.type)
    {
      case SWT.FocusIn :
      {
        if (hasFocus)
          return;
        hasFocus = true;
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.FocusIn, e);
        break;
      }
      case SWT.FocusOut :
      {
        Control focusControl = getDisplay().getFocusControl();
        if (focusControl == list || focusControl == arrow)
          return;
        hasFocus = false;
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.FocusOut, e);
        break;
      }
      case SWT.KeyDown :
      {

        if (event.character == SWT.ESC)
        { // escape key cancels popup list
          dropDown(false);
        }
        if (event.character == SWT.CR || event.character == '\t')
        {
          dropDown(false);
          Event e = new Event();
          e.time = event.time;
          e.stateMask = event.stateMask;
          notifyListeners(SWT.DefaultSelection, e);
        }
        if (event.keyCode == SWT.ARROW_UP || event.keyCode == SWT.ARROW_DOWN)
        {
          int oldIndex = getSelectionIndex();
          if (event.keyCode == SWT.ARROW_UP)
          {
            select(Math.max(oldIndex - 1, 0));
          } else
          {
            select(Math.min(oldIndex + 1, getItemCount() - 1));
          }

          if (oldIndex != getSelectionIndex())
          {
            Event e = new Event();
            e.time = event.time;
            e.stateMask = event.stateMask;
            notifyListeners(SWT.Selection, e);
          }
        }

        // Further work : Need to add support for incremental search in
        // pop up list as characters typed in label widget

        Event e = new Event();
        e.time = event.time;
        e.character = event.character;
        e.keyCode = event.keyCode;
        e.stateMask = event.stateMask;
        notifyListeners(SWT.KeyDown, e);
        break;
      }
      case SWT.KeyUp :
      {
        Event e = new Event();
        e.time = event.time;
        e.character = event.character;
        e.keyCode = event.keyCode;
        e.stateMask = event.stateMask;
        notifyListeners(SWT.KeyUp, e);
        break;
      }
      case SWT.Modify :
      {
        list.deselectAll();
        Event e = new Event();
        e.time = event.time;
        notifyListeners(SWT.Modify, e);
        break;
      }
      case SWT.MouseDown :
      {
        if (event.button != 1)
          return;
        boolean dropped = isDropped();
        if (!dropped)
          setFocus();
        dropDown(!dropped);
        break;
      }
      case SWT.MouseUp :
      {
        if (event.button != 1)
          return;
        break;
      }
      case SWT.Traverse :
      {
        switch (event.detail)
        {
          case SWT.TRAVERSE_TAB_NEXT :
          case SWT.TRAVERSE_ARROW_PREVIOUS :
          case SWT.TRAVERSE_ARROW_NEXT :
            // The tab key causes default selection and
            // the arrow keys are used to manipulate the list contents so
            // do not use them for traversal.
            event.doit = false;
            break;
        }

        Event e = new Event();
        e.time = event.time;
        e.detail = event.detail;
        e.doit = event.doit;
        e.keyCode = event.keyCode;
        notifyListeners(SWT.Traverse, e);
        event.doit = e.doit;
        break;
      }
    }
  }
}