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

package com.iw.plugins.spindle.editors.spec.actions;

import org.apache.tapestry.INamespace;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.parser.validator.DOMValidator;
import com.iw.plugins.spindle.editors.actions.BaseEditorAction;
import com.wutka.dtd.DTD;

/**
 *  Base class for spec actions that need the xml partitioning.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public abstract class BaseSpecAction extends BaseEditorAction
{

    protected String fDeclaredRootElementName;
    protected String fPublicId;
    protected DTD fDTD;
// TODO remove   protected XMLDocumentPartitioner fPartitioner;
    protected INamespace fNamespace;
 
    protected IDocument fDocument;

    public BaseSpecAction()
    {
        super();
// TODO remove       fPartitioner =
//            new XMLDocumentPartitioner(XMLDocumentPartitioner.SCANNER, XMLDocumentPartitioner.TYPES);
    }

    public BaseSpecAction(String text)
    {
        super(text);
    }

    public BaseSpecAction(String text, ImageDescriptor image)
    {
        super(text, image);
    }

    public BaseSpecAction(String text, int style)
    {
        super(text, style);
    }

    public final void run()
    {
        super.run();

        INamespace namespace = fEditor.getNamespace();
        if (namespace == null)
            return;

        if (fDocumentOffset < 0)
            return;

        fDeclaredRootElementName = null;
        fPublicId = null;
        fDTD = null;

        try
        {
            fDocument = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
//    TODO remove        fPartitioner.connect(fDocument);
            if (fDocument.getLength() == 0 || fDocument.get().trim().length() == 0)
                return;

            try
            {
                XMLNode root = XMLNode.createTree(fDocument, -1);
                fPublicId = root.fPublicId;
                fDeclaredRootElementName = root.fRootNodeId;
                fDTD = DOMValidator.getDTD(fPublicId);

            } catch (BadLocationException e)
            {
                UIPlugin.log(e);
                return;
            }

            if (fDTD == null || fDeclaredRootElementName == null)
                return;

            doRun();
        } catch (RuntimeException e)
        {
            UIPlugin.log(e);
            throw e;
        }
//   TODO remove     finally
//        {
//            fPartitioner.disconnect();
//        }
    }

    protected abstract void doRun();

}
