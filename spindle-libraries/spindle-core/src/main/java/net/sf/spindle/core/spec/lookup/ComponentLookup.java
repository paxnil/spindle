package net.sf.spindle.core.spec.lookup;

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
import net.sf.spindle.core.namespace.ICoreNamespace;

import org.apache.tapestry.spec.IComponentSpecification;

/**
 * Lookup class for ComponentSpecifications
 * 
 * @author glongman@gmail.com
 */
public class ComponentLookup extends AbstractLookup
{
    public IComponentSpecification lookupSpecification(String type)
    {
        int colonx = type.indexOf(':');

        if (colonx > 0)
        {
            String libraryId = type.substring(0, colonx);
            String simpleType = type.substring(colonx + 1);
            return lookupSpecification(libraryId, simpleType);
        }
        else
        {
            return lookupSpecification(null, type);
        }
    }

    protected IComponentSpecification lookupSpecification(String libraryId, String type)
    {
        ICoreNamespace useNamespace = getNamespace();

        IComponentSpecification result = null;

        if (libraryId != null)
            useNamespace = (ICoreNamespace) useNamespace.getChildNamespace(libraryId);

        result = useNamespace.getComponentSpecification(type);

        if (result == null && libraryId == null)
        {
            useNamespace = getFrameworkNamespace();
            if (useNamespace != null)
                return getFrameworkNamespace().getComponentSpecification(type);

        }
        return result;
    }
}