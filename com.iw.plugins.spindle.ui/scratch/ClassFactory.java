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
package com.iw.plugins.spindle.ui.wizards.factories;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.Flags;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.IScanner;
import org.eclipse.jdt.core.compiler.ITerminalSymbols;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.IImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.corext.dom.TokenScanner;
import org.eclipse.jdt.internal.corext.util.CodeFormatterUtil;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.CodeGeneration;
import org.eclipse.jdt.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.swt.SWT;
import org.eclipse.text.edits.MalformedTreeException;
import org.eclipse.text.edits.TextEdit;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.ui.util.UIUtils;

/**
 * @author GWL
 * @version Copyright 2002, Intelligent Works Incoporated All Rights Reserved
 */
public class ClassFactory
{
  static private class DefaultWorkingCopyOwner extends WorkingCopyOwner
  {

    public DefaultWorkingCopyOwner()
    {
      super();
    }
  }

  private static WorkingCopyOwner WC_OWNER = new DefaultWorkingCopyOwner();

  private static String[] getInheritedMethods(
      IType createdType,
      ImportsStructure imports,
      IProgressMonitor monitor) throws CoreException
  {

    ITypeHierarchy hierarchy = createdType.newSupertypeHierarchy(monitor);
    CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings();
    try
    {
      return StubUtility.evalUnimplementedMethods(
          createdType,
          hierarchy,
          false,
          settings,
          imports);
    } catch (NoSuchMethodError e)
    {
      UIPlugin.log("See spindle bug 898181 - will go away when Spindle is ported to 3.0");
      return new String[]{};
    }
  }

  /**
   * Format an entire Java source file
   * 
   * @param sourceString
   *          entire file as one unformatted String
   * @return formatted String
   */
  public static String formatJava(
      String sourceString,
      int initialIndentationLevel,
      String lineDelim)
  {
    String ret = sourceString;
    DefaultCodeFormatterOptions dcfo = DefaultCodeFormatterOptions
        .getJavaConventionsSettings();
    dcfo.line_separator = lineDelim;
    CodeFormatter cformatter = ToolFactory.createCodeFormatter(dcfo.getMap());
    TextEdit te = cformatter.format(
        CodeFormatter.K_COMPILATION_UNIT,
        sourceString,
        0,
        sourceString.length(),
        initialIndentationLevel,
        lineDelim);
    IDocument l_doc = new Document(sourceString);
    try
    {
      te.apply(l_doc);
    } catch (MalformedTreeException e)
    {
      // not fatal, since we still have our original contents
      UIPlugin.warn(e);
    } catch (BadLocationException e)
    {
      // not fatal, since we still have our original contents
      UIPlugin.warn(e);
    }
    String maybe = l_doc.get();
    // in case formatting fails, we'll just return what we got
    if (!didFormatChoke(maybe))
    {
      ret = maybe;
    } else
    {
      UIPlugin.warn("Java formatter choked"); //TODO choked
    }
    return ret + lineDelim; // dunno why we add the extra, but we did
  }

  /**
   * Examines a string and returns the first line delimiter found.
   */
  public static String getLineDelimiterUsed(IJavaElement elem) throws JavaModelException
  {
    ICompilationUnit cu = (ICompilationUnit) elem
        .getAncestor(IJavaElement.COMPILATION_UNIT);
    if (cu != null && cu.exists())
    {
      IBuffer buf = cu.getBuffer();
      int length = buf.getLength();
      for (int i = 0; i < length; i++)
      {
        char ch = buf.getChar(i);
        if (ch == SWT.CR)
        {
          if (i + 1 < length)
          {
            if (buf.getChar(i + 1) == SWT.LF)
            {
              return "\r\n"; //$NON-NLS-1$
            }
          }
          return "\r"; //$NON-NLS-1$
        } else if (ch == SWT.LF)
        {
          return "\n"; //$NON-NLS-1$
        }
      }
    }
    return System.getProperty("line.separator", "\n");
  }

  /**
   * Did the formatter choke, and return garbage?
   */
  protected static boolean didFormatChoke(String maybe)
  {
    if (maybe == null)
      return true;
    if (maybe.length() == 0)
      return true;
    // some known garbage strings, feel free to add more
    if (maybe.startsWith("null"))
      return true;
    if (maybe.startsWith("{MultiTextEdit"))
      return true;
    return false;
  }

