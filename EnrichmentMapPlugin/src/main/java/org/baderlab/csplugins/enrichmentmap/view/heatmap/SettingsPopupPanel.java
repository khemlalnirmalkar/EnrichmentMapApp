package org.baderlab.csplugins.enrichmentmap.view.heatmap;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.SwingUtilities;

import org.baderlab.csplugins.enrichmentmap.view.heatmap.HeatMapParams.Distance;
import org.baderlab.csplugins.enrichmentmap.view.util.SwingUtil;
import org.cytoscape.util.swing.LookAndFeelUtil;

@SuppressWarnings("serial")

public class SettingsPopupPanel extends JPanel {
	
	private JRadioButton cosineRadio;
	private JRadioButton euclideanRadio;
	private JRadioButton pearsonRadio;
	
	private JCheckBox showValuesCheck;
	
	private Consumer<Boolean> showValuesConsumer;
	private Consumer<Distance> distanceConsumer;
	
	private ActionListener cosineListener;
	private ActionListener euclideanListener;
	private ActionListener pearsonListener;
	private ActionListener showValuesListener;
	
	
	public SettingsPopupPanel() {
		createContents();
		setOpaque(false);
	}
	

	public void setShowValuesConsumer(Consumer<Boolean> showValuesConsumer) {
		this.showValuesConsumer = showValuesConsumer;
	}
	
	public void setDistanceConsumer(Consumer<Distance> dmConsumer) {
		this.distanceConsumer = dmConsumer;
	}
	
	
	/**
	 * Cannot use JComboBox on a JPopupMenu because of a bug in swing: 
	 * http://bugs.java.com/bugdatabase/view_bug.do?bug_id=4799266
	 */
	private void createContents() {		
		JLabel distanceLabel = new JLabel("  Hierarchical Cluster - Distance Metric  ");
		cosineRadio = new JRadioButton("Cosine");
		euclideanRadio = new JRadioButton("Euclidean");
		pearsonRadio = new JRadioButton("Pearson Correlation");
		JPanel distanceRadioPanel = createButtonPanel(cosineRadio, euclideanRadio, pearsonRadio);
				
		showValuesCheck = new JCheckBox("Show Values");
		showValuesCheck.addActionListener(showValuesListener = e -> {
			if(showValuesConsumer != null) {
				showValuesConsumer.accept(showValuesCheck.isSelected());
			}
		});
		
		cosineRadio.addActionListener(cosineListener = dmListenerFor(Distance.COSINE));
		euclideanRadio.addActionListener(euclideanListener = dmListenerFor(Distance.EUCLIDEAN));
		pearsonRadio.addActionListener(pearsonListener = dmListenerFor(Distance.PEARSON));
		
		SwingUtil.makeSmall(distanceLabel, showValuesCheck);
		
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setAutoCreateContainerGaps(false);
		layout.setAutoCreateGaps(!LookAndFeelUtil.isAquaLAF());
		
		layout.setHorizontalGroup(layout.createParallelGroup()
			.addComponent(showValuesCheck)
			.addComponent(distanceLabel)
			.addComponent(distanceRadioPanel)
		);
		
		layout.setVerticalGroup(layout.createSequentialGroup()
			.addGap(5)
			.addComponent(showValuesCheck)
			.addGap(10)
			.addComponent(distanceLabel)
			.addComponent(distanceRadioPanel)
			.addGap(5)
		);
	}
	
	
	private <T> ActionListener dmListenerFor(Distance dm) {
		return e -> {
			if(distanceConsumer != null) {
				distanceConsumer.accept(dm);
			}
		};
	}
	
	
	public void update(HeatMapParams params) {
		cosineRadio.removeActionListener(cosineListener);
		euclideanRadio.removeActionListener(euclideanListener);
		pearsonRadio.removeActionListener(pearsonListener);
		showValuesCheck.removeActionListener(showValuesListener);
		
		switch(params.getDistanceMetric()) {
			case COSINE:    cosineRadio.setSelected(true);    break;
			case EUCLIDEAN: euclideanRadio.setSelected(true); break;
			case PEARSON:   pearsonRadio.setSelected(true);   break;
		}
		showValuesCheck.setSelected(params.isShowValues());
		
		cosineRadio.addActionListener(cosineListener);
		euclideanRadio.addActionListener(euclideanListener);
		pearsonRadio.addActionListener(pearsonListener);
		showValuesCheck.addActionListener(showValuesListener);
	}
	
	
	public Distance getDistance() {
		if(cosineRadio.isSelected())
			return Distance.COSINE;
		if(euclideanRadio.isSelected())
			return Distance.EUCLIDEAN;
		return Distance.PEARSON;
	}
	
	public boolean isShowValues() {
		return showValuesCheck.isSelected();
	}
	
	private static JPanel createButtonPanel(JRadioButton ... buttons) {
		JPanel panel = new JPanel(new GridLayout(buttons.length, 1));
		ButtonGroup group = new ButtonGroup();
		for(JRadioButton button : buttons) {
			panel.add(button);
			group.add(button);
			SwingUtil.makeSmall(button);
		}
		panel.setOpaque(false);
		return panel;
	}

	public void popup(Component parent) {
		JPopupMenu menu = new JPopupMenu();
		menu.add(this);
		menu.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if(SwingUtilities.isRightMouseButton(e)) {
					menu.setVisible(false);
				}
			}
		});
		menu.show(parent, 0, parent.getHeight());
	}
}