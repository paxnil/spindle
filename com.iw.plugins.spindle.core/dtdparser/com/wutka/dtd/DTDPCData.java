package com.wutka.dtd;

import java.io.*;

/**
 * Represents the #PCDATA keyword in an Element's content spec
 * 
 * @author Mark Wutka
 * 
 */
public class DTDPCData extends DTDItem
{
  public DTDPCData()
  {
  }

  /** Writes out the #PCDATA keyword */
  public void write(PrintWriter out) throws IOException
  {
    out.print("#PCDATA");
    cardinal.write(out);
  }

  public boolean equals(Object ob)
  {
    if (ob == this)
      return true;
    if (!(ob instanceof DTDPCData))
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
    return DTDItemType.DTD_PCDATA;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.wutka.dtd.DTDItem#match(java.lang.String)
   */
  public boolean match(String match)
  {
    return false;
  }

}