package com.iw.plugins.spindle.spec;

/**
 * @author gwl
 * @version $Id$
 *
 * Copyright 2002, Intelligent Work Inc.
 * All Rights Reserved.
 */
public interface IIdentifiable {

  public String getIdentifier();
  public void setIdentifier(String id);
  public void setParent(Object parent);
  public Object getParent();

}
