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

package com.iw.plugins.spindle.editors.template.assist;

import java.util.Map;

import org.apache.tapestry.parse.TemplateParser;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.ui.IEditorInput;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.util.ContentAssistProcessor;

/**
 * Base class for Template completion assist. Basicly here to set a flag if XHTML
 * completions have been requested via the property page.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class TemplateContentAssistProcessor extends ContentAssistProcessor
{

    private boolean fHasHTMLExtension;
    public TemplateContentAssistProcessor(Editor editor)
    {
        super(editor);
        IEditorInput input = editor.getEditorInput();
        IStorage storage = (IStorage) input.getAdapter(IStorage.class);
        fHasHTMLExtension = storage != null && storage.getName().endsWith(".html");
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.util.ContentAssistProcessor#connect(org.eclipse.jface.text.IDocument)
     */
    protected void init(IDocument document) throws IllegalStateException
    {
        fDTD = null;
// TODO remove       super.connect(document);
        IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
        if (fHasHTMLExtension)
        {
            String dtdString = store.getString(PreferenceConstants.TEMPLATE_EDITOR_HTML_SHOW_XHTML);
            if (dtdString.equals(TemplateEditor.XHTML_NONE_LABEL))
                return;

            if (dtdString.equals(TemplateEditor.XHTML_STRICT_LABEL))
            {
                fDTD = TemplateEditor.XHTML_STRICT;
            } else if (dtdString.equals(TemplateEditor.XHTML_TRANSITIONAL_LABEL))
            {
                fDTD = TemplateEditor.XHTML_TRANSITIONAL;
            } else if (dtdString.equals(TemplateEditor.XHTML_FRAMES_LABEL))
            {
                fDTD = TemplateEditor.XHTML_FRAMESET;
            }

        }
    }

    protected String getJwcid(Map attributeMap)
    {
        XMLNode jwcidArt = (XMLNode) attributeMap.get(TemplateParser.JWCID_ATTRIBUTE_NAME);
        if (jwcidArt != null)
            return jwcidArt.getAttributeValue();

        return null;
    }

}
