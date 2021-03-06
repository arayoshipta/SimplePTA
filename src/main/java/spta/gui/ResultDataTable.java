/**
 * 
 */
package spta.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import ij.ImagePlus;
import ij.WindowManager;
import ij.measure.Calibration;
import spta.SimplePTA;
import spta.data.AnalyzeTrack;
import spta.data.TrackPoint;
/**
 * @author araiyoshiyuki
 *
 */
public class ResultDataTable extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public JTable jt;
	public List<List<TrackPoint>> tracklist;
	private Calibration cal;
	public ImagePlus imp;
	private ResultDataTable rdt;
	public int[] selectedlist;
	private ChartFrame cframe;

	// for Table
	static String[] columnNames = {"<html>#<br>&nbsp;", "From", "To", "FrameLength", "Ave. Intensity", "Ave. Velocity", "RunLength", "Check", "Color"};
	public Object[][] data;
	static Color[] comboColors = {Color.cyan,Color.blue,Color.red,Color.yellow,Color.green,Color.magenta,Color.orange,Color.white};
	static String[] cString = {"Cyan","Blue","Red","Yellow","Green","Magenta","Orange","White"};
	
	
	public ResultDataTable(List<List<TrackPoint>> tracklist, ImagePlus imp) {
		this.tracklist = tracklist;
		this.imp = imp;
		this.rdt = this;
		this.cal = imp.getCalibration();
		setTableObjectData(tracklist, imp);
		columnNames[5] = "<html>Ave. Velocity<br>" + "(" + cal.getUnit() + "/" + cal.getTimeUnit() + ")";
		columnNames[6] = "<html>RunLength<br>" + "(" + cal.getUnit() + ")";
		new makeTableFrame(this);
	}
	
	public void setTableObjectData(List<List<TrackPoint>> tl, ImagePlus imp) {
		this.imp = imp;
		this.cal = imp.getCalibration();
		int pointnum = 0;
		data = new Object[tl.size()][9];
		for(List<TrackPoint> list: tl) {
			AnalyzeTrack at = new AnalyzeTrack(imp, list);
			data[pointnum][0] = new Integer(pointnum);
			data[pointnum][1] = new Integer(at.startframe);
			data[pointnum][2] = new Integer(at.endframe);
			data[pointnum][3] = new Integer(at.framelength);
			data[pointnum][4] = new Double(at.aveInt);
			data[pointnum][5] = new Double(at.aveVel);
			data[pointnum][6] = new Double(at.runLen);
			data[pointnum][7] = new Boolean(false);
			data[pointnum][8] = Color.cyan; // Dafault color as Cyan
			pointnum++;
		}
		columnNames[5] = "<html>Ave. Velocity<br>" + "(" + cal.getUnit() + "/" + cal.getTimeUnit() + ")";
		columnNames[6] = "<html>RunLength<br>" + "(" + cal.getUnit() + ")";
	}
	
	class makeTableFrame implements ListSelectionListener {
		
		public makeTableFrame(final JFrame frame) {
			
			frame.setTitle(imp.getShortTitle() + ", Total Track = " + String.valueOf((tracklist.size())));
			frame.setBounds(10,10,400,200);
			JPanel pane = (JPanel)frame.getContentPane();
			
			TableModel dm = new DefaultTableModel(data, columnNames) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				
				@Override public Class<?> getColumnClass(int column) {
					return getValueAt(0, column).getClass(); // to return appropriate value
				}
			};
			jt = new JTable(dm) {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				
				@Override public boolean isCellEditable(int row,int column) {
					if(column < 7)
						return false;
					else
						return true; // only column over 7 is editable
				}				
			};
			JComboBox<Color> combobox = new JComboBox<Color>(comboColors);
			combobox.setRenderer(new MyCellRenderer());
			TableCellEditor editor = new DefaultCellEditor(combobox);
			jt.getColumnModel().getColumn(8).setCellEditor(editor);
			jt.setDefaultRenderer(Object.class, new ColorTableRenderer());
			TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(dm);
			sorter.setMaxSortKeys(2);
			jt.setRowSorter(sorter);
			jt.getSelectionModel().addListSelectionListener(this);
			JScrollPane tablePane = new JScrollPane(jt);
			pane.add(tablePane, BorderLayout.CENTER);
			
			JMenuBar menubar = new JMenuBar();
			frame.setJMenuBar(menubar);
			// File menu
			JMenu file = new JMenu("File");
			menubar.add(file);
			JMenuItem saveAll = new JMenuItem("Save all");
			saveAll.addActionListener(new SaveDataAction(imp, rdt));
			JMenuItem saveSelected = new JMenuItem("Save selected");
			saveSelected.addActionListener(new SaveDataAction(imp, rdt));
			JMenuItem saveChecked = new JMenuItem("Save checked");
			saveChecked.addActionListener(new SaveDataAction(imp, rdt));
			JMenuItem saveTableAsText = new JMenuItem("Save Table as Text Data");
			saveTableAsText.addActionListener(new SaveDataAction(imp, rdt));			
			file.add(saveAll);
			file.add(saveSelected);
			file.add(saveChecked);
			file.add(saveTableAsText);
			
			// Edit menu
			JMenu edit = new JMenu("Edit");
			menubar.add(edit);
			JMenuItem deleteTrack = new JMenuItem("Delete Track");
			deleteTrack.addActionListener(new EditTrackAction(imp, rdt));
			edit.add(deleteTrack);
			JMenuItem splitTrack = new JMenuItem("Split Track");
			splitTrack.addActionListener(new EditTrackAction(imp, rdt));
			edit.add(splitTrack);
			JMenuItem concatenateTrack = new JMenuItem("Concatenate Track");
			concatenateTrack.addActionListener(new EditTrackAction(imp, rdt));
			edit.add(concatenateTrack);
			
			frame.setVisible(true);
			frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
			WindowManager.addWindow((Frame)frame);
			rdt = (ResultDataTable)frame;
		}

		@Override
		public void valueChanged(ListSelectionEvent e) {
			if(e.getValueIsAdjusting()) return; // to avoid overlapping procedure
			int index = jt.convertRowIndexToModel(jt.getSelectedRow());
			selectedlist = jt.getSelectedRows();
			for (int ind = 0; ind < selectedlist.length; ind++) 
				selectedlist[ind] = jt.convertRowIndexToModel(selectedlist[ind]);
			SimplePTA.selectedlist = selectedlist;
			SimplePTA.tracklist = tracklist;
			cframe = SimplePTA.getcframe();
			if (selectedlist.length == 1) {
				if (cframe == null) {
					cframe = new ChartFrame(imp, tracklist.get(index));
					SimplePTA.setcframe(cframe);
				}
				else
					cframe.drawTrajectory(tracklist.get(index));
			}
			cframe.setVisible(true);
			imp.setT(tracklist.get(index).get(0).frame); // set imp to first track frame
		}
		
	}
	
	public Color getDataofColor(int index) {
		Object col = jt.getValueAt(jt.convertRowIndexToView(index), 8);
		if(col instanceof String)
			return Color.cyan;
		else
			return (Color)col;
	}

}
