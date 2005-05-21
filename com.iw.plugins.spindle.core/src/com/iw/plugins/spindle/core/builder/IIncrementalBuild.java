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

package com.iw.plugins.spindle.core.builder;

/**
 * Common Question asked of all kinds of Incremental Builders
 * 
 * @author glongman@gmail.com
 * 
 */
public interface IIncrementalBuild extends IBuild
{
  /**
   * A question asked of Incremental Builds by the TapestryBuilder.
   * <p>
   * Answering false prompts a Full AbstractBuild
   * 
   * @return true if an incremental build is indicated, false otherwise.
   */
  public boolean canIncrementalBuild();

  /**
   * A question asked of Incremental Builds by the TapestryBuilder.
   * <p>
   * Answering false prompts aborts an incremental build
   * 
   * @return true if an build is indicated, false otherwise.
   */
  public boolean needsIncrementalBuild();

}