package javatest.awt;

import com.jtransc.widgets.JTranscWidgets;

import javax.swing.*;

public class AWTTest {
	static public void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Throwable e) {
			e.printStackTrace();
		}

		JTranscWidgets.impl = new JTranscWidgets();
		//System.setProperty("java.awt.headless", "true");
		//Toolkit.
		JFrame frame = new JFrame("HelloWorldSwing");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		//Add the ubiquitous "Hello World" label.
		JLabel label = new JLabel("Hello World");
		frame.getContentPane().add(label);
		frame.pack();
		frame.setVisible(true);
		frame.dispose();
	}
}
