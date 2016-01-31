/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
 *
 * Copyright 2002 Jan Blok
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.izforge.izpack.panels.htmllicence;

import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.licence.AbstractLicenceConsolePanel;

import java.util.logging.Logger;

/**
 * HTML Licence Panel console helper
 */
public class HTMLLicenceConsolePanel extends AbstractLicenceConsolePanel
{
    /**
     * The logger.
     */
    private static final Logger logger = Logger.getLogger(HTMLLicenceConsolePanel.class.getName());

    private static final String DEFAULT_SUFFIX = ".licence";

    /**
     * Constructs an <tt>HTMLLicenceConsolePanel</tt>.
     *
     * @param panel     the parent panel/view. May be {@code null}
     * @param resources the resources
     */
    public HTMLLicenceConsolePanel(PanelView<ConsolePanel> panel, Resources resources)
    {
        super(panel, resources);
    }

    /**
     * Returns the text to display.
     *
     * @return the text. A <tt>null</tt> indicates failure
     */
    @Override
    protected String getText()
    {
        final String resNamePrefix = HTMLLicencePanel.class.getSimpleName();
        String text = null;

        Panel panel = getPanel();
        if (panel != null)
        {
            String panelId = panel.getPanelId();
            if (panelId != null)
            {
                String panelSpecificResName = resNamePrefix + '.' + panelId;
                text = getText(panelSpecificResName);
                if (text == null)
                {
                    text = getText(resNamePrefix + DEFAULT_SUFFIX);
                }
            }
        }

        if (text != null)
        {
            text = removeHTML(text);
        }
        else
        {
            logger.warning("Cannot open any of both license text resources ("
                    + resNamePrefix + '.' + panel.getPanelId() + ", " + resNamePrefix + DEFAULT_SUFFIX
                    + ") for panel type '" + HTMLLicencePanel.class.getSimpleName() + "" );
        }
        return text;
    }

}
