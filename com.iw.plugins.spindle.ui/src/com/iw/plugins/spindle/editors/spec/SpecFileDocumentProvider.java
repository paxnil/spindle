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

package com.iw.plugins.spindle.editors.spec;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.util.IndentingWriter;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.template.TemplateFileDocumentProvider;

/**
 *  Document provider for Tapestry Specs that come from workbench files
 * 
 *  Users should not instantiate. Rather call UIPlugin.getDefault().getSpecFileDocumentProvider()
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecFileDocumentProvider extends TemplateFileDocumentProvider
{
    public static String DEFAULT_ENCODING = "UTF-8";

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractDocumentProvider#createAnnotationModel(java.lang.Object)
     */
    protected IAnnotationModel createAnnotationModel(Object element) throws CoreException
    {

        if (element instanceof IFileEditorInput)
        {
            return new SpecAnnotationModel((IFileEditorInput) element);
        }

        return super.createAnnotationModel(element);
    }

    protected boolean setDocumentContent(IDocument document, IEditorInput editorInput, String encoding)
        throws CoreException
    {
        if (editorInput instanceof IFileEditorInput)
        {
            IFile file = ((IFileEditorInput) editorInput).getFile();
            try
            {
                setDocumentContentWithEmptyCheck(document, file.getContents(false), encoding);
            } catch (CoreException e)
            {
                if (e.getStatus() instanceof SpindleStatus)
                {
                  document.set(getSkeletonSpecification(file.getFileExtension()));
                } else
                {
                    throw e;
                }

            }
            return true;
        }

        return super.setDocumentContent(document, editorInput, encoding);
    }

    protected void setDocumentContent(IDocument document, InputStream contentStream, String encoding)
        throws CoreException
    {

        Reader in = null;

        try
        {

            in = new InputStreamReader(new BufferedInputStream(contentStream), "UTF-8");
            StringBuffer buffer = new StringBuffer();
            char[] readBuffer = new char[2048];
            int n = in.read(readBuffer);
            while (n > 0)
            {
                buffer.append(readBuffer, 0, n);
                n = in.read(readBuffer);
            }

            document.set(buffer.toString());

        } catch (IOException x)
        {
            UIPlugin.log(x);
        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException x)
                {}
            }
        }
    }

    private String getSkeletonSpecification(String extension)
    {
        IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
        boolean useTabs = store.getBoolean(PreferenceConstants.FORMATTER_USE_TABS_TO_INDENT);
        int tabSize = store.getInt(PreferenceConstants.EDITOR_DISPLAY_TAB_WIDTH);
        StringWriter swriter = new StringWriter();
        IndentingWriter iwriter = new IndentingWriter(swriter, useTabs, tabSize, 0, null);
        if ("jwc".equals(extension))
        {
            XMLUtil.writeComponentSpecification(iwriter, UIPlugin.DEFAULT_COMPONENT_SPEC, 0);
            return swriter.toString();
        } else if ("page".equals(extension))
        {
            XMLUtil.writeComponentSpecification(iwriter, UIPlugin.DEFAULT_PAGE_SPEC, 0);
            return swriter.toString();
        } else if ("application".equals(extension))
        {
            XMLUtil.writeApplicationSpecification(iwriter, UIPlugin.DEFAULT_APPLICATION_SPEC, 0);
            return swriter.toString();
        } else if ("library".equals(extension))
        {
            XMLUtil.writeLibrarySpecification(iwriter, UIPlugin.DEFAULT_LIBRARY_SPEC, 0);
            return swriter.toString();
        }
        return "";
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.editors.text.StorageDocumentProvider#setDocumentContent(org.eclipse.jface.text.IDocument, java.io.InputStream, java.lang.String)
     */
    protected void setDocumentContentWithEmptyCheck(IDocument document, InputStream contentStream, String encoding)
        throws CoreException
    {
        Reader in = null;

        try
        {

            if (encoding == null)
                encoding = getDefaultEncoding();

            in = new BufferedReader(new InputStreamReader(contentStream, encoding), DEFAULT_FILE_SIZE);
            StringBuffer buffer = new StringBuffer(DEFAULT_FILE_SIZE);
            char[] readBuffer = new char[2048];
            int n = in.read(readBuffer);
            while (n > 0)
            {
                buffer.append(readBuffer, 0, n);
                n = in.read(readBuffer);
            }

            String contents = buffer.toString();
            if (contents.length() == 0)
            {
                String msg = "empty file!";
                IStatus s = new SpindleStatus(IStatus.INFO, msg);
                throw new CoreException(s);
            }
            document.set(contents);

        } catch (IOException x)
        {
            String msg = x.getMessage() == null ? "" : x.getMessage(); //$NON-NLS-1$
            IStatus s = new Status(IStatus.ERROR, UIPlugin.PLUGIN_ID, IStatus.OK, msg, x);
            throw new CoreException(s);
        } finally
        {
            if (in != null)
            {
                try
                {
                    in.close();
                } catch (IOException x)
                {}
            }
        }
    }

}
