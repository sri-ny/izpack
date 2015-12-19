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

package com.izforge.izpack.panels.pdflicence;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.installer.console.ConsolePanel;
import com.izforge.izpack.installer.panel.PanelView;
import com.izforge.izpack.panels.licence.AbstractLicenceConsolePanel;

/**
 * HTML Licence Panel console helper
 */
public class PDFLicenceConsolePanel extends AbstractLicenceConsolePanel {

	private static final Logger logger = Logger.getLogger(AbstractLicenceConsolePanel.class.getName());
	private static final String RESOURCE_NAME = "PDFLicencePanel.licence";
	private final URL licenceURL;

	/**
	 * Constructs an <tt>PDFLicenceConsolePanel</tt>.
	 *
	 * @param panel
	 *            the parent panel/view. May be {@code null}
	 * @param resources
	 *            the resources
	 */
	public PDFLicenceConsolePanel(PanelView<ConsolePanel> panel, Resources resources) {
		super(panel, resources);
		this.licenceURL = resources.getURL(RESOURCE_NAME);
	}

	/**
	 * Returns the text to display.
	 *
	 * @return the text. A <tt>null</tt> indicates failure
	 */
	@Override
	protected String getText() {
		try {
			PDFTextStripper stripper = new PDFTextStripper();
			return stripper.getText(PDDocument.load(licenceURL));
		} catch (IOException e) {
			logger.log(Level.WARNING, "No licence text for resource: " + RESOURCE_NAME);
			return null;
		}
	}
}
