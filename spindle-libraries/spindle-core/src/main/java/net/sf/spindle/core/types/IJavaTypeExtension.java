package net.sf.spindle.core.types;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;

/**
 * Extension interface for {@link net.sf.spindle.core.types.IJavaType}.<p>
 * 
 * Introduces the ability to handle Tapestry annotations.<p>
 * 
 * This is modeled as an extension as the next version of the JDK (mustang) changes
 * the api naming scheme from com.sun.mirror.* to javax.annotation.*<p>
 * 
 * The core will gracefully degrade if IDE implementors choose not use this extension.
 * (ie. annotation processing will not be available).<p>
 * 
 * Usage: if your IDE implementation includes an implementation of the mirror api,
 * simple make your implementation of {@link net.sf.spindle.core.types.IJavaType} also
 * implements (@link IJavaTypeExtension).
 * 
 *
 */
public interface IJavaTypeExtension
{
    public AnnotationProcessorEnvironment getEnvironment();
}
