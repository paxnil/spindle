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

package tests.util;

import java.util.Set;

import junit.framework.TestCase;

import com.iw.plugins.spindle.core.util.OrderPreservingSet;

/**
 *  TODO Needs to be complete!
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class OrderPreservingSetTests extends TestCase
{

    String[] defaultValues = { "beagle", "calico", "field", "domestic", "chilean" };

    /**
     * Constructor for OrderPreservingSetTests.
     * @param arg0
     */
    public OrderPreservingSetTests(String arg0)
    {
        super(arg0);
    }

    /**
     * bgarson - May 6th 2003
     */
    private Set createDefaultSet()
    {
        return createSet(defaultValues);
    }

    private Set createSet(String[] values)
    {
        OrderPreservingSet result = new OrderPreservingSet();
        for (int i = 0; i < values.length; i++)
        {
            result.add(values[i]);
        }
        return result;
    }

    //tests addAll, contains and containsAll
    public void testAddall()
    {
        Set set1 = createDefaultSet();
        Set set2 = new OrderPreservingSet();
        set2.addAll(set1);
        set2.containsAll(set1);
        set2.contains("beagle");
    }

    public void testIsEmpty()
    {
        Set set1 = createDefaultSet();
        if (set1.isEmpty())
        {
            fail("set is not empty!!");
        }
        set1.clear();
        if (!set1.isEmpty())
        {
            fail("set is empty!");
        }
        set1.add("hello");
        if (set1.isEmpty())
        {
            fail("set is not empty!");
        }
    }

    public void testRemoveAll()
    {
        Set set1 = createDefaultSet();
        Set set2 = new OrderPreservingSet();
        Set set3 = new OrderPreservingSet();
        String[] values = { "table", "chair", "door", "house" };
        for (int i = 0; i < values.length; i++)
        {
            set3.add(values[i]);
        }
        set2.addAll(set1);
        set2.addAll(set3);
        set2.removeAll(set1);
        if (set2.containsAll(set1))
        {
            fail("set1 was removed!");
        }
        int oldsize = set2.size();
        set2.remove("door");
        assertEquals("Remove & Size not functioning properly", set2.size(), oldsize - 1);

    }

    public void testRetainAll()
    {
        String[] values = { "table", "chair", "door", "house" };
        String[] valhaf = { "chair", "door" };
        Set set1 = createSet(values);
        Set set2 = createSet(valhaf);
        set1.retainAll(set2);
        assertEquals("they should be equal", set1.size() , set2.size());
        assertTrue("should contain", set2.containsAll(set1));

    }

    public void testtoArray(){
        Set set1 = createDefaultSet();
        Object x = set1.toArray();
        set1.add(x);
        assertTrue("toArray() didn't work",set1.contains(x));
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(OrderPreservingSetTests.class);
    }

}
