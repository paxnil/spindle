package com.iw.plugins.spindle.parser.filters;

import java.util.HashMap;
import java.util.Map;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.ITemplateSource;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.util.xml.InvalidStringException;
import ognl.ExpressionSyntaxException;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XNIException;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.parser.TapestryErrorReporter;
import com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo;
import com.iw.plugins.spindle.spec.XMLUtil;

/**
 * Performs some extra validation that is Tapestry specific.
 * Note that not all elements defined as constants need this.
 * All are defined and will be used as needed
 */

public class TapestryValidator extends ErrorReportingFilter {

  /** find the augementation info */
  protected static final String AUGMENTATIONS =
    "http://intelligentworks.com/xml/features/augmentations-location";

  /** validators for DTD versions */
  private static TValidator[] availableValidators;

  /** elements we expect to see (all DTD versions) */
  public static final String APPLICATION_SPEC = "application";
  public static final String BEAN = "bean";
  public static final String BINDING = "binding";
  public static final String CONFIGURE = "configure";
  public static final String COMPONENT = "component";
  public static final String COMPONENT_ALIAS = "component-alias";
  public static final String COMPONENT_TYPE = "component-type";
  public static final String COMPONENT_SPECIFICATION = "component-specification";
  public static final String CONTEXT_ASSET = "context-asset";
  public static final String DESCRIPTION = "description";
  public static final String EXTENSION = "extension";
  public static final String EXTERNAL_ASSET = "external-asset";
  public static final String FIELD_BINDING = "field-binding";
  public static final String INHERITED_BINDING = "inherited-binding";
  public static final String LIBRARY = "library";
  public static final String LIBRARY_SPECIFICATION = "library-specification";
  public static final String LISTENER_BINDING = "listener-binding";
  public static final String PAGE = "page";
  public static final String PAGE_SPECIFICATION = "page-specification";
  public static final String PARAMETER = "parameter";
  public static final String PRIVATE_ASSET = "private-asset";
  public static final String PROPERTY = "property";
  public static final String RESERVED_PARAMETER = "reserved-parameter";
  public static final String PROPERTY_SPECIFICATION = "property-specification";
  public static final String SERVICE = "service";
  public static final String SET_PROPERTY = "set-property";
  public static final String SET_STRING_PROPERTY = "set-string-property";
  public static final String STATIC_BINDING = "static-binding";
  public static final String STRING_BINDING = "string-binding";

  /** initialize the validators **/
  // note that DTDs 1.0, 1.1, and 1.2 are not supported
  // and since lookup is by index, the first 3 entries in the
  // validator array will be null
  static {
  }

  protected String rootElementName;

  protected String currentElementName;

  protected XMLAttributes currentAttributes;

  protected Augmentations currentAugmentations;

  protected IClassResolver classResolver;

  private TValidator validator;

  public TapestryValidator() {
    if (availableValidators == null) {
      availableValidators =
        new TValidator[] { null, null, null, new DTD1_3Validator(), new DTD1_4Validator()};

    }
  }

  /**
  * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(String, String, String, Augmentations)
  */
  public void doctypeDecl(
    String rootElement,
    String publicId,
    String systemId,
    Augmentations augs)
    throws XNIException {
    rootElementName = rootElement;
    validator = getValidator(publicId);
  }

  /**
   * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(QName, XMLAttributes, Augmentations)
   */
  public void startElement(QName element, XMLAttributes attributes, Augmentations augs)
    throws XNIException {
    try {
      if (validator != null) {

        currentElementName = element.rawname.toLowerCase();
        currentAttributes = attributes;
        currentAugmentations = augs;
        validator.handleElement();
      }
    } finally {
      super.startElement(element, attributes, augs);
    }
  }

  /**
  * @see org.apache.xerces.xni.XMLDocumentHandler#endDocument(Augmentations)
  */
  public void endDocument(Augmentations augs) throws XNIException {
    try {
      super.endDocument(augs);
    } finally {
      rootElementName = null;
      validator = null;
    }
  }

  protected void reportError(String message, Augmentations augs) {
    if (errorReporter != null && errorReporter instanceof TapestryErrorReporter) {

      XMLEnityEventInfo info = (XMLEnityEventInfo) augs.getItem(AUGMENTATIONS);
      ((TapestryErrorReporter) errorReporter).reportTapestryError(message, info);

    }
  }

