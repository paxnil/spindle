package com.iw.plugins.spindle.parser.filters;

import org.apache.xerces.impl.XMLErrorReporter;

public class ErrorReportingFilter extends BaseDocumentFilter {

  private XMLErrorReporter errorReporter;

  public XMLErrorReporter getErrorHandler() {
    return errorReporter;
  }

  public void setErrorHandler(XMLErrorReporter errorHandler) {
    this.errorReporter = errorHandler;
  }

}
