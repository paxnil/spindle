package com.wutka.dtd;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Iterator;

/**
 * Represents a sequence in an element's content. A sequence is declared in the
 * DTD as (value1,value2,value3,etc.)
 * 
 * @author Mark Wutka
 * 
 */
public class DTDSequence extends DTDContainer
{
  public DTDSequence()
  {
  }

  /** Writes out a declaration for this sequence */
  public void write(PrintWriter out) throws IOException
  {
    out.print("(");

    Enumeration e = getItemsVec().elements();
    boolean isFirst = true;

    while (e.hasMoreElements())
    {
      if (!isFirst)
        out.print(",");
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
    if (!(ob instanceof DTDSequence))
      return false;

    return super.equals(ob);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wutka.dtd.DTDContainer#getContainerType()
   */
  public final DTDItemType getItemType()
  {
    return DTDItemType.DTD_SEQUENCE;
  }

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