  private TValidator getValidator(String publicId) {
    int DTDVersion = XMLUtil.getDTDVersion(publicId);
    if (DTDVersion < availableValidators.length) {
      return availableValidators[DTDVersion];
    }
    return null;
  }

  protected abstract class ElementValidator {

    public abstract void validateElement();

    protected void validateOGNLAttribute(int index) {
      if (index >= 0) {
        String possibleOGNL = currentAttributes.getValue(index);
        if (possibleOGNL != null && !"".equals(possibleOGNL.trim())) {
          try {
            validateOGNLExpression(possibleOGNL);
          } catch (ExpressionSyntaxException syn) {
            reportError(
              Tapestry.getString("OgnlUtils.unable-to-parse-expression", possibleOGNL),
              currentAttributes.getAugmentations(index));
          } catch (OgnlException e) {
            TapestryPlugin.getDefault().logException(e);
          }
        }
      }
    }

    protected void validateOGNLAttribute(String attributeName) {
      int index = currentAttributes.getIndex(attributeName);
      if (index >= 0) {
        validateOGNLAttribute(index);
      }
    }

    protected void validateOGNLExpression(String possibleOGNL) throws OgnlException {
      if (possibleOGNL != null && !"".equals(possibleOGNL.trim())) {
        Ognl.parseExpression(possibleOGNL);
      }
    }

    protected void validateClassAttribute(String attributeName) {
      if (classResolver != null) {
        int index = currentAttributes.getIndex(attributeName);
        if (index >= 0) {
          String value = currentAttributes.getValue(index);
          if (!classResolver.canResolveClass(value)) {
            reportError(
              "can't resolve class: " + value,
              currentAttributes.getAugmentations(index));
          }
        }
      }
    }

    protected void validateAttribute(String attributeName, String pattern, String errorKey) {

      int index = currentAttributes.getIndex(attributeName);
      if (index >= 0) {
        String value = currentAttributes.getValue(index);
        validateAttributeValue(index, value, pattern, errorKey);
      }

    }

    protected void validateAttributeValue(
      int index,
      String value,
      String pattern,
      String errorKey) {
      try {
        validateString(value, pattern, errorKey);
      } catch (InvalidStringException e) {
        reportError(e.getMessage(), currentAttributes.getAugmentations(index));
      }
    }

    protected void validateString(String value, String pattern, String errorKey)
      throws InvalidStringException {
      try {
        StringValidator.validate(value, pattern, errorKey);

      } catch (ApplicationRuntimeException runtime) {
        TapestryPlugin.getDefault().logException(runtime);
      }

    }

  }

  protected abstract class TValidator {

    protected Map validatorMap = new HashMap();

    public TValidator() {
      buildValidatorMap();
    }

    protected abstract void buildValidatorMap();

    protected void handleElement() {
      if (rootElementName.equals(currentElementName)) {
        handleRootElement();
      } else {
        ElementValidator validator = (ElementValidator) validatorMap.get(currentElementName);
        if (validator != null) {
          validator.validateElement();
        }
      }
    }

    /** base class does nothing special */
    protected void handleRootElement() {
      ElementValidator validator = (ElementValidator) validatorMap.get(currentElementName);
      if (validator != null) {
        validator.validateElement();
      }
    }

  }

  protected class BaseValidator extends TValidator {
    /**
     * build the element validators for those common to all DTDs
     * TBD.
     */
    protected void buildValidatorMap() {
      AssetElementValidator assetVal = new AssetElementValidator();
      validatorMap.put(CONTEXT_ASSET, assetVal);
      validatorMap.put(EXTERNAL_ASSET, assetVal);
      validatorMap.put(PRIVATE_ASSET, assetVal);
      validatorMap.put(BEAN, new BeanElementValidator());
      validatorMap.put(BINDING, new BindingElementValidator());
      validatorMap.put(COMPONENT, new ComponentElementValidator());
      validatorMap.put(COMPONENT_SPECIFICATION, new ComponentSpecificationValidator());
      validatorMap.put(CONFIGURE, new ConfigureElementValidator());
      validatorMap.put(EXTENSION, new ExtensionElementValidator());
      validatorMap.put(LIBRARY, new LibraryElementValidator());
      validatorMap.put(PAGE, new PageElementValidator());
      validatorMap.put(PARAMETER, new ParameterElementValidator());
      validatorMap.put(PROPERTY, new PropertyElementValidator());
      validatorMap.put(SERVICE, new ServiceElementValidator());
      validatorMap.put(SET_PROPERTY, new SetPropertyElementValidator());

    }

  }

