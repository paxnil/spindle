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
package com.iw.plugins.spindle.editorlib.pages;

import net.sf.tapestry.spec.ILibrarySpecification;

import com.iw.plugins.spindle.editors.SpindleFormPage;
import com.iw.plugins.spindle.model.TapestryLibraryModel;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class LibraryPagesSummarySection extends BasePagesSummarySection {

  /**
   * Constructor for ApplicationPagesSummarySection.
   * @param page
   */
  public LibraryPagesSummarySection(SpindleFormPage page) {
    super(page);
  }

  /**
   * @see com.iw.plugins.spindle.editors.pages.PagesSummarySection#getSpec()
   */
  protected ILibrarySpecification getSpec() {
    return (ILibrarySpecification) ((TapestryLibraryModel) getModel()).getSpecification();
  }

}
