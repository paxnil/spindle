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
package com.iw.plugins.spindle.ui.wizards.factories;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.IImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.formatter.DefaultCodeFormatterOptions;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;
import org.eclipse.jdt.ui.CodeGeneration;
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

  private IFile fGeneratedFile;

  private boolean fShouldImplementInheritedMethods;
  /**
   * Constructor for ClassFactory.
   */
  public ClassFactory(boolean implementInheritedMethods)
  {
    super();
    fShouldImplementInheritedMethods = implementInheritedMethods;
  }

  public IType createClass(
      IPackageFragmentRoot root,
      IPackageFragment pack,
      String classname,
      IType superClass,
      IType[] interfaces,
      boolean isAbstract,
      IMethodEvaluator methodEvaluator,
      IProgressMonitor monitor) throws CoreException, InterruptedException
  {
    fGeneratedFile = null;
    ICompilationUnit createdWorkingCopy = null;
    IType createdType = null;
    try
    {

      String superclassName = (superClass == null ? "java.lang.Object" : superClass
          .getElementName());
      String qualifiedName = pack == null ? classname : pack.getElementName() + "."
          + classname;

      String[] prefOrder = UIUtils.getImportOrderPreference();
      int threshold = UIUtils.getImportNumberThreshold();

      monitor.beginTask(UIPlugin.getString("ClassFactory.operationdesc", classname), 10);
      if (pack == null)
      {
        pack = root.getPackageFragment("");
      }
      if (!pack.exists())
      {
        String packName = pack.getElementName();
        pack = root.createPackageFragment(packName, true, null);
      }

      monitor.worked(1);

      ICompilationUnit parentCU = pack.createCompilationUnit(
          classname + ".java",
          "",
          false,
          new SubProgressMonitor(monitor, 2));

      createdWorkingCopy = (ICompilationUnit) parentCU.getWorkingCopy(
          WC_OWNER,
          null,
          monitor);

      ImportsStructure imports = new ImportsStructure(
          createdWorkingCopy,
          prefOrder,
          threshold,
          false);
      imports.addImport(pack.getElementName(), classname);

      String lineDelimiter = StubUtility.getLineDelimiterUsed(parentCU);

      String typeContent = createClassContent(
          parentCU,
          classname,
          superClass,
          interfaces,
          imports,
          isAbstract,
          lineDelimiter);

      String cuContent = constructCUContent(
          parentCU,
          classname,
          typeContent,
          lineDelimiter);

      createdWorkingCopy.getBuffer().setContents(cuContent);

      createdType = createdWorkingCopy.getType(classname);

      imports.create(false, new SubProgressMonitor(monitor, 1));

      ICompilationUnit cu = createdType.getCompilationUnit();

      synchronized (cu)
      {
        cu.reconcile(false, new SubProgressMonitor(monitor, 1));
      }

      createAllNewMethods(methodEvaluator, createdType, imports, new SubProgressMonitor(
          monitor,
          1));

      imports.create(false, new SubProgressMonitor(monitor, 1));

      synchronized (cu)
      {
        cu.reconcile(false, new SubProgressMonitor(monitor, 1));
      }

      ISourceRange range = createdType.getSourceRange();
      IBuffer buffer = cu.getBuffer();
      String originalContent = buffer.getText(range.getOffset(), range.getLength());
      String formattedContent;

      formattedContent = formatJava(originalContent, 0, lineDelimiter);

      buffer.replace(range.getOffset(), range.getLength(), formattedContent);

      cu.commitWorkingCopy(false, new SubProgressMonitor(monitor, 1));

      IContainer container = (IContainer) pack.getUnderlyingResource();
      fGeneratedFile = container.getFile(new Path(classname + ".java"));
    } catch (RuntimeException e)
    {
      UIPlugin.log(e);
      throw e;
    } finally
    {
      if (createdWorkingCopy != null)
        createdWorkingCopy.discardWorkingCopy();

      monitor.done();
    }
    //        createAllNewMethods(methodEvaluator, createdType, imports, new
    // SubProgressMonitor(monitor, 1));
    //
    //        monitor.worked(1);
    //
    //        String formattedContent = StubUtility.codeFormat(createdType.getSource(),
    // indent, lineDelimiter);
    //
    //        save(createdType, formattedContent, monitor);
    //        monitor.done();
    return createdType;
  }

  protected String constructCUContent(
      ICompilationUnit cu,
      String typeName,
      String typeContent,
      String lineDelimiter) throws CoreException
  {
    StringBuffer typeQualifiedName = new StringBuffer();
    typeQualifiedName.append(typeName);
    String typeComment = CodeGeneration.getTypeComment(
        cu,
        typeQualifiedName.toString(),
        lineDelimiter);
    IPackageFragment pack = (IPackageFragment) cu.getParent();
    String content = CodeGeneration.getCompilationUnitContent(
        cu,
        typeComment,
        typeContent,
        lineDelimiter);
    if (content != null)
    {
      
      ASTParser parser = ASTParser.newParser(AST.JLS2);  // handles JLS2 (J2SE 1.4)
      parser.setSource(content.toCharArray());
      CompilationUnit unit = (CompilationUnit) parser.createAST(null);
      

      //      CompilationUnit unit = AST.parseCompilationUnit(content.toCharArray());
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

  /*
   * Called from createType to construct the source for this type
   */
  private String createClassContent(
      ICompilationUnit parentCU,
      String classname,
      IType superclass,
      IType[] interfaces,
      IImportsStructure imports,
      boolean isAbstract,
      String lineDelimiter)
  {
    StringBuffer buf = new StringBuffer();
    buf.append("public");
    if (isAbstract)
    {
      buf.append(" abstract");
    }
    buf.append(" class ");
    buf.append(classname);
    writeSuperClass(superclass, buf, imports);
    writeInterfaces(interfaces, buf, imports);
    buf.append(" {");
    buf.append(lineDelimiter);
    buf.append(lineDelimiter);
    buf.append('}');
    buf.append(lineDelimiter);
    return buf.toString();
  }

  private void writeSuperClass(
      IType superclass,
      StringBuffer buf,
      IImportsStructure imports)
  {
    if (superclass == null)
    {
      return;
    }
    String typename = superclass.getElementName();
    if (!"".equals(typename) && !"java.lang.Object".equals(typename))
    {
      buf.append(" extends ");
      buf.append(Signature.getSimpleName(typename));
      if (!"java.lang.Object".equals(typename))
      {
        imports.addImport(superclass.getFullyQualifiedName());
      } else
      {
        imports.addImport(typename);
      }
    }
  }

  private void writeInterfaces(
      IType[] interfaces,
      StringBuffer buf,
      IImportsStructure imports)
  {
    if (interfaces == null || interfaces.length == 0)
    {
      return;
    }
    buf.append(" implements ");
    for (int i = 0; i < interfaces.length; i++)
    {
      String iface = interfaces[i].getElementName();
      buf.append(Signature.getSimpleName(iface));
      imports.addImport(interfaces[i].getFullyQualifiedName());
      if (i < interfaces.length - 1)
      {
        buf.append(", ");
      }
    }
  }

  private void createAllNewMethods(
      IMethodEvaluator methodEvaluator,
      IType createdType,
      ImportsStructure imports,
      IProgressMonitor monitor) throws CoreException, JavaModelException
  {

    IMethod[] inherited = null;

    if (fShouldImplementInheritedMethods)
      inherited = createMethods(
          getInheritedMethods(createdType, imports, monitor),
          createdType,
          imports);

    if (methodEvaluator != null && inherited != null)
    {
      methodEvaluator.newInheritedMethods(createdType, inherited);
      String[] emethods = methodEvaluator.methodsToCreate();
      if (emethods.length > 0)
      {
        methodEvaluator.createdMethods(createdType, createMethods(
            emethods,
            createdType,
            imports));
      }

    }
  }

  private IMethod[] createMethods(
      String[] methods,
      IType createdType,
      ImportsStructure imports) throws CoreException, JavaModelException
  {
    IMethod[] newMethods = new IMethod[methods.length];
    if (methods.length > 0)
    {
      for (int i = 0; i < methods.length; i++)
      {
        newMethods[i] = createdType.createMethod(methods[i], null, false, null);
      }
      // add imports
      imports.create(true, null);
    }
    return newMethods;
  }

  private void save(IType createdType, String formattedContent, IProgressMonitor monitor) throws JavaModelException
  {
    ISourceRange range = createdType.getSourceRange();
    IBuffer buf = createdType.getCompilationUnit().getBuffer();
    buf.replace(range.getOffset(), range.getLength(), formattedContent);
    buf.save(new SubProgressMonitor(monitor, 1), false);
  }

  /**
   * @return
   */
  public IFile getGeneratedFile()
  {
    return fGeneratedFile;
  }

}