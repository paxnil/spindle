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



package com.iw.plugins.spindle.core.spec;

import java.util.List;

import org.apache.tapestry.spec.ParameterSpecification;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface IParameterHolder {
	
	public ParameterSpecification getParameter(String name);
	public void setParameter(String name, PluginParameterSpecification spec);
	public void addParameter(String name, ParameterSpecification spec);
	public List getParameterNames();
	public void removeParameter(String name);

}
