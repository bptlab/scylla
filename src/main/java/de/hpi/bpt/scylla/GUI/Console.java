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
public class Console extends JTextPane{

	private PrintStream out;
	private PrintStream err;
	
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

	public PrintStream getOut() {
		return out;
	}

	private void setOut(PrintStream out) {
		this.out = out;
	}

	public PrintStream getErr() {
		return err;
	}

	private void setErr(PrintStream err) {
		this.err = err;
	}

}
