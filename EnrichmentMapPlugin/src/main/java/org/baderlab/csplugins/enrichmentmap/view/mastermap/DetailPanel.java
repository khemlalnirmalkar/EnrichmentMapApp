package org.baderlab.csplugins.enrichmentmap.view.mastermap;

import java.util.List;

import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.resolver.DataSetParameters;

public interface DetailPanel {

	JPanel getPanel();
	
	String getDisplayName();
	
	String getIcon();
	
	List<String> validateInput();
	
	default DataSetParameters createDataSetParameters() { return null; };
	
}
