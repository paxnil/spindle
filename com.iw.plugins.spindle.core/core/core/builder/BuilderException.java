package core.builder;

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

/**
 * Exception that is thrown internally by the Builds
 * 
 * 
 * @author glongman@gmail.com
 */
public class BuilderException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public BuilderException() {
		super();
	}

	public BuilderException(String arg0) {
		super(arg0);
	}

	public BuilderException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public BuilderException(Throwable arg0) {
		super(arg0);
	}
}