package scratchpad;

import java.text.MessageFormat;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public class InvalidStringConverter
{

    static private final String badAsset = "asset name=\"{0}\"";
    static private final String badBean = "bean name=\"{0}\"";
    static private final String badComponentAlias = "component-alias type=\"{0}\"";
    static private final String badComponentId = "component id=\"{0}\"";
    static private final String badComponentType = "type==\"{0}\"";
    static private final String badPropertyName = "property name=\"{0}\"";
    static private final String badExtensionName = "extension name=\"{0}\"";
    static private final String badLibraryId = "library id=\"{0}\"";
    static private final String badParameterName = "parameter name=\"{0}\"";
    static private final String badPageName = "page name=\"{0}\"";
    static private final String badServiceName = "page name=\"{0}\"";

    public static int getLineNumberForInvalidString(InvalidStringException e, IDocument document)
    {

        return getLineNumberForInvalidString(e.getInvalidString(), e.getValidationType(), document);

    }

    public static int getLineNumberForInvalidString(
        String invalid,
        StringValidationType type,
        IDocument document)
    {

        String[] error = new String[] { invalid };
        String formatString = null;

        if (StringValidationType.ASSET_NAME_PATTERN == type)
        {

            formatString = badAsset;

        }
        else if (StringValidationType.BEAN_NAME_PATTERN == type)
        {

            formatString = badBean;

        }
        else if (StringValidationType.COMPONENT_ALIAS_PATTERN == type)
        {

            formatString = badComponentAlias;

        }
        else if (StringValidationType.COMPONENT_ID_PATTERN == type)
        {

            formatString = badComponentId;

        }
        else if (StringValidationType.COMPONENT_TYPE_PATTERN == type)
        {

            formatString = badComponentType;

        }

        else if (StringValidationType.EXTENSION_NAME_PATTERN == type)
        {

            formatString = badExtensionName;

        }
        else if (StringValidationType.LIBRARY_ID_PATTERN == type)
        {

            formatString = badLibraryId;

        }
        else if (StringValidationType.PAGE_NAME_PATTERN == type)
        {

            formatString = badPageName;

        }
        else if (StringValidationType.PARAMETER_NAME_PATTERN == type)
        {

            formatString = badParameterName;

        }
        else if (StringValidationType.PROPERTY_NAME_PATTERN == type)
        {

            formatString = badPropertyName;

        }
        else if (StringValidationType.SERVICE_NAME_PATTERN == type)
        {

            formatString = badServiceName;

        }

        try
        {
            int resultOffset = -1;

            if (formatString != null)
            {

                MessageFormat format = new MessageFormat(formatString);
                String searchString = format.format(error);

                resultOffset = document.search(0, searchString, true, true, false);

            }

            if (resultOffset == -1) 
            {

                resultOffset = 0;

            }

            return document.getLineOfOffset(resultOffset + 1);

        }
        catch (BadLocationException ex)
        {
        }

        return 1;

    }
}
