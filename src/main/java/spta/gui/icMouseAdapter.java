/**
 * 
 */
package spta.gui;

import java.awt.Color;
import java.awt.event.*;

import ij.*;
import ij.gui.*;
import ij.measure.Calibration;
import ij.measure.Measurements;
import ij.process.*;

/**
 * @author araiyoshiyuki
 *
 */
public class icMouseAdapter extends MouseAdapter implements Measurements {
	
	public ImagePlus imp;
	public double lt, ut; // lower and upper limit of Threshold values
	private MainWindow mw;
	
	public icMouseAdapter(ImagePlus imp, int roisize, MainWindow mw) {
		this.imp = imp;
		this.mw = mw;
	}
	
	public void mouseClicked(MouseEvent e) {
		ImageProcessor ip = imp.getProcessor();
		FloatProcessor fip = ip.convertToFloatProcessor();
		ImageCanvas ic = imp.getCanvas();
		Calibration cal = imp.getCalibration();
		
		Overlay ol = imp.getOverlay();
		if(ol == null) 
			ol = new Overlay();
		
		ol.clear();
		lt = Math.round(ip.getMinThreshold());
		if(imp.getBitDepth()!=32)
			ut = Math.round(ip.getMaxThreshold());
		else
			ut = 65535;
		
		double wdx = ic.offScreenX(e.getX()); // to change the xy-cooridnate if the image is zoomed.
		double wdy = ic.offScreenY(e.getY()); // e.getX,Y returns pixel coordinates
		double fval = Float.intBitsToFloat(fip.getPixel((int)wdx, (int)wdy));

		if(ip.getMinThreshold() != -808080.0D && lt <= fval && fval <= ut) { // Is Threshold set ?
			Wand wand = new Wand(ip);
			wand.autoOutline((int)wdx, (int)wdy, lt, ut); // wand get pixel coordinate
			Roi wandRoi = new PolygonRoi(wand.xpoints,wand.ypoints,wand.npoints,Roi.POLYGON);
			imp.setRoi(wandRoi);
			ImageStatistics is = imp.getStatistics(CENTROID+RECT);
			double calcx = is.xCentroid / cal.pixelWidth;
			double calcy = is.yCentroid / cal.pixelHeight;
			Line lr1 = new Line(calcx, calcy - 20, calcx, calcy + 20);
			Line lr2 = new Line(calcx - 20, calcy, calcx + 20, calcy);
			lr2.setStrokeWidth(2);
			lr1.setStrokeWidth(2);
			lr1.setStrokeColor(Color.yellow);
			lr2.setStrokeColor(Color.yellow);
			ol.add(lr1);
			ol.add(lr2);

			imp.setOverlay(ol);
			//imp.updateAndDraw();  // no need to updateAndDraw()
			}
	}
}
