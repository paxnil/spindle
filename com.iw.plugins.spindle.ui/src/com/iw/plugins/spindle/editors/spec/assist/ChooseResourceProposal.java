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

package com.iw.plugins.spindle.editors.spec.assist;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;

import com.iw.plugins.spindle.Images;
import com.iw.plugins.spindle.core.resources.AbstractRootLocation;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.spec.SpecEditor;

/**
 * Proposal that gets its value from the user!
 * 
 * @author glongman@gmail.com
 */
public class ChooseResourceProposal implements ICompletionProposal
{
  public static final Filter ALWAYS_MATCH = new Filter()
  {
    public boolean matches(String candidate)
    {
      return true;
    }
  };

  public static final Filter NEVER_MATCH = new Filter()
  {
    public boolean matches(String candidate)
    {
      return false;
    }
  };

  public static final Filter EXCLUDE_PACKAGES;
  public static final Filter INCLUDE_PAGE_EXTENSION;
  public static final Filter INCLUDE_JWC_EXTENSION;
  public static final Filter INCLUDE_LIBRARY_EXTENSION;

  public static final Filter ASSET_EXCLUDE_EXTENSIONS;

  public static final Filter EXCLUDE_PACKAGE_DOT_HTML = new Filter()
  {
    public boolean matches(String candidate)
    {
      return "package.html".equals(candidate);
    }
  };

  public static final Filter EXCLUDE_CVS_FOLDERS = new Filter()
  {
    public boolean matches(String candidate)
    {
      return "CVS".equals(candidate);
    }
  };

  static
  {
    EXCLUDE_PACKAGES = new Filter(Filter.STARTS_WITH);
    EXCLUDE_PACKAGES.addFilter("com.sun");
    EXCLUDE_PACKAGES.addFilter("java.");
    EXCLUDE_PACKAGES.addFilter("sun.");
    EXCLUDE_PACKAGES.addFilter("javax.");
    EXCLUDE_PACKAGES.addFilter("CVS");
    EXCLUDE_PACKAGES.addFilter("META-INF");

    INCLUDE_PAGE_EXTENSION = new Filter(Filter.EXACT);
    INCLUDE_PAGE_EXTENSION.addFilter("page");

    INCLUDE_JWC_EXTENSION = new Filter(Filter.EXACT);
    INCLUDE_JWC_EXTENSION.addFilter("jwc");

    INCLUDE_LIBRARY_EXTENSION = new Filter(Filter.EXACT);
    INCLUDE_LIBRARY_EXTENSION.addFilter("library");

    ASSET_EXCLUDE_EXTENSIONS = new Filter(Filter.EXACT);
    ASSET_EXCLUDE_EXTENSIONS.addFilter("page");
    ASSET_EXCLUDE_EXTENSIONS.addFilter("library");
    ASSET_EXCLUDE_EXTENSIONS.addFilter("application");
    ASSET_EXCLUDE_EXTENSIONS.addFilter("class");
    ASSET_EXCLUDE_EXTENSIONS.addFilter("jar");

  }

  /**
   * 
   * Filter - no dupe checking is done - defaults to EXACT match
   */
  public static class Filter
  {
    public static final int EXACT = 0;
    public static final int STARTS_WITH = 1;
    public static final int ENDS_WITH = 2;

    private String[] filters;
    private int matchHint;

    public Filter(int matchType)
    {
      Assert.isLegal(matchType >= EXACT && matchType <= ENDS_WITH);
      matchHint = matchType;
    }

    public Filter()
    {
      this(STARTS_WITH);
    }

    void addFilter(String filter)
    {
      if (filters == null)
      {
        filters = new String[]{filter};
      } else
      {
        String[] newFilter = new String[filters.length + 1];
        System.arraycopy(filters, 0, newFilter, 1, filters.length);
        newFilter[0] = filter;
        filters = newFilter;
      }
    }

