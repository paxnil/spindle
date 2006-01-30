package net.sf.spindle.core.properties;
/*
The contents of this file are subject to the Mozilla Public License
Version 1.1 (the "License"); you may not use this file except in
compliance with the License. You may obtain a copy of the License at
http://www.mozilla.org/MPL/

Software distributed under the License is distributed on an "AS IS"
basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
License for the specific language governing rights and limitations
under the License.

The Original Code is __Spindle, an Eclipse Plugin For Tapestry__.

The Initial Developer of the Original Code is _____Geoffrey Longman__.
Portions created by _____Initial Developer___ are Copyright (C) _2004, 2005, 2006__
__Geoffrey Longman____. All Rights Reserved.

Contributor(s): __glongman@gmail.com___.
*/
import org.apache.tapestry.engine.IPropertySource;

/**
 * @author gwl
 *
 */
public class CorePropertySource implements IPropertySource
{
    
    IPropertySource fParent;
    IPropertySource fChild;

    //Defaults are implied parent
    public CorePropertySource(IPropertySource child)
    {
       this(DefaultProperties.getInstance(), child);
    }
    
    private CorePropertySource(IPropertySource parent, IPropertySource child) {
        fParent = parent;
        fChild = child;
    }
    
    public IPropertySource createChildPropertySource(IPropertySource child) {
        return new CorePropertySource(this, child);
    }

    public String getPropertyValue(String arg0)
    {
        String result = fChild == null ? null : fChild.getPropertyValue(arg0);
        if (result != null)
            return result;
        
        return fParent.getPropertyValue(arg0);
    }

}
