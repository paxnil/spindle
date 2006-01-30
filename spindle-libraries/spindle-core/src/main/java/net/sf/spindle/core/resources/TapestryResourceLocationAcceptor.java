package net.sf.spindle.core.resources;

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
import java.util.ArrayList;
import java.util.List;

public class TapestryResourceLocationAcceptor implements IResourceAcceptor
{
    /**
     * Accept flag for specifying .jwc files.
     */
    public static int ACCEPT_JWC = 0x00000001;

    /**
     * Accept flag for specifying .page files.
     */
    public static int ACCEPT_PAGE = 0x00000002;

    /**
     * Accept flag for specifying any file
     */
    public static int ACCEPT_ANY = 0x00000004;

    List<ICoreResource> fResults = new ArrayList<ICoreResource>();

    String fExpectedName;

    boolean fExactMatch;

    int fAcceptFlags;

    public TapestryResourceLocationAcceptor(String name, boolean exactMatch, int acceptFlags)
    {
        reset(name, exactMatch, acceptFlags);
    }

    public void reset(String name, boolean exactMatch, int acceptFlags)
    {
        fResults.clear();
        fExpectedName = name;
        fExactMatch = exactMatch;
        fAcceptFlags = acceptFlags;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.resources.IResourceLocationRequestor#accept(core.resources.ICoreResource)
     */
    public boolean accept(ICoreResource location)
    {
        String fullname = location.getName();
        String name = null;
        String extension = null;

        boolean match = false;
        if (fullname != null)
        {
            int cut = fullname.lastIndexOf('.');
            if (cut < 0)
            {
                name = fullname;
            }
            else if (cut == 0)
            {
                extension = fullname;
            }
            else
            {
                name = fullname.substring(0, cut);
                extension = fullname.substring(cut + 1);
            }
        }
        if (name != null)
        {
            if ("*".equals(fExpectedName))
            {
                match = true;
            }
            else if (fExactMatch)
            {
                match = fExpectedName.equals(name);
            }
            else
            {
                match = name.startsWith(fExpectedName);
            }
        }
        if (match)
        {
            if ((fAcceptFlags & ACCEPT_ANY) == 0)
            {
                if ("jwc".equals(extension))
                {
                    match = (fAcceptFlags & ACCEPT_JWC) != 0;
                }
                else if ("page".equals(extension))
                {
                    match = (fAcceptFlags & ACCEPT_PAGE) != 0;
                }
                else
                {
                    match = false;
                }
            }
        }
        if (match && !fResults.contains(location))
            fResults.add(location);

        return true;
    }

    public ICoreResource[] getResults()
    {
        return (ICoreResource[]) fResults.toArray(new ICoreResource[fResults.size()]);
    }

}