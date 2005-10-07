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

package core.spec;

import java.util.ArrayList;
import java.util.List;

import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.spec.BeanLifecycle;
import org.apache.tapestry.spec.IBeanSpecification;



import core.TapestryCore;
import core.extensions.IBeanSpecificationValidator;
import core.extensions.SpindleExtensionException;
import core.resources.ICoreResource;
import core.scanning.IScannerValidator;
import core.scanning.ScannerException;
import core.source.IProblem;
import core.source.ISourceLocationInfo;

/**
 * Spindle aware concrete implementation of IBeanSpecification
 * 
 * @author glongman@gmail.com
 */
public class PluginBeanSpecification extends BasePropertyHolder implements IBeanSpecification
{
    public static IBeanSpecificationValidator CONTRIBUTED_BEAN_SPEC_VALIDATORS = null;

    protected String fClassName;

    protected String propertyName;

    protected BeanLifecycle fLifecycle;

    /** @since 1.0.9 * */
    private String fDescription;

    /**
     * A List of {@link IBeanInitializer}.
     */

    protected List fInitializers;

    public PluginBeanSpecification()
    {
        super(BaseSpecification.BEAN_SPEC);
    }

    public PluginBeanSpecification(String className, BeanLifecycle lifecycle)
    {
        this();
        fClassName = className;
        fLifecycle = lifecycle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBeanSpecification#getClassName()
     */
    public String getClassName()
    {
        return fClassName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBeanSpecification#getLifecycle()
     */
    public BeanLifecycle getLifecycle()
    {
        return fLifecycle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBeanSpecification#addInitializer(org.apache.tapestry.bean.IBeanInitializer)
     */
    public void addInitializer(IBeanInitializer initializer)
    {
        if (fInitializers == null)
            fInitializers = new ArrayList();

        fInitializers.add(initializer);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBeanSpecification#getInitializers()
     */
    public List getInitializers()
    {
        return fInitializers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.spec.IPluginDescribable#getDescription()
     */
    public String getDescription()
    {
        return fDescription;
    }

    /*
     * (non-Javadoc)
     * 
     * @see core.spec.IPluginDescribable#setDescription(java.lang.String)
     */
    public void setDescription(String desc)
    {
        fDescription = desc;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBeanSpecification#setClassName(java.lang.String)
     */
    public void setClassName(String className)
    {
        this.fClassName = className;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.IBeanSpecification#setLifecycle(org.apache.tapestry.spec.BeanLifecycle)
     */
    public void setLifecycle(BeanLifecycle lifecycle)
    {
        fLifecycle = lifecycle;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.PropertyInjectable#getPropertyName()
     */
    public String getPropertyName()
    {
        return propertyName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.spec.PropertyInjectable#setPropertyName(java.lang.String)
     */
    public void setPropertyName(String propertyName)
    {
        this.propertyName = propertyName;
    }

    public void validate(Object parent, IScannerValidator validator)
    {

        PluginComponentSpecification component = (PluginComponentSpecification) parent;

        ISourceLocationInfo sourceInfo = (ISourceLocationInfo) getLocation();

        try
        {
            Object type = validator.validateTypeName(
                    (ICoreResource) component.getSpecificationLocation(),
                    fClassName,
                    IProblem.ERROR,
                    (fClassName != null ? sourceInfo.getAttributeSourceLocation("class")
                            : sourceInfo.getTagNameLocation()));

            if (type == null || CONTRIBUTED_BEAN_SPEC_VALIDATORS == null)
                return;

            if (CONTRIBUTED_BEAN_SPEC_VALIDATORS.canValidate(this))
            {
                try
                {
                    CONTRIBUTED_BEAN_SPEC_VALIDATORS.validate(this);
                }
                catch (SpindleExtensionException e1)
                {
                    validator.addProblem(IProblem.ERROR, sourceInfo.getStartTagSourceLocation(), e1
                            .getMessage(), true, IProblem.NOT_QUICK_FIXABLE);
                }
            }

        }
        catch (ScannerException e)
        {
            TapestryCore.log(e);
            e.printStackTrace();
        }

    }

}