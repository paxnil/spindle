package com.iw.plugins.spindle.core.util;
/* ***** BEGIN LICENSE BLOCK *****
 * Version: MPL 1.1
 *
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Original Code is Spindle, an Eclipse Plugin for Tapestry.
 *
 * The Initial Developer of the Original Code is
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * File utilities
 * 
 * 
 * @author glongman@gmail.com
 */
public class Files
{

  public static String readTextFile(IFile f) throws IOException
  {

    StringBuffer buf = new StringBuffer();

    BufferedReader in;
    try
    {
      in = new BufferedReader(new InputStreamReader(f.getContents()));
    } catch (CoreException e)
    {
      throw new IOException(e.getMessage());
    }
    String inputLine;
    while ((inputLine = in.readLine()) != null)
    {
      buf.append(inputLine);
      buf.append('\n');
    }

    in.close();
    return buf.toString();

  }

  public static void toTextFile(IFile f, String content, IProgressMonitor monitor) throws IOException
  {
    try
    {
      if (!f.exists())
      {
        f.create(new ByteArrayInputStream(content.getBytes()), true, null);
      } else
      {
        f.setContents(new ByteArrayInputStream(content.getBytes()), true, true, null);
      }
    } catch (CoreException e)
    {
      throw new IOException(e.getMessage());
    }
  }

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

  public static String readPropertyInXMLFile(IFile file, String property) throws IOException
  {
    if (!file.exists())
      return null;

    String content = readTextFile(file);
    int startTagIdx = content.indexOf("<" + property + ">");
    if (startTagIdx < 0)
      return null;

    int endTagIdx = content.indexOf("</" + property + ">");
    return content.substring(startTagIdx + property.length() + 2, endTagIdx);
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