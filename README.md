# FSQ_Modeling
Uses the Fundamental Surface of Quads to model key parameters of the galaxy cluster

## Using Java
The coding language used here is Java, so compiling is required before running any of the files. Whenever you need to compile a file, run the command
> javac <path/to/file>

Once compiled, run the file using the command
> java <path/to/file>

## Running the Code
The only file that you should need to run is `fundamentalSurface/Surface.java`. Because I didn't include a config file, this is also the only file that you will need to edit parameters in.

### Key Parameters
debug - boolean:  Leave this `false` unless you want to look at the specific outputs of the code. Making it `true` will flood your console with data that you (probably) won't need

useSubsets - boolean: Toggle this to `true` in order to have uncertainties estimated via bootstrapping. Toggling to `false` will leave uncertainties to be given by the optimization method used to minimize deviations from the FSQ. These uncertainties tend to be a couple orders of magnitude smaller, suggesting that they are greatly overestimating the accuracy of this method.

saveFSQData - boolean: Changing this to `true` will create a file `otherData/FSQPoints<fileNum>.csv` that saves the position of each quad in the FSQ space

buildFSQGrid - boolean: Changing this to `true` will cause the program to take much longer to run, but will create a file `centerData/centerData<data set #>.csv` that can be used to map the deviation from the FSQ as a function of moving the cluster center along the lensing plane.

radius - double: This defines the maximum distance along the x and y axis that will be tested as a potential FSQ center, measured in units of pixels

stepSize - double: This defines the step size between test FSQ centers, measured in units of pixels

accuracy - double: This is the precision to which the quad-estimated center is found (in units of pixels)

errNum - int: Sets the number of random subsets of quads used for estimating errors. Does nothing if getCenterError is `false`

errSize - int: Sets the number of quads contained in each random subset. Does nothing if getCenterError is `false`. NOTE: make sure that this is less than or equal to the total number of quads in the data set!

### Choosing Your Data Set
Upon running `fundamentalSurface/Surface.java`, you will be prompted to choose the number associated with your data set. You can also preemptively choose the data set by running the command
> java fundamentalSurface/Surface.java <data set #>

Note that the three special examples, Ares, Abell, and RXJ, each have their own associated numbers, 1000, 1001, and 1002, respectively. Choosing a number that cannot be found will result in the code ending early

### Outputs
The code will create or modify a few .csv files each time it is run.

`centerData/centerData<data set #>.csv`: Contains the deviation from the Fundamental Surface of Quads produced by setting the FSQ center to each test point from this run. Each row comes from a single data set, and the columns represent:

  Column 1: x position, measured in units of pixels

  Column 2: y position, measured in units of pixels

  Column 3: Value of \delta_{FSQ} at this point

`otherData/FSQPoints<data set #>.csv`: Only is created if saveFSQData is `true`. Contains the points of each quad in the data set within the FSQ space. Each row comes from a single quad, and the columns represent:

  Column 1: Value of \theta_12 in radians

  Column 2: Value of \theta_23 in radians

  Column 3: Value of \theta_34 in radians

`otherData/minData.csv`: Contains information about the FSQ center. Only updates if this data set does not already have a dedicated line in the file. Each row comes from a single data set, and the columns represent:

  Column 1: Data set number

  Column 2: Value of minimum \delta_{FSQ} in this data set

  Column 3: x position of minimum \delta_{FSQ} in this data set

  Column 4: y position of minimum \delta_{FSQ} in this data set

  Column 5: Uncertainty of FSQ Center location, measured in units of pixels. Calculated to be the average distance of the subsets of quads' FSQ center.

  Column 6: Size of Einstein Radius, or average distance of lensed image from the coordinate center of the data set, measured in units of pixels.

  Column 7: Average of |\theta_12| over all quads in the data set, measured in radians

  `otherData/slopeData.csv`: Contains information about the cluster's position angle and ellipticity. Only updates if this data set does not already have a dedicated line in the file. Each row comes from a single data set, and the columns represent:

  Column 1: Data set number

  Column 2: Slope of the line following \theta_Q

  Column 3: Slope of the line following \theta_Q + one standard deviation

  Column 4: Slope of the line following \theta_Q - one standard deviation

  Column 5: Value of the radius parameter for this run

  Column 6: Value of one standard deviation on \theta_Q. Calculated using the standard deviation of the position angles found by testing the random subsets of quads

  `otherData/subsetData.csv`: Contains information about the random subsets of quads used. Only updates if this data set does not already have a dedicated line in the file. Each row comes from a single data set, and the columns represent:

  Column 1: Data set number

  Column 2n+1: x position of the FSQ center for the *n*th subset

  Column 2(n+1): y position of the FSQ center for the *n*th subset

## Included Data Sets
With the exception of our three special example data sets, this code is interested only in the '.ims' extensions. The '.mas' extensions include the true mass profiles of the clusters, which can be plotted using whatever software you choose.

In order to process the '.ims' files, they must be inside of the `sersic/` folder when the code is being run. Included in this repository are data sets 600-699, in both their smooth and structured varieties. Because they are named identically, make sure to keep these files separated. I would recommend keeping them in either the `structured/` or `smooth/` folders until you wish to run the code.

The `.ims` files are all structured into columns, with each line corresponding to a single image produced by the cluster:

  Column 1: The number of the source that generated this image

  Column 2: The number of images produced by this source

  Column 3: x position of the image, measured in units of pixels

  Column 4: y position of the image, measured in units of pixels

  Column 5: Magnification of the image

  Column 6: x position of the source, measured in units of pixels

  Column 7: y position of the source, measured in units of pixels

  Column 8: Arrival order of image, relative to other images from its source

The `.mas` files files are all structured into columns, with each line being a point in the lens plane:

  Column 1: x position, measured in units of pixels
  
  Column 2: y position, measured in units of pixels
  
  Column 3: Surface mass density of lensing cluster at this point
  
For the three special files, they may be left in place in the `otherData/` directory.

The data for Ares, `otherData/images_quads_Ares.dat`, is structured into columns, with each line representing a single image. The lines are organized from top to bottom in arrival order.

  Column 1: x position of image, measured in arcseconds
  
  Column 2: y position of image, measured in arcseconds
  
  Column 3: Number of the source that generated this image
  
  Column 4: Image number within the source
  
  Column 5: Source redshift
  
The data for Abell 1689, `otherData/images_quads_A1689.dat`, is structured into columns, with each line representing a single image. The lines are organized from top to bottom in arrival order.

  Column 1: Number of the source that generated this image
  
  Column 2: Identification number for this image relative to others generated by this source
  
  Column 3: x position of image, measured in arcseconds
  
  Column 4: y position of image, measured in arcseconds
  
  Column 5: Source redshift
  
The data for RXJ 1347, `otherData/images_quads_RXJ1327`, is structured into columns, with each line representing a single image. The lines are organized  from top to bottom by arrival order.

  Column 1: <Source number>.<Image number> identifier
  
  Column 2: x position of image, measured in arcseconds
  
  Column 3: y position of image, measured in arcseconds
  
  Column 4: Source redshift