  private IPackageFragmentRoot fPackageFragmentyRoot;
  private IPackageFragment fPackageFragment;
  private int fModifiers;
  private String fTypeName;
  private IType fSuperClass;
  private IType[] fSuperInterfaces;
  private boolean fShouldImplementInheritedMethods;
  private IType fCreatedType;
  /**
   * Constructor for ClassFactory.
   */
  public ClassFactory(boolean implementInheritedMethods)
  {
    super();

  }

  public void createType(
      IPackageFragmentRoot root,
      IPackageFragment pack,
      String classname,
      IType superClass,
      IType[] interfaces,
      int modifiers,
      boolean implementInheritedMethods,

      IProgressMonitor monitor) throws CoreException, InterruptedException
  {
    fCreatedType = null;
    fPackageFragmentyRoot = root;
    fPackageFragment = pack;
    fTypeName = classname;
    fSuperClass = superClass;
    fSuperInterfaces = interfaces;
    fModifiers = modifiers;
    fShouldImplementInheritedMethods = implementInheritedMethods;

    createType(monitor);

  
  }

  
  /**
   * @return the created IType or null if a problem occured.
   */
  public IType getCreatedType()
  {
    return fCreatedType;
  }

  private void createType(IProgressMonitor monitor) throws CoreException,
      InterruptedException
  {
    if (monitor == null)
    {
      monitor = new NullProgressMonitor();
    }

    monitor.beginTask(UIPlugin.getString("ClassFactory.operationdesc", fTypeName), 10);

    ICompilationUnit createdWorkingCopy = null;
    try
    {
      IPackageFragmentRoot root = fPackageFragmentyRoot;
      IPackageFragment pack = fPackageFragment;
      if (pack == null)
      {
        pack = root.getPackageFragment(""); //$NON-NLS-1$
      }

      if (!pack.exists())
      {
        String packName = pack.getElementName();
        pack = root.createPackageFragment(packName, true, null);
      }

      monitor.worked(1);

      String clName = fTypeName;

      boolean isInnerClass = false;

      IType createdType;
      ImportsManager imports;
      int indent = 0;

      String lineDelimiter = System.getProperty("line.separator", "\n"); 

      ICompilationUnit parentCU = pack.createCompilationUnit(
          clName + ".java", "", false, new SubProgressMonitor(monitor, 2)); 
      // create a working copy with a new owner
      createdWorkingCopy = parentCU.getWorkingCopy(null);

      // use the compiler template a first time to read the imports
      String content = CodeGeneration.getCompilationUnitContent(
          createdWorkingCopy,
          null,
          "", lineDelimiter); //$NON-NLS-1$
      if (content != null)
        createdWorkingCopy.getBuffer().setContents(content);

      imports = new ImportsManager(createdWorkingCopy);
      // add an import that will be removed again. Having this import solves
      // 14661
      imports.addImport(JavaModelUtil.concatenateName(pack.getElementName(), fTypeName));

      String typeContent = constructTypeStub(imports, lineDelimiter);

      String cuContent = constructCUContent(parentCU, typeContent, lineDelimiter);

      createdWorkingCopy.getBuffer().setContents(cuContent);

      createdType = createdWorkingCopy.getType(clName);

      if (monitor.isCanceled())
      {
        throw new InterruptedException();
      }

      // add imports for superclass/interfaces, so types can be resolved
      // correctly

      ICompilationUnit cu = createdType.getCompilationUnit();
      boolean needsSave = !cu.isWorkingCopy();

      imports.create(needsSave, new SubProgressMonitor(monitor, 1));

      JavaModelUtil.reconcile(cu);

      if (monitor.isCanceled())
      {
        throw new InterruptedException();
      }

      // set up again
      imports = new ImportsManager(imports.getCompilationUnit(), imports.getAddedTypes());

      createTypeMembers(createdType, imports, new SubProgressMonitor(monitor, 1));

      // add imports
      imports.create(needsSave, new SubProgressMonitor(monitor, 1));

      removeUnusedImports(cu, imports.getAddedTypes(), needsSave);

      JavaModelUtil.reconcile(cu);

      ISourceRange range = createdType.getSourceRange();

      IBuffer buf = cu.getBuffer();
      String originalContent = buf.getText(range.getOffset(), range.getLength());

      String formattedContent = CodeFormatterUtil.format(
          CodeFormatter.K_CLASS_BODY_DECLARATIONS,
          originalContent,
          indent,
          null,
          lineDelimiter,
          pack.getJavaProject());
      buf.replace(range.getOffset(), range.getLength(), formattedContent);

      //      String fileComment = getFileComment(cu);
      //      if (fileComment != null && fileComment.length() > 0)
      //      {
      //        buf.replace(0, 0, fileComment + lineDelimiter);
      //      }
      cu.commitWorkingCopy(false, new SubProgressMonitor(monitor, 1));
      if (createdWorkingCopy != null)
      {
        fCreatedType = (IType) createdType.getPrimaryElement();
      } else
      {
        fCreatedType = createdType;
      }
    } finally
    {
      if (createdWorkingCopy != null)
      {
        createdWorkingCopy.discardWorkingCopy();
      }
      monitor.done();
    }

  }

