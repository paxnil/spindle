package net.sf.spindle.core.build;

import java.util.List;
import java.util.Map;

import net.sf.spindle.core.namespace.ICoreNamespace;
import net.sf.spindle.core.resources.ICoreResource;

import org.apache.hivemind.Resource;

public class ClashDetector
{

    /**
     * @param ns
     *            the location of a namespace we want to ensure is not in a clash situation
     * @param existingNS
     *            a list of namespace locations we might clash with
     * @param errorKey
     *            a key into the messages for error reporting.
     * @throws ClashException
     *             if ns is nested in the child locations of any of the extisting.
     */
    public static void checkNamspaceClash(ICoreNamespace ns, List existingNS, String errorKey)
            throws ClashException
    {
//        Assert.isNotNull(ns);
//        Assert.isNotNull(existingNS);
//        ICoreResource candidate = (ICoreResource) ns.getSpecificationLocation();
//        for (Iterator iter = existingNS.iterator(); iter.hasNext();)
//        {
//            ICoreResource existing = (ICoreResource) ((ICoreNamespace) iter.next())
//                    .getSpecificationLocation();
//            
//            if (candidate == existing)
//                continue;
//            
//            if (existing.clashesWith(candidate))
//                throw new ClashException(candidate, existing, null);
//        }
    }

    private Map<Resource, ICoreNamespace> fNamespaceMap;

    private Map<Resource, ICoreNamespace> fResourceMap;

    /**
     * Some resources, components mostly, we want to ensure are not claimed by more than one
     * namespace.
     * 
     * @param resource
     * @param namespace
     * @param errorKey
     * @throws ClashException
     */
    public void claimResourceForNamespace(ICoreResource resource, ICoreNamespace namespace,
            String errorKey) throws ClashException
    {
//        Assert.isNotNull(resource);
//        Assert.isNotNull(namespace);
//
//        if (fNamespaceMap == null)
//            fNamespaceMap = new HashMap();
//
//        ICoreNamespace existing = (ICoreNamespace) fNamespaceMap.get(resource);
//        if (existing != null)
//        {
//            if (!existing.equals(namespace))
//            {
//                ICoreResource requestor = (ICoreResource) namespace.getSpecificationLocation();
//                ICoreResource owner = (ICoreResource) existing.getSpecificationLocation();
//                // TODO add useful info for an error marker.
//                throw new ClashException(requestor, owner, resource);
//            }
//        }
//        else
//        {
//            fNamespaceMap.put(resource, namespace);
//        }
    }

    /**
     * Same thing, templates can belong to at most one component, page.
     * 
     * @param component
     * @param template
     * @throws ClashException
     */
    public void claimTemplateForComponent(ICoreResource component, ICoreResource template) throws ClashException
    {
//        Assert.isNotNull(component);
//        Assert.isNotNull(template);
//
//        if (fResourceMap == null)
//            fResourceMap = new HashMap();
//
//        ICoreResource existingComponent = (ICoreResource) fResourceMap.get(template);
//        if (existingComponent != null)
//        {
//            if (!existingComponent.equals(component))
//                // TODO add useful info for an error marker.
//                throw new ClashException(component, existingComponent, template);
//        }
//        else
//        {
//            fResourceMap.put(template, component);
//        }
    }

    public void clear()
    {
        if (fNamespaceMap != null)
            fNamespaceMap.clear();
    }
}
