package com.iw.plugins.spindle.parser;

import java.io.IOException;
import java.io.InputStream;

import net.sf.tapestry.IResourceLocation;
import net.sf.tapestry.IResourceResolver;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.IApplicationSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.util.xml.DocumentParseException;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.iw.plugins.spindle.parser.xml.MyDOMParser;

public class SpecificationParser {

  private TapestryObjectBuilder builder = new TapestryObjectBuilder();
  private MyDOMParser parser;

  /**
   * Constructor for TapestrySpecParser.
   */
  public SpecificationParser() {
    super();
  }

  /**
  *  Parses an input stream containing a page or component specification and assembles
  *  a {@link ComponentSpecification} from it.  
  *
  *  @throws DocumentParseException if the input stream cannot be fully
  *  parsed or contains invalid data.
  *
  **/

  public ComponentSpecification parseComponentSpecification(InputStream stream)
    throws DocumentParseException {
    Document document = parseToDocument(stream);

    return builder.buildComponentSpecification(document);

  }

  /**
   *  Parses an input stream containing a page specification and assembles
   *  a {@link ComponentSpecification} from it.  
   *
   *  @throws DocumentParseException if the input stream cannot be fully
   *  parsed or contains invalid data.
   * 
   *
   **/

  public ComponentSpecification parsePageSpecification(InputStream stream)
    throws DocumentParseException {
    Document document = parseToDocument(stream);

    return builder.buildPageSpecification(document);

  }

  /**
   *  Parses an resource containing an application specification and assembles
   *  an {@link ApplicationSpecification} from it.
   *
   *  @throws DocumentParseException if the input stream cannot be fully
   *  parsed or contains invalid data.
   *
   **/

  public IApplicationSpecification parseApplicationSpecification(InputStream stream)
    throws DocumentParseException {
    Document document = parseToDocument(stream);

    return builder.buildApplicationSpecification(document);

  }

  /**
   *  Parses an input stream containing a library specification and assembles
   *  a {@link LibrarySpecification} from it.
   *
   *  @throws DocumentParseException if the input stream cannot be fully
   *  parsed or contains invalid data.
   * 
   *  @since 2.2
   *
   **/

  public ILibrarySpecification parseLibrarySpecification(InputStream stream)
    throws DocumentParseException {
    Document document = parseToDocument(stream);

    return builder.buildLibrarySpecification(document);

  }

  protected Document parseToDocument(InputStream stream) {
    Document document;

    try {
      parser.parse(new InputSource(stream));
    } catch (SAXException e) {
    } catch (IOException e) {
    }
    document = parser.getDocument();
    return document;
  }

}
