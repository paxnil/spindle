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

package com.iw.plugins.spindle.editors.spec;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.documentsAndModels.StorageDocumentModelProvider;

/**
 * Document provider for specs that come out of Jar files
 * 
 * Users should not instantiate. Rather call
 * UIPlugin.getDefault().getSpecStorageDocumentProvider()
 * 
 * @author glongman@gmail.com
 * @version $Id: SpecStorageDocumentProvider.java,v 1.3 2003/11/02 12:45:14
 *          glongman Exp $
 */
public class SpecStorageDocumentProvider extends StorageDocumentModelProvider
{

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.editors.documentsAndModels.FileDocumentModelProvider#getSyntaxPartitioner()
   */
  protected IDocumentPartitioner getSyntaxPartitioner()
  {
    return UIPlugin.getDefault().getXMLTextTools().createXMLPartitioner();
  }

  protected void setDocumentContent(
      IDocument document,
      InputStream contentStream,
      String encoding) throws CoreException
  {

    Reader in = null;

    try
    {

      in = new InputStreamReader(new BufferedInputStream(contentStream), "UTF-8");
      StringBuffer buffer = new StringBuffer();
      char[] readBuffer = new char[2048];
      int n = in.read(readBuffer);
      while (n > 0)
      {
        buffer.append(readBuffer, 0, n);
        n = in.read(readBuffer);
      }

      document.set(buffer.toString());

    } catch (IOException x)
    {
      UIPlugin.log(x);
    } finally
    {
      if (in != null)
      {
        try
        {
          in.close();
        } catch (IOException x)
        {}
      }
    }
  }

}