package com.wutka.dtd;

import java.io.*;

/**
 * Represents the EMPTY keyword in an Element's content spec
 * 
 * @author Mark Wutka
 * @version $Revision$ $Date$ by $Author$
 */
public class DTDEmpty extends DTDItem
{
  public DTDEmpty()
  {
  }

  /** Writes out the keyword "EMPTY" */
  public void write(PrintWriter out) throws IOException
  {
    out.print("EMPTY");
    cardinal.write(out);
  }

  public boolean equals(Object ob)
  {
    if (ob == this)
      return true;
    if (!(ob instanceof DTDEmpty))
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
    return DTDItemType.DTD_EMPTY;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wutka.dtd.DTDItem#match(java.lang.String)
   */
  public boolean match(String match)
  {
    // TODO Auto-generated method stub
    return false;
  }

}