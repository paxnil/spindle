/*
 * Created on 13.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package com.iw.plugins.spindle.editors.util;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.Token;

/**
 * @author jll
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DocumentArtifactRule implements IPredicateRule {
    
    public static final IToken TAG = new Token("TAG");
    public static final IToken ENDTAG = new Token("ENDTAG");
    public static final IToken TEXT = new Token("TEXT");
    public static final IToken PI = new Token("PI");
    public static final IToken DECLARATION = new Token("DECL");
    public static final IToken COMMENT = new Token("COMMENT");
    public static final IToken EMPTYTAG = new Token("EMPTYTAG");

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner)
     */
    public IToken evaluate(ICharacterScanner scanner) {
        IToken result = Token.EOF;
        int c = scanner.read();
        
        if (c == -1) {
            return Token.EOF;
        }
        
        if (c != '<') {
            while (c != -1 && c != '<') {
                c = scanner.read();
            }

            scanner.unread();

            return TEXT;
        } else {
            result = TAG;
            c = scanner.read();
            
            switch (c) {
            case '!':
                result = DECLARATION;
                c = scanner.read();
                if (c == '-') {
                    c = scanner.read();
                    if (c == '-') {
                        c = scanner.read();
                        result = COMMENT;
                        c = scanTo(scanner, "-->", '\0', true);
                    } else {
                        c = scanTo(scanner, ">", '"', true);
                    }
                } else {
                    c = scanTo(scanner, ">", '"', true);
                }
                break;
            case '?':
                result = PI;
                c = scanTo(scanner, "?>", '\0', true);
                break;
            case '>':
                break;
            case '/':
                result = ENDTAG;
                c = scanTo(scanner, ">", '"', true);
                break;
            default:            
                c = scanTo(scanner, ">", '"', true);
                if (c != -1) {
                    scanner.unread();
                    scanner.unread();                
                    if (scanner.read() == '/') {
                        result = EMPTYTAG;
                    }
                    scanner.read();
                }
                break;
            }
//            if (c == -1) {
//                return Token.EOF;
//            }
        }
        
        return result;
    }
    
    private int scanTo(ICharacterScanner scanner, String end, char escapeChar, boolean isTagScan) {
        int c;
        int i = 0;
        boolean escaped = false;
        do {
            c = scanner.read();
            if (!escaped && isTagScan && c == '<') {
                scanner.unread();
                scanner.unread();
                return scanner.read();
            }
            if (escapeChar != '\0' && escapeChar == c) {
                escaped = !escaped;
                i = 0;
            } else if (!escaped) {            
                if (c == end.charAt(i)) {
                    i++;
                } else if (i > 0) {
                    i = 0;
                }
            }
            if (i >= end.length()) {
                return c;
            }
        } while (c != -1);
        
        return c;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#evaluate(org.eclipse.jface.text.rules.ICharacterScanner, boolean)
     */
    public IToken evaluate(ICharacterScanner scanner, boolean resume) {
        // TODO Auto-generated method stub
        return evaluate(scanner);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.text.rules.IPredicateRule#getSuccessToken()
     */
    public IToken getSuccessToken() {
        // TODO Auto-generated method stub
        return DECLARATION;
    }

}
