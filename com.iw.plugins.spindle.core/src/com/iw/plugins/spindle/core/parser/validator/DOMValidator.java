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

package com.iw.plugins.spindle.core.parser.validator;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.xerces.util.XMLResourceIdentifierImpl;
import org.apache.xerces.xni.parser.XMLInputSource;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.core.ITapestryMarker;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.parser.xml.TapestryEntityResolver;
import com.iw.plugins.spindle.core.scanning.W3CAccess;
import com.iw.plugins.spindle.core.source.DefaultProblem;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.source.ISourceLocation;
import com.iw.plugins.spindle.core.source.ISourceLocationInfo;
import com.iw.plugins.spindle.core.util.Assert;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDCardinal;
import com.wutka.dtd.DTDChoice;
import com.wutka.dtd.DTDContainer;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDEnumeration;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDItemType;
import com.wutka.dtd.DTDMixed;
import com.wutka.dtd.DTDName;
import com.wutka.dtd.DTDParser;

/**
 *  Validates Tapestry DOM trees. This Validator is tuned to
 *  validation Tapestry XML. Its not a generic validator!
 * 
 *  Uses an IProblemCollector, but does not manage the lifecycle of it.
 *  Assumes the provider of the collector will manage it.
 * 
 *  Assumes documents provided to it are well-formed!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class DOMValidator implements IProblemCollector
{
    private static Map DTDS;
    private static DTDNodeInfo IGNORE = new IgnoreInfo();

    static {

        try
        {
            boolean debug = false;
            String publicId;
            XMLResourceIdentifierImpl resourceIdentifier;

            DTDS = new HashMap();

            // TAP 1.3 DTD

            publicId = SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID;

            resourceIdentifier = new XMLResourceIdentifierImpl(publicId, null, null, null);

            String errorMessage = TapestryCore.getString("dom-validator-error-no-DTD", publicId);

            XMLInputSource source = TapestryEntityResolver.doResolveEntity(resourceIdentifier);
            if (source == null)
                throw new Error(errorMessage);

            DTDS.put(
                publicId,
                new DTDParser(
                    new InputStreamReader(source.getByteStream()),
                    SpecificationParser.TAPESTRY_DTD_1_3_PUBLIC_ID,
                    debug)
                    .parse());

            // TAP 3.0 DTD

            publicId = SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID;

            resourceIdentifier = new XMLResourceIdentifierImpl(publicId, null, null, null);

            source = TapestryEntityResolver.doResolveEntity(resourceIdentifier);
            if (source == null)
                throw new Error(errorMessage);

            DTDS.put(
                publicId,
                new DTDParser(
                    new InputStreamReader(source.getByteStream()),
                    SpecificationParser.TAPESTRY_DTD_3_0_PUBLIC_ID,
                    debug)
                    .parse());

        } catch (IOException e)
        {
            e.printStackTrace();
            throw new Error(TapestryCore.getString("dom-validator-error-no-DTD-parse", e.getMessage()));
        }
    }

    public static DTD getDTD(String publicId)
    {
        if (publicId == null)
            return null;
        return (DTD) DTDS.get(publicId);
    }

    private Document fXMLDocument;
    private String fRootElementName;
    private boolean fSeenRootElement;
    private boolean fIsRunning;
    private DTD fDTD;
    private Map fNodeInfoMap;
    private List fProblems = new ArrayList();
    private Map fSeenIds = new HashMap();

    public DOMValidator()
    {}

    // standalone version
    public void validate(Document xmlDocument)
    {
        fSeenIds.clear();
        beginCollecting();
        documentStart(xmlDocument);
        Node rootNode = xmlDocument.getDocumentElement();
        traverseNode(rootNode);
        if (!fSeenIds.isEmpty())
        {
            for (Iterator iter = fSeenIds.entrySet().iterator(); iter.hasNext();)
            {
                Map.Entry entry = (Map.Entry) iter.next();
                List nodeList = (List) entry.getValue();
                if (nodeList.size() < 2)
                    continue;
                for (Iterator iterator = nodeList.iterator(); iterator.hasNext();)
                {
                    Node node = (Node) iterator.next();
                    recordAttributeError(
                        node,
                        "id",
                        TapestryCore.getString("dom-validator-id-attribute-must-be-unique"));
                }
            }
        }
        documentEnd();
        endCollecting();
    }

    private void traverseNode(Node node)
    {
        nodeStart(node);
        if (W3CAccess.isElement(node))
        {
            for (Node child = node.getFirstChild(); child != null; child = child.getNextSibling())
            {
                traverseNode(child);
            }
        }
        nodeEnd(node);
    }

    private boolean canStart()
    {
        fRootElementName = W3CAccess.getDeclaredRootElement(fXMLDocument);
        String publicId = W3CAccess.getPublicId(fXMLDocument);

        if (fRootElementName != null && publicId != null)
            fDTD = (DTD) DTDS.get(publicId);

        if (fDTD == null)
        {
            reportDocumentError(
                TapestryCore.getString("dom-validator-error-no-doctype", publicId != null ? publicId : "found null"));
            return false;
        }
        return true;
    }

    /**
     * @param document
     * @param xmlDocument
     */
    public void documentStart(Document xmlDocument)
    {
        Assert.isNotNull(xmlDocument);
        if (fIsRunning)
            throw new Error("already running!");
        fXMLDocument = xmlDocument;
        fNodeInfoMap = new HashMap();
        fIsRunning = canStart();
    }

    public void documentEnd()
    {
        fIsRunning = false;
        if (!fSeenRootElement)
            reportDocumentError(TapestryCore.getString("dom-validator-error-no-root"));

    }

    public void nodeStart(Node node)
    {
        if (!fIsRunning)
            return;

        String name = node.getNodeName();

        boolean allowed = checkNodeAllowedHere(name, node);
        boolean isElement = W3CAccess.isElement(node);

        if (isElement)
        {
            if (!allowed)
            {
                fNodeInfoMap.put(node, IGNORE);
            } else
            {
                checkAttributes(name, node);
                createElementInfo(node);
            }
        }
    }

    /**
     * @param node
     */
    private void createElementInfo(Node node)
    {
        String nodeName = node.getNodeName();
        fNodeInfoMap.put(node, DTDAccess.createElementInfo(fDTD, nodeName));
    }

    public void nodeEnd(Node node)
    {
        if (!fIsRunning)
            return;

        if (W3CAccess.isElement(node))
        {
            String name = node.getNodeName();
            checkNodeHasAllRequiredChildren(name, node);
        }
    }

    /**
     * @param name
     * @param node
     */
    private void checkNodeHasAllRequiredChildren(String name, Node node)
    {
        //might not need this.
    }

    private boolean checkNodeAllowedHere(String name, Node node)
    {
        if (!fSeenRootElement)
        {
            if (!W3CAccess.isElement(node))
            {
                reportDocumentError(TapestryCore.getString("dom-validator-error-invalid-root"));
                return false;
            }

            fSeenRootElement = true;
            fIsRunning = name.equals(fRootElementName);
            if (!fIsRunning)
                recordTagNameProblem(
                    name,
                    node,
                    TapestryCore.getString("dom-validator-error-wrong-root-element", fRootElementName, name));
            return fIsRunning;
        }

        if (W3CAccess.isComment(node))
            return true;

        if (!W3CAccess.isTextNode(node) && !DTDAccess.isDeclaredElement(fDTD, name))
        {
            recordTagNameProblem(name, node, TapestryCore.getString("dom-validator-undeclared-element", name));
            fNodeInfoMap.put(node, IGNORE);
            return false;
        }

        Node parentNode = node.getParentNode();
        DTDNodeInfo parentInfo = (DTDNodeInfo) fNodeInfoMap.get(parentNode);
        if (parentInfo == null)
            throw new Error("No node info for parent of " + name);

        if (parentInfo == IGNORE)
            return false;

        try
        {
            parentInfo.childSeen(node);
        } catch (ValidatorException e)
        {
            //add a problem
            recordTagNameProblem(name, node, e.getMessage());
            return false;
        }
        return true;
    }

    /**
     * @param node
     */
    private void checkAttributes(String elementName, Node node)
    {
        String nodeName = node.getNodeName();
        DTDElement element = DTDAccess.getDTDElement(fDTD, nodeName);
        if (element == null)
            throw new Error("expected dtd info here!");

        ISourceLocationInfo sourceInfo = getNodeSourceInfo(node);
        Set sourceAttributeNames = sourceInfo.getAttributeNames();

        for (Iterator iter = element.attributes.keySet().iterator(); iter.hasNext();)
        {
            String declaredAttrName = (String) iter.next();
            DTDAttribute declaredAttribute = element.getAttribute(declaredAttrName);
            if (sourceAttributeNames.contains(declaredAttrName))
            {
                sourceAttributeNames.remove(declaredAttrName);
                String value = W3CAccess.getAttribute(node, declaredAttrName);
                if (declaredAttrName.equals("id"))
                    recordSeenIdValue(node, value);

                if (declaredAttribute.defaultValue != null)
                {
                    if (value.equals(declaredAttribute.defaultValue))
                        // its ok
                        continue;
                }

                if (declaredAttribute.type instanceof DTDEnumeration)
                {
                    if (!((DTDEnumeration) declaredAttribute.type).getItemsVec().contains(value))
                    {
                        recordAttributeError(
                            node,
                            declaredAttrName,
                            TapestryCore.getString(
                                "dom-validator-invalid-attr-value",
                                declaredAttrName,
                                declaredAttribute.type.toString()));
                    }
                } else if (declaredAttribute.type instanceof String)
                {
                    continue;
                } else
                {
                    throw new Error("unrecognized attribute type :" + declaredAttribute.toString());
                }

            } else if (declaredAttribute.decl == DTDDecl.REQUIRED)
            {
                recordErrorOnTagName(
                    node,
                    TapestryCore.getString("dom-validator-missing-attr", declaredAttrName, nodeName));
            }
        }
        if (!sourceAttributeNames.isEmpty())
        {
            for (Iterator iter = sourceAttributeNames.iterator(); iter.hasNext();)
            {
                String undeclaredName = (String) iter.next();
                recordAttributeError(
                    node,
                    undeclaredName,
                    TapestryCore.getString("dom-validator-undeclared-atttribute", undeclaredName, nodeName));
            }
        }
    }

    private void recordSeenIdValue(Node node, String value)
    {
        List nodeList = (List) fSeenIds.get(value);
        if (nodeList == null)
            nodeList = new ArrayList();
        nodeList.add(node);
        fSeenIds.put(value, nodeList);
    }

    private void recordErrorOnTagName(Node node, String errorMessage)
    {
        addProblem(IProblem.ERROR, getNodeSourceInfo(node).getTagNameLocation(), errorMessage, false);
    }

    /**
     * @param node
     * @param attrName
     * @param errorMessage
     */
    private void recordAttributeError(Node node, String attrName, String errorMessage)
    {
        ISourceLocationInfo sourceInfo = getNodeSourceInfo(node);
        ISourceLocation attributeLocation = sourceInfo.getAttributeSourceLocation(attrName);
        addProblem(IProblem.ERROR, attributeLocation, errorMessage, false);
    }

    /**
     * @param node
     * @param string
     */
    private void recordTagNameProblem(String elementName, Node node, String message)
    {
        addProblem(IProblem.ERROR, getTagNameLocation(elementName, node), message, false);
    }

    /**
     * @param node
     * @param string
     */
    private void reportDocumentError(String message)
    {
        addProblem(
            new DefaultProblem(ITapestryMarker.TAPESTRY_PROBLEM_MARKER, IProblem.ERROR, message, 1, -1, -1, false));
    }

    private ISourceLocation getTagNameLocation(String name, Node node)
    {
        if (W3CAccess.isTextNode(node))
        {
            Node parent = node.getParentNode();
            return getTagNameLocation(parent.getNodeName(), parent);
        }
        return getNodeSourceInfo(node).getTagNameLocation();
    }

    private ISourceLocationInfo getNodeSourceInfo(Node node)
    {
        return W3CAccess.getSourceLocationInfo(node);
    }

    private ISourceLocation getAttributeSourceLocation(String attributeName, Node node)
    {
        ISourceLocationInfo info = W3CAccess.getSourceLocationInfo(node);
        return info.getAttributeSourceLocation(attributeName);
    }

    private static class DTDAccess
    {
        Map DTDInfos;

        static boolean isDeclaredElement(DTD dtd, String name)
        {
            return dtd.elements.containsKey(name);
        }

        /**
         * 
         * @param fDTD
         * @param nodeName
         * @return a list containing the child elements allowed for this node.
         */
        public static DTDNodeInfo createElementInfo(DTD dtd, String elementName)
        {
            List result = Collections.EMPTY_LIST;
            DTDElement declaredElement = getDTDElement(dtd, elementName);
            if (declaredElement != null)
            {
                return new DTDNodeInfo(declaredElement);
            } else
            {
                throw new Error("undeclared element: " + elementName);
            }
        }

        /**
         * @param elementName
         * @return
         */
        static DTDElement getDTDElement(DTD dtd, String elementName)
        {
            return (DTDElement) dtd.elements.get(elementName);
        }

    }

    private static class DTDNodeInfo
    {
        String elementName;
        DTDItemType contentType;
        DTDItem workingContent;
        boolean allowsText = false;

        protected DTDNodeInfo()
        {}

        public DTDNodeInfo(DTDElement element)
        {
            elementName = element.getName();
            DTDItem content = element.getContent();
            contentType = content.getItemType();
            try
            {
                workingContent = (DTDItem) content.clone();
            } catch (CloneNotSupportedException e)
            {
                throw new Error("clone failed");
            }
            if (contentType == DTDItemType.DTD_MIXED)
            {
                DTDMixed mixed = (DTDMixed) workingContent;
                List items = mixed.getItemsVec();
                for (Iterator iter = items.iterator(); iter.hasNext();)
                {
                    DTDItem mixedItem = (DTDItem) iter.next();
                    if (mixedItem.getItemType() == DTDItemType.DTD_PCDATA)
                        iter.remove();
                }
                allowsText = true;
            } else
            {
                allowsText = false;
            }
        }

        private String getAllowedContent()
        {
            return workingContent.toString();
        }

        public void childSeen(Node node) throws ValidatorException
        {
            if (W3CAccess.isTextNode(node))
            {
                if (allowsText)
                    return;
                throw new ValidatorException(TapestryCore.getString("dom-validator-text-not-allowed", elementName));
            }
            String childName = node.getNodeName();
            if (workingContent == null)
            {
                throw new ValidatorException(
                    TapestryCore.getString("dom-validator-child-not-allowed", childName, elementName));
            }
            if (contentType == DTDItemType.DTD_SEQUENCE
                || contentType == DTDItemType.DTD_MIXED
                || contentType == DTDItemType.DTD_CHOICE)
            {

                List items = ((DTDContainer) workingContent).getItemsVec();
                if (contentType == DTDItemType.DTD_SEQUENCE || contentType == DTDItemType.DTD_MIXED)
                {
                    if (checkContainer(childName, items))
                        workingContent = null;

                } else if (contentType == DTDItemType.DTD_CHOICE)
                {
                    if (match(workingContent, childName))
                    {
                        if (workingContent.getCardinal() == DTDCardinal.NONE)
                            workingContent = null;
                    } else
                    {
                        throw new ValidatorException(
                            TapestryCore.getString(
                                "dom-validator-element-not-allowed",
                                childName,
                                getAllowedContent()));
                    }

                }
            } else
            {
                throw new Error("unexpected type: " + contentType.toString());
            }
        }

        /**
         * @param childName
         * @param items
         * @return true iff the container is completely satisfied and should be removed
         * from consideration.
         */
        private boolean checkContainer(String childName, List items) throws ValidatorException
        {
            if (items.isEmpty())
                throw new ValidatorException(
                    TapestryCore.getString("dom-validator-child-not-allowed", childName, elementName));
            // does it match the first?
            DTDItem item = (DTDItem) items.get(0);
            DTDItemType type = item.getItemType();
            DTDCardinal cardinal = item.getCardinal();
            if (match(item, childName))
            { //there can be only ONE!
                if (cardinal == DTDCardinal.NONE)
                {
                    items.remove(0);
                }
            } else
            { // doesn't match, but there must be at least one
                if (cardinal == DTDCardinal.ONEMANY)
                {

                    throw new ValidatorException(
                        TapestryCore.getString("dom-validator-element-not-allowed", childName, item.toString()));
                } else
                {
                    boolean found = false;
                    //go down the line looking for a match
                    for (int i = 1; i < items.size(); i++)
                    {
                        item = (DTDItem) items.get(i);
                        cardinal = item.getCardinal();
                        if (found = match(item, childName))
                        {
                            int j = 0;
                            for (Iterator iter = items.iterator(); iter.hasNext() && j < i;)
                            {
                                iter.next();
                                iter.remove();
                                j++;

                            }
                            if (cardinal == DTDCardinal.NONE)
                                items.remove(i);
                            break;
                        } else if (cardinal == DTDCardinal.NONE || cardinal == DTDCardinal.ONEMANY)
                        {
                            throw new ValidatorException(
                                TapestryCore.getString(
                                    "dom-validator-element-not-allowed",
                                    childName,
                                    item.toString()));
                        }
                    }
                    if (!found)
                        throw new ValidatorException(
                            TapestryCore.getString(
                                "dom-validator-element-not-allowed",
                                childName,
                                getAllowedContent()));
                }
            }
            return items.isEmpty();
        }

        private boolean match(DTDItem item, String childName)
        {
            DTDItemType type = item.getItemType();
            if (type == DTDItemType.DTD_NAME)
            {
                return ((DTDName) item).getValue().equals(childName);
            } else if (type == DTDItemType.DTD_CHOICE)
            {
                for (Iterator iter = ((DTDChoice) item).getItemsVec().iterator(); iter.hasNext();)
                {
                    DTDName name = (DTDName) iter.next();
                    String childName1 = childName;
                    if (name.getValue().equals(childName1))
                        return true;
                }
                return false;
            }
            throw new Error("unexpected type" + type.toString());
        }

    }

    private static class IgnoreInfo extends DTDNodeInfo
    {
        public IgnoreInfo()
        {
            super();
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.validator.DOMValidator.DTDNodeInfo#childSeen(org.w3c.dom.Node)
         */
        public void childSeen(Node node) throws ValidatorException
        {}

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.source.IProblemCollector#addProblem(int, com.iw.plugins.spindle.core.source.ISourceLocation, java.lang.String)
     */
    public void addProblem(int severity, ISourceLocation location, String message, boolean isTemporary)
    {
        fProblems.add(
            new DefaultProblem(
                ITapestryMarker.TAPESTRY_PROBLEM_MARKER,
                severity,
                message,
                location.getLineNumber(),
                location.getCharStart(),
                location.getCharEnd(),
                isTemporary));
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.source.IProblemCollector#addProblem(com.iw.plugins.spindle.core.source.IProblem)
     */
    public void addProblem(IProblem problem)
    {
        fProblems.add(problem);

    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.source.IProblemCollector#beginCollecting()
     */
    public void beginCollecting()
    {
        fProblems.clear();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.source.IProblemCollector#endCollecting()
     */
    public void endCollecting()
    {
        // do nothing.
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.source.IProblemCollector#getProblems()
     */
    public IProblem[] getProblems()
    {
        return (IProblem[]) fProblems.toArray(new IProblem[fProblems.size()]);
    }

}
