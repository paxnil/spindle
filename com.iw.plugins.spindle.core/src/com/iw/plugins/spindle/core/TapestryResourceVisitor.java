package com.iw.plugins.spindle.core;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.JavaModelException;

public class TapestryResourceVisitor implements IResourceVisitor {

  static private List knownExtensions = Arrays.asList(TapestryBuilder.KnownExtensions);

  ArrayList collector;
  FullBuild build;
  private IPath outputLocation;
  private Set knownServletNames;
  private TapestryProject tapestryProject;

  public TapestryResourceVisitor(FullBuild build, ArrayList collector) {
    this.collector = collector;
    this.build = build;
    if (build.knownValidServlets != null) {
      this.knownServletNames = build.knownValidServlets.keySet();
    }
    this.tapestryProject = build.tapestryBuilder.tapestryProject;
    try {
      this.outputLocation = build.javaProject.getOutputLocation();
    } catch (JavaModelException e) {
      TapestryCore.log(e);
    }
  }

  public boolean visit(IResource resource) throws CoreException {
    if (resource.getFullPath().equals(outputLocation)) {
      return false;
    }
    if (resource instanceof IFolder) {
      if (resource.equals(tapestryProject.getAppRootFolder())) {
        collector.add(resource);
        debug(resource, true);
        return false;
      } else if (resource.equals(tapestryProject.getWebContextFolder())) {
        collector.add(resource);
        debug(resource, true);
      } else if (build.knownValidServlets != null) {
        String folderName = resource.getName();
        if (resource.getParent().equals(tapestryProject.getWebContextFolder())
          && knownServletNames.contains(folderName)) {
          collector.add(resource);
          debug(resource, true);
          return false;
        }
      }
    } else if (resource instanceof IFile) {
      String extension = resource.getFileExtension();
      if (knownExtensions.contains(extension)) {
        collector.add(resource);
        debug(resource, true);
      } else {
        debug(resource, false);
      }
    }

    return true;
  }

  protected void debug(IResource resource, boolean included) {
    if (TapestryBuilder.DEBUG) {
      PrintStream printer = (included ? System.out : System.err);
      printer.println(resource.getFullPath().toString());
    }
  }

}
