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
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
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
                Object[] result = fRoot.getChildren(fRoot);
                addAll(result);
                return result;
            }
            return new Object[] {};
        }
        public Object[] getChildren(Object obj)
        {
            if (obj instanceof DocumentArtifact)
            {
                Object[] result = ((DocumentArtifact) obj).getChildren(obj);
                addAll(result);
                return result;
            }

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

        private void addAll(Object[] elements)
        {
            if (elements == null || elements.length == 0)
                return;

            if (fFlatChildren.length == 0)
            {
                fFlatChildren = elements;
                fCorresponders = new Object[elements.length];
                for (int i = 0; i < elements.length; i++)
                    fCorresponders[i] = ((DocumentArtifact) elements[i]).getCorrespondingNode();
                return;
            }

            Object[] expandedFlat = new Object[fFlatChildren.length + elements.length];
            System.arraycopy(fFlatChildren, 0, expandedFlat, 0, fFlatChildren.length);
            System.arraycopy(elements, 0, expandedFlat, fFlatChildren.length, elements.length);
            Object[] expandedCorresponders = new Object[fCorresponders.length + elements.length];
            System.arraycopy(fCorresponders, 0, expandedCorresponders, 0, fCorresponders.length);
            for (int i = 0; i < elements.length; i++)
            {
                expandedCorresponders[fCorresponders.length + i] =
                    ((DocumentArtifact) elements[i]).getCorrespondingNode();
            }

            fFlatChildren = expandedFlat;
            fCorresponders = expandedCorresponders;

        }
        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {
            fFlatChildren = new DocumentArtifact[0];
            fCorresponders = new DocumentArtifact[0];
        }

    }

    public class BasicLabelProvider extends LabelProvider //implements IColorProvider
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

        //        /* (non-Javadoc)
        //         * @see org.eclipse.jface.viewers.IColorProvider#getBackground(java.lang.Object)
        //         */
        //        public Color getBackground(Object element)
        //        {
        //            return fTree.getBackground();
        //        }
        //
        //        private Color getColor(String key)
        //        {
        //            ISharedTextColors colors = UIPlugin.getDefault().getSharedTextColors();
        //            return colors.getColor(PreferenceConverter.getColor(fPreferences, key + ITextStylePreferences.SUFFIX_FOREGROUND));
        //        }
        //
        //        /* (non-Javadoc)
        //         * @see org.eclipse.jface.viewers.IColorProvider#getForeground(java.lang.Object)
        //         */
        //        public Color getForeground(Object element)
        //        {
        //
        //            if (element instanceof DocumentArtifact)
        //            {
        //                DocumentArtifact artifact = (DocumentArtifact) element;
        //                String type = artifact.getType();
        //                if (type == DocumentArtifactPartitioner.DECL)
        //                {
        //
        //                    if (artifact.getParent().getType().equals("/"))
        //                        return getColor(IXMLSyntaxConstants.XML_DECL);
        //
        //                    return getColor(IXMLSyntaxConstants.XML_CDATA);
        //
        //                }
        //
        //                if (type == DocumentArtifactPartitioner.TAG)
        //                    return getColor(IXMLSyntaxConstants.XML_TAG);
        //
        //                if (type == DocumentArtifactPartitioner.EMPTYTAG)
        //                    return getColor(IXMLSyntaxConstants.XML_TAG);
        //
        //                if (type == DocumentArtifactPartitioner.ATTR)
        //                    return getColor(IXMLSyntaxConstants.XML_ATT_VALUE);
        //
        //                if (type == DocumentArtifactPartitioner.COMMENT)
        //                    return getColor(IXMLSyntaxConstants.XML_COMMENT);
        //
        //                if (type == DocumentArtifactPartitioner.PI)
        //                    return getColor(IXMLSyntaxConstants.XML_PI);
        //            }
        //            return fTree.getForeground();
        //        }

    }

    private SpecEditor fEditor;
    private Tree fTree;
    private TreeViewer treeViewer;
    private DocumentArtifact fRoot;
    private Object[] fFlatChildren = new DocumentArtifact[0];
    private Object[] fCorresponders = new DocumentArtifact[0];
    private IPreferenceStore fPreferences;
    private IPropertyChangeListener fPreferenceStoreListener = new IPropertyChangeListener()
    {
        public void propertyChange(PropertyChangeEvent event)
        {
            treeViewer.refresh(true);
        }

    };

    public SpecificationOutlinePage(SpecEditor editor, IPreferenceStore store)
    {
        fEditor = editor;
        fPreferences = store;
    }

    public void createControl(Composite parent)
    {
        fTree = new Tree(parent, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
        treeViewer = new TreeViewer(fTree);
        treeViewer.addSelectionChangedListener(this);
        treeViewer.setContentProvider(createContentProvider());
        treeViewer.setLabelProvider(createLabelProvider());
        treeViewer.setAutoExpandLevel(TreeViewer.ALL_LEVELS);
        treeViewer.setUseHashlookup(true);
        treeViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) treeViewer.getSelection();
                if (!selection.isEmpty())
                    fEditor.openTo(selection.getFirstElement());
            }
        });
        fPreferences.addPropertyChangeListener(fPreferenceStoreListener);

    }

    public void dispose()
    {
        fPreferences.removePropertyChangeListener(fPreferenceStoreListener);
        super.dispose();
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
                    ISelection oldSelection = getSelection();
                    treeViewer.setInput(fRoot);
                    //                    treeViewer.refresh();
                    treeViewer.setSelection(oldSelection);
                } catch (RuntimeException e)
                {
                    UIPlugin.log(e);
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
     * @see org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse.jface.viewers.ISelection)
     */
    public void setSelection(ISelection selection)
    {
        if (!selection.isEmpty() && selection instanceof IStructuredSelection)
        {
            Object selected = ((IStructuredSelection) selection).getFirstElement();
            if (selected instanceof IRegion && fRoot != null)
            {
                int documentOffset = ((IRegion) selected).getOffset();
                Object found = null;
                for (int i = 0; i < fFlatChildren.length; i++)
                {
                    Position p = (Position) fFlatChildren[i];
                    if (p.offset <= documentOffset && documentOffset < p.offset + p.length)
                    {
                        found = p;
                    }
                }
                if (found == null)
                {
                    int index = 0;
                    boolean exists = false;
                    for (; index < fCorresponders.length; index++)
                    {
                        Position p = (Position) fCorresponders[index];
                        if (p != null && p.offset <= documentOffset && documentOffset < p.offset + p.length)
                        {
                            exists = true;
                            break;
                        }
                    }
                    if (exists)
                        found = fFlatChildren[index];
                }
                if (found != null)
                {
                    treeViewer.setSelection(new StructuredSelection(found));
                } else
                {
                    treeViewer.setSelection(StructuredSelection.EMPTY);
                }
            }
            super.setSelection(selection);
        }

    }

}