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
 * Portions created by the Initial Developer are Copyright (C) 2003
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@intelligentworks.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors.spec;

import java.util.Map;

import org.apache.tapestry.spec.IApplicationSpecification;
import org.apache.tapestry.spec.IComponentSpecification;
import org.apache.tapestry.spec.ILibrarySpecification;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.text.source.SourceViewerConfiguration;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.core.TapestryCore;
import com.iw.plugins.spindle.core.TapestryProject;
import com.iw.plugins.spindle.core.artifacts.TapestryArtifactManager;
import com.iw.plugins.spindle.core.parser.Parser;
import com.iw.plugins.spindle.core.scanning.ApplicationScanner;
import com.iw.plugins.spindle.core.scanning.ComponentScanner;
import com.iw.plugins.spindle.core.scanning.IScannerValidator;
import com.iw.plugins.spindle.core.scanning.LibraryScanner;
import com.iw.plugins.spindle.core.scanning.ScannerException;
import com.iw.plugins.spindle.core.scanning.SpecificationScanner;
import com.iw.plugins.spindle.core.scanning.SpecificationValidator;
import com.iw.plugins.spindle.core.scanning.W3CAccess;
import com.iw.plugins.spindle.core.source.IProblem;
import com.iw.plugins.spindle.core.source.IProblemCollector;
import com.iw.plugins.spindle.core.spec.PluginComponentSpecification;
import com.iw.plugins.spindle.core.util.Assert;
import com.iw.plugins.spindle.editors.Editor;
import com.iw.plugins.spindle.editors.ReconcileWorker;

/**
 *  Editor for Tapestry Spec files
 * 
 * @author glongman@intelligentworks.com
 * @version $Id$
 */
public class SpecEditor extends Editor
{

    ReconcileWorker fReconciler = null;
    IScannerValidator fValidator = null;
    Parser fParser = new Parser();
    Object fReconciledSpec;

    public SpecEditor()
    {
        super();
    }

