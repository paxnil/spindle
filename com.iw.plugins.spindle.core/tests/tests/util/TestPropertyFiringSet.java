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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import com.iw.plugins.spindle.core.util.PropertyFiringSet;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class TestPropertyFiringSet extends PropertyFiringBase
{

    /**
     * Constructor for TestPropertyFiringSet.
     * @param arg0
     */
    public TestPropertyFiringSet(String arg0)
    {
        super(arg0);
    }

    public void testAddPCListenerInConstructor()
    {
        final String eventProperty = "testSet";
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

        PropertyFiringSet set = new PropertyFiringSet(listener, eventProperty);
        set.add(stored);
    }

    public void testAddSameTwice()
    {
        String testOwner = "geoff";
        final String eventProperty = "testSet";
        final String stored = "value";

        PropertyFiringSet set = new PropertyFiringSet(testOwner, eventProperty);
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
        set.addPropertyChangeListener(listener1);
        set.add(stored);

        set.removePropertyChangeListener(listener1);
        PropertyChangeListener listener2 = new ErrorListener();
        set.addPropertyChangeListener(listener2);
        set.add(stored);
    }

    public void testRemoves()
    {

        String testOwner = "geoff";
        final String eventProperty = "testSet";
        final String stored = "value";
        ErrorListener error = new ErrorListener();

        PropertyFiringSet set = new PropertyFiringSet(testOwner, eventProperty);
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
        set.addPropertyChangeListener(listener1);
        set.add(stored);

        set.removePropertyChangeListener(listener1);
        OneShotListener listener2 = new OneShotListener(testOwner, stored, null, eventProperty);
        set.addPropertyChangeListener(listener2);

        set.remove(stored);

        // try and remove same twice
        set.removePropertyChangeListener(listener2);
        set.addPropertyChangeListener(error);
        set.remove(stored);

        //try and remove a non existant value
        // note that the error listener is still registered

        set.remove("dummy");

    }

    public void testaddAll()
    {
        String testOwner = "Porsche Dealership";
        final String eventProperty = "Carrera";
        final String stored = "3 in stock";
        TestListener listener1 = new TestListener(testOwner);
        PropertyFiringSet testSet = new PropertyFiringSet(testOwner, eventProperty);
        PropertyFiringSet testSet2 = new PropertyFiringSet("Dealership", "Carrera");
        testSet2.add("Carrera");
        testSet2.add("Carrera G2");
        testSet2.add("Carrera G3");
        testSet.addAll(testSet2);

    }

    public void testRemoveAll()
    {
        String testOwner = "Porsche Dealership";
        final String eventProperty = "Carrera";
        final String stored = "3 in stock";
        TestListener listener1 = new TestListener(testOwner);
        PropertyFiringSet testSet = new PropertyFiringSet(testOwner, eventProperty);
        PropertyFiringSet testSet2 = new PropertyFiringSet("Dealership", "Carrera 2");
        testSet2.add("Carrera");
        testSet2.add("Carrera G2");
        testSet2.add("Carrera G3");
        testSet.addAll(testSet2);
        testSet.removeAll(testSet2);

    }
    public void testRetainAll()
    {
        String testOwner = "Porsche Dealership";
        final String eventProperty = "Carrera";
        final String stored = "3 in stock";
        TestListener listener1 = new TestListener(testOwner);
        PropertyFiringSet testSet = new PropertyFiringSet(testOwner, eventProperty);
        PropertyFiringSet testSet2 = new PropertyFiringSet("Dealership", "Carrera");
        PropertyFiringSet testSet3 = new PropertyFiringSet("Dealership", "Boxers");
        testSet3.add("Carrera G2");
        testSet2.add("Carrera");
        testSet2.add("Carrera G2");
        testSet2.add("Carrera G3");
        testSet.addAll(testSet2);
        testSet.retainAll(testSet3);

    }

    public void testClear()
    {
        String testOwner = "Porsche Dealership";
        final String eventProperty = "Carrera";
        final String stored = "3 in stock";
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
        PropertyFiringSet testSet = new PropertyFiringSet(testOwner, eventProperty);
        testSet.addPropertyChangeListener(listener1);
        testSet.add(stored);
        testSet.clear();

    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestPropertyFiringSet.class);
    }

}
