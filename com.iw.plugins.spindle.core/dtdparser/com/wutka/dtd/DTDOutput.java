package com.wutka.dtd;

import java.io.*;

/**
 * Defines the method used for writing DTD information to a PrintWriter
 * 
 * @author Mark Wutka
 * 
 */
public interface DTDOutput
{
  public void write(PrintWriter out) throws IOException;
}