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
import java.util.List;
import java.util.ListIterator;

import com.iw.plugins.spindle.core.util.PropertyFiringList;

/**
 *  TODO Needs to be complete
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TestPropertyFiringList extends PropertyFiringBase
{

    String testOwner = "geoff";
    final String eventProperty = "testSet";
    final String stored = "value";

    /**
     * Constructor for TestPropertyFiringList.
     * @param arg0
     */
    public TestPropertyFiringList(String arg0)
    {
        super(arg0);
    }

    private PropertyFiringList createDefaultList()
    {
        return createList(testOwner, eventProperty);
    }

    private PropertyFiringList createList(String testOwner, String eventProperty)
    {
        PropertyFiringList result = new PropertyFiringList(testOwner, eventProperty);

        return result;
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

    public void testAddPCListenerInConstructor()
    {
        final String eventProperty = "testList";
        final String stored = "value";

        TestListener listener = new TestListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                super.propertyChange(evt);
                assertEquals(propertyName, eventProperty);
                assertNull(oldValue);
                assertEquals(newValue, stored);
            }
        };

        PropertyFiringList map = new PropertyFiringList(listener, eventProperty);
        map.add(stored);
    }

    public void testAdds()
    {

        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        list.addPropertyChangeListener(listener1);
        list.add(stored);

        list.add(stored);
        assertTrue(list.size() == 2);
        assertTrue(list.contains(stored));
        assertTrue(list.indexOf(stored) != list.lastIndexOf(stored));
        list.add(2, stored);
        assertTrue(list.size() == 3);
        assertTrue(list.contains(stored));
        assertTrue(list.indexOf(stored) != list.lastIndexOf(stored));
    }

    public void testRemovePropertyChangeListener()
    {
        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        list.addPropertyChangeListener(listener1);
        list.removePropertyChangeListener(listener1);
    }

    public void testClear()
    {
        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        list.addPropertyChangeListener(listener1);
        list.clear();
        assertTrue(list.size() == 0);

    }

    public void testRemoveBoolversion()
    {
        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        list.add(listener1);
        assertTrue(list.size() == 1);
        list.remove(listener1);
        assertTrue(list.size() == 0);

    }

    public void testAddAll()
    {
        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        testOwner = "Brian";
        PropertyFiringList list2 = new PropertyFiringList(testOwner, eventProperty);
        TestListener listener2 = new TestListener(testOwner);
        list2.add(listener1);
        list2.add(listener2);
        list.addAll(0, list2);
        assertTrue(list.containsAll(list2));
        list.remove(listener1);
        assertFalse(list.containsAll(list2));
    }

    public void testRemoveAll()
    {
        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        testOwner = "Brian";
        PropertyFiringList list2 = new PropertyFiringList(testOwner, eventProperty);
        TestListener listener2 = new TestListener(testOwner);
        list2.add(listener1);
        list2.add(listener2);
        list.addAll(list2);
        list.removeAll(list2);
        assertFalse(list.contains(list2));

    }

    public void testRemoveObjVersion()
    {
        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        testOwner = "Brian";
        TestListener listener2 = new TestListener(testOwner);
        list.add(listener1);
        list.add(listener2);
        int position = list.indexOf(listener1);
        list.remove(position);
        assertFalse(list.contains(listener1));
        list.add(listener1);
        list.add(listener1);
        assertTrue(list.size() == 3);
        position = list.lastIndexOf(listener1);
        list.remove(position);

    }

    public void testSet()
    {

        PropertyFiringList list = createDefaultList();
        TestListener listener1 = createDefaultListener();
        testOwner = "Brian";
        TestListener listener2 = new TestListener(testOwner);
        list.add(listener1);
        list.add(listener2);
        int position = list.indexOf(listener1);
        list.set(position, listener2);
        assertTrue(list.contains(listener2));
        assertFalse(list.contains(listener1));

    }

    public void testRetainAll()
    {

        PropertyFiringList List1 = createDefaultList();
        PropertyFiringList List2 = createList("brian", "eventProperty");
        List1.add("hello");
        List1.add("goodbye");
        List1.add("doggy");
        List1.add("kitty");
        List2.add("doggy");
        List2.add("not in list 1");
        List1.retainAll(List2);
        assertTrue(List1.contains("doggy"));
        List2.retainAll(List1);
        assertEquals(List1.size(), List2.size());

    }

    public void testIterators()
    {
        PropertyFiringList List1 = createDefaultList();
        List1.add("hello");
        List1.add("goodbye");
        List1.add("doggy");
        List1.add("kitty");
        List1.add("cow");
        List1.add("horse");
        ListIterator L0 = List1.listIterator();
        assertSame("hello", (String) L0.next());
        ListIterator L1 = List1.listIterator(0);
        String temp = (String) L1.next();
        assertSame("hello", temp);
        ListIterator L2 = List1.listIterator(4);
        String temp2 = (String) L2.next();
        String temp3 = (String) L2.next();
        assertSame("cow", temp2);
        List1.clear();
    }

    public void testToArray()
    {
        PropertyFiringList List1 = createDefaultList();
        TestListener listener1 = createDefaultListener();
        TestListener listener2 = createListener("Mallory");
        List1.add(listener1);
        List1.add(listener2);
        Object[] x = List1.toArray();
        List1.clear();
        Object y = List1.toArray(x);

    }

    public void testSubList()
    {
        PropertyFiringList List1 = createDefaultList();
        List1.add("hello");
        List1.add("goodbye");
        List1.add("doggy");
        List1.add("kitty");
        List1.add("cow");
        List1.add("horse");
        List List2 = List1.subList(1, 4);
        Object a = List2.get(0);
        Object b = List2.get(1);
        Object c = List2.get(2);

    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestPropertyFiringList.class);
    }

}
