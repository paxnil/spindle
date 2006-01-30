package net.sf.spindle.core;
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
import net.sf.spindle.core.resources.IResourceRoot;
import net.sf.spindle.core.types.IJavaTypeFinder;
*/
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.sf.spindle.core.scanning.IScannerValidator;
import net.sf.spindle.core.source.ISourceLocationInfo;

import org.apache.tapestry.IBinding;



/**
 * This class is the temporary landing zone for stuff that will eventually be replaced by some form
 * of Hivemind Support.
 * 
 * @deprecated
 */
public class PicassoMigration
{

    public static final String DEFAULT_TEMPLATE_EXTENSION = "html";

    public static final String TEMPLATE_ASSET_NAME = "$template";

    public static final int OGNL_BINDING = 0;

    public static final int MESSAGE_BINDING = 1;

    public static final int LITERAL_BINDING = 2;

    public static final int ASSET_BINDING = 3;

    public static final int BEAN_BINDING = 4;

    public static final int LISTENER_BINDING = 5;

    public static final int COMPONENT_BINDING = 6;

    public static final int HIVEMIND_BINDING = 7;

    public static final List BINDING_NAMES = Collections.unmodifiableList(Arrays
            .asList(new Object[]
            { "ognl", "message", "", "asset", "bean", "listener", "component", "hivemind" }));
    
    public static int getBindingType(String prefix)
    {
        if (prefix == null)
            return LITERAL_BINDING;

        return BINDING_NAMES.indexOf(prefix);
    }

    public static final int INJECT_OBJECT = 0;

    public static final int INJECT_STATE = 1;

    public static final int INJECT_META = 2;

    public static final int INJECT_SCRIPT = 3;

    public static final List INJECT_NAMES = Collections.unmodifiableList(Arrays.asList(new Object[]
    { "object", "state", "meta", "script" }));

    public static int getInjectType(String type)
    {
        return INJECT_NAMES.indexOf(type);
    }
   

    public static interface IBindingValidator
    {
        public boolean validate(IBinding binding, ISourceLocationInfo sourceInfo,
                IScannerValidator validator);
    }

    public static final int CONTEXT_ASSET = 0;

    public static final int CLASSPATH_ASSET = 1;

    public static final int DEFAULT_ASSET = 2; // external

    public static final List ASSET_TYPES = Collections.unmodifiableList(Arrays.asList(new Object[]
    { "context", "classpath", "" }));

    // TODO this is ugly
    public static int getAssetType(String prefix)
    {

        if (prefix == null)
            return DEFAULT_ASSET;

       return ASSET_TYPES.indexOf(prefix);
    }
}