  /*
   * Called from createType to construct the source for this type
   */
  private String constructTypeStub(ImportsManager imports, String lineDelimiter)
  {
    StringBuffer buf = new StringBuffer();

    int modifiers = fModifiers;
    buf.append(Flags.toString(modifiers));
    if (modifiers != 0)
    {
      buf.append(' ');
    }
    //		buf.append(fIsClass ? "class " : "interface "); //$NON-NLS-2$
    // //$NON-NLS-1$
    buf.append("class");
    buf.append(fTypeName);
    writeSuperClass(buf, imports);
    writeSuperInterfaces(buf, imports);
    buf.append('{');
    buf.append(lineDelimiter);
    buf.append(lineDelimiter);
    buf.append('}');
    buf.append(lineDelimiter);
    return buf.toString();
  }

  //	 ---- construct CU body----------------

  private void writeSuperClass(StringBuffer buf, ImportsManager imports)
  {
    String typename = fSuperClass.getElementName();
    //	if (fIsClass && typename.length() > 0 &&
    // !"java.lang.Object".equals(typename)) {
    if (typename.length() > 0 && !"java.lang.Object".equals(typename))
    {
      buf.append(" extends ");

      String qualifiedName = fSuperClass != null ? JavaModelUtil
          .getFullyQualifiedName(fSuperClass) : typename;
      buf.append(imports.addImport(qualifiedName));
    }
  }

  private void writeSuperInterfaces(StringBuffer buf, ImportsManager imports)
  {
    IType[] interfaces = fSuperInterfaces;
    int last = interfaces.length - 1;
    if (last >= 0)
    {
      //		if (fIsClass) {
      if (true)
      {
        buf.append(" implements ");
      } else
      {
        buf.append(" extends ");
      }
      for (int i = 0; i <= last; i++)
      {
        String typename = interfaces[i].getFullyQualifiedName();
        buf.append(imports.addImport(typename));
        if (i < last)
        {
          buf.append(',');
        }
      }
    }
  }

