package com.izforge.izpack.panels.pdflicence;

import javax.swing.JPanel;

import org.icepdf.ri.common.ComponentKeyBinding;
import org.icepdf.ri.common.SwingController;
import org.icepdf.ri.common.SwingViewBuilder;

public class PDFDisplayer extends javax.swing.JFrame {

	private static final String FILE_NAME = "./src/test/resources/com/izforge/izpack/panels/panel/PDFLicencePanel.pdf";
	private static final long serialVersionUID = 1L;

	public PDFDisplayer() {
		initComponents();
	}

	private void initComponents() {

		
		setTitle("PDF Licence");
		addWindowListener(new java.awt.event.WindowAdapter() {
			@Override
			public void windowClosing(java.awt.event.WindowEvent evt) {
				exitApplication();
			}
		});

		SwingController controller = new SwingController();
		SwingViewBuilder factory = new SwingViewBuilder(controller);

		JPanel viewerComponentPanel = factory.buildViewerPanel();
		ComponentKeyBinding.install(controller, viewerComponentPanel);

		// Open a PDF document to view
		controller.openDocument(FILE_NAME);

		getContentPane().add(controller.getDocumentViewController().getViewContainer(), java.awt.BorderLayout.CENTER);
		controller.setZoom(1.3F);
		setSize(900, 900);
	}

	private void exitApplication() {
		this.setVisible(false);
		this.dispose();
	}

	/**
	 * @param args
	 *            the command line arguments
	 *
	 * @throws Exception
	 *             If anything goes wrong.
	 */
	public static void main(String[] args) throws Exception {
		PDFDisplayer viewer = new PDFDisplayer();
		viewer.setVisible(true);
	}

}
