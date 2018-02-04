package de.hpi.bpt.scylla.GUI;

import java.awt.Dimension;
import java.io.File;
import java.util.Observable;
import java.util.Observer;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;

import de.hpi.bpt.scylla.GUI.GlobalConfigurationPane.GlobalConfigurationPane;
import de.hpi.bpt.scylla.GUI.SimulationConfigurationPane.SimulationConfigurationPane;

@SuppressWarnings("serial")
public class EditorTabTitlePanel extends JPanel implements Observer{
	
	private JTabbedPane parentPane;
	private EditorPane childPane;
	private JLabel labelTitle;
	

	/**
	 * Create the panel.
	 */
	public EditorTabTitlePanel(JTabbedPane parent, EditorPane child) {
		parentPane = parent;
		childPane = child;
		JLabel labelIcon = new JLabel();
		if(child instanceof GlobalConfigurationPane)labelIcon.setIcon(ScyllaGUI.ICON_GLOBALCONF);
		if(child instanceof SimulationConfigurationPane)labelIcon.setIcon(ScyllaGUI.ICON_SIMCONF);
		labelTitle = new JLabel();
		JButton buttonClose = new JButton();
		buttonClose.addActionListener((e)->{
			childPane.be_close();
		});
		buttonClose.setBorderPainted(false);
		buttonClose.setOpaque(false);
		buttonClose.setIcon(ScyllaGUI.resizeIcon(ScyllaGUI.ICON_CLOSE,labelTitle.getFont().getSize(),labelTitle.getFont().getSize()));
		setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		setOpaque(false);
		add(labelIcon);
		add(Box.createRigidArea(new Dimension((int) (5.0*ScyllaGUI.SCALE),0)));
		add(labelTitle);
		add(buttonClose);
		childPane.getTitleObservers().add(this);
	}
	
	private void setFile(File file) {
		if(file != null) {
			String saved = childPane.isSaved() ? "" : "*";
			//setToolTipText(file.getPath());
			labelTitle.setText(saved+file.getName());
		}else {
			labelTitle.setText("<no file>");
		}
	}
	
	private void closeTab() {
		parentPane.removeTabAt(parentPane.indexOfTabComponent(this));
	}
	//TODO: Notifications don't work

	@Override
	public void update(Observable o, Object arg) {
		if(arg == childPane) {
			closeTab();
			return;
		}
		setFile((File) arg);
	}
	
	

}
