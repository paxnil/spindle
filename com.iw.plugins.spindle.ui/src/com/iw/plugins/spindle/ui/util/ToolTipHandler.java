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
package com.iw.plugins.spindle.ui.util;

import org.eclipse.jface.text.AbstractHoverInformationControlManager;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.TextPresentation;
import org.eclipse.jface.text.DefaultInformationControl.IInformationPresenter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/** 
 * toolip handler for Trees, Tables & Toolbars!
 */

public class ToolTipHandler
{

    private TooltipController tipController;
    private boolean handlerEnabled = true;

    /**
     * Creates a new tooltip handler
     *
     * @param parent the parent Shell
     */
    public ToolTipHandler(Shell parent)
    {
        tipController = new TooltipController(new TooltipCreator());
    }

    public boolean isHandlerEnabled()
    {
        return handlerEnabled;
    }

    public void setHandlerEnabled(boolean flag)
    {
        this.handlerEnabled = flag;
    }

    protected String getToolTipText(Object object, Point widgetPosition)
    {
        if (object instanceof Widget)
        {
            return (String) ((Widget) object).getData("TIP_TEXT");
        }
        return null;
    }

    /**
     * Enables customized hover help for a specified control
     * 
     * @control the control on which to enable hoverhelp
     */
    public void activateHoverHelp(final Control control)
    {

        tipController.install(control);

    }

    public class TooltipCreator implements IInformationControlCreator
    {

        /**
         * @see org.eclipse.jface.text.IInformationControlCreator#createInformationControl(Shell)
         */
        public IInformationControl createInformationControl(Shell parent)
        {
            return new DefaultInformationControl(parent, new TooltipPresenter());
        }

    }

    class TooltipController extends AbstractHoverInformationControlManager
    {

        /**
        * Constructor for InformationController.
        * @param creator
        */
        public TooltipController(IInformationControlCreator creator)
        {
            super(creator);
            setAnchor(ANCHOR_TOP);
            setFallbackAnchors(new Anchor[] { ANCHOR_LEFT, ANCHOR_BOTTOM, ANCHOR_RIGHT });
            setSizeConstraints(50, 10, false, false);

        }

        /**
        * Hides the information control and stops the information control closer.
        */
        protected void hideInformationControl()
        {
            IInformationControl control = getInformationControl();
            if (control != null)
            {
                control.setVisible(false);
                this.setEnabled(true);
            }
        }

        /**
         * @see org.eclipse.jface.text.AbstractInformationControlManager#computeInformation()
         */
        protected void computeInformation()
        {

            if (!handlerEnabled)
            {
                return;
            }

            Point widgetPosition = getHoverEventLocation();
            int heightCue = -1;

            Widget widget = (Widget) getSubjectControl();

            if (widget instanceof ToolBar)
            {
                ToolBar w = (ToolBar) widget;
                widget = w.getItem(widgetPosition);
            }
            if (widget instanceof Table)
            {
                Table w = (Table) widget;
                heightCue = w.getItemHeight();
                widget = w.getItem(widgetPosition);
            }
            if (widget instanceof Tree)
            {
                Tree w = (Tree) widget;
                heightCue = w.getItemHeight();
                widget = w.getItem(widgetPosition);
            }

            String information = null;

            Rectangle area = null;
            if (widget instanceof Control)
            {

                area = ((Control) widget).getBounds();
                area.x = 0;
                area.y = 0;

            } else
            {

                area =
                    new Rectangle(widgetPosition.x - 16, widgetPosition.y - 16, 32, heightCue == -1 ? 32 : heightCue);

            }
            if (widget != null)
            {
                information = getToolTipText(widget, widgetPosition);
            }

            setInformation(information, area);

        }

    }

    public static class TooltipPresenter implements IInformationPresenter
    {

        /**
        * @see org.eclipse.jface.text.DefaultInformationControl.IInformationPresenter#updatePresentation(Display, String, TextPresentation, int, int)
        */
        public String updatePresentation(
            Display display,
            String hoverInfo,
            TextPresentation presentation,
            int maxWidth,
            int maxHeight)
        {

            if (hoverInfo == null)
                return null;

            int firstLineBreak = hoverInfo.indexOf("\n");
            firstLineBreak = (firstLineBreak == -1 ? hoverInfo.length() : firstLineBreak);

            presentation.addStyleRange(new StyleRange(0, hoverInfo.length(), null, null, SWT.BOLD));

            //      presentation.addStyleRange(new StyleRange(0, firstLineBreak, null, null, SWT.BOLD));

            return hoverInfo;

        }

    }

}