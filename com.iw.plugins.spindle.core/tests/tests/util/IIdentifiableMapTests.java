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

import com.iw.plugins.spindle.core.spec.IIdentifiable;
import com.iw.plugins.spindle.core.util.IIdentifiableMap;

/**
 *  TODO Add Type comment
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class IIdentifiableMapTests extends PropertyFiringBase
{

    /**
     * Constructor for IIdeintifiableMapTests.
     * @param arg0
     */
    public IIdentifiableMapTests(String arg0)
    {
        super(arg0);
    }

    public void testAddPCListenerInConstructor()
    {

        final IIdentifiable mock = new MockIIdentifiable(this);
        // called by the map created below! can't use a OneShot Listener!
        TestListener mockListener = new TestListener()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                super.propertyChange(evt);
                assertEquals("wrong property source!", source, this);
                assertEquals(evt.getPropertyName(), "testMap");
                assertTrue(newValue == mock);
                assertNull(oldValue);

            }
        };

        IIdentifiableMap map = new IIdentifiableMap(mockListener, "testMap");
        assertTrue(mock.getIdentifier() == null);
        assertTrue(mock.getParent() == null);
        map.put("blah", mock);
        assertEquals(mock.getIdentifier(), "blah");
        assertTrue(mock.getParent() == mockListener);
    }

    public void testRemove()
    {
        final IIdentifiable dean = new MockIIdentifiable(this);
        final String testOwner = "geoff";

        IIdentifiableMap map = new IIdentifiableMap(testOwner, "geoffsMap");

        map.put("dean", dean);

        assertEquals(dean.getIdentifier(), "dean");
        assertTrue(dean.getParent() == testOwner);

        TestListener listener = new OneShotListener(testOwner, dean, null, "geoffsMap");
        map.addPropertyChangeListener(listener);

        map.remove("dean");

        assertTrue(dean.getIdentifier() == null);
        assertTrue(dean.getParent() == null);
    }

    public void testPutAgain()
    {
        final IIdentifiable porsche = new MockIIdentifiable(this);
        final IIdentifiable ferrari = new MockIIdentifiable(this);
        final String testOwner = "chris";

        IIdentifiableMap map = new IIdentifiableMap(testOwner, "chrisCars");
        map.put("current-ride", porsche);

        assertEquals(porsche.getIdentifier(), "current-ride");
        assertTrue(porsche.getParent() == testOwner);

        TestListener listener = new OneShotListener(testOwner, porsche, ferrari, "chrisCars");
        map.addPropertyChangeListener(listener);

        map.put("current-ride", ferrari);

        assertTrue(porsche.getIdentifier() == null);
        assertTrue(porsche.getParent() == null);
        assertEquals(ferrari.getIdentifier(), "current-ride");
        assertTrue(ferrari.getParent() == testOwner);
    }

    public void testClear()
    {
        final IIdentifiable porsche = new MockIIdentifiable(this);
        final IIdentifiable ferrari = new MockIIdentifiable(this);
        final String testOwner = "chris";

        IIdentifiableMap map = new IIdentifiableMap(testOwner, "chrisCars");
        map.put("current-ride", porsche);
        map.put("dream-ride", ferrari);

        assertEquals(porsche.getIdentifier(), "current-ride");
        assertEquals(porsche.getParent(), testOwner);

        assertEquals(ferrari.getIdentifier(), "dream-ride");
        assertEquals(ferrari.getParent(), testOwner);

        map.clear();

        assertNull(porsche.getIdentifier());
        assertNull(porsche.getParent());

        assertNull(ferrari.getIdentifier());
        assertNull(ferrari.getParent());
    }

    public void testReplace()
    {
        final IIdentifiable porsche = new MockIIdentifiable(this);
        final String testOwner = "chris";

        IIdentifiableMap map = new IIdentifiableMap(testOwner, "chrisCars");
        map.put("current-ride", porsche);

        assertEquals(porsche.getIdentifier(), "current-ride");
        assertEquals(porsche.getParent(), testOwner);

        map.put("dream-ride", porsche);

        assertEquals(porsche.getIdentifier(), "dream-ride");
        assertEquals(porsche.getParent(), testOwner);

        map.remove("dream-ride");

        assertNull(porsche.getIdentifier());
        assertNull(porsche.getParent());

    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(IIdentifiableMapTests.class);
    }

}
