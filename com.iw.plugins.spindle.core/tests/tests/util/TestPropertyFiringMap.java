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

import java.beans.PropertyChangeEvent;
import java.util.Map;

import com.iw.plugins.spindle.core.util.PropertyFiringMap;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TestPropertyFiringMap extends PropertyFiringBase
{
    String testOwner = "Brian";
    String eventProperty = "Brians Car";
    /**
     * Constructor for TestPropertyFiringMap.
     * @param arg0
     */
    public TestPropertyFiringMap(String arg0)
    {
        super(arg0);
    }

    private TestListener createDefaultListener()
    {
        return createListener(testOwner);
    }

    private TestListener createListener(String testOwner)
    {
        TestListener result = new TestListener(testOwner);

        return result;
    }
    private PropertyFiringMap createDefaultMap()
    {
        return createMap(testOwner, eventProperty);
    }

    private PropertyFiringMap createMap(String testOwner, String eventProperty)
    {
        PropertyFiringMap result = new PropertyFiringMap(testOwner, eventProperty);

        return result;
    }
    public void testAddPCListenerInConstructor()
    {
        final String eventProperty = "testMap";
        final String stored = "value";

        TestListener listener = new TestListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                super.propertyChange(evt);
                assertEquals(eventPropertyName, eventProperty);
                assertNull(eventOldValue);
                assertEquals(eventNewValue, stored);
            }
        };

        PropertyFiringMap map = new PropertyFiringMap(listener, eventProperty);
        map.put("blah", stored);
    }

    public void testRemovePropertyChangeListener()
    {
        PropertyFiringMap map = createDefaultMap();
        TestListener listener1 = new OneShotListener("Brian", null, "Carrera", "Brians Car");
        // TestListener listener2 = createListener("Geoff");
        map.addPropertyChangeListener(listener1);
        map.put("Porsche", "Carrera");
        map.removePropertyChangeListener(listener1);
        map.put("Porsche", "Carrera G3");

    }

    public void testAddAll()
    {
        Map testMap = createMap("Brian", "Brians Car");
        testMap.put("porsche", "carrera");
        testMap.put("subaru", "wrx");
        testMap.put("honda", "civic");
        PropertyFiringMap map = createDefaultMap();
        map.putAll(testMap);

    }

    public void testRemove()
    {
        String testOwner = "geoff";
        String propertyName = "geoffsKids";

        PropertyFiringMap map = new PropertyFiringMap(testOwner, propertyName);

        map.put("son", "dean");
        TestListener listener = new OneShotListener(testOwner, "dean", null, propertyName);
        map.addPropertyChangeListener(listener);

        map.remove("son");

    }

    public void testPutAgain()
    {
        String testOwner = "chris";
        String testPropertyName = "chrisCars";

        PropertyFiringMap map = new PropertyFiringMap(testOwner, testPropertyName);
        map.put("current-ride", "porsche");

        TestListener listener = new OneShotListener(testOwner, "porsche", "ferrari", testPropertyName);
        map.addPropertyChangeListener(listener);

        map.put("current-ride", "ferrari");

    }

    public void testClear()
    {

        String testOwner = "chris";
        String testPropertyName = "chrisCars";

        PropertyFiringMap map = new PropertyFiringMap(testOwner, testPropertyName);
        map.put("current-ride", "porsche");
        map.put("dream-ride", "ferrari");

        OneShotListener listener = new OneShotListener(testOwner, null, map, testPropertyName);
        map.addPropertyChangeListener(listener);

        map.clear();
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestPropertyFiringMap.class);
    }

}
