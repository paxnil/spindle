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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.lang.StringUtils;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.TypedPosition;
import org.xmen.internal.ui.text.XMLDocumentPartitioner;

import com.iw.plugins.spindle.UIPlugin;
import com.wutka.dtd.OrderPreservingMap;

/**
 *  Produced by the XMLDocumentPartitioner.
 *  Represents an xml artifact in the document.
 * 
 * @author Jens Lukowski (dark_angel@users.sourceforge.net )
 * @version $Id$
 */
public class XMLNode extends TypedPosition implements Comparable
{
    public static final Comparator COMPARATOR = new Comparator()
    {
        public int compare(Object o1, Object o2)
        {
            int offset1 = ((XMLNode) o1).getOffset();
            int offset2 = ((XMLNode) o2).getOffset();
            return (offset1 > offset2) ? 1 : ((offset1 < offset2) ? -1 : 0);
        }
    };
    public static final int TAG = 0;
    public static final int ATTR = 1;
    public static final int DOUBLEQUOTE = 2;
    public static final int SINGLEQUOTE = 3;
    public static final int ATTRIBUTE = 4;
    public static final int ATT_VALUE = 5;
    public static final int AFTER_ATTRIBUTE = 6;
    public static final int AFTER_ATT_VALUE = 7;
    public static final int IN_TERMINATOR = 8;

    static public final Map TERMINATORS;

    static {
        TERMINATORS = new HashMap();
        TERMINATORS.put(XMLDocumentPartitioner.TAG, ">");
        TERMINATORS.put(XMLDocumentPartitioner.ATTR, "");
        TERMINATORS.put(XMLDocumentPartitioner.TEXT, "");
        TERMINATORS.put(XMLDocumentPartitioner.PI, "?>");
        TERMINATORS.put(XMLDocumentPartitioner.DECL, ">");
        TERMINATORS.put(XMLDocumentPartitioner.ENDTAG, ">");
        TERMINATORS.put(XMLDocumentPartitioner.COMMENT, "-->");
        TERMINATORS.put(XMLDocumentPartitioner.EMPTYTAG, "/>");
    }

