package com.wutka.dtd;

import java.io.*;

/**
 * Represents any item in the DTD
 * 
 * @author Mark Wutka
 * 
 */
public abstract class DTDItem implements DTDOutput, Cloneable
{
  /** Indicates how often the item may occur */
  public DTDCardinal cardinal;

  public DTDItem()
  {
    cardinal = DTDCardinal.NONE;
  }

  public DTDItem(DTDCardinal aCardinal)
  {
    cardinal = aCardinal;
  }

  public abstract DTDItemType getItemType();

  public Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }

  /** Writes out a declaration for this item */
  public abstract void write(PrintWriter out) throws IOException;

  public abstract boolean match(String match);

  public boolean equals(Object ob)

  {
    if (ob == this)
      return true;
    if (!(ob instanceof DTDItem))
      return false;

    DTDItem other = (DTDItem) ob;

    if (cardinal == null)
    {
      if (other.cardinal != null)
        return false;
    } else
    {
      if (!cardinal.equals(other.cardinal))
        return false;
    }

    return true;
  }

  /** Sets the cardinality of the item */
  public void setCardinal(DTDCardinal aCardinal)
  {
    cardinal = aCardinal;
  }

  /** Retrieves the cardinality of the item */
  public DTDCardinal getCardinal()
  {
    return cardinal;
  }

  public String toString()
  {
    try
    {
      StringWriter stw = new StringWriter();
      PrintWriter writer = new PrintWriter(stw);
      write(writer);
      return stw.toString();
    } catch (IOException e)
    {
      return this.toString();
    }
  }
}