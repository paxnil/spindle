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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.wutka.dtd;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.iw.plugins.spindle.core.util.Assert;

/**
 * A Set that preserves the order things are added
 * 
 * @author glongman@gmail.com
 * 
 */
public class OrderPreservingSet implements Set
{

  List fStore;

  public OrderPreservingSet()
  {
    super();
    fStore = new ArrayList();
  }

  public OrderPreservingSet(Collection c)
  {
    this();
    fStore.addAll(c);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#add(java.lang.Object)
   */
  public boolean add(Object arg0)
  {
    if (fStore.contains(arg0))
      return false;

    Assert.isNotNull(arg0);
    fStore.add(arg0);
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#addAll(java.util.Collection)
   */
  public boolean addAll(Collection arg0)
  {
    Assert.isNotNull(arg0);
    for (Iterator iter = arg0.iterator(); iter.hasNext();)
    {
      Object element = iter.next();
      add(element);
    }
    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#clear()
   */
  public void clear()
  {
    fStore.clear();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#contains(java.lang.Object)
   */
  public boolean contains(Object arg0)
  {
    return fStore.contains(arg0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#containsAll(java.util.Collection)
   */
  public boolean containsAll(Collection arg0)
  {
    Assert.isNotNull(arg0);
    int expected = arg0.size();
    int found = 0;
    for (Iterator iter = arg0.iterator(); iter.hasNext();)
    {
      Object element = iter.next();
      if (fStore.contains(element))
        found++;
    }
    return expected == found;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#isEmpty()
   */
  public boolean isEmpty()
  {
    return fStore.isEmpty();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#iterator()
   */
  public Iterator iterator()
  {
    return Collections.unmodifiableList(fStore).iterator();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#remove(java.lang.Object)
   */
  public boolean remove(Object arg0)
  {
    return fStore.remove(arg0);
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#removeAll(java.util.Collection)
   */
  public boolean removeAll(Collection arg0)
  {
    Assert.isNotNull(arg0);
    boolean changed = false;
    for (Iterator iter = arg0.iterator(); iter.hasNext();)
    {
      Object element = iter.next();
      changed = fStore.remove(element) || changed;
    }
    return changed;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#retainAll(java.util.Collection)
   */
  public boolean retainAll(Collection collection)
  {
    Assert.isNotNull(collection);
    boolean changed = false;
    List copy = new ArrayList(fStore);
    for (int i = 0; i < copy.size(); i++)
    {
      Object element = (Object) copy.get(i);
      if (!collection.contains(element))
      {
        fStore.remove(element);
        changed = true;
      }
    }

    return changed;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#size()
   */
  public int size()
  {
    return fStore.size();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#toArray()
   */
  public Object[] toArray()
  {
    return fStore.toArray();
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.util.Collection#toArray(java.lang.Object[])
   */
  public Object[] toArray(Object[] arg0)
  {
    Assert.isNotNull(arg0);
    return fStore.toArray(arg0);
  }

}