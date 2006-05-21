package net.sf.spindle.core.build;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.spindle.core.types.IJavaType;
import net.sf.spindle.core.types.IJavaTypeFinder;

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
/**
 * A lot of basic testing will be ok if all requested types are found to exist. In addition, can
 * indicate what types are interfaces. No annotation support yet!
 */
public class PermissiveTypeFinder implements IJavaTypeFinder
{

    private Set<String> interfaceTypes = new HashSet<String>();

    public IJavaType findType(String fullyQualifiedName)
    {
        return new Type(fullyQualifiedName);
    }

    public void addInterface(String fqn)
    {
        interfaceTypes.add(fqn);
    }

    public boolean isCachingJavaTypes()
    {
        return false;
    }

    class Type implements IJavaType
    {

        private String fqn;

        public Type(String fqn)
        {
            super();
            this.fqn = fqn;
        }

        public boolean exists()
        {
            return true;
        }

        public String getFullyQualifiedName()
        {
            return fqn;
        }

        public Object getUnderlier()
        {
            return null;
        }

        public boolean isAnnotation()
        {
            return false;
        }

        public boolean isBinary()
        {
            return false;
        }

        public boolean isInterface()
        {
            return interfaceTypes.contains(fqn);
        }

        public boolean isSuperTypeOf(IJavaType candidate)
        {
            // hmm, may need this - WEBXMLSCANNER uses it.
            return false;
        }
        
        @Override
        public boolean equals(Object obj)
        {
            if (obj == null)
                return false;
            if (!(obj instanceof Type))
                return false;
            
            Type other = (Type) obj;
            
            return this.fqn.equals(other.fqn);
        }

    }

}
