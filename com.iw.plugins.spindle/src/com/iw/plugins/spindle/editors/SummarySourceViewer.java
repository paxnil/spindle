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
package com.iw.plugins.spindle.editors;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.rules.RuleBasedPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.SourceViewer;
import org.eclipse.jface.text.source.VerticalRuler;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.pde.internal.ui.editor.XMLConfiguration;
import org.eclipse.pde.internal.ui.editor.text.PDEPartitionScanner;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

import com.iw.plugins.spindle.model.ITapestryModel;
import com.iw.plugins.spindle.ui.text.ColorManager;
import com.iw.plugins.spindle.ui.text.ISpindleColorManager;

public class SummarySourceViewer extends SourceViewer  {

  static private IDocument NotFoundDocument = null;
  static private IDocument ErrorReadingDocument = null;
  static {
    NotFoundDocument = new Document();
    NotFoundDocument.set("\n\n\n\t\tNo Data Found");
    ErrorReadingDocument = new Document();
    ErrorReadingDocument.set("\n\n\n\t\tAn Error occurred reading the document");
  }

  private Font currentFont;
  private IPreferenceStore preferenceStore;
  private IPropertyChangeListener propertyChangeListener = new PropertyChangeListener();
  private IDocumentProvider provider;
  private IStorage currentStorage;
  private IMenuListener menuListener, popupListener;
  private Menu menu;

  protected ISpindleColorManager colorManager = new ColorManager();

  /**
   * Constructor for OverviewSourceViewer
   */
  public SummarySourceViewer(Composite parent) {
    super(
      parent,
      new VerticalRuler(1),
      SWT.V_SCROLL | SWT.H_SCROLL | SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);

    Control control = getControl();
    initializeWidgetFont((StyledText) getTextWidget());
    provider = new StorageDocumentProvider();
    MenuManager manager = new MenuManager("readonly-source-viewer", "readonly-source-viewer");
    manager.setRemoveAllWhenShown(true);
    manager.addMenuListener(getContextMenuListener());
    menu = manager.createContextMenu(getTextWidget());
    getTextWidget().setMenu(menu);
    configure(new XMLConfiguration(colorManager));
  }

  protected final IMenuListener getContextMenuListener() {
    if (menuListener == null) {
      menuListener = new IMenuListener() {

        public void menuAboutToShow(IMenuManager menu) {
          String id = menu.getId();
          if ("readonly-source-viewer".equals(id)) {
            getTextWidget().setFocus();
            if (popupListener != null) {
              popupListener.menuAboutToShow(menu);
            }
          }
        }
      };
    }
    return menuListener;
  }

  public void dispose() {

    if (colorManager != null) {
      colorManager.dispose();
      colorManager = null;
    }

    if (currentFont != null) {
      currentFont.dispose();
      currentFont = null;
    }
    if (propertyChangeListener != null) {
      if (preferenceStore != null) {
        preferenceStore.removePropertyChangeListener(propertyChangeListener);
        preferenceStore = null;
      }
      propertyChangeListener = null;
    }
    if (super.getControl() != null) {
      super.getControl().dispose();
    }
  }

  public void setPopupListener(IMenuListener listener) {
    popupListener = listener;
  }

  public IStorage getCurrentStorage() {
    return currentStorage;
  }

  public void update(IAnnotationModel model, ITapestryModel tapmodel) {
    update(model, tapmodel.getUnderlyingStorage());
  }

  public void updateNotFound() {
    setDocument(NotFoundDocument);
    showAnnotations(false);
    setEditable(false);
    currentStorage = null;
  }

  public void update(IAnnotationModel model, IStorage storage) {
    if (storage == null) {
      updateNotFound();
    } else {
      setDocument(provider.getDocument(storage), model);
      showAnnotations(model != null);
    }
    setEditable(false);
    currentStorage = storage;
  }

  private void initializeWidgetFont(StyledText styledText) {
    IPreferenceStore store = getPreferenceStore();
    if (store != null) {

      FontData data = null;

      if (store.contains(JFaceResources.TEXT_FONT) && !store.isDefault(JFaceResources.TEXT_FONT)) {
        data = PreferenceConverter.getFontData(store, JFaceResources.TEXT_FONT);
      } else {
        data = PreferenceConverter.getDefaultFontData(store, JFaceResources.TEXT_FONT);
      }

      if (data != null) {
        Font font = new Font(styledText.getDisplay(), data);
        styledText.setFont(font);

        if (currentFont != null)
          currentFont.dispose();

        currentFont = font;
        return;
      }
    }

    // if all the preferences failed
    styledText.setFont(JFaceResources.getTextFont());
  }



  protected void setPreferenceStore(IPreferenceStore store) {
    if (preferenceStore != null) {
      preferenceStore.removePropertyChangeListener(propertyChangeListener);
    }
    preferenceStore = store;
    if (preferenceStore != null) {
      preferenceStore.addPropertyChangeListener(propertyChangeListener);
    }
  }

