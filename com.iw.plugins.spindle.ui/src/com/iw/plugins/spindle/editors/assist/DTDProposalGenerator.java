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

package com.iw.plugins.spindle.editors.assist;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.tapestry.util.MultiKey;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.graphics.Image;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.util.Assert;
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
 * Helper class for creating and accessing DTD based completion proposals
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: DTDProposalGenerator.java,v 1.2 2003/12/13 14:43:12 glongman
 *                     Exp $
 */
public class DTDProposalGenerator
{

  static public class ElementInfo
  {
    public String elementName;
    public boolean empty;
    public String[][] attrvalues;
    public String comment;
    public Image image; // not copied!
    public int order; //not copied!
    public int totalAttrCount;
    ElementInfo copy()
    {
      ElementInfo result = new ElementInfo();
      result.elementName = elementName;
      result.empty = empty;
      result.attrvalues = attrvalues;
      result.comment = comment;
      result.totalAttrCount = totalAttrCount;
      return result;
    }
    public OrderedProposal generate(
        IDocument document,
        int completionOffset,
        int completionLength)
    {
      return ProposalFactory.getTagProposal(
          document,
          completionOffset,
          completionLength,
          this);
    }
  }

  private static final String DEFAULT_NEW_ELEMENT_INFO = "DEFAULT_NEW_ELEMENT_INFO";
  private static final String OPTIONAL_NEW_ELEMENT_INFO = "OPTIONAL_NEW_ELEMENT_INFO";

  private static final String ELEMENT_ATTRIBUTES = "ELEMENT_ATTRIBUTES";
  private static final String ELEMENT_REQUIRED_ATTRIBUTES = "ELEMENT_REQUIRED_ATTRIBUTES";
  private static final String ELEMENT_ATTRIBUTE_DEFAULT_VALUE = "ELEMENT_ATTRIBUTE_DEFAULT_VALUE";
  private static final String ELEMENT_ATTRIBUTE_ALLOWED_VALUES = "ELEMENT_ATTRIBUTE_ALLOWED_VALUES";
  private static final String ELEMENT_COMMENT = "ELEMENT_ATTRIBUTE_ALLOWED_VALUES";
  private static final String ALL_ELEMENTS = "ALL_ELEMENTS";
  private static final String ALL_ELEMENTS_BY_ROOT = "ALL_ELEMENTS_BY_ROOT";

  /**
   * Map caching the results of all of the lookup methods in this class
   */
  static private final Map DTDInfoMap = new HashMap();

  static public void reset()
  {
    DTDInfoMap.clear();
  }

  /**
   * Examine a DTD and return the names all of the children of a parent. The
   * string returned is in DTD source format.
   * 
   * Results are cached
   * 
   * @param dtd the DTD we are basing the lookup on
   * @param elementName the element whose children we are seeking.
   * @return a String containing the child names, or null if the elementName is
   *                 not a valid element in the DTDs
   */
  private static String getAllowedElements(DTD dtd, String elementName)
  {
    Assert.isLegal(dtd != null);

    String useName = elementName.toLowerCase();
    MultiKey key = new MultiKey(new Object[]{dtd, useName}, false);

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

  private static List internalGetAllElementNames(DTD dtd)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;
    MultiKey key = new MultiKey(new Object[]{dtd, ALL_ELEMENTS}, false);

    List result = (List) DTDInfoMap.get(key);

    if (result == null)
    {
      result = new ArrayList();
      for (Iterator iter = dtd.elements.keySet().iterator(); iter.hasNext();)
      {
        String name = (String) iter.next();
        result.add(name);
      }
      result = Collections.unmodifiableList(result);
      DTDInfoMap.put(key, result);
    }
    return result;
  }

