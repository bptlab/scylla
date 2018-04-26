package de.hpi.bpt.scylla.GUI.fileDialog;

import java.awt.Component;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Semaphore;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.hpi.bpt.scylla.GUI.WebSwing.WebSwingUtils;

public class WebFileChooser implements FileDialog{
	
	private String title;
	private MailSlot mailSlot;
	private String[] selectedFilePaths;
	private Semaphore semaphore;
	private Set<FileNameExtensionFilter> fileExtensionFilters;
	

	public WebFileChooser() {
		mailSlot = new MailSlot();
		semaphore = new Semaphore(0);
		fileExtensionFilters = new HashSet<FileNameExtensionFilter>();
	}
	
	public int showDialog() {
		WebSwingUtils.JSGLOBAL.call("openFileDialog",mailSlot,getFileExtensionFilters());
		try {
			semaphore.acquire();
		} catch (InterruptedException e) {
			e.printStackTrace();
			return ERROR_OPTION;
		}
		if(getSelectedFilePaths() == null || getSelectedFilePaths().length == 0)return CANCEL_OPTION;
		return APPROVE_OPTION;
	}
	
	public String[] getSelectedFilePaths() {
		return selectedFilePaths;
	}
	
	
	public class MailSlot implements Consumer<String>{

		@Override
		public void accept(String t) {
			//Empty string should give no results but would give String[]#{""}
			selectedFilePaths = !t.isEmpty() ? t.split(Pattern.quote("|")) : new String[0];
			semaphore.release();
		}
		
	}
	
	
	private String[][] getFileExtensionFilters(){
		FileNameExtensionFilter[] filters = new FileNameExtensionFilter[fileExtensionFilters.size()];
		fileExtensionFilters.toArray(filters);
		String[][] extensions = new String[filters.length][];
		for(int i = 0; i < filters.length; i++) {
			extensions[i] = filters[i].getExtensions();
		}
		return extensions;
	}
	
	
	
	/////////////////////Interface////////////////////////
	
	public void setDialogTitle(String s) {
		title = s;
	}

	@Override
	public void addChoosableFileFilter(FileFilter filter) {
		if(filter instanceof FileNameExtensionFilter) {
			fileExtensionFilters.add((FileNameExtensionFilter) filter);
		}
	}

	@Override
	public void setFileFilter(FileFilter filter) {
		//ignore
	}

	@Override
	public int showDialog(Component parent, String approveButtonText) {
		return showDialog();
	}

}
