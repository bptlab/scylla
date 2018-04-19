package de.hpi.bpt.scylla.GUI.fileDialog;

import java.awt.Component;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public interface FileDialog {

	public static final int APPROVE_OPTION = JFileChooser.APPROVE_OPTION;
	public static final int CANCEL_OPTION = JFileChooser.CANCEL_OPTION;
	public static final int ERROR_OPTION = JFileChooser.ERROR_OPTION;
	
	public void setDialogTitle(String s);
	public void addChoosableFileFilter(FileFilter filter);
	public void setFileFilter(FileFilter filter);
	public int showDialog(Component parent, String approveButtonText);
	
	public String[] getSelectedFilePaths();
	public default String getSelectedFilePath(){return getSelectedFilePaths()[0];}
	
	public static FileDialog request(String currentDirectoryPath) {
		return new WebFileChooser();
	}

}
