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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.core.resources.search;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IStorage;

import com.iw.plugins.spindle.core.builder.TapestryBuilder;

/**
 * Acceptor that will accept/reject things based on the flags set in it.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id: AbstractTapestrySearchAcceptor.java,v 1.3 2003/08/14 19:11:48
 *          glongman Exp $
 */
public abstract class AbstractTapestrySearchAcceptor implements ISearchAcceptor
{

  public static final int ACCEPT_NONE = 0x0100000;

  public static final int ACCEPT_LIBRARIES = 0x0000001;

  /**
   * Accept flag for specifying components.
   */
  public static final int ACCEPT_COMPONENTS = 0x00000002;
  /**
   * Accept flag for specifying application.
   */
  public static final int ACCEPT_APPLICATIONS = 0x00000004;
  /**
   * Accept flag for specifying HTML files
   */
  public static final int ACCEPT_HTML = 0x00000008;
  /**
   * Accept flag for specifying page files
   */
  public static final int ACCEPT_PAGES = 0x00000010;
  /**
   * Accept flag for specifying script files
   */
  public static final int ACCEPT_SCRIPT = 0x00000020;
  /**
   * Accept flag for specifying any tapestry files
   */
  public static final int ACCEPT_ANY = 0x00000100;

  private List fSeekExtensions = Arrays.asList(TapestryBuilder.KnownExtensions);

  private List fResults = new ArrayList();

  private int fAcceptFlags;

  public AbstractTapestrySearchAcceptor()
  {
    reset(ACCEPT_ANY);
  }

  public AbstractTapestrySearchAcceptor(int acceptFlags)
  {
    reset(acceptFlags);
  }

  public void reset()
  {
    fResults.clear();
  }

  public void reset(int flags)
  {
    reset();
    this.fAcceptFlags = flags;
  }

  protected boolean acceptAsTapestry(IStorage storage)
  {
    String extension = storage.getFullPath().getFileExtension();
    if (!fSeekExtensions.contains(extension))
      return false;

    if ((fAcceptFlags & ACCEPT_ANY) != 0)
      return true;

    if ("jwc".equals(extension) && (fAcceptFlags & ACCEPT_COMPONENTS) == 0)
      return false;

    if ("application".equals(extension) && (fAcceptFlags & ACCEPT_APPLICATIONS) == 0)
      return false;

    if ("html".equals(extension) && (fAcceptFlags & ACCEPT_HTML) == 0)
      return false;

    if ("library".equals(extension) && (fAcceptFlags & ACCEPT_LIBRARIES) == 0)
      return false;

    if ("page".equals(extension) && (fAcceptFlags & ACCEPT_PAGES) == 0)
      return false;

    if ("script".equals(extension) && (fAcceptFlags & ACCEPT_SCRIPT) == 0)
      return false;

    return true;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.iw.plugins.spindle.core.resources.search.ISearchAcceptor#accept(java.lang.Object,
   *      org.eclipse.core.resources.IStorage)
   */
  public final boolean accept(Object parent, IStorage storage)
  {
    if (!acceptAsTapestry(storage))
      return true; // continue the search

    return acceptTapestry(parent, storage);
  }

  /** return false to abort the search * */
  public abstract boolean acceptTapestry(Object parent, IStorage storage);
}