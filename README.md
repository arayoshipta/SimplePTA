#SimplePTA
## About
SimplePTA is a ImageJ1.x plugin that enable semi-automatic object tracking. 

## Important
This plugin is free to use, but the copyright is not abandoned. **I HAVE NO RESPONSIBILITY TO ANY DAMAGE BECAUSE OF THE USE OF THIS PLUG-IN**

##Install
If you are beginner, I recommend you to use [Fiji](https://fiji.sc/) since it equips all required libraries for SimpltPTA.
### Fiji
 1. Put "SimplePTA_-0.5.0-SNAPSHOT.jar" into Fiji's plugins folder
 2. Start Fiji, then you'll find "PTA2" in plugins menu bar

### ImageJ
 1. Download additional jar files as follows
 - JFreeChart.jar
 - JCommons.jar
	 - These can be downloads from [JFreeChart](http://www.jfree.org/jfreechart/)
 - commons-math3-3.x.x.jar
	 - [Apache Commons Math](http://commons.apache.org/proper/commons-math/download_math.cgi)

## Usage
### Multiple tracking
1. Prepare stack image.
2. Launch **SimplePTA** from plugin menu.
3. Adjust threshold. *This process is required if you want to store **Size** information.*
4. Choose tracking methods from Main Window. Now I offer "Centroid", "Center of Mass", and "2-Dimensional Gaussian Fitting" 
5. Click object what you want to track. The cross-target will appear.
6. Press "Single Track" button to perfomr tracking.
7. After tracking cells, Result table data will appear.


### Table
1. When you click the row of table,  multi-plot graph will be appeared.
2. Multiple selection enables multiple indication of Roi's in the image.
3. Table has tow menus, Save and Edit

#### Save menu
1. You can save your data as text data from save menu.
	 - I encourage you to analyze data by Pandas in python. 

#### Edit menu
1. You can **delete**, **split**, and **concatenate** tracks.
2. **Split** can be performed at the current frame
3. To concatenate tracks, choose two tracks that don't overlap about time.
4. If there is a gap between last frame of first track and first frame of second track, simple interpolation will be performed.

#### History
2016.08.01 version 0.5 uploaded to GitHub

#### Author information
Yoshiyuki Arai

E-mail: projectptaj@gmail.com