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
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.template.assist;

import java.util.HashSet;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;

import com.iw.plugins.spindle.core.parser.template.CoreTemplateParser;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.UITapestryAccess;
import com.iw.plugins.spindle.editors.template.TemplateEditor;

/**
 * Helper class for Template ContentAssistProcessors
 * 
 * Knows how to extract tapestry information.
 * 
 * @author glongman@gmail.com
 * @version $Id: TemplateTapestryAccess.java,v 1.3 2003/12/13 14:43:11 glongman
 *          Exp $
 */
public class TemplateTapestryAccess extends UITapestryAccess
{

  private static Pattern SIMPLE_ID_PATTERN;
  private static Pattern IMPLICIT_ID_PATTERN;
  private static PatternMatcher PATTERN_MATCHER;

  static
  {
    Perl5Compiler compiler = new Perl5Compiler();

    try
    {
      SIMPLE_ID_PATTERN = compiler.compile(TemplateParser.SIMPLE_ID_PATTERN);
      IMPLICIT_ID_PATTERN = compiler.compile(TemplateParser.IMPLICIT_ID_PATTERN);
    } catch (MalformedPatternException ex)
    {
      throw new Error(ex);
    }

    PATTERN_MATCHER = new Perl5Matcher();
  }

  private PluginComponentSpecification fSpecification;
  private String fRawJwcid = null;
  /** The id. Implicit or not. Never null */
  private String fSimpleId = null;
  /** implicit component - the full type including namespace qualifier */
  private String fFullType = null;
  /** implicit component - the namespace qualifier */
  private String fLibraryId = null;
  /** implicit component - the simple type name. Same as fFullType - fLibraryId */
  private String fSimpleType = null;
  /** the spec of the component referred to directly or indirectly by a jwcid */
  private PluginComponentSpecification fContainedComponentSpecification = null;
  /** not null iff fSimpleId is not null* */
  private IContainedComponent fContainedComponent = null;

  public TemplateTapestryAccess(Editor editor) throws IllegalArgumentException
  {
    super(editor);
    fSpecification = (PluginComponentSpecification) ((TemplateEditor) editor)
        .getSpecification();

    Assert.isLegal(fSpecification != null);
  }

  public void setJwcid(String jwcid)
  {

    if (jwcid == null || jwcid.trim().length() == 0)
      return;

    fRawJwcid = jwcid.trim();
    if (fRawJwcid.equalsIgnoreCase(CoreTemplateParser.REMOVE_ID)
        || fRawJwcid.equalsIgnoreCase(CoreTemplateParser.CONTENT_ID))
      return;

    if (PATTERN_MATCHER.matches(fRawJwcid, IMPLICIT_ID_PATTERN))
    {
      MatchResult match = PATTERN_MATCHER.getMatch();

      fSimpleId = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_ID_GROUP);
      fFullType = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_TYPE_GROUP);

      fLibraryId = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP);
      fSimpleType = match.group(CoreTemplateParser.IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP);

    } else
    {
      if (PATTERN_MATCHER.matches(fRawJwcid, SIMPLE_ID_PATTERN))
        fSimpleId = fRawJwcid;

    }
    resolveContainedComponent();
  }

  void resolveContainedComponent()
  {
    fContainedComponent = null;
    fContainedComponentSpecification = null;

    if (isSimpleIdOnly())
    {
      fContainedComponent = fSpecification.getComponent(fSimpleId);
      if (fContainedComponent == null)
        return;
      String copyOf = fContainedComponent.getCopyOf();
      if (copyOf != null)
      {
        fContainedComponent = fSpecification.getComponent(copyOf);
        if (fContainedComponent == null)
          return;
      }
      fContainedComponentSpecification = (PluginComponentSpecification) resolveComponentType(fContainedComponent
          .getType());
    } else if (fFullType != null)
    {
      fContainedComponentSpecification = (PluginComponentSpecification) resolveComponentType(fFullType);
    }
  }

  public boolean isSimpleIdOnly()
  {
    return fSimpleId != null && fFullType == null;
  }

  public IComponentSpecification getBaseSpecification()
  {
    return fSpecification;
  }

  public IComponentSpecification getResolvedComponent()
  {
    return fContainedComponentSpecification;
  }

  public IContainedComponent getContainedComponent()
  {
    return fContainedComponent;
  }

  public String getSimpleId()
  {
    return fSimpleId;
  }

  public String getFullType()
  {
    return fFullType;
  }

  Result[] findParameters()
  {
    return findParameters(null, null);
  }

  Result[] findParameters(String match, HashSet existing)
  {
    if (fContainedComponentSpecification == null)
    {
      return new Result[]{};
    }
    if (fContainedComponent != null)
    {
      existing.addAll(fContainedComponent.getBindingNames());
    }

    return findParameters(fContainedComponentSpecification, match, existing);
  }

  Result[] getSimpleIds()
  {
    return getContainedIds(fSpecification);
  }

  Result getComponentContextInformation()
  {
    if (fContainedComponentSpecification == null)
      return null;

    IComponentSpecification componentSpec = fContainedComponentSpecification;

    return createComponentInformationResult(
        fContainedComponent != null ? fContainedComponent.getType() : fFullType,
        null,
        fContainedComponentSpecification);
  }

  Result getParameterContextInformation(String parameterName)
  {
    return createParameterResult(fContainedComponentSpecification, parameterName);
  }

  public String toString()
  {
    return "\n\tfull=" + fFullType + "\n\tlib=" + fLibraryId + "\n\tsimple="
        + fSimpleType + "\n\tsimpleId=" + fSimpleId;
  }

}