    public static synchronized XMLNode createTree(IDocument document, int stopOffset) throws BadLocationException
    {
        return createTree(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY, document, stopOffset);
    }
    public static synchronized XMLNode createTree(String postionCategory, IDocument document, int stopOffset)
        throws BadLocationException
    {
        Position[] pos = null;
        try
        {
            pos = document.getPositions(postionCategory);
        } catch (BadPositionCategoryException e)
        {
            e.printStackTrace();
            return null;
        }
        Arrays.sort(pos, COMPARATOR);

        XMLNode root = new XMLNode(0, 0, "/", document);
        root.fPos = pos;
        root.fParent = null;
        XMLNode parent = root;
        for (int i = 0; i < pos.length; i++)
        {
            XMLNode node = (XMLNode) pos[i];

            String type = node.getType();
            if (type != XMLDocumentPartitioner.ENDTAG)
            {
                if (root.fPublicId == null && type == XMLDocumentPartitioner.DECL)
                {
                    root.fPublicId = node.readPublicId();
                    root.fRootNodeId = node.getRootNodeId();
                }
                node.setParent(parent);
            }

            if (type == XMLDocumentPartitioner.TAG)
                parent = node;

            if (type == XMLDocumentPartitioner.ENDTAG)
            {
                node.setParent(parent.fParent);
                node.fCorrespondingNode = parent;
                parent.fCorrespondingNode = node;
                parent = parent.fParent;
            }
        }
        return root;
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
                if (offset >= pos[i].getOffset() && offset <= pos[i].getOffset() + pos[i].getLength())
                    return (XMLNode) pos[i];
            }
        } catch (BadPositionCategoryException e)
        {
            //do nothing
        }
        return null;
    }

    protected IDocument fDocument = null;
    protected XMLNode fParent;
    private List children = new ArrayList();
    protected XMLNode fCorrespondingNode;
    public String fPublicId; // only available in the root artifact
    public String fRootNodeId; // only available in the root artifact
    private Position[] fPos; // valid only in a root after a tree is built.

    public XMLNode(int offset, int length, String type, IDocument document)
    {
        super(offset, length, type);
        this.fDocument = document;
    }

    public XMLNode get(int documentOffset)
    {
        if (fPos != null)
        {
            for (int i = 0; i < fPos.length; i++)
            {
                if (fPos[i].offset <= documentOffset && documentOffset < fPos[i].offset + fPos[i].length)
                    return (XMLNode) fPos[i];
            }

        }
        return null;
    }

    public boolean whiteSpaceOnly()
    {
        try
        {
            String test = fDocument.get(getOffset(), getLength());
            return test.trim().length() == 0;
        } catch (BadLocationException e)
        {
            // do nothing
        }
        return true;
    }

    public String getContent()
    {
        try
        {
            return fDocument.get(getOffset(), getLength());
        } catch (BadLocationException e)
        {
            // do nothing
        }
        return "";
    }

    public String getContentTo(int to, boolean stripLeader)
    {
        try
        {
            String result = fDocument.get(getOffset(), to - getOffset());
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

    public boolean isMultiLine()
    {
        try
        {
            return fDocument.getLineOfOffset(getOffset()) < fDocument.getLineOfOffset(getOffset() + getLength());
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
        }
        return false;
    }

    public String getContentFrom(int from)
    {
        try
        {
            return fDocument.get(from, getOffset() - from + getLength() - 1);
        } catch (BadLocationException e)
        {
            // do nothing
        }
        return "";
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

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj)
    {
        if (this == obj)
            return 0;

        if (!(obj instanceof XMLNode))
            return 0;

        XMLNode other = (XMLNode) obj;
        return (getOffset() > other.getOffset()) ? 1 : ((getOffset() < other.getOffset()) ? -1 : 0);
    }

    public String getName()
    {
        String name = "unknown";

        String type = getType();
        if (type.equals(XMLDocumentPartitioner.TEXT))
        {
            name = "#TEXT";
        } else if (type.equals(XMLDocumentPartitioner.COMMENT))
        {
            name = "#COMMENT";
        } else if (getType().equals(XMLDocumentPartitioner.ATTR))
        {
            name = getAttributeName();
        } else if (
            type.equals(XMLDocumentPartitioner.TAG)
                || type.equals(XMLDocumentPartitioner.EMPTYTAG)
                || type.equals(XMLDocumentPartitioner.ENDTAG)
                || type.equals(XMLDocumentPartitioner.PI)
                || type.equals(XMLDocumentPartitioner.DECL))
        {
            name = getTagName();
        }

        return name;
    }

    public boolean isTerminated()
    {
        if (getLength() == 0)
            return true;
        String type = getType();
        if (type.equals(XMLDocumentPartitioner.TEXT))
            return true;

        if (type.equals(XMLDocumentPartitioner.ATTR))
            return true;

        String terminator = (String) TERMINATORS.get(type);
        int length = terminator.length();
        try
        {
            return terminator.equals(fDocument.get(getOffset() + getLength() - length, length));

        } catch (BadLocationException e)
        {
            // do nothing
        }
        return false;
    }

    private String getAttributeName()
    {
        try
        {
            String content = fDocument.get(getOffset(), getLength());
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
        if (getLength() == 1)
            return null;
        try
        {
            content = fDocument.get(getOffset(), getLength());
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
            return null;
        }

        if (Character.isWhitespace(content.charAt(1)))
            return null;

        StringTokenizer st = new StringTokenizer(content, "= \t\n\r<>/");

        if (st.hasMoreTokens())
            name = st.nextToken();

        if (name == null)
            name = "";

        return name;
    }

    public String getAttributeValue()
    {
        int index = getAttributeValueStart();

        if (index < 0)
            return null;

        try
        {
            String content = fDocument.get(getOffset(), getLength());
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
            String content = fDocument.get(getOffset(), getLength());
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
        return new Region(getOffset() , getLength());

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
            if (artifact.getOffset() <= offset && offset <= artifact.getOffset() + artifact.getLength())
                return artifact;
        }
        return null;
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

    public List getAttributes()
    {
        List attrs = new ArrayList();
        String content = null;
        int state = TAG;
        int start = -1;
        int startLength = 0;
        int endLength = 0;
        if (XMLDocumentPartitioner.PI.equals(getType()))
        {
            startLength = 2;
            endLength = 2;
        } else if (XMLDocumentPartitioner.DECL.equals(getType()))
        {
            startLength = 2;
            endLength = 1;
        } else if (XMLDocumentPartitioner.TAG.equals(getType()))
        {
            startLength = 1;
            endLength = 1;
        } else if (XMLDocumentPartitioner.EMPTYTAG.equals(getType()))
        {
            startLength = 1;
            endLength = 2;
        } else
        {
            return attrs;
        }

        try
        {
            content = fDocument.get(getOffset(), getLength());
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
            return attrs;
        }

        String name = getName();
        int initial = name == null ? 0 : name.length();

        for (int i = startLength + initial; i < content.length() - endLength; i++)
        {
            char c = content.charAt(i);
            switch (c)
            {
                case '"' :

                    if (state == DOUBLEQUOTE)
                    {
                        attrs.add(
                            new XMLNode(getOffset() + start, i - start + 1, XMLDocumentPartitioner.ATTR, fDocument));
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
                        attrs.add(
                            new XMLNode(getOffset() + start, i - start + 1, XMLDocumentPartitioner.ATTR, fDocument));
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

                            attrs.add(
                                new XMLNode(
                                    getOffset() + start,
                                    i - start + 1,
                                    XMLDocumentPartitioner.ATTR,
                                    fDocument));
                            start = -1;
                            state = TAG;
                        }
                    }
            }
        }

        if (start != -1)
        {
            attrs.add(
                new XMLNode(
                    getOffset() + start,
                    content.length() - startLength - start - (!getType().equals(XMLDocumentPartitioner.TAG) ? 1 : 0),
                    XMLDocumentPartitioner.ATTR,
                    fDocument));
        }
        for (Iterator iter = attrs.iterator(); iter.hasNext();)
        {
            XMLNode attr = (XMLNode) iter.next();
            attr.length = StringUtils.stripEnd(attr.getContent(), null).length();
        }

        return attrs;
    }

    public int getStateAt(int offset)
    {
        int state = TAG;
        String content = null;
        try
        {
            content = fDocument.get(getOffset(), offset - getOffset());
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

    public IDocument getDocument()
    {
        return fDocument;
    }

    public void setDocument(IDocument document)
    {
        this.fDocument = document;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append(" name=");
        buf.append(getName());
        buf.append(",");
        buf.append(" type=");
        buf.append(getType());
        buf.append(",");
        buf.append(" content=");
        buf.append(getContent());
        return buf.toString();
    }

    public String superString()
    {
        return super.toString();
    }
    public String getStateString(int offset)
    {
        int state = getStateAt(offset);
        switch (state)
        {
            case TAG :
                return "TAG";
            case ATTR :
                return "ATTR";
            case ATT_VALUE :
                return "ATT_VALUE";
            case DOUBLEQUOTE :
                return "DOUBLEQUOTE";
            case SINGLEQUOTE :
                return "SINGLEQUOTE";
            case AFTER_ATTRIBUTE :
                return "AFTER_ATTRIBUTE";
            case AFTER_ATT_VALUE :
                return "AFTER_ATT_VALUE";
            case ATTRIBUTE :
                return "ATTRIBUTE";
            default :
                return Integer.toString(state);
        }
    } /**
                                                                                                                                                                                                                                                                               * @return
                                                                                                                                                                                                                                                                               */
    public XMLNode getCorrespondingNode()
    {
        return fCorrespondingNode;
    } /**
                                                                                                                                                                                                                                                                                   * @return
                                                                                                                                                                                                                                                                                   */
    public XMLNode getParent()
    {
        return fParent;
    } /**
                                                                                                                                                                                                                                                                                   * @param artifact
                                                                                                                                                                                                                                                                                   */
    public void setCorrespondingNode(XMLNode artifact)
    {
        fCorrespondingNode = artifact;
    } /**
                                                                                                                                                                                                                                                                                   * @param artifact
                                                                                                                                                                                                                                                                                   */
    public void setParent(XMLNode artifact)
    {
        fParent = artifact;
        if (fParent != null && !fParent.getChildren().contains(this))
            fParent.addChild(this);

    }

    public synchronized void addChild(XMLNode childArtifact)
    {
        for (int i = 0; i < children.size(); i++)
        {
            if (((XMLNode) children.get(i)).getOffset() > childArtifact.getOffset())
                children.add(i, childArtifact);

        }
        if (!children.contains(childArtifact))
            children.add(childArtifact);
    }

    public List getChildren()
    {
        return children;
    }

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
                if (childType == XMLDocumentPartitioner.ENDTAG
                    || (childType == XMLDocumentPartitioner.TEXT && child.containsOnlyWhitespace()))
                    continue;
                result.add(child);

            }

            String type = artifact.getType();
            if (type == XMLDocumentPartitioner.TAG || type == XMLDocumentPartitioner.EMPTYTAG)
            {
                result.addAll(0, artifact.getAttributes());
            } else if (type == XMLDocumentPartitioner.DECL)
            {}

            return result.toArray(new XMLNode[0]);
        }

        return null;
    }

    public boolean containsOnlyWhitespace()
    {
        return getContent().trim().length() == 0;
    }

    //    // Tags only - if you have an end tag - pass its corresponding tag
    //    public XMLNode findLastChild()
    //    {
    //        if (fParent == null)
    //            throw new IllegalStateException("create tree first");
    //
    //        String type = getType();
    //        if (type == XMLDocumentPartitioner.TAG)
    //            return null;
    //
    //        Position[] pos = null;
    //        try
    //        {
    //            pos = fDocument.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
    //        } catch (BadPositionCategoryException e)
    //        {
    //            e.printStackTrace();
    //            return null;
    //        }
    //        Arrays.sort(pos, COMPARATOR);
    //        int index = 0;
    //        while (pos[index] != this)
    //            index++;
    //        XMLNode result = null;
    //        XMLNode next = null;
    //        if (index > 0 && index < pos.length)
    //        {
    //            for (int i = index + 1; i < pos.length; i++)
    //            {
    //                next = (XMLNode) pos[i];
    //                if (next.getParent() == fParent)
    //                    break;
    //                if (next.getType() != XMLDocumentPartitioner.TAG
    //                    || next.getType() != XMLDocumentPartitioner.EMPTYTAG
    //                    || next.getParent() != this)
    //                    continue;
    //                result = next;
    //            }
    //        }
    //
    //        return result;
    //    }

    public XMLNode getPreviousSibling()
    {
        if (fParent == null)
            throw new IllegalStateException("create tree first");

        String myType = getType();
        if (fParent.getType().equals("/"))
            return null;
        if (myType.equals(XMLDocumentPartitioner.ENDTAG))
        {
            if (fCorrespondingNode != null)
            {
                return fCorrespondingNode.getPreviousSibling();
            } else
            {
                return null;
            }
        }

        XMLNode candidate = getPreviousArtifact();
        if (candidate == null)
            return null;
        if (candidate.fParent == fParent)
            return candidate;
        return null;
    }

    public XMLNode getPreviousArtifact()
    {
        return getArtifactAt(fDocument, getOffset() - 1);
    }

    public XMLNode getNextArtifact()
    {
        return getArtifactAt(fDocument, getOffset() + getLength() + 1);
    }

    /** may be the prev sibling or the parent
     *  TODO remove 
     */
    public XMLNode getPreviousSiblingTag(String allowed)
    {
        if (fParent == null || fParent.getType().equals("/") || allowed == null)
            return null;
        Position[] pos = null;
        try
        {
            pos = fDocument.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
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
                if (result == fParent)
                    return null;
                String type = result.getType();
                if (result.getParent() != fParent
                    || (type.equals(XMLDocumentPartitioner.TEXT)
                        || type.equals(XMLDocumentPartitioner.COMMENT)
                        || type.equals(XMLDocumentPartitioner.DECL)))
                {
                    continue;
                }

                String name = result.getName();
                if (name == null)
                    continue;
                if (type.equals(XMLDocumentPartitioner.ENDTAG))
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

}
