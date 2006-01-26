package net.sf.spindle.core.resources;
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

import java.io.InputStream;

import org.apache.hivemind.Resource;


/**
 * Extends
 * <code>org.apache.tapestry.IResourceLocation<code> to record additional
 * bits of information describing a Tapestry artifact found in the workspace.
 * 
 * @author glongman@gmail.com
 * 
 * @see org.apache.tapestry.IResourceLocation
 */

public interface ICoreResource extends Resource, ResourceExtension
{

  public boolean isClasspathResource();

  public boolean isBinaryResource();

  /**
   * Returns an open input stream on the contents of this descriptor. The caller
   * is responsible for closing the stream when finished.
   */
  public InputStream getContents();
  
  public boolean clashesWith(ICoreResource resource);
  
  public boolean isFolder();

}