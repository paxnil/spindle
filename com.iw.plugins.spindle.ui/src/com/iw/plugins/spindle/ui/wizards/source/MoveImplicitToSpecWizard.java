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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.editors.text.FileDocumentProvider;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginContainedComponent;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.template.assist.TemplateTapestryAccess;

/**
 *  Wizard to move in implicit (@ComponentType) declaration
 *  from a Template to a component specification.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class MoveImplicitToSpecWizard extends Wizard
{
    // info for the template operations
    private TemplateEditor fTemplateEditor;
    private XMLNode fImplicitNode;
    private List fAttributeList;

    //info for the spec operations
    private ITextEditor fTextEditor;

    //the implicit component
    private IComponentSpecification fImplicitComponent;

    private String fSimpleId;
    private String fFullType;

    private PluginComponentSpecification fBuildStateComponent;

    private MoveAttributesPage fMovePage;

    public MoveImplicitToSpecWizard(
        TemplateEditor templateEditor,
        XMLNode sourceNode,
        List sourceAttributes,
        PluginComponentSpecification buildStateComponent)
    {
        this(templateEditor, sourceNode, sourceAttributes, null, buildStateComponent);
    }

    public MoveImplicitToSpecWizard(
        TemplateEditor templateEditor,
        XMLNode sourceNode,
        List sourceAttributes,
        ITextEditor targetEditor,
        PluginComponentSpecification buildStateComponent)
    {
        super();
        setWindowTitle("Move implicit component from template to specification");
        setNeedsProgressMonitor(true);
        init(templateEditor, sourceNode, sourceAttributes, targetEditor, buildStateComponent);

    }

    private void init(
        TemplateEditor templateEditor,
        XMLNode sourceNode,
        List sourceAttributes,
        ITextEditor targetEditor,
        PluginComponentSpecification buildStateComponent)
    {
        fTemplateEditor = templateEditor;
        fImplicitNode = sourceNode;
        fAttributeList = sourceAttributes;
        fTextEditor = targetEditor;
        fBuildStateComponent = buildStateComponent;
        TemplateTapestryAccess access = new TemplateTapestryAccess(templateEditor);
        String jwcid = null;
        for (Iterator iter = sourceAttributes.iterator(); iter.hasNext();)
        {
            XMLNode node = (XMLNode) iter.next();
            if (node.getName().equals(TemplateParser.JWCID_ATTRIBUTE_NAME))
            {
                jwcid = node.getAttributeValue();
                break;
            }
        }
        access.setJwcid(jwcid);
        fSimpleId = access.getSimpleId();
        fFullType = access.getFullType();
        fImplicitComponent = access.getResolvedComponent();
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#addPages()
     */
    public void addPages()
    {
        fMovePage = new MoveAttributesPage("Fiddle with attributes");
        fMovePage.init(
            fBuildStateComponent,
            fSimpleId,
            fAttributeList,
            fBuildStateComponent.getPublicId(),
            fImplicitComponent != null ? fImplicitComponent.getParameterNames() : Collections.EMPTY_LIST);
        addPage(fMovePage);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.wizard.IWizard#performFinish()
     */
    public boolean performFinish()
    {
        try
        {
            getContainer().run(false, false, getRunnable());
        } catch (InvocationTargetException e)
        {
            UIPlugin.log(e);
        } catch (InterruptedException e)
        {
            UIPlugin.log(e);
        }
        return true;
    }
    private void doFinish()
    {
        String id = fMovePage.getTemplateComponentId().trim();
        List moving = fMovePage.getAttributesThatMove();
        List staying = fMovePage.getAttributesThatStay();

        PluginContainedComponent resultContained = createNewContainedComponent(moving);
        FileDocumentProvider provider = null;
        IDocument document = null;
        try
        {
            if (fTextEditor != null)
            {
                provider = (FileDocumentProvider) fTextEditor.getDocumentProvider();
                document = provider.getDocument(fTextEditor.getEditorInput());
            } else
            {
                IFile file =
                    (IFile) ((IResourceWorkspaceLocation) fBuildStateComponent.getSpecificationLocation()).getStorage();
                provider = UIPlugin.getDefault().getSpecFileDocumentProvider();
                provider.connect(this);
                document = provider.getDocument(new FileEditorInput(file));
            }
            int offset = findInsertOffset();

        } catch (CoreException e)
        {
            UIPlugin.log(e);
        } finally
        {
            if (fTextEditor == null)
                provider.disconnect(this);
        }
    }

    private IRunnableWithProgress getRunnable()
    {
        return new IRunnableWithProgress()
        {
            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {
                doFinish();
            }
        };
    }

    /**
     * @return
     */
    private int findInsertOffset()
    {
        List componentIds = fBuildStateComponent.getComponentIds();
        if (componentIds.isEmpty())
            return -1; //TODO handle insert new!
        List containedComponents = new ArrayList();
        for (Iterator iter = containedComponents.iterator(); iter.hasNext();)
        {
            String id = (String) iter.next();
            containedComponents.add(fBuildStateComponent.getComponent(id));
        }
        return 0;

    }

    private PluginContainedComponent createNewContainedComponent(List moving)
    {
        PluginContainedComponent component = new PluginContainedComponent();
        component.setType(fFullType);
        if (!moving.isEmpty())
        {
            for (Iterator iter = moving.iterator(); iter.hasNext();)
            {
                PluginBindingSpecification binding = new PluginBindingSpecification();
                XMLNode node = (XMLNode) iter.next();
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
                component.setBinding(name, binding);
            }
        }
        return component;
    }

    private void rewriteTemplateTag(String id, List staying)
    {
        IDocument templateDocument =
            fTemplateEditor.getDocumentProvider().getDocument(fTemplateEditor.getEditorInput());
        StringBuffer buffer = new StringBuffer("<");
        buffer.append(fImplicitNode.getName());
        buffer.append(" jwcid=\"");
        buffer.append(id);
        buffer.append("\"");
        if (!staying.isEmpty())
        {
            buffer.append(" ");
            for (Iterator iter = staying.iterator(); iter.hasNext();)
            {
                XMLNode attribute = (XMLNode) iter.next();
                buffer.append(attribute.getContent().trim());
                if (iter.hasNext())
                    buffer.append(" ");
            }
        }
        String type = fImplicitNode.getType();
        if (type == XMLDocumentPartitioner.TAG)
            buffer.append(">");
        else
            buffer.append("/>");

        try
        {
            templateDocument.replace(fImplicitNode.getOffset(), fImplicitNode.getLength(), buffer.toString());
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
        }
    }

}