  /**
   * Return all of the element names allowed by the DTD at a point in the
   * document after the last child of a given parent element.
   * 
   * Results are cached.
   * 
   * @param dtd the DTD in use by the document
   * @param parentElement the name of the element that is parent
   * @param lastChild the name of the last child of the parent before the place
   *                     we are interested
   * @param sort if true place the last child at the front of a sorted list of
   *                     elementNames
   * @return list of String
   */
  public static List getAllowedChildren(
      DTD dtd,
      String parentElement,
      String lastChild,
      boolean sort)
  {
    Assert.isLegal(dtd != null);

    String useParent = parentElement.toLowerCase();
    String useChild = lastChild == null ? "~NULL_CHILD" : lastChild.toLowerCase();
    MultiKey key = new MultiKey(
        new Object[]{dtd, useParent, useChild, new Boolean(sort)},
        false);

    List result = (List) DTDInfoMap.get(key);

    if (result == null)
    {

      MultiKey nonSorted = new MultiKey(new Object[]{dtd, useParent, useChild,
          new Boolean(false)}, false);
      MultiKey sorted = new MultiKey(new Object[]{dtd, useParent, useChild,
          new Boolean(true)}, false);
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
      StringTokenizer tok = new StringTokenizer(allowed, "(),*| ?+");
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
        if (!useChild.equals("~NULL_CHILD"))
        {
          result.remove(useChild);
          result.add(0, useChild);
        }
        DTDInfoMap.put(nonSorted, Collections.unmodifiableList(result));
        //sort
        TreeSet set = new TreeSet(result);
        if (!useChild.equals("~NULL_CHILD"))
          set.remove(useChild);
        ArrayList sortedList = new ArrayList(set);
        if (!useChild.equals("~NULL_CHILD"))
          sortedList.add(0, useChild);
        DTDInfoMap.put(sorted, Collections.unmodifiableList(sortedList));
        result = (List) DTDInfoMap.get(key);

      }
    }

