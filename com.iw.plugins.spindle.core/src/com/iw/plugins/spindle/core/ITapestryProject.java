package com.iw.plugins.spindle.core;

import org.eclipse.core.resources.IFolder;


public interface ITapestryProject extends ITapestryArtifact {

  ITapestryArtifact createRoot(IFolder folder);

}
