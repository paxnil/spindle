package com.iw.plugins.spindle.editors;

import java.util.Iterator;
import java.util.Vector;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;


public class SelectionProvider implements ISelectionProvider {

  private Vector listeners = new Vector();
  private ISelection selection;

  public SelectionProvider() {
  }
  
  public void addSelectionChangedListener(ISelectionChangedListener listener) {
    listeners.addElement(listener);
  }
  
  public ISelection getSelection() {
    return selection;
  }
  
  public void removeSelectionChangedListener(ISelectionChangedListener listener) {
    listeners.removeElement(listener);
  }
  
  public synchronized void setSelection(ISelection selection) {
    this.selection = selection;
    SelectionChangedEvent event = new SelectionChangedEvent(this, selection);
    for (Iterator iter = ((Vector) listeners.clone()).iterator(); iter.hasNext();) {
      ISelectionChangedListener listener = (ISelectionChangedListener) iter.next();
      listener.selectionChanged(event);
    }
  }

}
