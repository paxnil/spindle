package com.iw.plugins.spindle.parser;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import net.sf.tapestry.IResourceLocation;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.spec.ComponentSpecification;
import net.sf.tapestry.spec.IApplicationSpecification;
import net.sf.tapestry.spec.ILibrarySpecification;
import net.sf.tapestry.util.xml.DocumentParseException;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.parser.filters.TapestryValidator;
import com.iw.plugins.spindle.parser.xml.TapestryDOMParser;
import com.iw.plugins.spindle.parser.xml.TapestryParserConfiguration;
import com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo;
import com.iw.plugins.spindle.util.SpindleMultiStatus;

public class PluginSpecificationParser {

  private SpecificationConfiguration parserConfig;
  private TapestryDOMParser parser;
  private TapestryObjectBuilder builder;

  public PluginSpecificationParser() {

    builder = new TapestryObjectBuilder();

  }

  public IStatus getStatus() {
    return parserConfig.getErrorReporter().getStatus();
  }

  private void checkParser() {
    if (parser == null) {
      parserConfig = new SpecificationConfiguration();
      parser = new TapestryDOMParser(parserConfig);
    }
  }

  /**
  *  Parses an input stream containing a page or component specification and assembles
  *  a {@link ComponentSpecification} from it.  
  *
  *  @throws DocumentParseException if the input stream cannot be fully
  *  parsed or contains invalid data.
  *
  **/

  public ComponentSpecification parseComponentSpecification(
    InputStream stream,
    IResourceLocation location)
    throws DocumentParseException {
    Document document = parseToDocument(stream, location);

    return builder.buildComponentSpecification(document, location);

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

  public ComponentSpecification parsePageSpecification(
    InputStream stream,
    IResourceLocation location)
    throws DocumentParseException {
    Document document = parseToDocument(stream, location);

    return builder.buildPageSpecification(document, location);

  }

  /**
   *  Parses an resource containing an application specification and assembles
   *  an {@link ApplicationSpecification} from it.
   *
   *  @throws DocumentParseException if the input stream cannot be fully
   *  parsed or contains invalid data.
   *
   **/

  public IApplicationSpecification parseApplicationSpecification(
    InputStream stream,
    IResourceLocation location)
    throws DocumentParseException {
    Document document = parseToDocument(stream, location);

    return builder.buildApplicationSpecification(document, location);

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

  public ILibrarySpecification parseLibrarySpecification(
    InputStream stream,
    IResourceLocation location)
    throws DocumentParseException {
    Document document = parseToDocument(stream, location);

    return builder.buildLibrarySpecification(document, location);

  }

  protected Document parseToDocument(InputStream stream, IResourceLocation location)
    throws DocumentParseException {
    Document document;

    checkParser();

    try {
      InputSource input = new InputSource(stream);
      parser.parse(input);
    } catch (SAXParseException ex) {
      // This constructor captures the line number and column number

      throw new DocumentParseException(
        Tapestry.getString("AbstractDocumentParser.unable-to-parse", location, ex.getMessage()),
        location,
        ex);
    } catch (SAXException ex) {
      throw new DocumentParseException(
        Tapestry.getString("AbstractDocumentParser.unable-to-parse", location, ex.getMessage()),
        location,
        ex);
    } catch (IOException ex) {
      throw new DocumentParseException(
        Tapestry.getString("AbstractDocumentParser.unable-to-read", location, ex.getMessage()),
        location,
        ex);
    }
    document = parser.getDocument();
    return document;
  }

  protected class SpecificationConfiguration extends TapestryParserConfiguration {

    private TapestryValidator tapestryValidator = new TapestryValidator();

    /**
    * @see com.iw.plugins.spindle.parser.xml.StandardParserConfiguration#createErrorReporter()
    */
    protected XMLErrorReporter createErrorReporter() {
      return new TapestryErrorReporter();
    }

    public TapestryErrorReporter getErrorReporter() {
      return (TapestryErrorReporter) fErrorReporter;
    }

    /**
     * setup pipeline - schemas are not supported!
     */
    protected void configurePipeline() {

      if (fDTDValidator != null) {
        fScanner.setDocumentHandler(fDTDValidator);
        fDTDValidator.setDocumentHandler(fNamespaceBinder);
        fNamespaceBinder.setDocumentHandler(fDocumentHandler);
      } else {
        fScanner.setDocumentHandler(fNamespaceBinder);
        fNamespaceBinder.setDocumentHandler(fDocumentHandler);
      }

      fLastComponent = fNamespaceBinder;

      // setup dtd pipeline
      if (fDTDScanner != null) {
        if (fDTDValidator != null) {
          fDTDScanner.setDTDHandler(fDTDValidator);
          fDTDValidator.setDTDHandler(fDTDHandler);

          fDTDScanner.setDTDContentModelHandler(fDTDValidator);
          fDTDValidator.setDTDContentModelHandler(fDTDContentModelHandler);
        } else {
          fDTDScanner.setDTDHandler(fDTDHandler);
          fDTDScanner.setDTDContentModelHandler(fDTDContentModelHandler);
        }
      }

      tapestryValidator.setErrorReporter(fErrorReporter);

      tapestryValidator.setDocumentSource(fLastComponent);
      tapestryValidator.setDocumentHandler(fDocumentHandler);

      fLastComponent = tapestryValidator;
    }

  }

}
