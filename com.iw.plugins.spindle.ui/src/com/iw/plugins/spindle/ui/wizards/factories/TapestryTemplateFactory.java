/*******************************************************************************
 * BEGIN LICENSE BLOCK Version: MPL 1.1
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
package com.iw.plugins.spindle.ui.wizards.factories;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateException;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.assist.usertemplates.XMLFileContextType;

public class TapestryTemplateFactory extends TemplateFactory
{

  public static final String CONTEXT_TYPE = XMLFileContextType.TEMPLATE_FILE_CONTEXT_TYPE;

  public TapestryTemplateFactory()
  {
    super(CONTEXT_TYPE);
    addDefaultResolvers();
  }

  public String getContent(Template template) throws BadLocationException,
      TemplateException
  {
    return getGeneratedContent(template, createTemplateContext(), true);
  }

  public IFile createTapestryTemplate(
      IFile file,
      Template template,
      IProgressMonitor monitor) throws CoreException, InterruptedException
  {
    monitor.beginTask(UIPlugin.getString("ApplicationFactory.operationdesc", template
        .getDescription()), 3);

    monitor.worked(1);

    InputStream contents;
    try
    {
      contents = new ByteArrayInputStream(getContent(template).getBytes());
    } catch (Exception e)
    {
      UIPlugin.log_it(e);
      contents = new ByteArrayInputStream("\n\n\n\nan error occured. Check the log"
          .getBytes());
    }
    monitor.worked(1);
    if (!file.exists())
    {
      file.create(contents, false, new SubProgressMonitor(monitor, 1));
    } else
    {
      file.setContents(contents, true, true, new SubProgressMonitor(monitor, 1));
    }
    monitor.worked(1);
    monitor.done();
    return file;
  }
}