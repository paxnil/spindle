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
package com.iw.plugins.spindle.editors.assist;

import java.util.Comparator;

import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;

/**
 * ProposalFactory factory methods for common proposals.
 * 
 * @author glongman@gmail.com
 *  
 */
public class ProposalFactory
{

  public static final Comparator PROPOSAL_COMPARATOR = new Comparator()
  {
    String s1;
    String s2;
    int z1;
    int z2;
    public int compare(Object o1, Object o2)
    {

      ICompletionProposal p1 = (ICompletionProposal) o1;
      ICompletionProposal p2 = (ICompletionProposal) o2;

      s1 = p1.getDisplayString();
      s2 = p2.getDisplayString();

      z1 = z2 = 0;

      if (p1 instanceof OrderedProposal)
        z1 = ((OrderedProposal) p1).getYOrder();

      if (p2 instanceof OrderedProposal)
        z2 = ((OrderedProposal) p2).getYOrder();

      return (z1 == z2) ? s1.compareTo(s2) : (z1 < z2 ? -1 : 1);
    }
  };

  public static final String DEFAULT_ATTR_VALUE = "";

  /**
   * @deprecated @param attributeName
   * @param addLeadingSpace
   * @param replacementOffset
   * @return
   */
  public static CompletionProposal getAttributeProposal(
      String attributeName,
      boolean addLeadingSpace,
      int replacementOffset)
  {
    return getAttributeProposal(
        attributeName,
        attributeName,
        DEFAULT_ATTR_VALUE,
        null,
        addLeadingSpace,
        replacementOffset);
  }

  /**
   * @deprecated @param attributeName
   * @param displayName
   * @param defaultValue
   * @param extraInfo
   * @param addLeadingSpace
   * @param replacementOffset
   * @return
   */
  public static CompletionProposal getAttributeProposal(
      String attributeName,
      String displayName,
      String defaultValue,
      String extraInfo,
      boolean addLeadingSpace,
      int replacementOffset)
  {
    String replacementString = (addLeadingSpace ? " " : "") + attributeName + "=\""
        + defaultValue + "\"";
    Point replacementPoint = new Point((addLeadingSpace ? 1 : 0) + attributeName.length()
        + 2, defaultValue.length());

    return new CompletionProposal(
        replacementString,
        replacementOffset,
        0,
        replacementPoint,
        Images.getSharedImage("bullet.gif"),
        displayName,
        null,
        extraInfo);
  }

  public static OrderedProposal createTemplateProposal(
      Template template,
      TemplateContext context,
      Region region,
      Image image,
      String extraInfo,
      int yOrder)
  {

    if (image == null)
      image = Images.getSharedImage("bullet.gif");
    return new SpindleTemplateProposal(
        template,
        context,
        region,
        extraInfo,
        image,
        yOrder);
  }

  public static OrderedProposal getTagProposal(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTDProposalGenerator.ElementInfo info)
  {
    TagTemplateContext context = new TagTemplateContext(
        document,
        completionOffset,
        completionLength,
        info);

    return createTemplateProposal(context.getTemplate(), context, new Region(
        completionOffset,
        completionLength), info.image, info.comment, info.order);
  }

  public static OrderedProposal getElementAttributeProposal(
      IDocument document,
      String attributeName,
      int completionOffset,
      int completionLength,
      boolean addLeadingSpace,
      Image image,
      String extraInfo,
      int yOrder)
  {
    AttributeTemplateContext context;
    context = new AttributeTemplateContext(
        document,
        completionOffset,
        completionLength,
        addLeadingSpace);
    context.setAttributeName(attributeName);

    return createTemplateProposal(context.getTemplate(), context, new Region(
        completionOffset,
        completionLength), image, extraInfo, yOrder);
  }

}