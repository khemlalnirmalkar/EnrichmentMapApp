package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.util.concurrent.FutureTask;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.baderlab.csplugins.enrichmentmap.EnrichmentMapManager;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisVisualStyle.EdgeWidthParams;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.equations.EquationCompiler;
import org.cytoscape.model.CyNetwork;

@SuppressWarnings("serial")
public class EdgeWidthDialog extends JDialog {

	private final EquationCompiler equationCompiler;
	private final CyNetwork network;
	private final double similarityCutoff;
	private final String prefix;
	
	private JFormattedTextField emLowerWidthText;
	private JFormattedTextField emUpperWidthText;
	private JFormattedTextField lessThan100Text;
	private JFormattedTextField lessThan10Text;
	private JFormattedTextField greaterThanText;
	
	
	public EdgeWidthDialog(CySwingApplication application, CyApplicationManager applicationManager, EnrichmentMapManager enrichmentMapManager, EquationCompiler equationCompiler) {
		super(application.getJFrame(), true);
		this.network = applicationManager.getCurrentNetwork();
		this.equationCompiler = equationCompiler;
		
		EnrichmentMap map = enrichmentMapManager.getMap(network.getSUID());
		this.similarityCutoff = map.getParams().getSimilarityCutOff();
		this.prefix = map.getParams().getAttributePrefix();
		
		setTitle("EnrichmentMap Edge Width");
		setResizable(false);
		
		createContents();
		setTextFieldValues(EdgeWidthParams.restore(network));
		
		pack();
		setMinimumSize(getPreferredSize());
	}

	
	private void createContents() {
		JPanel parent = new JPanel();
		parent.setLayout(new BorderLayout());
		parent.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		add(parent);
		
		String networkName = network.getRow(network).get(CyNetwork.NAME, String.class);
		JLabel networkLabel = new JLabel("Network: " + networkName);
		networkLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
		
		JPanel genesetOverlapPanel = createGenesetOverlapPanel();
		JPanel signatureSetPanel   = createSignatureSetPanel();
		
		JPanel widthPanel = new JPanel();
		widthPanel.setLayout(new BoxLayout(widthPanel, BoxLayout.Y_AXIS));
		widthPanel.add(genesetOverlapPanel);
		widthPanel.add(Box.createVerticalStrut(5));
		widthPanel.add(signatureSetPanel);
		
		JPanel buttonPanel = createButtonPanel();
		
		parent.add(networkLabel, BorderLayout.NORTH);
		parent.add(widthPanel, BorderLayout.CENTER);
		parent.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	private static GridBagConstraints gridBagConstraints(int gridx, int gridy) {
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.WEST;
		gbc.gridx = gridx;
		gbc.gridy = gridy;
		gbc.insets = new Insets(5, 5, 0, 0);
		return gbc;
	}
	
	private JPanel createGenesetOverlapPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Geneset Overlap"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		
		
		// Similarity Coefficient
		JLabel similarityLabel = new JLabel("Similarity Coefficient     ");
		gbc = gridBagConstraints(0, 0);
		gbc.weightx = 1.0;
		panel.add(similarityLabel, gbc);
		
		JLabel lowerBound = new JLabel(String.format("%.1f", similarityCutoff));
		gbc = gridBagConstraints(1, 0);
		panel.add(lowerBound, gbc);
		
		JLabel upperBound = new JLabel("1.0"); // hardcoded
		gbc = gridBagConstraints(2, 0);
		panel.add(upperBound, gbc);
		
		JLabel spacer = new JLabel("");
		gbc = gridBagConstraints(3, 0);
		panel.add(spacer, gbc);
		
		
		// Width
		JLabel widthLabel = new JLabel("Width");
		gbc = gridBagConstraints(0, 1);
		panel.add(widthLabel, gbc);
		
		emLowerWidthText = new JFormattedTextField(new DecimalFormat());
		emLowerWidthText.setPreferredSize(new Dimension(50, emLowerWidthText.getPreferredSize().height));
		gbc = gridBagConstraints(1, 1);
		gbc.insets = new Insets(5, 5, 5, 0);
		panel.add(emLowerWidthText, gbc);
		
		emUpperWidthText = new JFormattedTextField(new DecimalFormat());
		emUpperWidthText.setPreferredSize(new Dimension(50, emUpperWidthText.getPreferredSize().height));
		gbc = gridBagConstraints(2, 1);
		gbc.insets = new Insets(5, 5, 5, 0);
		panel.add(emUpperWidthText, gbc);
		
		JLabel mappingText = new JLabel("(continuous mapping) ");
		gbc = gridBagConstraints(3, 1);
		panel.add(mappingText, gbc);
		
		return panel;
	}
	
	
	private JPanel createSignatureSetPanel() {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createTitledBorder("Signature Set (Post Analysis)"));
		panel.setLayout(new GridBagLayout());
		GridBagConstraints gbc;
		
