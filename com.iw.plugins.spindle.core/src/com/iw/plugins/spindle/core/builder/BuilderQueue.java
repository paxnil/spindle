package com.iw.plugins.spindle.core.builder;
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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/**
 * Helper class used by the Full Build
 * 
 * 
 * @author glongman@intelligentworks.com
 */
public class BuilderQueue
{

  List fToBeProcessed;
  List fHaveBeenProcessed;

  public BuilderQueue()
  {
    this.fToBeProcessed = new ArrayList(11);
    this.fHaveBeenProcessed = new ArrayList(11);
  }

  public int getProcessedCount()
  {
    return fHaveBeenProcessed.size();
  }

  public int getWaitingCount()
  {
    return fToBeProcessed.size();
  }

  public void add(Object element)
  {
    fToBeProcessed.add(element);
  }

  public void addAll(Object[] elements)
  {
    for (int i = 0, length = elements.length; i < length; i++)
      add(elements[i]);

  }

  public void addAll(Collection elements)
  {
    fToBeProcessed.addAll(elements);
  }

  public void clear()
  {
    this.fToBeProcessed.clear();
    this.fHaveBeenProcessed.clear();
  }

  public void finished(Object element)
  {
    fToBeProcessed.remove(element);
    fHaveBeenProcessed.add(element);
  }

  public void finished(List elements)
  {
    for (Iterator iter = elements.iterator(); iter.hasNext();)
    {
      finished(iter.next());

    }
  }

  public boolean isProcessed(Object element)
  {
    return fHaveBeenProcessed.contains(element);
  }

  public boolean isWaiting(Object element)
  {
    return fToBeProcessed.contains(element);
  }

  public Object peekWaiting()
  {
    return fToBeProcessed.get(0);
  }

  public boolean hasWaiting()
  {
    return !fToBeProcessed.isEmpty();
  }

  public String toString()
  {
    return "BuildQueue: " + fToBeProcessed;
  }
}