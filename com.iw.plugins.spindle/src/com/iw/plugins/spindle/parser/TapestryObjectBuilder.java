package com.iw.plugins.spindle.parser;

import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.IApplicationSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.util.xml.DocumentParseException;
import org.w3c.dom.Document;

import com.iw.plugins.spindle.parser.xml.TapestryEntityResolver;
import com.iw.plugins.spindle.spec.TapestryPluginSpecFactory;

public class TapestryObjectBuilder extends net.sf.tapestry.parse.SpecificationParser {

  /**
   * Constructor for SpecificationParser.
   */
  public TapestryObjectBuilder() {
    super();
    setFactory(new TapestryPluginSpecFactory());

  }

  /**
   *  assembles a {@link ComponentSpecification} from a parsed component spec document.  
   *
   *  @throws DocumentParseException if the document contains invalid data.
   *
   **/
  public ComponentSpecification buildComponentSpecification(Document document)
    throws DocumentParseException {

    return convertComponentSpecification(document, false);

  }

  /**
   *  assembles a {@link ComponentSpecification} from a parsed page spec document.  
   *
   *  @throws DocumentParseException if the document contains invalid data.
   *
   **/
  public ComponentSpecification buildPageSpecification(Document document)
    throws DocumentParseException {

    return convertComponentSpecification(document, true);

  }

  /**
   *  assembles a {@link ApplicationSpecification} rom a parsed app spec document.  
   *
   *   @throws DocumentParseException if the document contains invalid data.
   *
   **/

  public IApplicationSpecification buildApplicationSpecification(Document document)
    throws DocumentParseException {

    return convertApplicationSpecification(document, null);

  }

  /**
   *  assembles a {@link LibrarySpecification} from it.
   *
   *  @throws DocumentParseException if the document contains invalid data.
   *
   **/

  public ILibrarySpecification buildLibrarySpecification(Document document)
    throws DocumentParseException {

    return convertLibrarySpecification(document, null);

  }

  /**
   * @see net.sf.tapestry.util.xml.AbstractDocumentParser#register(String, String)
   */
  protected void register(String publicId, String entityPath) {
    super.register(publicId, entityPath);
    TapestryEntityResolver.register(publicId, entityPath);
  }

  /**
   * We are handling this using the our own xerces parser filter
   */
  protected void validate(String value, String pattern, String errorKey)
    throws DocumentParseException {
    // do nothing
  }

}