    return result;
  }

  public static List getNewElementCompletionProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      String elementName)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    return internalGetNewElementCompletionProposals(
        document,
        completionLength,
        completionLength,
        dtd,
        elementName);
  }

  private static List internalGetNewElementCompletionProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      String elementName)
  {
    Assert.isLegal(dtd != null);
    Assert.isLegal(elementName != null);

    String useName = elementName.toLowerCase();
    MultiKey defaultKey = new MultiKey(new Object[]{dtd, useName,
        DEFAULT_NEW_ELEMENT_INFO}, false);
    ElementInfo defaultProposal = (ElementInfo) DTDInfoMap.get(defaultKey);
    if (defaultProposal == null)
    {
      computeNewElementProposalInfos(dtd, useName);
    }
    defaultProposal = (ElementInfo) DTDInfoMap.get(defaultKey);
    if (defaultProposal == null)
      return Collections.EMPTY_LIST;

    ArrayList result = new ArrayList();
    result.add(defaultProposal.generate(document, completionOffset, completionLength));

    MultiKey optionalKey = new MultiKey(new Object[]{dtd, useName,
        OPTIONAL_NEW_ELEMENT_INFO}, false);
    ElementInfo optionalProposal = (ElementInfo) DTDInfoMap.get(optionalKey);

    if (optionalProposal != null)
      result.add(optionalProposal.generate(document, completionOffset, completionLength));

    return result;
  }

  private static void computeNewElementProposalInfos(DTD dtd, String elementName)
  {
    Assert.isLegal(dtd != null);

    DTDElement element = (DTDElement) dtd.elements.get(elementName);

    if (element == null)
      return;

    ElementInfo emptyInfo = new ElementInfo();
    emptyInfo.elementName = elementName;
    emptyInfo.attrvalues = new String[0][];
    emptyInfo.empty = true;
    emptyInfo.comment = internalGetElementComment(dtd, elementName);

    boolean allowsChildren = element.getContent().getItemType() != DTDItemType.DTD_EMPTY;
    emptyInfo.totalAttrCount = internalGetAttributes(dtd, elementName).size();
    boolean hasAttibutes = emptyInfo.totalAttrCount != 0;

    if (hasAttibutes)
    {
      List requiredAttributes = internalGetRequiredAttributes(dtd, elementName);
      if (!requiredAttributes.isEmpty())
      {
        String[][] attrValues = new String[requiredAttributes.size()][];
        int i = 0;
        for (Iterator iter = requiredAttributes.iterator(); iter.hasNext();)
        {
          String attrName = (String) iter.next();
          String defaultValue = internalGetDefaultAttributeValue(
              dtd,
              elementName,
              attrName);
          attrValues[i++] = new String[]{attrName,
              defaultValue == "" ? null : defaultValue};
        }
        emptyInfo.attrvalues = attrValues;
      }
    }

    ElementInfo nonEmptyInfo = null;
    if (allowsChildren)
    {
      nonEmptyInfo = emptyInfo.copy();
      nonEmptyInfo.empty = false;
    }

    ElementInfo defaultInfo = emptyInfo;
    ElementInfo optionalInfo = nonEmptyInfo;

    if (isTapestryDTD(dtd))
    {
      if (nonEmptyInfo != null
          && (elementName.equals("application") || elementName.equals("library")
              || elementName.equals("component-specification")
              || elementName.equals("page-specification")
              || elementName.equals("description") || elementName.equals("extension")))
      {
        // non empty only!
        defaultInfo = nonEmptyInfo;
        optionalInfo = null;
      } else if (nonEmptyInfo != null
          && (elementName.equals("component") || elementName.equals("listener-binding")))
      { // non empty first!
        defaultInfo = nonEmptyInfo;
        optionalInfo = emptyInfo;
      }
      defaultInfo.image = Images.getSharedImage("bullet.gif");
      if (optionalInfo != null)
        optionalInfo.image = Images.getSharedImage("bullet_d.gif");

    } else
    {
      //          This is for the XHTML strict dtd
      if (nonEmptyInfo != null)
      {
        defaultInfo = nonEmptyInfo;
        optionalInfo = emptyInfo;
      }
      defaultInfo.order = 100;
      defaultInfo.image = Images.getSharedImage("bullet_web.gif");
      if (optionalInfo != null)
      {
        optionalInfo.order = 101;
        optionalInfo.image = Images.getSharedImage("bullet_web.gif");
      }

    }

    MultiKey defaultKey = new MultiKey(new Object[]{dtd, elementName,
        DEFAULT_NEW_ELEMENT_INFO}, false);
    DTDInfoMap.put(defaultKey, defaultInfo);
    MultiKey optionalKey = new MultiKey(new Object[]{dtd, elementName,
        OPTIONAL_NEW_ELEMENT_INFO}, false);
    DTDInfoMap.put(optionalKey, optionalInfo);
  }
  //  /**
  //   * @deprecated
  //   * @param dtd
  //   * @param elementName
  //   */
  //  private static void computeNewElementProposals(DTD dtd, String elementName)
  //  {
  //    Assert.isLegal(dtd != null);
  //
  //    DTDElement element = (DTDElement) dtd.elements.get(elementName);
  //
  //    if (element == null)
  //      return;
  //
  //    String commentString = internalGetElementComment(dtd, elementName);
  //
  //    String emptyOption = null;
  //    String nonEmptyOption = null;
  //    String prelude = "<" + elementName;
  //
  //    DTDItem content = element.getContent();
  //
  //    boolean allowsChildren = content.getItemType() != DTDItemType.DTD_EMPTY;
  //
  //    int offset = -1;
  //    CompletionProposal emptyProposal;
  //    CompletionProposal nonEmptyProposal = null;
  //
  //    boolean hasAttibutes = !internalGetAttributes(dtd, elementName).isEmpty();
  //    if (hasAttibutes)
  //    {
  //      List requiredAttributes = internalGetRequiredAttributes(dtd, elementName);
  //      if (!requiredAttributes.isEmpty())
  //      {
  //        prelude += " ";
  //        for (Iterator iter = requiredAttributes.iterator(); iter.hasNext();)
  //        {
  //          String attrName = (String) iter.next();
  //          prelude += attrName + "=\"\"";
  //          if (offset == -1)
  //            offset = prelude.length() - 1;
  //          if (iter.hasNext())
  //            prelude += " ";
  //        }
  //      }
  //    }
  //
  //    emptyOption = prelude + "/>";
  //
  //    if (offset == -1 && hasAttibutes)
  //    {
  //      offset = 1 + elementName.length();
  //    } else if (offset == -1)
  //    {
  //      offset = emptyOption.length();
  //    }
  //
  //    if (allowsChildren)
  //    {
  //      nonEmptyOption = prelude + "></" + elementName + ">";
  //    }
  //
  //    emptyProposal = new CompletionProposal(
  //        emptyOption,
  //        -1,
  //        -1,
  //        new Point(offset, 0),
  //        null,
  //        elementName + " (empty)",
  //        null,
  //        commentString != null && commentString.length() > 0 ? commentString :
  // emptyOption);
  //
  //    if (nonEmptyOption != null)
  //    {
  //      if (!hasAttibutes)
  //        offset -= 1;
  //
  //      nonEmptyProposal = new CompletionProposal(nonEmptyOption, -1, -1, new
  // Point(
  //          offset,
  //          0), null, elementName, null, commentString != null
  //          && commentString.length() > 0 ? commentString : nonEmptyOption);
  //    }
  //
  //    CompletionProposal defaultProposal = emptyProposal;
  //    CompletionProposal optionalProposal = nonEmptyProposal;
  //
  //    if (isTapestryDTD(dtd))
  //    {
  //      if (nonEmptyProposal != null
  //          && (elementName.equals("application") || elementName.equals("library")
  //              || elementName.equals("component-specification")
  //              || elementName.equals("page-specification")
  //              || elementName.equals("description") || elementName.equals("extension")))
  //      {
  //        // non empty only!
  //        defaultProposal = nonEmptyProposal;
  //        optionalProposal = null;
  //      } else if (nonEmptyProposal != null
  //          && (elementName.equals("component") ||
  // elementName.equals("listener-binding")))
  //      { // non empty first!
  //        defaultProposal = nonEmptyProposal;
  //        optionalProposal = emptyProposal;
  //      }
  //      defaultProposal.setImage(Images.getSharedImage("bullet.gif"));
  //      if (optionalProposal != null)
  //        optionalProposal.setImage(Images.getSharedImage("bullet_d.gif"));
  //
  //    } else
  //    {
  //      // This is for the XHTML strict dtd
  //      if (nonEmptyProposal != null)
  //      {
  //        defaultProposal = nonEmptyProposal;
  //        optionalProposal = emptyProposal;
  //      }
  //      defaultProposal.setYOrder(100);
  //      defaultProposal.setImage(Images.getSharedImage("bullet_web.gif"));
  //      if (optionalProposal != null)
  //      {
  //        optionalProposal.setYOrder(101);
  //        optionalProposal.setImage(Images.getSharedImage("bullet_web.gif"));
  //      }
  //
  //    }
  //
  //    MultiKey defaultKey = new MultiKey(new Object[]{dtd, elementName,
  //        DEFAULT_NEW_ELEMENT_PROPOSAL}, false);
  //    DTDInfoMap.put(defaultKey, defaultProposal);
  //    MultiKey optionalKey = new MultiKey(new Object[]{dtd, elementName,
  //        OPTIONAL_NEW_ELEMENT_PROPOSAL}, false);
  //    DTDInfoMap.put(optionalKey, optionalProposal);
  //  }

  public static String getElementComment(DTD dtd, String elementName)
  {
    return internalGetElementComment(dtd, elementName.toLowerCase());
  }

  private static String internalGetElementComment(DTD dtd, String elementName)
  {
    if (!isTapestryDTD(dtd))
      return null;

    MultiKey key = new MultiKey(new Object[]{dtd, elementName, ELEMENT_COMMENT}, false);
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

  public static List getAttributes(DTD dtd, String elementName)
  {
    return internalGetAttributes(dtd, elementName.toLowerCase());
  }

  private static List internalGetAttributes(DTD dtd, String elementName)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    MultiKey key = new MultiKey(new Object[]{dtd, elementName, ELEMENT_ATTRIBUTES}, false);
    List attributes = (List) DTDInfoMap.get(key);
    if (attributes == null)
    {
      DTDElement element = (DTDElement) dtd.elements.get(elementName);
      if (element == null)
      {
        DTDInfoMap.put(key, Collections.EMPTY_LIST);
      } else
      {
        Map attrTable = element.attributes;
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
            if (defaultValue == null)
              defaultValue = getTapestryDefaultValue(dtd, elementName, attrName);
            if (dtdattr.decl == DTDDecl.REQUIRED)
              required.add(attrName);
            // find allowed and default values (if any)
            Object type = dtdattr.type;
            if (type instanceof DTDEnumeration)
            {
              DTDEnumeration enum = (DTDEnumeration) type;
              allowedValues = new ArrayList(((DTDEnumeration) type).getItemsVec());
            } else
            {
              allowedValues = null;
            }
            defaultKey = new MultiKey(new Object[]{dtd, elementName, attrName,
                ELEMENT_ATTRIBUTE_DEFAULT_VALUE}, false);
            DTDInfoMap.put(defaultKey, defaultValue != null ? defaultValue : "");
            allowedValuesKey = new MultiKey(new Object[]{dtd, elementName, attrName,
                ELEMENT_ATTRIBUTE_ALLOWED_VALUES}, false);
            DTDInfoMap.put(allowedValuesKey, (allowedValues == null || allowedValues
                .isEmpty()) ? Collections.EMPTY_LIST : Collections
                .unmodifiableList(allowedValues));
          }
          DTDInfoMap.put(key, attributes.isEmpty() ? Collections.EMPTY_LIST : Collections
              .unmodifiableList(attributes));
          MultiKey requiredKey = new MultiKey(new Object[]{dtd, elementName,
              ELEMENT_REQUIRED_ATTRIBUTES}, false);
          DTDInfoMap.put(requiredKey, required.isEmpty()
              ? Collections.EMPTY_LIST : Collections.unmodifiableList(required));
        }
      }
    }
    if (attributes == null || attributes.isEmpty())
      return Collections.EMPTY_LIST;
    return attributes;
  }

  public static String getTapestryDefaultValue(
      DTD dtd,
      String elementName,
      String attrName)
  {
    return internalGetTapestryDefaultValue(dtd, elementName.toLowerCase(), attrName
        .toLowerCase());
  }

  private static String internalGetTapestryDefaultValue(
      DTD dtd,
      String elementName,
      String attrName)
  {
    String result = null;
    if (isTapestryDTD(dtd))
    {
      if ("application".equals(elementName))
      {
        if ("engine-class".equals(attrName))
          result = TapestryCore.getString("TapestryEngine.defaultEngine");
      } else if ("component-specification".equals(elementName))
      {
        if ("class".equals(attrName))
          result = TapestryCore.getString("TapestryComponentSpec.defaultSpec");
      } else if ("page-specification".equals(elementName))
      {
        if ("class".equals(attrName))
          result = TapestryCore.getString("TapestryPageSpec.defaultSpec");
      }
    }

    return result;
  }

  private static boolean isTapestryDTD(DTD dtd)
  {
    return dtd != null && dtd.getPublicId() != null;
  }

  public static List getRequiredAttributes(DTD dtd, String elementName)
  {
    return internalGetRequiredAttributes(dtd, elementName.toLowerCase());
  }

  private static List internalGetRequiredAttributes(DTD dtd, String elementName)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    MultiKey key = new MultiKey(new Object[]{dtd, elementName,
        ELEMENT_REQUIRED_ATTRIBUTES}, false);
    List result = (List) DTDInfoMap.get(key);
    if (result == null)
    {
      internalGetAttributes(dtd, elementName);
      return (List) DTDInfoMap.get(key);
    }
    return result;
  }

  public static String getDefaultAttributeValue(
      DTD dtd,
      String elementName,
      String attrName)
  {
    return internalGetDefaultAttributeValue(dtd, elementName.toLowerCase(), attrName
        .toLowerCase());
  }

  private static String internalGetDefaultAttributeValue(
      DTD dtd,
      String elementName,
      String attrName)
  {
    if (dtd == null)
      return null;

    MultiKey key = new MultiKey(new Object[]{dtd, elementName, attrName,
        ELEMENT_ATTRIBUTE_DEFAULT_VALUE}, false);
    String result = (String) DTDInfoMap.get(key);
    if (result == null)
    {
      internalGetAttributes(dtd, elementName);
      return (String) DTDInfoMap.get(key);
    }
    return result;
  }

  public static List getAllowedAttributeValues(
      DTD dtd,
      String elementName,
      String attrName)
  {
    return internalGetAllowedAttributeValues(dtd, elementName.toLowerCase(), attrName
        .toLowerCase());
  }

  private static List internalGetAllowedAttributeValues(
      DTD dtd,
      String elementName,
      String attrName)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;
    MultiKey key = new MultiKey(new Object[]{dtd, elementName, attrName,
        ELEMENT_ATTRIBUTE_ALLOWED_VALUES}, false);
    List result = (List) DTDInfoMap.get(key);
    if (result == null)
    {
      internalGetAttributes(dtd, elementName);
      result = (List) DTDInfoMap.get(key);
    }

    return result;
  }

  public static List findRawNewTagProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      XMLNode artifact)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    XMLNode parent = artifact.getParent();
    XMLNode previousSibling = null;

    if (parent != null)
    {
      if (parent.getType().equals("/"))
      {
        String rootNode = parent.rootNodeId;
        if (rootNode != null)
          return internalGetNewElementCompletionProposals(
              document,
              completionOffset,
              completionLength,
              dtd,
              rootNode);

      } else
      {
        boolean lookupPreviousSib = artifact.getCorrespondingNode() != parent;
        String parentName = parent.getName();
        if (parentName != null)
        {
          String parentAllowedContent = getAllowedElements(dtd, parentName);
          if (parentAllowedContent != null)
          {
            String sibName = null;
            if (lookupPreviousSib)
              previousSibling = artifact.getPreviousSiblingTag(parentAllowedContent);
            if (previousSibling != null)
              sibName = previousSibling.getName();
            return getRawNewTagProposals(
                document,
                completionOffset,
                completionLength,
                dtd,
                parentName,
                sibName);
          }
        }
      }
    }
    return Collections.EMPTY_LIST;

  }

  public static List getRawNewTagProposalsSimple(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      XMLNode artifact)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    XMLNode parent = artifact.getParent();
    if (parent == null || parent.getType().equals("/") || parent.getName() == null)
      return getAllElementProposals(document, completionOffset, completionLength, dtd);

    return getRawNewTagProposals(
        document,
        completionLength,
        completionLength,
        dtd,
        parent.getName(),
        null);
  }

  public static List getRawNewTagProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      String parentName,
      String sibName)
  {
    List allowedChildren = getAllowedChildren(dtd, parentName, sibName, false);
    List result = new ArrayList();
    for (Iterator iter = allowedChildren.iterator(); iter.hasNext();)
    {
      String tagName = (String) iter.next();
      result.addAll(internalGetNewElementCompletionProposals(
          document,
          completionOffset,
          completionLength,
          dtd,
          tagName));

    }
    return result;
  }

  private static List getAllElementProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;
    List allElements = internalGetAllElementNames(dtd);
    List result = new ArrayList();
    for (Iterator iter = allElements.iterator(); iter.hasNext();)
    {
      String elementName = (String) iter.next();
      result.addAll(internalGetNewElementCompletionProposals(
          document,
          completionOffset,
          completionLength,
          dtd,
          elementName));
    }
    return result;
  }

}