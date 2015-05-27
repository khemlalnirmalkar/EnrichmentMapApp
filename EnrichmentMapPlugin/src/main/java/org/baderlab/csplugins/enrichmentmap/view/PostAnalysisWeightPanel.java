package org.baderlab.csplugins.enrichmentmap.view;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;

import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters;
import org.baderlab.csplugins.enrichmentmap.PostAnalysisParameters.FilterMetric;
import org.baderlab.csplugins.enrichmentmap.model.DataSet;
import org.baderlab.csplugins.enrichmentmap.model.EnrichmentMap;
import org.baderlab.csplugins.enrichmentmap.model.GeneExpressionMatrix;
import org.baderlab.csplugins.enrichmentmap.model.Ranking;
import org.cytoscape.application.swing.CySwingApplication;

@SuppressWarnings("serial")
public class PostAnalysisWeightPanel extends CollapsiblePanel {

	private final CySwingApplication application;
	
	private PostAnalysisParameters paParams;
	private EnrichmentMap map;
	
	// Universe sizes
	private int universeGmt;
	private int universeExpression;
	private int universeIntersection;
	
    private JComboBox<String> datasetCombo;
	private JComboBox<String> rankingCombo;
	private JComboBox<FilterMetric> rankTestCombo;
	private JFormattedTextField rankTestTextField;
	
	private JRadioButton gmtRadioButton;
	private JRadioButton intersectionRadioButton;
	private JRadioButton expressionSetRadioButton;
	private JRadioButton userDefinedRadioButton;
    private JFormattedTextField universeSelectionTextField;
	
