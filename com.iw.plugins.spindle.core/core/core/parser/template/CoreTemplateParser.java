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

package core.parser.template;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.hivemind.ApplicationRuntimeException;
import org.apache.hivemind.Location;
import org.apache.hivemind.Resource;
import org.apache.tapestry.parse.ITemplateParserDelegate;
import org.apache.tapestry.parse.LocalizationToken;
import org.apache.tapestry.parse.OpenToken;
import org.apache.tapestry.parse.TemplateParseException;
import org.apache.tapestry.parse.TemplateParser;
import org.apache.tapestry.parse.TemplateToken;
import org.apache.tapestry.parse.TemplateTokenFactory;



import core.TapestryCore;
import core.source.IProblem;
import core.source.IProblemCollector;
import core.source.ISourceLocation;
import core.source.SourceLocation;

/**
 * Subclass of the Tapestry template parser, tweaked for Spindle.
 * 
 * @author glongman@gmail.com
 */
public class CoreTemplateParser extends TemplateParser
{
    public static final String REMOVE_ID = "$remove$";

    public static final String CONTENT_ID = "$content$";

    public static final int IMPLICIT_ID_PATTERN_ID_GROUP = 1;

    public static final int IMPLICIT_ID_PATTERN_TYPE_GROUP = 2;

    public static final int IMPLICIT_ID_PATTERN_LIBRARY_ID_GROUP = 4;

    public static final int IMPLICIT_ID_PATTERN_SIMPLE_TYPE_GROUP = 5;

    static class CoreTemplateTokenFactory extends TemplateTokenFactory
    {
        private TagEventHandler eventHandler;

        public CoreTemplateTokenFactory(TagEventHandler eventHandler)
        {
            this.eventHandler = eventHandler;
        }

        public LocalizationToken createLocalizationToken(String tagName, String localizationKey,
                boolean raw, Map attributes, Location startLocation)
        {
            return new CoreLocalizationToken(tagName, localizationKey, raw, attributes,
                    startLocation, eventHandler.getEventInfo());
        }

        public OpenToken createOpenToken(String tagName, String jwcId, String type,
                Location location)
        {
            return new CoreOpenToken(tagName, jwcId, type, location, eventHandler.getEventInfo());
        }

    }

    /**
     * Handler to collect line/offset information for Start Tags, Includes attributes too.
     */

    private TagEventHandler fEventHandler = new TagEventHandler();
    
    /**
     * The component attribute name (usually 'jwcid')
     */
    private String fComponentAttributeName;

    /**
     * List of Problems found during the parse.
     */

    private IProblemCollector fProblemCollector;

    public CoreTemplateParser()
    {
        super();
        setFactory(new CoreTemplateTokenFactory(fEventHandler));
    }    

    @SuppressWarnings("unchecked") /* unchecked as getTokens() in the parser is not JDK1.5 aware */
	public TemplateToken[] parse(char[] templateData, ITemplateParserDelegate delegate,
            Resource resourceLocation) throws TemplateParseException
    {
        TemplateToken[] result = null;

        try
        {
            beforeParse(templateData, delegate, resourceLocation);
            fComponentAttributeName = delegate.getComponentAttributeName();
            try
            {
                parse();
            }
            catch (Exception e)
            {
                //ignore, problems already recorded
                //e.printStackTrace();
            }

            List tokens = getTokens();

            result = (TemplateToken[]) tokens.toArray(new TemplateToken[tokens.size()]);
        }
        catch (RuntimeException e)
        {
            TapestryCore.log(e);
            throw e;
        }
        finally
        {
            afterParse();
        }

        return result;
    }

    public void setProblemCollector(IProblemCollector collector)
    {
        fProblemCollector = collector;
    }

    private ISourceLocation getSourceLocation(int line, int cursor, String message)
    {
        if (message.startsWith("Tag"))
        {
            return getJWCIDLocation();
        }
        ISourceLocation result = fEventHandler.getEventInfo().findLocation(cursor);
        if (result == null)
            result = new SourceLocation(line, cursor);
        return result;
    }

    private ISourceLocation getJWCIDLocation()
    {
        Map attrMap = fEventHandler.getEventInfo().getAttributeMap();
        ISourceLocation result = (ISourceLocation) findCaselessly(
                fComponentAttributeName,
                attrMap);
        if (result == null)
            result = fEventHandler.getEventInfo().getStartTagLocation();
        return result;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.parse.TemplateParser#templateParseProblem(org.apache.tapestry.ApplicationRuntimeException,
     *      int, int)
     */
    protected void templateParseProblem(ApplicationRuntimeException exception, int line, int cursor)
            throws ApplicationRuntimeException
    {
        if (fProblemCollector != null)
            fProblemCollector.addProblem(
                    IProblem.ERROR,
                    getJWCIDLocation(),
                    exception.getMessage(),
                    false,
                    IProblem.NOT_QUICK_FIXABLE);
        super.templateParseProblem(exception, line, cursor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.parse.TemplateParser#templateParseProblem(java.lang.String,
     *      org.apache.tapestry.ILocation, int, int)
     */
    protected void templateParseProblem(String message, Location location, int line, int cursor)
            throws TemplateParseException
    {
        if (fProblemCollector != null)
        {
            fProblemCollector.addProblem(
                    IProblem.ERROR,
                    getSourceLocation(line, cursor, message),
                    message,
                    false,
                    IProblem.NOT_QUICK_FIXABLE);
        }

        super.templateParseProblem(message, location, line, cursor);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.parse.TemplateParser#attributeBeginEvent(java.lang.String, int, int)
     */
    protected void attributeBeginEvent(String attributeName, int startLine, int cursorPosition)
    {
        fEventHandler.attributeBegin(attributeName, startLine, cursorPosition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.parse.TemplateParser#attributeEndEvent(int)
     */
    protected void attributeEndEvent(int cursorPosition)
    {
        fEventHandler.attributeEnd(cursorPosition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.parse.TemplateParser#tagBeginEvent(int, int)
     */
    protected void tagBeginEvent(int startLine, int cursorPosition)
    {
        fEventHandler.tagBegin(startLine, cursorPosition);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.tapestry.parse.TemplateParser#tagEndEvent(int)
     */
    protected void tagEndEvent(int cursorPosition)
    {
        fEventHandler.tagEnd(cursorPosition);
    }

    protected Object findCaselessly(String key, Map map)
    {
        Object result = map.get(key);

        if (result != null)
            return result;

        Iterator i = map.entrySet().iterator();
        while (i.hasNext())
        {
            Map.Entry entry = (Map.Entry) i.next();

            String entryKey = (String) entry.getKey();

            if (entryKey.equalsIgnoreCase(key))
                return entry.getValue();
        }

        return null;
    }

}