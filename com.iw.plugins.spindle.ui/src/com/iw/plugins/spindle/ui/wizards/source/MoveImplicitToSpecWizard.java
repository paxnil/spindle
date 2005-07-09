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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.ui.wizards.source;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;


import org.apache.hivemind.LocationHolder;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.IComponentSpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.PreferenceConstants;
import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginContainedComponent;
import com.iw.plugins.spindle.core.util.IndentingWriter;
import com.iw.plugins.spindle.core.util.XMLUtil;
import com.iw.plugins.spindle.editors.template.TemplateEditor;
import com.iw.plugins.spindle.editors.template.assist.TemplateTapestryAccess;

/**
 * Wizard to move in implicit (@ComponentType) declaration from a Template to a
 * component specification.
 * 
 * @author glongman@gmail.com
 */
public class MoveImplicitToSpecWizard extends Wizard
{

  private static final String FORMATTER_USE_TABS_TO_INDENT = PreferenceConstants.FORMATTER_TAB_CHAR;
  private static final String EDITOR_DISPLAY_TAB_WIDTH = PreferenceConstants.FORMATTER_TAB_SIZE;
  
  // info for formatting
  private boolean fUseTabIndent;
  private int fTabSpaces;

  // info for the template operations
  private TemplateEditor fTemplateEditor;
  private XMLNode fImplicitNode;
  private List fAttributeList;

  //info for the spec operations
  private ITextEditor fSpecEditor;

  //the implicit component
  private IComponentSpecification fImplicitComponent;

  private String fSimpleId;
  private String fFullType;

  private PluginComponentSpecification fRelatedSpec;

  private MoveImplicitAttributesPage fMovePage;
  private MoveImplicitPreviewPage fPreviewPage;

  // A document provider used iff the related spec is not
  // open in an editor
  private IDocumentProvider fSpecProvider;
  private IEditorInput fSpecFileInput;
  private IAnnotationModel fSpecAnnotationModel;

  //the original and proposed modified spec text
  private IDocument fOriginalSpecDocument;
  private IDocument fModifiedSpecDocument;

  //the original and proposed modified template text
  private IDocument fOriginalTemplateDocument;
  private IDocument fModifiedTemplateDocument;

  private ModifyDocumentCommand fSpecCommand;
  private ModifyDocumentCommand fTemplateCommand;

  public MoveImplicitToSpecWizard(TemplateEditor templateEditor, XMLNode sourceNode,
      List sourceAttributes, PluginComponentSpecification relatedSpec)
  {
    this(templateEditor, sourceNode, sourceAttributes, null, relatedSpec);
  }

  public MoveImplicitToSpecWizard(TemplateEditor templateEditor, XMLNode sourceNode,
      List sourceAttributes, ITextEditor specEditor,
      PluginComponentSpecification relatedSpec)
  {
    super();
    setWindowTitle("Move implicit component from template to specification");
    setNeedsProgressMonitor(true);
    init(templateEditor, sourceNode, sourceAttributes, specEditor, relatedSpec);

  }

  private void init(
      TemplateEditor templateEditor,
      XMLNode sourceNode,
      List sourceAttributes,
      ITextEditor targetEditor,
      PluginComponentSpecification buildStateComponent)
  {
    IPreferenceStore store = UIPlugin.getDefault().getPreferenceStore();
    fUseTabIndent = store.getBoolean(FORMATTER_USE_TABS_TO_INDENT);
    fTabSpaces = store.getInt(EDITOR_DISPLAY_TAB_WIDTH);
    fTemplateEditor = templateEditor;
    fImplicitNode = sourceNode;
    fAttributeList = sourceAttributes;
    fSpecEditor = targetEditor;
    fRelatedSpec = buildStateComponent;
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
    initializeDocuments();
  }

