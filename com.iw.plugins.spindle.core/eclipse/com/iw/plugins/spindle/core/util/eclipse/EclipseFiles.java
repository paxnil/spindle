package com.iw.plugins.spindle.core.util.eclipse;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * @author gwl
 */
public class EclipseFiles
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

    

}
