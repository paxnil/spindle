package com.iw.plugins.spindle.parser;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.Assert;

import com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo;

public class ParserStatus implements XMLEnityEventInfo, IStatus {

  static private IStatus[] empty = new IStatus[0];

  private int beginLineNumber;
  private int beginColumnNumber;
  private XMLEnityEventInfo info;
  private String plugin;
  private String message;
  private int severity;

  /**
   * Constructor for ParserWarning.
   */
  public ParserStatus(int severity, String plugin, String message, XMLEnityEventInfo info) {
    Assert.isTrue(severity >= OK && severity <= ERROR);
    this.severity = severity;
    this.plugin = plugin;
    this.message = message;
    this.info = info;
  }

  public ParserStatus(
    int severity,
    String plugin,
    String message,
    int beginLineNumber,
    int beginColumn) {
    Assert.isTrue(severity >= OK && severity <= ERROR);
    this.severity = severity;
    this.plugin = plugin;
    this.message = message;
    this.beginLineNumber = beginLineNumber;
    this.beginColumnNumber = beginColumnNumber;
  }

  /**
   * @see com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo#getBeginColumnNumber()
   */
  public int getBeginColumnNumber() {
    if (info != null) {
      return info.getBeginColumnNumber();
    }
    return beginColumnNumber;
  }

  /**
   * @see com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo#getBeginLineNumber()
   */
  public int getBeginLineNumber() {
    if (info != null) {
      return info.getBeginLineNumber();
    }
    return beginLineNumber;
  }

  /**
   * @see com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo#getEndColumnNumber()
   */
  public int getEndColumnNumber() {
    if (info != null) {
      return info.getEndColumnNumber();
    }
    return -1;
  }

  /**
   * @see com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo#getEndLineNumber()
   */
  public int getEndLineNumber() {
    if (info != null) {
      return info.getEndLineNumber();
    }
    return -1;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getChildren()
   */
  public IStatus[] getChildren() {
    return empty;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getCode()
   */
  public int getCode() {
    return 0;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getException()
   */
  public Throwable getException() {
    return null;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getMessage()
   */
  public String getMessage() {
    return message;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getPlugin()
   */
  public String getPlugin() {
    return plugin;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#getSeverity()
   */
  public int getSeverity() {
    return severity;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#isMultiStatus()
   */
  public boolean isMultiStatus() {
    return false;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#isOK()
   */
  public boolean isOK() {
    return severity == OK;
  }

  /**
   * @see org.eclipse.core.runtime.IStatus#matches(int)
   */
  public boolean matches(int severityMask) {
    return (severity & severityMask) != 0;
  }

}
