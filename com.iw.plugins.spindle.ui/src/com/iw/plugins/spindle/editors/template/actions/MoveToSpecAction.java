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

package com.iw.plugins.spindle.editors.template.actions;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.actions.RequiredSaveEditorAction;
import com.iw.plugins.spindle.core.builder.TapestryArtifactManager;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Markers;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.template.TemplatePartitionScanner;
import com.iw.plugins.spindle.ui.util.UIUtils;
import com.iw.plugins.spindle.ui.widgets.PixelConverter;
import com.iw.plugins.spindle.ui.widgets.ResizableWizardDialog;
import com.iw.plugins.spindle.ui.wizards.source.MoveImplicitToSpecWizard;

/**
 * Move an implictly declared component from the template to the specification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MoveToSpecAction extends BaseTemplateAction
{

    public static final String ACTION_ID = UIPlugin.PLUGIN_ID + ".template.moveImplicitToSpec";

    private PluginComponentSpecification fRelatedSpec;
    private int fOffset;
    private XMLNode fNode;
    private List fAttributeList;

    public MoveToSpecAction()
    {
        super();
        setText("Move Implicit to Spec");
    }

    /** this method must operate in isolation (document) as the partitioner
     *  has not yet been attached.
     */
    public void update()
    {
        if (fEditor.isEditable())
        {
            fRelatedSpec = (PluginComponentSpecification) fEditor.getSpecification();
            if (fRelatedSpec != null)
            {
                try
                {
                    fOffset = fEditor.getCaretOffset();
                    IDocument document = fEditor.getDocumentProvider().getDocument(fEditor.getEditorInput());
                    ITypedRegion region = document.getPartition(fOffset);
                    if (region != null && region.getType() == TemplatePartitionScanner.TAPESTRY_JWCID_ATTRIBUTE)
                    {
                        String content = document.get(region.getOffset(), region.getLength());
                        setEnabled(
                            content != null
                                && content.trim().length() > 2
                                && content.indexOf('@') >= 0
                                && content.indexOf('$') < 0);
                        return;
                    }
                } catch (BadLocationException e)
                {}
            }
        }
        setEnabled(false);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.template.actions.BaseTemplateAction#doRun()
     */
    protected void doRun()
    {
        fNode = null;
        fAttributeList = null;
        if (!checkNode())
            return;

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) fRelatedSpec.getSpecificationLocation();
        IStorage storage = location.getStorage();
        if (storage == null)
            UIPlugin.log("Move Implicit..storage was null");

        IFile file = null;
        if (!(storage instanceof IFile))
        {
            MessageDialog.openInformation(
                UIPlugin.getDefault().getActiveWorkbenchShell(),
                "Aborting Operation",
                location.getName() + " is not editable. ");
            return;
        }

        file = (IFile) storage;

        IEditorPart editor = UIUtils.getEditorFor(location);
        if (editor != null)
        {
            if (editor.isDirty())
            {
                RequiredSaveEditorAction action = new RequiredSaveEditorAction(editor);
                if (!action.save("Must save", location.getName() + " must be saved to continue"))
                    return;
            }
        }
        // now the target spec has been saved
        // we must check for fatal build errors on the file..
        IMarker[] errors = Markers.getFatalProblemsFor(file);
        if (errors.length != 0)
        {
            MessageDialog.openError(
                UIPlugin.getDefault().getActiveWorkbenchShell(),
                "Aborting Operation",
                location.getName() + " has fatal problems. ");
            return;
        }
        fRelatedSpec = null;
        Map specMap = TapestryArtifactManager.getTapestryArtifactManager().getSpecMap(file.getProject(), false);

        if (specMap != null)
            fRelatedSpec = (PluginComponentSpecification) specMap.get(file);

        if (fRelatedSpec == null)
        {
            MessageDialog.openError(
                UIPlugin.getDefault().getActiveWorkbenchShell(),
                "Aborting Operation",
                "could not obtain specification object for '" + location.getName() + "'. ");
            return;
        }
        disconnect();
        launchWizard(UIPlugin.getDefault().getActiveWorkbenchShell());
    }

    private void launchWizard(Shell shell)
    {
        MoveImplicitToSpecWizard wizard =
            new MoveImplicitToSpecWizard((TemplateEditor) fEditor, fNode, fAttributeList, null, fRelatedSpec);
        WizardDialog dialog = new ResizableWizardDialog(shell, wizard);
        PixelConverter converter = new PixelConverter(shell);
        dialog.setMinimumPageSize(converter.convertWidthInCharsToPixels(110), converter.convertHeightInCharsToPixels(25));
        dialog.open();
    }

    /**
     * @return
     */
    private boolean checkNode()
    {
        String message = null;
        fNode = XMLNode.getArtifactAt(fDocument, fOffset);
        if (fNode == null
            || (fNode.getType() != XMLDocumentPartitioner.TAG && fNode.getType() != XMLDocumentPartitioner.EMPTYTAG))
        {
            message = "invalid selection at cursor position.";
        } else if (!fNode.isTerminated())
        {
            message = "tag at cursor postion is not properly terminated.";
        } else if (fNode.getName() == null)
        {
            message = "tag at cursor does not have a name";
        } else
        {
            fAttributeList = fNode.getAttributes();
            if (fAttributeList.isEmpty())
            {
                message = "unable to extract attributes from tag at cursor position.";
            } else
            {
                Set seenNames = new HashSet();
                for (Iterator iter = fAttributeList.iterator(); iter.hasNext();)
                {
                    XMLNode attribute = (XMLNode) iter.next();
                    String name = attribute.getName();
                    if (seenNames.contains(name))
                    {
                        message = "attribute name'" + name + " occurs more than once.";
                        break;
                    }
                    seenNames.add(name);
                    if (!attribute.isTerminated())
                    {
                        message = "attribute '" + name + "  is not properly terminated.";
                        break;
                    }
                    String content = attribute.getAttributeValue();
                    if (content == null)
                    {
                        message = "unable to obtain a value for '" + name + "'. Ensure the tag is well formed";
                        break;
                    }
                    if (content.length() == 0)
                    {
                        message = "attributes can't be empty (" + name + ").";
                        break;
                    }
                    if (content.indexOf('<') >= 0)
                    {
                        message =
                            "attribute '" + name + "' may not be properly terminated, or contain unescaped characters.";
                        break;
                    }
                }
            }
        }
        if (message != null)
        {
            MessageDialog.openError(UIPlugin.getDefault().getActiveWorkbenchShell(), "Aborting Operation", message);
            return false;
        }
        return true;
    }

}