  /*
   * @see NewTypeWizardPage#createTypeMembers
   */
  protected void createTypeMembers(
      IType type,
      ImportsManager imports,
      IProgressMonitor monitor) throws CoreException
  {
    //		boolean doMain= isCreateMain();
    boolean doMain = false;
    //		boolean doConstr= isCreateConstructors();
    boolean doConstr = false;
    //		boolean doInherited= isCreateInherited();
    boolean doInherited = false;
    try 
    {
      createInheritedMethods(type, doConstr, doInherited, imports, new SubProgressMonitor(
          monitor,
          1));
    } catch (CoreException e)
    {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    if (doMain)
    {
      StringBuffer buf = new StringBuffer();
      buf.append("public static void main(");
      buf.append(imports.addImport("java.lang.String"));
      buf.append("[] args) {}");
      type.createMethod(buf.toString(), null, false, null);
    }

    if (monitor != null)
    {
      monitor.done();
    }
  }

  /**
   * Uses the New Java file template from the code template page to generate a
   * compilation unit with the given type content.
   * 
   * @param cu
   *          The new created compilation unit
   * @param typeContent
   *          The content of the type, including signature and type body.
   * @param lineDelimiter
   *          The line delimiter to be used.
   * @return String Returns the result of evaluating the new file template with
   *         the given type content.
   * @throws CoreException
   * @since 2.1
   */
  protected String constructCUContent(
      ICompilationUnit cu,
      String typeContent,
      String lineDelimiter) throws CoreException
  {
    String typeComment = getTypeComment(cu, lineDelimiter);
    IPackageFragment pack = (IPackageFragment) cu.getParent();
    String content = CodeGeneration.getCompilationUnitContent(
        cu,
        typeComment,
        typeContent,
        lineDelimiter);
    if (content != null)
    {
      ASTParser parser = ASTParser.newParser(AST.JLS2);
      parser.setSource(content.toCharArray());
      CompilationUnit unit = (CompilationUnit) parser.createAST(null);
      if ((pack.isDefaultPackage() || unit.getPackage() != null)
          && !unit.types().isEmpty())
      {
        return content;
      }
    }
    StringBuffer buf = new StringBuffer();
    if (!pack.isDefaultPackage())
    {
      buf.append("package ").append(pack.getElementName()).append(';'); //$NON-NLS-1$
    }
    buf.append(lineDelimiter).append(lineDelimiter);
    if (typeComment != null)
    {
      buf.append(typeComment).append(lineDelimiter);
    }
    buf.append(typeContent);
    return buf.toString();
  }

  /**
   * Hook method that gets called from <code>createType</code> to retrieve a
   * type comment. This default implementation returns the content of the 'type
   * comment' template.
   * 
   * @return the type comment or <code>null</code> if a type comment is not
   *         desired
   * 
   * @since 3.0
   */
  protected String getTypeComment(ICompilationUnit parentCU, String lineDelimiter)
  {
    try
    {
      StringBuffer typeName = new StringBuffer();
      //			if (isEnclosingTypeSelected()) {
      //				typeName.append(JavaModelUtil.getTypeQualifiedName(getEnclosingType())).append('.');
      //			}
      typeName.append(fTypeName);
      String comment = CodeGeneration.getTypeComment(
          parentCU,
          typeName.toString(),
          lineDelimiter);
      if (comment != null && isValidComment(comment))
      {
        return comment;
      }
    } catch (CoreException e)
    {
      JavaPlugin.log(e);
    }
    return null;
  }

  private boolean isValidComment(String template)
  {
    IScanner scanner = ToolFactory.createScanner(true, false, false, false);
    scanner.setSource(template.toCharArray());
    try
    {
      int next = scanner.getNextToken();
      while (TokenScanner.isComment(next))
      {
        next = scanner.getNextToken();
      }
      return next == ITerminalSymbols.TokenNameEOF;
    } catch (InvalidInputException e)
    {}
    return false;
  }

  /**
   * Creates the bodies of all unimplemented methods and constructors and adds
   * them to the type. Method is typically called by implementers of
   * <code>NewTypeWizardPage</code> to add needed method and constructors.
   * 
   * @param type
   *          the type for which the new methods and constructor are to be
   *          created
   * @param doConstructors
   *          if <code>true</code> unimplemented constructors are created
   * @param doUnimplementedMethods
   *          if <code>true</code> unimplemented methods are created
   * @param imports
   *          an import manager to add all needed import statements
   * @param monitor
   *          a progress monitor to report progress
   */
  protected IMethod[] createInheritedMethods(
      IType type,
      boolean doConstructors,
      boolean doUnimplementedMethods,
      ImportsManager imports,
      IProgressMonitor monitor) throws CoreException
  {
    ArrayList newMethods = new ArrayList();
    ITypeHierarchy hierarchy = null;
    CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings();

    if (doConstructors)
    {
//      hierarchy = type.newSupertypeHierarchy(type.getCompilationUnit().getOwner(), monitor);
//      IType superclass = hierarchy.getSuperclass(type);
      IType superclass = fSuperClass; 
      if (superclass != null)
      {
        String[] constructors = StubUtility.evalConstructors(
            type,
            superclass,
            settings,
            imports);
        if (constructors != null)
        {
          for (int i = 0; i < constructors.length; i++)
          {
            newMethods.add(constructors[i]);
          }
        }

      }
    }
    if (doUnimplementedMethods)
    {
      if (hierarchy == null)
      {
        hierarchy = type.newSupertypeHierarchy(monitor);
      }
      String[] unimplemented = StubUtility.evalUnimplementedMethods(
          type,
          hierarchy,
          false,
          settings,
          imports);
      if (unimplemented != null)
      {
        for (int i = 0; i < unimplemented.length; i++)
        {
          newMethods.add(unimplemented[i]);
        }
      }
    }
    IMethod[] createdMethods = new IMethod[newMethods.size()];
    for (int i = 0; i < newMethods.size(); i++)
    {
      String content = (String) newMethods.get(i) + '\n'; // content will be
      // formatted, OK to
      // use \n
      createdMethods[i] = type.createMethod(content, null, false, null);
    }
    return createdMethods;
  }

  private void removeUnusedImports(ICompilationUnit cu, Set addedTypes, boolean needsSave) throws CoreException
  {
    ASTParser parser = ASTParser.newParser(AST.JLS2);
    parser.setSource(cu);
    parser.setResolveBindings(true);
    CompilationUnit root = (CompilationUnit) parser.createAST(null);
    IProblem[] problems = root.getProblems();
    ArrayList res = new ArrayList();
    for (int i = 0; i < problems.length; i++)
    {
      int id = problems[i].getID();
      if (id == IProblem.UnusedImport || id == IProblem.ImportNotVisible)
      { // not visibles hide unused -> remove both
        String imp = problems[i].getArguments()[0];
        res.add(imp);
      }
    }
    if (!res.isEmpty())
    {
      ImportsManager imports = new ImportsManager(cu, addedTypes);
      for (int i = 0; i < res.size(); i++)
      {
        String curr = (String) res.get(i);
        imports.removeImport(curr);
      }
      imports.create(needsSave, null);
    }
  }

  /**
   * Class used in stub creation routines to add needed imports to a compilation
   * unit.
   */
  public static class ImportsManager implements /* internal */IImportsStructure
  {

    private ImportsStructure fImportsStructure;
    private Set fAddedTypes;

    ImportsManager(IImportsStructure importsStructure)
    {
      fImportsStructure = (ImportsStructure) importsStructure;
    }

    ImportsManager(ICompilationUnit createdWorkingCopy) throws CoreException
    {
      this(createdWorkingCopy, new HashSet());
    }

    ImportsManager(ICompilationUnit createdWorkingCopy, Set addedTypes)
        throws CoreException
    {
      IPreferenceStore store = PreferenceConstants.getPreferenceStore();
      String[] prefOrder = UIUtils.getImportOrderPreference();
      int threshold = UIUtils.getImportNumberThreshold();
      fAddedTypes = addedTypes;

      fImportsStructure = new ImportsStructure(
          createdWorkingCopy,
          prefOrder,
          threshold,
          true);
    }

    ICompilationUnit getCompilationUnit()
    {
      return fImportsStructure.getCompilationUnit();
    }

    /**
     * Adds a new import declaration that is sorted in the existing imports. If
     * an import already exists or the import would conflict with another import
     * of an other type with the same simple name the import is not added.
     * 
     * @param qualifiedTypeName
     *          The fully qualified name of the type to import (dot separated)
     * @return Returns the simple type name that can be used in the code or the
     *         fully qualified type name if an import conflict prevented the
     *         import
     */
    public String addImport(String qualifiedTypeName)
    {
      fAddedTypes.add(qualifiedTypeName);
      return fImportsStructure.addImport(qualifiedTypeName);
    }

    void create(boolean needsSave, SubProgressMonitor monitor) throws CoreException
    {
      fImportsStructure.create(needsSave, monitor);
    }

    void removeImport(String qualifiedName)
    {
      if (fAddedTypes.contains(qualifiedName))
      {
        fImportsStructure.removeImport(qualifiedName);
      }
    }

    Set getAddedTypes()
    {
      return fAddedTypes;
    }
  }

}