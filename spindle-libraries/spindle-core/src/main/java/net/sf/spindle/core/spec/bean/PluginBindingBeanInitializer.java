package net.sf.spindle.core.spec.bean;

import net.sf.spindle.core.spec.BaseSpecification;

/**
 * @author Administrator TODO To change the template for this generated type comment go to Window -
 *         Preferences - Java - Code Style - Code Templates
 */
public class PluginBindingBeanInitializer extends AbstractPluginBeanInitializer
{

    public PluginBindingBeanInitializer()
    {
        super(BaseSpecification.BINDING_BEAN_INIT);
    }

    /**
     * @return Returns the bindingReference.
     */
    public String getBindingReference()
    {
        return getValue();
    }

    /**
     * @param bindingReference
     *            The bindingReference to set.
     */
    public void setBindingReference(String bindingReference)
    {
        setValue(bindingReference);
    }
}
