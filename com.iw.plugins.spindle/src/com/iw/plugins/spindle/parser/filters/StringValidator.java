package com.iw.plugins.spindle.parser.filters;

import java.util.HashMap;
import java.util.Map;

import net.sf.tapestry.ApplicationRuntimeException;
import net.sf.tapestry.Tapestry;
import net.sf.tapestry.util.xml.DocumentParseException;
import net.sf.tapestry.util.xml.InvalidStringException;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class StringValidator {

  static private PatternCompiler patternCompiler;

  static private PatternMatcher matcher;

  static private Map compiledPatterns;

  static public void validate(String value, String pattern, String errorKey)
    throws InvalidStringException {
    if (compiledPatterns == null)
      compiledPatterns = new HashMap();

    Pattern compiled = (Pattern) compiledPatterns.get(pattern);

    if (compiled == null) {
      compiled = compilePattern(pattern);

      compiledPatterns.put(pattern, compiled);
    }

    if (matcher == null)
      matcher = new Perl5Matcher();

    if (matcher.matches(value, compiled))
      return;

    throw new InvalidStringException(Tapestry.getString(errorKey, value), value, null);
  }

  static private Pattern compilePattern(String pattern) {
    if (patternCompiler == null)
      patternCompiler = new Perl5Compiler();

    try {
      return patternCompiler.compile(pattern, Perl5Compiler.SINGLELINE_MASK);
    } catch (MalformedPatternException ex) {
      throw new ApplicationRuntimeException(ex);
    }
  }

}
