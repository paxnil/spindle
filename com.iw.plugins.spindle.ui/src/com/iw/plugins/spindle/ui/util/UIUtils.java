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

import java.util.StringTokenizer;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;

/**
 *  Access to features exposed by the JDT UI plugin
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class UIUtils
{
    public static int getImportNumberThreshold()
    {
        IPreferenceStore prefs = JavaPlugin.getDefault().getPreferenceStore();
        int threshold = prefs.getInt(PreferenceConstants.ORGIMPORTS_ONDEMANDTHRESHOLD);
        if (threshold < 0)
        {
            threshold = Integer.MAX_VALUE;
        }
        return threshold;
    }

    public static String[] getImportOrderPreference()
    {
        IPreferenceStore prefs = JavaPlugin.getDefault().getPreferenceStore();
        String str = prefs.getString(PreferenceConstants.ORGIMPORTS_IMPORTORDER);
        if (str != null)
        {
            return unpackOrderList(str);
        }
        return new String[0];
    }

    private static String[] unpackOrderList(String str)
    {
        StringTokenizer tok = new StringTokenizer(str, ";");
        int nTokens = tok.countTokens();
        String[] res = new String[nTokens];
        for (int i = 0; i < nTokens; i++)
        {
            res[i] = tok.nextToken();
        }
        return res;
    }

    public static IEditorPart getEditorFor(IResourceWorkspaceLocation location)
    {
        IStorage storage = location.getStorage();
        if (storage != null)
            return UIUtils.getEditorFor(storage);

        return null;
    }

    public static IEditorPart getEditorFor(IStorage storage)
    {

        IWorkbench workbench = UIPlugin.getDefault().getWorkbench();
        IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();

        for (int i = 0; i < windows.length; i++)
        {
            IWorkbenchPage[] pages = windows[i].getPages();
            for (int x = 0; x < pages.length; x++)
            {

                IEditorReference[] editors = pages[x].getEditorReferences();

                for (int z = 0; z < editors.length; z++)
                {

                    IEditorReference ref = editors[z];
                    IEditorPart editor = ref.getEditor(true);

                    if (editor == null)
                    {
                        continue;
                    }

                    IStorage editorStorage = (IStorage) editor.getEditorInput().getAdapter(IStorage.class);
                    if (editorStorage != null)
                        return editor;
                }
            }
        }
        return null;
    }

    public static String findWordString(IDocument document, int offset)
    {
        try
        {
            IRegion region = findWord(document, offset);
            if (region != null)
                return document.get(region.getOffset(), region.getLength());
        } catch (BadLocationException e)
        {
            //do nothing
        }
        return null;
    }

    public static IRegion findWord(IDocument document, int offset)
    {

        int start = -1;
        int end = -1;

        try
        {

            int pos = offset;
            char c;

            while (pos >= 0)
            {
                c = document.getChar(pos);
                if (!Character.isJavaIdentifierPart(c))
                    break;
                --pos;
            }

            start = pos;

            pos = offset;
            int length = document.getLength();

            while (pos < length)
            {
                c = document.getChar(pos);
                if (!Character.isJavaIdentifierPart(c))
                    break;
                ++pos;
            }

            end = pos;

        } catch (BadLocationException x)
        {}

        if (start > -1 && end > -1)
        {
            if (start == offset && end == offset)
                return new Region(offset, 0);
            else if (start == offset)
                return new Region(start, end - start);
            else
                return new Region(start + 1, end - start - 1);
        }

        return null;
    }

}
