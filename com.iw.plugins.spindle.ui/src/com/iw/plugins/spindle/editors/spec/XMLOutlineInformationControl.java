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

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Shell;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.editors.util.XMLNodeContentProvider;
import com.iw.plugins.spindle.editors.util.XMLNodeLabelProvider;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class XMLOutlineInformationControl extends TreeInformationControl
{

    protected static class XMLPatternFilter extends NamePatternFilter
    {
        public boolean select(Viewer viewer, Object parentElement, Object element)
        {
            XMLNode node = (XMLNode) element;

            if (node.getType() == XMLDocumentPartitioner.ATTR)
            {
                if (match(node.getAttributeValue()))
                    return true;

                return hasUnfilteredChild(viewer, element);
            }

            return super.select(viewer, parentElement, element);
        }
    }

    private SpecEditor fEditor;

    public XMLOutlineInformationControl(Shell parent, int shellStyle, int treeStyle, SpecEditor editor)
    {
        super(parent, shellStyle, treeStyle);
        setContentProvider(new XMLNodeContentProvider());
        setLabelProvider(new XMLNodeLabelProvider());
        fEditor = editor;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.OutlineInformationControl#createFilter()
     */
    protected NamePatternFilter createFilter()
    {
        return new XMLPatternFilter();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.OutlineInformationControl#doSetInput(java.lang.Object)
     */
    protected void doSetInput(Object information)
    {
        if (information instanceof XMLNode)
            fTreeViewer.setInput((XMLNode) information);
        else
            fTreeViewer.setInput(null);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.spec.OutlineInformationControl#doGotoSelectedElement(java.lang.Object)
     */
    protected boolean doHandleSelectedElement(Object selected)
    {
        try
        {
            fEditor.openTo(selected);
        } finally
        {
            return true;
        }
    }

}
