package com.iw.plugins.spindle.parser;

import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.MessageFormatter;
import org.apache.xerces.xni.XMLLocator;
import org.apache.xerces.xni.XNIException;
import org.apache.xerces.xni.parser.XMLComponentManager;
import org.apache.xerces.xni.parser.XMLConfigurationException;
import org.eclipse.core.runtime.IStatus;

import com.iw.plugins.spindle.TapestryPlugin;
import com.iw.plugins.spindle.parser.xml.XMLEnityEventInfo;
import com.iw.plugins.spindle.util.SpindleMultiStatus;

/**
 * @author Administrator
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class TapestryErrorReporter extends XMLErrorReporter {

  SpindleMultiStatus status;

  public IStatus getStatus() {
    return status;
  }

  /**
   * @see org.apache.xerces.xni.parser.XMLComponent#reset(XMLComponentManager)
   */
  public void reset(XMLComponentManager componentManager) throws XMLConfigurationException {
    super.reset(componentManager);
    status = new SpindleMultiStatus();
  }

  public void reportTapestryError(String message, XMLEnityEventInfo eventInfo) {
    status.addStatus(
      new ParserStatus(ParserStatus.WARNING, TapestryPlugin.ID_PLUGIN, message, eventInfo));

  }

  /**
   * @see org.apache.xerces.impl.XMLErrorReporter#reportError(XMLLocator, String, String, Object[], short)
   */
  public void reportError(
    XMLLocator location,
    String domain,
    String key,
    Object[] arguments,
    short severity)
    throws XNIException {
    if (severity == XMLErrorReporter.SEVERITY_FATAL_ERROR) {
      super.reportError(location, domain, key, arguments, severity);
    }
    MessageFormatter messageFormatter = getMessageFormatter(domain);
    String message;
    if (messageFormatter != null) {
      message = messageFormatter.formatMessage(fLocale, key, arguments);
    } else {
      StringBuffer str = new StringBuffer();
      str.append(domain);
      str.append('#');
      str.append(key);
      int argCount = arguments != null ? arguments.length : 0;
      if (argCount > 0) {
        str.append('?');
        for (int i = 0; i < argCount; i++) {
          str.append(arguments[i]);
          if (i < argCount - 1) {
            str.append('&');
          }
        }
      }
      message = str.toString();
    }

    int new_severity = ParserStatus.ERROR;
    if (severity == XMLErrorReporter.SEVERITY_WARNING) {
      new_severity = ParserStatus.WARNING;
    }
    status.addStatus(
      new ParserStatus(
        new_severity,
        TapestryPlugin.ID_PLUGIN,
        message,
        location.getLineNumber(),
        location.getColumnNumber()));

  }

}