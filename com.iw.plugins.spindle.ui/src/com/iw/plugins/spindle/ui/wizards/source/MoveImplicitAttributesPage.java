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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.core.util.SpindleStatus;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.ui.dialogfields.DialogField;
import com.iw.plugins.spindle.ui.dialogfields.IDialogFieldChangedListener;
import com.iw.plugins.spindle.ui.dialogfields.StringField;
import com.iw.plugins.spindle.ui.widgets.SectionWidget;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2003, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class MoveImplicitAttributesPage extends WizardPage
{

    private IComponentSpecification fContainerSpec;

    private String fInitialTemplateComponentId;
    private StringField fTemplateComponentId;
    private String fPublicId;

    private TableViewer fAttributesThatStayViewer;
    private TableViewer fAttributesThatMoveViewer;
    private Button fRightButton;
    private Button fLeftButton;
    private Button fUpButton;
    private Button fDownButton;

    private ArrayList fAllAttributes = new ArrayList();
    private ArrayList fAttributesThatStay = new ArrayList();
    private ArrayList fAttributesThatMove = new ArrayList();

    private IScannerValidator fValidator = new BaseValidator();

    private Status fCurrentStatus;

    public MoveImplicitAttributesPage(String pageName)
    {
        super(pageName);
        this.setImageDescriptor(Images.getImageDescriptor("applicationDialog.gif"));
        this.setDescription("Decide what is moved. Warning: clicking 'finish' is not undoable!");
        fTemplateComponentId = new StringField("Component Id:");
        fTemplateComponentId.addListener(new IDialogFieldChangedListener()
        {
            public void dialogFieldChanged(DialogField field)
            {
                if (field == fTemplateComponentId)
                    updateStatus();
            }
            public void dialogFieldButtonPressed(DialogField field)
            {}
            public void dialogFieldStatusChanged(IStatus status, DialogField field)
            {}
        });
        fCurrentStatus = new Status(null);
    }

    public void init(
        IComponentSpecification containerSpec,
        String simpleId,
        List attributeList,
        String publicId,
        List declaredParameterNames)
    {
        fContainerSpec = containerSpec;
        fInitialTemplateComponentId = simpleId;
        fAttributesThatStay = new ArrayList();
        fAttributesThatMove = new ArrayList();
        for (Iterator iter = attributeList.iterator(); iter.hasNext();)
        {
            XMLNode node = (XMLNode) iter.next();
            String name = node.getName();
            if (name.equals(TemplateParser.JWCID_ATTRIBUTE_NAME))
                continue;
            String content = node.getAttributeValue();
            boolean isTapestry =
                content.startsWith("ognl:") || content.startsWith("message:") || content.startsWith("string:");
            if (declaredParameterNames.contains(name) || isTapestry)
                fAttributesThatMove.add(node);
            else
                fAttributesThatStay.add(node);

            fAllAttributes.add(fAttributesThatStay);
            fAllAttributes.add(fAttributesThatMove);
        }
        fPublicId = publicId;
    }

    /**
     * @param string
     */
    protected Status getComponentIdStatus(String newValue)
    {
        Status status = new Status(fTemplateComponentId);
        if (newValue == null || newValue.trim().length() == 0)
        {
            status.setError("must enter a component id.");
            return status;
        }
        if (fContainerSpec.getComponentIds().contains(newValue))
        {
            status.setError(
                "'"
                    + fContainerSpec.getSpecificationLocation().getName()
                    + "' already has a component with id = '"
                    + newValue
                    + "'");
            return status;
        }

        try
        {
            fValidator.validatePattern(
                newValue,
                SpecificationParser.COMPONENT_ID_PATTERN,
                "SpecificationParser.invalid-component-id",
                0);
        } catch (ScannerException e)
        {
            status.setError(e.getMessage());
            return status;
        }
        return status;
    }

    protected Status getToMoveStatus()
    {
        Status status = new Status(fAttributesThatMove);
        if (fAttributesThatMove.isEmpty() && !fAllAttributes.isEmpty())
            status.setWarning("No attributes will move!");

        return status;
    }

    private void updateStatus()
    {
        Status idStatus = getComponentIdStatus(fTemplateComponentId.getTextValue());
        Status toMoveStatus = getToMoveStatus();
        setErrorMessage(null);
        setMessage(null);
        if (!idStatus.isOK())
        {
            setErrorMessage(idStatus.getMessage());
            fCurrentStatus = idStatus;
        } else if (!toMoveStatus.isOK())
        {
            setMessage(toMoveStatus.getMessage(), toMoveStatus.getSeverity());
            fCurrentStatus = toMoveStatus;
        }
        setPageComplete(!fCurrentStatus.isError());
    }

    public String getTemplateComponentId()
    {
        return fTemplateComponentId.getTextValue();
    }

    public List getAttributesThatMove()
    {
        return fAttributesThatMove;
    }

    public List getAttributesThatStay()
    {
        return fAttributesThatStay;
    }

    public void createControl(Composite parent)
    {

        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.numColumns = 2;
        layout.makeColumnsEqualWidth = false;
        container.setLayout(layout);
        GridData gd;

        Control simpleIdControl = fTemplateComponentId.getControl(container);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        simpleIdControl.setLayoutData(gd);
        fTemplateComponentId.setTextValue(fInitialTemplateComponentId == null ? "" : fInitialTemplateComponentId);

        Composite leftColumn = new Composite(container, SWT.NULL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.widthHint = convertWidthInCharsToPixels(30);
        leftColumn.setLayoutData(gd);
        GridLayout leftLayout = new GridLayout();
        leftLayout.verticalSpacing = 10;
        leftLayout.marginWidth = 0;
        leftColumn.setLayout(leftLayout);
        Composite rightColumn = new Composite(container, SWT.NULL);
        gd = new GridData(GridData.FILL_BOTH);
        rightColumn.setLayoutData(gd);
        GridLayout rightLayout = new GridLayout();
        rightLayout.verticalSpacing = 10;
        rightLayout.marginWidth = 0;
        rightColumn.setLayout(rightLayout);
        AttributesThatStaySection allSection = new AttributesThatStaySection("Attibutes that stay");
        Control allSectionControl = allSection.createControl(leftColumn);
        gd = new GridData(GridData.FILL_BOTH);
        allSectionControl.setLayoutData(gd);
        AttributesThatMoveSection pageSection = new AttributesThatMoveSection("Attributes that move");
        Control pageSectionControl = pageSection.createControl(rightColumn);
        gd = new GridData(GridData.FILL_BOTH);
        pageSectionControl.setLayoutData(gd);
        fAttributesThatStayViewer.setContentProvider(new IStructuredContentProvider()
        {
            public Object[] getElements(Object inputElement)
            {
                return fAttributesThatStay.toArray();
            }
            public void dispose()
            {}
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {}
        });
        fAttributesThatStayViewer.setLabelProvider(new AttributesLabelProvider());
        fAttributesThatStayViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                updateButtonsEnabled();
            }
        });
        fAttributesThatStayViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {

                IStructuredSelection selection = (IStructuredSelection) fAttributesThatStayViewer.getSelection();
                if (!selection.isEmpty())
                {
                    handleLeftRightChange(fAttributesThatStayViewer, selection.getFirstElement());
                }
            }
        });
        fAttributesThatMoveViewer.setContentProvider(new IStructuredContentProvider()
        {
            public Object[] getElements(Object inputElement)
            {
                return fAttributesThatMove.toArray();
            }
            public void dispose()
            {}
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {}
        });
        fAttributesThatMoveViewer.setLabelProvider(new BindingsLabelProvider());
        fAttributesThatMoveViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                updateButtonsEnabled();
            }
        });
        fAttributesThatMoveViewer.addDoubleClickListener(new IDoubleClickListener()
        {
            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) fAttributesThatMoveViewer.getSelection();
                if (!selection.isEmpty())
                {
                    handleLeftRightChange(fAttributesThatMoveViewer, selection.getFirstElement());
                }
            }
        });

        createUpDownButtons(container);

        fAttributesThatStayViewer.setInput(fAttributesThatStay);
        fAttributesThatMoveViewer.setInput(fAttributesThatMove);
        updateButtonsEnabled();
        setControl(container);
    }

    private void updateButtonsEnabled()
    {

        fRightButton.setEnabled(!fAttributesThatStay.isEmpty() && !fAttributesThatStayViewer.getSelection().isEmpty());
        fLeftButton.setEnabled(!fAttributesThatMove.isEmpty() && !fAttributesThatMoveViewer.getSelection().isEmpty());
        fUpButton.setEnabled(!fAttributesThatMove.isEmpty() && canMoveUp(fAttributesThatMoveViewer.getSelection()));
        fDownButton.setEnabled(!fAttributesThatMove.isEmpty() && canMoveDown(fAttributesThatMoveViewer.getSelection()));
    }

    /**
     * @param selection
     * @return
     */
    private boolean canMoveUp(ISelection selection)
    {
        if (selection instanceof IStructuredSelection && !selection.isEmpty())
        {
            IStructuredSelection structured = (IStructuredSelection) selection;
            return structured.size() == 1 && fAttributesThatMove.indexOf(structured.getFirstElement()) > 0;
        }
        return false;
    }

    /**
     * @param selection
     * @return
     */
    private boolean canMoveDown(ISelection selection)
    {
        if (selection instanceof IStructuredSelection && !selection.isEmpty())
        {
            IStructuredSelection structured = (IStructuredSelection) selection;
            return structured.size() == 1
                && fAttributesThatMove.indexOf(structured.getFirstElement()) < fAttributesThatMove.size() - 1;
        }
        return false;
    }

    private void createLeftRightButtons(Composite parent)
    {
        Composite buttonContainer = new Composite(parent, SWT.NULL);
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_CENTER | GridData.FILL_VERTICAL);
        buttonContainer.setLayoutData(gd);
        GridLayout buttonLayout = new GridLayout();
        buttonLayout.verticalSpacing = 5;
        buttonLayout.marginWidth = 0;
        buttonContainer.setLayout(buttonLayout);

        fRightButton = new Button(buttonContainer, SWT.BORDER);
        fRightButton.setText("");
        fRightButton.setImage(Images.getSharedImage("right.gif"));
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        fRightButton.setLayoutData(gd);
        fRightButton.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {

                handleLeftRightChange(fAttributesThatStayViewer, fAttributesThatStayViewer.getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {}
        });

        fLeftButton = new Button(buttonContainer, SWT.BORDER);
        fLeftButton.setText("");
        fLeftButton.setImage(Images.getSharedImage("left.gif"));
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        fLeftButton.setLayoutData(gd);
        fLeftButton.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {

                handleLeftRightChange(fAttributesThatMoveViewer, fAttributesThatMoveViewer.getSelection());
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {}
        });
    }

    private void createUpDownButtons(Composite parent)
    {
        GridLayout layout;
        GridData gd;
        Composite buttons = new Composite(parent, SWT.NONE);
        layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        layout.numColumns = 3;
        layout.makeColumnsEqualWidth = false;

        buttons.setLayout(layout);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;

        buttons.setLayoutData(gd);
        Label spacer = new Label(buttons, SWT.NULL);
        spacer.setText("");
        spacer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        fUpButton = new Button(buttons, SWT.BORDER);
        fUpButton.setText("");
        fUpButton.setImage(Images.getSharedImage("up.gif"));
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        fUpButton.setLayoutData(gd);
        fUpButton.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {

                handleUpDownChange(fAttributesThatMoveViewer.getSelection(), true);
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {}
        });

        fDownButton = new Button(buttons, SWT.BORDER);
        fDownButton.setText("");
        fDownButton.setImage(Images.getSharedImage("down.gif"));
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        fDownButton.setLayoutData(gd);
        fDownButton.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {

                handleUpDownChange(fAttributesThatMoveViewer.getSelection(), false);
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {}
        });
    }

    /**
     * @param selection
     * @param b
     */
    protected void handleUpDownChange(ISelection selection, boolean up)
    {
        //        if (up && !canMoveUp(selection))
        //        {
        //            return;
        //        } else if (!up && !canMoveDown(selection))
        //        {
        //            return;
        //        }
        IStructuredSelection structured = (IStructuredSelection) selection;
        Object moving = structured.getFirstElement();
        int index = fAttributesThatMove.indexOf(moving);
        if (up)
        {
            Object above = fAttributesThatMove.get(index - 1);
            fAttributesThatMove.set(index - 1, moving);
            fAttributesThatMove.set(index, above);
        } else
        {
            Object below = fAttributesThatMove.remove(index + 1);
            fAttributesThatMove.add(index, below);
        }
        fAttributesThatMoveViewer.setInput(fAttributesThatMove);
        fAttributesThatMoveViewer.getControl().setFocus();
        updateButtonsEnabled();
    }

    private void handleLeftRightChange(Viewer fromViewer, Object changeObject)
    {

        IStructuredSelection selection = null;
        if (changeObject instanceof ISelection)
        {

            selection = (IStructuredSelection) changeObject;
        } else
        {

            selection = new StructuredSelection(changeObject);
        }

        List fromList = null;
        List toList = null;
        TableViewer toViewer = null;
        if (fromViewer == fAttributesThatStayViewer)
        {

            fromList = fAttributesThatStay;
            toList = fAttributesThatMove;
            toViewer = fAttributesThatMoveViewer;
        } else
        {

            fromList = fAttributesThatMove;
            toList = fAttributesThatStay;
            toViewer = fAttributesThatStayViewer;
        }

        if (!selection.isEmpty())
        {

            List selectedObjects = selection.toList();
            fromList.removeAll(selectedObjects);
            toList.addAll(selectedObjects);
            fromViewer.setInput(fromList);
            toViewer.setInput(toViewer);
            toViewer.getControl().setFocus();
            toViewer.setSelection(selection);
        }
        updateButtonsEnabled();
        updateStatus();
    }

    class AttributesThatStaySection extends SectionWidget
    {
        public AttributesThatStaySection(String headerText)
        {
            super();
            setDescriptionPainted(false);
            setHeaderText(headerText);
        }
        public Composite createClient(Composite parent)
        {
            Composite composite = new Composite(parent, SWT.NONE);
            GridData gd;
            GridLayout layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            composite.setLayout(layout);
            gd = new GridData(GridData.FILL_BOTH);
            composite.setLayoutData(gd);
            Composite leftColumn = new Composite(composite, SWT.NULL);
            gd = new GridData(GridData.FILL_BOTH | GridData.VERTICAL_ALIGN_CENTER);
            leftColumn.setLayoutData(gd);
            GridLayout leftLayout = new GridLayout();
            leftLayout.verticalSpacing = 0;
            leftLayout.marginWidth = 0;
            leftColumn.setLayout(leftLayout);
            Table table = new Table(leftColumn, SWT.MULTI | SWT.BORDER);
            table.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));
            fAttributesThatStayViewer = new TableViewer(table);
            return composite;
        }

    }

    abstract class BaseProvider implements ILabelProvider
    {
        public Image getImage(Object element)
        {
            return null;
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
    }

    class BindingsLabelProvider extends BaseProvider
    {
        private PluginBindingSpecification binding = new PluginBindingSpecification();
        public String getText(Object element)
        {
            XMLNode node = (XMLNode) element;
            String name = node.getName();
            String value = node.getAttributeValue();
            if (value.startsWith("ognl:"))
            {
                value = value.substring(value.indexOf(':') + 1);
                binding.setType(BindingType.DYNAMIC);
            } else if (value.startsWith("message:") || value.startsWith("string:"))
            {
                value = value.substring(value.indexOf(':') + 1);
                binding.setType(BindingType.STRING);
            } else
            {
                binding.setType(BindingType.STATIC);
            }
            binding.setValue(value);
            StringWriter swriter = new StringWriter();
            PrintWriter pwriter = new PrintWriter(swriter);
            XMLUtil.writeBinding(name, binding, pwriter, 0, fPublicId);
            return swriter.toString();
        }
    }

    class AttributesLabelProvider extends BaseProvider
    {
        public String getText(Object element)
        {
            return ((XMLNode) element).getContent();
        }
    }

    class AttributesThatMoveSection extends SectionWidget
    {
        public AttributesThatMoveSection(String headerText)
        {
            super();
            setDescriptionPainted(false);
            setHeaderText(headerText);
        }

        public Composite createClient(Composite parent)
        {
            Composite container = new Composite(parent, SWT.NONE);
            GridData gd;
            GridLayout layout;
            layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.numColumns = 1;
            container.setLayout(layout);
            gd = new GridData(GridData.FILL_BOTH);
            container.setLayoutData(gd);

            Composite columns = new Composite(container, SWT.NONE);
            layout = new GridLayout();
            layout.marginHeight = 0;
            layout.marginWidth = 0;
            layout.numColumns = 2;
            columns.setLayout(layout);
            gd = new GridData(GridData.FILL_BOTH);
            columns.setLayoutData(gd);

            Composite leftColumn = new Composite(columns, SWT.NULL);
            gd = new GridData(GridData.FILL_VERTICAL | GridData.VERTICAL_ALIGN_CENTER);
            leftColumn.setLayoutData(gd);
            GridLayout leftLayout = new GridLayout();
            leftLayout.verticalSpacing = 0;
            leftLayout.marginWidth = 0;
            leftColumn.setLayout(leftLayout);

            Composite rightColumn = new Composite(columns, SWT.NULL);
            gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
            rightColumn.setLayoutData(gd);
            GridLayout rightLayout = new GridLayout();
            rightLayout.verticalSpacing = 0;
            rightLayout.marginWidth = 0;
            rightColumn.setLayout(rightLayout);

            createLeftRightButtons(leftColumn);

            Table table = new Table(rightColumn, SWT.MULTI | SWT.BORDER);
            gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
            gd.widthHint = MoveImplicitAttributesPage.this.convertWidthInCharsToPixels(30);
            table.setLayoutData(gd);
            fAttributesThatMoveViewer = new TableViewer(table);

            return container;
        }
    }

    class Status extends SpindleStatus
    {
        private Object owner;
        public Status(Object owner)
        {
            super();
            this.owner = owner;
        }

        public Object getOwner()
        {
            return owner;
        }

    }
}
