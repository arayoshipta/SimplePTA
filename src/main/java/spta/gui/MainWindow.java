/**
 * 
 */
package spta.gui;

import javax.swing.JFrame;

import ij.*;
import ij.gui.*;
import ij.process.*;
import spta.SimplePTA;
import spta.data.TrackPoint;
import spta.track.TrackObject;

import java.awt.GridBagLayout;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.FlowLayout;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.border.BevelBorder;
import java.awt.Color;
import javax.swing.border.EtchedBorder;
import javax.swing.JLabel;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import java.awt.Font;
import javax.swing.ButtonGroup;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.Rectangle;
import javax.swing.BoxLayout;

/**
 * @author araiyoshiyuki
 *
 */
public class MainWindow extends JFrame {
	private final ButtonGroup methodButtonGroup = new ButtonGroup();
	private final ButtonGroup roiButtonGroup = new ButtonGroup();
	
	public SpinnerNumberModel roisize;
	public SpinnerNumberModel searchrange;
	public SpinnerNumberModel lowersize;
	JSpinner RoiSize;
	public int method;
	public boolean stateOfTrajectory = true;

	private ImagePlus imp;
	private JCheckBox AllCheckBox;
	private JCheckBox ROICheckBox;
	private JCheckBox NumberCheckBox;
	
	TrackObject to;
	
