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
 * Portions created by the Initial Developer are Copyright (C) 2004
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.editors.formatter;

import java.util.LinkedList;

import org.eclipse.jface.text.Assert;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;

/**
 * MasterFormattingStrategy the master xml formatting Strategy
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MasterFormattingStrategy extends ContextBasedFormattingStrategy
    implements
      IFormattingStrategy
{

  /** Documents to be formatted by this strategy */
  private final LinkedList fDocuments = new LinkedList();
  /** Partitions to be formatted by this strategy */
  private final LinkedList fPartitions = new LinkedList();

  /** access to the preferences store * */
  private FormattingPreferences prefs;

  public MasterFormattingStrategy()
  {
    this.prefs = new FormattingPreferences();
  }

  public MasterFormattingStrategy(FormattingPreferences prefs)
  {
    Assert.isNotNull(prefs);
    this.prefs = prefs;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#format()
   */
  public void format()
  {
    // TODO Auto-generated method stub
    super.format();
    final IDocument document = (IDocument) fDocuments.removeFirst();
    final TypedPosition partition = (TypedPosition) fPartitions.removeFirst();

    if (document != null && partition != null)
    {

      XMLContentFormatter formatter = new XMLContentFormatter(
          new MasterFormatWorker(),
          new String[]{DefaultPartitioner.CONTENT_TYPES_CATEGORY,
              XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY},
          prefs);
      formatter.format(document, partition);
    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStarts(org.eclipse.jface.text.formatter.IFormattingContext)
   */
  public void formatterStarts(IFormattingContext context)
  {
    // TODO Auto-generated method stub
    super.formatterStarts(context);
    fDocuments.addLast(context.getProperty(FormattingContextProperties.CONTEXT_MEDIUM));
    fPartitions.addLast(context
        .getProperty(FormattingContextProperties.CONTEXT_PARTITION));
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#formatterStops()
   */
  public void formatterStops()
  {

    super.formatterStops();
    fDocuments.clear();
    fPartitions.clear();
  }
}