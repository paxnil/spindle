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

package com.iw.plugins.spindle.editors.spec.assist;

import org.apache.tapestry.spec.IComponentSpecification;

import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.UITapestryAccess;

/**
 * Contains Helper methods for content assist in specs
 * 
 * @author glongman@gmail.com
 * 
 */
public class SpecTapestryAccess extends UITapestryAccess
{

  private IComponentSpecification fComponentSpecification;
  public SpecTapestryAccess(Editor editor) throws IllegalArgumentException
  {
    super(editor);
    Object spec = editor.getSpecification();
    Assert.isLegal(spec != null);
    if (spec instanceof IComponentSpecification)
      fComponentSpecification = (IComponentSpecification) spec;
  }
}