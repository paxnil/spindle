package org.eclipse.pde.internal.ui.editor;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentPartitioner;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.ui.texteditor.AbstractDocumentProvider;

import com.iw.plugins.spindle.TapestryPlugin;

public abstract class StreamDocumentProvider extends AbstractDocumentProvider {
	private IDocumentPartitioner partitioner;
	private String enc;

public StreamDocumentProvider(IDocumentPartitioner partitioner, String encoding) {
	this.partitioner = partitioner;
	this.enc = encoding;
}

protected IDocumentPartitioner getPartitioner() {
	return partitioner;
}

protected String getEncoding() {
	return enc;
}

protected IAnnotationModel createAnnotationModel(Object element) throws CoreException {
	return null;
}
protected void doSaveDocument(IProgressMonitor monitor, Object element, IDocument document, boolean force) throws CoreException {}
protected void setDocumentContent(IDocument document, InputStream contentStream) {
	try {
		Reader in;
		if (enc==null)
		   in = new InputStreamReader(contentStream);
		else
		   in = new InputStreamReader(contentStream, enc);
		int chunkSize = contentStream.available();
		StringBuffer buffer = new StringBuffer(chunkSize);
		char[] readBuffer = new char[chunkSize];
		int n = in.read(readBuffer);
		while (n > 0) {
			buffer.append(readBuffer);
			n = in.read(readBuffer);
		}
		in.close();
		document.set(buffer.toString());

	} catch (IOException e) {
		TapestryPlugin.getDefault().logException(e);
	}
}
	public long getSynchronizationStamp(Object element) {
		return 0;
	}

	public long getModificationStamp(Object element) {
		return 0;
	}
	
	public boolean isDeleted(Object element) {
		return false;
	}
}
