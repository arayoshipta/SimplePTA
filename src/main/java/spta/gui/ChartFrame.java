/**
 * 
 */
package spta.gui;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;

import ij.IJ;
import ij.ImagePlus;
import ij.measure.Calibration;
import spta.data.AnalyzeTrack;
import spta.data.TrackPoint;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;

/**
 * @author araiyoshiyuki
 *
 */
public class ChartFrame extends JFrame {
	private ImagePlus imp;
	private Calibration cal;
	public ChartPanel xypanel, intpanel, sqdpanel, velpanel, areapanel, costpanel;
	public XYPlot xyXYPlot, xyIntPlot, xyAreaPlot, xyCosTPlot, xyVelPlot;
	public ChartFrame chartframe;
	public List<TrackPoint> track;
	
	private double[] x, y, intensities, timeseries, timeseriesm, timeseriesc, objarea, cost, velocities, sqd, sqdlag;
	
	public ChartFrame(ImagePlus imp, List<TrackPoint> track) {
		this.imp = imp;
		this.cal = imp.getCalibration();
		this.track = track;
				
		getContentPane().setLayout(new GridLayout(3, 2, 0, 0));
		// (0,0) = xy-chart, (0,1) = Intensity, 
		// (1,0) = sqd, (1,1) = Velocity
		// (2,0) = area, (2,1) = cost
		setBounds(500, 10, 500, 500);
		setTitle("Trajectories");
		chartframe = this;
		drawTrajectory(track);
	}

	public void setData(ImagePlus imp, List<TrackPoint> track) {
		AnalyzeTrack at = new AnalyzeTrack(imp, track);
		this.x = at.xtrack;
		this.y = at.ytrack;
		this.intensities = at.intensities;
		this.objarea = at.objarea;
		this.timeseries = at.timeseries;
		this.timeseriesm = at.timeseriesm;
		this.timeseriesc = at.timeseriesc;
		this.cost = at.cost;
		this.velocities = at.velocities;
		this.sqd = at.sqd;
		this.sqdlag = at.sqdlag;
	}
	
	public void drawTrajectory(List<TrackPoint> track2) {
		setData(imp, track2);
		
		DefaultXYDataset xyset = new DefaultXYDataset();
		DefaultXYDataset intset = new DefaultXYDataset();
		DefaultXYDataset sqdset = new DefaultXYDataset();
		DefaultXYDataset velset = new DefaultXYDataset();
		DefaultXYDataset areaset = new DefaultXYDataset();
		DefaultXYDataset costset = new DefaultXYDataset();

		xyset.addSeries("xy", new double[][]{x, y});
		intset.addSeries("Intensities", new double[][]{timeseries, intensities});
		sqdset.addSeries("sqd", new double[][]{sqdlag, sqd});
		velset.addSeries("Velocities", new double[][]{timeseriesm, velocities});
		areaset.addSeries("Area", new double[][]{timeseries, objarea});
		costset.addSeries("Cos-Theta", new double[][]{timeseriesc, cost});
		
		JFreeChart xychart =
				ChartFactory.createXYLineChart("x-y trajectory", "x ["+cal.getUnit()+"]", 
						"y ["+cal.getUnit()+"]", xyset, PlotOrientation.VERTICAL, false, true, false);
		JFreeChart intchart = 
				ChartFactory.createXYLineChart("Intensity", cal.getTimeUnit(), "Intensity",
						intset, PlotOrientation.VERTICAL, false, true, false);
		JFreeChart sqdchart = 
				ChartFactory.createXYLineChart("Square Disp.", "delta-x [" + cal.getTimeUnit() + "]",
						"Square Disp. [" + cal.getUnit() + "^2]", sqdset, PlotOrientation.VERTICAL,
						false, true, false);
		JFreeChart velchart = 
				ChartFactory.createXYLineChart("Velocity", cal.getTimeUnit(), "Velocity [" + cal.getUnit() + " / " + cal.getTimeUnit() + "]",
						velset, PlotOrientation.VERTICAL, false, true, false);
		JFreeChart areachart = 
				ChartFactory.createXYLineChart("Area", cal.getTimeUnit(), "Area [" + cal.getUnit() + "^2]",
						areaset, PlotOrientation.VERTICAL, false, true, false);				
		JFreeChart costchart =
				ChartFactory.createXYLineChart("Cos-Theta", cal.getTimeUnit(), "Cos-T",
						costset, PlotOrientation.VERTICAL, false, true, false);
		if(xypanel == null) {
			xypanel = new ChartPanel(xychart);
			intpanel = new ChartPanel(intchart);
			sqdpanel = new ChartPanel(sqdchart);
			velpanel = new ChartPanel(velchart);
			areapanel = new ChartPanel(areachart);
			costpanel = new ChartPanel(costchart);
			this.getContentPane().add(xypanel);
			this.getContentPane().add(intpanel);
			this.getContentPane().add(sqdpanel);
			this.getContentPane().add(velpanel);
			this.getContentPane().add(areapanel);
			this.getContentPane().add(costpanel);
		} else {
			xypanel.setChart(xychart);
			intpanel.setChart(intchart);
			sqdpanel.setChart(sqdchart);
			velpanel.setChart(velchart);
			areapanel.setChart(areachart);
			costpanel.setChart(costchart);
			validate();
		}
		
		
		xyXYPlot = configXYchart(xychart);
		xyXYPlot.getRangeAxis().setInverted(true);
		xyIntPlot = configXYchart(intchart);
		xyAreaPlot = configXYchart(areachart);
		xyCosTPlot = configXYchart(costchart);
		xyVelPlot = configXYchart(velchart);
		 
		
		imp.setSlice(track.get(0).frame);
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public void setFrame(int setframe) {
		if (timeseries == null) return;
		int index;
		/*
		 * domain and range crosshair is changed depending on the frame
		 */
		double frameint = cal.frameInterval == 0?1:cal.frameInterval;
		index = Arrays.binarySearch(timeseriesc, setframe * frameint);
		if(index > 0) {
			xyXYPlot.setDomainCrosshairValue(x[index]);
			xyXYPlot.setRangeCrosshairValue(y[index]);
			xyIntPlot.setDomainCrosshairValue(timeseries[index]);
			xyIntPlot.setRangeCrosshairValue(intensities[index]);
			xyCosTPlot.setDomainCrosshairValue(timeseriesc[index]);
			xyCosTPlot.setRangeCrosshairValue(cost[index]);
			xyAreaPlot.setDomainCrosshairValue(timeseries[index]);
			xyAreaPlot.setRangeCrosshairValue(objarea[index]);
			xyVelPlot.setDomainCrosshairValue(timeseriesm[index]);
			xyVelPlot.setRangeCrosshairValue(velocities[index]);
		}
	}
	
	private XYPlot configXYchart(JFreeChart xyLineChart) {
		XYPlot xyplot = xyLineChart.getXYPlot();
		xyplot.setDomainCrosshairVisible(true);
		xyplot.setRangeCrosshairVisible(true);
		NumberAxis xAxis = (NumberAxis)xyplot.getDomainAxis();
		xAxis.setAutoRangeIncludesZero(false);
		NumberAxis yAxis = (NumberAxis)xyplot.getRangeAxis();
		yAxis.setAutoRangeIncludesZero(false);
		
		return xyplot;
	}

}
