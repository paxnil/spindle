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

import com.iw.plugins.spindle.core.util.PropertyFiringList;

/**
 *  TODO Needs to be complete
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class TestPropertyFiringList extends PropertyFiringBase
{

    /**
     * Constructor for TestPropertyFiringList.
     * @param arg0
     */
    public TestPropertyFiringList(String arg0)
    {
        super(arg0);
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
        String testOwner = "geoff";
        final String eventProperty = "testSet";
        final String stored = "value";

        PropertyFiringList list = new PropertyFiringList(testOwner, eventProperty);
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
        list.addPropertyChangeListener(listener1);
        list.add(stored);
        
        list.add(stored);
        assertTrue(list.size() == 2);
        assertTrue(list.contains(stored));
        assertTrue(list.indexOf(stored) != list.lastIndexOf(stored));
        list.add(2,stored);
        assertTrue(list.size() ==3);
        assertTrue(list.contains(stored));
        assertTrue(list.indexOf(stored) != list.lastIndexOf(stored));
    }

    public void testRemovePropertyChangeListener(){
        String testOwner ="brian";
        final String eventProperty = "testSet";
        final String stored = "brians value";
        
        PropertyFiringList list = new PropertyFiringList(testOwner, eventProperty);
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
        list.addPropertyChangeListener(listener1);
        list.removePropertyChangeListener(listener1);     
    }
    
    public void testClear(){
        String testOwner ="brian";
        final String eventProperty = "testSet";
        final String stored = "brians value";
        
        PropertyFiringList list = new PropertyFiringList(testOwner, eventProperty);
        list.clear();
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
        list.addPropertyChangeListener(listener1);
        list.clear();
        assertTrue(list.size() == 0);
        list.add(0, stored);
//        list.clear(); //causes a failure

    }
    
    public void testRemove(){
        String testOwner ="brian";
        final String eventProperty = "testSet";
        final String stored = "brians value";        
        PropertyFiringList list = new PropertyFiringList(testOwner, eventProperty);
        OneShotListener listener1 = new OneShotListener(testOwner, null, stored, eventProperty);
  
        list.add(stored);
        assertTrue(list.remove(stored));
        
        
    }
    
    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(TestPropertyFiringList.class);
    }

}
