package org.eclipse.pde.internal.ui.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.ui.IStorageEditorInput;

import com.iw.plugins.spindle.TapestryPlugin;

public class StorageDocumentProvider extends StreamDocumentProvider {

	public StorageDocumentProvider(IDocumentPartitioner partitioner) {
		this(partitioner, null);
	}

	public StorageDocumentProvider(
		IDocumentPartitioner partitioner,
		String encoding) {
		super(partitioner, encoding);
	}

	protected IDocument createDocument(Object element) throws CoreException {
		if (element instanceof IStorageEditorInput) {
			Document document = new Document();
			IDocumentPartitioner part = getPartitioner();
			if (part != null) {
				part.connect(document);
				document.setDocumentPartitioner(part);
			}
			IStorage storage = ((IStorageEditorInput)element).getStorage();
			setDocumentContent(document, storage);
			return document;
		}
		return null;
	}
	protected void setDocumentContent(IDocument document, IStorage storage) {
		try {
			InputStream contentStream = storage.getContents();
			setDocumentContent(document, contentStream);
			contentStream.close();
		} catch (Exception e) {
			TapestryPlugin.getDefault().logException(e);
		}
	}

}