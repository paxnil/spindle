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
import java.util.Map;

import com.iw.plugins.spindle.core.util.PropertyFiringMap;

import junit.framework.TestCase;

/**
 *  This class tests PropertyFiringMap, Set and List in contexy
 * 
 * @author glongman@gmail.com
 * @version $Id$
 */
public class PropertyFiringTestInContext extends TestCase
{

    protected abstract class AnimalShelter implements PropertyChangeListener
    {

        Map animals;

        public void putAnimal(String name, Animal animal)
        {
            if (animals == null)
            {
                animals = new PropertyFiringMap(this, "animals");
            }
            animals.put(name, animal);
        }

        public void removeAnimal(String name)
        {
            if (animals != null)
            {
                animals.remove(name);
            }
        }

        public int getAnimalCount()
        {
            if (animals != null)
            {
                return animals.size();
            }
            return 0;
        }

        public boolean hasAnimals()
        {
            if (animals == null)
            {
                return false;
            }
            return animals.isEmpty();
        }

    }

    protected class Animal
    {

        private String type;

        public Animal(String type)
        {
            this.type = type;
        }
    }

    public PropertyFiringTestInContext(String arg0)
    {
        super(arg0);
    }

    public void testAnimalShelterAddOneAnimal()
    {

        final Animal doggy = new Animal("doggy");
        AnimalShelter testShelter = new AnimalShelter()
        {
            public void propertyChange(PropertyChangeEvent evt)
            {
                assertSame(this, evt.getSource());
                assertEquals("animals", evt.getPropertyName());
                assertNull(evt.getOldValue());
                assertSame(doggy, evt.getNewValue());

            }
        };

        testShelter.putAnimal("rufus", doggy);

    }

    public void testAnimalShelterAddTwoAnimals()
    {

        final Animal doggy = new Animal("doggy");
        final Animal kitty = new Animal("kitty");
        AnimalShelter testShelter = new AnimalShelter()
        {

            public void propertyChange(PropertyChangeEvent evt)
            {
                assertSame(this, evt.getSource());
                assertEquals("animals", evt.getPropertyName());
                assertNull(evt.getOldValue());
                int count = getAnimalCount();
                assertTrue("no event should have been fired!", count != 0);
                if (count == 1)
                {
                    assertSame(doggy, evt.getNewValue());

                } else if (count == 2)
                {

                    assertSame(kitty, evt.getNewValue());
                } else
                {
                    fail("count > 2!");
                }

            }
        };

        testShelter.putAnimal("rufus", doggy);
        testShelter.putAnimal("fred", kitty);
    }

    public void testAnimalShelterRemoveOneAnimal()
    {

        final Animal doggy = new Animal("doggy");
        AnimalShelter testShelter = new AnimalShelter()
        {

            public void propertyChange(PropertyChangeEvent evt)
            {
                assertSame(this, evt.getSource());
                assertEquals("animals", evt.getPropertyName());

                Object oldValue = evt.getOldValue();
                // if its a put, check to see if its the right one!
                if (oldValue == null)
                {
                    assertSame(doggy, evt.getNewValue());
                } else
                {
                    //assume its the remove
                    assertSame(doggy, oldValue);
                    assertNull(evt.getNewValue());
                }

            }
        };

        testShelter.putAnimal("rufus", doggy);
        testShelter.removeAnimal("rufus");
    }

    public static void main(String[] args)
    {
        junit.textui.TestRunner.run(PropertyFiringTestInContext.class);
    }

}