    public boolean matches(String candidate)
    {
      if (filters == null || candidate == null)
        return false;
      for (int i = 0; i < filters.length; i++)
      {
        switch (matchHint)
        {
          case EXACT :
            if (candidate.equals(filters[i]))
              return true;
          case STARTS_WITH :
            if (candidate.startsWith(filters[i]))
              return true;
          case ENDS_WITH :
            if (candidate.endsWith(filters[i]))
              return true;
        }

      }
      return false;
    }
  }

  private SpecEditor editor;
  private AbstractRootLocation root;

  private String chosenAsset;

  private int documentOffset;
  private int replacementOffset;
  private int replacementLength;
  private Filter containerExclusionFilter = NEVER_MATCH;
  private Filter fileExclusionFilter = NEVER_MATCH;
  private Filter extensionExclusionFilter = NEVER_MATCH;
  private Filter extensionInclusionFilter = ALWAYS_MATCH;
  private boolean previousCAState;
  private boolean allowRelativePaths;

  public ChooseResourceProposal(SpecEditor specEditor, boolean previousCAState,
      AbstractRootLocation rootObject, int documentOffset, int replacementOffset,
      int replacementLength)
  {
    this.previousCAState = previousCAState;
    Assert.isNotNull(specEditor);
    Assert.isNotNull(rootObject);
    editor = specEditor;
    root = rootObject;

    this.documentOffset = documentOffset;
    this.replacementOffset = replacementOffset;
    this.replacementLength = replacementLength;
  }

  public AbstractRootLocation getRootLocation()
  {
    return root;
  }

  public void setChosenAsset(String chosenAsset)
  {
    this.chosenAsset = chosenAsset;
  }

  public void setContainerExclusionFilter(Filter filter)
  {
    containerExclusionFilter = filter;
  }

  public Filter getContainerExclusionFilter()
  {
    return containerExclusionFilter;
  }

  public void setFileExclusionFilter(Filter filter)
  {
    fileExclusionFilter = filter;
  }

  public Filter getFileExclusionFilter()
  {
    return fileExclusionFilter;
  }

  public void setExtensionExclusionFilter(Filter filter)
  {
    extensionExclusionFilter = filter;
  }

  public Filter getExtensionExclusionFilter()
  {
    return extensionExclusionFilter;
  }

  public void setExtensionInclusionFilter(Filter filter)
  {
    extensionInclusionFilter = filter;
  }

  public Filter getExtensionlnclusionFilter()
  {
    return extensionInclusionFilter;
  }

  public void setAllowRelativePaths(boolean flag)
  {
    allowRelativePaths = flag;
  }

  public boolean getAllowRelativePaths()
  {
    return allowRelativePaths;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#apply(org.eclipse.jface.text.IDocument)
   */
  public void apply(IDocument document)
  {
    if (chosenAsset == null)
    {
      editor.invokeAssetChooser(this);
    } else
    {
      if (chosenAsset.length() == 0)
      {
        chosenAsset = null;
        return;
      }

      try
      {
        document.replace(replacementOffset, replacementLength, chosenAsset);

      } catch (BadLocationException x)
      {
        // ignore
      } finally
      {
        ContentAssistant assistant = editor.getContentAssistant();
        if (assistant != null)
          assistant.enableAutoInsert(previousCAState);
      }

    }

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getAdditionalProposalInfo()
   */
  public String getAdditionalProposalInfo()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getContextInformation()
   */
  public IContextInformation getContextInformation()
  {
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getDisplayString()
   */
  public String getDisplayString()
  {
    return "Choose Asset";
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getImage()
   */
  public Image getImage()
  {
    return Images.getSharedImage("file_obj.gif");
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.contentassist.ICompletionProposal#getSelection(org.eclipse.jface.text.IDocument)
   */
  public Point getSelection(IDocument document)
  {
    if (chosenAsset == null)
      return new Point(documentOffset, 0);

    return new Point(replacementOffset + chosenAsset.length(), 0);
  }

}