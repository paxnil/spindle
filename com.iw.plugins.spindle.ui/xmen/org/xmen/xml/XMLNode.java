/*******************************************************************************
 * Copyright (c) 2000, 2003 Jens Lukowski and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *    Jens Lukowski - initial API and implementation
 *    Geoff Longman - heavily modified for Spindle
 *******************************************************************************/
package org.xmen.xml;

import java.util.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.text.*;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.TypedPosition;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.xmen.internal.ui.text.ITypeConstants;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;

import com.iw.plugins.spindle.UIPlugin;
import com.wutka.dtd.OrderPreservingMap;

/**
 * @author jll
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XMLNode extends TypedPosition
    implements
      IAdaptable,
      IWorkbenchAdapter,
      Comparable
{
  public static final int TAG = 0;
  public static final int ATTR = 1;
  public static final int DOUBLEQUOTE = 2;
  public static final int SINGLEQUOTE = 3;
  public static final int ATTRIBUTE = 4;
  public static final int ATT_VALUE = 5;
  public static final int AFTER_ATTRIBUTE = 6;
  public static final int AFTER_ATT_VALUE = 7;
  public static final int IN_TERMINATOR = 8;

  public static final Comparator COMPARATOR = new Comparator()
  {
    public int compare(Object o1, Object o2)
    {
      int offset1 = ((XMLNode) o1).getOffset();
      int offset2 = ((XMLNode) o2).getOffset();
      return (offset1 > offset2) ? 1 : ((offset1 < offset2) ? -1 : 0);
    }
  };

  static public final Map TERMINATORS;

  static
  {
    TERMINATORS = new HashMap();
    TERMINATORS.put(ITypeConstants.TAG, ">");
    TERMINATORS.put(ITypeConstants.ATTR, "");
    TERMINATORS.put(ITypeConstants.TEXT, "");
    TERMINATORS.put(ITypeConstants.PI, "?>");
    TERMINATORS.put(ITypeConstants.DECL, ">");
    TERMINATORS.put(ITypeConstants.ENDTAG, ">");
    TERMINATORS.put(ITypeConstants.COMMENT, "-->");
    TERMINATORS.put(ITypeConstants.EMPTYTAG, "/>");
  }

  public static XMLNode getArtifactAt(IDocument doc, int offset)
  {
    return getArtifactAt(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY, doc, offset);
  }

  public static XMLNode getArtifactAt(String positionCategory, IDocument doc, int offset)
  {
    try
    {
      Position[] pos = doc.getPositions(positionCategory);
      for (int i = 0; i < pos.length; i++)
      {
        if (offset >= pos[i].getOffset()
            && offset <= pos[i].getOffset() + pos[i].getLength())
          return (XMLNode) pos[i];
      }
    } catch (BadPositionCategoryException e)
    {
      //do nothing
    }
    return null;
  }

  private boolean added = false;
  private boolean modified = false;
  private XMLNode parent = null;
  private List children = new ArrayList();
  private XMLNode correspondingNode = null;
  private IDocument document = null;
  public String publicId; //valid only for the root node
  public String rootNodeId; //valid only for the root node

  /**
   * @param offset
   * @param length
   * @param type
   */
  public XMLNode(int offset, int length, String type, IDocument document)
  {
    super(offset, length, type);
    added = true;
    this.document = document;
  }

  /**
   * @param region
   */
  public XMLNode(ITypedRegion region)
  {
    super(region);
    added = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.Position#setLength(int)
   */
  public void setLength(int length)
  {
    super.setLength(length);
    added = false;
    modified = true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.Position#setOffset(int)
   */
  public void setOffset(int offset)
  {
    super.setOffset(offset);
    added = false;
    modified = true;
  }

  /**
   * @return
   */
  public boolean isAdded()
  {
    return added;
  }

  /**
   * @return
   */
  public boolean isModified()
  {
    return modified;
  }

  /**
   * @param b
   */
  public void setAdded(boolean b)
  {
    added = b;
  }

  /**
   * @param b
   */
  public void setModified(boolean b)
  {
    modified = b;
  }

  /**
   * @return
   */
  public synchronized List getChildren()
  {
    return children;
  }

  /**
   * @return
   */
  public XMLNode getParent()
  {
    return parent;
  }

  /**
   * @param node
   */
  public void setParent(XMLNode node)
  {
    parent = node;
    if (parent != null && !parent.getChildren().contains(this))
    {
      parent.addChild(this);
    }
  }

  public synchronized void addChild(XMLNode childArtifact)
  {
    for (int i = 0; i < children.size(); i++)
    {
      if (((XMLNode) children.get(i)).getOffset() > childArtifact.getOffset()) {
        children.add(i, childArtifact);
        break;
      }

    }
    if (!children.contains(childArtifact))
      children.add(childArtifact);
  }

  public List getChildrenAfter(XMLNode child)
  {
    List result = new ArrayList();
    for (int i = 0; i < children.size(); i++)
    {
      if (((XMLNode) children.get(i)).getOffset() > child.getOffset())
      {
        result.add(child);
      }
    }

    return result;
  }

  public synchronized void removeChild(XMLNode child)
  {
    children.remove(child);
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  public Object getAdapter(Class adapter)
  {
    if (adapter.equals(IWorkbenchAdapter.class))
    {
      return this;
    }

    return null;
  }

  public boolean containsOnlyWhitespaces()
  {
    return getContent().trim().length() == 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
   */
  public Object[] getChildren(Object obj)
  {
    if (obj instanceof XMLNode)
    {
      List result = new ArrayList();

      XMLNode artifact = (XMLNode) obj;
      for (Iterator it = artifact.getChildren().iterator(); it.hasNext();)
      {
        XMLNode child = (XMLNode) it.next();
        String childType = child.getType();
        if (childType == ITypeConstants.ENDTAG
            || (childType == ITypeConstants.TEXT && child.containsOnlyWhitespaces()))
          continue;
        result.add(child);

      }

      String type = artifact.getType();
      if (type == ITypeConstants.TAG || type == ITypeConstants.EMPTYTAG)
      {
        result.addAll(0, artifact.getAttributes());
      } else if (type == ITypeConstants.DECL)
      {}

      return result.toArray(new XMLNode[0]);
    }

    return null;
  }

  public String getContent()
  {
    String content = "";

    try
    {
      content = document.get(getOffset(), getLength());
    } catch (BadLocationException e)
    {}

    return content;
  }

  public String getContentTo(int to)
  {
    return getContentTo(to, false);
  }

  public String getContentTo(int to, boolean stripLeader)
  {
    try
    {
      String result = document.get(getOffset(), to - getOffset());
      if (stripLeader)
      {
        return result.substring(1);
      } else
      {
        return result;
      }

    } catch (BadLocationException e)
    {
      // do nothing
    }
    return "";
  }

  public String getContentFrom(int from)
  {
    String content = "";

    try
    {
      content = document.get(from, getOffset() - from + getLength() - 1);
    } catch (BadLocationException e)
    {}

    return content;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
   */
  public ImageDescriptor getImageDescriptor(Object object)
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
   */
  public String getLabel(Object o)
  {
    return getName();
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
   */
  public Object getParent(Object o)
  {
    // TODO Auto-generated method stub
    return this;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Comparable#compareTo(java.lang.Object)
   */
  public int compareTo(Object obj)
  {
    if (this == obj)
      return 0;

    if (!(obj instanceof XMLNode))
      return 0;

    XMLNode other = (XMLNode) obj;
    return (getOffset() > other.getOffset()) ? 1 : ((getOffset() < other.getOffset())
        ? -1 : 0);
  }

  /**
   * @return
   */
  public XMLNode getCorrespondingNode()
  {
    return correspondingNode == null ? this : correspondingNode;
  }

  /**
   * @param node
   */
  public void setCorrespondingNode(XMLNode node)
  {
    correspondingNode = node;
  }

  public String getName()
  {
    String name = "unknown";

    String type = getType();
    if (type.equals(ITypeConstants.TEXT))
    {
      name = "#TEXT";
    } else if (type.equals(ITypeConstants.COMMENT))
    {
      name = "#COMMENT";
    } else if (getType().equals(ITypeConstants.ATTR))
    {
      name = getAttributeName();
    } else if (type.equals(ITypeConstants.TAG) || type.equals(ITypeConstants.EMPTYTAG)
        || type.equals(ITypeConstants.ENDTAG) || type.equals(ITypeConstants.PI)
        || type.equals(ITypeConstants.DECL))
    {
      name = getTagName();
    }

    return name;
  }

  public XMLNode getNextArtifact()
  {
    return getArtifactAt(document, getOffset() + getLength() + 1);
  }

  public XMLNode getPreviousArtifact()
  {
    return getArtifactAt(document, getOffset() - 1);
  }

  /** @deprecated not needed anymore? */
  public XMLNode getPreviousSibling()
  {
    if (parent == null)
      throw new IllegalStateException("create tree first");

    String myType = getType();
    if (parent.getType().equals("/"))
      return null;
    if (myType.equals(ITypeConstants.ENDTAG))
    {
      if (correspondingNode != null)
      {
        return correspondingNode.getPreviousSibling();
      } else
      {
        return null;
      }
    }

    XMLNode candidate = getPreviousArtifact();
    if (candidate == null)
      return null;
    if (candidate.parent == parent)
      return candidate;
    return null;
  }

  public XMLNode getPreviousSiblingTag(String allowed)
  {
    if (parent == null || parent.getType().equals("/") || allowed == null)
      return null;
    Position[] pos = null;
    try
    {
      pos = document.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
    } catch (BadPositionCategoryException e)
    {
      e.printStackTrace();
      return null;
    }
    Arrays.sort(pos, COMPARATOR);
    int index = 0;
    while (pos[index] != this)
      index++;
    if (index > 0)
    {
      XMLNode result = null;
      for (int i = index - 1; i >= 0; i--)
      {
        result = (XMLNode) pos[i];
        if (result == parent)
          return null;
        String type = result.getType();
        if (result.getParent() != parent
            || (type.equals(ITypeConstants.TEXT) || type.equals(ITypeConstants.COMMENT) || type
                .equals(ITypeConstants.DECL)))
        {
          continue;
        }

        String name = result.getName();
        if (name == null)
          continue;
        if (type.equals(ITypeConstants.ENDTAG))
        {
          XMLNode corresponding = result.getCorrespondingNode();
          if (allowed.indexOf(corresponding.getName().toLowerCase()) >= 0)
            return corresponding;
        } else if (allowed.indexOf(name) >= 0)
        {
          return result;
        }
      }
    }

    return null;
  }

  public String getValue()
  {
    String name = "<unknown type>";

    if (getType().equals(ITypeConstants.TEXT))
    {} else if (getType().equals(ITypeConstants.TAG))
    {
      if (children.size() > 0
          && ((XMLNode) children.get(0)).getType().equals(ITypeConstants.TEXT))
      {
        name = ((XMLNode) children.get(0)).getContent().trim();
      }
    } else if (getType().equals(ITypeConstants.PI))
    {} else if (getType().equals(ITypeConstants.ATTR))
    {
      return getAttributeValue();
    } else if (getType().equals(ITypeConstants.COMMENT))
    {} else if (getType().equals(ITypeConstants.DECL))
    {} else if (getType().equals(ITypeConstants.ENDTAG))
    {} else if (getType().equals(ITypeConstants.EMPTYTAG))
    {}

    return name;
  }

  private String getAttributeName()
  {
    try
    {
      String content = document.get(getOffset(), getLength());
      int index = content.indexOf("=");
      if (index == -1)
      {
        index = content.indexOf("\"");
        if (index == -1)
        {
          index = content.indexOf("'");
          if (index == -1)
            index = content.length();
        }
      }
      return content.substring(0, index).trim();
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }
    return null;
  }

  private String getTagName()
  {
    String content = null;
    String name = null;

    try
    {
      content = document.get(getOffset(), getLength());
    } catch (BadLocationException e)
    {
      //            e.printStackTrace();
      return null;
    }

    StringTokenizer st = new StringTokenizer(content, " \t\n\r<>/");

    if (st.hasMoreTokens())
    {
      name = st.nextToken();
    }

    if (name == null)
    {
      name = "";
    }

    return name;
  }

  public String getAttributeValue()
  {
    int index = getAttributeValueStart();

    if (index < 0)
      return null;

    try
    {
      String content = document.get(getOffset(), getLength());
      content = content.substring(index).trim();

      if (content.length() < 2)
        return "";

      return content.substring(1, content.length() - 1);
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }

    return null;
  }

  private int getAttributeValueStart()
  {
    int index = 0;
    try
    {
      String content = document.get(getOffset(), getLength());
      int singleIndex = content.indexOf("\"");
      int doubleIndex = content.indexOf("'");

      if (singleIndex < 0 && doubleIndex < 0)
        return 0;

      if (singleIndex >= 0 && doubleIndex > 0)
      {

        index = Math.min(singleIndex, doubleIndex);
      } else
      {
        index = Math.max(singleIndex, doubleIndex);
      }

    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }

    return index;
  }

  public IRegion getAttributeValueRegion()
  {
    int index = getAttributeValueStart();

    if (index < 0)
      return null;

    if (index == 0)
      return new Region(getOffset(), getLength());

    String value = getAttributeValue();
    if (value != null)
      return new Region(getOffset() + index + 1, value.length());

    return null;
  }

  public XMLNode getAttributeAt(int offset)
  {
    List attrs = getAttributes();
    for (Iterator it = attrs.iterator(); it.hasNext();)
    {
      XMLNode artifact = (XMLNode) it.next();
      if (artifact.getOffset() <= offset
          && offset <= artifact.getOffset() + artifact.getLength())
        return artifact;
    }
    return null;
  }

  public List getAttributes()
  {
    List attrs = new ArrayList();
    String content = null;
    int state = TAG;
    int start = -1;
    int startLength = 0;
    int endLength = 0;
    if (ITypeConstants.PI.equals(getType()))
    {
      startLength = 2;
      endLength = 2;
    } else if (ITypeConstants.DECL.equals(getType()))
    {
      startLength = 2;
      endLength = 1;
    } else if (ITypeConstants.TAG.equals(getType()))
    {
      startLength = 1;
      endLength = 1;
    } else if (ITypeConstants.EMPTYTAG.equals(getType()))
    {
      startLength = 1;
      endLength = 2;
    } else
    {
      return attrs;
    }

    try
    {
      content = document.get(getOffset(), getLength());
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
      return attrs;
    }

    String name = getName();
    int initial = name == null ? 0 : content.indexOf(name)+name.length() ;

    for (int i = startLength + initial; i < content.length() - endLength; i++)
    {
      char c = content.charAt(i);
      switch (c)
      {
        case '"' :

          if (state == DOUBLEQUOTE)
          {
            attrs.add(new XMLNode(
                getOffset() + start,
                i - start + 1,
                ITypeConstants.ATTR,
                document));
            start = -1;
            state = TAG;
          } else if (state == SINGLEQUOTE)
          {
            break;
          } else
          {
            state = DOUBLEQUOTE;
          }
          break;
        case '\'' :
          if (state == SINGLEQUOTE)
          {
            attrs.add(new XMLNode(
                getOffset() + start,
                i - start + 1,
                ITypeConstants.ATTR,
                document));
            start = -1;
            state = TAG;
          } else if (state == DOUBLEQUOTE)
          {
            break;
          } else
          {
            state = SINGLEQUOTE;
          }
          break;
        default :
          if (!Character.isWhitespace(c))
          {
            if (state == TAG)
            {
              start = i;
              state = ATTR;
            }
          } else if (state == ATTR)
          {
            boolean stop = false;
            int j = i;
            // lookahead to see if this is an attribute name with no value
            for (; j < content.length() - endLength; j++)
            {
              char lookahead = content.charAt(j);

              switch (lookahead)
              {
                case '=' :
                  break;
                case '"' :
                  break;
                case '\'' :
                  break;
                default :
                  stop = !Character.isWhitespace(lookahead);
                  break;
              }
              if (stop)
                break;
            }
            if (stop)
            {

              attrs.add(new XMLNode(
                  getOffset() + start,
                  i - start + 1,
                  ITypeConstants.ATTR,
                  document));
              start = -1;
              state = TAG;
            }
          }
      }
    }

    if (start != -1)
    {
      attrs.add(new XMLNode(
          getOffset() + start,
          content.length() - startLength - start
              - (!getType().equals(ITypeConstants.TAG) ? 1 : 0),
          ITypeConstants.ATTR,
          document));
    }
    for (Iterator iter = attrs.iterator(); iter.hasNext();)
    {
      XMLNode attr = (XMLNode) iter.next();
      attr.length = StringUtils.stripEnd(attr.getContent(), null).length();
    }

    return attrs;
  }

  public Map getAttributesMap()
  {
    Map result = new OrderPreservingMap();
    for (Iterator iter = getAttributes().iterator(); iter.hasNext();)
    {
      XMLNode attr = (XMLNode) iter.next();
      result.put(attr.getName().toLowerCase(), attr);
    }
    return result;
  }

  public int getStateAt(int offset)
  {
    int state = TAG;
    String content = null;
    try
    {
      content = document.get(getOffset(), offset - getOffset());
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
      return TAG;
    }

    String name = getName();
    if (name == null)
      return TAG;

    String type = getType();
    if (type == "/")
      return -1;

    String terminator = (String) TERMINATORS.get(type);
    int terminatorLength = terminator.length();

    for (int i = name.length(); i < content.length(); i++)
    {
      char c = content.charAt(i);
      switch (c)
      {
        case '=' :
          if (state == AFTER_ATTRIBUTE || state == ATTRIBUTE)
          {
            state = ATT_VALUE;
          }
          break;
        case '"' :
          if (state == DOUBLEQUOTE)
          {
            state = AFTER_ATT_VALUE;
          } else if (state == SINGLEQUOTE)
          {
            break;
          } else
          {
            state = DOUBLEQUOTE;
          }
          break;
        case '\'' :
          if (state == SINGLEQUOTE)
          {
            state = AFTER_ATT_VALUE;
          } else if (state == DOUBLEQUOTE)
          {
            break;
          } else
          {
            state = SINGLEQUOTE;
          }
          break;
        default :
          if (Character.isWhitespace(c))
          {
            switch (state)
            {
              case TAG :
                state = ATTRIBUTE;
                break;
              case ATTR :
                state = AFTER_ATTRIBUTE;
                break;
              case AFTER_ATT_VALUE :
                state = ATTRIBUTE;
                break;
            }
          } else if (terminatorLength > 0)
          {
            switch (state)
            {
              case IN_TERMINATOR :
                break;
              case DOUBLEQUOTE :
              case SINGLEQUOTE :
                break;
              default :
                if (c == terminator.charAt(0))
                  state = IN_TERMINATOR;
                break;
            }
          }
      }
    }

    return state;
  }

  /**
   * For !DOCTYPE:
   * 
   * [28] doctypedecl ::= ' <!DOCTYPE' S Name (S ExternalID)? S? ('['
   * (markupdecl | DeclSep)* ']' S?)? '>' [28a] DeclSep ::= PEReference | S [29]
   * markupdecl ::= elementdecl | AttlistDecl | EntityDecl | NotationDecl | PI |
   * Comment [75] ExternalID ::= 'SYSTEM' S SystemLiteral | 'PUBLIC' S
   * PubidLiteral S SystemLiteral
   */
  public String getDTDLocation()
  {
    //TODO: this must be changed to include inner DTDs
    String content = getContent();
    String location = null;
    int index = -1;
    int endIndex = -1;

    content = content.substring("<!DOCTYPE".length());

    index = content.indexOf("SYSTEM");

    if (index != -1)
    {
      index = content.indexOf("\"", index + "SYSTEM".length());

      if (index != -1)
      {
        endIndex = content.indexOf("\"", index + 1);
      } else
      {
        index = content.indexOf("'", index + "SYSTEM".length());
        if (index == -1)
        {
          return null;
        }
        endIndex = content.indexOf("'", index + 1);
      }
    } else
    {
      index = content.indexOf("PUBLIC");
      if (index == -1)
      {
        return null;
      }
      index = content.indexOf("\"", index + "PUBLIC".length());
      if (index != -1)
      {
        // skip public ID
        index = content.indexOf("\"", index + 1);
      } else
      {
        index = content.indexOf("'", index + "PUBLIC".length());
        if (index == -1)
        {
          return null;
        }
        // skip public ID
        index = content.indexOf("'", index + 1);
      }
      index = content.indexOf("\"", index + 1);
      if (index != -1)
      {
        endIndex = content.indexOf("\"", index + 1);
      } else
      {
        index = content.indexOf("'", index + 1);
        if (index != -1)
        {
          endIndex = content.indexOf("'", index + 1);
        }
      }
    }
    if (index == -1 || endIndex == -1)
    {
      return null;
    }
    location = content.substring(index + 1, endIndex);

    return location;
  }

  /**
   * @return
   */
  public IDocument getDocument()
  {
    return document;
  }

  /**
   * @param document
   */
  public void setDocument(IDocument document)
  {
    this.document = document;
  }

  /*
   * (non-Javadoc)
   * 
   * @see java.lang.Object#toString()
   */
  public String toString()
  {
    String s = super.toString();
    s += "[";
    s += "name=" + getName() + ";";
    s += "type=" + getType() + ";";
    s += "content=" + getContent() + ";";
    s += "isDeleted=" + isDeleted() + ";";
    s += "[" + getOffset() + "," + getLength() + "]";
    s += "]";

    return s;
  }

  public boolean isTerminated()
  {
    if (getLength() == 0)
      return true;
    String type = getType();
    if (type.equals(ITypeConstants.TEXT))
      return true;

    if (type.equals(ITypeConstants.ATTR))
      return true;

    String terminator = (String) TERMINATORS.get(type);
    int length = terminator.length();
    try
    {
      return terminator.equals(document.get(getOffset() + getLength() - length, length));

    } catch (BadLocationException e)
    {
      // do nothing
    }
    return false;
  }

  public String readPublicId()
  {
    String content = getContent();
    int start = -1;
    int end = -1;
    int minLength = "<!DOCTYPE".length();
    if (content.length() < minLength)
      return "";

    content = content.substring("<!DOCTYPE".length());
    start = content.indexOf("PUBLIC");
    if (start >= 0)
    {
      start = content.indexOf("\"", start + "PUBLIC".length());
      if (start >= 0)
      {
        String test = content.substring(start + 1);
        end = content.indexOf("\"", ++start);

        if (end >= 0)
          return content.substring(start, end);

      }
    }
    return null;
  }

  public String getRootNodeId()
  {
    String content = getContent();
    int index = -1;
    int end = -1;
    int minLength = "<!DOCTYPE".length();
    if (content.length() < minLength)
      return "";
    content = content.substring("<!DOCTYPE".length());

    index = content.indexOf("PUBLIC");
    if (index >= 0)
    {
      content = content.substring(0, index).trim();
      return content.length() == 0 ? null : content;
    } else
    {
      index = content.indexOf("SYSTEM");
      if (index >= 0)
      {
        content = content.substring(0, index).trim();
        return content.length() == 0 ? null : content;
      } else
      {
        content = content.trim();
        return content.length() == 0 ? null : content;
      }
    }
  }

}