	public MainWindow(ImagePlus _imp, final List<List<TrackPoint>> tracklist) {
		setBounds(new Rectangle(500, 220, 300, 300));
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		this.imp = _imp;
		
		setTitle("Simple PTA ver.0.7.2");
		setResizable(false);
		getContentPane().setLayout(new GridLayout(3, 1, 0, 0));
		
		JPanel command_panel = new JPanel();
		getContentPane().add(command_panel);
		command_panel.setLayout(new GridLayout(1, 1, 0, 0));
		
		JPanel TrackPanel = new JPanel();
		command_panel.add(TrackPanel);
		TrackPanel.setLayout(new GridLayout(1, 2, 0, 0));
		
		JPanel panel = new JPanel();
		TrackPanel.add(panel);
		
		JButton DoTrackButton = new JButton("Single Track");
		DoTrackButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Roi currentRoi = imp.getRoi();
				if(currentRoi == null) {
					IJ.error("No object was selected");
					return;
				}
				int roitype = currentRoi.getType();				
				if(imp.getDimensions()[2] != 1) {
					IJ.error("only single channel image");
					return;
				}
				
				if(imp.getStackSize() != imp.getNFrames()) {
					int currentSlice = imp.getSlice();
					GenericDialog yesnostack = new GenericDialog("Frame or Slice?");
					yesnostack.addMessage("Total Slice size: " + imp.getNSlices() + " is not equal "
							+ "to the total stack size: "+ imp.getStackSize() + " ");
					yesnostack.addMessage("Is it ok to convert slice to stack?");
					yesnostack.enableYesNoCancel();
					yesnostack.showDialog();
					if(yesnostack.wasOKed()) {
						imp.setDimensions(1, 1, imp.getStackSize());
						imp.setT(currentSlice); // move to current slice
					}
				}
				if (roitype == 2 && (imp.getSlice() != imp.getStackSize())) { // i.e Roi is wand
					GenericDialog yesnotrack = new GenericDialog("Track?");
					yesnotrack.addMessage("Track this object?");
					yesnotrack.enableYesNoCancel();
					yesnotrack.showDialog();
					if(yesnotrack.wasOKed()) {
						IJ.log("Start Tracking");
						SimplePTA.isTracking = true;
						to = new TrackObject(imp, currentRoi, method, roisize, searchrange, tracklist);
						to.start();
						SimplePTA.isTracking = false;
					}
				}
			}
		});
		panel.setLayout(new GridLayout(1, 1, 0, 0));
		panel.add(DoTrackButton);
		
		JPanel DetectionPanel = new JPanel();
		TrackPanel.add(DetectionPanel);
		DetectionPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Detection Methods", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		DetectionPanel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JRadioButton Centroid_RadioButton = new JRadioButton("Centroid");
		DetectionPanel.add(Centroid_RadioButton);
		Centroid_RadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				method = 0;
			}
		});
		Centroid_RadioButton.setSelected(true);
		methodButtonGroup.add(Centroid_RadioButton);
		Centroid_RadioButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		
		JRadioButton CenterOfMass_RadioButton = new JRadioButton("CeterOfMass");
		DetectionPanel.add(CenterOfMass_RadioButton);
		CenterOfMass_RadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				method = 1;
			}
		});
		methodButtonGroup.add(CenterOfMass_RadioButton);
		CenterOfMass_RadioButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		
		JRadioButton Gaussian_RadioButton = new JRadioButton("2D Gaussian");
		DetectionPanel.add(Gaussian_RadioButton);
		Gaussian_RadioButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				method = 2;
			}
		});
		methodButtonGroup.add(Gaussian_RadioButton);
		Gaussian_RadioButton.setFont(new Font("Lucida Grande", Font.PLAIN, 13));
		
		JPanel param_panel = new JPanel();
		getContentPane().add(param_panel);
		param_panel.setLayout(new GridLayout(1, 1, 0, 0));
		
		JPanel DetectionParamPanel = new JPanel();
		DetectionParamPanel.setBorder(new TitledBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null),
				"Detection Parameters", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		param_panel.add(DetectionParamPanel);
		DetectionParamPanel.setLayout(new GridLayout(3, 2, 0, 0));
		
		JLabel LabelRoi = new JLabel("Roi Size (pixels)");
		LabelRoi.setToolTipText("The lenght of the side of square ROI");
		DetectionParamPanel.add(LabelRoi);
		
		roisize = new SpinnerNumberModel(12, 5, null, 1);
		RoiSize = new JSpinner(new SpinnerNumberModel(new Integer(12), new Integer(5), null, new Integer(1)));
		RoiSize.setToolTipText("The lenght of the side of square ROI (Pixels)");
		RoiSize.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SimplePTA.roisize = (Integer)roisize.getValue();
			}
		});
		DetectionParamPanel.add(RoiSize);
		
		JLabel lblSearchRangepixels = new JLabel("Search Size (pixels)");
		lblSearchRangepixels.setToolTipText("The length of the side of a square ROI which limits the search range to find next object. ");
		DetectionParamPanel.add(lblSearchRangepixels);
		
		searchrange = new SpinnerNumberModel(30, 1, null, 1);
		JSpinner SearchRange = new JSpinner(new SpinnerNumberModel(new Integer(30), new Integer(1), null, new Integer(1)));
		SearchRange.setToolTipText("The length of the side of a square ROI which limits the search range to find next object. ");
		SearchRange.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SimplePTA.searchrange = (Integer)searchrange.getValue();
			}
		});
		DetectionParamPanel.add(SearchRange);
		
		JLabel lblLowerSizepixels = new JLabel("Lower size (pixels)");
		lblLowerSizepixels.setToolTipText("Lower size of objects");
		
		DetectionParamPanel.add(lblLowerSizepixels);
		
		lowersize = new SpinnerNumberModel(5, 1, null, 1);
		JSpinner LowerSize = new JSpinner();
		LowerSize.setModel(new SpinnerNumberModel(new Integer(5), new Integer(1), null, new Integer(1)));
		LowerSize.setToolTipText("Lower size of objects");
		LowerSize.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent arg0) {
				SimplePTA.lowersize = (Integer)lowersize.getValue();
			}
			
		});
		DetectionParamPanel.add(LowerSize);
		
		JPanel AppearancePanel = new JPanel();
		getContentPane().add(AppearancePanel);
		AppearancePanel.setLayout(new GridLayout(2, 1, 0, 0));
		
		JPanel TrajectoryPanel = new JPanel();
		TrajectoryPanel.setBorder(new TitledBorder(null, "Trajectory",
				TitledBorder.LEADING, TitledBorder.TOP, null, null));
		AppearancePanel.add(TrajectoryPanel);
		TrajectoryPanel.setLayout(new GridLayout(1, 2, 0, 0));
		
		JRadioButton AlwaysRadioButton = new JRadioButton("Always");
		AlwaysRadioButton.setToolTipText("Whole trajecotires are always shown.");
		AlwaysRadioButton.setSelected(true);
		roiButtonGroup.add(AlwaysRadioButton);
		TrajectoryPanel.add(AlwaysRadioButton);
		AlwaysRadioButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stateOfTrajectory = true;
				imp.updateAndDraw();
			}
			
		});
		
		JRadioButton GrowingRadioButton = new JRadioButton("Growing");
		GrowingRadioButton.setToolTipText("Trajectories will be drawn with corresponding to the frame position");
		roiButtonGroup.add(GrowingRadioButton);
		TrajectoryPanel.add(GrowingRadioButton);
		GrowingRadioButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				stateOfTrajectory = false;
				imp.updateAndDraw();
			}
			
		});
		
		JPanel AppearancePanel2 = new JPanel();
		AppearancePanel.add(AppearancePanel2);
		AppearancePanel2.setLayout(new GridLayout(2, 3, 0, 0));
		
		AllCheckBox = new JCheckBox("All");
		AllCheckBox.setToolTipText("Show all tracks");
		AppearancePanel2.add(AllCheckBox);
		AllCheckBox.addActionListener(new ActionListener () {

			@Override
			public void actionPerformed(ActionEvent e) {
				imp.updateAndDraw();
			}
			
		});
		
		ROICheckBox = new JCheckBox("ROI");
		ROICheckBox.setSelected(true);
		ROICheckBox.setToolTipText("Show squrare ROI\n");
		AppearancePanel2.add(ROICheckBox);
		ROICheckBox.addActionListener(new ActionListener () {

			@Override
			public void actionPerformed(ActionEvent e) {
				imp.updateAndDraw();
			}
			
		});
		
		NumberCheckBox = new JCheckBox("Number");
		NumberCheckBox.setSelected(true);
		NumberCheckBox.setToolTipText("Show number of each track");
		AppearancePanel2.add(NumberCheckBox);
		NumberCheckBox.addActionListener(new ActionListener () {

			@Override
			public void actionPerformed(ActionEvent e) {
				imp.updateAndDraw();
			}
			
		});
		
