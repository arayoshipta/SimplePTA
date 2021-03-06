/**
 * 
 */
package spta;

import ij.plugin.frame.*;
import ij.process.*;
import spta.gui.*;
import spta.data.*;

import java.awt.Font;
import java.awt.Frame;
import java.awt.geom.GeneralPath;
import java.util.ArrayList;
import java.util.List;

import ij.*;
import ij.gui.GenericDialog;
import ij.gui.ImageCanvas;
import ij.gui.Overlay;
import ij.gui.Roi;
import ij.gui.ShapeRoi;
import ij.gui.TextRoi;
import ij.measure.Calibration;

/**
 * @author araiyoshiyuki
 *
 */
public class SimplePTA extends PlugInFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static Frame frame = null;
	private static ImagePlus imp;
	private static ImageCanvas ic;
	private static Calibration cal;
	public static MainWindow mw;
	public static List<List<TrackPoint>> tracklist;
	public static int roisize = 12;
	public static int searchrange = 12;
	private static ResultDataTable rdt;
	private static ChartFrame cframe;
	public static ImageListener listener;
	public static icMouseAdapter ima;
	public static int[] selectedlist;
	public static boolean isTracking = false;
	public static Integer lowersize = 5;

	public SimplePTA() {
		super("PTA");
		// VersionCheck
		if (IJ.versionLessThan("1.43g"))
			return;
		if (frame != null){
			IJ.error("SimplePTA is already implemented");
			return;
		}
		frame = this;

		imp = WindowManager.getCurrentImage();
		
		// if the image is already opened
		if (imp != null) {
			// open the Threshold Adjuster
			ThresholdAdjuster ta = new ThresholdAdjuster();
			ta.setEnabled(true);
			IJ.setAutoThreshold(imp, "Triangle");
			ta.imageUpdated(imp);
			
			ImageProcessor ip = imp.getProcessor();
			mw = new MainWindow(imp, tracklist);
			mw.setVisible(true);
			tracklist = new ArrayList<List<TrackPoint>>(100);  // for data storage

			//to avoid automatic point detection
			//ip.setThreshold(-808080.0D, ip.getMax(), ImageProcessor.RED_LUT);
			ic = imp.getCanvas();
			cal = imp.getCalibration();
			ic.addMouseListener(ima = new icMouseAdapter(imp, roisize, mw));
			WindowManager.addWindow(mw);
			ImagePlus.addImageListener(listener = new ImageListener() {

				@Override
				public void imageClosed(ImagePlus arg0) {
					// TODO Auto-generated method stub
					if (rdt != null) {
						GenericDialog gd = new GenericDialog("Close table");
						gd.addMessage("Close Result data table?");
						gd.enableYesNoCancel();
						gd.showDialog();
						if(gd.wasOKed()) {
							rdt.dispose();
							rdt = null;
							tracklist = null;
							if (cframe != null) {
								cframe.dispose();
								cframe = null;
							}
						} else {
							return;
						}
					}
					ic.removeMouseListener(ima);
				}

				@Override
				public void imageOpened(ImagePlus arg0) {
					imp = arg0;
					if (mw != null) {
						mw.setImp(arg0);
						roisize = (Integer)mw.roisize.getValue();
						searchrange = (Integer)mw.searchrange.getValue();
						lowersize = (Integer)mw.lowersize.getValue();
						if(rdt == null)
							tracklist = new ArrayList<List<TrackPoint>>(100);  // allocate only rdt is null
					}
					ic = imp.getCanvas();
					ic.addMouseListener(ima = new icMouseAdapter(imp, roisize, mw));
				}

				@Override
				public void imageUpdated(ImagePlus arg0) {
					if(selectedlist == null) {
						return;
					}					
					if(selectedlist.length < 1)
						return;
					if(rdt == null)
						return;
					if(arg0 != rdt.imp)
						return;
					imp = arg0;
					
					if (isTracking)
						return;
					
					Overlay tempol = imp.getOverlay();
					if (tempol == null)
						tempol = new Overlay();
					tempol.clear();
					int focusedlistlen = mw.isAllTrack()?tracklist.size():selectedlist.length;
					for(int slist = 0;slist < focusedlistlen;slist++) {
						List<TrackPoint> focusedlist;
						if(mw.isAllTrack())
							focusedlist = tracklist.get(slist);
						else
							focusedlist = tracklist.get(selectedlist[slist]);

						for(TrackPoint tp: focusedlist) {
							if (tp.frame == imp.getCurrentSlice()) {
								GeneralPath gp = new GeneralPath();
								if(mw.stateOfTrajectory) {
									for(int i = 0; i<focusedlist.size() - 1; i++) {
										TrackPoint fp = focusedlist.get(i);
										TrackPoint sp = focusedlist.get(i+1);
										gp.moveTo(fp.tx / cal.pixelWidth, fp.ty / cal.pixelHeight);
										gp.lineTo(sp.tx / cal.pixelWidth, sp.ty / cal.pixelHeight);
									}
								} else {
									for(int i = 0; i<focusedlist.indexOf(tp); i++) {
										gp.moveTo(focusedlist.get(i).tx / cal.pixelWidth, 
												focusedlist.get(i).ty / cal.pixelHeight);
										gp.lineTo(focusedlist.get(i + 1).tx / cal.pixelWidth, 
												focusedlist.get(i + 1).ty / cal.pixelHeight);
									}
								}
								ShapeRoi sr = new ShapeRoi(gp);
								sr.setStrokeColor(rdt.getDataofColor(tracklist.indexOf(focusedlist)));
								tempol.add(sr);
								if(mw.isNumTrack()) {
									Roi numroi;
									int rs = tp.roisize;
									numroi = new TextRoi((int)(tp.tx / cal.pixelWidth) + rs / 2 + 2, 
											(int)(tp.ty / cal.pixelHeight) - rs / 2, 
											String.valueOf(tracklist.indexOf(focusedlist)),
											new Font("SansSerif", Font.PLAIN, 10));
									numroi.setStrokeColor(rdt.getDataofColor(tracklist.indexOf(focusedlist)));
									tempol.add(numroi);
								}
								if(mw.isRoiTrack()) {
									Roi squareroi;
									int rs = tp.roisize;
									squareroi = new Roi((int)(tp.tx / cal.pixelWidth) - rs / 2,
											(int)(tp.ty / cal.pixelHeight) - rs / 2, rs, rs);
									squareroi.setStrokeColor(rdt.getDataofColor(tracklist.indexOf(focusedlist)));
									tempol.add(squareroi);
								}
							}
						}
					}
					imp.setOverlay(tempol);
					if (cframe != null) {
						cframe.setFrame(imp.getFrame()); // update crosshair in chartframe
						cframe.validate();
					}
				}
				
			});
		} else {
			IJ.error("There are no image.");
			frame = null;
		}
	}
	
	public static void setTrackList(List<List<TrackPoint>> tlist) {
		tracklist = tlist;
	}
	
	public static List<List<TrackPoint>> getTrackList() {
		return tracklist;
	}
	
	public static void updateRDT(ImagePlus simp, ResultDataTable srdt) {
		/*
		 * update Result Data Table
		 */
		imp = simp;
		ic = imp.getCanvas();
		rdt = srdt;
	}
	
	public static void setlist(List<List<TrackPoint>> tlist, int[] slist) {
		tracklist = tlist;
		selectedlist = slist;
	}

	public static ResultDataTable getRDT() {
		// TODO Auto-generated method stub
		return rdt;
	}
	
	public static ChartFrame getcframe() {
		return cframe;
	}
	
	public static void setcframe(ChartFrame cf) {
		cframe = cf;
	}
}