  private void initializeDocuments()
  {
    if (fSpecEditor != null)
    {
      fOriginalSpecDocument = fSpecEditor.getDocumentProvider().getDocument(
          fSpecEditor.getEditorInput());
    } else
    {
      fSpecProvider = UIPlugin.getDefault().getSpecFileDocumentProvider();
      try
      {
        ICoreResource location = (ICoreResource) fRelatedSpec
            .getSpecificationLocation();
        IFile file = (IFile) location.getStorage();
        fSpecFileInput = new FileEditorInput(file);
        fSpecProvider.connect(fSpecFileInput);
        fOriginalSpecDocument = fSpecProvider.getDocument(fSpecFileInput);
        fSpecAnnotationModel = fSpecProvider.getAnnotationModel(fSpecFileInput);
        fSpecAnnotationModel.connect(fOriginalSpecDocument);
      } catch (CoreException e)
      {
        UIPlugin.log(e);
      }
    }

    fOriginalTemplateDocument = fTemplateEditor.getDocumentProvider().getDocument(
        fTemplateEditor.getEditorInput());
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.wizard.IWizard#addPages()
   */
  public void addPages()
  {
    fMovePage = new MoveImplicitAttributesPage("Fiddle with attributes");
    fMovePage.init(
        fRelatedSpec,
        fSimpleId,
        fAttributeList,
        fRelatedSpec.getPublicId(),
        fImplicitComponent != null
            ? fImplicitComponent.getParameterNames() : Collections.EMPTY_LIST);
    addPage(fMovePage);

    fPreviewPage = new MoveImplicitPreviewPage("Preview", this);
    addPage(fPreviewPage);
  }

  /*
   * (non-Javadoc)
   * 
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
    } finally
    {
      cleanup();
    }
    return true;
  }

  private void cleanup()
  {
    if (fSpecFileInput != null)
    {
      fSpecProvider.disconnect(fSpecFileInput);
      fSpecAnnotationModel.disconnect(fOriginalSpecDocument);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.wizard.IWizard#performCancel()
   */
  public boolean performCancel()
  {
    cleanup();

    return true;
  }

  //    /* (non-Javadoc)
  //     * @see
  // org.eclipse.jface.wizard.IWizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
  //     */
  //    public IWizardPage getNextPage(IWizardPage page)
  //    {
  //        IWizardPage nextPage = super.getNextPage(page);
  //        if (nextPage != null && nextPage == fPreviewPage)
  //        {
  //            performModifications();
  //            fPreviewPage.refresh();
  //        }
  //        return nextPage;
  //    }

  /**
   * called when moving from the preview back to the first page. clean up any
   * mods.
   */
  public void clearModifications()
  {
    fModifiedSpecDocument = null;
    fSpecCommand = null;
    fModifiedTemplateDocument = null;
    fTemplateCommand = null;
  }

  /**
   * called when moving from the first page to the preview page. Generates the
   * modified documents for preview.
   */
  void performModifications()
  {
    String id = fMovePage.getTemplateComponentId().trim();
    List moving = fMovePage.getAttributesThatMove();
    List staying = fMovePage.getAttributesThatStay();
    fSpecCommand = computeSpecModification(id, moving);

    fModifiedSpecDocument = new Document();
    String unmodified = fOriginalSpecDocument.get();
    StringWriter writer = new StringWriter();
    fModifiedSpecDocument.set(unmodified);
    try
    {
      fSpecCommand.execute(fModifiedSpecDocument);
      fModifiedSpecDocument.set(fModifiedSpecDocument.get());
    } catch (BadLocationException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
      UIPlugin.log(e);
    }

    fTemplateCommand = computeTemplateModification(id, staying);
    fModifiedTemplateDocument = new Document();
    fModifiedTemplateDocument.set(fOriginalTemplateDocument.get());
    try
    {
      fTemplateCommand.execute(fModifiedTemplateDocument);
      fModifiedTemplateDocument.set(fModifiedTemplateDocument.get());
    } catch (BadLocationException e1)
    {
      // TODO Auto-generated catch block
      e1.printStackTrace();
      UIPlugin.log(e1);
    }
  }

  private void doFinish(IProgressMonitor monitor)
  {
    if (fModifiedSpecDocument == null)
      performModifications();
    try
    {
      fSpecCommand.execute(fOriginalSpecDocument);
      if (fSpecEditor != null)
      {
        fSpecEditor.doSave(monitor);
      } else
      {
        fSpecProvider.saveDocument(monitor, fSpecFileInput, fOriginalSpecDocument, true);
      }
      fTemplateCommand.execute(fOriginalTemplateDocument);
    } catch (CoreException e)
    {
      UIPlugin.log(e);
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    } finally
    {
      if (fSpecProvider != null)
        fSpecProvider.disconnect(fSpecFileInput);
    }
  }

  private IRunnableWithProgress getRunnable()
  {
    return new IRunnableWithProgress()
    {
      public void run(IProgressMonitor monitor) throws InvocationTargetException,
          InterruptedException
      {
        doFinish(monitor);
      }
    };
  }

  /**
   * Find the place to insert a new ContainedComponent.
   * <p>
   * In general, the new contained component is inserted after the last
   * occurance of 'something'. If there are no 'somethings' in the spec, then we
   * want to insert the new contained component as the first child of the root
   * node.
   * <p>
   * The 'somethings' (in order of search)...
   * <ul>
   * <li>Already existing Contained Components</li>
   * <li>PropertySpecifications</li>
   * <li>Property Declarations</li>
   * <li>Parameter Specifications (won't be any if this component is a page!)
   * </li>
   * <li>Descriptions</li>
   * </ul>
   * 
   * @return
   */
  private ModifyDocumentCommand computeSpecModification(
      String componentId,
      List templateAttributesThatMove)
  {
    PluginContainedComponent newContainedComponent = createNewContainedComponent(templateAttributesThatMove);

    LocationHolder found = fRelatedSpec;
    //check for existing ContainedComponents
    List componentIds = fRelatedSpec.getComponentIds();

    if (!componentIds.isEmpty())
    {
      found = (LocationHolder) fRelatedSpec.getComponent((String) componentIds
          .get(componentIds.size() - 1));
    } else
    {
      List propertySpecs = fRelatedSpec.getPropertySpecificationNames();
      if (!propertySpecs.isEmpty())
      {
        found = (LocationHolder) fRelatedSpec
            .getPropertySpecification((String) propertySpecs
                .get(propertySpecs.size() - 1));
      } else
      {
        List propertyDecls = fRelatedSpec.getPropertyDeclarations();
        if (!propertyDecls.isEmpty())
        {
          found = (LocationHolder) propertyDecls.get(propertyDecls.size() - 1);
        } else
        {
          List parameterDecls = fRelatedSpec.getParameterNames();
          if (!parameterDecls.isEmpty())
          {
            found = (LocationHolder) fRelatedSpec.getParameter((String) parameterDecls
                .get(parameterDecls.size() - 1));
          } else
          {
            List descriptionDecls = fRelatedSpec.getDescriptionDeclarations();
            if (!descriptionDecls.isEmpty())
            {
              found = (LocationHolder) descriptionDecls.get(descriptionDecls.size() - 1);
            }
          }
        }
      }
    }

    ModifyDocumentCommand result = new ModifyDocumentCommand();
    ISourceLocationInfo sourceInfo = (ISourceLocationInfo) found.getLocation();
    boolean emptyTag = sourceInfo.isEmptyTag();
    int initialIndent = 0;
    ISourceLocation startLocation = sourceInfo.getStartTagSourceLocation();
    result.offset = startLocation.getCharEnd() + 1;
    result.length = 0;

    if (found != fRelatedSpec)
    {
      initialIndent = getIndent(fOriginalSpecDocument, startLocation.getCharStart());
      result.text = computeNewContainedComponentString(
          initialIndent,
          0,
          componentId,
          newContainedComponent);
      if (!emptyTag)
      {
        ISourceLocation endLocation = sourceInfo.getEndTagSourceLocation();
        result.offset = endLocation.getCharEnd() + 1;
      }

    } else
    {
      //here is the case where we want to insert the new ContainedComponent
      // right after
      // the root node.
      if (emptyTag)
      {
        //here we have to rewrite the root tag as a non empty tag!
        result.offset = startLocation.getCharStart();
        result.length = fOriginalSpecDocument.getLength() - result.offset;
        PluginComponentSpecification rewriteSpec = new PluginComponentSpecification(
            fRelatedSpec);
        rewriteSpec.addComponent(componentId, newContainedComponent);
        result.text = rewiteRootTag(rewriteSpec);
      } else
      {
        //no rewrite, we just need to insert.
        result.text = computeNewContainedComponentString(
            initialIndent,
            1,
            componentId,
            newContainedComponent);
      }
    }
    return result;
  }

  private String computeNewContainedComponentString(
      int initialIndent,
      int indent,
      String id,
      PluginContainedComponent newComponent)
  {
    StringWriter swriter = new StringWriter();
    String lineDelimiter = getLineDelimiter(fOriginalSpecDocument);
    swriter.write(lineDelimiter);
    swriter.write(lineDelimiter);
    XMLUtil.writeContainedComponent(newComponent, id, new IndentingWriter(
        swriter,
        true,
        fUseTabIndent,
        fTabSpaces,
        initialIndent,
        lineDelimiter), indent, fRelatedSpec.getPublicId(), false);
    return swriter.toString();
  }

  private String rewiteRootTag(PluginComponentSpecification rewriteComponent)
  {
    StringWriter swriter = new StringWriter();
    String lineDelimiter = getLineDelimiter(fOriginalSpecDocument);
    XMLUtil.writeComponentSpecification(new IndentingWriter(
        swriter,
        true,
        fUseTabIndent,
        fTabSpaces,
        0,
        lineDelimiter), rewriteComponent, 0, false);
    return swriter.toString();
  }

  /**
   * Returns the indentation of the line of the given offset.
   * 
   * @param document the document
   * @param offset the offset
   * @return the indentation of the line of the offset
   */
  private int getIndent(IDocument document, int offset)
  {
    try
    {
      int start = document.getLineOfOffset(offset);
      start = document.getLineOffset(start);

      int count = 0;
      for (int i = start; i < document.getLength(); ++i)
      {
        char c = document.getChar(i);
        if ('\t' == c)
          count += fTabSpaces;
        else if (' ' == c)
          count++;
        else
          break;
      }
      return count;
    } catch (BadLocationException x)
    {
      return 0;
    }
  }

  /**
   * Embodies the policy which line delimiter to use when inserting into a
   * document. <br>
   * <em>Copied from org.eclipse.jdt.internal.corext.codemanipulation.StubUtility</em>
   */
  private String getLineDelimiter(IDocument document)
  {
    String lineDelim = null;
    try
    {
      lineDelim = document.getLineDelimiter(0);
    } catch (BadLocationException e)
    {}
    if (lineDelim == null)
    {
      String systemDelimiter = System.getProperty("line.separator", "\n");
      String[] lineDelims = document.getLegalLineDelimiters();
      for (int i = 0; i < lineDelims.length; i++)
      {
        if (lineDelims[i].equals(systemDelimiter))
        {
          lineDelim = systemDelimiter;
          break;
        }
      }
      if (lineDelim == null)
      {
        lineDelim = lineDelims.length > 0 ? lineDelims[0] : systemDelimiter;
      }
    }
    return lineDelim;
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

  private ModifyDocumentCommand computeTemplateModification(String id, List staying)
  {
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
    if (type == ITypeConstants.TAG)
      buffer.append(">");
    else
      buffer.append("/>");

    ModifyDocumentCommand result = new ModifyDocumentCommand();
    result.offset = fImplicitNode.getOffset();
    result.length = fImplicitNode.getLength();
    result.text = buffer.toString();

    return result;
  }

  /**
   * @return the modified XML spec document (may be null)
   */
  public IDocument getModifiedSpecDocument()
  {
    return fModifiedSpecDocument;
  }

  /**
   * @return the modified template document (may be null)
   */
  public IDocument getModifiedTemplateDocument()
  {
    return fModifiedTemplateDocument;
  }

  /**
   * @return the original (unmodified) XML spec document
   */
  public IDocument getOriginalSpecDocument()
  {
    return fOriginalSpecDocument;
  }

  /**
   * @return the original (unmodified) XML spec document
   */
  public IDocument getOriginalTemplateDocument()
  {
    return fOriginalTemplateDocument;
  }

  public IStorage getTemplateStorage()
  {
    return fTemplateEditor.getStorage();
  }

  public IStorage getSpecStorage()
  {
    if (fSpecEditor != null)
    {
      return (IStorage) fSpecEditor.getEditorInput().getAdapter(IFile.class);
    } else
    {
      return (IStorage) fSpecFileInput.getAdapter(IFile.class);
    }
  }

  class ModifyDocumentCommand
  {
    int offset;
    int length;
    String text;

    void execute(IDocument document) throws BadLocationException
    {

      document.replace(offset, length, text);
    }
  }

}