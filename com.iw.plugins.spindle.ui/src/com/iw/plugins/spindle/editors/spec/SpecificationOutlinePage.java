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

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.views.contentoutline.ContentOutlinePage;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.util.DocumentArtifact;
import com.iw.plugins.spindle.editors.util.DocumentArtifactPartitioner;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecificationOutlinePage extends ContentOutlinePage
{

    public class BasicContentProvider implements ITreeContentProvider
    {
        public Object[] getElements(Object obj)
        {
            if (fRoot != null)
            {
                return fRoot.getChildren(fRoot);
            }
            return new Object[] {};
        }
        public Object[] getChildren(Object obj)
        {
            if (obj instanceof DocumentArtifact)
                return ((DocumentArtifact) obj).getChildren(obj);
            return new Object[0];
        }
        public boolean hasChildren(Object obj)
        {
            return getChildren(obj).length > 0;
        }
        public Object getParent(Object obj)
        {
            if (obj == fRoot)
                return null;
            return ((DocumentArtifact) obj).getParent();
        }
        public void dispose()
        {}
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {}

    }

    public class BasicLabelProvider extends LabelProvider
    {
        public String getText(Object obj)
        {
            if (obj instanceof DocumentArtifact)
            {
                DocumentArtifact artifact = (DocumentArtifact) obj;
                String type = artifact.getType();

                if (type == DocumentArtifactPartitioner.TAG
                    || type == DocumentArtifactPartitioner.EMPTYTAG
                    || type == DocumentArtifactPartitioner.DECL)
                {
                    String name = artifact.getName();
                    return name == null ? "" : name;
                }

                if (type == DocumentArtifactPartitioner.ATTR)
                {
                    String name = artifact.getName();
                    String attrvalue = artifact.getAttributeValue();
                    return (name == null ? "" : name)
                        + " = "
                        + StringUtils.abbreviate(attrvalue == null ? "" : attrvalue, 50);
                }

                if (type == DocumentArtifactPartitioner.COMMENT)
                    return "COMMENT" + StringUtils.abbreviate(artifact.getContent().trim(), 50);

                if (type == DocumentArtifactPartitioner.TEXT)
                    return StringUtils.abbreviate(artifact.getContent().trim(), 50);

                if (type == DocumentArtifactPartitioner.PI)
                    return StringUtils.abbreviate(artifact.getContent().trim(), 50);
            }

            return obj.toString();
        }
        public Image getImage(Object obj)
        {
            if (obj instanceof DocumentArtifact)
            {
                DocumentArtifact artifact = (DocumentArtifact) obj;
                String type = artifact.getType();
                if (type == DocumentArtifactPartitioner.DECL)
                {

                    if (artifact.getParent().getType().equals("/"))
                        return Images.getSharedImage("decl16.gif");

                    return Images.getSharedImage("cdata16.gif");

                }

                if (type == DocumentArtifactPartitioner.TAG)
                    return Images.getSharedImage("tag16.gif");

                if (type == DocumentArtifactPartitioner.EMPTYTAG)
                    return Images.getSharedImage("empty16.gif");

                if (type == DocumentArtifactPartitioner.ATTR)
                    return Images.getSharedImage("bullet.gif");

                if (type == DocumentArtifactPartitioner.COMMENT)
                    return Images.getSharedImage("comment16.gif");

                if (type == DocumentArtifactPartitioner.TEXT)
                    return Images.getSharedImage("text16.gif");

                if (type == DocumentArtifactPartitioner.PI)
                    return Images.getSharedImage("pi16.gif");
            }
            return null;
        }
    }
    private SpecEditor fEditor;
    private TreeViewer treeViewer;
    private DocumentArtifact fRoot;
    private boolean fFireSelection = true;

    public SpecificationOutlinePage(SpecEditor editor)
    {
        fEditor = editor;
    }

    public void createControl(Composite parent)
    {
        Tree widget = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer = new TreeViewer(widget);
        treeViewer.addSelectionChangedListener(this);
        treeViewer.setContentProvider(createContentProvider());
        treeViewer.setLabelProvider(createLabelProvider());
        treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        treeViewer.setUseHashlookup(true);
    }

    public void setRoot(final DocumentArtifact artifact)
    {
        fRoot = artifact;
        Display d = getControl().getDisplay();
        d.asyncExec(new Runnable()
        {
            public void run()
            {
                try
                {

                    fFireSelection = false;
                    ISelection oldSelection = getSelection();
                    treeViewer.setInput(fRoot);
//                    treeViewer.refresh();
                    treeViewer.setSelection(oldSelection);
                } catch (RuntimeException e)
                {
                    UIPlugin.log(e);
                } finally
                {
                    fFireSelection = true;
                }
            }
        });
    }

    protected ITreeContentProvider createContentProvider()
    {
        return new BasicContentProvider();
    }

    protected ILabelProvider createLabelProvider()
    {
        return new BasicLabelProvider();
    }

    public Control getControl()
    {
        return treeViewer != null ? treeViewer.getControl() : null;
    }

    public void setFocus()
    {
        if (treeViewer != null)
            treeViewer.getTree().setFocus();
    }

    public ISelection getSelection()
    {
        if (treeViewer == null)
            return StructuredSelection.EMPTY;
        return treeViewer.getSelection();
    }
    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ISelectionChangedListener#selectionChanged(org.eclipse.jface.viewers.SelectionChangedEvent)
     */
    public void selectionChanged(SelectionChangedEvent event)
    {
        if (fFireSelection)
            super.selectionChanged(event);
    }

}