/*******************************************************************************
 * ***** BEGIN LICENSE BLOCK Version: MPL 1.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 * 
 * The Initial Developer of the Original Code is Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2004 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@intelligentworks.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.editors.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;

public class AttributeTemplateContext extends DocumentTemplateContext
{

  protected static final String DEFAULT_ATT_VALUE = "value";

  static final String ATTR_LEADING_SPACE_PATTERN = " ${attributeName}=\"${value}\"";
  static final String ATTR_NO_LEADING_SPACE_PATTERN = "${attributeName}=\"${value}\"";

  //  protected static final Template ATTR_NO_LEADING_SPACE_TEMPLATE;
  //  protected static final Template ATTR_LEADING_SPACE_TEMPLATE;
  //
  //  protected static TemplateBuffer ATTR_NO_LS_BUFFER;
  //  protected static TemplateBuffer ATTR_LS_BUFFER;

  protected static final TemplateContextType ATTR_TEMPLATE_CONTEXT_TYPE = new AttributeContextType();

  //  static
  //  {
  //    TemplateTranslator translator = new TemplateTranslator();
  //    ATTR_NO_LEADING_SPACE_TEMPLATE = new Template(
  //        "",
  //        "",
  //        AttributeContextType.ATTR_CONTEXT_ID,
  //        ATTR_NO_LEADING_SPACE_PATTERN);
  //
  //    ATTR_LEADING_SPACE_TEMPLATE = new Template(
  //        "",
  //        "",
  //        AttributeContextType.ATTR_CONTEXT_ID,
  //        ATTR_LEADING_SPACE_PATTERN);
  //
  //    try
  //    {
  //      ATTR_NO_LS_BUFFER = translator.translate(ATTR_NO_LEADING_SPACE_TEMPLATE);
  //      ATTR_LS_BUFFER = translator.translate(ATTR_LEADING_SPACE_TEMPLATE);
  //    } catch (TemplateException e)
  //    {
  //      UIPlugin.log(e);
  //    }
  //  }
  private boolean useLeadingSpace;
  public AttributeTemplateContext(IDocument document, int completionOffset,
      int completionLength, boolean useLeadingSpace)
  {
    super(ATTR_TEMPLATE_CONTEXT_TYPE, document, completionOffset, completionLength);
    this.useLeadingSpace = useLeadingSpace;
    setAttributeValue(DEFAULT_ATT_VALUE);
  }

  /* since the templates are fixed, we can hand them out here */
  Template getTemplate()
  {
    if (useLeadingSpace)
    {
      return new Template(
          "",
          "",
          AttributeContextType.ATTR_CONTEXT_ID,
          ATTR_LEADING_SPACE_PATTERN);
    } else
    {
      return new Template(
          "",
          "",
          AttributeContextType.ATTR_CONTEXT_ID,
          ATTR_NO_LEADING_SPACE_PATTERN);
    }
  }

  public boolean canEvaluate(Template template)
  {  
    return true;
  }

  public TemplateBuffer evaluate(Template template) throws BadLocationException,
      TemplateException
  {
    if (!canEvaluate(template))
      return null;
   
    TemplateTranslator translator = new TemplateTranslator();
    TemplateBuffer buffer = translator.translate(template.getPattern());
 
    getContextType().resolve(buffer, this);

    return buffer;
  }

  public String getAttributeName()
  {
    return getVariable(AttributeContextType.ATT_NAME_VAR);
  }

  public void setAttributeName(String name)
  {
    setVariable(AttributeContextType.ATT_NAME_VAR, name);
  }

  public void setAttributeValue(String value)
  {
    setVariable(AttributeContextType.ATT_VALUE_VAR, value);
  }
}