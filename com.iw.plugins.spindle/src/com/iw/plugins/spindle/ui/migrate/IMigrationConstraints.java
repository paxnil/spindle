package com.iw.plugins.spindle.ui.migrate;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Works Inc.
 * All Rights Reserved.
 */
public interface IMigrationConstraints {

  /**
   * Constraint to tell MigrationWorkUnits to ensure any DTD changes happen.
   * If this constraint is missing, no DTD changes will occur.
   */
  public int MIGRATE_DTD = 0x00000001;

  /**
   * Constraint to tell MigrationWorkUnits to ensure that any components
   * or pages that define contained components using full paths (pre 1.3 DTD)
   * are changed to use aliases instead.
   * If this constraint is missing, no component alias changes will occur
   */
  public int MIGRATE_COMPONENT_ALIASES = 0x00000002;

  /**
   * Constraint to tell MigrationWorkUnits to ensure that any pages
   * that are contained in .jwc files are moved to .page files (assuming
   * that the DTD is 1.3 or better or that we are upping the DTD to 1.3 or better
   * during this migration
   * If this constraint is missing, no page migration will occur
   */
  public int MIGRATE_UPGRADE_PAGES = 0x00000004;
  
  public int ANY = MIGRATE_DTD | MIGRATE_COMPONENT_ALIASES | MIGRATE_UPGRADE_PAGES;

}
