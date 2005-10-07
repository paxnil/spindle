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
 * The Initial Developer of the Original Code is Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005 the Initial
 * Developer. All Rights Reserved.
 * 
 * Contributor(s):
 * 
 * glongman@gmail.com
 * 
 * ***** END LICENSE BLOCK *****
 */
package com.iw.plugins.spindle.editors.assist;

import java.util.HashMap;
import java.util.Map;

import org.apache.tapestry.util.MultiKey;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.DocumentTemplateContext;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateException;
import org.eclipse.jface.text.templates.TemplateTranslator;

import core.util.Assert;

public class TagTemplateContext extends DocumentTemplateContext
{

  protected static final String DEFAULT_ATT_VALUE = AttributeTemplateContext.DEFAULT_ATT_VALUE;
  protected static final String ELEMENT_VAR = "elementName";
  protected static final String ATTR_NAME_VAR = "attribute_";
  protected static final String ATTR_VALUE_VAR = "value_";
  protected static final String NEXT_ATTR_VAR = "next";

  static final String ELEMENT_LEADIN = "<${elementName}";
  static final String EMPTY_ELEMENT_TAIL = "/>${cursor}";
  static final String EMPTY_ELEMENT_HAS_MORE_ATTRS_TAIL = "${next}/>${cursor}";
  static final String NON_EMPTY_ELEMENT_TAIL = ">${cursor}</${elementName}>";
  static final String NON_EMPTY_HAS_MORE_ATTRS_TAIL = "${next}>${cursor}</${elementName}>";
  static final String ATTR_LEAD = "${attribute_";
  static final String ATTR_MIDDLE = "}=\"${value_";
  static final String ATTR_TAIL = "}\"";

  private static Map PATTERN_MAP = new HashMap();

  protected static final TemplateContextType TAG_TEMPLATE_CONTEXT_TYPE = new TagContextType();

  static String generatePattern(int attrCount, boolean emptyTag, int totalAttrCount)
  {
    StringBuffer buffer = new StringBuffer();
    buffer.append(ELEMENT_LEADIN);
    for (int i = 0; i < attrCount; i++)
    {
      buffer.append(" ");
      buffer.append(ATTR_LEAD);
      buffer.append(i);
      buffer.append(ATTR_MIDDLE);
      buffer.append(i);
      buffer.append(ATTR_TAIL);
    }
    if (emptyTag)
    {
      if (totalAttrCount > attrCount)
      {
        buffer.append(EMPTY_ELEMENT_HAS_MORE_ATTRS_TAIL);
      } else
      {
        buffer.append(EMPTY_ELEMENT_TAIL);
      }
    } else
    {
      if (totalAttrCount > attrCount)
      {
        buffer.append(NON_EMPTY_HAS_MORE_ATTRS_TAIL);
      } else
      {
        buffer.append(NON_EMPTY_ELEMENT_TAIL);
      }
    }
    return buffer.toString();
  }

  public static String getPattern(int attrCount, boolean emptyTag, int totalAttrCount)
  {
    MultiKey key = new MultiKey(new Object[]{String.valueOf(attrCount),
        String.valueOf(emptyTag)}, false);
    String result = (String) PATTERN_MAP.get(key);
    if (result == null)
    {
      result = generatePattern(attrCount, emptyTag, totalAttrCount);
      PATTERN_MAP.put(key, result);
    }
    return result;
  }

  private String elementName;
  private String[][] attrValues;
  private boolean emptyTag;
  private int totalAttrCount;

  public TagTemplateContext(IDocument document, int completionOffset,
      int completionLength, ProposalFactory.ElementProposalInfo info)
  {
    super(TAG_TEMPLATE_CONTEXT_TYPE, document, completionOffset, completionLength);
    Assert.isNotNull(info.attrvalues);
    this.elementName = info.elementName;
    this.attrValues = info.attrvalues;
    this.emptyTag = info.empty;
    this.totalAttrCount = info.totalAttrCount;
    setVariables();
  }

  /* since the templates are fixed, we can hand them out here */
  Template getTemplate()
  {

    return new Template("", "", TagContextType.ELEMENT_CONTEXT_ID, getPattern(
        attrValues.length,
        emptyTag,
        totalAttrCount));
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

  public void setVariables()
  {
    setVariable(NEXT_ATTR_VAR, "");
    setVariable(ELEMENT_VAR, elementName);
    for (int i = 0; i < attrValues.length; i++)
    {
      String[] entry = attrValues[i];
      setVariable(ATTR_NAME_VAR + i, entry[0]);
      setVariable(ATTR_VALUE_VAR + i, entry[1] == null
          ? AttributeTemplateContext.DEFAULT_ATT_VALUE : entry[1]);
    }
  }

  public String getDisplayString()
  {
    return getVariable(ELEMENT_VAR) + (emptyTag ? " (empty) " : "");
  }

}