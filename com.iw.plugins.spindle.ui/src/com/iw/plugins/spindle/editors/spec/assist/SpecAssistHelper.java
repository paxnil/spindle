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

package com.iw.plugins.spindle.editors.spec.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.tapestry.util.MultiKey;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.editors.util.CompletionProposal;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDComment;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDEnumeration;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDItemType;
import com.wutka.dtd.DTDSequence;

/**
 *  Contains Helper methods for content assist in specs.s
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecAssistHelper
{
    private static final Integer DEFAULT_NEW_ELEMENT_PROPOSAL = new Integer(0);
    private static final Integer OPTIONAL_NEW_ELEMENT_PROPOSAL = new Integer(1);
    private static final Integer ELEMENT_ATTRIBUTES = new Integer(2);
    private static final Integer ELEMENT_REQUIRED_ATTRIBUTES = new Integer(3);
    private static final Integer ELEMENT_ATTRIBUTE_DEFAULT_VALUE = new Integer(4);
    private static final Integer ELEMENT_ATTRIBUTE_ALLOWED_VALUES = new Integer(5);
    private static final Integer ELEMENT_COMMENT = new Integer(6);
    /**
      * Map caching the results of all of the lookup methods in this class
      */
    static private final Map DTDInfoMap = new HashMap();

    static public void reset()
    {
        DTDInfoMap.clear();
    }

    /**
     * Examine a DTD and return the names all of the children of a parent.
     * The string returned is in DTD source format.
     * 
     * Results are cached
     * 
     * @param dtd the DTD we are basing the lookup on
     * @param elementName the element whose children we are seeking.
     * @return a String containing the child names, or null if the elementName is not a valid element in the DTDs
     */
    public static String getAllowedElements(DTD dtd, String elementName)
    {
        String useName = elementName.toLowerCase();
        MultiKey key = new MultiKey(new Object[] { dtd, useName }, false);

        String result = (String) DTDInfoMap.get(key);
        if (result == null)
        {
            DTDElement element = (DTDElement) dtd.elements.get(useName);
            if (element != null)
            {
                result = element.content.toString();
                DTDInfoMap.put(key, result);
            }
        }

        return result;
    }

    /**
     *  Return all of the element names allowed by the DTD at a point in 
     *  the document after the last child of a given parent element.
     * 
     *  Results are cached.
     * 
     * @param dtd the DTD in use by the document
     * @param parentElement the name of the element that is parent 
     * @param lastChild the name of the last child of the parent before the place we are interested
     * @param sort if true place the last child at the front of a sorted list of elementNames
     * @return list of String
     */
    public static List getAllowedChildren(DTD dtd, String parentElement, String lastChild, boolean sort)
    {
        String useParent = parentElement.toLowerCase();
        String useChild = lastChild == null ? "~NULL_CHILD" : lastChild.toLowerCase();
        MultiKey key = new MultiKey(new Object[] { dtd, useParent, useChild, new Boolean(sort)}, false);

        List result = (List) DTDInfoMap.get(key);

        if (result == null)
        {

            MultiKey nonSorted = new MultiKey(new Object[] { dtd, useParent, useChild, new Boolean(false)}, false);
            MultiKey sorted = new MultiKey(new Object[] { dtd, useParent, useChild, new Boolean(true)}, false);
            String allowed = getAllowedElements(dtd, useParent);
            if (allowed == null || allowed.equals("EMPTY") || allowed.equals("(PCDATA)"))
            {
                result = Collections.EMPTY_LIST;
                DTDInfoMap.put(nonSorted, result);
                DTDInfoMap.put(sorted, result);
            } else if (!useChild.equals("~NULL_CHILD"))
            {
                DTDElement element = (DTDElement) dtd.elements.get(useParent);
                DTDSequence sequence = (DTDSequence) element.getContent();
                Vector sequenceItems = sequence.getItemsVec();
                int seqSize = sequenceItems.size();
                if (seqSize > 1)
                {
                    int matchIndex = -1;

                    for (int i = 0; i < seqSize; i++)
                    {
                        DTDItem item = (DTDItem) sequenceItems.get(i);
                        if (item.match(useChild))
                        {

                            matchIndex = i;
                            break;
                        }
                    }

                    if (matchIndex > 0)
                    {
                        DTDSequence clone = (DTDSequence) sequence.clone();
                        Vector items = clone.getItemsVec();
                        int i = 0;
                        for (Iterator iter = items.iterator(); iter.hasNext();)
                        {
                            iter.next();
                            if (i++ < matchIndex)
                            {
                                iter.remove();
                            } else
                            {
                                break;
                            }
                        }
                        allowed = clone.toString();
                    }
                }

            }
            result = new ArrayList();
            StringTokenizer tok = new StringTokenizer(allowed, "(),*| ?");
            while (tok.hasMoreTokens())
            {
                result.add(tok.nextToken());
            }
            if (result.isEmpty())
            {
                result = Collections.EMPTY_LIST;
                DTDInfoMap.put(nonSorted, result);
                DTDInfoMap.put(sorted, result);
            } else
            {
                result.remove(useChild);
                result.add(0, useChild);
                DTDInfoMap.put(nonSorted, Collections.unmodifiableList(result));
                //sort
                TreeSet set = new TreeSet(result);
                set.remove(useChild);
                ArrayList sortedList = new ArrayList(set);
                sortedList.add(0, useChild);
                DTDInfoMap.put(sorted, Collections.unmodifiableList(sortedList));
                result = (List) DTDInfoMap.get(key);

            }
        }

        return result;
    }

    public static List getNewElementCompletionProposals(DTD dtd, String elementName)
    {
        String useName = elementName.toLowerCase();
        MultiKey defaultKey = new MultiKey(new Object[] { dtd, useName, DEFAULT_NEW_ELEMENT_PROPOSAL }, false);
        Object defaultProposal = DTDInfoMap.get(defaultKey);
        if (defaultProposal == null)
        {
            computeNewElementProposals(dtd, useName);
        }
        defaultProposal = DTDInfoMap.get(defaultKey);
        if (defaultProposal == null)
            return Collections.EMPTY_LIST;

        ArrayList result = new ArrayList();
        result.add(defaultProposal);

        MultiKey optionalKey = new MultiKey(new Object[] { dtd, useName, OPTIONAL_NEW_ELEMENT_PROPOSAL }, false);
        Object optionalProposal = DTDInfoMap.get(optionalKey);

        if (optionalProposal != null)
            result.add(optionalProposal);

        return result;
    }
    /**
     * @param dtd
     * @param elementName
     */
    private static void computeNewElementProposals(DTD dtd, String elementName)
    {
        String publicId = dtd.getPublicId();
        DTDElement element = (DTDElement) dtd.elements.get(elementName);

        if (element == null)
            return;

        String commentString = internalGetElementComment(dtd, elementName);

        String emptyOption = null;
        String nonEmptyOption = null;
        String prelude = "<" + elementName;

        DTDItem content = element.getContent();

        boolean allowsChildren = content.getItemType() != DTDItemType.DTD_EMPTY;

        int offset = -1;
        CompletionProposal emptyProposal;
        CompletionProposal nonEmptyProposal = null;

        boolean hasAttibutes = !internalGetAttributes(dtd, elementName).isEmpty();
        if (hasAttibutes)
        {
            List requiredAttributes = internalGetRequiredAttributes(dtd, elementName);
            if (!requiredAttributes.isEmpty())
            {
                prelude += " ";
                for (Iterator iter = requiredAttributes.iterator(); iter.hasNext();)
                {
                    String attrName = (String) iter.next();
                    prelude += attrName + "=\"\"";
                    if (offset == -1)
                        offset = prelude.length() - 1;
                    if (iter.hasNext())
                        prelude += " ";
                }
            }
        }

        emptyOption = prelude + "/>";

        if (offset == -1 && hasAttibutes)
        {
            offset = 1 + elementName.length();
        } else if (offset == -1)
        {
            offset = emptyOption.length();
        }

        if (allowsChildren)
        {
            nonEmptyOption = prelude + "> </" + elementName + ">";
        }

        emptyProposal =
            new CompletionProposal(
                emptyOption,
                -1,
                -1,
                new Point(offset, 0),
                null,
                elementName + " (empty)",
                null,
                commentString.length() > 0 ? commentString : emptyOption);

        if (nonEmptyOption != null)
        {
            if (!hasAttibutes)
                offset -= 1;

            nonEmptyProposal =
                new CompletionProposal(
                    nonEmptyOption,
                    -1,
                    -1,
                    new Point(offset, 0),
                    null,
                    elementName,
                    null,
                    commentString.length() > 0 ? commentString : nonEmptyOption);
        }

        CompletionProposal defaultProposal = emptyProposal;
        CompletionProposal optionalProposal = nonEmptyProposal;

        if (nonEmptyProposal != null
            && (elementName.equals("application")
                || elementName.equals("library")
                || elementName.equals("component-specification")
                || elementName.equals("page-specifiation")
                || elementName.equals("description")))
        {
            // non empty only!
            defaultProposal = nonEmptyProposal;
            optionalProposal = null;
        } else if (nonEmptyProposal != null && elementName.equals("component"))
        {
            // non empty first!
            defaultProposal = nonEmptyProposal;
            optionalProposal = emptyProposal;
        }

        defaultProposal.setImage(Images.getSharedImage("bullet.gif"));
        if (optionalProposal != null)
        {
            optionalProposal.setImage(Images.getSharedImage("bullet_d.gif"));
        }

        MultiKey defaultKey = new MultiKey(new Object[] { dtd, elementName, DEFAULT_NEW_ELEMENT_PROPOSAL }, false);
        DTDInfoMap.put(defaultKey, defaultProposal);
        MultiKey optionalKey = new MultiKey(new Object[] { dtd, elementName, OPTIONAL_NEW_ELEMENT_PROPOSAL }, false);
        DTDInfoMap.put(optionalKey, optionalProposal);
    }

    public static String getElementComment(DTD dtd, String elementName)
    {
        return internalGetElementComment(dtd, elementName.toLowerCase());
    }

    private static String internalGetElementComment(DTD dtd, String elementName)
    {
        MultiKey key = new MultiKey(new Object[] { dtd, elementName, ELEMENT_COMMENT }, false);
        String result = (String) DTDInfoMap.get(key);
        if (result == null)
        {
            DTDElement element = (DTDElement) dtd.elements.get(elementName);
            if (element == null)
            {
                result = "";
            } else
            {
                int index = dtd.items.indexOf(element);
                if (index > 0)
                {
                    try
                    {
                        DTDComment comment = (DTDComment) dtd.items.get(index - 1);
                        result = comment.getText();
                    } catch (ClassCastException e)
                    {
                        result = "";
                    }
                }
            }
            DTDInfoMap.put(key, result);
        }
        return result;
    }
    
    public static List getAttributes(DTD dtd, String elementName) {
        return internalGetAttributes(dtd, elementName.toLowerCase());
    }

    /**
     * @param dtd
     * @param elementName assumed to be lowercase
     */
    private static List internalGetAttributes(DTD dtd, String elementName)
    {
        MultiKey key = new MultiKey(new Object[] { dtd, elementName, ELEMENT_ATTRIBUTES }, false);
        List attributes = (List) DTDInfoMap.get(key);
        if (attributes == null)
        {
            DTDElement element = (DTDElement) dtd.elements.get(elementName);
            if (element == null)
            {
                DTDInfoMap.put(key, Collections.EMPTY_LIST);
            } else
            {
                Hashtable attrTable = element.attributes;
                if (attrTable == null || attrTable.isEmpty())
                {
                    DTDInfoMap.put(key, Collections.EMPTY_LIST);
                } else
                {
                    attributes = new ArrayList();
                    List required = new ArrayList();
                    List allowedValues = null;
                    MultiKey defaultKey;
                    MultiKey allowedValuesKey;
                    for (Iterator iter = attrTable.keySet().iterator(); iter.hasNext();)
                    {
                        String attrName = (String) iter.next();
                        attributes.add(attrName);
                        DTDAttribute dtdattr = (DTDAttribute) attrTable.get(attrName);
                        String defaultValue = dtdattr.getDefaultValue();
                        if (dtdattr.decl == DTDDecl.REQUIRED)
                            required.add(attrName);

                        // find allowed and default values (if any)
                        Object type = dtdattr.type;
                        if (type instanceof DTDEnumeration)
                        {
                            DTDEnumeration enum = (DTDEnumeration) type;
                            allowedValues = new ArrayList(((DTDEnumeration) type).getItemsVec());
                        }
                        defaultKey =
                            new MultiKey(
                                new Object[] { dtd, elementName, attrName, ELEMENT_ATTRIBUTE_DEFAULT_VALUE },
                                false);
                        DTDInfoMap.put(key, defaultValue != null ? defaultValue : "");
                        allowedValuesKey =
                            new MultiKey(
                                new Object[] { dtd, elementName, attrName, ELEMENT_ATTRIBUTE_ALLOWED_VALUES },
                                false);
                        DTDInfoMap.put(
                            key,
                            (allowedValues == null || allowedValues.isEmpty())
                                ? Collections.EMPTY_LIST
                                : Collections.unmodifiableList(allowedValues));

                    }
                    DTDInfoMap.put(
                        key,
                        attributes.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(attributes));
                    MultiKey requiredKey =
                        new MultiKey(new Object[] { dtd, elementName, ELEMENT_REQUIRED_ATTRIBUTES }, false);
                    DTDInfoMap.put(
                        requiredKey,
                        required.isEmpty() ? Collections.EMPTY_LIST : Collections.unmodifiableList(required));
                }
            }
        }
        if (attributes == null || attributes.isEmpty())
            return Collections.EMPTY_LIST;

        return attributes;
    }

    /**
     * @param dtd
     * @param elementName
     * @param elementName 
     * @return
     */
    public static List getRequiredAttributes(DTD dtd, String elementName)
    {
        return internalGetRequiredAttributes(dtd, elementName.toLowerCase());
    }

    /**
     * @param dtd
     * @param elementName assumed to be lowercase
     * @return
     */
    private static List internalGetRequiredAttributes(DTD dtd, String elementName)
    {
        MultiKey key = new MultiKey(new Object[] { dtd, elementName, ELEMENT_REQUIRED_ATTRIBUTES }, false);
        List result = (List) DTDInfoMap.get(key);
        if (result == null)
        {
            internalGetAttributes(dtd, elementName);
            return (List) DTDInfoMap.get(key);
        }
        return result;
    }

    /**
     * @param dtd
     * @param elementName
     * @param elementName 
     * @return
     */
    public static String getDefaultAttributeValue(DTD dtd, String elementName, String attrName)
    {
        return internalGetDefaultAttributeValue(dtd, elementName.toLowerCase(), attrName.toLowerCase());
    }
    /**
     * @param dtd
     * @param elementName
     * @param elementName assumed to be lowercase
     * @return
     */
    private static String internalGetDefaultAttributeValue(DTD dtd, String elementName, String attrName)
    {
        MultiKey key =
            new MultiKey(new Object[] { dtd, elementName, attrName, ELEMENT_ATTRIBUTE_DEFAULT_VALUE }, false);

        String result = (String) DTDInfoMap.get(key);
        if (result == null)
        {
            internalGetAttributes(dtd, elementName);
            return (String) DTDInfoMap.get(key);
        }
        return result;
    }

    /**
     * @param dtd
     * @param elementName
     * @param elementName 
     * @return
     */
    public static List getAllowedAttributeValues(DTD dtd, String elementName, String attrName)
    {
        return internalGetAllowedAttributeValues(dtd, elementName.toLowerCase(), attrName.toLowerCase());
    }

    /**
     * @param dtd
     * @param elementName
     * @param elementName assumed to be lowercase
     * @return
     */
    private static List internalGetAllowedAttributeValues(DTD dtd, String elementName, String attrName)
    {
        MultiKey key =
            new MultiKey(new Object[] { dtd, elementName, attrName, ELEMENT_ATTRIBUTE_ALLOWED_VALUES }, false);
        List result = (List) DTDInfoMap.get(key);
        if (result == null)
        {
            internalGetAttributes(dtd, elementName);
            result = (List) DTDInfoMap.get(key);
        }

        return result;
    }

    /**
     * Return the default ICompletionProposal for inserting an XML Comment.
     * <pre>
     *  <!--  -->
     * </pre>
     * The cursor position after the proposal is applied is in the middle.
     * 
     * @param replacementOffset the location in the document where the proposal will be applied
     * @param replacementLength the number of characters in the document from replacementOffset that will be replaced.
     * @return
     */
    public static ICompletionProposal getDefaultInsertCommentProposal(int replacementOffset, int replacementLength)
    {
        CompletionProposal proposal =
            new CompletionProposal(
                "<!--  -->",
                replacementOffset,
                replacementLength,
                new Point(5, 0),
                Images.getSharedImage("bullet_d.gif"),
                "Insert comment",
                null,
                null);
        proposal.setYOrder(99);
        return proposal;
    }
}