//		roisize = new SpinnerNumberModel(12, 5, 50, 1); //	
//		searchrange = new SpinnerNumberModel(30, 5, 100, 1);
//		lowersize = new SpinnerNumberModel(5, 1, 100, 1);
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				ResultDataTable rdt = SimplePTA.getRDT();
				if (rdt != null) {
					GenericDialog gd = new GenericDialog("Close");
					gd.addMessage("Do you want to close PTA2?");
					gd.enableYesNoCancel();
					gd.showDialog();
					if(gd.wasCanceled())
						return;
					rdt.dispose();
				}
				ImagePlus.removeImageListener(SimplePTA.listener);
				WindowManager.removeWindow(SimplePTA.mw);
				imp.setOverlay(null);
				SimplePTA.frame = null;
			}
			
//			@Override
//			public void windowClosed(WindowEvent e) {
//				ImagePlus.removeImageListener(SimplePTA.listener);
//				WindowManager.removeWindow(SimplePTA.mw);
//				imp.setOverlay(null);
//				SimplePTA.frame = null;
//				IJ.log("Window closed");
//			}
		});
	}
	
	public int retMethod() {
		return method;
	}

	public boolean isAllTrack() {
		return AllCheckBox.isSelected();
	}
	
	public boolean isRoiTrack() {
		return ROICheckBox.isSelected();
	}
	
	public boolean isNumTrack() {
		return NumberCheckBox.isSelected();
	}
	
	public void setImp(ImagePlus imp) {
		this.imp = imp;
	}
	
	public ImagePlus getImp() {
		return imp;
	}

}
