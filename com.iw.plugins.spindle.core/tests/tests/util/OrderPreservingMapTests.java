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

package tests.util;

import java.util.Iterator;
import java.util.Map;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import com.iw.plugins.spindle.core.util.OrderPreservingMap;

/**
 *  Test the Order Preserving Map
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class OrderPreservingMapTests extends TestCase
{

    String[] defaultKeys = { "dog", "cat", "mouse", "gerbil", "degu" };
    String[] defaultValues = { "beagle", "calico", "field", "domestic", "chilean" };

    /**
     * Constructor for OrderPreservingMap.
     * @param arg0
     */
    public OrderPreservingMapTests(String arg0)
    {
        super(arg0);
    }

    private Map createDefaultMap()
    {
        return createMap(defaultKeys, defaultValues);
    }

    private Map createMap(String[] keys, String[] values)
    {
        assertEquals(keys.length, values.length);
        OrderPreservingMap result = new OrderPreservingMap();
        for (int i = 0; i < keys.length; i++)
        {
            result.put(keys[i], values[i]);
        }
        return result;
    }

    private void containsAllKeys(Map map, String[] keys)
    {
        for (int i = 0; i < keys.length; i++)
        {
            if (!map.containsKey(keys[i]))
            {
                fail("map is missing key:" + keys[i]);
            }
        }
    }

    /**
    * @param map
    * @param keys
    * @author bgarson
    * @return
    */

    public void testValues()
    {
        Map map = createDefaultMap();
        Map map2 = createDefaultMap();
        if (!(map.values().containsAll(map2.values())))
        {
            fail("this should have passed");
        }

        //create a new map
        OrderPreservingMap map3 = new OrderPreservingMap();
        for (int i = 0; i < defaultKeys.length; i++)
        {
            // TODO Did you notice that you are putting the values in as keys and the keys in as values?
            // bad style. You could make new String arrays called map3keys, map3values   
            // String [] map3Keys = {"a","b", yadda yadda};
            // String [] map3Values = {"1","2", yadda yadda};       
            map3.put(defaultValues[i], defaultKeys[i]);
        }

        //test to make sure the values aren't the same
        if ((map.values().containsAll(map3.values())))
        {
            fail("this should have failed");
        }
        map.clear();
        if ((map.values().containsAll(map2.values())))
        {
            fail("this should have failed");
        }
    }

    public void testIsEmpty()
    {
        OrderPreservingMap mapInOrder = new OrderPreservingMap();
        if (!mapInOrder.isEmpty())
        {
            fail("map should have been empty");
        }

        for (int i = 0; i < defaultKeys.length; i++)
        {
            mapInOrder.put(defaultKeys, defaultValues);
        }
        if (mapInOrder.isEmpty())
        {
            fail("map should have 5 values in it");
        }
        mapInOrder.clear();
        String[] k1 = { "a", "b", "c" };
        String[] v1 = { "A", "B", "C" };
        Map map = createMap(k1, v1);

        mapInOrder.putAll(map);
        assertTrue(mapInOrder.size() == 3);
    }

    public void testEntrySet()
    {
        OrderPreservingMap mapresult = new OrderPreservingMap();
        try
        {
            mapresult.entrySet();
            fail("we expected this to fail but it didn't");
        } catch (RuntimeException e)
        {}
    }

    private void checkKeyValueOrder(Map map, String[] keys, String[] values)
    {
        assertEquals("invalid test input", keys.length, values.length);
        int i = 0;
        for (Iterator iter = map.keySet().iterator(); iter.hasNext();)
        {
            String expectedKey = keys[i];
            String expectedValue = values[i];
            String foundKey = (String) iter.next();
            assertEquals("key found in wrong order:" + foundKey, expectedKey, foundKey);
            Object foundValue = map.get(foundKey);
            assertEquals("expected value '" + expectedValue + "' but got '" + foundValue, expectedValue, foundValue);
            i++;
        }
    }

    public void testPutGet()
    {
        Map map = createDefaultMap();
        containsAllKeys(map, defaultKeys);
        checkKeyValueOrder(map, defaultKeys, defaultValues);

    }

    public void test2()
    {

        String[] k1 = { "a", "b", "c" };
        String[] v1 = { "A", "B", "C" };

        Map map = createMap(k1, v1);
        assertTrue(map.size() == 3);
        map.put("a", "Z");
        assertTrue(map.get("a") == "Z");
        assertTrue(map.size() == 3);
        try
        {
            checkKeyValueOrder(map, k1, v1);
            fail("shoulda failed!");
        } catch (AssertionFailedError error)
        {
            //do nothing this is expected
        }
        v1[0] = "Z";
        checkKeyValueOrder(map, k1, v1);
        map.put("c", "ZZZ");
        try
        {
            checkKeyValueOrder(map, k1, v1);
            fail("shoulda failed!");
        } catch (AssertionFailedError error2)
        {}
        v1[2] = "ZZZ";
        checkKeyValueOrder(map, k1, v1);

        map.put("a", null);
        assertTrue(!map.containsKey("a"));
        assertTrue(!map.containsValue("Z"));
        checkKeyValueOrder(map, new String[] { "b", "c" }, new String[] { "B", "ZZZ" });

        map.put("a", "blows");
        checkKeyValueOrder(map, new String[] { "b", "c", "a" }, new String[] { "B", "ZZZ", "blows" });

        assertNull(map.remove("PPPPP"));
        checkKeyValueOrder(map, new String[] { "b", "c", "a" }, new String[] { "B", "ZZZ", "blows" });

        map.remove("c");
        checkKeyValueOrder(map, new String[] { "b", "a" }, new String[] { "B", "blows" });

        try
        {
            map.put(null, "gomer");
            fail("allowed null key");
        } catch (NullPointerException e)
        {}

        try
        {
            map.put("gomer", null);
            fail("allowed null value");
        } catch (NullPointerException e)
        {}

        map.remove(null);
        checkKeyValueOrder(map, new String[] { "b", "a" }, new String[] { "B", "blows" });
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(OrderPreservingMapTests.class);
    }

}
