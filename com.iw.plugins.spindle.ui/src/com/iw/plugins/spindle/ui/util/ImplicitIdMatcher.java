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

package com.iw.plugins.spindle.ui.util;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;
import org.apache.tapestry.ApplicationRuntimeException;
import org.apache.tapestry.parse.TemplateParser;

/**
 * Perl expression matcher for implicit components found in Tapestry templates.
 * 
 * @author glongman@intelligentworks.com
 * 
 */
public class ImplicitIdMatcher
{
  private static final int IMPLICIT_ID_PATTERN_ID_GROUP = 1;
  private static final int IMPLICIT_ID_PATTERN_TYPE_GROUP = 2;
  private static final int IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP = 4;
  private static final int IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP = 5;

  private Pattern fImplicitIdPattern;
  private PatternMatcher fPatternMatcher;

  private String fJwcId;
  private String fType;
  private String fLibraryId;
  private String fSimpleType;
  private boolean fValid;

  public ImplicitIdMatcher()
  {
    super();
    Perl5Compiler compiler = new Perl5Compiler();

    try
    {
      fImplicitIdPattern = compiler.compile(TemplateParser.IMPLICIT_ID_PATTERN);
    } catch (MalformedPatternException ex)
    {
      throw new ApplicationRuntimeException(ex);
    }

    fPatternMatcher = new Perl5Matcher();
  }

  public boolean isMatch(String candidateJwcid)
  {
    fValid = false;
    if (fPatternMatcher.matches(candidateJwcid, fImplicitIdPattern))
    {
      fValid = true;

      MatchResult match = fPatternMatcher.getMatch();

      fJwcId = match.group(IMPLICIT_ID_PATTERN_ID_GROUP);
      fType = match.group(IMPLICIT_ID_PATTERN_TYPE_GROUP);

      fLibraryId = match.group(IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP);
      fSimpleType = match.group(IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP);
    }
    return fValid;
  }

  private void checkValid()
  {
    if (!fValid)
      throw new Error("Invalid Access to ImplicitIdMatcher");
  }

  public String getJwcId()
  {
    checkValid();
    return fJwcId;
  }

  public String getLibraryId()
  {
    checkValid();
    return fLibraryId;
  }

  public String getSimpleType()
  {
    checkValid();
    return fSimpleType;
  }

  public String getType()
  {
    checkValid();
    return fType;
  }

}