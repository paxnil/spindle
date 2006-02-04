package net.sf.spindle.xerces.parser.xml.dom;

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
import java.util.HashMap;
import java.util.Map;

import net.sf.spindle.xerces.parser.xml.DTDConfiguration;
import net.sf.spindle.xerces.parser.xml.TapestryEntityResolver;

import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.grammars.Grammar;
import org.apache.xerces.xni.grammars.XMLGrammarDescription;
import org.apache.xerces.xni.grammars.XMLGrammarPool;


public class TapestryDOMParserConfiguration extends DTDConfiguration
{
    /** custom Xerces Feature identifier */
    public static final String AUGMENTATIONS = "http://net.sf.spindle/xml/features/augmentations-location";

    public static XMLGrammarPool GRAMMAR_POOL = new GrammarPoolImpl();

    public static void clearCache()
    {
        GRAMMAR_POOL.clear();
    }

    /**
     * Constructor for MyConfiguration.
     */
    public TapestryDOMParserConfiguration()
    {
        this(GRAMMAR_POOL);        
    }

    public TapestryDOMParserConfiguration(XMLGrammarPool grammarPool)
    {
        super(null, grammarPool);
        addRecognizedFeatures(new String[]
        { AUGMENTATIONS });                
    }

    /**
     * @see org.apache.xerces.parsers.BasicParserConfiguration#reset()
     */
    protected void reset() throws XNIException
    {
        super.reset();

        setProperty(
                "http://apache.org/xml/properties/internal/entity-resolver",
                new TapestryEntityResolver());        
    }
    
    public static class GrammarPoolImpl implements XMLGrammarPool {
        
        Map grammarsMap = new HashMap();
        
        private boolean locked = false;

        public Grammar[] retrieveInitialGrammarSet(String grammarType)
        {
            return new Grammar [] {};
        }

        public void cacheGrammars(String grammarType, Grammar[] grammars)
        {
            if (locked)
                return;
           
            for (int i = 0; i < grammars.length; i++)
            {
                XMLGrammarDescription grammarDescription = grammars[i].getGrammarDescription();
                grammarsMap.put(grammarDescription.getPublicId(), grammars[i]);
            }
            
        }

        public Grammar retrieveGrammar(XMLGrammarDescription desc)
        {
            return (Grammar) grammarsMap.get(desc.getPublicId());
        }

        public void lockPool()
        {
            locked = true;
            
        }

        public void unlockPool()
        {
            locked  = false;
            
        }

        public void clear()
        {
            grammarsMap.clear();
            
        }
        
    }

}