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
package com.iw.plugins.spindle.editors.template.assist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateBuffer;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariable;
import org.eclipse.jface.text.templates.TemplateVariableResolver;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.MultiTextEdit;
import org.eclipse.text.edits.RangeMarker;
import org.eclipse.text.edits.ReplaceEdit;
import org.eclipse.text.edits.TextEdit;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.Assert;

/**
 * AttributeContextType TODO add something here
 * 
 * @author glongman@gmail.com
 *  
 */
public class AttributeContextType extends TemplateContextType
{
  public static final String ATT_NAME_VAR = "attributeName";
  public static final String ATT_VALUE_VAR = "value";

  public static final String ATTR_CONTEXT_ID = UIPlugin.PLUGIN_ID
      + ".xml.attribute.templates.context";

  class VariableResolver extends TemplateVariableResolver
  {
    protected VariableResolver(String type, String description)
    {
      super();
      setType(type);
      setDescription(description);
    }
  }

  public AttributeContextType()
  {
    super(ATTR_CONTEXT_ID);
  }

  public void resolve(TemplateBuffer buffer, TemplateContext context) throws MalformedTreeException,
      BadLocationException
  {
    Assert.isNotNull(context);
    TemplateVariable[] variables = buffer.getVariables();

    List positions = variablesToPositions(variables);
    List edits = new ArrayList(5);

    TemplateVariable keeper = null;

    // iterate over all variables and try to resolve them
    for (int i = 0; i != variables.length; i++)
    {
      TemplateVariable variable = variables[i];

      if (variable.isUnambiguous())
        continue;

      // remember old values
      int[] oldOffsets = variable.getOffsets();
      int oldLength = variable.getLength();
      String oldValue = variable.getDefaultValue();

      String type = variable.getType();
      if (ATT_VALUE_VAR.equals(type))
        keeper = variables[i];
      TemplateVariableResolver resolver = null;//(TemplateVariableResolver)
      // fResolvers.get(type);
      if (resolver == null)
        resolver = new VariableResolver(type, "");
      resolver.resolve(variable, context);

      String value = variable.getDefaultValue();

      if (!oldValue.equals(value))
        // update buffer to reflect new value
        for (int k = 0; k != oldOffsets.length; k++)
          edits.add(new ReplaceEdit(oldOffsets[k], oldLength, value));
    }

    IDocument document = new Document(buffer.getString());
    MultiTextEdit edit = new MultiTextEdit(0, document.getLength());
    edit.addChildren((TextEdit[]) positions.toArray(new TextEdit[positions.size()]));
    edit.addChildren((TextEdit[]) edits.toArray(new TextEdit[edits.size()]));
    edit.apply(document, TextEdit.UPDATE_REGIONS);

    positionsToVariables(positions, variables);

    buffer.setContent(document.get(), new TemplateVariable[]{keeper});
  }
  
  private static List variablesToPositions(TemplateVariable[] variables)
  {
    List positions = new ArrayList(5);
    for (int i = 0; i != variables.length; i++)
    {
      int[] offsets = variables[i].getOffsets();
      for (int j = 0; j != offsets.length; j++)
        positions.add(new RangeMarker(offsets[j], 0));
    }
   
    return positions;
  }

  private static void positionsToVariables(List positions, TemplateVariable[] variables)
  {
    Iterator iterator = positions.iterator();

    for (int i = 0; i != variables.length; i++)
    {
      TemplateVariable variable = variables[i];

      int[] offsets = new int[variable.getOffsets().length];
      for (int j = 0; j != offsets.length; j++)
        offsets[j] = ((TextEdit) iterator.next()).getOffset();

      variable.setOffsets(offsets);
    }
  }
}

