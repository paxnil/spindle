package net.sf.spindle.core.build;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
/**
 * Helper class used by the AbstractBuild
 * 
 * 
 * @author glongman@gmail.com
 */
public class BuilderQueue
{

  List<Object> fToBeProcessed;
  List<Object> fHaveBeenProcessed;

  public BuilderQueue()
  {
    this.fToBeProcessed = new ArrayList<Object>(11);
    this.fHaveBeenProcessed = new ArrayList<Object>(11);
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

  public void addAll(Collection<Object> elements)
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