package com.iw.plugins.spindle.core;

import java.util.HashMap;
import java.util.Map;

public class TapestryModelCache {

  Map cache = new HashMap();

  TapestryModelInfo modelInfo;

  public Object getInfo(ITapestryArtifact artifact) {
    int type = artifact.getArtifactType();
    if (type == ITapestryArtifact.TAPESTRY_MODEL) {
      return modelInfo;
    }
    return cache.get(artifact);
  }

  protected void putInfo(ITapestryArtifact artifact, TapestryArtifactInfo info) {
    int type = artifact.getArtifactType();
    if (type == ITapestryArtifact.TAPESTRY_MODEL) {
      modelInfo = (TapestryModelInfo) info;
    }
    cache.put(artifact, info);
  }

  protected void removeInfo(ITapestryArtifact artifact) {
    int type = artifact.getArtifactType();
    if (type == ITapestryArtifact.TAPESTRY_MODEL) {
      modelInfo = null;
    }
    cache.remove(artifact);
  }

}
