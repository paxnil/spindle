package com.iw.plugins.spindle.parser.xml;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import net.sf.tapestry.parse.SpecificationParser;
import org.apache.xerces.xni.XMLResourceIdentifier;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLEntityResolver;
import org.apache.xerces.xni.parser.XMLInputSource;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TapestryEntityResolver implements XMLEntityResolver {

  static private Map entities = new HashMap();

  static public void register(String publicId, String entityPath) {

    entities.put(publicId, entityPath);

  }

  /**
   * @see org.apache.xerces.xni.parser.XMLEntityResolver#resolveEntity(XMLResourceIdentifier)
   */
  public XMLInputSource resolveEntity(XMLResourceIdentifier resourceIdentifier)
    throws XNIException, IOException {
    String publicId = resourceIdentifier.getPublicId();

    String entityPath = null;

    entityPath = (String) entities.get(publicId);

    if (entityPath != null) {
      InputStream stream =
        getClass().getResourceAsStream("net.sf.tapestry.util.xml." + entityPath);

      XMLInputSource result =
        new XMLInputSource(
          resourceIdentifier.getPublicId(),
          resourceIdentifier.getLiteralSystemId(),
          resourceIdentifier.getBaseSystemId(),
          stream,
          (String) null);

      return result;

    }

    return null;

  }
}
