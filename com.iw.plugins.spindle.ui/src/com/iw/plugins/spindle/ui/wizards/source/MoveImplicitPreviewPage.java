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

package com.iw.plugins.spindle.ui.wizards.source;

import java.util.ArrayList;

import org.eclipse.compare.CompareConfiguration;
import org.eclipse.compare.contentmergeviewer.TextMergeViewer;
import org.eclipse.compare.internal.MergeViewerContentProvider;
import org.eclipse.core.resources.IStorage;
import org.eclipse.jface.text.TextViewer;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

import com.iw.plugins.spindle.Images;

/**
 *  Preview the changes that will be made to move an implicit component declaration
 *  from a template to its specification
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MoveImplicitPreviewPage extends WizardPage
{

    static private final SourceViewerConfiguration fViewerConfig = new SimpleSourceViewerConfiguration();

    private MoveImplicitToSpecWizard fWizard;
    private TextMergeViewer fTextViewer;
    private CompareConfiguration fConfiguration = new Configuration();

    private TableViewer fTableViewer;
    private ILabelProvider fLabelProvider;
    private Object[] fViewerContents;

    public MoveImplicitPreviewPage(String pageName, MoveImplicitToSpecWizard wizard)
    {
        super(pageName);
        fWizard = wizard;
        setImageDescriptor(Images.getImageDescriptor("applicationDialog.gif"));
        setDescription("Preview. Warning: clicking 'finish' is not undoable!");
        ArrayList contents = new ArrayList();
        contents.add(wizard.getTemplateStorage());
        contents.add(wizard.getSpecStorage());
        fViewerContents = contents.toArray();
    }

    public void refresh()
    {
        // bump the viewer to update its view
        if (fTextViewer != null)
            fTextViewer.setInput(new Long(System.currentTimeMillis()));
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);
        container.setLayoutData(new GridData(GridData.FILL_BOTH));

        createSelectionViewer(container);

        ContentProvider provider = new ContentProvider(fConfiguration);
        fTextViewer = new XMLTextViewer(container, fConfiguration);
        fTextViewer.setContentProvider(provider);
        fTextViewer.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        setControl(container);
    }

    private void createSelectionViewer(Composite parent)
    {
        fTableViewer = new TableViewer(parent);
        Control tree = fTableViewer.getControl();
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        tree.setLayoutData(data);
        fTableViewer.setContentProvider(new IStructuredContentProvider()
        {
            public Object[] getElements(Object inputElement)
            {
                return fViewerContents;
            }

            public void dispose()
            {}

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {}
        });
        fLabelProvider = new ILabelProvider()
        {
            public Image getImage(Object element)
            {
                String name = ((IStorage) element).getName();
                if (name.endsWith(".page"))
                {
                    return Images.getSharedImage("page16.gif");
                } else if (name.endsWith(".jwc"))
                {
                    return Images.getSharedImage("component16.gif");
                } else
                {
                    return Images.getSharedImage("html16.gif");
                }
            }

            public String getText(Object element)
            {
                return ((IStorage) element).getName();
            }

            public void addListener(ILabelProviderListener listener)
            {}

            public void dispose()
            {}

            public boolean isLabelProperty(Object element, String property)
            {
                return false;
            }

            public void removeListener(ILabelProviderListener listener)
            {}
        };
        fTableViewer.setLabelProvider(fLabelProvider);
        fTableViewer.setInput("dummy");
        fTableViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                refresh();
            }
        });
        fTableViewer.setSelection(new StructuredSelection(fViewerContents[0]));
    }

    private Object getSelection()
    {
        IStructuredSelection selection = (IStructuredSelection) fTableViewer.getSelection();
        if (selection.isEmpty())
            return null;
        return selection.getFirstElement();
    }

    class Configuration extends CompareConfiguration
    {
        /* (non-Javadoc)
         * @see org.eclipse.compare.CompareConfiguration#getLeftImage(java.lang.Object)
         */
        public Image getLeftImage(Object element)
        {
            return getRightImage(element);
        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.CompareConfiguration#getLeftLabel(java.lang.Object)
         */
        public String getLeftLabel(Object element)
        {
            Object realElement = getSelection();
            if (realElement == null)
                return null;
            return fLabelProvider.getText(realElement);
        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.CompareConfiguration#getRightImage(java.lang.Object)
         */
        public Image getRightImage(Object element)
        {
            Object realElement = getSelection();
            if (realElement == null)
                return null;
            return fLabelProvider.getImage(realElement);
        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.CompareConfiguration#getRightLabel(java.lang.Object)
         */
        public String getRightLabel(Object element)
        {
            Object realElement = getSelection();
            if (realElement == null)
                return null;
            return fLabelProvider.getText(realElement) + " : MODIFIED";
        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.CompareConfiguration#isLeftEditable()
         */
        public boolean isLeftEditable()
        {
            return false;
        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.CompareConfiguration#isRightEditable()
         */
        public boolean isRightEditable()
        {
            return false;
        }
    }

    class ContentProvider extends MergeViewerContentProvider
    {
        public ContentProvider(CompareConfiguration cc)
        {
            super(cc);
        }
        /* (non-Javadoc)
         * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getLeftContent(java.lang.Object)
         */
        public Object getLeftContent(Object element)
        {
            Object selection = getSelection();
            if (selection == null)
                return null;
            if (selection == fViewerContents[0])
            {
                return fWizard.getOriginalTemplateDocument();
            } else
            {
                return fWizard.getOriginalSpecDocument();
            }

        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.contentmergeviewer.IMergeViewerContentProvider#getRightContent(java.lang.Object)
         */
        public Object getRightContent(Object element)
        {
            Object selection = getSelection();
            if (selection == null)
                return null;
            if (selection == fViewerContents[0])
            {
                return fWizard.getModifiedTemplateDocument();
            } else
            {
                return fWizard.getModifiedSpecDocument();
            }
        }

    }

    class XMLTextViewer extends TextMergeViewer
    {
        public XMLTextViewer(Composite parent, CompareConfiguration configuration)
        {
            super(parent, SWT.BORDER, configuration);
        }

        /* (non-Javadoc)
         * @see org.eclipse.compare.contentmergeviewer.TextMergeViewer#configureTextViewer(org.eclipse.jface.text.TextViewer)
         */
        protected void configureTextViewer(TextViewer textViewer)
        {
            if (textViewer instanceof SourceViewer)
            {
                ((SourceViewer) textViewer).configure(fViewerConfig);
            }
        }

        // to avoid pointless 'save' messages!
        protected boolean doSave(Object newInput, Object oldInput)
        {
            return true;
        }
    }
}
