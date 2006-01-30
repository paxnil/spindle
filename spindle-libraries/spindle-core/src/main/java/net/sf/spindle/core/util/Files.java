package net.sf.spindle.core.util;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;


/**
 * File utilities
 * 
 * 
 * @author glongman@gmail.com
 */
public class Files
{

  public static void copy(String inputFilename, String outputFilename) throws IOException
  {
    copy(new File(inputFilename), new File(outputFilename));
  }

  public static void copy(File inputFile, File outputFile) throws IOException
  {
    BufferedInputStream fr = new BufferedInputStream(new FileInputStream(inputFile));
    BufferedOutputStream fw = new BufferedOutputStream(new FileOutputStream(outputFile));
    byte[] buf = new byte[8192];
    int n;
    while ((n = fr.read(buf)) >= 0)
      fw.write(buf, 0, n);
    fr.close();
    fw.close();
  }

  public static String readFileToString(InputStream contentStream, String encoding) throws IOException
  {
    Reader in;
    if (encoding == null)
      in = new BufferedReader(new InputStreamReader(contentStream));
    else
      in = new BufferedReader(new InputStreamReader(contentStream, encoding));
    int chunkSize = contentStream.available();
    StringBuffer buffer = new StringBuffer(chunkSize);
    int c = -1;
    while ((c = in.read()) != -1)
    {
      buffer.append((char) c);
    }
    in.close();
    return buffer.toString();
  }

}