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

package tests.Scanners.mr;

import org.apache.tapestry.spec.SpecFactory;

import tests.Parser.mr.MRBaseParserTest;

import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.spec.TapestryPluginSpecFactory;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class BaseComponentScannerTest extends MRBaseParserTest
{
    protected MockComponentScanner scanner;
    protected SpecFactory factory;
    protected IScannerValidator validator;
    /**
     * 
     */
    protected BaseComponentScannerTest()
    {
        super();
    }

    /**
     * @param arg0
     */
    protected BaseComponentScannerTest(String arg0)
    {
        super(arg0);
    }

    public void setUp() throws Exception
    {
        super.setUp();
        factory = new TapestryPluginSpecFactory();
        validator = new BaseValidator();
        scanner = new MockComponentScanner(parser, factory, validator);
    }

}
