/*
 * Created on Apr 11, 2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.iw.plugins.spindle.core.parser.xml.pull;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

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

import com.iw.plugins.spindle.core.parser.ISourceLocationResolver;
import com.iw.plugins.spindle.core.util.Assert;

/**
 *  An XML parser that builds a psuedo DOM tree in a pull fashion
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TapestryPullParser extends XMLDocumentParser implements XMLErrorHandler
{

    /* a fast stack, not threadsafe! */
    class NodeStack extends ArrayList
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

    /** need this so that the parser can stop itself **/
    private TapestryPullParserConfiguration configuration;

    /** preserve the current state of the parse **/
    private NodeStack parseStack = new NodeStack();

    private ISourceLocationResolver resolver;

    /* debug flag, set to false when this code goes into production! */
    private boolean debug = true;

    /* various flags*/
    private boolean documentStarted;
    private boolean dtdIsDone = false;
    private boolean rootElementSeen;
    private boolean documentIsDone;

    private String publicId;
    private PullParserNode rootElement;
    private PullParserNode lastCompletedElement;

    /**
     * @param config
     */
    public TapestryPullParser(XMLParserConfiguration config)
    {
        super(config);
        Assert.isLegal(config instanceof XMLPullParserConfiguration);
        this.configuration = (TapestryPullParserConfiguration) config;
    }

    /* 
     * reset everything, called just before a parse starts
     * 
     * @see org.apache.xerces.parsers.XMLParser#reset()
     */
    protected void reset() throws XNIException
    {
        super.reset();
        documentStarted = false;
        rootElementSeen = false;
        rootElement = null;
        documentIsDone = false;
        parseStack.clear();
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
     * I we want to use the location reported by the parser in Eclipse,
     * we need one of these.
     * 
     */
    public void setSourceResolver(ISourceLocationResolver resolver)
    {
        this.resolver = resolver;
    }

    /**
     * 
     * 
     * Internal method to bump the parser.
     * 
     * Usually called by instances of PullParserNode
     * 
     * returns true if there is more to parse.
     * returns false if the document is done.
     *  
     **/
    protected boolean bump()
    {
        //TODO Brian check this out
        checkSanity();
        try
        {
            System.err.println("pp bump");
            return configuration.parse();
        } catch (IOException e)
        {
            throw new XNIException(e.getMessage());
        }
    }

    /* ***************************************************** 
     *    org.apache.xerces.xni.XMLDocumentHandler Stuff      
     * *****************************************************/

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#characters(org.apache.xerces.xni.XMLString, org.apache.xerces.xni.Augmentations)
     */
    public void characters(XMLString text, Augmentations augs) throws XNIException
    {        
        super.characters(text, augs);
        System.out.println("characters: " + text);
        if (!rootElementSeen) {
            // do nothing
            return;
        }
        
        
        PullParserNode parent = parseStack.peek();
        PullParserNode temp = PullParserNode.createTextNode(this, parent, text.toString());
        if (parent.firstChild == null)
        {
            parent.setFirstChild(temp);

        } else
        {
            temp.setPreviousSibling(lastCompletedElement);
            lastCompletedElement.setNextSibling(temp);
        }
        temp.completed();
        temp.setEmpty();
        lastCompletedElement = temp;
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#doctypeDecl(java.lang.String, java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void doctypeDecl(String rootElement, String publicId, String systemId, Augmentations augs)
        throws XNIException
    {

        super.doctypeDecl(rootElement, publicId, systemId, augs);
        this.publicId = publicId;
        System.out.println("doctype decl: [root|publicId|systemId] " + rootElement + "|" + publicId + "|" + systemId);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#emptyElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
     */
    public void emptyElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException
    {
        // TODO Auto-generated method stub
        super.emptyElement(element, attributes, augs);
        System.out.println("emptyElement: " + element.rawname);
        lastCompletedElement.setEmpty();

    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#endDocument(org.apache.xerces.xni.Augmentations)
     */
    public void endDocument(Augmentations augs) throws XNIException
    {
        super.endDocument(augs);
        documentIsDone = true;
        System.out.println("endDocument: ");
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#endElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.Augmentations)
     */
    public void endElement(QName element, Augmentations augs) throws XNIException
    {
        // TODO Auto-generated method stub

        // find the last one, finish him up, pop off the stack
        super.endElement(element, augs);
        System.out.println("endElement: " + element.rawname);
        parseStack.dump(System.err);

        lastCompletedElement = parseStack.pop();
        if (lastCompletedElement != null)
        {
            lastCompletedElement.completed();
        }
        //we don't want to stop parsing if the only
        // element left on the stack is root!
        if (parseStack.peek() != rootElement)
        {
            configuration.stopParsing();
        }
        parseStack.dump(System.err);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#startDocument(org.apache.xerces.xni.XMLLocator, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void startDocument(XMLLocator locator, String encoding, Augmentations augs) throws XNIException
    {
        super.startDocument(locator, encoding, augs);
        reset();
        documentStarted = true;
        System.out.println("startDocument: ");
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#startElement(org.apache.xerces.xni.QName, org.apache.xerces.xni.XMLAttributes, org.apache.xerces.xni.Augmentations)
     */
    public void startElement(QName element, XMLAttributes attributes, Augmentations augs) throws XNIException
    {
        super.startElement(element, attributes, augs);
        System.out.println("startElement: " + element.rawname);
        parseStack.dump(System.err);
        if (!dtdIsDone)
        {} else if (!rootElementSeen)
        {

            rootElementSeen = true;
            // minor change, we need to save a reference to the root node!
            rootElement = PullParserNode.createElementNode(this, null, element.rawname, attributes);
            parseStack.push(rootElement);
        } else
        {
            PullParserNode parent = parseStack.peek();
            PullParserNode temp = PullParserNode.createElementNode(this, parent, element.rawname, attributes);
            if (parent.firstChild == null)
            {
                parent.setFirstChild(temp);

            } else
            {
                temp.setPreviousSibling(lastCompletedElement);
                lastCompletedElement.setNextSibling(temp);
            }
            parseStack.push(temp);

        }
        configuration.stopParsing();
        int length = attributes.getLength();
        for (int i = 0; i < length; i++)
        {
            System.out.println("\t\t" + attributes.getQName(i) + "='" + attributes.getNonNormalizedValue(i) + "'");
        }
        parseStack.dump(System.err);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDocumentHandler#xmlDecl(java.lang.String, java.lang.String, java.lang.String, org.apache.xerces.xni.Augmentations)
     */
    public void xmlDecl(String version, String encoding, String standalone, Augmentations augs) throws XNIException
    {
        // TODO Auto-generated method stub
        super.xmlDecl(version, encoding, standalone, augs);
        System.out.println("xmlDecl : [version|encoding|standalone] " + version + "|" + encoding + "|" + standalone);

    }

    //    /* ********************************************************** 
    //     *  END OF  org.apache.xerces.xni.XMLDocumentHandler Stuff      
    //     * **********************************************************/
    //
    //    /* **************************************** 
    //     *    org.w3c.dom.Node Stuff      
    //     * ****************************************/
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#appendChild(org.w3c.dom.Node)
    //     */
    //    public Node appendChild(Node arg0) throws DOMException
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#cloneNode(boolean)
    //     */
    //    public Node cloneNode(boolean arg0)
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getAttributes()
    //     */
    //    public NamedNodeMap getAttributes()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getAttributes();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getChildNodes()
    //     */
    //    public NodeList getChildNodes()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getFirstChild()
    //     */
    //    public Node getFirstChild()
    //    {
    //        checkSanity(false);
    //        State current = seenStates.peek();
    //        next();
    //        if (documentIsDone || current.isComplete())
    //        {
    //            return null;
    //        }
    //        return this;
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getLastChild()
    //     */
    //    public Node getLastChild()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getLocalName()
    //     */
    //    public String getLocalName()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getLocalName();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getNamespaceURI()
    //     */
    //    public String getNamespaceURI()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getNamespaceURI();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getNextSibling()
    //     */
    //    public Node getNextSibling()
    //    {
    //        checkSanity(false);
    //        State current = seenStates.peek();
    //        if (!current.isComplete())
    //        {
    //            while (!current.isComplete())
    //            {
    //                next();
    //            }
    //        }
    //        if (documentIsDone)
    //        {
    //            return null;
    //        }
    //        return this;
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getNodeName()
    //     */
    //    public String getNodeName()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getNodeName();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getNodeType()
    //     */
    //    public short getNodeType()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getNodeType();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getNodeValue()
    //     */
    //    public String getNodeValue() throws DOMException
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getNodeValue();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getOwnerDocument()
    //     */
    //    public Document getOwnerDocument()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getParentNode()
    //     */
    //    public Node getParentNode()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getPrefix()
    //     */
    //    public String getPrefix()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().getPrefix();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#getPreviousSibling()
    //     */
    //    public Node getPreviousSibling()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#hasAttributes()
    //     */
    //    public boolean hasAttributes()
    //    {
    //        checkSanity(false);
    //        return seenStates.peek().hasAttributes();
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#hasChildNodes()
    //     */
    //    public boolean hasChildNodes()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#insertBefore(org.w3c.dom.Node, org.w3c.dom.Node)
    //     */
    //    public Node insertBefore(Node arg0, Node arg1) throws DOMException
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#isSupported(java.lang.String, java.lang.String)
    //     */
    //    public boolean isSupported(String arg0, String arg1)
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#normalize()
    //     */
    //    public void normalize()
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#removeChild(org.w3c.dom.Node)
    //     */
    //    public Node removeChild(Node arg0) throws DOMException
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#replaceChild(org.w3c.dom.Node, org.w3c.dom.Node)
    //     */
    //    public Node replaceChild(Node arg0, Node arg1) throws DOMException
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#setNodeValue(java.lang.String)
    //     */
    //    public void setNodeValue(String arg0) throws DOMException
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.w3c.dom.Node#setPrefix(java.lang.String)
    //     */
    //    public void setPrefix(String arg0) throws DOMException
    //    {
    //        throw new IllegalStateException("not supported");
    //    }
    //
    //    /* **************************************** 
    //     *   END OF org.w3c.dom.Node Stuff      
    //     * ****************************************/
    //
    //    /* **************************************** 
    //     *    XML Error Handler Stuff      
    //     * ****************************************/
    //
    //    /* (non-Javadoc)
    //     * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
    //     */
    //    public void error(String domain, String key, XMLParseException exception) throws XNIException
    //    {
    //        // TODO Auto-generated method stub
    //        System.err.println("error");
    //        exception.printStackTrace(System.err);
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
    //     */
    //    public void fatalError(String domain, String key, XMLParseException exception) throws XNIException
    //    {
    //        System.err.println("fatalError");
    //        exception.printStackTrace(System.err);
    //        throw new ParserRuntimeException(exception);
    //
    //    }
    //
    //    /* (non-Javadoc)
    //     * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
    //     */
    //    public void warning(String domain, String key, XMLParseException exception) throws XNIException
    //    {
    //        System.err.println("fatalError");
    //        exception.printStackTrace(System.err);
    //        throw exception;
    //
    //    }
    //
    //
    //
    //    /* **************************************** 
    //     *    END of XML Error Handler Stuff      
    //     * ****************************************/

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#error(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void error(String arg0, String arg1, XMLParseException arg2) throws XNIException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#fatalError(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void fatalError(String arg0, String arg1, XMLParseException arg2) throws XNIException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.parser.XMLErrorHandler#warning(java.lang.String, java.lang.String, org.apache.xerces.xni.parser.XMLParseException)
     */
    public void warning(String arg0, String arg1, XMLParseException arg2) throws XNIException
    {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#endDTD(org.apache.xerces.xni.Augmentations)
     */
    public void endDTD(Augmentations arg0) throws XNIException
    {
        dtdIsDone = true;
        super.endDTD(arg0);
    }

    /* (non-Javadoc)
     * @see org.apache.xerces.xni.XMLDTDHandler#startDTD(org.apache.xerces.xni.XMLLocator, org.apache.xerces.xni.Augmentations)
     */
    public void startDTD(XMLLocator arg0, Augmentations arg1) throws XNIException
    {
        // TODO Auto-generated method stub
        super.startDTD(arg0, arg1);
    }

    /**
     * @return
     */
    public String getPublicId()
    {
        return publicId;
    }

    /**
     * @return
     */
    public Node getRootNode()
    {

        return rootElement;
    }

}
