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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.formatter;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.MultiPassContentFormatter;

/**
 * fix for bug 70104
 */
public class FixedMultiPassContentFormatter extends MultiPassContentFormatter
{

  public FixedMultiPassContentFormatter(final String partitioning, final String type)
  {
    super(partitioning, type);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.formatter.MultiPassContentFormatter#formatSlaves(org.eclipse.jface.text.formatter.IFormattingContext,
   *              org.eclipse.jface.text.IDocument, int, int)
   */
  protected void formatSlaves(
      final IFormattingContext context,
      final IDocument document,
      final int offset,
      final int length)
  {

    if (offset >= document.getLength() - 1)
      return;

    int useLength = length;
    while (offset + useLength > document.getLength())
      useLength--;

    super.formatSlaves(context, document, offset, useLength);
  }
}