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

package com.iw.plugins.spindle.core.scanning;

import org.apache.tapestry.INamespace;
import org.apache.tapestry.engine.ITemplateSource;
import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.AssetType;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.BindingType;
import org.apache.tapestry.spec.Direction;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IListenerBindingSpecification;
import org.eclipse.jdt.core.IType;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.spec.IPluginPropertyHolder;
import com.iw.plugins.spindle.core.spec.PluginAssetSpecification;
import com.iw.plugins.spindle.core.spec.PluginBeanSpecification;
import com.iw.plugins.spindle.core.spec.PluginBindingSpecification;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.spec.PluginContainedComponent;
import com.iw.plugins.spindle.core.spec.PluginDescriptionDeclaration;
import com.iw.plugins.spindle.core.spec.PluginParameterSpecification;
import com.iw.plugins.spindle.core.spec.PluginPropertySpecification;
import com.iw.plugins.spindle.core.spec.PluginReservedParameterDeclaration;
import com.iw.plugins.spindle.core.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.core.spec.bean.PluginMessageBeanInitializer;
import com.iw.plugins.spindle.core.util.XMLUtil;

/**
 * Scanner that turns a node tree into a IComponentSpecification
 * 
 * @author glongman@gmail.com
 *  
 */
public class ComponentScanner extends SpecificationScanner
{
  boolean fIsPageSpec;
  boolean fSeenTemplateAsset;
  protected INamespace fNamespace;