  protected class DTD1_3Validator extends BaseValidator {
    /**
    * @see filters.TapestryValidator.TValidator#buildValidatorMap()
    */
    protected void buildValidatorMap() {
      super.buildValidatorMap();
      validatorMap.put(COMPONENT_ALIAS, new ComponentTypeElementValidator());
    }

  }

  protected class DTD1_4Validator extends BaseValidator {
    /**
    * @see filters.TapestryValidator.TValidator#buildValidatorMap()
    */
    protected void buildValidatorMap() {
      super.buildValidatorMap();
      validatorMap.put(COMPONENT_TYPE, new ComponentTypeElementValidator());
    }

  }

  protected class PageElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "name",
        SpecificationParser.PAGE_NAME_PATTERN,
        "SpecificationParser.invalid-page-name");
    }

  }

  /*
   *  called component-type in 1.4 but component-alias in 1.3 DTD
   *  use this one for both
   */
  protected class ComponentTypeElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "type",
        SpecificationParser.COMPONENT_ALIAS_PATTERN,
        "SpecificationParser.invalid-component-alias");
    }

  }

  /*
   *  validates all asset types
   */
  protected class AssetElementValidator extends ElementValidator {

    public void validateElement() {

      // special case: $template as asset name
      int index = currentAttributes.getIndex("name");
      String value = currentAttributes.getValue(index);
      if (!ITemplateSource.TEMPLATE_ASSET_NAME.equals(value)) {
        validateAttribute(
          "name",
          SpecificationParser.ASSET_NAME_PATTERN,
          "SpecificationParser.invalid-asset-name");
      }
    }
  }

  protected class BeanElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "name",
        SpecificationParser.BEAN_NAME_PATTERN,
        "SpecificationParser.invalid-bean-name");

      validateClassAttribute("class");
    }
  }

  protected class BindingElementValidator extends ElementValidator {

    public void validateElement() {

      validateOGNLAttribute("expression");
    }
  }

  protected class ComponentElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "id",
        SpecificationParser.COMPONENT_ID_PATTERN,
        "SpecificationParser.invalid-component-id");

      // this is ok even if type is missing
      validateAttribute(
        "type",
        SpecificationParser.COMPONENT_TYPE_PATTERN,
        "SpecificationParser.invalid-component-type");

    }
  }

  protected class ConfigureElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "property-name",
        SpecificationParser.PROPERTY_NAME_PATTERN,
        "SpecificationParser.invalid-property-name");

    }
  }

  protected class ExtensionElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "name",
        SpecificationParser.EXTENSION_NAME_PATTERN,
        "SpecificationParser.invalid-extension-name");

      validateClassAttribute("class");

    }
  }

  protected class LibraryElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "id",
        SpecificationParser.LIBRARY_ID_PATTERN,
        "SpecificationParser.invalid-library-id");

    }
  }

  protected class ParameterElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "name",
        SpecificationParser.PARAMETER_NAME_PATTERN,
        "SpecificationParser.invalid-parameter-name");

    }
  }

  protected class PropertyElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "name",
        SpecificationParser.PROPERTY_NAME_PATTERN,
        "SpecificationParser.invalid-property-name");

    }
  }

  protected class ServiceElementValidator extends ElementValidator {

    public void validateElement() {

      validateAttribute(
        "name",
        SpecificationParser.SERVICE_NAME_PATTERN,
        "SpecificationParser.invalid-service-name");

      validateClassAttribute("class");

    }
  }

  protected class SetPropertyElementValidator extends ElementValidator {

    public void validateElement() {

      validateOGNLAttribute("expression");

    }
  }

  protected class ComponentSpecificationValidator extends ElementValidator {

    public void validateElement() {

      validateClassAttribute("class");

    }
  }
}
