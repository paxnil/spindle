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

package tests.Scanners;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.apache.tapestry.parse.SpecificationParser;
import org.apache.tapestry.spec.IAssetSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.IContainedComponent;
import org.apache.tapestry.spec.SpecFactory;

import com.iw.plugins.spindle.core.parser.DefaultProblem;
import com.iw.plugins.spindle.core.parser.IProblem;
import com.iw.plugins.spindle.core.parser.IProblemCollector;
import com.iw.plugins.spindle.core.parser.ISourceLocation;
import com.iw.plugins.spindle.core.scanning.BaseValidator;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.spec.TapestryCoreSpecFactory;

/**
 *  TESTs for the BaseValidator
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class ScannerBaseValidatorTest extends TestCase
{

    TestCollector collector;
    ISourceLocation dummyLocation;
    /**
     * Constructor for ScannerValidatorTest.
     * @param name
     */
    public ScannerBaseValidatorTest(String name)
    {
        super(name);

    }

    public void setUp()
    {
        collector = new TestCollector();
        dummyLocation = new DummySourceLocation();
    }

    class TestProblem extends DefaultProblem
    {

        public TestProblem(int severity, ISourceLocation location, String message)
        {
            super("test", severity, message, location.getLineNumber(), location.getCharStart(), location.getCharEnd());
        }

    }

    public void testValidatePattern()
    {
        //  Using pattern: "^_?[a-zA-Z]\\w*$"
        //
        //  Simple property names match Java variable names; a leading letter
        //  (or underscore), followed by letters, numbers and underscores.
        String pattern = SpecificationParser.COMPONENT_ID_PATTERN;
        String valid = "Corky";
        String invalid = ",Corky";

        //first without a collector
        BaseValidator validator = new BaseValidator();
        try
        {
            validator.validatePattern(
                valid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("failed on valid");
        }
        try
        {
            validator.validatePattern(
                valid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR,
                dummyLocation);
        } catch (ScannerException e)
        {
            fail("failed on valid");
        }
        try
        {
            validator.validatePattern(
                invalid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR);
            fail("passed an invalid string");
        } catch (ScannerException e)
        {
            // expected result
        }
        try
        {
            validator.validatePattern(
                invalid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR,
                dummyLocation);
            fail("passed an invalid string");
        } catch (ScannerException e)
        {
            // expected result
        }
        String dummy = validator.getNextDummyString();
        try
        {
            validator.validatePattern(
                dummy,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("failed on dummy, should have ignored it");
        }
        try
        {
            validator.validatePattern(
                dummy,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR,
                dummyLocation);
        } catch (ScannerException e)
        {
            fail("failed on dummy, should have ignored it");
        }

        // now with the collector

        validator.setProblemCollector(collector);

        try
        {
            validator.validatePattern(
                valid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }

        assertTrue("failed on valid", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validatePattern(
                valid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR,
                dummyLocation);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on valid", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validatePattern(
                invalid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("passed invalid", !collector.isEmpty() && collector.getProblems().length == 1);
        collector.clear();
        try
        {
            validator.validatePattern(
                invalid,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR,
                dummyLocation);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("passed invalid", !collector.isEmpty() && collector.getProblems().length == 1);
        collector.clear();
        String dummy2 = validator.getNextDummyString();
        try
        {
            validator.validatePattern(
                dummy2,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on dummy, should have ignored it", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validatePattern(
                dummy2,
                pattern,
                "SpecificationParser.framework-library-id-is-reserved",
                IProblem.ERROR,
                dummyLocation);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on dummy, should have ignored it", collector.isEmpty());
        collector.clear();
    }

    public void testValidateExpression()
    {
        // OGNL only
        String valid = "#fact = :[#this<=1? 1 : #this*#fact(#this-1)], #fact(30H)";
        // missing trailing }
        String invalid = "~listeners.{?#this instanceof antlr.TokenListener";

        //first without collector
        BaseValidator validator = new BaseValidator();
        try
        {
            validator.validateExpression(valid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("failed on valid");
        }
        try
        {
            validator.validateExpression(valid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("failed on valid");
        }
        try
        {
            validator.validateExpression(invalid, IProblem.ERROR);
            fail("passed invalid expression");
        } catch (ScannerException e)
        {
            // expected result
        }
        try
        {
            validator.validateExpression(invalid, IProblem.ERROR, dummyLocation);
            fail("passed invalid expression");
        } catch (ScannerException e)
        {
            // expected result
        }
        String dummy = validator.getNextDummyString();
        try
        {
            validator.validateExpression(dummy, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("failed on dummy");
        }
        try
        {
            validator.validateExpression(dummy, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("failed on dummy");
        }

        // now with collector
        validator.setProblemCollector(collector);
        try
        {
            validator.validateExpression(valid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on valid expression", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateExpression(valid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on valid expression", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateExpression(invalid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on invalid expression", !collector.isEmpty() && collector.getProblems().length == 1);
        collector.clear();
        try
        {
            validator.validateExpression(invalid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("failed on invalid expression", !collector.isEmpty() && collector.getProblems().length == 1);
        collector.clear();
        String dummy2 = validator.getNextDummyString();
        try
        {
            validator.validateExpression(dummy2, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("should have ignored dummy expression", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateExpression(dummy2, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("Should not have thrown exception");
        }
        assertTrue("should have ignored dummy expression", collector.isEmpty());
    }

    // note that BaseValidator never fails on these
    public void testValidateTypeName()
    {
        String valid = "java.lang.Object";
        String invalid = "invalid";
        BaseValidator validator = new BaseValidator();
        try
        {
            validator.validateTypeName(valid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("should have passed");
        }
        try
        {
            validator.validateTypeName(valid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("should have passed");
        }
        try
        {
            validator.validateTypeName(invalid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("should have passed");
        }
        try
        {
            validator.validateTypeName(invalid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("should have passed");
        }
        try
        {
            validator.validateTypeName(null, IProblem.ERROR);
        } catch (Throwable e)
        {
            fail("should have passed");
        }
        try
        {
            validator.validateTypeName(null, IProblem.ERROR, dummyLocation);
        } catch (Throwable e)
        {
            fail("should have passed");
        }
        String dummy = validator.getNextDummyString();
        try
        {
            validator.validateTypeName(dummy, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("should have passed");
        }
        try
        {
            validator.validateTypeName(dummy, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("should have passed");
        }

        // now use the collector

        validator.setProblemCollector(collector);
        try
        {
            validator.validateTypeName(valid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateTypeName(valid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateTypeName(invalid, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateTypeName(invalid, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateTypeName(null, IProblem.ERROR);
        } catch (Throwable e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateTypeName(null, IProblem.ERROR, dummyLocation);
        } catch (Throwable e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        String dummy2 = validator.getNextDummyString();
        try
        {
            validator.validateTypeName(dummy2, IProblem.ERROR);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());
        collector.clear();
        try
        {
            validator.validateTypeName(dummy2, IProblem.ERROR, dummyLocation);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue("should have passed", collector.isEmpty());

    }

    public void testValidateAsset()
    {
        BaseValidator validator = new BaseValidator();
        doValidateAsset(validator, new TapestryCoreSpecFactory());
        doValidateAsset(validator, new SpecFactory());
    }

    //  note that BaseValidator never fails on these
    protected void doValidateAsset(IScannerValidator validator, SpecFactory factory)
    {
        IComponentSpecification componentSpec = factory.createComponentSpecification();
        IAssetSpecification assetSpec = factory.createAssetSpecification();
        try
        {
            validator.validateAsset(componentSpec, assetSpec, null);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        validator.setProblemCollector(collector);
        try
        {
            validator.validateAsset(componentSpec, assetSpec, null);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue(collector.isEmpty());
    }

    public void testValidateContainedComponent()
    {
        BaseValidator validator = new BaseValidator();
        doValidateContainedComponent(validator, new TapestryCoreSpecFactory());
        doValidateContainedComponent(validator, new SpecFactory());
    }

    //  note that BaseValidator never fails on these
    protected void doValidateContainedComponent(IScannerValidator validator, SpecFactory factory)
    {
        IComponentSpecification componentSpec = factory.createComponentSpecification();
        IContainedComponent containedSpec = factory.createContainedComponent();
        try
        {
            validator.validateContainedComponent(componentSpec, containedSpec, null);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        validator.setProblemCollector(collector);
        try
        {
            validator.validateContainedComponent(componentSpec, containedSpec, null);
        } catch (ScannerException e)
        {
            fail("no exception expected");
        }
        assertTrue(collector.isEmpty());
    }

    class TestCollector implements IProblemCollector
    {
        List problems = new ArrayList();
        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.IProblemCollector#addProblem(com.iw.plugins.spindle.core.parser.IProblem)
         */
        public void addProblem(IProblem problem)
        {
            problems.add(problem);

        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.IProblemCollector#getProblems()
         */
        public IProblem[] getProblems()
        {
            return (IProblem[]) problems.toArray(new IProblem[problems.size()]);
        }

        public void clear()
        {
            problems.clear();
        }

        public boolean isEmpty()
        {
            return problems.isEmpty();
        }
        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.IProblemCollector#addProblem(int, com.iw.plugins.spindle.core.parser.ISourceLocation, java.lang.String)
         */
        public void addProblem(int severity, ISourceLocation location, String message)
        {
            problems.add(
                new DefaultProblem(
                    "poo",
                    severity,
                    message,
                    location.getLineNumber(),
                    location.getCharStart(),
                    location.getCharEnd()));

        }

        public void addSourceProblem(int severity, ISourceLocation location, String message)
        {
            addProblem(severity, location, message);

        }

    }

    class DummySourceLocation implements ISourceLocation
    {
        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getCharEnd()
         */
        public int getCharEnd()
        {
            return 0;
        }
        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getCharStart()
        */
        public int getCharStart()
        {
            return 0;
        }
        /* (non-Javadoc)
        * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getLineNumber()
        */
        public int getLineNumber()
        {
            return 0;
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.ISourceLocation#contains(int)
         */
        public boolean contains(int cursorPosition)
        {
            return cursorPosition == 0;
        }
        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.core.parser.ISourceLocation#getLocationOffset(int)
         */
        public ISourceLocation getLocationOffset(int cursorPosition)
        {
            return this;
        }

    }
}
