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
 * Intelligent Works Incorporated.
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */
package com.iw.plugins.spindle.core.spec;

import org.apache.tapestry.bean.IBeanInitializer;
import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IBeanSpecification;
import org.apache.tapestry.spec.IBindingSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.IExtensionSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.apache.tapestry.spec.IListenerBindingSpecification;
import org.apache.tapestry.spec.IParameterSpecification;
import org.apache.tapestry.spec.IPropertySpecification;
import org.apache.tapestry.spec.SpecFactory;

import com.iw.plugins.spindle.core.spec.bean.PluginExpressionBeanInitializer;
import com.iw.plugins.spindle.core.spec.bean.PluginFieldBeanInitializer;
import com.iw.plugins.spindle.core.spec.bean.PluginMessageBeanInitializer;

public class TapestryCoreSpecFactory extends SpecFactory
{

    /**
     * Constructor for TapestryPluginFactory
     */
    public TapestryCoreSpecFactory()
    {
        super();
    }

    public IApplicationSpecification createApplicationSpecification()
    {
        return new PluginApplicationSpecification();
    }

    public IAssetSpecification createAssetSpecification()
    {
        return new PluginAssetSpecification();
    }

    public IBeanSpecification createBeanSpecification()
    {
        return new PluginBeanSpecification();
    }

    public IBindingSpecification createBindingSpecification()
    {
        return new PluginBindingSpecfication();
    }

    public IComponentSpecification createComponentSpecification()
    {
        return new PluginComponentSpecification();
    }

    public IContainedComponent createContainedComponent()
    {
        return new PluginContainedComponent();
    }

    public IParameterSpecification createParameterSpecification()
    {
        return new PluginParameterSpecification();
    }

    //  public IBeanInitializer createStaticBeanInitializer(String propertyName, Object staticValue) {
    //    return new PluginStaticBeanInitializer(propertyName, staticValue);
    //  }
    //
    //  /* (non-Javadoc)
    //   * @see SpecFactory#createFieldBeanInitializer(String, String)
    //   */
    //  public IBeanInitializer createFieldBeanInitializer(String propertyName, String fieldName) {
    //    return new PluginFieldBeanInitializer(propertyName, fieldName);
    //  }

    /**
     * @see net.sf.tapestry.spec.SpecFactory#createExtensionSpecification()
     */
    public IExtensionSpecification createExtensionSpecification()
    {
        return new PluginExtensionSpecification();
    }

    /**
     * @see net.sf.tapestry.spec.SpecFactory#createLibrarySpecification()
     */
    public ILibrarySpecification createLibrarySpecification()
    {
        return new PluginLibrarySpecification();
    }

    public IBeanInitializer createFieldBeanInitializer()
    {
        return new PluginFieldBeanInitializer();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.SpecFactory#createExpressionBeanInitializer()
     */
    public IBeanInitializer createExpressionBeanInitializer()
    {
        return new PluginExpressionBeanInitializer();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.SpecFactory#createListenerBindingSpecification()
     */
    public IListenerBindingSpecification createListenerBindingSpecification()
    {
        return new PluginListenerBindingSpecification();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.SpecFactory#createPropertySpecification()
     */
    public IPropertySpecification createPropertySpecification()
    {

        return new PluginPropertySpecification();
    }

    /* (non-Javadoc)
     * @see org.apache.tapestry.spec.SpecFactory#createStringBeanInitializer()
     */
    public IBeanInitializer createMessageBeanInitializer()
    {
        return new PluginMessageBeanInitializer();
    }

}
