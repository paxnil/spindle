package org.eclipse.pde.internal.ui.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;

import com.iw.plugins.spindle.TapestryPlugin;

public class SystemFileDocumentProvider extends StreamDocumentProvider {

	public SystemFileDocumentProvider(IDocumentPartitioner partitioner) {
		this(partitioner, null);
	}

	public SystemFileDocumentProvider(
		IDocumentPartitioner partitioner,
		String encoding) {
		super(partitioner, encoding);
	}

	protected IDocument createDocument(Object element) throws CoreException {
		if (element instanceof SystemFileEditorInput) {
			Document document = new Document();
			IDocumentPartitioner part = getPartitioner();
			if (part != null) {
				part.connect(document);
				document.setDocumentPartitioner(part);
			}
			File file =
				(File) ((SystemFileEditorInput) element).getAdapter(File.class);
			setDocumentContent(document, file);
			return document;
		}
		return null;
	}
	protected void doSaveDocument(
		IProgressMonitor monitor,
		Object element,
		IDocument document,
		boolean force)
		throws CoreException {
	}
	protected void setDocumentContent(IDocument document, File file) {
		try {
			InputStream contentStream = new FileInputStream(file);
			setDocumentContent(document, contentStream);
			contentStream.close();
		} catch (IOException e) {
			TapestryPlugin.getDefault().logException(e);
		}
	}

}