  /*
   * Don't need to throw an exception or add a problem here, the Parser will
   * already have caught this
   * 
   * @see com.iw.plugins.spindle.core.scanning.AbstractScanner#doScan(
   */
  protected Object beforeScan(Object source) throws ScannerException
  {
    if (super.beforeScan(source) == null)
      return null;

    String extension = fStorage.getFullPath().getFileExtension();
    if (!extension.equals("jwc") && !extension.equals("page"))
      return null;

    fIsPageSpec = extension.equals("page");
    fSeenTemplateAsset = false;
    return fSpecificationFactory.createComponentSpecification();
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.processing.AbstractScanner#doScan(org.w3c.dom.Node)
   */
  protected void doScan(Object source, Object resultObject) throws ScannerException
  {
    super.doScan(source, resultObject);
    IComponentSpecification specification = (IComponentSpecification) resultObject;

    specification.setPublicId(fPublicId);
    specification.setSpecificationLocation(fResourceLocation);
    specification.setPageSpecification(fIsPageSpec);
    specification.setLocation(getSourceLocationInfo(fRootNode));
    ((PluginComponentSpecification) specification).setNamespace(fNamespace);

    // Only components specify these two attributes.

    specification.setAllowBody(getBooleanAttribute(fRootNode, "allow-body"));
    specification.setAllowInformalParameters(getBooleanAttribute(
        fRootNode,
        "allow-informal-parameters"));

    scanComponentSpecification(fRootNode, specification, fIsPageSpec);
  }

  public void setNamespace(INamespace namespace)
  {
    fNamespace = namespace;
  }

  protected void scanAsset(
      IComponentSpecification specification,
      Node node,
      AssetType type,
      String attributeName) throws ScannerException
  {
    String name = getAttribute(node, "name", false);

    String value = getAttribute(node, attributeName);

    // not revalidatable - error state would only change if the file changed!
    if (specification.getAsset(name) != null)
    {
      addProblem(IProblem.ERROR, getAttributeSourceLocation(node, "name"), TapestryCore
          .getTapestryString("ComponentSpecification.duplicate-asset", specification
              .getSpecificationLocation()
              .getName(), name), false);
    }

    // not revalidatable - error state would only change if the file changed!
    if (name != null && !name.equals(ITemplateSource.TEMPLATE_ASSET_NAME))
      validatePattern(
          name,
          SpecificationParser.ASSET_NAME_PATTERN,
          "SpecificationParser.invalid-asset-name",
          IProblem.ERROR,
          getAttributeSourceLocation(node, "name"));

    PluginAssetSpecification asset = (PluginAssetSpecification) fSpecificationFactory
        .createAssetSpecification();

    asset.setType(type);
    asset.setPath(value);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(specification.getSpecificationLocation());
    asset.setLocation(location);

    specification.addAsset(name, asset);

    asset.validate(specification, fValidator);

    scanPropertiesInNode(asset, node);

  }

  /**
   * @since 1.0.4
   *  
   */

  protected void scanBean(IComponentSpecification specification, Node node) throws ScannerException
  {
    String name = getAttribute(node, "name", true);

    // not revalidatable - error state would only change if the file changed!
    if (specification.getBeanSpecification(name) != null)
    {
      addProblem(IProblem.ERROR, getAttributeSourceLocation(node, "name"), TapestryCore
          .getTapestryString("ComponentSpecification.duplicate-bean", specification
              .getSpecificationLocation()
              .getName(), name), false);
    }

    // not revalidatable - error state would only change if the file changed!
    validatePattern(
        name,
        SpecificationParser.BEAN_NAME_PATTERN,
        "SpecificationParser.invalid-bean-name",
        IProblem.ERROR,
        getAttributeSourceLocation(node, "name"));

    // className is revalidatable (see below)
    String className = getAttribute(node, "class", false);

    String lifecycleString = getAttribute(node, "lifecycle");

    BeanLifecycle lifecycle = (BeanLifecycle) SpecificationScanner.TYPE_CONVERSION_MAP
        .get(lifecycleString);

    PluginBeanSpecification bspec = (PluginBeanSpecification) fSpecificationFactory
        .createBeanSpecification();

    bspec.setClassName(className);
    bspec.setLifecycle(lifecycle);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(specification.getSpecificationLocation());
    bspec.setLocation(location);

    specification.addBeanSpecification(name, bspec);

   

    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
    {
      if (isElement(child, "description"))
      {
        bspec.setDescription(getValue(child));
        //TODO record description declaration
        continue;
      }

      if (isElement(child, "property"))
      {
        scanProperty((IPluginPropertyHolder) bspec, child);
        continue;
      }

      if (isElement(child, "set-property"))
      {
        scanSetProperty(bspec, child);
        continue;
      }

      if (isElement(child, "set-string-property"))
      {
        scanSetMessageProperty(bspec, child);
        continue;
      }

      if (isElement(child, "set-message-property"))
      {
        scanSetMessageProperty(bspec, child);
        continue;
      }
    }
    
    bspec.validate(specification, fValidator);
  }

  protected void scanBinding(
      IContainedComponent component,
      Node node,
      BindingType type,
      String attributeName) throws ScannerException
  {
    String name = getAttribute(node, "name", true, true);

    if (name != null && component.getBinding(name) != null)
    {
      addProblem(
          IProblem.ERROR,
          getAttributeSourceLocation(node, "name"),
          "duplicate binding name '" + name + "'",
          false);
      // TODO I18N
    }

    if (name == null)
      name = "";

    String value = null;
    boolean fromAttribute = true;

    if (type == BindingType.INHERITED || type == BindingType.STRING)
    {
      value = getAttribute(node, attributeName);
    } else
    {
      // must be done here - never revalidatable
      try
      {
        ExtendedAttributeResult result = getExtendedAttribute(node, attributeName, true);
        value = result.value;
        fromAttribute = result.fromAttribute;

      } catch (ScannerException e)
      {
        int severity = IProblem.ERROR;

        if (type == BindingType.STATIC)
          severity = IProblem.WARNING;

        addProblem(IProblem.WARNING, e.getLocation(), e.getMessage(), false);

        value = getNextDummyString();
      }
    }

    PluginBindingSpecification binding = (PluginBindingSpecification) fSpecificationFactory
        .createBindingSpecification();

    binding.setType(type);
    binding.setValue(value);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(component.getLocation().getResourceLocation());
    binding.setLocation(location);

    // no point in making revalidatable - error state would only change if the
    // file changed!
    if (type == BindingType.DYNAMIC)
    {
      ISourceLocation src = fromAttribute ? getAttributeSourceLocation(
          node,
          attributeName) : getBestGuessSourceLocation(node, true);
      validateExpression(value, IProblem.ERROR, src);
    }

    component.setBinding(name, binding);
  }

  protected void scanComponent(IComponentSpecification specification, Node node) throws ScannerException
  {
    String id = getAttribute(node, "id", false);

    ISourceLocation idLoc = getAttributeSourceLocation(node, "id");

    // not revalidatable - error state would only change if the file changed!
    if (specification.getComponent(id) != null)
    {
      addProblem(IProblem.ERROR, idLoc, TapestryCore.getTapestryString(
          "ComponentSpecification.duplicate-component",
          specification.getSpecificationLocation().getName(),
          id), false);
    }

    // not revalidatable - error state would only change if the file changed!
    validatePattern(
        id,
        SpecificationParser.COMPONENT_ID_PATTERN,
        "SpecificationParser.invalid-component-id",
        IProblem.ERROR,
        idLoc);

    String type = getAttribute(node, "type");
    String copyOf = getAttribute(node, "copy-of");

    // all below is not revalidatable - error state would only change if the
    // file changed!
    if (type == null && copyOf == null)
    {
      addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), TapestryCore
          .getTapestryString("SpecificationParser.missing-type-or-copy-of", id), false);

    } else if (type != null && copyOf != null)
    {
      addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), TapestryCore
          .getTapestryString("SpecificationParser.both-type-and-copy-of", id), false);
    } else if (copyOf != null)
    {
      IContainedComponent parentComponent = specification.getComponent(copyOf);
      if (parentComponent == null)
      {
        addProblem(
            IProblem.ERROR,
            getAttributeSourceLocation(node, "copy-of"),
            TapestryCore.getTapestryString("SpecificationParser.unable-to-copy", copyOf),
            false);
      }
    } else
    {
      validatePattern(
          type,
          SpecificationParser.COMPONENT_TYPE_PATTERN,
          "SpecificationParser.invalid-component-type",
          IProblem.ERROR,
          getAttributeSourceLocation(node, "type"));
    }

    PluginContainedComponent c = (PluginContainedComponent) fSpecificationFactory
        .createContainedComponent();
    c.setType(type);
    c.setCopyOf(copyOf);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(specification.getSpecificationLocation());
    c.setLocation(location);

    for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
    {
      if (isElement(child, "binding"))
      {
        scanBinding(c, child, BindingType.DYNAMIC, "expression");
        continue;
      }

      // Field binding is in 1.3 DTD, but removed from 1.4

      if (isElement(child, "field-binding"))
      {
        if (XMLUtil.getDTDVersion(fPublicId) < XMLUtil.DTD_3_0)
        {
          scanBinding(c, child, BindingType.FIELD, "field-name");
        } else
        {
          addProblem(
              IProblem.ERROR,
              getNodeStartSourceLocation(child),
              "field-binding not supported in DTD 3.0 and up.",
              false);
        }
        continue;
      }

      if (isElement(child, "listener-binding"))
      {
        scanListenerBinding(c, child);
        continue;
      }

      if (isElement(child, "inherited-binding"))
      {
        scanBinding(c, child, BindingType.INHERITED, "parameter-name");
        continue;
      }

      if (isElement(child, "static-binding"))
      {
        scanBinding(c, child, BindingType.STATIC, "value");
        continue;
      }

      // <string-binding> added in release 2.0.4

      if (isElement(child, "string-binding"))
      {
        scanBinding(c, child, BindingType.STRING, "key");
        continue;
      }

      if (isElement(child, "message-binding"))
      {
        scanBinding(c, child, BindingType.STRING, "key");
        continue;
      }

      if (isElement(child, "property"))
      {
        scanProperty((IPluginPropertyHolder) c, child);
        continue;
      }
    }

    specification.addComponent(id, c);

    // the revalidatable stuff
    c.validate(specification, fValidator);

  }

  protected void scanComponentSpecification(
      Node rootNode,
      IComponentSpecification specification,
      boolean isPage) throws ScannerException
  {

    //must be done here!
    String rootName = rootNode.getNodeName();
    if (isPage)
    {
      // not revalidatable - error state would only change if the file changed!
      if (!rootName.equals("page-specification"))
      {
        throw new ScannerException(TapestryCore.getTapestryString(
            "AbstractDocumentParser.incorrect-document-type",
            "page-specification",
            rootName), getBestGuessSourceLocation(rootNode, false), false);
//        addProblem(
//            IProblem.ERROR,
//            getBestGuessSourceLocation(rootNode, false),
//            TapestryCore.getTapestryString(
//                "AbstractDocumentParser.incorrect-document-type",
//                "page-specification",
//                rootName),
//            false);
//        return;
      }
      // not revalidatable - error state would only change if the file changed!
    } else if (!rootName.equals("component-specification"))
    {
      throw new ScannerException(TapestryCore.getTapestryString(
          "AbstractDocumentParser.incorrect-document-type",
          "component-specification",
          rootName), getBestGuessSourceLocation(rootNode, false), false);
//      addProblem(
//          IProblem.ERROR,
//          getBestGuessSourceLocation(rootNode, false),
//          TapestryCore.getTapestryString(
//              "AbstractDocumentParser.incorrect-document-type",
//              "component-specification",
//              rootName),
//          false);
//      return;
    }
    String componentClassname = getAttribute(rootNode, "class");

    PluginComponentSpecification pluginSpec = (PluginComponentSpecification) specification;

    if (componentClassname == null)
    {
      if (fIsPageSpec)
      {

        specification.setComponentClassName("org.apache.tapestry.html.BasePage");
      } else
      {
        specification.setComponentClassName("org.apache.tapestry.BaseComponent");
      }

    } else
    {
      specification.setComponentClassName(componentClassname);
    }

    pluginSpec.validateSelf(fValidator);

    for (Node node = rootNode.getFirstChild(); node != null; node = node.getNextSibling())
    {
      if (isElement(node, "parameter"))
      {
        try
        {
          scanParameter(specification, node);
        } catch (Exception e)
        {
          e.printStackTrace();
        }
        continue;
      }

      if (isElement(node, "reserved-parameter"))
      {
        if (isPage)
        {
          // not revalidatable - error state would only change if the file
          // changed!
          addProblem(IProblem.ERROR, getNodeStartSourceLocation(node), TapestryCore
              .getTapestryString(
                  "SpecificationParser.not-allowed-for-page",
                  "reserved-parameter"), false);
        } else
        {
          scanReservedParameter(specification, node);
        }
        continue;
      }

      if (isElement(node, "bean"))
      {
        scanBean(specification, node);
        continue;
      }

      if (isElement(node, "component"))
      {
        scanComponent(specification, node);
        continue;
      }

      if (isElement(node, "external-asset"))
      {
        scanAsset(specification, node, AssetType.EXTERNAL, "URL");
        continue;
      }

      if (isElement(node, "context-asset"))
      {
        scanAsset(specification, node, AssetType.CONTEXT, "path");
        continue;
      }

      if (isElement(node, "private-asset"))
      {
        scanAsset(specification, node, AssetType.PRIVATE, "resource-path");
        continue;
      }

      if (isElement(node, "property"))
      {
        scanProperty((IPluginPropertyHolder) specification, node);
        continue;
      }

      if (isElement(node, "description"))
      {
        String value = getValue(node);
        specification.setDescription(value);
        PluginDescriptionDeclaration declaration = new PluginDescriptionDeclaration(
            null,
            value,
            getSourceLocationInfo(node));
        ((PluginComponentSpecification) specification)
            .addDescriptionDeclaration(declaration);
        continue;
      }

      if (isElement(node, "property-specification"))
      {
        scanPropertySpecification(specification, node);
        continue;
      }
    }
  }

  protected void scanListenerBinding(IContainedComponent component, Node node) throws ScannerException
  {
    String name = getAttribute(node, "name", true);

    String language = getAttribute(node, "language");

    // The script itself is the character data wrapped by the element.

    String script = getValue(node);

    IListenerBindingSpecification binding = fSpecificationFactory
        .createListenerBindingSpecification();

    component.setBinding(name, binding);
    binding.setType(BindingType.LISTENER);
    binding.setLanguage(language);
    binding.setValue(script);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(component.getLocation().getResourceLocation());
    binding.setLocation(location);

    component.setBinding(name, binding);
  }

  protected void scanParameter(IComponentSpecification specification, Node node) throws ScannerException
  {

    String name = getAttribute(node, "name", true);

    // not revalidatable - error state would only change if the file changed!
    if (specification.getParameter(name) != null)
    {
      addProblem(IProblem.ERROR, getAttributeSourceLocation(node, "name"), TapestryCore
          .getTapestryString("ComponentSpecification.duplicate-parameter", specification
              .getSpecificationLocation()
              .getName(), name), false);
    }

    PluginParameterSpecification param = (PluginParameterSpecification) fSpecificationFactory
        .createParameterSpecification();

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(specification.getSpecificationLocation());
    param.setLocation(location);

    // not revalidatable - error state would only change if the file changed!
    validatePattern(
        name,
        SpecificationParser.PARAMETER_NAME_PATTERN,
        "SpecificationParser.invalid-parameter-name",
        IProblem.ERROR,
        getAttributeSourceLocation(node, "name"));

    String typeAttr = "type";
    int DTDVersion = XMLUtil.getDTDVersion(specification.getPublicId());
    switch (DTDVersion)
    {
      case XMLUtil.DTD_1_3 :
        typeAttr = "java-type";
        break;

      case XMLUtil.DTD_3_0 :

        break;
    }

    String type = getAttribute(node, typeAttr);

    if (type == null)
      type = "java.lang.Object";

    param.setType(type);

    param.setRequired(getBooleanAttribute(node, "required"));

    //new rule in Tapestry 3.0-beta4
    //if a parameter is required, its not allowed to have a default value.
    // BTW default values are only allowed in DTD 3.0
    if (DTDVersion >= XMLUtil.DTD_3_0)
    {
      String defaultValue = getAttribute(node, "default-value");
      param.setDefaultValue(defaultValue);

      // not revalidatable - error state would only change if the file changed!
      if (param.isRequired() && defaultValue != null)
      {
        addProblem(
            IProblem.ERROR,
            getAttributeSourceLocation(node, "default-value"),
            TapestryCore
                .getTapestryString(
                    "EstablishDefaultParameterValuesVisitor.parameter-must-have-no-default-value",
                    specification.getSpecificationLocation().getName(),
                    name),
            false);
      }

    }

    String propertyName = getAttribute(node, "property-name");

    // If not specified, use the name of the parameter.

    if (TapestryCore.isNull(propertyName))
    {
      propertyName = name;

    } else
    {
      // not revalidatable - error state would only change if the file changed!
      validatePattern(
          propertyName,
          SpecificationParser.PROPERTY_NAME_PATTERN,
          "SpecificationParser.invalid-property-name",
          IProblem.ERROR,
          getAttributeSourceLocation(node, name));
    }

    param.setPropertyName(propertyName);

    String direction = getAttribute(node, "direction");

    if (direction != null)
      param.setDirection((Direction) TYPE_CONVERSION_MAP.get(direction));

    specification.addParameter(name, param);

    param.validate(specification, fValidator);

    Node child = node.getFirstChild();
    if (child != null && isElement(child, "description"))
      param.setDescription(getValue(child));

  }

  /** @since 2.4 * */

  protected void scanPropertySpecification(IComponentSpecification spec, Node node) throws ScannerException
  {

    String name = getAttribute(node, "name", true);

    if (spec.getPropertySpecification(name) != null)
    {
      //   not revalidatable - error state would only change if the file changed!
      addProblem(IProblem.ERROR, getAttributeSourceLocation(node, "name"), TapestryCore
          .getTapestryString(
              "ComponentSpecification.duplicate-property-specification",
              spec.getSpecificationLocation().getName(),
              name), false);
    }

    PluginPropertySpecification ps = (PluginPropertySpecification) fSpecificationFactory
        .createPropertySpecification();

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(spec.getSpecificationLocation());
    ps.setLocation(location);

    //   not revalidatable - error state would only change if the file changed!
    validatePattern(
        name,
        SpecificationParser.PROPERTY_NAME_PATTERN,
        "SpecificationParser.invalid-property-name",
        IProblem.ERROR,
        getAttributeSourceLocation(node, "name"));

    ps.setName(name);

    String type = null;

    type = getAttribute(node, "type");

    if (type == null || type.trim().length() == 0)
      type = "java.lang.Object";

    ps.setType(type);

    boolean persistent = getBooleanAttribute(node, "persistent");

    ps.setPersistent(persistent);

    //   must be done now - not revalidatable
    ExtendedAttributeResult result = null;
    String initialValue = null;
    try
    {
      result = getExtendedAttribute(node, "initial-value", false);
      initialValue = result.value;
    } catch (ScannerException e)
    {
      addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false);
    }

    //   not revalidatable - error state would only change if the file changed!
    if (initialValue != null)
    {
      validateExpression(initialValue, IProblem.ERROR, result.fromAttribute
          ? getAttributeSourceLocation(node, "initial-value")
          : getBestGuessSourceLocation(node, true));
    }

    ps.setInitialValue(initialValue);

    spec.addPropertySpecification(ps);

    ps.validate(spec, fValidator);

  }

  /**
   * @since 1.0.5
   *  
   */

  protected void scanReservedParameter(IComponentSpecification spec, Node node)
  {
    String name = getAttribute(node, "name", false);

    //   not revalidatable - error state would only change if the file changed!
    if (name != null && spec.isReservedParameterName(name))
    {
      addProblem(
          IProblem.ERROR,
          getAttributeSourceLocation(node, "name"),
          "duplicate reserved parameter name: " + name,
          false);
    }
    PluginReservedParameterDeclaration declaration = new PluginReservedParameterDeclaration(
        name,
        getSourceLocationInfo(node));

    ((PluginComponentSpecification) spec).addReservedParameterDeclaration(declaration);

    spec.addReservedParameterName(name);
  }

  /**
   * This is a new simplified version structured around OGNL, in the 1.3 DTD.
   * 
   * @since 2.2
   *  
   */

  protected void scanSetProperty(IBeanSpecification spec, Node node) throws ScannerException
  {
    String name = getAttribute(node, "name", true);

    //   not revalidatable - error state would only change if the file changed!
    checkPropertyName(node, name);

    //   must be done now - not revalidatable
    ExtendedAttributeResult result = null;
    String expression = null;
    try
    {
      result = getExtendedAttribute(node, "expression", true);
      expression = result.value;
    } catch (ScannerException e)
    {
      addProblem(IProblem.ERROR, e.getLocation(), e.getMessage(), false);
    }

    //   not revalidatable - error state would only change if the file changed!
    if (expression != null)
    {
      validateExpression(expression, IProblem.ERROR, result.fromAttribute
          ? getAttributeSourceLocation(node, "expression") : getBestGuessSourceLocation(
              node,
              true));
    }

    PluginExpressionBeanInitializer iz = (PluginExpressionBeanInitializer) fSpecificationFactory
        .createExpressionBeanInitializer();

    iz.setPropertyName(name);
    iz.setExpression(expression);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(spec.getLocation().getResourceLocation());
    iz.setLocation(location);

    spec.addInitializer(iz);

  }

  /**
   * String properties in the 1.3 DTD are handled a little differently.
   * 
   * @since 2.2
   *  
   */

  protected void scanSetMessageProperty(IBeanSpecification spec, Node node) throws ScannerException
  {
    String name = getAttribute(node, "name");
    String key = getAttribute(node, "key");

    //   not revalidatable - error state would only change if the file changed!
    checkPropertyName(node, name);

    //   not revalidatable - error state would only change if the file changed!
    if (key != null)
    {
      if (key.trim().length() == 0)
      {
        addProblem(
            IProblem.ERROR,
            getAttributeSourceLocation(node, "key"),
            "key must not be empty",
            false);
      }
    }

    PluginMessageBeanInitializer iz = (PluginMessageBeanInitializer) fSpecificationFactory
        .createMessageBeanInitializer();

    iz.setPropertyName(name);
    iz.setKey(key);

    ISourceLocationInfo location = getSourceLocationInfo(node);
    location.setResourceLocation(spec.getLocation().getResourceLocation());
    iz.setLocation(location);

    spec.addInitializer(iz);

  }

  private void checkPropertyName(Node node, String name) throws ScannerException
  {
    if (name == null || name.trim().length() == 0)
    {
      addProblem(
          IProblem.ERROR,
          getAttributeSourceLocation(node, "name"),
          TapestryCore.getTapestryString(
              "SpecificationParser.invalid-property-name",
              "not specified"),
          false);
    } else
    {
      validatePattern(
          name,
          SpecificationParser.PROPERTY_NAME_PATTERN,
          "SpecificationParser.invalid-property-name",
          IProblem.ERROR,
          getAttributeSourceLocation(node, "name"));
    }
  }

  public IType validateTypeSpecial(
      IResourceWorkspaceLocation dependant,
      String typeName,
      int severity,
      ISourceLocation location) throws ScannerException
  {
    String useName = typeName;
    if (useName.indexOf(".") < 0)
      useName = "java.lang." + useName;

    return validateTypeName(dependant, useName, severity, location);

  }

}