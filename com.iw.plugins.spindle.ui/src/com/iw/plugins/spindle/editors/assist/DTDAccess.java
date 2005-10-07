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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
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


import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDAttribute;
import com.wutka.dtd.DTDComment;
import com.wutka.dtd.DTDDecl;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDEnumeration;
import com.wutka.dtd.DTDItem;
import com.wutka.dtd.DTDSequence;

import core.CoreMessages;
import core.util.Assert;

/**
 * Helper class for accessing DTD information
 * 
 * @author glongman@gmail.com
 */
public class DTDAccess
{

  private static final String ELEMENT_ATTRIBUTE_NAMES = "ELEMENT_ATTRIBUTE_NAMES";
  private static final String ELEMENT_REQUIRED_ATTRIBUTE_NAMES = "ELEMENT_REQUIRED_ATTRIBUTE_NAMES";
  private static final String ELEMENT_ATTRIBUTE_DEFAULT_VALUE = "ELEMENT_ATTRIBUTE_DEFAULT_VALUE";
  private static final String ELEMENT_ATTRIBUTE_ALLOWED_VALUES = "ELEMENT_ATTRIBUTE_ALLOWED_VALUES";
  private static final String ELEMENT_COMMENT = "ELEMENT_ATTRIBUTE_ALLOWED_VALUES";
  private static final String ALL_ELEMENTS = "ALL_ELEMENTS";
  //  private static final String ALL_ELEMENTS_BY_ROOT = "ALL_ELEMENTS_BY_ROOT";

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
  public static String getAllowedElements(DTD dtd, String elementName) {
    return internalGetAllowedElements(dtd, elementName);
  }

  
  private static String internalGetAllowedElements(DTD dtd, String elementName)
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

  public static List getAttributeNames(DTD dtd, String elementName)
  {
    return internalGetAttributeNames(dtd, elementName.toLowerCase());
  }

  private static List internalGetAttributeNames(DTD dtd, String elementName)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    MultiKey key = new MultiKey(
        new Object[]{dtd, elementName, ELEMENT_ATTRIBUTE_NAMES},
        false);
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
              defaultValue = internalGetTapestryDefaultValue(dtd, elementName, attrName);
            if (defaultValue == null)
                defaultValue = "";
            if (dtdattr.decl == DTDDecl.REQUIRED)
              required.add(attrName);
            // find allowed and default values (if any)
            Object type = dtdattr.type;
            if (type instanceof DTDEnumeration)
            {
              DTDEnumeration enumeration = (DTDEnumeration) type;
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
              ELEMENT_REQUIRED_ATTRIBUTE_NAMES}, false);
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
          result = CoreMessages.format("TapestryEngine.defaultEngine");
      } else if ("component-specification".equals(elementName))
      {
        if ("class".equals(attrName))
          result = CoreMessages.format("TapestryComponentSpec.defaultSpec");
      } else if ("page-specification".equals(elementName))
      {
        if ("class".equals(attrName))
          result = CoreMessages.format("TapestryPageSpec.defaultSpec");
      }
    }

    return result;
  }

  public static boolean isTapestryDTD(DTD dtd)
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
        ELEMENT_REQUIRED_ATTRIBUTE_NAMES}, false);
    List result = (List) DTDInfoMap.get(key);
    if (result == null)
    {
      internalGetAttributeNames(dtd, elementName);
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
      internalGetAttributeNames(dtd, elementName);
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
      internalGetAttributeNames(dtd, elementName);
      result = (List) DTDInfoMap.get(key);
    }

    return result;
  }

  static List getAllElementProposals(
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
      result.addAll(ProposalFactory.internalGetNewElementCompletionProposals(
          document,
          completionOffset,
          completionLength,
          dtd,
          elementName));
    }
    return result;
  }

}