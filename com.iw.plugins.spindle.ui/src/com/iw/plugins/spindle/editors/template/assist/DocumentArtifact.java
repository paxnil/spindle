/*
 * Created on 17.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.iw.plugins.spindle.editors.template.assist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TypedPosition;

import com.iw.plugins.spindle.UIPlugin;

/**
 * @author jll
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DocumentArtifact extends TypedPosition implements Comparable
{
    public static final int TAG = 0;
    public static final int ATTR = 1;
    public static final int DOUBLEQUOTE = 2;
    public static final int SINGLEQUOTE = 3;
    public static final int ATTRIBUTE = 4;
    public static final int ATT_VALUE = 5;
    public static final int AFTER_ATTRIBUTE = 6;
    public static final int AFTER_ATT_VALUE = 7;

    protected IDocument fDocument = null;

    public DocumentArtifact(int offset, int length, String type, IDocument document)
    {
        super(offset, length, type);
        this.fDocument = document;
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

    /* (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(Object obj)
    {
        if (this == obj)
            return 0;

        if (!(obj instanceof DocumentArtifact))
            return 0;

        DocumentArtifact other = (DocumentArtifact) obj;
        return (getOffset() > other.getOffset()) ? 1 : ((getOffset() < other.getOffset()) ? -1 : 0);
    }

    public String getName()
    {
        String name = "unknown";

        if (getType().equals(DocumentArtifactPartitioner.TEXT))
        {
            return "#TEXT";
        } else if (getType().equals(DocumentArtifactPartitioner.TAG))
        {
            return getTagName();
        } else if (getType().equals(DocumentArtifactPartitioner.PI))
        {
            return getTagName();
        } else if (getType().equals(DocumentArtifactPartitioner.ATTR))
        {
            return getAttributeName();
        } else if (getType().equals(DocumentArtifactPartitioner.COMMENT))
        {
            return "#COMMENT";
        } else if (getType().equals(DocumentArtifactPartitioner.DECL))
        {
            return getTagName();
        } else if (getType().equals(DocumentArtifactPartitioner.ENDTAG))
        {
            return getTagName();
        } else if (getType().equals(DocumentArtifactPartitioner.EMPTYTAG))
        {
            return getTagName();
        }

        return name;
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

        try
        {
            content = fDocument.get(getOffset(), getLength());
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
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
        String content = null;
        int index = 0;

        try
        {
            content = fDocument.get(getOffset(), getLength());
            index = content.indexOf("\"");

            if (index == -1)
            {
                index = content.indexOf("'");
            }
        } catch (BadLocationException e)
        {
            UIPlugin.log(e);
        }

        content = content.substring(index).trim();

        return content.substring(1, content.length() - 1);
    }

    public DocumentArtifact getAttributeAt(int offset)
    {
        List attrs = getAttributes();

        for (Iterator it = attrs.iterator(); it.hasNext();)
        {
            DocumentArtifact node = (DocumentArtifact) it.next();

            if (node.getOffset() <= offset && offset <= node.getOffset() + node.getLength())
            {
                return node;
            }
        }

        return null;
    }

    public Map getAttributesMap()
    {
        Map result = new HashMap();
        for (Iterator iter = getAttributes().iterator(); iter.hasNext();)
        {
            DocumentArtifact attr = (DocumentArtifact) iter.next();
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

        if (DocumentArtifactPartitioner.PI.equals(getType()))
        {
            startLength = 2;
            endLength = 2;
        } else if (DocumentArtifactPartitioner.DECL.equals(getType()))
        {
            startLength = 2;
            endLength = 1;
        } else if (DocumentArtifactPartitioner.TAG.equals(getType()))
        {
            startLength = 1;
            endLength = 1;
        } else if (DocumentArtifactPartitioner.EMPTYTAG.equals(getType()))
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

        if (getName() == null)
        {
            return attrs;
        }

        for (int i = startLength + getName().length(); i < content.length() - endLength; i++)
        {
            char c = content.charAt(i);
            switch (c)
            {
                //            case '=':
                //                if (state == TAG) {
                //                    state = ATTR_VALUE;
                //                }
                //                break;
                //
                case '"' :
                    if (state == DOUBLEQUOTE)
                    {
                        attrs.add(
                            new DocumentArtifact(
                                getOffset() + start,
                                i - start + 1,
                                DocumentArtifactPartitioner.ATTR,
                                fDocument));
                        start = -1;
                        state = TAG;
                    } else
                    {
                        state = DOUBLEQUOTE;
                    }
                    break;

                case '\'' :
                    if (state == SINGLEQUOTE)
                    {
                        attrs.add(
                            new DocumentArtifact(
                                getOffset() + start,
                                i - start + 1,
                                DocumentArtifactPartitioner.ATTR,
                                fDocument));
                        start = -1;
                        state = TAG;
                    } else
                    {
                        state = SINGLEQUOTE;
                    }
                    break;

                default :
                    if (!Character.isWhitespace(c) && state == TAG)
                    {
                        start = i;
                        state = ATTR;
                    }
            }
        }

        if (start != -1)
        {
            attrs.add(
                new DocumentArtifact(
                    getOffset() + start,
                    content.length() - startLength - start,
                    DocumentArtifactPartitioner.ATTR,
                    fDocument));
        }

        return attrs;
    }

    public int getStateAt(int offset)
    {
        String content = null;
        int state = TAG;

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
                    } else
                    {
                        state = DOUBLEQUOTE;
                    }
                    break;

                case '\'' :
                    if (state == SINGLEQUOTE)
                    {
                        state = AFTER_ATT_VALUE;
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
    }

}
