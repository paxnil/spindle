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

import java.util.Map;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IParameterSpecification;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.texteditor.AbstractTextEditor;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.resources.ClasspathRootLocation;
import com.iw.plugins.spindle.core.resources.ContextRootLocation;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.spec.BaseSpecLocatable;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.editors.spec.assist.SpecTapestryAccess;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * Open an interesting thing, if possible.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: OpenDeclarationAction.java,v 1.7.2.2 2004/06/22 12:23:01
 *          glongman Exp $
 */
public class OpenDeclarationAction extends BaseSpecAction
{
  public static final String ACTION_ID = UIPlugin.PLUGIN_ID + ".spec.openDeclaration";

  public OpenDeclarationAction()
  {
    super();
    //      TODO I10N
    setText("&Open Declaration");
    setId(ACTION_ID);
  }

  protected void doRun()
  {
    XMLNode artifact = XMLNode.getArtifactAt(fDocument, fDocumentOffset);
    String type = artifact.getType();
    if (type == ITypeConstants.TEXT || type == ITypeConstants.COMMENT
        || type == ITypeConstants.PI || type == ITypeConstants.DECL)
    {
      return;
    }
    if (type == ITypeConstants.ENDTAG)
      artifact = artifact.getCorrespondingNode();

    if (artifact == null)
      return;

    String name = artifact.getName();

    if (name == null)
      return;

    name = name.toLowerCase();

    if (name.equals("binding") || name.equals("static-binding")
        || name.equals("inherited-binding") || name.equals("message-binding")
        || name.equals("string-binding") || name.equals("field-binding"))
    {
      XMLNode parent = artifact.getParent();
      String parentName = parent.getName();
      if (parentName == null)
        return;
      parentName = parentName.toLowerCase();
      if (!parentName.equals("component"))
        return;

      handleComponentBinding(parent, artifact);
      return;
    }

    if ("component".equals(name))
      handleComponentLookup(artifact);

    else if ("application".equals(name))
      handleTypeLookup(artifact, "engine-class");

    else if ("bean".equals(name))
      handleTypeLookup(artifact, "class");

    else if ("component-specification".equals(name))
      handleTypeLookup(artifact, "class");

    else if ("page-specification".equals(name))
      handleTypeLookup(artifact, "class");

    else if ("extension".equals(name))
      handleTypeLookup(artifact, "class");

    else if ("service".equals(name))
      handleTypeLookup(artifact, "class");

    else if ("property-specification".equals(name))
      handleTypeLookup(artifact, "type");

    else if ("private-asset".equals(name))
      handlePrivateAsset(artifact);

    else if ("context-asset".equals(name))
      handleContextAsset(artifact);

    else if (SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID.equals(fDTD.getPublicId())
        && "component-alias".equals(name))
      handleRelativeLookup(artifact, "specification-path");
    else if (SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID.equals(fDTD.getPublicId())
        && "component-type".equals(name))
      handleRelativeLookup(artifact, "specification-path");

    else if ("page".equals(name))
      handleRelativeLookup(artifact, "specification-path");

    else if ("library".equals(name))
      handleLibraryLookup(artifact);

    else if ("parameter".equals(name))
    {
      if (fDTD.getPublicId() == SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID)
      {
        handleTypeLookup(artifact, "java-type");
      } else
      {
        handleTypeLookup(artifact, "type");
      }
    }

  }