  protected IPreferenceStore getPreferenceStore() {
    return preferenceStore;
  }

  protected IDocumentPartitioner createDocumentPartitioner() {
    RuleBasedPartitioner partitioner =
      new RuleBasedPartitioner(
        new PDEPartitionScanner(),
        new String[] { PDEPartitionScanner.XML_TAG, PDEPartitionScanner.XML_COMMENT });
    return partitioner;
  }

  class PropertyChangeListener implements IPropertyChangeListener {

    public void propertyChange(PropertyChangeEvent event) {
      if (JFaceResources.TEXT_FONT.equals(event.getProperty()))
        initializeWidgetFont((StyledText) getControl());
    }
  };

  protected class StorageDocumentProvider implements IDocumentProvider {

    /**
    * Constructor for StorageDocumentProvider
    */
    public StorageDocumentProvider() {
      super();
    }

    public IDocument createDocument(IStorage storage) throws CoreException {
      Document document = new Document();
      setDocumentContent(document, getStorageContents(storage));
      if (document != null) {
        IDocumentPartitioner partitioner = createDocumentPartitioner();
        if (partitioner != null) {
          partitioner.connect(document);
          document.setDocumentPartitioner(partitioner);
        }
      }
      return document;
    }

    private InputStream getStorageContents(IStorage storage) throws CoreException {
      try {
        return storage.getContents();
      } catch (Exception resex) {
        return ((IFile) storage).getContents(true);
      }
    }

    protected void setDocumentContent(IDocument document, InputStream contentStream)
      throws CoreException {

      Reader in = null;
      try {

        in = new InputStreamReader(new BufferedInputStream(contentStream), "UTF8");
        StringBuffer buffer = new StringBuffer();
        char[] readBuffer = new char[2048];
        int n = in.read(readBuffer);
        while (n > 0) {
          buffer.append(readBuffer, 0, n);
          n = in.read(readBuffer);
        }

        document.set(buffer.toString());
      } catch (IOException x) {
        String clazz = x.getClass().getName();
        clazz = clazz.substring(clazz.lastIndexOf("."), clazz.length());
        document.set("\n\n\n\t\t An error occurred: " + clazz + " " + x.getMessage());
      } finally {
        if (in != null) {
          try {
            in.close();
          } catch (IOException x) {
          }
        }
      }
    } /**
       * @see IDocumentProvider#aboutToChange(Object)
       */
    public void aboutToChange(Object arg0) {
    } /**
       * @see IDocumentProvider#addElementStateListener(IElementStateListener)
       */
    public void addElementStateListener(IElementStateListener arg0) {
    } /**
       * @see IDocumentProvider#canSaveDocument(Object)
       */
    public boolean canSaveDocument(Object arg0) {
      return false;
    } /**
       * @see IDocumentProvider#changed(Object)
       */
    public void changed(Object arg0) {
    } /**
       * @see IDocumentProvider#connect(Object)
       */
    public void connect(Object arg0) throws CoreException {
    } /**
       * @see IDocumentProvider#disconnect(Object)
       */
    public void disconnect(Object arg0) {
    } /**
       * @see IDocumentProvider#getAnnotationModel(Object)
       */
    public IAnnotationModel getAnnotationModel(Object arg0) {
      return null;
    } /**
       * @see IDocumentProvider#getDocument(Object)
       */
    public IDocument getDocument(Object element) {
      try {
        if (element instanceof IStorage) {
          return createDocument((IStorage) element);
        } else if (element instanceof ITapestryModel) {
          return createDocument(((ITapestryModel) element).getUnderlyingStorage());
        }
      } catch (CoreException corex) {
        corex.printStackTrace();
      }
      return null;
    } /**
       * @see IDocumentProvider#getModificationStamp(Object)
       */
    public long getModificationStamp(Object arg0) {
      return 0;
    } /**
       * @see IDocumentProvider#getSynchronizationStamp(Object)
       */
    public long getSynchronizationStamp(Object arg0) {
      return 0;
    } /**
       * @see IDocumentProvider#isDeleted(Object)
       */
    public boolean isDeleted(Object arg0) {
      return false;
    } /**
       * @see IDocumentProvider#mustSaveDocument(Object)
       */
    public boolean mustSaveDocument(Object arg0) {
      return false;
    } /**
       * @see IDocumentProvider#removeElementStateListener(IElementStateListener)
       */
    public void removeElementStateListener(IElementStateListener arg0) {
    } /**
       * @see IDocumentProvider#resetDocument(Object)
       */
    public void resetDocument(Object arg0) throws CoreException {
    } /**
       * @see IDocumentProvider#saveDocument(IProgressMonitor, Object, IDocument, boolean)
       */
    public void saveDocument(IProgressMonitor arg0, Object arg1, IDocument arg2, boolean arg3)
      throws CoreException {
    }

  }
}