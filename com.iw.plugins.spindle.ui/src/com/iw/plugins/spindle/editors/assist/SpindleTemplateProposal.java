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

import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateProposal;
import org.eclipse.swt.graphics.Image;


/**
 * SpindleTemplateProposal orderable template proposal.
 * 
 * @author glongman@gmail.com
 *  
 */
public class SpindleTemplateProposal extends TemplateProposal implements OrderedProposal
{
  private int fYOrder;
  private TemplateContext fContext;
  private String fAdditionalInfo;

  public SpindleTemplateProposal(Template template, TemplateContext context,
      IRegion region, String extraInfo, Image image, int yOrder, int relevance)
  {
    super(template, context, region, image, relevance);
    fYOrder = yOrder;
    fContext = context;
    fAdditionalInfo = extraInfo;
  }

  public SpindleTemplateProposal(Template template, TemplateContext context,
      IRegion region, String extraInfo,  Image image, int yOrder)
  {
    this(template, context, region, extraInfo,  image, yOrder, 99);
  }

  public String getDisplayString()
  {
    if (fContext instanceof AttributeTemplateContext)
      return ((AttributeTemplateContext) fContext).getAttributeName();

    return super.getDisplayString();
  }

  public void setAdditionalProposalInfo(String info)
  {
    fAdditionalInfo = info;
  }

  public String getAdditionalProposalInfo()
  {
    if (fAdditionalInfo != null)
      return fAdditionalInfo;

    return super.getAdditionalProposalInfo();
  }
  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.assist.OrderedProposal#setYOrder(int)
   */
  public void setYOrder(int order)
  {
    fYOrder = order;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.assist.OrderedProposal#getYOrder()
   */
  public int getYOrder()
  {
    return fYOrder;
  }

}