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
 * Portions created by the Initial Developer are Copyright (C) 2002
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */



package com.iw.plugins.spindle.ui.migrate;

import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.ui.actions.RenameAction;
import org.eclipse.jface.util.Assert;
import org.eclipse.pde.core.IModelChangedEvent;
import org.eclipse.pde.core.IModelChangedListener;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.TapestryLibraryModel;
import com.iw.plugins.spindle.spec.PluginApplicationSpecification;
import com.iw.plugins.spindle.spec.XMLUtil;
import com.iw.plugins.spindle.util.Indenter;
import com.iw.plugins.spindle.util.Utils;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public abstract class MigrationWorkUnit implements IMigrationConstraints, IModelChangedListener {

  protected MigrationContext context;
  protected ITapestryModel sourceModel;

  private String oldPath;
  private String newPath;
  private String oldContents;

  private ITapestryModel model;

  private boolean dirty = false;

  private int requiredPublicId;
  
  private boolean committed = false;

  /**
   * Constructor for MigrationWorkUnit.
   */
  public MigrationWorkUnit() {
    super();
  }

  public MigrationWorkUnit(
    MigrationContext context,
    ITapestryModel sourceModel,
    int requiredPublicId) {

    this.context = context;
    this.sourceModel = sourceModel;
    Assert.isNotNull(XMLUtil.getPublicId(requiredPublicId));
    this.requiredPublicId = requiredPublicId;
    sourceModel.addModelChangedListener(this);
  }

  /**
  * @see org.eclipse.pde.core.IModelChangedListener#modelChanged(IModelChangedEvent)
  */
  public void modelChanged(IModelChangedEvent event) {
    dirty = true;
  }
  
  public boolean isMigratingDTD() {
  	
  	return (context.getConstraints() & MIGRATE_DTD) != 0;
  	
  }

  public boolean migrate() throws CoreException {

    prepareToMigrate();
    doMigration();
    return isDirty();

  }

  /**
   * Method prepareToMigrate.
   */
  protected void prepareToMigrate() throws CoreException {

    saveOldSource();
  }

  /**
  * Method doMigration.
  */
  protected abstract void doMigration();

  protected void saveOldSource() throws CoreException {

    IFile sourceFile = (IFile) sourceModel.getUnderlyingStorage();

    setOldPath(sourceFile.getFullPath().toString());

    if (sourceFile.exists()) {
      oldContents = new String(Utils.getResourceContentsAsByteArray(sourceFile));

    }
  }

  public void commitMigration(IProgressMonitor monitor) throws CoreException {

    if (dirty && !committed) {

      if (newPath == null || "".equals(newPath)) {

        normalCommit(monitor);

      } else {

        nameChangeCommit(monitor);
      }

    }

  }

  private void normalCommit(IProgressMonitor monitor) throws CoreException {
  	
    IFile sourceFile = (IFile) sourceModel.getUnderlyingStorage();
    
    ByteArrayInputStream source = new ByteArrayInputStream(sourceModel.toXML().getBytes());
    
    sourceFile.setContents(source, true, true, monitor);

  }

  private void nameChangeCommit(IProgressMonitor monitor) throws CoreException {

    normalCommit(monitor);
    
    IFile sourceFile = (IFile) sourceModel.getUnderlyingStorage();
    
    IPath renamedPath = new Path(newPath);
    
    sourceFile.move(renamedPath, true, monitor);

  }

  public boolean canUndo() {

    return false;
  }

  public void undo(IProgressMonitor monitor) {

  }

  protected void migrateDTD() {

    int migrateToDTD = getRequiredPublicId();

    if (migrateToDTD < 0) {

      return;
    }

    ITapestryModel source = sourceModel;

    int currentPublicId = XMLUtil.getDTDVersion(source.getPublicId());

    if (currentPublicId < migrateToDTD) {

      String newPublicId = XMLUtil.getPublicId(migrateToDTD);

      source.setPublicId(newPublicId);

      setDirty(true);

    }
  }

  /**
   * Returns the model.
   * @return ITapestryModel
   */
  public ITapestryModel getModel() {
    return model;
  }

  /**
   * Returns the newPath.
   * @return String
   */
  public String getNewPath() {
    return newPath;
  }

  /**
   * Returns the oldContents.
   * @return String
   */
  public String getOldContents() {
    return oldContents;
  }

  /**
   * Returns the oldPath.
   * @return String
   */
  public String getOldPath() {
    return oldPath;
  }

  /**
   * Sets the model.
   * @param model The model to set
   */
  public void setModel(ITapestryModel model) {
    this.model = model;
  }

  /**
   * Sets the newPath.
   * @param newPath The newPath to set
   */
  public void setNewPath(String newPath) {
    this.newPath = newPath;
  }

  /**
   * Sets the oldContents.
   * @param oldContents The oldContents to set
   */
  public void setOldContents(String oldContents) {
    this.oldContents = oldContents;
  }

  /**
   * Sets the oldPath.
   * @param oldPath The oldPath to set
   */
  public void setOldPath(String oldPath) {
    this.oldPath = oldPath;
  }

  public void write(PrintWriter writer) {

    if (!dirty) {

      return;
    }

    writer.println("<migration-workunit");
    Indenter.printlnIndented(writer, 1, "class=\"" + getClass().getName() + "\"");
    Indenter.printIndented(writer, 1, "old-path=\"" + oldPath + "\"");

    if (newPath != null && !"".equals(newPath)) {

      writer.println();
      Indenter.printIndented(writer, 1, "new-path=\"" + newPath + "\"");

    }

    writer.println(">");

    XMLUtil.writeDescription(writer, 0, oldContents);

    writer.println("</migration-workunit>");
  }

  static public void main(String[] args) {

    MigrationWorkUnit wu = new MigrationWorkUnit() {

      protected void doMigration() {
        setDirty(true);
      };
    };

    wu.setOldPath("/a/a/a/a/a/a/POO.jwc");

    wu.setNewPath("/a/a/a/a/a/a/Poo.page");

    wu.setOldContents(
      "ksdlksjdlkjasdlkjasdlkjasdljkasd\nsalkdjlaskjdlaksjdlkajsdlkjasldkjasd\nasldkjalsjdlaskjdlasjdljasldkjasldkjaslkdjalsjd\naslkdjalskjdlkasjdlkajsd\naslkdjalsjdlasjdlaskjd\nasldkjlasjdlkasjdlajksdlajsd\naslkdjlasjdlkasjdlkjasldjaslkdja\naslkdjlasjdlajsldjasldj");

    StringWriter swriter = new StringWriter();

    PrintWriter writer = new PrintWriter(swriter);

    wu.write(writer);

    System.out.println(swriter.toString());

  }

  /**
   * Returns the dirty.
   * @return boolean
   */
  public boolean isDirty() {
    return dirty;
  }

  /**
   * Sets the dirty.
   * @param dirty The dirty to set
   */
  public void setDirty(boolean dirty) {
    this.dirty = dirty;
  }

  /**
   * Returns the requiredPublicId.
   * @return int
   */
  public int getRequiredPublicId() {
    return requiredPublicId;
  }

  /**
   * Sets the requiredPublicId.
   * @param requiredPublicId The requiredPublicId to set
   */
  public void setRequiredPublicId(int requiredPublicId) {
    this.requiredPublicId = requiredPublicId;
  }

  /**
   * Returns the committed.
   * @return boolean
   */
  public boolean isCommitted() {
    return committed;
  }

}
