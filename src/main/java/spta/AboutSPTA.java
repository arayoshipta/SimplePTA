package spta;
import ij.plugin.PlugIn;
import ij.IJ;
import ij.gui.*;


public class AboutSPTA implements PlugIn {

	public void run(String arg0) {
		String title = "About...";
		String message = "Simple Particle Track and Analysis (Simple PTA) Version 0.9.0\n" +
				"developed by Masahiro Nishiyama and Yoshiyuki Arai\n" +
				"Citation: Nishiyama M, Arai Y., Tracking the Movement of a Single Prokaryotic Cell in Extreme Environmental Conditions." +
				"\nMethods in Molecular Biology, Vol. 1593, 2017";
		new MessageDialog(null, title, message);
	}

}
