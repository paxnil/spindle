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

package tests.perl;

import java.util.Locale;

import com.iw.plugins.spindle.core.resources.templates.TemplateFinder;

import junit.framework.TestCase;

/**
 *  Tests for the Perl expression used to find localized Templates.
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class PerlTest extends TestCase
{

    public PerlTest(String arg0)
    {
        super(arg0);
    }

    public void test()
    {

        String expression = TemplateFinder.PatternPrefix + "Boo" + TemplateFinder.PatternSuffix;
        System.err.println(expression);
        long start = System.currentTimeMillis();
        boolean match = TemplateFinder.Perl.match(expression, "Boo_en");
        System.out.println("time = " + (System.currentTimeMillis() - start));
        assertTrue(match);

        expression = TemplateFinder.PatternPrefix + "Boo" + TemplateFinder.PatternSuffix;
        System.err.println(expression);
        start = System.currentTimeMillis();
        match = TemplateFinder.Perl.match(expression, "Boo");
        System.out.println("time = " + (System.currentTimeMillis() - start));
        assertTrue(match);

        expression = TemplateFinder.PatternPrefix + "Moo.loo" + TemplateFinder.PatternSuffix;
        System.err.println(expression);
        start = System.currentTimeMillis();
        match = TemplateFinder.Perl.match(expression, "Boo");
        System.out.println("time = " + (System.currentTimeMillis() - start));
        assertFalse(match);
    }
    
    public void testSuperExpression() {
        
        Locale [] all = Locale.getAvailableLocales();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < all.length; i++)
        {
            buffer.append("_"+all[i].toString());
            if (i<all.length -1) {
                buffer.append('|');
            }
        }
        String prefix = "/^";
        String suffix = "("+buffer.toString()+"){0,1}$/i";
        System.out.println(suffix);
        String expression = prefix + "Moo.loo" + suffix;
        System.err.println(expression);
        long start = System.currentTimeMillis();
        boolean match = TemplateFinder.Perl.match(expression, "Moo.loo");
        System.out.println("time = " + (System.currentTimeMillis() - start));
        assertTrue(match);

        expression = prefix + "BOB" + suffix;
        System.err.println(expression);
        start = System.currentTimeMillis();
        match = TemplateFinder.Perl.match(expression, "BOB_pp");
        System.out.println("time = " + (System.currentTimeMillis() - start));
        assertFalse(match);
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PerlTest.class);
    }

}
