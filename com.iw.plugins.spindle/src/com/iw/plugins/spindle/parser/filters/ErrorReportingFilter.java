package com.iw.plugins.spindle.parser.filters;

import org.apache.xerces.impl.XMLErrorReporter;

public class ErrorReportingFilter extends BaseDocumentFilter {

  protected XMLErrorReporter errorReporter;

  public XMLErrorReporter getErrorReporter() {
    return errorReporter;
  }

  public void setErrorReporter(XMLErrorReporter errorReporter) {
    this.errorReporter = errorReporter;
  }

}