    private DefaultComboBoxModel<String> rankingModel;
    private DefaultComboBoxModel<String> datasetModel;
	
    
	public PostAnalysisWeightPanel(CySwingApplication application) {
		super("Edge Weight Calculation Parameters");
		this.application = application;
		createPanel();
	}
	
	
	private void createPanel() {
		JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        
        datasetCombo = new JComboBox<>();
        // Dataset model is already initialized
        datasetModel = new DefaultComboBoxModel<>();
        datasetCombo.setModel(datasetModel);
        datasetCombo.addActionListener(new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	String dataset = (String)datasetCombo.getSelectedItem();
            	if(dataset == null)
            		return;
            	paParams.setSignature_dataSet(dataset);
            	updateUniverseSize();
            }
        });
        panel.add(datasetCombo);
        
        rankingModel = new DefaultComboBoxModel<>();
        rankingCombo = new JComboBox<>();
        rankingCombo.setModel(rankingModel);
        rankingCombo.addActionListener( new ActionListener() {
            public void actionPerformed( ActionEvent e ) {
            	paParams.setSignature_rankFile((String)rankingCombo.getSelectedItem());
            }
        });
        panel.add(rankingCombo);
        
        DecimalFormat decFormat = new DecimalFormat();
        decFormat.setParseIntegerOnly(false);
        rankTestTextField = new JFormattedTextField(decFormat);
        rankTestTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        
        rankTestCombo = new JComboBox<>();
        rankTestCombo.addItem(FilterMetric.MANN_WHIT);
        rankTestCombo.addItem(FilterMetric.HYPERGEOM);
        rankTestCombo.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (FilterMetric.MANN_WHIT.equals(rankTestCombo.getSelectedItem())) {
                    paParams.setSignature_rankTest(FilterMetric.MANN_WHIT);
                    rankTestTextField.setValue(paParams.getSignature_Mann_Whit_Cutoff());
                } else if (FilterMetric.HYPERGEOM.equals(rankTestCombo.getSelectedItem())) {
                    paParams.setSignature_rankTest(FilterMetric.HYPERGEOM);
                    rankTestTextField.setValue(paParams.getSignature_Hypergeom_Cutoff());
                }
            }
        });
        panel.add(rankTestCombo);
        
        JPanel cutoffLabel = new JPanel();
        cutoffLabel.add(new JLabel("Select Cutoff:"));
        panel.add(cutoffLabel);
        
        JPanel cutoffPanel = new JPanel();
        cutoffPanel.setLayout(new BoxLayout(cutoffPanel, BoxLayout.X_AXIS));

        cutoffPanel.add(rankTestCombo);
        cutoffPanel.add(rankTestTextField);

        panel.add(cutoffPanel);
        
        // Create Universe selection panel
        CollapsiblePanel universeSelectionPanel = new CollapsiblePanel("Advanced Hypergeometric Universe");
        universeSelectionPanel.setCollapsed(true);
        universeSelectionPanel.getContentPane().setLayout(new BorderLayout());
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        c.weighty = 1;
        c.weightx = 1;
        c.insets = new Insets(0,0,0,0);
        c.fill = GridBagConstraints.HORIZONTAL;
        
        JPanel radioButtonsPanel = new JPanel();
        radioButtonsPanel.setLayout(gridbag);
        
    	gmtRadioButton = new JRadioButton();
        gmtRadioButton.setActionCommand("GMT");
        gmtRadioButton.addActionListener(new UniverseSelectActionListener());        
        gmtRadioButton.setSelected(true);
        
        expressionSetRadioButton = new JRadioButton();
        expressionSetRadioButton.setActionCommand("Expression Set");
        expressionSetRadioButton.addActionListener(new UniverseSelectActionListener());
        
        intersectionRadioButton = new JRadioButton();
        intersectionRadioButton.setActionCommand("Intersection");
        intersectionRadioButton.addActionListener(new UniverseSelectActionListener());
        
        userDefinedRadioButton = new JRadioButton("User Defined");
        userDefinedRadioButton.setActionCommand("User Defined");
        userDefinedRadioButton.addActionListener(new UniverseSelectActionListener());  
        
        ButtonGroup buttonGroup = new ButtonGroup();
        buttonGroup.add(gmtRadioButton);
        buttonGroup.add(expressionSetRadioButton);
        buttonGroup.add(intersectionRadioButton);
        buttonGroup.add(userDefinedRadioButton);

        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = GridBagConstraints.REMAINDER;
        gridbag.setConstraints(gmtRadioButton, c);
        radioButtonsPanel.add(gmtRadioButton);
        
        c.gridy = 1;
        gridbag.setConstraints(expressionSetRadioButton, c);
        radioButtonsPanel.add(expressionSetRadioButton);

        c.gridy = 2;
        gridbag.setConstraints(intersectionRadioButton, c);
        radioButtonsPanel.add(intersectionRadioButton);
        
        c.gridy = 3;
        c.gridwidth = 2;
        gridbag.setConstraints(userDefinedRadioButton, c);
        radioButtonsPanel.add(userDefinedRadioButton);
        
        c.gridx = 2;
        DecimalFormat intFormat = new DecimalFormat();
        intFormat.setParseIntegerOnly(true);
        universeSelectionTextField = new JFormattedTextField(intFormat);
        universeSelectionTextField.addPropertyChangeListener("value", new FormattedTextFieldAction());
        universeSelectionTextField.setEnabled(false);
        gridbag.setConstraints(universeSelectionTextField, c);
        radioButtonsPanel.add(universeSelectionTextField);
        
        universeSelectionPanel.getContentPane().add(radioButtonsPanel, BorderLayout.CENTER);
               
        panel.add(universeSelectionPanel);
        
        getContentPane().add(panel, BorderLayout.NORTH);
    }

	
	private class UniverseSelectActionListener implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			int size = 0;
			switch(e.getActionCommand()) {
				case "GMT":
					size = universeGmt;
		        	universeSelectionTextField.setEnabled(false);
		        	break;
				case "Expression Set":
		            size = universeExpression;
		            universeSelectionTextField.setEnabled(false);
		            break;
				case "Intersection":
					size = universeIntersection;
	            	universeSelectionTextField.setEnabled(false);
	            	break;
				case "User Defined":
					size = ((Number)universeSelectionTextField.getValue()).intValue();
		            universeSelectionTextField.setEnabled(true);
		            break;
			}
			paParams.setUniverseSize(size);
		}
	}
	
	
	private class FormattedTextFieldAction implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
        	JFormattedTextField source = (JFormattedTextField) e.getSource();
	        if (source == rankTestTextField) {
	        	Number val = (Number)rankTestTextField.getValue();
	        	if(val == null || val.doubleValue() < 0.0) {
	        		JOptionPane.showMessageDialog(application.getJFrame(), "Universe value must be greater than zero", "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
	        		universeSelectionTextField.setValue(val = 1);
	        	}
	        	
	            if (rankTestCombo.getSelectedItem().equals(FilterMetric.MANN_WHIT)) {
	        		paParams.setSignature_Mann_Whit_Cutoff(val.doubleValue());
	        	}
	        	if (rankTestCombo.getSelectedItem().equals(FilterMetric.HYPERGEOM)) {
	        		paParams.setSignature_Hypergeom_Cutoff(val.doubleValue());
	        	}
	        }
	        else if (source == universeSelectionTextField) {
	        	Number val = (Number)universeSelectionTextField.getValue();
	        	if(val == null || val.intValue() < 0) {
	        		JOptionPane.showMessageDialog(application.getJFrame(), "Universe value must be greater than zero", "Parameter out of bounds", JOptionPane.WARNING_MESSAGE);
	        		universeSelectionTextField.setValue(val = 1);
	        	}
	        	paParams.setUniverseSize(val.intValue());
	        }
        }
	}
	
	
	void resetPanel() {
		gmtRadioButton.setSelected(true);
        rankTestCombo.setSelectedItem(paParams.getDefault_signature_rankTest());
    }
    
    
    void updateContents(EnrichmentMap currentMap, PostAnalysisParameters paParams) {
    	this.map = currentMap;
		this.paParams = paParams;
		
		Map<String,DataSet> datasetMap = map.getDatasets();
        String[] datasetArray = datasetMap.keySet().toArray(new String[datasetMap.size()]);
        Arrays.sort(datasetArray);
        this.datasetModel.removeAllElements();
        for (String dataset : datasetArray) {
        	datasetModel.addElement(dataset);
        }
        
        Map<String,Ranking> rankingMap = map.getAllRanks();
        String[] rankingArray = rankingMap.keySet().toArray(new String[rankingMap.size()]);
        Arrays.sort(rankingArray);
        rankingModel.removeAllElements();
        for (String ranking : rankingArray) {
        	rankingModel.addElement(ranking);
        }
        
		updateUniverseSize();
        
        rankTestCombo.setSelectedItem(paParams.getDefault_signature_rankTest());
    }
    
    
    private void updateUniverseSize() {
    	String signature_dataSet = paParams.getSignature_dataSet();
    	GeneExpressionMatrix expressionSets = map.getDataset(signature_dataSet).getExpressionSets();
    	
    	universeGmt = map.getNumberOfGenes();
    	universeExpression = expressionSets.getExpressionUniverse();
    	universeIntersection = expressionSets.getExpressionMatrix().size();
    	
        gmtRadioButton.setText("GMT (" + universeGmt + ")");
        expressionSetRadioButton.setText("Expression Set (" + universeExpression + ")");
        intersectionRadioButton.setText("Intersection (" + universeIntersection + ")");
        
        universeSelectionTextField.setValue(universeExpression);
        
        if(gmtRadioButton.isSelected())
        	paParams.setUniverseSize(universeGmt);
        else if(expressionSetRadioButton.isSelected())
        	paParams.setUniverseSize(universeExpression);
        else if(intersectionRadioButton.isSelected())
        	paParams.setUniverseSize(universeIntersection);
        else
        	paParams.setUniverseSize((Integer)universeSelectionTextField.getValue());
    }
    
    
}
