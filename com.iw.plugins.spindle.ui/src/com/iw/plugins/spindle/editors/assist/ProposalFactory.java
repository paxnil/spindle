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
 * Portions created by the Initial Developer are Copyright (C) 2004
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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.tapestry.util.MultiKey;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.link.LinkedModeUI;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.TemplateContext;
import org.eclipse.swt.graphics.Image;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.util.Assert;
import com.wutka.dtd.DTD;
import com.wutka.dtd.DTDElement;
import com.wutka.dtd.DTDItemType;

/**
 * ProposalFactory factory methods for common proposals.
 * 
 * Also provides proposal for element tags and element attributes from a DTD
 * 
 * DTD base proposal information is cached.
 * 
 * @author glongman@gmail.com
 *  
 */
public class ProposalFactory
{

  static class ElementProposalInfo
  {
    public String elementName;
    public boolean empty;
    public String[][] attrvalues;
    public String comment;
    public Image image; // not copied!
    public int order; //not copied!
    public int totalAttrCount;
    ElementProposalInfo copy()
    {
      ElementProposalInfo result = new ElementProposalInfo();
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
      return ProposalFactory.createTagProposal(
          document,
          completionOffset,
          completionLength,
          this);
    }
  }

  static public class AttributeProposalInfo
  {
    public String attributeName;
    public String defaultValue;
    public String comment;
    public Image image;
    public int order;
    public boolean required;
  }

  public static final Comparator PROPOSAL_COMPARATOR = new Comparator()
  {
    String s1;
    String s2;
    int z1;
    int z2;
    public int compare(Object o1, Object o2)
    {

      ICompletionProposal p1 = (ICompletionProposal) o1;
      ICompletionProposal p2 = (ICompletionProposal) o2;

      s1 = p1.getDisplayString();
      s2 = p2.getDisplayString();

      z1 = z2 = 0;

      if (p1 instanceof OrderedProposal)
        z1 = ((OrderedProposal) p1).getYOrder();

      if (p2 instanceof OrderedProposal)
        z2 = ((OrderedProposal) p2).getYOrder();

      return (z1 == z2) ? s1.compareTo(s2) : (z1 < z2 ? -1 : 1);
    }
  };

  /* cached proposal information */
  private static final String ELEMENT_ATTR_INFOS = "ELEMENT_ATTR_INFOS";
  private static final String ELEMENT_ATTR_INFOS_REQUIRED = "ELEMENT_ATTR_INFOS_REQUIRED";
  private static final String DEFAULT_NEW_ELEMENT_INFO = "DEFAULT_NEW_ELEMENT_INFO";
  private static final String OPTIONAL_NEW_ELEMENT_INFO = "OPTIONAL_NEW_ELEMENT_INFO";

  static private final Map ProposalInfoCache = new HashMap();

  /** @deprecated */
  public static final String DEFAULT_ATTR_VALUE = "";

  public static OrderedProposal createTemplateProposal(
      Template template,
      TemplateContext context,
      Region region,
      Image image,
      String extraInfo,
      int yOrder,
      Object cyclingMode)
  {

    if (image == null)
      image = Images.getSharedImage("bullet.gif");

    SpindleTemplateProposal result = new SpindleTemplateProposal(
        template,
        context,
        region,
        extraInfo,
        image,
        yOrder);

    result.setCyclingMode(cyclingMode);

    return result;
  }

  public static OrderedProposal createTagProposal(
      IDocument document,
      int completionOffset,
      int completionLength,
      ElementProposalInfo info)
  {
    TagTemplateContext context = new TagTemplateContext(
        document,
        completionOffset,
        completionLength,
        info);

    return createTemplateProposal(
        context.getTemplate(),
        context,
        new Region(completionOffset, completionLength),
        info.image,
        info.comment,
        info.order,
        LinkedModeUI.CYCLE_ALWAYS);
  }

  static OrderedProposal createElementAttributeProposal(
      IDocument document,
      int completionOffset,
      int completionLength,
      boolean addLeadingSpace,
      AttributeProposalInfo info)
  {
    return createElementAttributeProposal(
        document,
        info.attributeName,
        completionOffset,
        completionLength,
        addLeadingSpace,
        info.image,
        info.comment,
        info.order);
  }

  public static OrderedProposal createElementAttributeProposal(
      IDocument document,
      String attributeName,
      int completionOffset,
      int completionLength,
      boolean addLeadingSpace,
      Image image,
      String extraInfo,
      int yOrder)
  {
    AttributeTemplateContext context;
    context = new AttributeTemplateContext(
        document,
        completionOffset,
        completionLength,
        addLeadingSpace);
    context.setAttributeName(attributeName);

    return createTemplateProposal(context.getTemplate(), context, new Region(
        completionOffset,
        completionLength), image, extraInfo, yOrder, LinkedModeUI.CYCLE_NEVER);
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

    return ProposalFactory.internalGetNewElementCompletionProposals(
        document,
        completionLength,
        completionLength,
        dtd,
        elementName);
  }

