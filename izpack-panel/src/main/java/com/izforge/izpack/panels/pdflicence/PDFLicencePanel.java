/*
 * IzPack - Copyright 2001-2008 Julien Ponge, All Rights Reserved.
 *
 * http://izpack.org/
 * http://izpack.codehaus.org/
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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.KeyStroke;

import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

import com.izforge.izpack.api.GuiId;
import com.izforge.izpack.api.data.Panel;
import com.izforge.izpack.api.resource.Resources;
import com.izforge.izpack.gui.IzPanelLayout;
import com.izforge.izpack.gui.LabelFactory;
import com.izforge.izpack.gui.log.Log;
import com.izforge.izpack.installer.data.GUIInstallData;
import com.izforge.izpack.installer.gui.InstallerFrame;
import com.izforge.izpack.installer.gui.IzPanel;

/**
 * The IzPack HTML license panel.
 *
 * @author Julien Ponge
 */
public class PDFLicencePanel extends IzPanel implements ActionListener {

	/**
	 *
	 */
	private static final long serialVersionUID = 3256728385458746416L;

	private static final float DEFAULT_ZOOM = 1.25F;

	/**
	 * The text area.
	 */
	// private JEditorPane textArea;
	private JPanel documentPanel = new JPanel();

	/**
	 * The radio buttons.
	 */
	private JRadioButton yesRadio;
	private JRadioButton noRadio;

	/**
	 * Constructs an <tt>PDFLicencePanel</tt>.
	 *
	 * @param panel
	 *            the panel
	 * @param parent
	 *            the parent window
	 * @param installData
	 *            the installation data
	 * @param resources
	 *            the resources
	 * @param log
	 *            the log
	 */
	public PDFLicencePanel(Panel panel, final InstallerFrame parent, GUIInstallData installData, Resources resources,
			Log log) {
		super(panel, parent, installData, new IzPanelLayout(log), resources);

		// We put our components
		add(LabelFactory.create(getString("LicencePanel.info"), parent.getIcons().get("history"), LEADING), NEXT_LINE);

		SwingController controller = new SwingController();
		SwingViewBuilder factory = new SwingViewBuilder(controller);
		JPanel viewerComponentPanel = factory.buildViewerPanel();
		ComponentKeyBinding.install(controller, viewerComponentPanel);

		// Open a PDF document to view
		controller.openDocument(loadLicence());

		controller.setZoom(getZoomFrom(installData));
		ActionListener fireDefault = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JButton defaultButton = parent.getRootPane().getDefaultButton();
				if (defaultButton != null && defaultButton.isEnabled()) {
					defaultButton.doClick();
				}
			}
		};
		documentPanel.registerKeyboardAction(fireDefault, null, KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				JComponent.WHEN_FOCUSED);
		add(controller.getDocumentViewController().getViewContainer(), NEXT_LINE);

		ButtonGroup group = new ButtonGroup();

		yesRadio = new JRadioButton(getString("LicencePanel.agree"), false);
		yesRadio.setName(GuiId.LICENCE_YES_RADIO.id);
		group.add(yesRadio);
		add(yesRadio, NEXT_LINE);
		yesRadio.addActionListener(this);

		noRadio = new JRadioButton(getString("LicencePanel.notagree"), true);
		noRadio.setName(GuiId.LICENCE_NO_RADIO.id);
		group.add(noRadio);
		add(noRadio, NEXT_LINE);
		noRadio.addActionListener(this);
		setInitialFocus(documentPanel);
		getLayoutHelper().completeLayout();

	}

	private float getZoomFrom(GUIInstallData installData) {
		try {
			return Float.valueOf(installData.getAttribute("PDFLicencePanel.zoom").toString());
		} catch (Exception e) {
			return DEFAULT_ZOOM;
		}
	}

	/**
	 * Loads the license text.
	 *
	 * @return The license text URL.
	 */
	protected URL loadLicence() {
		String resNamePrefix = "PDFLicencePanel";
		String resNameStr = resNamePrefix + ".licence";

		if (getMetadata() != null && getMetadata().getPanelId() != null) {
			try {
				String panelSpecificResName = resNamePrefix + '.' + this.getMetadata().getPanelId();
				String panelspecificResContent = getResources().getString(panelSpecificResName, null);
				if (panelspecificResContent != null) {
					resNameStr = panelSpecificResName;
				}
			} catch (Exception e) {
				// Those ones can be skipped
			}
		}

		try {
			return getResources().getURL(resNameStr);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return null;
	}

	/**
	 * Actions-handling method (here it launches the installation).
	 *
	 * @param e
	 *            The event.
	 */
	public void actionPerformed(ActionEvent e) {
		if (yesRadio.isSelected()) {
			parent.unlockNextButton();
		} else {
			parent.lockNextButton();
		}
	}

	/**
	 * Indicates wether the panel has been validated or not.
	 *
	 * @return true if the user agrees with the license, false otherwise.
	 */
	public boolean isValidated() {
		if (noRadio.isSelected()) {
			parent.exit();
			return false;
		}
		return (yesRadio.isSelected());
	}

	/**
	 * Called when the panel becomes active.
	 */
	public void panelActivate() {
		if (!yesRadio.isSelected()) {
			parent.lockNextButton();
		}
	}

}
