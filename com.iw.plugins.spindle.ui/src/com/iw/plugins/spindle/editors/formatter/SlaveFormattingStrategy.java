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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.jface.text.formatter.ContextBasedFormattingStrategy;
import org.eclipse.jface.text.formatter.FormattingContextProperties;
import org.eclipse.jface.text.formatter.IFormattingContext;
import org.eclipse.jface.text.formatter.IFormattingStrategy;
import org.eclipse.jface.text.rules.DefaultPartitioner;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;

import com.iw.plugins.spindle.UIPlugin;

/**
 * SlaveFormattingStrategy formatting xml start tags only Will do nothing if
 * fPrefs aren't set to wrap long lines.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: MasterFormattingStrategy.java,v 1.1.2.1 2004/07/11 04:33:42
 *                     glongman Exp $
 */
public class SlaveFormattingStrategy extends ContextBasedFormattingStrategy
    implements
      IFormattingStrategy
{

  /** Documents to be formatted by this strategy */
  private final LinkedList fDocuments = new LinkedList();
  /** Partitions to be formatted by this strategy */
  private final LinkedList fPartitions = new LinkedList();

  /** access to the preferences store * */
  private FormattingPreferences fPrefs;

  /** partition types we allow */
  private String[] fAllowedTypes;

  private FormatWorker fFormatWorker;

  public SlaveFormattingStrategy(String[] allowedContentTypes, FormatWorker worker)
  {
    this(new FormattingPreferences(), allowedContentTypes, worker);
  }

  public SlaveFormattingStrategy(FormattingPreferences prefs,
      String[] allowedContentTypes, FormatWorker worker)
  {
    Assert.isNotNull(prefs);
    Assert.isNotNull(allowedContentTypes);
    Assert.isLegal(allowedContentTypes.length > 0);
    Assert.isNotNull(worker);
    Assert.isLegal(worker.usesEdits());
    fPrefs = prefs;
    fAllowedTypes = allowedContentTypes;
    fFormatWorker = worker;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.formatter.IFormattingStrategyExtension#format()
   */
  public void format()
  {

    super.format();

    final IDocument document = (IDocument) fDocuments.removeFirst();
    final TypedPosition partition = (TypedPosition) fPartitions.removeFirst();

    if (!isAllowedType(partition.getType()))
      return;

    if (document != null && partition != null)
    {
      Object result = fFormatWorker.format(fPrefs, document, partition, new int[]{});
      if (result != null && result instanceof TextEdit)
        try
        {
          ((TextEdit) result).apply(document);
        } catch (MalformedTreeException e)
        {
          UIPlugin.log(e);
        } catch (BadLocationException e)
        {
          UIPlugin.log(e);
        }
    }
  }

  private boolean isAllowedType(String type)
  {
    for (int i = 0; i < fAllowedTypes.length; i++)
    {
      if (fAllowedTypes[i].equals(type))
        return true;
    }

    return false;
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