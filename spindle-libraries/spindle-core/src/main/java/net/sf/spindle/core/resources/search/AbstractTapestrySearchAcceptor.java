package net.sf.spindle.core.resources.search;
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
import java.util.Set;

/**
 * Acceptor that will accept/reject things based on the flags set in it. Will only accept the most
 * standard tapestry file extensions:
 * <ul>
 * <li>.application
 * <li>
 * <li>.library
 * <li>
 * <li>.jwc</li>
 * <li>.page</li>
 * <li>.html</li>
 * <li>.srcipt</li>
 * 
 * @author glongman@gmail.com
 */
public abstract class AbstractTapestrySearchAcceptor implements ISearchAcceptor<Object>
{

    public static final int ACCEPT_NONE = 0x0100000;

    public static final int ACCEPT_LIBRARIES = 0x0000001;

    /**
     * Accept flag for specifying components.
     */
    public static final int ACCEPT_COMPONENTS = 0x00000002;

    /**
     * Accept flag for specifying application.
     */
    public static final int ACCEPT_APPLICATIONS = 0x00000004;

    /**
     * Accept flag for specifying template files
     */
    public static final int ACCEPT_TEMPLATE = 0x00000008;

    /**
     * Accept flag for specifying page files
     */
    public static final int ACCEPT_PAGES = 0x00000010;

    /**
     * Accept flag for specifying script files
     */
    public static final int ACCEPT_SCRIPT = 0x00000020;

    /**
     * Accept flag for specifying any tapestry files
     */
    public static final int ACCEPT_ANY = 0x00000100;

    private List fResults = new ArrayList();

    private int fAcceptFlags;

    private Set fAllowedTemplateExtensions;

    public AbstractTapestrySearchAcceptor(int acceptFlags, Set allowedTemplateExtensions)
    {
        fAllowedTemplateExtensions = allowedTemplateExtensions;
        reset(acceptFlags);
    }

    public void reset(int flags)
    {
        fResults.clear();
        this.fAcceptFlags = flags;
    }

    protected abstract boolean isExcluded(Object leaf);

    protected abstract String getFileExtension(Object leaf);

    protected boolean acceptAsTapestry(Object leaf)
    {
        String extension = getFileExtension(leaf);

        boolean acceptAny = (fAcceptFlags & ACCEPT_ANY) != 0;

        if ("jwc".equals(extension) && (acceptAny || (fAcceptFlags & ACCEPT_COMPONENTS) != 0))
            return true;

        if ("application".equals(extension)
                && (acceptAny || (fAcceptFlags & ACCEPT_APPLICATIONS) != 0))
            return true;

        if ("library".equals(extension) && (acceptAny || (fAcceptFlags & ACCEPT_LIBRARIES) != 0))
            return true;

        if ("page".equals(extension) && (acceptAny || (fAcceptFlags & ACCEPT_PAGES) != 0))
            return true;

        if ("script".equals(extension) && (acceptAny || (fAcceptFlags & ACCEPT_SCRIPT) != 0))
            return true;

        if (fAllowedTemplateExtensions == null || fAllowedTemplateExtensions.isEmpty())
        {
            if ("html".equals(extension) && (acceptAny || (fAcceptFlags & ACCEPT_TEMPLATE) != 0))
                return true;
        }
        else
        {
            if (fAllowedTemplateExtensions.contains(extension)
                    && (acceptAny || (fAcceptFlags & ACCEPT_TEMPLATE) != 0))
                return true;
        }

        return false;
    }

    public final boolean accept(Object parent, Object leaf)
    {
        if (isExcluded(leaf) || !acceptAsTapestry(leaf))
            return true; // continue the search

        //returing false means stop the search
        return acceptTapestry(parent, leaf);
    }

    /** return false to abort the search * */
    public abstract boolean acceptTapestry(Object parent, Object leaf);
}