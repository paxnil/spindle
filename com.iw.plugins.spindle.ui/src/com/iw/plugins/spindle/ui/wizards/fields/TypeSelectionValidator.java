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

package com.iw.plugins.spindle.ui.wizards.fields;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;

import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * an ISelectionValidator that validates the type of the elements.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: TypeSelectionValidator.java,v 1.1 2003/10/20 20:19:13 glongman
 *          Exp $
 */
public class TypeSelectionValidator implements ISelectionStatusValidator
{

  private Class[] fAcceptedTypes;
  private boolean fAllowMultipleSelection;
  private Collection fRejected;

  private IStatus fError = new SpindleStatus(IStatus.ERROR, "");
  private IStatus fOK = new SpindleStatus();

  public TypeSelectionValidator(Class[] acceptedTypes, boolean allowMultipleSelection)
  {
    this(acceptedTypes, allowMultipleSelection, null);
  }

  public TypeSelectionValidator(Class[] acceptedTypes, boolean allowMultipleSelection,
      List rejectedElements)
  {
    Assert.isNotNull(acceptedTypes);
    fAcceptedTypes = acceptedTypes;
    fAllowMultipleSelection = allowMultipleSelection;
    fRejected = rejectedElements;
  }

  /*
   * @see org.eclipse.ui.dialogs.ISelectionValidator#isValid(java.lang.Object)
   */
  public IStatus validate(Object[] elements)
  {
    if (isValid(elements))
      return fOK;

    return fError;
  }

  private boolean accept(Object o)
  {
    for (int i = 0; i < fAcceptedTypes.length; i++)
    {
      if (fAcceptedTypes[i].isInstance(o))
        return true;

    }
    return false;
  }

  private boolean reject(Object elem)
  {
    return (fRejected != null) && fRejected.contains(elem);
  }

  private boolean isValid(Object[] selection)
  {
    if (selection.length == 0)
      return false;

    if (!fAllowMultipleSelection && selection.length != 1)
      return false;

    for (int i = 0; i < selection.length; i++)
    {
      Object o = selection[i];
      if (!accept(o) || reject(o))
        return false;

    }
    return true;
  }

}