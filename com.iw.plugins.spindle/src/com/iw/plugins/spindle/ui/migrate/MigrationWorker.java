package com.iw.plugins.spindle.ui.migrate;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IStorage;
import org.eclipse.jdt.core.IPackageFragment;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.model.manager.TapestryProjectModelManager;
import com.iw.plugins.spindle.project.ITapestryProject;
import com.iw.plugins.spindle.util.lookup.ILookupRequestor;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public class MigrationWorker implements ILookupRequestor {

    List results = new ArrayList();
    TapestryProjectModelManager mgr;
    IModelMigrator migrator;
    /**
     * Constructor for MigrationLookupRequestor.
     */
    public MigrationWorker(IModelMigrator migrator, TapestryProjectModelManager manager) {
      this.migrator = migrator;
      this.mgr = manager;
    }

    /**
    * @see com.iw.plugins.spindle.util.ITapestryLookupRequestor#accept(IStorage, IPackageFragment)
    */
    public boolean accept(IStorage storage, IPackageFragment frgament) {
      if (storage.isReadOnly()) {
      	return false;
      }
      mgr.connect(storage, this);
      ITapestryModel model = mgr.getEditableModel(storage, this);
      if (model == null || !model.isEditable()) {
      	mgr.disconnect(storage, this);
      	return false;
      }
      if (model.isLoaded() && migrator.migrate(model)) {
        results.add(model);
      }
      return true;
    }

    /**
     * @see com.iw.plugins.spindle.util.ITapestryLookupRequestor#isCancelled()
     */
    public boolean isCancelled() {
      return false;
    }

    public ITapestryModel[] getResults() {
      return (ITapestryModel[]) results.toArray(new ITapestryModel[results.size()]);
    }

  }
