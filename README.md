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
getCenterError - boolean: Toggle this to `true` if you want to be given an accurate uncertainty on cluster parameters. To be given a less accurate estimate, change to `false`
saveFSQData - boolean: Changing this to `true` will create a file `otherData/FSQPoints<fileNum>.csv` that saves the position of each quad in the FSQ space
radius - double: This defines the maximum distance along the x and y axis that will be tested as a potential FSQ center, measured in units of pixels
stepSize - double: This defines the step size between test FSQ centers, measured in units of pixels
errNum - int: Sets the number of random subsets of quads used for estimating errors. Does nothing if getCenterError is `false`
errSize - int: Sets the number of quads contained in each random subset. Does nothing if getCenterError is `false`. NOTE: make sure that this is less than or equal to the total number of quads in the data set!
### Choosing Your Data Set
Upon running `fundamentalSurface/Surface.java`, you will be prompted to choose the number associated with your data set. You can also preemptively choose the data set by running the command
> java fundamentalSurface/Surface.java <data set #>
Note that the three special examples, Ares, Abell, and RXJ, each have their own associated numbers, 1000, 1001, and 1002, respectively. Choosing a number that cannot be found will result in the code ending early

## Included Data Sets
With the exception of our three special example data sets, this code is interested only in the '.ims' extensions. The '.mas' extensions include the true mass profiles of the clusters, which can be plotted using whatever software you choose.
In order to process the '.ims' files, they must be inside of the `sersic/` folder when the code is being run. Included in this repository are data sets 700-799, in both their smooth and structured varieties. Because they are named identically, make sure to keep these files separated. I would recommend keeping them in either the `structured` or `smooth` folders until you wish to run the code.
