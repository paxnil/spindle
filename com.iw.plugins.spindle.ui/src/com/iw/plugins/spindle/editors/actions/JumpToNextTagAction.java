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

package com.iw.plugins.spindle.editors.actions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;

/**
 * Jump to the next tag action
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class JumpToNextTagAction extends JumpToNextAttributeAction
{

  /**
   * @param forward
   */
  public JumpToNextTagAction(boolean forward)
  {
    super(forward);
  }

  /**
   * @param text
   * @param forward
   */
  public JumpToNextTagAction(String text, boolean forward)
  {
    super(text, forward);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.actions.JumpToNextAttributeAction#buildRegionList(org.eclipse.jface.text.IDocument)
   */
  protected void buildRegionList(IDocument document)
  {
    attachPartitioner();

    Position[] positions = null;
    try
    {
      positions = document.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
    } catch (BadPositionCategoryException e)
    {
      UIPlugin.log(e);
      return;
    }

    Arrays.sort(positions, XMLNode.COMPARATOR);

    ArrayList regionCollector = new ArrayList();

    List attributes = null;

    for (int i = 0; i < positions.length; i++)
    {
      XMLNode node = (XMLNode) positions[i];
      attributes = node.getAttributes();

      if (attributes.isEmpty())
        continue;

      regionCollector.add(((XMLNode) attributes.get(0)).getAttributeValueRegion());
    }

    fRegions = (IRegion[]) regionCollector.toArray(new IRegion[regionCollector.size()]);
  }

}