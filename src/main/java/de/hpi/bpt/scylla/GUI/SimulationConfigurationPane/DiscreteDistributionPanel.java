package de.hpi.bpt.scylla.GUI.SimulationConfigurationPane;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.NumberFormat;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.NumberFormatter;

import de.hpi.bpt.scylla.GUI.FormManager;
import de.hpi.bpt.scylla.GUI.NoNegativeDoubleFormat;
import de.hpi.bpt.scylla.GUI.ScyllaGUI;
import de.hpi.bpt.scylla.creation.SimulationConfiguration.Distribution.DiscreteDistribution;

/**
 * Special {@link DistributionPanel} sub panel for discrete distributions
 * @author Leon Bein
 *
 */
@SuppressWarnings("serial")
public class DiscreteDistributionPanel extends JScrollPane {
	
	/**Displayed distribution type; doubles {@link DistributionPanel#distribution} to avoid multiple casts*/
	private DiscreteDistribution distribution;
	/**General simulation configuration form manager*/
	private FormManager formManager;
	
	/**Table model managing entry set entries*/
	private DefaultTableModel model;


	/**
	 * Constructor
	 * @param d : Discrete distribution to be displayed
	 * @param fm : Supervising form manager
	 */
	public DiscreteDistributionPanel(DiscreteDistribution d, FormManager fm) {
		setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		getVerticalScrollBar().setUnitIncrement(32);
		
		formManager = fm;
		
		//Create table/list with entries and their probabilities in %
		model = new DefaultTableModel(new Object[][]{}, new Object[]{"Value","Probability (in %)"});
		JTable list = new JTable(model);
		list.setFillsViewportHeight(true);
		list.setShowVerticalLines(false);
		list.setShowHorizontalLines(false);
		list.setShowGrid(false);
		list.setRowHeight(list.getFont().getSize());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setSurrendersFocusOnKeystroke(true);
		setViewportView(list);
		//Set value input as positive integer field
		{
			NumberFormatter formatter = new NumberFormatter(NumberFormat.getInstance());
		    formatter.setValueClass(Integer.class);
		    formatter.setMinimum(0);//No negative task durations allowed
		    formatter.setMaximum(Integer.MAX_VALUE);
		    formatter.setAllowsInvalid(false);
			JFormattedTextField intfield = new JFormattedTextField(formatter);
			intfield.setColumns(10);
			list.getColumn("Value").setCellEditor(new DefaultCellEditor(intfield));
		}
		//Set probability input as percent field
		{
			NumberFormatter formatter = new NumberFormatter(new NoNegativeDoubleFormat());
			formatter.setValueClass(Double.class);
			formatter.setMinimum(0.0);
			formatter.setMaximum(100.0);
			formatter.setAllowsInvalid(false);
			JFormattedTextField doublefield = new JFormattedTextField(formatter);
			doublefield.setColumns(10);
			list.getColumn("Probability (in %)").setCellEditor(new DefaultCellEditor(doublefield));
		}
		//Set table editing listener to value/frequency editing
		{
			model.addTableModelListener((TableModelEvent e) -> {
				if(e.getType() == TableModelEvent.UPDATE && !formManager.isChangeFlag()){
					int row = e.getFirstRow();
					int col = e.getColumn();
					String value = (String) model.getValueAt(row, col);
					if(col == 0){
						distribution.setValue(row, value);
					}else{
						distribution.setFrequency(row, Double.valueOf(value)/100.0);
					}
					formManager.setSaved(false);
				}
			});
		}
		
		JPanel panelSidebar = new JPanel();
		setRowHeaderView(panelSidebar);
		GridBagLayout gbl_panelSidebar = new GridBagLayout();
		panelSidebar.setLayout(gbl_panelSidebar);
		
		JButton buttonAdd = new JButton("");
		buttonAdd.setIcon(ScyllaGUI.ICON_PLUS);
		buttonAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int value = 0;
				double frequency = 0;
				model.addRow(new Object[]{value,frequency});
				distribution.addEntry(value, frequency);
			}
		});
		GridBagConstraints gbc_buttonAdd = new GridBagConstraints();
		gbc_buttonAdd.weighty = 1.0;
		gbc_buttonAdd.weightx = 1.0;
		gbc_buttonAdd.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_buttonAdd.fill = GridBagConstraints.BOTH;
		gbc_buttonAdd.gridx = 0;
		gbc_buttonAdd.gridy = 0;
		panelSidebar.add(buttonAdd, gbc_buttonAdd);
		
		JButton buttonRemove = new JButton("");
		buttonRemove.setIcon(ScyllaGUI.ICON_REMOVE);
		buttonRemove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int i = list.getSelectedRow();
				if(i >= 0){
					model.removeRow(i);
					distribution.removeEntry(i);
					formManager.setSaved(false);
				}
			}
		});
		GridBagConstraints gbc_buttonRemove = new GridBagConstraints();
		gbc_buttonRemove.weighty = 1.0;
		gbc_buttonRemove.weightx = 1.0;
		gbc_buttonRemove.insets = new Insets(ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET, ScyllaGUI.STDINSET);
		gbc_buttonRemove.fill = GridBagConstraints.BOTH;
		gbc_buttonRemove.gridx = 0;
		gbc_buttonRemove.gridy = 1;
		panelSidebar.add(buttonRemove, gbc_buttonRemove);
		
		setDistribution(d);

	}


	/**
	 * Sets this panel's distribution and imports all entries if existing
	 * @param d
	 */
	private void setDistribution(DiscreteDistribution d) {
		formManager.setChangeFlag(true);
		
		distribution = d;
		int size = d.getEntrySize();
		for(int i = 0; i < size; i++){
			model.addRow(new Object[]{d.getValue(i),d.getFrequency(i)*100.0});
		}
		
		formManager.setChangeFlag(false);
	}
	

}
