package com.iw.plugins.spindle.factories;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.codemanipulation.IImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.ImportsStructure;
import org.eclipse.jdt.internal.corext.codemanipulation.StubUtility;
import org.eclipse.jdt.internal.ui.preferences.ImportOrganizePreferencePage;
import org.eclipse.jdt.internal.ui.preferences.JavaPreferencesSettings;

import com.iw.plugins.spindle.MessageUtil;

/**
 * @author GWL
 * @version 
 *
 * Copyright 2002, Intelligent Works Incoporated
 * All Rights Reserved
 */
public class ClassFactory {

  /**
   * Constructor for ClassFactory.
   */
  public ClassFactory() {
    super();
  }

  public IType createClass(
    IPackageFragmentRoot root,
    IPackageFragment pack,
    String classname,
    IType superClass,
    IType [] interfaces,
    IMethodEvaluator methodEvaluator,
    IProgressMonitor monitor)
    throws CoreException, InterruptedException {

    IType createdType;
    ImportsStructure imports;
    int indent = 0;
    String superclassName = (superClass == null ? "java.lang.Object" : superClass.getElementName());

    monitor.beginTask(MessageUtil.getFormattedString("ClassFactory.operationdesc", classname), 10);
    if (pack == null) {
      pack = root.getPackageFragment("");
    }
    if (!pack.exists()) {
      String packName = pack.getElementName();
      pack = root.createPackageFragment(packName, true, null);
    }

    monitor.worked(1);

    String lineDelimiter = null;
    ICompilationUnit parentCU = pack.getCompilationUnit(classname + ".java");
    imports = getImports(parentCU);
    lineDelimiter = StubUtility.getLineDelimiterUsed(parentCU);
    String content = createClassBody(classname, superClass, interfaces, imports, lineDelimiter);
    createdType = parentCU.createType(content, null, false, new SubProgressMonitor(monitor, 5));
    // add imports for sclass, so the type can be parsed correctly
    if (imports != null) {
      imports.create(true, new SubProgressMonitor(monitor, 1));
    }
    createAllNewMethods(methodEvaluator, createdType, imports, new SubProgressMonitor(monitor, 1));

    monitor.worked(1);

    String formattedContent = StubUtility.codeFormat(createdType.getSource(), indent, lineDelimiter);

    save(createdType, formattedContent, monitor);
    monitor.done();
    return createdType;
  }

  private ImportsStructure getImports(ICompilationUnit parentCU) {
    ImportsStructure imports = null;
    String[] prefOrder = ImportOrganizePreferencePage.getImportOrderPreference();
    int threshold = ImportOrganizePreferencePage.getImportNumberThreshold();
    try {
      imports = new ImportsStructure(parentCU, prefOrder, threshold, false);
    } catch (CoreException correx) {
    }
    return imports;
  }

  /*
   * Called from createType to construct the source for this type
   */
  private String createClassBody(
    String classname,
    IType superclass,
    IType [] interfaces,
    IImportsStructure imports,
    String lineDelimiter) {
    StringBuffer buf = new StringBuffer();

    buf.append("public class");
    buf.append(' ');
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

  private void writeSuperClass(IType superclass, StringBuffer buf, IImportsStructure imports) {
    if (superclass == null) {
      return;
    }
    String typename = superclass.getElementName();
    if (!"".equals(typename) && !"java.lang.Object".equals(typename)) {
      buf.append(" extends ");
      buf.append(Signature.getSimpleName(typename));
      if (!"java.lang.Object".equals(typename)) {
        imports.addImport(superclass.getFullyQualifiedName());
      } else {
        imports.addImport(typename);
      }
    }
  }

  private void writeInterfaces(IType[] interfaces, StringBuffer buf, IImportsStructure imports) {
    if (interfaces == null || interfaces.length == 0) {
      return;
    }
    buf.append(" implements ");
    for (int i = 0; i < interfaces.length; i++) {
      String iface = interfaces[i].getElementName();
      buf.append(Signature.getSimpleName(iface));
      imports.addImport(interfaces[i].getFullyQualifiedName());
      if (i < interfaces.length - 1) {
        buf.append(", ");
      }
    }
  }
  
  private void createAllNewMethods(
    IMethodEvaluator methodEvaluator,
    IType createdType,
    ImportsStructure imports,
    IProgressMonitor monitor)
    throws CoreException, JavaModelException {

    IMethod[] inherited = null;
    inherited = createMethods(getInheritedMethods(createdType, imports, monitor), createdType, imports);
    if (methodEvaluator != null) {
      methodEvaluator.newInheritedMethods(createdType, inherited);
      String[] emethods = methodEvaluator.methodsToCreate();
      if (emethods.length > 0) {
        methodEvaluator.createdMethods(createdType, createMethods(emethods, createdType, imports));
      }

    }
  }

  private IMethod[] createMethods(String[] methods, IType createdType, ImportsStructure imports)
    throws CoreException, JavaModelException {

    IMethod[] newMethods = new IMethod[methods.length];
    if (methods.length > 0) {
      for (int i = 0; i < methods.length; i++) {
        newMethods[i] = createdType.createMethod(methods[i], null, false, null);
      }
      // add imports
      imports.create(true, null);
    }
    return newMethods;
  }
  
  private static String[] getInheritedMethods(IType createdType, ImportsStructure imports, IProgressMonitor monitor)
    throws JavaModelException {

    ITypeHierarchy hierarchy = createdType.newSupertypeHierarchy(monitor);
    CodeGenerationSettings settings = JavaPreferencesSettings.getCodeGenerationSettings();
    return StubUtility.evalUnimplementedMethods(createdType, hierarchy, false, settings, null, imports);
  }
  
  private void save(IType createdType, String formattedContent, IProgressMonitor monitor)
    throws JavaModelException {
    ISourceRange range = createdType.getSourceRange();
    IBuffer buf = createdType.getCompilationUnit().getBuffer();
    buf.replace(range.getOffset(), range.getLength(), formattedContent);
    buf.save(new SubProgressMonitor(monitor, 1), false);
  }



}