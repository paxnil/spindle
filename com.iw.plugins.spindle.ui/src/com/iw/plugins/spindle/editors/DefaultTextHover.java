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
 * Geoffrey Longman.
 * Portions created by the Initial Developer are Copyright (C) 2001-2005
 * the Initial Developer. All Rights Reserved.
 *
 * Contributor(s):
 * 
 *  glongman@gmail.com
 *
 * ***** END LICENSE BLOCK ***** */

package com.iw.plugins.spindle.editors;

import java.text.MessageFormat;
import java.util.List;

import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextHoverExtension;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.ITypedRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommand;
import org.eclipse.ui.commands.ICommandManager;
import org.eclipse.ui.commands.IKeySequenceBinding;
import org.eclipse.ui.keys.KeySequence;

import com.iw.plugins.spindle.UIPlugin;
import com.iw.plugins.spindle.editors.assist.AbstractContentAssistProcessor;
import com.iw.plugins.spindle.ui.util.ToolTipHandler;

/**
 * Text Hover for Editor annotations
 * 
 * @author glongman@gmail.com
 */
public class DefaultTextHover implements ITextHover, ITextHoverExtension
{

    Editor fEditor;

    private ICommand fCommand;
    {
        ICommandManager commandManager = PlatformUI.getWorkbench().getCommandSupport()
                .getCommandManager();
        fCommand = commandManager.getCommand("com.iw.plugins.spindle.ui.editor.commands.show.info");
        if (!fCommand.isDefined())
            fCommand = null;
    }

    public DefaultTextHover(Editor editor)
    {
        fEditor = editor;
    }

    /*
     * Formats a message as HTML text.
     */
    private String formatMessage(String message)
    {
        return message;
    }

    /*
     * @see ITextHover#getHoverInfo(ITextViewer, IRegion)
     */
    public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion)
    {
        if (fEditor == null)
            return null;

        try
        {
            ITypedRegion typedRegion = textViewer.getDocument().getPartition(
                    hoverRegion.getOffset());

            ContentAssistant assistant = fEditor.getContentAssistant();
            if (assistant == null)
                return null;

            synchronized (fEditor)
            {
                AbstractContentAssistProcessor assister = (AbstractContentAssistProcessor) assistant
                        .getContentAssistProcessor(typedRegion.getType());
                if (assister != null)
                {
                    IContextInformation[] infos = assister.computeInformation(
                            textViewer,
                            hoverRegion.getOffset());
                    if (infos != null && infos.length > 0)
                        return infos[0].getInformationDisplayString();
                }
            }

        }
        catch (BadLocationException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.text.ITextHover#getHoverRegion(org.eclipse.jface.text.ITextViewer,
     *      int)
     */
    public IRegion getHoverRegion(ITextViewer textViewer, int offset)
    {
        return new Region(offset, 0);
    }

    public IInformationControlCreator getHoverControlCreator()
    {
        return new IInformationControlCreator()
        {
            public IInformationControl createInformationControl(Shell parent)
            {
                return new DefaultInformationControl(parent, SWT.NONE,
                        new ToolTipHandler.TooltipPresenter(), getStatusFieldMessage());
            }
        };
    }

    protected String getStatusFieldMessage()
    {

        KeySequence[] sequences = getKeySequences();
        if (sequences == null)
            return null;

        String keySequence = sequences[0].format();
        return MessageFormat
                .format(
                        UIPlugin.getResourceBundle().getString("Info_message"), new Object[] { keySequence == null ? "" : keySequence }); //$NON-NLS-1$
    }

    private KeySequence[] getKeySequences()
    {
        if (fCommand != null)
        {
            List list = fCommand.getKeySequenceBindings();
            if (!list.isEmpty())
            {
                KeySequence[] keySequences = new KeySequence[list.size()];
                for (int i = 0; i < keySequences.length; i++)
                {
                    keySequences[i] = ((IKeySequenceBinding) list.get(i)).getKeySequence();
                }
                return keySequences;
            }
        }
        return null;
    }

}