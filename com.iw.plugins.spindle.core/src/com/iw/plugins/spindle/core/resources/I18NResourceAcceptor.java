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

package com.iw.plugins.spindle.core.resources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.oro.text.PatternCacheLRU;
import org.apache.oro.text.perl.Perl5Util;
import org.apache.tapestry.util.MultiKey;

import com.iw.plugins.spindle.core.util.Assert;

/**
 *  An acceptor that will accept  all permutations of a file that matches Tapestry I18N specs.
 * <p>The following would match for Hello.gif
 * <ul>
 * <li>Hello_en.gif</li>
 * <li>Hello_en_US.gif</li>
 * <li>Hello_fr.gif</li>
* <li>etc</li>
 * </ul>
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class I18NResourceAcceptor implements IResourceLocationAcceptor
{
    private static Map CachedNamePatterns = new HashMap();
    private static Perl5Util PERL;
    private static final String PatternPrefix = "/^";
    private static String PatternSuffix;

    public static String[] ALL_I18N_SUFFIXES;

    static {
        Locale[] all = Locale.getAvailableLocales();
        List suffixes = new ArrayList();
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < all.length; i++)
        {
            String next = "_" + all[i].toString();
            suffixes.add(next);
            buffer.append(next);
            if (i < all.length - 1)
            {
                buffer.append('|');
            }
        }

        ALL_I18N_SUFFIXES = new String[suffixes.size()];
        suffixes.toArray(ALL_I18N_SUFFIXES);

        PatternSuffix = "(" + buffer.toString() + "){0,1}$/i";
    }

    private String fExtension;
    private String fPattern;
    private ArrayList fResults = new ArrayList();

    public void configure(String fileNameInclExtension)
    {
        if (fileNameInclExtension == null)
            return;
            
        fileNameInclExtension = fileNameInclExtension.trim();

        if (fileNameInclExtension.length() == 0)
            return;

        String baseName = fileNameInclExtension;
        String extension = null;
        int dotx = fileNameInclExtension.lastIndexOf('.');
        if (dotx > 0)
        {
            baseName = fileNameInclExtension.substring(0, dotx);
            extension = fileNameInclExtension.substring(dotx + 1);
        }
        configure(baseName, extension);
    }

    public void configure(String baseName, String extension)
    {
        Assert.isNotNull(baseName);
        fResults.clear();
        fExtension = extension;
        fPattern = null;

        MultiKey key = new MultiKey(new Object[] { baseName, fExtension == null ? "NULL" : fExtension }, false);
        fPattern = (String) CachedNamePatterns.get(key);
        if (fPattern == null)
        {
            fPattern = PatternPrefix + baseName + PatternSuffix;
            CachedNamePatterns.put(key, fPattern);
        }
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceLocationAcceptor#accept(com.iw.plugins.spindle.core.resources.IResourceWorkspaceLocation)
     */
    public boolean accept(IResourceWorkspaceLocation location)
    {
        // stop the lookup if there is no pattern!
        if (fPattern == null)
            return false;

        if (PERL == null)
        PERL = new Perl5Util(new PatternCacheLRU(100));

        String name = location.getName();
        if (name != null && name.trim().length() > 0)
        {
            String foundName = name;
            String foundExtension = null;
            int dotx = name.lastIndexOf('.');
            if (dotx > 0)
            {
                foundName = name.substring(0, dotx);
                foundExtension = name.substring(dotx + 1);
            }
            if (fExtension.equals(foundExtension) && PERL.match(fPattern, foundName))
                fResults.add(location);
        }
        return true;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.core.resources.IResourceLocationAcceptor#getResults()
     */
    public IResourceWorkspaceLocation[] getResults()
    {
        return (IResourceWorkspaceLocation[]) fResults.toArray(new IResourceWorkspaceLocation[fResults.size()]);
    }

//    public static void main(String[] args) throws Exception
//    {
//        Perl5Util util = new Perl5Util();
//
//        String pattern = PatternPrefix + "Test" + PatternSuffix;
//
//        testOld(util, "Test", pattern);
//        testOld(util, "boo_en", pattern);
//        testOld(util, "Test_en", pattern);
//        testOld(util, "Test_en_ca", pattern);
//        testOld(util, "boo", pattern);
//
//        Perl5Matcher matcher = new Perl5Matcher();
//
//        Pattern compiled = (new Perl5Compiler()).compile(pattern,);
//
//        testNew(matcher, "Test", compiled);
//        testNew(matcher, "boo_en", compiled);
//        testNew(matcher, "Test_en", compiled);
//        testNew(matcher, "Test_en_ca", compiled);
//        testNew(matcher, "boo", compiled);
//
//    }
//
//    private static void testOld(Perl5Util util, String test, String pattern)
//    {
//        System.out.print(test + "[ " + pattern.substring(0, 7) + "]");
//        if (util.match(pattern, test))
//        {
//            System.out.println("pass");
//        } else
//        {
//            System.out.println("fail");
//        }
//
//    }
//
//    private static void testNew(Perl5Matcher util, String test, Pattern pattern)
//    {
//        System.out.print(test + "[ " + pattern.getPattern().substring(0, 7) + "]");
//        if (util.matches(test, pattern))
//        {
//            System.out.println("pass");
//        } else
//        {
//            System.out.println("fail");
//        }
//
//    }

}
