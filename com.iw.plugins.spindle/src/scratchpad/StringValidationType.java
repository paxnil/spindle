//
// Tapestry Web Application Framework
// Copyright (c) 2000-2002 by Howard Lewis Ship
//
// Howard Lewis Ship
// http://sf.net/projects/tapestry
// mailto:hship@users.sf.net
//
// This library is free software.
//
// You may redistribute it and/or modify it under the terms of the GNU
// Lesser General Public License as published by the Free Software Foundation.
//
// Version 2.1 of the license should be included with this distribution in
// the file LICENSE, as well as License.html. If the license is not
// included with this distribution, you may find a copy at the FSF web
// site at 'www.gnu.org' or 'www.fsf.org', or you may write to the
// Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139 USA.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied waranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// Lesser General Public License for more details.
//

package scratchpad;

import net.sf.tapestry.parse.SpecificationParser;
import net.sf.tapestry.util.Enum;
import net.sf.tapestry.util.xml.AbstractDocumentParser;

/**
 *  Defines the different types of string validations possible for the
 *  {@link SpecificationParser}.
 *
 *  @author Geoffrey Longman
 *  @version $Id$
 * 
 **/

public final class StringValidationType extends Enum
{

    /**
     *  Perl5 pattern that parameter names must conform to.  
     *  Letter, followed by letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType PARAMETER_NAME_PATTERN =
        new StringValidationType(
            "PARAMETER_NAME_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-parameter-name");

    /**
    *  Perl5 pattern that property names (that can be connected to
    *  parameters) must conform to.  
    *  Letter, followed by letter, number or underscore.
    *  
    * 
    *  @since 2.2
    * 
    **/

    public static final StringValidationType PROPERTY_NAME_PATTERN =
        new StringValidationType(
            "PROPERTY_NAME_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-property-name");

    /**
     *  Perl5 pattern for page names.  Letter
     *  followed by letter, number, dash, underscore or period.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType PAGE_NAME_PATTERN =
        new StringValidationType(
            "PAGE_NAME_PATTERN",
            SpecificationParser.EXTENDED_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-page-name");

    /**
     *  Perl5 pattern for component alias. 
     *  Letter, followed by letter, number, or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType COMPONENT_ALIAS_PATTERN =
        new StringValidationType(
            "COMPONENT_ALIAS_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-component-alias");

    /**
     *  Perl5 pattern for helper bean names.  
     *  Letter, followed by letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType BEAN_NAME_PATTERN =
        new StringValidationType(
            "BEAN_NAME_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-bean-name");

    /**
     *  Perl5 pattern for component ids.  Letter, followed by
     *  letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType COMPONENT_ID_PATTERN =
        new StringValidationType(
            "COMPONENT_ID_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-component-id");

    /**
     *  Perl5 pattern for asset names.  Letter, followed by
     *  letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType ASSET_NAME_PATTERN =
        new StringValidationType(
            "ASSET_NAME_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-asset-name");

    /**
     *  Perl5 pattern for service names.  Letter
     *  followed by letter, number, dash, underscore or period.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType SERVICE_NAME_PATTERN =
        new StringValidationType(
            "SERVICE_NAME_PATTERN",
            SpecificationParser.EXTENDED_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-service-name");

    /**
     *  Perl5 pattern for library ids.  Letter followed
     *  by letter, number or underscore.
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType LIBRARY_ID_PATTERN =
        new StringValidationType(
            "LIBRARY_ID_PATTERN",
            AbstractDocumentParser.SIMPLE_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-library-id");

    /**
     *  Per5 pattern for extension names.  Letter followed
     *  by letter, number, dash, period or underscore. 
     * 
     *  @since 2.2
     * 
     **/

    public static final StringValidationType EXTENSION_NAME_PATTERN =
        new StringValidationType(
            "EXTENSION_NAME_PATTERN",
            SpecificationParser.EXTENDED_PROPERTY_NAME_PATTERN,
            "SpecificationParser.invalid-extension-name");

    /**
     *  Perl5 pattern for component types.  Component types are an optional
     *  namespace prefix followed by a normal identifier.
     * 
     *  @since 2.2
     **/

    public static final StringValidationType COMPONENT_TYPE_PATTERN =
        new StringValidationType(
            "COMPONENT_TYPE_PATTERN",
            SpecificationParser.COMPONENT_TYPE_PATTERN,
            "SpecificationParser.invalid-component-type");

    private String pattern;
    private String errorKey;

    private StringValidationType(String name, String pattern, String errorKey)
    {
        super(name);
        this.pattern = pattern;
        this.errorKey = errorKey;
    }

    public String getPattern()
    {
        return pattern;
    }

    public String getErrorKey()
    {
        return errorKey;
    }

}