		JLabel pValueLabel = new JLabel("p-value   ");
		gbc = gridBagConstraints(0, 0);
		gbc.weightx = 1.0;
		panel.add(pValueLabel, gbc);
		
		JLabel lessThan100 = new JLabel("<= cutoff/100     ");
		gbc = gridBagConstraints(1, 0);
		panel.add(lessThan100, gbc);
		
		JLabel lessThan10 = new JLabel("<= cutoff/10     ");
		gbc = gridBagConstraints(2, 0);
		panel.add(lessThan10, gbc);
		
		JLabel greaterThan = new JLabel("> cutoff/10     ");
		gbc = gridBagConstraints(3, 0);
		panel.add(greaterThan, gbc);
		
		
		// Width
		JLabel widthLabel = new JLabel("Width");
		gbc = gridBagConstraints(0, 1);
		panel.add(widthLabel, gbc);
		
		lessThan100Text = new JFormattedTextField(new DecimalFormat());
		lessThan100Text.setPreferredSize(new Dimension(50, lessThan100Text.getPreferredSize().height));
		gbc = gridBagConstraints(1, 1);
		gbc.insets = new Insets(5, 5, 5, 0);
		panel.add(lessThan100Text, gbc);
		
		lessThan10Text = new JFormattedTextField(new DecimalFormat());
		lessThan10Text.setPreferredSize(new Dimension(50, lessThan10Text.getPreferredSize().height));
		gbc = gridBagConstraints(2, 1);
		gbc.insets = new Insets(5, 5, 5, 0);
		panel.add(lessThan10Text, gbc);
		
		greaterThanText = new JFormattedTextField(new DecimalFormat());
		greaterThanText.setPreferredSize(new Dimension(50, greaterThanText.getPreferredSize().height));
		gbc = gridBagConstraints(3, 1);
		gbc.insets = new Insets(5, 5, 5, 0);
		panel.add(greaterThanText, gbc);
		
		return panel;
	}
	
	
	private void setTextFieldValues(EdgeWidthParams widthParams) {
		emUpperWidthText.setValue(widthParams.em_upper);
		emLowerWidthText.setValue(widthParams.em_lower);
		lessThan100Text.setValue(widthParams.pa_lessThan100);
		lessThan10Text.setValue(widthParams.pa_lessThan10);
		greaterThanText.setValue(widthParams.pa_greater);
	}
	
	
	private JPanel createButtonPanel() {
		JPanel parentPanel = new JPanel(new BorderLayout());
		JPanel defautsPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.TRAILING));
		
		JButton restoreDefaultsButton = new JButton("Restore Defaults");
		restoreDefaultsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setTextFieldValues(EdgeWidthParams.defaultValues());
			}
		});
		defautsPanel.add(restoreDefaultsButton);
		
		
		JButton cancelButton = new JButton("Cancel");
		buttonPanel.add(cancelButton);
		
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		JButton createButton = new JButton("OK");
		buttonPanel.add(createButton);
		
		createButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				double emLowerWidth = ((Number)emLowerWidthText.getValue()).doubleValue();
				double emUpperWidth = ((Number)emUpperWidthText.getValue()).doubleValue();
				double lessThan100  = ((Number)lessThan100Text.getValue()).doubleValue();
				double lessThan10   = ((Number)lessThan10Text.getValue()).doubleValue();
				double greaterThan  = ((Number)greaterThanText.getValue()).doubleValue();
				
				EdgeWidthParams params = new EdgeWidthParams(emLowerWidth, emUpperWidth, lessThan100, lessThan10, greaterThan);
				params.save(network);
				
				// Forces the view to update
				new FutureTask<>(new Runnable() {
					public void run() {
						PostAnalysisVisualStyle.applyWidthEquation(equationCompiler, prefix, network);
					}
				}, null).run();
				
				dispose();
			}
		});
		
		parentPanel.add(defautsPanel, BorderLayout.WEST);
		parentPanel.add(buttonPanel, BorderLayout.EAST);
		return parentPanel;
	}
	
}
