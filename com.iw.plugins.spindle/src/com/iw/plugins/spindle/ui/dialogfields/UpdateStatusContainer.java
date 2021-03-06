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
 package com.iw.plugins.spindle.ui.dialogfields;

import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;

import com.iw.plugins.spindle.util.SpindleStatus;


/**
 * @author GWL
 *
 */
public class UpdateStatusContainer implements IDialogFieldChangedListener {

  private HashMap map = new HashMap();
  private PropertyChangeSupport propertySupport;

  public void add(DialogField widget) {
    widget.addListener(this);
    IStatus widgetStatus = new SpindleStatus();
    map.put(widget, widgetStatus);
  }

  public void remove(DialogField widget) {
    widget.removeListener(this);
    map.remove(widget);
  }

  /**
   * @see IUpdateStatus#getStatus()
   */
  public IStatus getStatus() {
    return findMostSevereStatus();
  }

  public IStatus getStatus(boolean ignoreDisabled) {
    return findMostSevereStatus(ignoreDisabled);
  }

  public IStatus getStatusFor(Object obj) {
    if (map.containsKey(obj)) {
      return (IStatus) map.get(obj);
    }
    return null;
  }

  protected IStatus findMostSevereStatus(boolean ignoreDisabled) {
    Set entries = map.entrySet();
    IStatus[] statii = null;
    if (ignoreDisabled) {
      Set enabled = new HashSet();
      Iterator i = entries.iterator();
      while (i.hasNext()) {
        Map.Entry entry = (Map.Entry) i.next();
        try {
          DialogField field = (DialogField) entry.getKey();
          if (!field.isEnabled()) {
            continue;
          }
        } catch (ClassCastException e) {
          // do nothing
        }
        enabled.add(entry);
      }
      entries = enabled;
    }
    statii = new IStatus[entries.size()];
    Iterator i = entries.iterator();
    int j = 0;
    while (i.hasNext()) {
      Map.Entry entry = (Map.Entry) i.next();
      statii[j++] = (IStatus) entry.getValue();
    }
    return getMostSevere(statii);
  }

  protected IStatus findMostSevereStatus() {
    return findMostSevereStatus(false);
  }

  public IStatus getMostSevere(IStatus[] status) {
    IStatus max = null;
    for (int i = 0; i < status.length; i++) {
      IStatus curr = status[i];
      if (curr.matches(IStatus.ERROR)) {
        return curr;
      }
      if (max == null || curr.getSeverity() > max.getSeverity()) {
        max = curr;
      }
    }
    return max;
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldButtonPressed(DialogField)
   */
  public void dialogFieldButtonPressed(DialogField field) {
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldChanged(DialogField)
   */
  public void dialogFieldChanged(DialogField field) {
  }

  /**
   * @see IDialogFieldChangedListener#dialogFieldStatusChanged(IStatus, DialogField)
   */
  public void dialogFieldStatusChanged(IStatus status, DialogField field) {
    if (map.containsKey(field)) {
      map.put(field, status);
    }
  }

}
