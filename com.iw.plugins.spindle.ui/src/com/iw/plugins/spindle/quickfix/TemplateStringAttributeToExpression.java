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
package com.iw.plugins.spindle.quickfix;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMarkerResolution;
import org.eclipse.ui.IMarkerResolution2;
import org.eclipse.ui.IMarkerResolutionGenerator;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * <p>
 * Marker resolution generator for the case where a user has entered a literal string for a
 * parameter that requires an expression instead.
 * </p>
 * <p>
 * Consider a contrived component MyComponent that has a parameter 'value' that requires value of
 * type myPackage.MyType
 * </P>
 * TODO The following will bugger things up - fix
 * <p>
 * <span jwcid="@MyComponent" value="theValue"/>
 * </p>
 * will cause an error because Tapestry can not convert the literal String 'theValue' into an
 * instance of myPackage.MyType. 9 times out of 10 the user forgot to prepend the string 'theValue'
 * with 'ognl:' and that's the Quickfix here.
 */
public class TemplateStringAttributeToExpression implements IMarkerResolutionGenerator
{

    public static class QFResolution implements IMarkerResolution, IMarkerResolution2
    {
        String fLabel;

        String fOldValue;

        int fOffset;

        int fLength;

        public QFResolution(String value, int offset, int length)
        {
            fOldValue = value;
            fLabel = "replace '" + value + "' with 'ognl:" + value + "'";
            fOffset = offset;
            fLength = length;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#getLabel()
         */
        public String getLabel()
        {
            return fLabel;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution#run(org.eclipse.core.resources.IMarker)
         */
        public void run(IMarker marker)
        {
            IFile file = (IFile) marker.getResource();
            IEditorPart editor = UIUtils.getEditorFor(file);
            if (editor == null)
                editor = UIPlugin.openTapestryEditor(file);
            if (editor == null || (!(editor instanceof ITextEditor)))
                return;
            ITextEditor textEditor = (ITextEditor) editor;
            IDocument document = textEditor.getDocumentProvider().getDocument(
                    textEditor.getEditorInput());
            textEditor.selectAndReveal(fOffset, fLength);
            try
            {
                document.replace(fOffset, fLength, "ognl:" + fOldValue);
            }
            catch (BadLocationException e)
            {
                UIPlugin.log(e);
            }
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution2#getDescription()
         */
        public String getDescription()
        {
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.IMarkerResolution2#getImage()
         */
        public Image getImage()
        {
            return null;
        }

    }

    private static final IMarkerResolution[] NO_RESOLUTIONS = new IMarkerResolution[] {};

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IMarkerResolutionGenerator#getResolutions(org.eclipse.core.resources.IMarker)
     */
    public IMarkerResolution[] getResolutions(IMarker marker)
    {

        IFile file = getFile(marker);
        if (file == null
                || marker.getAttribute(ITapestryMarker.PROBLEM_CODE, -1) != IProblem.TEMPLATE_SCANNER_CHANGE_TO_EXPRESSION)
            return NO_RESOLUTIONS;

        int charstart = marker.getAttribute(IMarker.CHAR_START, -1);
        int charend = marker.getAttribute(IMarker.CHAR_END, -1);

        if (charstart <= 0 || charend <= 0 || charend <= charstart)
            return NO_RESOLUTIONS;

        IDocumentProvider provider = UIPlugin.getDefault().getTemplateFileDocumentProvider();
        IEditorInput input = new FileEditorInput(file);
        try
        {
            provider.connect(input);
            IDocument document = provider.getDocument(input);

            int offset = charstart;
            int length = charend - charstart;
            String value;
            try
            {
                value = document.get(offset, length);
                if (!value.startsWith("ognl:"))
                    return new IMarkerResolution[]
                    { new QFResolution(value, offset, length) };
            }
            catch (BadLocationException e)
            {
                UIPlugin.log(e);
            }
        }
        catch (CoreException e)
        {

            UIPlugin.log(e);
        }
        finally
        {
            provider.disconnect(input);
        }
        return NO_RESOLUTIONS;
    }

    private IFile getFile(IMarker marker)
    {
        IResource res = marker.getResource();
        if (res instanceof IFile && res.isAccessible())
            return (IFile) res;

        return null;
    }
}