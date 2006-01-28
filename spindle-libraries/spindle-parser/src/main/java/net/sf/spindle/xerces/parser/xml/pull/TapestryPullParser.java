/*
 * Created on Apr 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package net.sf.spindle.xerces.parser.xml.pull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import net.sf.spindle.core.source.ISourceLocationResolver;
import net.sf.spindle.core.util.Assert;

import org.apache.xerces.parsers.XMLDocumentParser;
import org.apache.xerces.xni.Augmentations;
import org.apache.xerces.xni.QName;
import org.apache.xerces.xni.XMLAttributes;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XMLString;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLErrorHandler;
import org.apache.xerces.xni.parser.XMLParseException;
import org.apache.xerces.xni.parser.XMLParserConfiguration;
import org.apache.xerces.xni.parser.XMLPullParserConfiguration;
import org.w3c.dom.Node;

/**
 * An XML parser that builds a psuedo DOM tree in a pull fashion
 * 
 * @author glongman@gmail.com
 * 
 */
public class TapestryPullParser extends XMLDocumentParser implements XMLErrorHandler
{
  static public boolean Debug = true;

  /* a fast stack, not threadsafe! */
  static private class NodeStack extends ArrayList
  {
    private int last = -1;

    public NodeStack()
    {
      super();
    }

    public final void push(PullParserNode type)
    {

      super.add(type);
      last++;

    }

    public final PullParserNode pop()
    {
      return (PullParserNode) super.remove(last--);
    }

    public final PullParserNode peek()
    {
      if (last < 0)
      {
        return null;
      }
      return (PullParserNode) super.get(last);
    }

    public final boolean empty()
    {
      return last < 0;
    }
    public void reset()
    {
      super.clear();
      last = -1;
    }

    public void dump(PrintStream stream)
    {
      StringBuffer buffer = new StringBuffer();
      buffer.append("NodeStack<");
      if (last < 0)
      {
        buffer.append("empty");
      } else
      {
        for (int i = 0; i <= last; i++)
        {
          PullParserNode node = (PullParserNode) super.get(i);
          buffer.append("Node(" + node.getNodeName() + ")");
        }
      }
      buffer.append(">");
      stream.println(buffer.toString());
    }
  }

  /** need this so that the parser can stop itself * */
  private TapestryPullParserConfiguration fConfiguration;

  /** preserve the current state of the parse * */
  private NodeStack fParseStack = new NodeStack();

  private ISourceLocationResolver fResolver;

  /* various flags */
  private boolean fDocumentStarted;
  private boolean fDtdIsDone = false;
  private boolean fRootElementSeen;
  private boolean fDocumentIsDone;

  private String fPublicId;
  private PullParserNode fRootElement;
  private PullParserNode fLastCompletedElement;

  /**
   * @param config
   */
  public TapestryPullParser(XMLParserConfiguration config)
  {
    super(config);
    Assert.isLegal(config instanceof XMLPullParserConfiguration);
    fConfiguration = (TapestryPullParserConfiguration) config;
  }

  /*
   * reset everything, called just before a parse starts
   * 
   * @see org.apache.xerces.parsers.XMLParser#reset()
   */
  protected void reset() throws XNIException
  {
    super.reset();
    fDocumentStarted = false;
    fRootElementSeen = false;
    fRootElement = null;
    fDocumentIsDone = false;
    fParseStack.reset();
  }

  private void checkSanity() throws RuntimeException
  {
    // TODO this needs to be revisited

    //        if (!documentStarted || !rootElementSeen || documentIsDone)
    //        {
    //            throw new RuntimeException("TapestryPullParser is insane!");
    //        }
  }

  /**
   * 
   * I we want to use the location reported by the parser in Eclipse, we need
   * one of these.
   *  
   */
  public void setSourceResolver(ISourceLocationResolver resolver)
  {
    this.fResolver = resolver;
  }

  /**
   * Internal method to bump the parser.
   * 
   * Usually called by instances of PullParserNode
   * 
   * returns true if there is more to parse. returns false if the document is
   * done.
   *  
   */
  protected boolean bump()
  {
    //TODO Brian check this out
    checkSanity();
    try
    {
      if (Debug)
        System.err.println("pp bump");
      return fConfiguration.parse();
    } catch (IOException e)
    {
      throw new XNIException(e.getMessage());
    }
  }

