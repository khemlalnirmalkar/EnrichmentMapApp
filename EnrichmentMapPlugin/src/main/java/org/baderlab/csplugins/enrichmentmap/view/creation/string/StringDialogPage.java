package org.baderlab.csplugins.enrichmentmap.view.creation.string;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogCallback;
import org.baderlab.csplugins.enrichmentmap.view.util.dialog.CardDialogPage;

public class StringDialogPage implements CardDialogPage {

	@Override
	public String getID() {
		return getClass().getSimpleName();
	}

	@Override
	public String getPageComboText() {
		return "Create Enrichment Map";
	}

	@Override
	public JPanel createBodyPanel(CardDialogCallback callback) {
		return new JPanel();
	}

	@Override
	public void finish() {

	}

}