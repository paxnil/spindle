package com.iw.plugins.spindle.core;

public class TapestryProjectInfo extends OpenableArtifactInfo {

  /**
   * Constructor for TapestryProjectInfo.
   */
  public TapestryProjectInfo() {
    super();
    parent = TapestryModelManager.getTapestryModelManager().getTapestryModel();
  }

}
