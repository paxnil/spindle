package com.wutka.dtd;

import java.io.*;
import java.util.*;

/** Represents a choice of items.
 * A choice in a DTD looks like (option1 | option2 | option3)
 *
 * @author Mark Wutka
 * @version $Revision$ $Date$ by $Author$
 */
public class DTDChoice extends DTDContainer implements Cloneable
{
    public DTDChoice()
    {}

    /** Writes out the possible choices to a PrintWriter */
    public void write(PrintWriter out) throws IOException
    {
        out.print("(");
        Enumeration e = getItemsVec().elements();
        boolean isFirst = true;

        while (e.hasMoreElements())
        {
            if (!isFirst)
                out.print(" | ");
            isFirst = false;

            DTDItem item = (DTDItem) e.nextElement();

            item.write(out);
        }
        out.print(")");
        cardinal.write(out);
    }

    public boolean equals(Object ob)
    {
        if (ob == this)
            return true;
        if (!(ob instanceof DTDChoice))
            return false;

        return super.equals(ob);
    }
    /* (non-Javadoc)
     * @see com.wutka.dtd.DTDContainer#getContainerType()
     */
    public final DTDItemType getItemType()
    {
        return DTDItemType.DTD_CHOICE;
    }

    public Object clone()
    {
        return super.clone();
    }

    /* (non-Javadoc)
     * @see com.wutka.dtd.DTDItem#match(java.lang.String)
     */
    public boolean match(String match)
    {
        for (Iterator iter = getItemsVec().iterator(); iter.hasNext();)
        {
            DTDItem item = (DTDItem) iter.next();
            if (item.match(match))
                return true;
        }
        return false;
    }

}