  /**
   * @param artifact
   * @param string
   */
  private void handleLibraryLookup(XMLNode artifact)
  {
    XMLNode attribute = artifact.getAttributeAt(fDocumentOffset);
    if (attribute == null)
      return;

    String name = attribute.getName();

    if (name == null)
      return;

    if (!"specification-path".equals(name.toLowerCase()))
      return;

    String path = attribute.getAttributeValue();
    if (path == null)
      return;

    //here we are doing a classpath lookup,
    //need to get access to the ClasspathRoot
    IStorage storage = fEditor.getStorage();
    if (storage != null)
    {
      TapestryProject project = TapestryCore.getDefault().getTapestryProjectFor(storage);
      if (project == null)
        return;

      try
      {
        ClasspathRootLocation root = project.getClasspathRoot();
        if (root == null)
          return;

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) root
            .getRelativeLocation(path);
        IStorage s = location.getStorage();
        if (s != null)
          foundResult(s, null, null);
      } catch (CoreException e)
      {
        UIPlugin.log(e);
      }
    }
  }

  /**
   * @param artifact
   */
  private void handlePrivateAsset(XMLNode artifact)
  {
    XMLNode attribute = artifact.getAttributeAt(fDocumentOffset);
    if (attribute == null)
      return;

    String name = attribute.getName();

    if (name == null)
      return;

    if (!"resource-path".equals(name.toLowerCase()))
      return;

    String path = attribute.getAttributeValue();
    if (path == null)
      return;

    //here we are doing a classpath lookup,
    //need to get access to the ClasspathRoot
    IStorage storage = fEditor.getStorage();
    if (storage != null)
    {
      TapestryProject project = TapestryCore.getDefault().getTapestryProjectFor(storage);
      if (project == null)
        return;

      try
      {
        ClasspathRootLocation root = project.getClasspathRoot();
        if (root == null)
          return;

        IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) root
            .getRelativeLocation(path);
        IStorage s = location.getStorage();
        if (s != null)
          foundResult(s, null, null);
      } catch (CoreException e)
      {
        UIPlugin.log(e);
      }
    }

  }

  /**
   * @param artifact
   */
  private void handleContextAsset(XMLNode artifact)
  {
    XMLNode attribute = artifact.getAttributeAt(fDocumentOffset);
    if (attribute == null)
      return;

    String name = attribute.getName();

    if (name == null)
      return;

    if (!"path".equals(name.toLowerCase()))
      return;

    String path = attribute.getAttributeValue();
    if (path == null)
      return;

    //here we are doing a context lookup,
    //need to get access to the ContextRoot
    IStorage storage = fEditor.getStorage();
    if (storage != null)
    {
      TapestryProject project = TapestryCore.getDefault().getTapestryProjectFor(storage);
      if (project == null)
        return;

      ContextRootLocation contextRoot = project.getWebContextLocation();
      if (contextRoot == null)
        return;

      IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) contextRoot
          .getRelativeLocation(path);
      IStorage s = location.getStorage();
      if (s != null)
        foundResult(s, null, null);
    }
  }

  private void handleRelativeLookup(XMLNode artifact, String attrName)
  {
    XMLNode attribute = (XMLNode) artifact.getAttributesMap().get(attrName);
    if (attribute == null)
      return;

    String name = attribute.getName();

    if (name == null)
      return;

    if (!attrName.equals(name.toLowerCase()))
      return;

    String path = attribute.getAttributeValue();
    if (path == null)
      return;

    //here we are doing a relative lookup
    //need to get the location object for the Spec we are editing
    //That means it can have no error markers (parsed without error in the last
    // build)
    BaseSpecLocatable spec = (BaseSpecLocatable) fEditor.getSpecification();
    if (spec != null)
    {
      IResourceWorkspaceLocation rootLocation = (IResourceWorkspaceLocation) spec
          .getSpecificationLocation();
      if (rootLocation == null)
        return;

      IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) rootLocation
          .getRelativeLocation(path);
      IStorage s = location.getStorage();
      if (s != null)
        foundResult(s, null, null);
    }
  }

  /**
   * @param artifact
   */
  private void handleComponentLookup(XMLNode artifact)
  {

    SpecTapestryAccess access = null;
    try
    {
      access = new SpecTapestryAccess(fEditor);
    } catch (IllegalArgumentException e)
    {
      // do nothing
    }

    if (access == null)
      return;

    // first try and resolve the component...
    XMLNode attribute = artifact.getAttributeAt(fDocumentOffset);
    if (attribute == null)
      return;

    String name = attribute.getName();

    if (name == null)
      return;

    String typeName = null;
    if ("type".equals(name.toLowerCase()))
      typeName = attribute.getAttributeValue();

    if (typeName == null)
      return;

    PluginComponentSpecification spec = (PluginComponentSpecification) access
        .resolveComponentType(typeName);
    if (spec == null)
      return;

    IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec
        .getSpecificationLocation();
    if (location == null || location.getStorage() == null)
      return;

    foundResult(location.getStorage(), null, null);

  }

  private void handleComponentBinding(XMLNode parent, XMLNode binding)
  {
    try
    {
      SpecTapestryAccess access = new SpecTapestryAccess(fEditor);
      // first try and resolve the component...
      Map attrMap = parent.getAttributesMap();
      XMLNode typeAttribute = (XMLNode) attrMap.get("type");
      if (typeAttribute == null)
        return;

      String resolveType = typeAttribute.getAttributeValue();

      if (resolveType == null)
        return;

      PluginComponentSpecification spec = (PluginComponentSpecification) access
          .resolveComponentType(resolveType);
      if (spec == null)
        return;

      Map bindingAttrs = binding.getAttributesMap();
      XMLNode nameAttribute = (XMLNode) bindingAttrs.get("name");
      if (nameAttribute == null)
        return;

      String parameterName = nameAttribute.getAttributeValue();
      if (parameterName == null)
        return;

      IParameterSpecification parameterSpec = spec.getParameter(parameterName);

      IResourceWorkspaceLocation location = (IResourceWorkspaceLocation) spec
          .getSpecificationLocation();
      if (location == null || location.getStorage() == null)
        return;

      foundResult(location.getStorage(), parameterName, parameterSpec);

    } catch (IllegalArgumentException e)
    {
      // do nothing
    }

  }

  private void handleTypeLookup(XMLNode artifact, String attrName)
  {
    XMLNode attribute = artifact.getAttributeAt(fDocumentOffset);
    if (attribute == null)
      return;

    String name = attribute.getName();

    if (name == null)
      return;

    String typeName = null;
    if (attrName.equals(name.toLowerCase()))
      typeName = attribute.getAttributeValue();

    if (typeName == null)
      return;
    IType type = resolveType(typeName);

    if (type == null)
      return;

    foundResult(type, null, null);

  }

  protected void foundResult(Object result, String key, Object moreInfo)
  {
    if (result instanceof IType)
    {
      try
      {
        JavaUI.openInEditor((IType) result);
      } catch (PartInitException e)
      {
        UIPlugin.log(e);
      } catch (JavaModelException e)
      {
        UIPlugin.log(e);
      }
    } else if (result instanceof IStorage)
    {
      UIPlugin.openTapestryEditor((IStorage) result);
      IEditorPart editor = UIUtils.getEditorFor((IStorage) result);
      if (editor != null && (editor instanceof AbstractTextEditor) || moreInfo != null)
      {
        if (moreInfo instanceof IParameterSpecification && key != null)
        {
          revealParameter((AbstractTextEditor) editor, key);
        }
      }
    }
  }

  private void revealParameter(AbstractTextEditor editor, String parameterName)
  {
    IDocument document = editor
        .getDocumentProvider()
        .getDocument(editor.getEditorInput());
    //   TODO remove XMLDocumentPartitioner partitioner =
    //            new XMLDocumentPartitioner(XMLDocumentPartitioner.SCANNER,
    // XMLDocumentPartitioner.TYPES);
    try
    {
      XMLNode reveal = null;
      //  TODO remove partitioner.connect(document);
      Position[] pos = null;
      //   TODO remove pos =
      // document.getPositions(partitioner.getManagingPositionCategories()[0]);
      pos = document.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
      for (int i = 0; i < pos.length; i++)
      {
        XMLNode artifact = (XMLNode) pos[i];
        if (artifact.getType() == ITypeConstants.ENDTAG)
          continue;
        String name = artifact.getName();
        if (name == null)
          continue;

        if (!"parameter".equals(name.toLowerCase()))
          continue;

        Map attributesMap = artifact.getAttributesMap();
        XMLNode attribute = (XMLNode) attributesMap.get("name");
        if (attribute == null)
          continue;

        String value = attribute.getAttributeValue();
        if (value != null && value.equals(parameterName))
        {
          reveal = artifact;
          break;
        }
      }
      if (reveal != null)
        editor.setHighlightRange(reveal.getOffset(), reveal.getLength(), true);

    } catch (Exception e)
    {
      UIPlugin.log(e);
    }

    //  TODO remove finally
    //        {
    //            partitioner.disconnect();
    //        }
  }

}