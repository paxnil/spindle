package com.iw.plugins.spindle.core.builder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.iw.plugins.spindle.core.CoreMessages;
import com.iw.plugins.spindle.core.namespace.ICoreNamespace;
import com.iw.plugins.spindle.core.resources.ICoreResource;
import com.iw.plugins.spindle.core.util.Assert;

public class NamespaceClashDetector
{

    public static void checkClash(ICoreResource ns, List existingNS, String errorKey)
            throws NamespaceClashException
    {
        Assert.isNotNull(ns);
        Assert.isNotNull(existingNS);
        for (Iterator iter = existingNS.iterator(); iter.hasNext();)
        {
            ICoreResource existing = (ICoreResource) iter.next();
            if (existing.clashesWith(ns))
                throw new NamespaceClashException(CoreMessages.format(errorKey, new Object[]
                { ns, existing }));
        }
    }

    private Map fNamespaceMap;

    public void checkClash(ICoreResource resource, ICoreNamespace namespace, String errorKey)
            throws NamespaceClashException
    {
        Assert.isNotNull(resource);
        Assert.isNotNull(namespace);

        if (fNamespaceMap == null)
            fNamespaceMap = new HashMap();

        ICoreNamespace existing = (ICoreNamespace) fNamespaceMap.get(resource);
        if (existing != null)
            error(namespace, existing, resource, errorKey);
        else
            fNamespaceMap.put(resource, namespace);

    }

    private void error(ICoreNamespace namespace, ICoreNamespace existing, ICoreResource resource,
            String errorKey)
    {
        // TODO add useful info for an error marker.
        throw new NamespaceClashException(CoreMessages.format(errorKey, new Object[]
        { resource, namespace, existing }));
    }

    public void clear()
    {
        if (fNamespaceMap != null)
            fNamespaceMap.clear();
    }
}