  public static List getRawNewTagProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      XMLNode artifact)
  {
    return getRawNewTagProposals(
        document,
        completionOffset,
        completionLength,
        dtd,
        artifact,
        true);
  }

  public static List getRawNewTagProposals(
      IDocument document,
      int completionOffset,
      int completionLength,
      DTD dtd,
      XMLNode artifact,
      boolean seekParentNode)
  {
    if (dtd == null)
      return Collections.EMPTY_LIST;

    XMLNode parent;
    if (seekParentNode)
    {
      parent = artifact.getParent();
    } else
    {
      parent = artifact;
    }

    if (parent == null || parent.getType().equals("/") || parent.getName() == null)
      return DTDAccess.getAllElementProposals(
          document,
          completionOffset,
          completionLength,
          dtd);

    return getRawNewTagProposals(
        document,
        completionOffset,
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
    List allowedChildren = DTDAccess.getAllowedChildren(dtd, parentName, sibName, false);
    List result = new ArrayList();
    for (Iterator iter = allowedChildren.iterator(); iter.hasNext();)
    {
      String tagName = (String) iter.next();
      result.addAll(ProposalFactory.internalGetNewElementCompletionProposals(
          document,
          completionOffset,
          completionLength,
          dtd,
          tagName));

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
          return ProposalFactory.internalGetNewElementCompletionProposals(
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
          String parentAllowedContent = DTDAccess.getAllowedElements(dtd, parentName);
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

  public static List getAttributeProposals(
      DTD dtd,
      IDocument document,
      int completionOffset,
      int completionLength,
      String tagName,
      List excludeNames,
      HashSet existingAttributeNames,
      String prefix, //better be lowercase!
      boolean addLeadingSpace)
  {

    if (dtd != null || tagName != null)
      return Collections.EMPTY_LIST;

    List allInfos = getNewAttributeProposalInfos(dtd, tagName);

    if (allInfos.isEmpty())
      return Collections.EMPTY_LIST;

    List result = new ArrayList();

    boolean ignorePrefix = prefix == null || prefix.trim().length() == 0;

    for (Iterator iter = allInfos.iterator(); iter.hasNext();)
    {
      AttributeProposalInfo info = (AttributeProposalInfo) iter.next();

      if (existingAttributeNames.contains(info.attributeName))
        continue;

      boolean match = true;
      if (!ignorePrefix)
        match = info.attributeName.startsWith(prefix);

      if (match && !excludeNames.contains(info.attributeName)
          && !existingAttributeNames.contains(info.attributeName))
      {
        result.add(createElementAttributeProposal(
            document,
            completionOffset,
            completionLength,
            addLeadingSpace,
            info));

        excludeNames.add(info.attributeName);
      }
    }

    //    
    //    
    //
    //    List dtdAttributeNames;
    //
    //    if (dtd != null && tagName != null)
    //    {
    //      dtdAttributeNames = DTDAccess.getAttributeNames(dtd, tagName);
    //    } else
    //    {
    //      return Collections.EMPTY_LIST;
    //    }
    //
    //    List result = new ArrayList();
    //
    //    boolean ignorePrefix = prefix == null || prefix.trim().length() == 0;
    //
    //    for (Iterator iter = dtdAttributeNames.iterator(); iter.hasNext();)
    //    {
    //      String attrname = ((String) iter.next()).toLowerCase();
    //
    //      if (existingAttributeNames.contains(attrname))
    //        continue;
    //
    //      boolean match = true;
    //      if (!ignorePrefix)
    //        match = attrname.startsWith(prefix);
    //
    //      List requiredAttributes = DTDAccess.getRequiredAttributes(dtd, tagName);
    //
    //      if (match && !excludeNames.contains(attrname))
    //      {
    //        result.add(ProposalFactory.createElementAttributeProposal(
    //            document,
    //            attrname,
    //            completionOffset,
    //            completionLength,
    //            addLeadingSpace,
    //            requiredAttributes.contains(attrname) ? Images
    //                .getSharedImage("bullet_pink.gif") : Images
    //                .getSharedImage("bullet_web.gif"),
    //            null,
    //            requiredAttributes.contains(attrname) ? 98 : 99));
    //      }
    //    }
    return result;
  }

  /**
   * @see getNewElementCompletionProposals
   */
  static List internalGetNewElementCompletionProposals(
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
    ElementProposalInfo defaultProposal = (ElementProposalInfo) ProposalInfoCache
        .get(defaultKey);
    if (defaultProposal == null)
    {
      computeNewElementProposalInfos(dtd, useName);
    }
    defaultProposal = (ElementProposalInfo) ProposalInfoCache.get(defaultKey);
    if (defaultProposal == null)
      return Collections.EMPTY_LIST;

    ArrayList result = new ArrayList();
    result.add(defaultProposal.generate(document, completionOffset, completionLength));

    MultiKey optionalKey = new MultiKey(new Object[]{dtd, useName,
        OPTIONAL_NEW_ELEMENT_INFO}, false);
    ElementProposalInfo optionalProposal = (ElementProposalInfo) ProposalInfoCache
        .get(optionalKey);

    if (optionalProposal != null)
      result.add(optionalProposal.generate(document, completionOffset, completionLength));

    return result;
  }

  static List getNewAttributeProposalInfosRequired(DTD dtd, String elementName)
  {
    Assert.isLegal(dtd != null);
    Assert.isLegal(elementName != null);

    MultiKey key = new MultiKey(new Object[]{dtd, elementName,
        ELEMENT_ATTR_INFOS_REQUIRED}, false);
    List result = (List) ProposalInfoCache.get(key);

    if (result == null)
      computeNewAttributeProposalInfos(dtd, elementName);

    result = (List) ProposalInfoCache.get(key);

    return result;
  }

  static List getNewAttributeProposalInfos(DTD dtd, String elementName)
  {
    Assert.isLegal(dtd != null);
    Assert.isLegal(elementName != null);

    MultiKey key = new MultiKey(new Object[]{dtd, elementName, ELEMENT_ATTR_INFOS}, false);
    List result = (List) ProposalInfoCache.get(key);

    if (result == null)
      computeNewAttributeProposalInfos(dtd, elementName);

    result = (List) ProposalInfoCache.get(key);

    return result;
  }

  private static void computeNewAttributeProposalInfos(DTD dtd, String elementName)
  {
    Assert.isLegal(dtd != null);

    DTDElement element = (DTDElement) dtd.elements.get(elementName);

    if (element == null)
      return;

    List attributes = DTDAccess.getAttributeNames(dtd, elementName);

    if (attributes.isEmpty())
      return;

    List requiredAttributes = DTDAccess.getRequiredAttributes(dtd, elementName);

    List all = new ArrayList();
    List required = new ArrayList();

    for (Iterator iter = attributes.iterator(); iter.hasNext();)
    {
      String attrName = (String) iter.next();
      AttributeProposalInfo info = new AttributeProposalInfo();
      info.attributeName = attrName;
      String defaultValue = DTDAccess
          .getDefaultAttributeValue(dtd, elementName, attrName);
      info.defaultValue = defaultValue.length() == 0 ? null : defaultValue;
      if (info.defaultValue == null)
        info.defaultValue = DTDAccess.getTapestryDefaultValue(dtd, elementName, attrName);

      info.required = requiredAttributes.contains(attrName);
      info.image = info.required ? Images.getSharedImage("bullet_pink.gif") : Images
          .getSharedImage("bullet_web.gif");
      info.order = info.required ? 100 : 101;
      all.add(info);
      if (info.required)
        required.add(info);
    }

    MultiKey key = new MultiKey(new Object[]{dtd, elementName, ELEMENT_ATTR_INFOS}, false);
    ProposalInfoCache.put(key, all);
    key = new MultiKey(new Object[]{dtd, elementName, ELEMENT_ATTR_INFOS_REQUIRED}, false);
    ProposalInfoCache.put(key, required);
  }

  private static void computeNewElementProposalInfos(DTD dtd, String elementName)
  {
    Assert.isLegal(dtd != null);

    DTDElement element = (DTDElement) dtd.elements.get(elementName);

    if (element == null)
      return;

    ElementProposalInfo emptyInfo = new ElementProposalInfo();
    emptyInfo.elementName = elementName;
    emptyInfo.attrvalues = new String[0][];
    emptyInfo.empty = true;
    emptyInfo.comment = DTDAccess.getElementComment(dtd, elementName);

    boolean allowsChildren = element.getContent().getItemType() != DTDItemType.DTD_EMPTY;
    emptyInfo.totalAttrCount = DTDAccess.getAttributeNames(dtd, elementName).size();
    boolean hasAttibutes = emptyInfo.totalAttrCount != 0;

    if (hasAttibutes)
    {
      List requiredAttributes = DTDAccess.getRequiredAttributes(dtd, elementName);
      if (!requiredAttributes.isEmpty())
      {
        String[][] attrValues = new String[requiredAttributes.size()][];
        int i = 0;
        for (Iterator iter = requiredAttributes.iterator(); iter.hasNext();)
        {
          String attrName = (String) iter.next();
          String defaultValue = DTDAccess.getDefaultAttributeValue(
              dtd,
              elementName,
              attrName);
          attrValues[i++] = new String[]{attrName,
              defaultValue == "" ? null : defaultValue};
        }
        emptyInfo.attrvalues = attrValues;
      }
    }

    ElementProposalInfo nonEmptyInfo = null;
    if (allowsChildren)
    {
      nonEmptyInfo = emptyInfo.copy();
      nonEmptyInfo.empty = false;
    }

    ElementProposalInfo defaultInfo = emptyInfo;
    ElementProposalInfo optionalInfo = nonEmptyInfo;

    if (DTDAccess.isTapestryDTD(dtd))
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
    ProposalInfoCache.put(defaultKey, defaultInfo);
    MultiKey optionalKey = new MultiKey(new Object[]{dtd, elementName,
        OPTIONAL_NEW_ELEMENT_INFO}, false);
    ProposalInfoCache.put(optionalKey, optionalInfo);
  }

}