    /* (non-Javadoc)
     * @see org.eclipse.ui.texteditor.AbstractTextEditor#doSetInput(org.eclipse.ui.IEditorInput)
     */
    protected void doSetInput(IEditorInput input) throws CoreException
    {
        fReconciler = null;
        if (input instanceof IFileEditorInput)
        {
            // only files have reconcilers
            IFile file = ((IFileEditorInput) input).getFile();
            String extension = file.getFullPath().getFileExtension();
            if (extension == null)
                return;
            if ("application".equals(extension))
            {
                fReconciler = new ApplicationReconciler();
            } else if ("jwc".equals(extension) || "page".equals(extension))
            {
                fReconciler = new ComponentReconciler();
            } else if ("library".equals(extension))
            {
                fReconciler = new LibraryReconciler();
            }
        }
        super.doSetInput(input);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createContentOutlinePage(org.eclipse.ui.IEditorInput)
     */
    public IContentOutlinePage createContentOutlinePage(IEditorInput input)
    {
        return null;
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createDocumentProvider(org.eclipse.ui.IEditorInput)
     */
    protected IDocumentProvider createDocumentProvider(IEditorInput input)
    {
        if (input instanceof IFileEditorInput)
            return UIPlugin.getDefault().getSpecFileDocumentProvider();

        return new SpecStorageDocumentProvider();
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.Editor#createSourceViewerConfiguration()
     */
    protected SourceViewerConfiguration createSourceViewerConfiguration()
    {
        return new XMLConfiguration(UIPlugin.getDefault().getXMLTextTools(), this);
    }

    /* (non-Javadoc)
     * @see com.iw.plugins.spindle.editors.ISelfReconcilingEditor#reconcile(com.iw.plugins.spindle.core.parser.IProblemCollector, org.eclipse.core.runtime.IProgressMonitor)
     */
    public void reconcile(IProblemCollector collector, IProgressMonitor monitor)
    {
        fReconciledSpec = null;

        if (fReconciler != null)
            fReconciler.reconcile(collector, monitor);

        //        (() fOutline.setSpec(fReconciledSpec));
    }

    /** 
     * return the Tapestry specification object obtained during the last build 
     * note this method may trigger a build!
     */
    Object getSpec(IFile file)
    {
        IProject project = file.getProject();
        TapestryArtifactManager manager = TapestryArtifactManager.getTapestryArtifactManager();
        Map specs = manager.getSpecMap(project);
        if (specs != null)
            return specs.get(file);

        return null;
    }

    abstract class BaseWorker implements ReconcileWorker
    {
        protected IProblemCollector collector;
        protected IProgressMonitor monitor;

        /**
         * 
         */
        public BaseWorker()
        {
            super();
        }

        /* (non-Javadoc)
         * @see com.iw.plugins.spindle.editors.ReconcileWorker#isReadyToReconcile()
         */
        public boolean isReadyToReconcile()
        {
            return SpecEditor.this.isReadyToReconcile();
        }

        protected boolean isCancelled()
        {
            return monitor != null && monitor.isCanceled();
        }

        public final void reconcile(IProblemCollector problemCollector, IProgressMonitor progressMonitor)
        {
            Assert.isNotNull(problemCollector);
            this.collector = problemCollector;
            this.monitor = progressMonitor == null ? new NullProgressMonitor() : progressMonitor;
            Object reconcileResult = null;
            boolean didReconcile = false;
            if (!isCancelled())
            {

                IEditorInput input = getEditorInput();
                if ((input instanceof IFileEditorInput))
                {
                    IFile file = ((IFileEditorInput) input).getFile();
                    TapestryProject project = TapestryCore.getDefault().getTapestryProjectFor(file);
                    Object spec = getSpec(file);
                    if (project != null && spec != null)
                    {

                        SpecificationValidator validator;
                        try
                        {
                            validator = new SpecificationValidator(project, false);
                            reconcileResult =
                                doReconcile(getDocumentProvider().getDocument(input).get(), spec, validator);
                        } catch (CoreException e)
                        {
                            UIPlugin.log(e);
                        }
                    }
                    didReconcile = true;
                }
            }
            fReconciledSpec = reconcileResult;
            // Inform the collector that no reconcile occured 
            if (!didReconcile)
            {
                problemCollector.beginCollecting();
                problemCollector.endCollecting();
            }
        }
        /** return true iff a reconcile occured **/
        protected abstract Object doReconcile(String content, Object spec, IScannerValidator validator);

        protected Document parse(String content)
        {
            Assert.isNotNull(collector);
            Document result = null;
            try
            {
                result = fParser.parse(content);
            } catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            collector.beginCollecting();
            IProblem[] problems = fParser.getProblems();
            for (int i = 0; i < problems.length; i++)
            {
                System.err.println(problems[i]);
                collector.addProblem(problems[i]);
            }
            collector.endCollecting();
            System.err.println("fatal?" + fParser.getHasFatalErrors());
            if (fParser.getProblems().length > 0)
                return null;
            return result;
        }
    }

    class ComponentReconciler extends BaseWorker
    {
        ComponentScanner scanner;

        public ComponentReconciler()
        {
            super();
            scanner = new ComponentScanner();
            scanner.setFactory(TapestryCore.getSpecificationFactory());
        }

        protected Object doReconcile(String content, Object spec, IScannerValidator validator)
        {
            if (spec instanceof IComponentSpecification)
            {
                PluginComponentSpecification useSpec = (PluginComponentSpecification) spec;
                if (isCancelled())
                    return null;
                Document document = parse(content);
                if (isCancelled())
                    return null;
                if (document != null)
                {
                    String publicId = W3CAccess.getPublicId(document);
                    if (publicId != null)
                    {
                        scanner.setNamespace(useSpec.getNamespace());
                        scanner.setExternalProblemCollector(collector);
                        scanner.setResourceLocation(useSpec.getSpecificationLocation());
                        validator.setProblemCollector(scanner);

                        try
                        {
                            return scanner.scan(document, validator);
                        } catch (ScannerException e)
                        {
                            e.printStackTrace();
                        }
                    }

                }
            }
            return null;
        }
    }

    class LibraryReconciler extends BaseWorker
    {
        LibraryScanner scanner;

        public LibraryReconciler()
        {
            super();
            scanner = new LibraryScanner();
            scanner.setFactory(TapestryCore.getSpecificationFactory());
        }

        protected boolean isSpecOk(Object spec)
        {
            return spec instanceof ILibrarySpecification;
        }

        protected SpecificationScanner getInitializedScanner(ILibrarySpecification library)
        {
            scanner.setExternalProblemCollector(collector);
            scanner.setResourceLocation(library.getSpecificationLocation());
            String publicId = W3CAccess.getPublicId(fParser.getParsedDocument());
            if (publicId == null)
                return null;
            return scanner;
        }
        protected Object doReconcile(String content, Object spec, IScannerValidator validator)
        {
            if (isSpecOk(spec))
            {
                if (isCancelled())
                    return null;
                Node node = parse(content);
                if (isCancelled())
                    return null;
                if (node != null)
                {
                    SpecificationScanner initializedScanner = getInitializedScanner((ILibrarySpecification) spec);
                    if (initializedScanner != null)
                    {
                        validator.setProblemCollector(initializedScanner);
                        try
                        {
                            return initializedScanner.scan(node, validator);
                        } catch (ScannerException e)
                        {
                            // eat it
                        }
                    }
                }
            }
            return null;
        }
    }

    class ApplicationReconciler extends LibraryReconciler
    {
        ApplicationScanner scanner;

        public ApplicationReconciler()
        {
            super();
            scanner = new ApplicationScanner();
            scanner.setFactory(TapestryCore.getSpecificationFactory());
        }

        protected boolean isSpecOk(Object spec)
        {
            return spec instanceof IApplicationSpecification;
        }
    }

}
