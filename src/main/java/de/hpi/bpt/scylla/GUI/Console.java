package de.hpi.bpt.scylla.GUI;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;

@SuppressWarnings("serial")
/**
 * Console pane that can print plain and error messages and can be used to reroute standard console output.
 * @author Leon Bein
 *
 */
public class Console extends JTextPane{

	/**Standard output stream*/
	private PrintStream out;
	/**Error output stream*/
	private PrintStream err;
	
	/**
	 * Constructor, initializes the print streams and their styles
	 */
	public Console() {
		Style styleErr = addStyle(null, null);
		StyleConstants.setForeground(styleErr, ScyllaGUI.ERRORFONT_COLOR);
		setOut(new PrintStream(new OutputStream(){
			@Override
			public void write(int c) throws IOException {
				Document d = getDocument();
				try {
					d.insertString(d.getLength(), String.valueOf((char)c), null);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				setCaretPosition(d.getLength());
			}
			
		}));

		setErr(new PrintStream(new OutputStream(){
			@Override
			public void write(int c) throws IOException {
				Document d = getDocument();
				try {
					d.insertString(d.getLength(), String.valueOf((char)c), styleErr);
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
				setCaretPosition(d.getLength());
			}
			
		}));
	}

	/** @return The standard output print stream. */
	public PrintStream getOut() {
		return out;
	}

	/** Sets the standard output print stream */
	private void setOut(PrintStream out) {
		this.out = out;
	}

	/**@return The error output print stream*/
	public PrintStream getErr() {
		return err;
	}

	/**Sets the error output print stream*/
	private void setErr(PrintStream err) {
		this.err = err;
	}

}
