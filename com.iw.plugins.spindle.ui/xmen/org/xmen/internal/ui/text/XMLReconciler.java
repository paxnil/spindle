/*
 * Created on 16.05.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.xmen.internal.ui.text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.BadPositionCategoryException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.xmen.xml.XMLNode;

import com.iw.plugins.spindle.UIPlugin;

/**
 * @author jll
 * 
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class XMLReconciler implements IDocumentListener
{//IReconcilingStrategy, {
  private IDocument document;
  private ArrayList storedPos;
  private ArrayList deleted;
  private ArrayList added;
  //    private XMLOutlinePage op;
  //    private XMLTable table;
  //    private XMLTextEditor editor;
  private XMLNode root;
  //    private Namespace dtdGrammar;
  private Map namespaces;
  private boolean sendOnlyAdditions = false;
  private boolean firstTime = true;

  /**
   *  
   */
  //    public XMLReconciler(XMLTextEditor editor, XMLOutlinePage op) {
  //        this.editor = editor;
  //        this.op = op;
  //        namespaces = new TreeMap();
  //    }
  public XMLReconciler()
  {

  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.reconciler.IReconcilingStrategy#setDocument(org.eclipse.jface.text.IDocument)
   */
  public void setDocument(IDocument document)
  {
    this.document = document;
  }

  //    /* (non-Javadoc)
  //     * @see
  // org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.reconciler.DirtyRegion,
  // org.eclipse.jface.text.IRegion)
  //     */
  //    public void reconcile(DirtyRegion dirtyRegion, IRegion subRegion) {
  //        System.out.println("Reconcile dirty:"+dirtyRegion);
  //    }
  //
  //    /* (non-Javadoc)
  //     * @see
  // org.eclipse.jface.text.reconciler.IReconcilingStrategy#reconcile(org.eclipse.jface.text.IRegion)
  //     */
  //    public void reconcile(IRegion partition) {
  //        System.out.println("Reconcile for "+((TypedRegion) partition).getType());
  //        documentChanged(null);
  //// try {
  //// Position[] pos = document.getPositions("__content_types_category");
  //// for (int i = 0; i < pos.length; i++) {
  //// System.out.println(((TypedPosition) pos[i]).getType());
  //// }
  //// } catch (BadPositionCategoryException e) {
  //// e.printStackTrace();
  //// }
  //    }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
   */
  public void documentAboutToBeChanged(DocumentEvent event)
  {
    // do nothing
  }

  public void createTree(IDocument doc)
  {
    document = doc;
    try
    {
      Position[] pos = doc.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
      Arrays.sort(pos, XMLNode.COMPARATOR);
      root = new XMLNode(0, 0, "/", doc);
      storedPos = new ArrayList();
      root.setParent(null);
      for (int i = 0; i < pos.length; i++)
      {
        storedPos.add(pos[i]);
        ((XMLNode) pos[i]).setAdded(false);
      }
      added = (ArrayList) storedPos.clone();
      deleted = new ArrayList();
      fix(pos, 0, root);
      firstTime = false;
      //   TODO revisit editor.setNamespaces(namespaces);

      //   original XMLNode parent = root;
      //            for (Iterator it = added.iterator(); it.hasNext(); ) {
      //                XMLNode node = (XMLNode) it.next();
      //                
      //                if (!node.getType().equals("ENDTAG")) {
      //                    node.setParent(parent);
      //                }
      //                if (node.getType().equals("TAG")) {
      //                    parent = node;
      //                } else if (node.getType().equals("ENDTAG")) {
      //                    node.setParent(parent.getParent());
      //                    node.setCorrespondingNode(parent);
      //                    parent.setCorrespondingNode(node);
      //                    parent = parent.getParent();
      //                } else if (node.getType().equals("DECL")) {
      //                    if (node.getName().equals("!DOCTYPE")) {
      //                        String dtdLocation = node.getDTDLocation();
      //                        if (dtdLocation != null) {
      //                            dtdGrammar = new Namespace(null, null, dtdLocation, null);
      //                            if (!dtdGrammar.readSchema(dtdLocation)) {
      //                                dtdGrammar = null;
      //                            }
      //                        }
      //                    }
      //                }
      //            }
    } catch (BadPositionCategoryException e)
    {
      UIPlugin.log(e);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
   */
  public void documentChanged(DocumentEvent event)
  {
    IDocument doc = event.getDocument();
    document = doc;
    XMLNode firstAdded = null;
    try
    {
      Position[] pos = doc.getPositions(XMLDocumentPartitioner.CONTENT_TYPES_CATEGORY);
      Arrays.sort(pos, XMLNode.COMPARATOR);
      if (root == null)
      {
        System.out.println("root is null!");
        // original root = new XMLNode(0, 0, "/", doc);
        //                storedPos = new ArrayList();
        //                root.setParent(null);
        //                for (int i = 0; i < pos.length; i++) {
        //                    storedPos.add(pos[i]);
        //                    ((XMLNode) pos[i]).setAdded(false);
        //                }
        //                added = (ArrayList) storedPos.clone();
        //                deleted = new ArrayList();
        //                XMLNode parent = root;
        //                for (Iterator it = added.iterator(); it.hasNext(); ) {
        //                    XMLNode node = (XMLNode) it.next();
        //                    
        //                    if (!node.getType().equals("ENDTAG")) {
        //                        node.setParent(parent);
        //                    }
        //                    if (node.getType().equals("TAG")) {
        //                        parent = node;
        //                    }
        //                    if (node.getType().equals("ENDTAG")) {
        //                        node.setParent(parent.getParent());
        //                        node.setCorrespondingNode(parent);
        //                        parent.setCorrespondingNode(node);
        //                        parent = parent.getParent();
        //                    }
        //                }
      } else
      {
        if (deleted == null)
        {
          deleted = new ArrayList();
        } else
        {
          deleted.clear();
        }
        for (int i = 0; i < storedPos.size(); i++)
        {
          if (((XMLNode) storedPos.get(i)).isDeleted())
          {
            XMLNode n = (XMLNode) storedPos.get(i);
            deleted.add(n);
          }
        }
        updateTree(deleted);
        //   original for (int i = 0; i < pos.length; i++) {
        //                    XMLNode n = (XMLNode) pos[i];
        //                    if (n.isModified()) {
        //                        n.setModified(false);
        //                        if (n.getType().equals(ITypeConstants.DECL)) {
        //                            if (n.getName().equals("!DOCTYPE")) {
        //                                String dtdLocation = n.getDTDLocation();
        //                                if (dtdLocation != null) {
        //                                    dtdGrammar = new Namespace(null, null, dtdLocation, null);
        //                                    if (!dtdGrammar.readSchema(dtdLocation)) {
        //                                        dtdGrammar = null;
        //                                    }
        //                                }
        //                            }
        //                        }
        //                    }
        //                }
        if (added == null)
        {
          added = new ArrayList();
        } else
        {
          added.clear();
        }
        boolean fixed = false;
        for (int i = 0; i < pos.length; i++)
        {
          XMLNode n = (XMLNode) pos[i];
          if (n.isAdded() || n.isModified())
          {
            if (n.isAdded())
            {
              storedPos.add(n);
            }
            n.setAdded(false);
            n.setModified(false);
            if (!fixed)
            {
              firstAdded = n;
              if (i == 0)
              {
                fix(pos, i, root);
              } else
              {
                XMLNode prev = (XMLNode) pos[i - 1];

                if (prev.getType().equals(ITypeConstants.TAG))
                {
                  fix(pos, i, prev);
                } else
                {
                  fix(pos, i, prev.getParent());
                }
              }
              if (n.getType().equals(ITypeConstants.DECL))
              {
                
                //   TODO revisit if (n.getName().equals("!DOCTYPE")) {
                //                                    String dtdLocation = n.getDTDLocation();
                //                                    if (dtdLocation != null) {
                //                                        dtdGrammar = new Namespace(null, null, dtdLocation,
                // editor.getMPE());
                //                                        if (!dtdGrammar.readSchema(dtdLocation)) {
                //                                            dtdGrammar = null;
                //                                        } else {
                //                                            editor.setDTD(dtdGrammar);
                //                                        }
                //                                    }
                //                                } else if (n.getParent() != null &&
                // ITypeConstants.START_DECL.equals(n.getParent().getType())) {
                //                                	if (n.getParent().getCorrespondingNode() != n.getParent()) {
                //                                		XMLNode start = n.getParent();
                //                                		XMLNode end = n.getParent().getCorrespondingNode();
                //
                //										try {
                //											dtdGrammar = Namespace.createDTD(doc.get(start.getOffset(),
                // end.getOffset() +end.getLength() - start.getOffset()),
                // editor.getMPE());
                //											if (dtdGrammar != null) {
                //												editor.setDTD(dtdGrammar);
                //											}
                //										} catch (BadLocationException e1) {
                //											e1.printStackTrace();
                //										}
                //                                	}
                //                                }
              } else
              {
                fixed = true;
              }
            }
          }
        }
      }
      //  TODO revisit if (op != null && op.getControl() != null &&
      // !op.getControl().isDisposed() && op.getControl().isVisible()) {
      //                op.update(root);
      //            }
      //            if (sendOnlyAdditions) {
      //                table.incrementalUpdate(firstAdded);
      //            }
      //            editor.setNamespaces(namespaces);
      sendOnlyAdditions = false;
    } catch (BadPositionCategoryException e)
    {
      UIPlugin.log(e);
    }
  }

  public void prependNewNodeTo(String name, XMLNode to)
  {
    try
    {
      sendOnlyAdditions = true;
      document.replace(to.getOffset(), 0, "<" + name + "/>");
    } catch (BadLocationException e)
    {

      UIPlugin.log(e);
    }
  }

  public void appendNewNodeTo(String name, XMLNode to)
  {
    try
    {
      sendOnlyAdditions = true;
      document.replace(to.getOffset() + to.getLength(), 0, "<" + name + "/>");
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }
  }

  private void fix(Position[] pos, int start, XMLNode parent)
  {
    if (parent == null)
    {
      parent = root;
    }

    for (int i = start; i < pos.length; i++)
    {
      XMLNode node = (XMLNode) pos[i];
      String type = node.getType();
      XMLNode nodeParent = node.getParent();
      String nodeName = node.getName();

      if (node.isDeleted)
      {
        System.out.println("deleted!" + node);
      }

      if (!type.equals(ITypeConstants.ENDTAG) && !type.equals(ITypeConstants.END_DECL))
      {
        if (nodeParent != parent)
        {
          if (nodeParent != null)
          {
            nodeParent.removeChild(node);
          }
          node.setParent(parent);
        }
      }

      if (type.equals(ITypeConstants.TAG))
      {
        List attrs = node.getAttributes();
        node.setCorrespondingNode(node);
        for (Iterator it = attrs.iterator(); it.hasNext();)
        {
          XMLNode element = (XMLNode) it.next();
          String name = element.getName();
          //   TODO revist if (name.indexOf("xmlns") != -1) {
          //                        String value = element.getValue();
          //                        int index = name.indexOf(":");
          //                        String prefix = null;
          //                        Namespace ns = null;
          //                        
          //                        if (index == -1) {
          //                            prefix = Namespace.DEFAULTNAMESPACE;
          //                        } else {
          //                            prefix = name.substring(index + 1);
          //                        }
          //                        
          //                        ns = (Namespace) namespaces.get(prefix);
          //                        
          //                        if (ns == null || !ns.getUri().equals(value)) {
          //                            ns = new Namespace(prefix, value, null, editor.getMPE());
          //                            if (!firstTime &&
          // XMenPlugin.getDefault().getPreferenceStore().getBoolean("LoadSchemasOnlyOnSave"))
          // {
          //                                namespaces.put(prefix, ns);
          //                            } else {
          //                                if (ns.readSchema(value)) {
          //                                    namespaces.put(prefix, ns);
          //                                }
          //                            }
          //                        }
          //
          //                    }
        }
        parent = node;
      } else if (type.equals(ITypeConstants.ENDTAG))
      {
        node.setCorrespondingNode(node);
        if (parent != null)
        {
          XMLNode newParent = parent;
          while (!nodeName.equals(newParent.getName()) && newParent != root
              && newParent != null)
          {
            newParent = newParent.getParent();
          }
          if (newParent != root && newParent != null)
          {
            parent = newParent;
            node.setCorrespondingNode(parent);
            parent.setCorrespondingNode(node);
            if (nodeParent != parent.getParent())
            {
              if (nodeParent != null)
              {
                nodeParent.removeChild(node);
              }
              node.setParent(parent.getParent());
            }
            parent = parent.getParent();
          } else
          {
            node.setParent(parent);
          }
        }
      } else if (type.equals(ITypeConstants.DECL))
      {

        if ("!DOCTYPE".equals(nodeName))
        {
          root.publicId = node.readPublicId();
          root.rootNodeId = node.getRootNodeId();
        }
        //   TODO revisit if (n.getName() != null &&
        // n.getName().equals("!DOCTYPE")) {
        //                    String dtdLocation = n.getDTDLocation();
        //                    if (dtdLocation != null) {
        //                        dtdGrammar = new Namespace(null, null, dtdLocation, editor.getMPE());
        //                        if (!dtdGrammar.readSchema(dtdLocation)) {
        //                            dtdGrammar = null;
        //                        } else {
        //                            editor.setDTD(dtdGrammar);
        //                        }
        //                    }
        //                }
      } else if (ITypeConstants.START_DECL.equals(type))
      {
        node.setCorrespondingNode(null);
        parent = node;
      } else if (ITypeConstants.END_DECL.equals(type))
      {
        node.setCorrespondingNode(null);
        if (parent != null)
        {
          XMLNode newParent = parent;
          while (!ITypeConstants.START_DECL.equals(newParent.getType())
              && newParent != root && newParent != null)
          {
            newParent = newParent.getParent();
          }
          if (newParent != root && newParent != null)
          {
            parent = newParent;
            node.setCorrespondingNode(parent);
            parent.setCorrespondingNode(node);
            //	TODO revisit try {
            //							dtdGrammar = Namespace.createDTD(document.get(parent.getOffset(),
            // n.getOffset() + n.getLength() - parent.getOffset()),
            // editor.getMPE());
            //							if (dtdGrammar != null) {
            //								editor.setDTD(dtdGrammar);
            //							}
            //						} catch (BadLocationException e1) {
            //							e1.printStackTrace();
            //						}

            if (nodeParent != parent.getParent())
            {
              if (nodeParent != null)
              {
                nodeParent.removeChild(node);
              }
              node.setParent(parent.getParent());
            }
            parent = parent.getParent();
          } else
          {
            node.setParent(parent);
          }
        }
      }
    }
  }

  //    private void viewTree(XMLNode n) {
  //        System.out.println(n);
  //        System.out.println("Children:");
  //        if (n.getChildren().size() == 0) {
  //            System.out.println("\tnone");
  //        }
  //        for (int i = 0; i < n.getChildren().size(); i++) {
  //            System.out.println(((XMLNode)
  // n.getChildren().get(i)).getName()+"|"+n.getChildren().get(i));
  //            //viewTree((XMLNode) n.getChildren().get(i));
  //        }
  //    }

  public void updateTree(List deleted)
  {
    for (Iterator it = deleted.iterator(); it.hasNext();)
    {
      XMLNode node = (XMLNode) it.next();

      /*
       * if (!node.getChildren().isEmpty()) { for (Iterator iter =
       * node.getChildren().iterator(); iter.hasNext();) { XMLNode element =
       * (XMLNode) iter.next(); element.setParent(node.getParent()); }
       * node.getChildren().clear(); } else if (node.getType().equals("ENDTAG")) {
       * List after = node.getParent().getChildrenAfter(node); for (Iterator
       * iter = after.iterator(); iter.hasNext();) { XMLNode element = (XMLNode)
       * iter.next(); element.setParent(node.getCorrespondingNode()); } } else
       */
      if (node.getType().equals(ITypeConstants.DECL))
      {
        String nodeName = node.getName();
        if ("!DOCTYPE".equals(nodeName))
        {
          root.publicId = null;
          root.rootNodeId = null;
          System.out.println("dtd removed");
        }       
      }
      if (node.getParent() != null)
      {
        node.getParent().removeChild(node);
        node.setParent(null);
      } else
      {
        System.out.println("parent not set!" + node.getType() + "|" + node.getName()
            + "|" + node);
      }
      storedPos.remove(node);
    }
  }

  public void insertTagAfter(String name, XMLNode node)
  {
    int offset = node.getOffset() + node.getLength() + 1;

    try
    {
      document.replace(offset, 0, "<" + name + "/>");
      sendOnlyAdditions = true;
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }
  }

  /**
   * @return
   */
  public XMLNode getRoot()
  {
    return root;
  }

  /**
   * @param node
   */
  public void setRoot(XMLNode node)
  {
    root = node;
  }

  /**
   * @return
   */
  public ArrayList getStoredPos()
  {
    return storedPos;
  }

  //    /**
  //     * @return
  //     */
  //    public Namespace getDTDGrammar() {
  //        return dtdGrammar;
  //    }
  //
  //    /**
  //     * @return
  //     */
  //    public XMLTable getTable() {
  //        return table;
  //    }
  //
  //    /**
  //     * @param table
  //     */
  //    public void setTable(XMLTable table) {
  //        this.table = table;
  //    }
  //
  //    /**
  //     * @return
  //     */
  //    public Map getNamespaces() {
  //        return namespaces;
  //    }

  /**
   * @param name
   * @param to
   */
  public void addAttributeTo(String name, XMLNode to)
  {
    try
    {
      sendOnlyAdditions = true;
      document.replace(to.getOffset() + to.getLength() - 1, 0, " " + name + "=\"\"");
    } catch (BadLocationException e)
    {
      UIPlugin.log(e);
    }
  }

  public void reset()
  {
    // TODO Auto-generated method stub

  }

  public void dispose()
  {
    document.removeDocumentListener(this);
    document = null;
  }

  public String getPublicId()
  {
    if (root == null)
      return null;

    return root.publicId;
  }

  public String getRootNodeId()
  {
    if (root == null)
      return null;

    return root.rootNodeId;
  }
}