  /*****************************************************************************
   * org.apache.xerces.xni.XMLDocumentHandler Stuff
   ****************************************************************************/

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#characters(org.apache.xerces.xni.XMLString,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void characters(XMLString text, Augmentations augs) throws XNIException
  {
    super.characters(text, augs);
    if (Debug)
      System.out.println("characters: " + text);
    if (!fRootElementSeen)
      // do nothing
      return;

    PullParserNode parent = fParseStack.peek();
    PullParserNode temp = PullParserNode.createTextNode(this, parent, text.toString());
    if (parent.fFirstChild == null)
    {
      parent.setFirstChild(temp);

    } else
    {
      temp.setPreviousSibling(fLastCompletedElement);
      fLastCompletedElement.setNextSibling(temp);
    }
    temp.completed();
    temp.setEmpty();
    fLastCompletedElement = temp;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(java.lang.String,
   *      java.lang.String, java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void doctypeDecl(
      String rootElement,
      String publicId,
      String systemId,
      Augmentations augs) throws XNIException
  {

    super.doctypeDecl(rootElement, publicId, systemId, augs);
    this.fPublicId = publicId;
    if (Debug)
      System.out.println("doctype decl: [root|publicId|systemId] " + rootElement + "|"
          + publicId + "|" + systemId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.XMLAttributes,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException
  {
    super.emptyElement(element, attributes, augs);
    if (Debug)
      System.out.println("emptyElement: " + element.rawname);
    fLastCompletedElement.setEmpty();

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#endDocument(org.apache.xerces.xni.Augmentations)
   */
  public void endDocument(Augmentations augs) throws XNIException
  {
    super.endDocument(augs);
    fDocumentIsDone = true;
    if (Debug)
      System.out.println("endDocument: ");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void endElement(QName element, Augmentations augs) throws XNIException
  {
    // find the last one, finish him up, pop off the stack
    super.endElement(element, augs);
    if (Debug)
      System.out.println("endElement: " + element.rawname);
    fParseStack.dump(System.err);

    fLastCompletedElement = fParseStack.pop();
    if (fLastCompletedElement != null)
      fLastCompletedElement.completed();

    //we don't want to stop parsing if the only
    // element left on the stack is root!
    if (fParseStack.peek() != fRootElement)
      fConfiguration.stopParsing();

    fParseStack.dump(System.err);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(org.apache.xerces.xni.XMLLocator,
   *      java.lang.String, org.apache.xerces.xni.Augmentations)
   */
  public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException
  {
    super.startDocument(locator, encoding, augs);
    reset();
    fDocumentStarted = true;
    if (Debug)
      System.out.println("startDocument: ");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(org.apache.xerces.xni.QName,
   *      org.apache.xerces.xni.XMLAttributes,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException
  {
    super.startElement(element, attributes, augs);
    if (Debug)
      System.out.println("startElement: " + element.rawname);
    fParseStack.dump(System.err);
    if (!fDtdIsDone)
    {} else if (!fRootElementSeen)
    {

      fRootElementSeen = true;
      // minor change, we need to save a reference to the root node!
      fRootElement = PullParserNode.createElementNode(
          this,
          null,
          element.rawname,
          attributes);
      fParseStack.push(fRootElement);
    } else
    {
      PullParserNode parent = fParseStack.peek();
      PullParserNode temp = PullParserNode.createElementNode(
          this,
          parent,
          element.rawname,
          attributes);
      if (parent.fFirstChild == null)
      {
        parent.setFirstChild(temp);

      } else
      {
        temp.setPreviousSibling(fLastCompletedElement);
        fLastCompletedElement.setNextSibling(temp);
      }
      fParseStack.push(temp);

    }
    fConfiguration.stopParsing();
    int length = attributes.getLength();
    for (int i = 0; i < length; i++)
    {
      System.out.println("\t\t" + attributes.getQName(i) + "='"
          + attributes.getNonNormalizedValue(i) + "'");
    }
    fParseStack.dump(System.err);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDocumentHandler#xmlDecl(java.lang.String,
   *      java.lang.String, java.lang.String,
   *      org.apache.xerces.xni.Augmentations)
   */
  public void xmlDecl(
      String version,
      String encoding,
      String standalone,
      Augmentations augs) throws XNIException
  {
    super.xmlDecl(version, encoding, standalone, augs);
    if (Debug)
      System.out.println("xmlDecl : [version|encoding|standalone] " + version + "|"
          + encoding + "|" + standalone);

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String,
   *      java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
   */
  public void error(String arg0, String arg1, XMLParseException arg2) throws XNIException
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(java.lang.String,
   *      java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
   */
  public void fatalError(String arg0, String arg1, XMLParseException arg2) throws XNIException
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(java.lang.String,
   *      java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
   */
  public void warning(String arg0, String arg1, XMLParseException arg2) throws XNIException
  {
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.apache.xerces.xni.XMLDTDHandler#endDTD(org.apache.xerces.xni.Augmentations)
   */
  public void endDTD(Augmentations arg0) throws XNIException
  {
    fDtdIsDone = true;
    super.endDTD(arg0);
  }

  /**
   * @return String the public id of the DTD
   */
  public String getPublicId()
  {
    return fPublicId;
  }

  /**
   * @return Node the root node of the document
   */
  public Node getRootNode()
  {
    return fRootElement;
  }

}