/*
 * (c) Copyright 2001 MyCorporation.
 * All Rights Reserved.
 */
package com.iw.plugins.spindle.editors.scratch;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ViewForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Item;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPart;

/**
 * Implementation of a TiledEditor
 */
public class StackedMultiEditor extends MultiEditor
{
    private static final boolean test = true;
    private CTabFolder fTabFolder;
    private CLabel innerEditorTitle[];

    /*
     * @see IWorkbenchPart#createPartControl(Composite)
     */
    public void createPartControl(Composite parent)
    {
        if (test)
        {
            createOtherPartControl(parent);
            return;
        }
        parent = new Composite(parent, SWT.BORDER);

        parent.setLayout(new FillLayout());
        SashForm sashForm = new SashForm(parent, SWT.HORIZONTAL);
        IEditorPart innerEditors[] = getInnerEditors();

        for (int i = 0; i < innerEditors.length; i++)
        {
            final IEditorPart e = innerEditors[i];
            ViewForm viewForm = new ViewForm(sashForm, SWT.NONE);
            viewForm.marginWidth = 0;
            viewForm.marginHeight = 0;

            createInnerEditorTitle(i, viewForm);

            Composite content = createInnerPartControl(viewForm, e);

            viewForm.setContent(content);
            updateInnerEditorTitle(e, innerEditorTitle[i]);

            final int index = i;
            e.addPropertyListener(new IPropertyListener()
            {
                public void propertyChanged(Object source, int property)
                {
                    if (property == IEditorPart.PROP_DIRTY || property == IWorkbenchPart.PROP_TITLE)
                        if (source instanceof IEditorPart)
                            updateInnerEditorTitle((IEditorPart) source, innerEditorTitle[index]);
                }
            });
        }
    }

    public void createOtherPartControl(Composite parent)
    {
        fTabFolder = createContainer(parent);
        createTabs();
    }

    protected void createTabs()
    {
        IEditorPart innerEditors[] = getInnerEditors();
        for (int i = 0; i < innerEditors.length; i++)
        {
            final IEditorPart e = innerEditors[i];
            Composite parent = new Composite(fTabFolder, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 1;
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.horizontalSpacing = 0;
            layout.verticalSpacing = 0;
            parent.setLayout(new GridLayout());
            Control title = createInnerEditorTitle(i, parent);
            title.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            Composite content = createInnerPartControl(parent, e);
            layout = new GridLayout();

            content.setLayoutData(new GridData(GridData.FILL_BOTH));
            final int index = i;
            e.addPropertyListener(new IPropertyListener()
            {
                public void propertyChanged(Object source, int property)
                {
                    if (property == IEditorPart.PROP_DIRTY || property == IWorkbenchPart.PROP_TITLE)
                        if (source instanceof IEditorPart)
                            updateInnerEditorTitle((IEditorPart) source, innerEditorTitle[index]);
                }
            });
            Item item = createItem(parent);
            item.setData(e);
            updateInnerEditorTitle(e, innerEditorTitle[i]);
        }
        fTabFolder.setSelection(fTabFolder.getItem(0));
    }

    /**
     * Draw the gradient for the specified editor.
     */
    protected void drawGradient(IEditorPart innerEditor, Gradient g)
    {
        CLabel label = innerEditorTitle[getIndex(innerEditor)];
        if ((label == null) || label.isDisposed())
            return;

        label.setForeground(g.fgColor);
        label.setBackground(g.bgColors, g.bgPercents);
    }
    /*
     * Create the label for each inner editor. 
     */
    protected Control createInnerEditorTitle(int index, Composite parent)
    {
        CLabel titleLabel = new CLabel(parent, SWT.SHADOW_NONE);
        //hookFocus(titleLabel);
        titleLabel.setAlignment(SWT.LEFT);
        titleLabel.setBackground(null, null);
        if (parent instanceof ViewForm)
        {
            ((ViewForm) parent).setTopLeft(titleLabel);
        }
        if (innerEditorTitle == null)
            innerEditorTitle = new CLabel[getInnerEditors().length];
        innerEditorTitle[index] = titleLabel;
        return titleLabel;
    }
    /*
     * Update the tab for an editor.  This is typically called
     * by a site when the tab title changes.
     */
    public void updateInnerEditorTitle(IEditorPart editor, CLabel label)
    {
        if ((label == null) || label.isDisposed())
            return;

        String title = editor.getTitle();
        Image image = editor.getTitleImage();
        String toolTipText = editor.getTitleToolTip();

        if (editor.isDirty())
            title = "*" + title; //$NON-NLS-1$
        label.setText(title);
        if (image != null)
            if (!image.equals(label.getImage()))
                label.setImage(image);
        label.setToolTipText(toolTipText);

        int index = getIndex(editor);
        CTabItem item = fTabFolder.getItem(index);
        item.setText(title);
        if (image != null)
            if (!image.equals(item.getImage()))
                item.setImage(image);

        item.setToolTipText(toolTipText);
    }

    private CTabFolder createContainer(Composite parent)
    {
        final CTabFolder container = new CTabFolder(parent, SWT.BOTTOM);
        container.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                int newPageIndex = container.indexOf((CTabItem) e.item);
                StackedMultiEditor.this.pageChange(newPageIndex);
            }
        });
        return container;
    }

    private CTabItem createItem(Control control)
    {
        CTabItem item = new CTabItem(fTabFolder, SWT.NONE);
        item.setControl(control);
        return item;
    }

    protected void pageChange(int index)
    {}
}