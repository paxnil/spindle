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
package com.iw.plugins.spindle.editors.assist.usertemplates;

import org.apache.tapestry.parse.SpecificationParser;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.GlobalTemplateVariables;
import org.eclipse.jface.text.templates.SimpleTemplateVariableResolver;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContextType;
import org.eclipse.jface.text.templates.TemplateVariableResolver;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.SpindleStatus;

/**
 * XMLFileContextType context type for creating new xml files.
 * 
 * @author glongman@gmail.com
 *  
 */
public class XMLFileContextType extends TemplateContextType
{

  public static class PublicId extends SimpleTemplateVariableResolver
  {

    public static final String NAME = "publicId";

    public PublicId()
    {
      super(NAME, "the DTD public Id");
      setEvaluationString(SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID);
    }
  }

  public static class PublicIdUrl extends SimpleTemplateVariableResolver
  {

    public static final String NAME = "publicIdUrl";

    public PublicIdUrl()
    {
      super(NAME, "the DTD public Id url");
      setEvaluationString("http://jakarta.apache.org/tapestry/dtd/Tapestry_3_0.dtd");
    }
  }

  public static class Encoding extends SimpleTemplateVariableResolver
  {
    public static final String NAME = "encoding";

    public Encoding()
    {
      super(NAME, "file encoding");
      setEvaluationString("UTF-8");
    }
  }

  public static class PageClass extends SimpleTemplateVariableResolver
  {
    public static final String NAME = "pageClass";

    public PageClass()
    {
      super(NAME, "pageClass");
      setEvaluationString(UIPlugin.getString("SecondPageWizardPage.defaultSpecClass"));
    }
  }

  public static class ComponentClass extends SimpleTemplateVariableResolver
  {
    public static final String NAME = "componentClass";

    public ComponentClass()
    {
      super(NAME, "the component class");
      setEvaluationString(UIPlugin
          .getString("SecondComponentWizardPage.defaultSpecClass"));
    }
  }

  public static class EngineClass extends SimpleTemplateVariableResolver
  {
    public static final String NAME = "engineClass";

    public EngineClass()
    {
      super(NAME, "the engine");
      setEvaluationString("org.apache.tapestry.engine.BaseEngine");
    }
  }

  public static class AllowInformal extends SimpleTemplateVariableResolver
  {
    public static final String NAME = "allowInformal";

    public AllowInformal()
    {
      super(NAME, "does this component allow informal parameters?");
      setEvaluationString("yes");
    }
  }

  public static class AllowBody extends SimpleTemplateVariableResolver
  {
    public static final String NAME = "allowBody";

    public AllowBody()
    {
      super(NAME, "does this component allow a body?");
      setEvaluationString("yes");
    }
  }

  public static IStatus validateTemplateName(
      IPreferenceStore store,
      String contextId,
      String templateName)
  {

    SpindleStatus status = new SpindleStatus();
    String value = store.getString(contextId);

    if (!templateExists(value, contextId))
      status.setError(UIPlugin.getString("templates.missing.pref", value, UIPlugin
          .getString(contextId + ".label")));

    return status;
  }

  private static boolean templateExists(String name, String contextId)
  {
    Template[] templates = UserTemplateAccess
        .getDefault()
        .getTemplateStore()
        .getTemplates(contextId);
    boolean found = false;
    for (int i = 0; i < templates.length; i++)
    {
      if (templates[i].getName().equals(name))
      {
        found = true;
        break;
      }
    }
    return found;
  }

  public static final TemplateVariableResolver PUBLIC_ID = new PublicId();
  public static final TemplateVariableResolver PUBLIC_ID_URL = new PublicIdUrl();
  public static final TemplateVariableResolver ENCODING = new Encoding();

  /** This context's id's */
  public static final String APPLICATION_FILE_CONTEXT_TYPE = UIPlugin.PLUGIN_ID
      + ".templateContextType.applicationFile";
  public static final String LIBRARY_FILE_CONTEXT_TYPE = UIPlugin.PLUGIN_ID
      + ".templateContextType.libraryFile";
  public static final String PAGE_FILE_CONTEXT_TYPE = UIPlugin.PLUGIN_ID
      + ".templateContextType.pageFile";
  public static final String COMPONENT_FILE_CONTEXT_TYPE = UIPlugin.PLUGIN_ID
      + ".templateContextType.componentFile";
  public static final String TEMPLATE_FILE_CONTEXT_TYPE = UIPlugin.PLUGIN_ID
      + ".templateContextType.templateFile";

  /**
   * Creates a new XML context type.
   */
  public XMLFileContextType()
  {
    addGlobalResolvers();
  }

  private void addGlobalResolvers()
  {
    addResolver(new GlobalTemplateVariables.Cursor());
    addResolver(new GlobalTemplateVariables.WordSelection());
    addResolver(new GlobalTemplateVariables.LineSelection());
    addResolver(new GlobalTemplateVariables.Dollar());
    addResolver(new GlobalTemplateVariables.Date());
    addResolver(new GlobalTemplateVariables.Year());
    addResolver(new GlobalTemplateVariables.Time());
    addResolver(new GlobalTemplateVariables.User());
    addResolver(new XMLFileContextType.PublicId());
    addResolver(new XMLFileContextType.PublicIdUrl());
    addResolver(new XMLFileContextType.Encoding());
    addResolver(new XMLFileContextType.AllowInformal());
    addResolver(new XMLFileContextType.ComponentClass());
    addResolver(new XMLFileContextType.PageClass());
    addResolver(new XMLFileContextType.AllowInformal());
    addResolver(new XMLFileContextType.AllowBody());
  }
}