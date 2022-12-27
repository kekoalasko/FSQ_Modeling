package fundamentalSurface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

import javax.imageio.ImageIO;

import java.awt.image.BufferedImage;
import java.awt.Color;

import util.LensEvent;

public class Surface {
	private static final boolean debug = false;	//Debugging = true
	private static final boolean getCenterError = true;	//Get error on cluster center = true
	private static final boolean saveFSQData = false;	//Save FSQ points to demonstrate deviation from the surface
	private static final double p00 = -5.792;		//Start parameters from fundamental surface of quads
	private static final double p10 = 1.783;		//pXY is the coefficient for theta_12^X*theta_34^Y
	private static final double p01 = 1.784;		//
	private static final double p20 = 0.1648;		//
	private static final double p11 = -0.7275;		//
	private static final double p02 = 0.1643;		//
	private static final double p30 = -0.04591;		//
	private static final double p21 = 0.0549;		//
	private static final double p12 = 0.05493;		//
	private static final double p03 = -0.04579;		//
	private static final double p40 = -0.0001486;	//
	private static final double p31 = 0.01487;		//
	private static final double p22 = -0.03429;		//
	private static final double p13 = 0.01487;		//
	private static final double p04 = -0.0001593;	//End parameters from fundamental surface of quads
	private static final double radius = 250.0;	//Square "radius" within which we will search for the cluster center
	private static final double stepSize = radius / 500.0;	//Resolution of the search within the radius
	private static final int errNum = 30;	//Number of subsets to use for estimate of center error
	private static final int errSize = 10;	//Size of subsets to use for estimate of center error
	private static final int ARES = 1000;	//Code used to read the Ares simulation data
	private static final int ABELL = 1001;	//Code used to read the Abell 1689 observed data
	private static final int RXJ = 1002;	//Code used to read the RXJ1347 observed data
	private static final String aresPath = "otherData/images_quads_Ares.dat";	//Path to Ares data
	private static final String abellPath = "otherData/images_quads_A1689.dat"; //Path to Abell 1689 data
	private static final String RXJPath = "otherData/images_quads_RXJ1347.dat";	//Path to Ares data
	private static final File slopeData = new File("otherData/slopeData.csv");	//File in which slope data is stored
	private static final File minData = new File("otherData/minData.csv");	//File in which data on cluster minimum RMS data is stored
	private static final File subsetData = new File("otherData/subsetData.csv");	//File in which subset center points are stored
	
	private static int fileNum;	//Number associated with data set, must be integer in [0,99]
	private static String fileEnd;	//Number extension associated with file
	private static double[] imageX;	//Position on x-axis of image
	private static double[] imageY;	//Position on y-axis of image
	private static double[] magnification;	//Magnification of image
	private static double[] sourceN;	//Source number
	private static double[] sourceX;	//Position on x-axis of source
	private static double[] sourceY;	//Position on y-axis of source
	private static LensEvent[] events;	//Events from data set
	private static int numTiles;	//Number of tiles in one direction which are being tested as the cluster center
	private static double[] bestCenter;	//The RMS minimum center of the cluster
	
	@SuppressWarnings("unused")
	public static void main(String[] args) {
		//Uses argument to determine file number & name
		Scanner scan = new Scanner(System.in);
		try {
			fileNum = Integer.valueOf(args[0]);
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (ArrayIndexOutOfBoundsException e) {
			System.out.println("Enter data set number:");
			fileNum = scan.nextInt();
		}
		if (fileNum < 10) {
			fileEnd = "0" + fileNum;
			System.out.println("Working with Data Set " + fileEnd + "...");
		} else if (fileNum == ARES) {
			System.out.println("Working with simulated cluster Ares...");
			fileEnd = String.valueOf(ARES);
		} else if (fileNum == ABELL) {
			System.out.println("Working with Galaxy Cluster Abell 1689...");
			fileEnd = String.valueOf(ABELL);
		} else if (fileNum == RXJ) {
			System.out.println("Working with Galaxy Cluster RXJ1347...");
			fileEnd = String.valueOf(RXJ);
		} else {
			fileEnd = String.valueOf(fileNum);
			System.out.println("Working with Data Set " + fileEnd + "...");
		}
		
		//Collect source/image values from file
		if (fileNum < 1000) {
			try {
				//Determine number of lines in file
				int lines = (int) Files.lines(Paths.get("sersic/makegalKLsersic.ims" + fileEnd)).count();
				if (debug) System.out.println("Lines read:\t" + lines);
				imageX = new double[lines];
				imageY = new double[lines];
				magnification = new double[lines];
				sourceX = new double[lines];
				sourceY = new double[lines];
				
				Scanner ims = new Scanner(new File("sersic/makegalKLsersic.ims" + fileEnd));
				for (int i = 0; i < lines; i ++) {
					ims.next(); //Column 1 = source #
					ims.next(); //Column 2 = # of images from source
					imageX[i] = ims.nextDouble(); //Column 3 = image x
					imageY[i] = ims.nextDouble(); //Column 4 = image y
					magnification[i] = ims.nextDouble(); //Column 5 = image magnification
					sourceX[i] = ims.nextDouble(); //Column 6 = source x
					sourceY[i] = ims.nextDouble(); //Column 7 = source y
					ims.next(); //Column 8 = arrival order
				}
				ims.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (fileNum == ARES) {
			try {
				//Determine number of lines in file
				int lines = (int) Files.lines(Paths.get(aresPath)).count();
				imageX = new double[lines];
				imageY = new double[lines];
				sourceN = new double[lines];
				
				Scanner ims = new Scanner(new File(aresPath));
				for (int i = 0; i < lines; i ++) {
					imageX[i] = ims.nextDouble();	//Column 1 = image x
					imageY[i] = ims.nextDouble();	//Column 2 = image y
					sourceN[i] = ims.nextInt();	//Column 3 = source #
					ims.next();	//image # (ordered by arrival?)
					ims.next();	//source redshift
				}
				ims.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (fileNum == ABELL) {
			try {
				//Determine number of lines in file
				int lines = (int) Files.lines(Paths.get(abellPath)).count();
				imageX = new double[lines];
				imageY = new double[lines];
				sourceN = new double[lines];
				
				Scanner ims = new Scanner(new File(abellPath));
				for (int i = 0; i < lines; i ++) {
					sourceN[i] = ims.nextInt();	//Column 1 = source #
					ims.next();	//Column 2 = image # (ordered by arrival?)
					imageX[i] = ims.nextDouble();	//Column 3 = image x
					imageY[i] = ims.nextDouble();	//Column 4 = image y
					ims.next();	//Column 5 = source redshift
				}
				ims.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (fileNum == RXJ) {
			try {
				//Determine number of lines in file
				int lines = (int) Files.lines(Paths.get(RXJPath)).count();
				imageX = new double[lines];
				imageY = new double[lines];
				sourceN = new double[lines];
				
				Scanner ims = new Scanner(new File(RXJPath));
				for (int i = 0; i < lines; i ++) {
					sourceN[i] = Math.floor(ims.nextDouble());	//Column 1 = source #
					imageX[i] = -ims.nextDouble();	//Column 2 = image x
					imageY[i] = ims.nextDouble();	//Column 3 = image y
					ims.next();	//Column 4 = source redshift
				}
				ims.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		//Construct lensing events
		if (sourceN == null) {
			events = new LensEvent[imageX.length / 5];
			for (int i = 0; i < events.length; i ++) {
				events[i] = new LensEvent(new int[] {5*i + 1, 5*i + 2, 5*i + 3, 5*i + 4, 5*i + 5});
				events[i].setImageX(new double[] {imageX[5*i], imageX[5*i+1], imageX[5*i+2], imageX[5*i+3], imageX[5*i+4]});
				events[i].setImageY(new double[] {imageY[5*i], imageY[5*i+1], imageY[5*i+2], imageY[5*i+3], imageY[5*i+4]});
				if (debug && events[i].findCenter() % 5 != 0) {
					System.out.println("Unable to find center of event #" + (i + 1));
					System.out.println("Image coordinates:");
					System.out.println("x = " + imageX[5*i] + "\ty = " + imageY[5*i]);
					System.out.println("x = " + imageX[5*i+1] + "\ty = " + imageY[5*i+1]);
					System.out.println("x = " + imageX[5*i+2] + "\ty = " + imageY[5*i+2]);
					System.out.println("x = " + imageX[5*i+3] + "\ty = " + imageY[5*i+3]);
					System.out.println("x = " + imageX[5*i+4] + "\ty = " + imageY[5*i+4]);
				}
			}
		} else {
			events = new LensEvent[imageX.length / 4];
			for (int i = 0; i < events.length; i ++) {
				events[i] = new LensEvent(new int[] {4*i + 1, 4*i + 2, 4*i + 3, 4*i + 4});
				events[i].setImageX(new double[] {imageX[4*i], imageX[4*i+1], imageX[4*i+2], imageX[4*i+3]});
				events[i].setImageY(new double[] {imageY[4*i], imageY[4*i+1], imageY[4*i+2], imageY[4*i+3]});
			}
		}
		if (debug) System.out.println("Analyzing " + events.length + " events...");
		
		if (debug) System.out.println("Calculating RMS values from FSQ for different cluster centers...");
		
		//Keeps record of min and max values
		double RMSmin = Integer.MAX_VALUE;
		double RMSmax = 0;
		double minX = -1;
		double minY = -1;
		
		//Find center of the cluster
		double aveX = 0.0;
		double aveY = 0.0;
		if (fileNum < 1000) { //Only for sets given on a 0-500 pixel square grid
			aveX = 250;
			aveY = 250;
		} else if (fileNum == ARES) {
			aveX = -15;
			aveY = -30;
		} else if (fileNum == RXJ || fileNum == ABELL) {
			aveX = 0;
			aveY = 0;
		} else { //Sets with an unknown center
			//Find average of all image locations
			int size = imageX.length;
			for (int i = 0; i < size; i ++) {
				aveX += imageX[i] / size;
				aveY += imageY[i] / size;
			}
		}

		//Find the RMS values of theta23 for having the galaxy cluster in numTiles^2 different locations
		numTiles = (int) (2*radius / stepSize) + 1;
		double[][] rmsValues = new double[numTiles*numTiles][3];
		for (int i = 0; i < numTiles; i ++) { //x index
			for (int j = 0; j < numTiles; j ++) { //y index	
				//Define cluster center for this iteration
				double centerX = aveX + stepSize * (i - numTiles / 2);
				double centerY = aveY + stepSize * (j - numTiles / 2);
				setClusterCenter(centerX, centerY);
				
				//Get expected and measured values
				double[] thetaExpct = new double[events.length];
				double[] thetaMeasd = new double[events.length];
				for (int k = 0; k < events.length; k ++) {
					double theta12 = findTheta12(events[k]);
					double theta34 = findTheta34(events[k]);
					thetaExpct[k] = fitTheta23(theta12, theta34);
					thetaMeasd[k] = findTheta23(events[k]);
				}
				
				//Find RMS value and save to array
				rmsValues[numTiles*i+j][0] = centerX;
				rmsValues[numTiles*i+j][1] = centerY;
				rmsValues[numTiles*i+j][2] = RMS(thetaExpct, thetaMeasd);
				
				//Update min/max values
				if (rmsValues[numTiles*i+j][2] < RMSmin) {
					RMSmin = rmsValues[numTiles*i+j][2];
					minX = centerX;
					minY = centerY;
				}
				if (rmsValues[numTiles*i+j][2] > RMSmax) {
					RMSmax = rmsValues[numTiles*i+j][2];
				}
			}
		}
		
		//Records the minimum RMS center point
		bestCenter = new double[] {minX, minY};
		if (debug) System.out.println("Minimum RMS Center Point:");
		if (debug) System.out.println("(" + bestCenter[0] + ", " + bestCenter[1] + ")");
		
		//Find errNum cluster centers based on subsets of events of size errSize
		double centerDist = 0.0;
		double[][] centers = new double[errNum][2];
		LensEvent[][] subsets = new LensEvent[errNum][errSize];
		if (getCenterError) {
			int[][] indexList = getRandomIntsArray(errSize, errNum, 0, events.length);
			for (int i = 0; i < errNum; i ++) {
				int[] index = indexList[i];
				if (debug) System.out.print("Random integer sequence:\t");
				for (int j = 0; j < errSize; j ++) {
					subsets[i][j] = events[index[j]];
					if (debug) System.out.print(index[j] + ", ");
				}
				if (debug) System.out.print("\nAssociated Center:\t");
				centers[i] = findCenter(subsets[i], aveX, aveY);
				if (debug) System.out.println("(" + centers[i][0] + ", " + centers[i][1] + ")");
				
				//Compute average distance from center
				centerDist += Math.sqrt(Math.pow(centers[i][0] - minX, 2) + Math.pow(centers[i][1] - minY, 2)) / errNum;
			}
		}
		
		//Finds bisector of theta12
		double bisector = 0.0;
		double[] angles = new double[events.length];
		if (sourceN == null) {
			for (int i = 0; i < events.length; i ++) {
				double[] img1 = new double[] {imageX[5*i], imageY[5*i]};
				double[] img2 = new double[] {imageX[5*i+1], imageY[5*i+1]};
				angles[i] = bisect(bestCenter, img1, img2);
			}
		} else {
			for (int i = 0; i < events.length; i ++) {
				double[] img1 = new double[] {imageX[4*i], imageY[4*i]};
				double[] img2 = new double[] {imageX[4*i+1], imageY[4*i+1]};
				angles[i] = bisect(bestCenter, img1, img2);
			}
		}
		bisector = aveAngle(angles);
		if (debug) System.out.println("Bisector of theta12:");
		if (debug) System.out.println("Angle:\t" + bisector + "\tSlope:\t" + Math.tan(bisector));
		
		//Calculates bisector of theta_12 for each quad/subset of quads
		double[] bisectors;
		if (getCenterError) {	//Compute bisectors for centers from multiple subsets
			bisectors = new double[errNum];
			double[] moreAngles = new double[errSize];
			for (int j = 0; j < errNum; j ++) {
				for (int i = 0; i < errSize; i ++) {
					double[] img1 = new double[] {subsets[j][i].getX(0), subsets[j][i].getY(0)};
					double[] img2 = new double[] {subsets[j][i].getX(1), subsets[j][i].getY(1)};
					moreAngles[i] = bisect(new double[] {centers[j][0], centers[j][1]}, img1, img2);
				}
				bisectors[j] = aveAngle(moreAngles);
			}
		} else {	//Compute bisector for a single center
			bisectors = new double[events.length];
			if (sourceN == null) {
				for (int i = 0; i < events.length; i ++) {
					double[] img1 = new double[] {imageX[5*i], imageY[5*i]};
					double[] img2 = new double[] {imageX[5*i+1], imageY[5*i+1]};
					bisectors[i] = bisect(bestCenter, img1, img2);
				}
			} else {
				for (int i = 0; i < events.length; i ++) {
					double[] img1 = new double[] {imageX[4*i], imageY[4*i]};
					double[] img2 = new double[] {imageX[4*i+1], imageY[4*i+1]};
					bisectors[i] = bisect(bestCenter, img1, img2);
				}
			}
		}
		
		//Computes error on bisector
		double stddev = 0.0;
		double[] delta = new double[bisectors.length];
		for (int i = 0; i < delta.length; i ++) {
			delta[i] = bisector - bisectors[i];
			if (delta[i] > Math.PI/2) {
				delta[i] -= Math.PI;
			} else if (delta[i] < -Math.PI/2) {
				delta[i] += Math.PI;
			}
			stddev += delta[i]*delta[i];
		}
		stddev = Math.sqrt(stddev/delta.length);
		
		if (debug) System.out.println("Standard Deviation on Bisector of theta12:");
		if (debug) System.out.println("sigma:\t" + stddev + "\tSlope Upper Bound:\t" + Math.tan(bisector + stddev) + "\tSlope Lower Bound:\t" + Math.tan(bisector - stddev));
		
		//Reset cluster center to bestCenter
		setClusterCenter(bestCenter[0], bestCenter[1]);
		
		//Get average image distribution from center image
		double aveImgDist = 0.0;
		if (sourceN == null) {
			for (int i = 0; i < events.length; i ++) {
				aveImgDist += distance(imageX[5*i+1], imageY[5*i+1], bestCenter[0], bestCenter[1]) / (4 * events.length);
				aveImgDist += distance(imageX[5*i+2], imageY[5*i+2], bestCenter[0], bestCenter[1]) / (4 * events.length);
				aveImgDist += distance(imageX[5*i+3], imageY[5*i+3], bestCenter[0], bestCenter[1]) / (4 * events.length);
				aveImgDist += distance(imageX[5*i+4], imageY[5*i+4], bestCenter[0], bestCenter[1]) / (4 * events.length);
			}
		} else {
			for (int i = 0; i < events.length; i ++) {
				aveImgDist += distance(imageX[4*i], imageY[4*i], bestCenter[0], bestCenter[1]) / (4 * events.length);
				aveImgDist += distance(imageX[4*i+1], imageY[4*i+1], bestCenter[0], bestCenter[1]) / (4 * events.length);
				aveImgDist += distance(imageX[4*i+2], imageY[4*i+2], bestCenter[0], bestCenter[1]) / (4 * events.length);
				aveImgDist += distance(imageX[4*i+3], imageY[4*i+3], bestCenter[0], bestCenter[1]) / (4 * events.length);
			}
		}
		
		//Get average of theta12 for all given quads
		double aveTheta12 = 0.0;
		for (int i = 0; i < events.length; i ++) {
			aveTheta12 += findTheta12(events[i]) / events.length;
		}
		
		//Save slope information
		if (debug) System.out.println("Saving bisector data...");
		saveBisector(bisector, stddev);
		
		//Save minimum RMS information
		if (debug) System.out.println("Saving minimum RMS data...");
		saveMinData(RMSmin, minX, minY, centerDist, aveImgDist, aveTheta12);
		
		//Save subset center information
		if (debug) System.out.println("Saving subset data...");
		saveSubset(centers);
		
		//Save generated image
		try {
			if (debug) System.out.println("Saving FSQ cluster center RMS values");
			//plotLog(RMSmin, RMSmax, rmsValues);
			//plotLinear(RMSmin, RMSmax, rmsValues);
			plotCSV(rmsValues);
			if (debug) System.out.println("RMS values saved");
		} catch (IOException e) {
			System.out.println("Something went wrong with saving RMS values");
			e.printStackTrace();
		}
		
		//Save FSQ Points
		if (saveFSQData) {
			try {
				setClusterCenter(bestCenter[0], bestCenter[1]);	//Reset cluster center to that with minimum \delta_{FSQ}
				String FSQPath = "otherData/FSQPoints" + fileEnd + ".csv";
				FileWriter FSQWriter = new FileWriter(new File(FSQPath), true);
				for (int i = 0; i < events.length; i ++) {
					FSQWriter.write(String.valueOf(findTheta12(events[i])));
					FSQWriter.write(',');
					FSQWriter.write(String.valueOf(findTheta23(events[i])));
					FSQWriter.write(',');
					FSQWriter.write(String.valueOf(findTheta34(events[i])));
					FSQWriter.write(',');
					FSQWriter.write('\n');
				}
				FSQWriter.close();
			} catch (IOException e) {
				System.out.println("There was an error in saving the FSQ points");
			}
		}
		
		System.out.println("Data Set " + fileEnd + " completed.");
		
		scan.close();
	}

	//Takes a sequence of angles in [0,π] and returns the average angle
	private static double aveAngle(double[] angles) {
		double aveNormal = 0;
		for (int i = 0; i < angles.length; i ++) {
			aveNormal += angles[i] / angles.length;
		}
		double sigmaNormal = 0;
		for (int i = 0; i < angles.length; i ++) {
			sigmaNormal += Math.pow(aveNormal - angles[i], 2);
			if (angles[i] > Math.PI / 2) {
				angles[i] -= Math.PI;
			}
		}
		
		double aveOffset = 0;
		for (int i = 0; i < angles.length; i ++) {
			aveOffset += angles[i] / angles.length;
		}
		double sigmaOffset = 0;
		for (int i = 0; i < angles.length; i ++) {
			sigmaOffset += Math.pow(aveOffset - angles[i], 2);
		}
		
		if (sigmaOffset < sigmaNormal && aveOffset >= 0) {
			return aveOffset;
		} else if (sigmaOffset < sigmaNormal && aveOffset < 0) {
			return aveOffset + Math.PI;
		}
		return aveNormal;
	}

	private static void plotLog(double min, double max, double[][] values) throws IOException {
		BufferedImage visualizer = new BufferedImage(numTiles, numTiles, BufferedImage.TYPE_INT_RGB);
		double range = Math.log(max) - Math.log(min);
		for (int i = 0; i < numTiles; i ++) {
			for (int j = 0; j < numTiles; j ++) {
				Color pixel = new Color(0.0f, (float) ((Math.log(values[numTiles*i+j][2]) - Math.log(min)) / range), 1.0f);
				visualizer.setRGB(i, j, pixel.getRGB());
			}
		}
		
		//Save generated image
		if (fileNum == ARES) {
			ImageIO.write(visualizer, "png", new File("images/clusterCenterLogAres.png"));
		} else if (fileNum == ABELL) {
			ImageIO.write(visualizer, "png", new File("images/clusterCenterLogA1689.png"));
		} else {
			ImageIO.write(visualizer, "png", new File("images/clusterCenterLog" + fileEnd + ".png"));
		}
	}
	
	private static void plotLinear(double min, double max, double[][] values) throws IOException {
		BufferedImage visualizer = new BufferedImage(numTiles, numTiles, BufferedImage.TYPE_INT_RGB);
		double range = max-min;
		for (int i = 0; i < numTiles; i ++) {
			for (int j = 0; j < numTiles; j ++) {
				Color pixel = new Color(0.0f, (float) ((values[numTiles*i+j][2] - min) / range), 1.0f);
				visualizer.setRGB(i, j, pixel.getRGB());
			}
		}
		
		//Save generated image
		if (fileNum == ARES) {
			ImageIO.write(visualizer, "png", new File("images/clusterCenterAres.png"));
		} else if (fileNum == ABELL) {
			ImageIO.write(visualizer, "png", new File("images/clusterCenterA1689.png"));
		} else {
			ImageIO.write(visualizer, "png", new File("images/clusterCenter" + fileEnd + ".png"));
		}
	}
	
	private static void plotCSV(double[][] values) throws IOException {
		//Determining file name
		FileWriter writer;
		if (fileNum == ARES) {
			writer = new FileWriter("centerData/centerDataAres.csv");
		} else if (fileNum == ABELL) {
			writer = new FileWriter("centerData/centerDataA1689.csv");
		} else if (fileNum == RXJ) {
			writer = new FileWriter("centerData/centerDataRXJ1347.csv");
		} else {
			writer = new FileWriter("centerData/centerData" + fileEnd + ".csv");
		}
		
		//Saving data
		for (int i = 0; i < numTiles*numTiles; i ++) {
			writer.write(String.valueOf(values[i][0]));	//Column 1: x position
			writer.write(',');
			writer.write(String.valueOf(values[i][1]));	//Column 2: y position
			writer.write(',');
			writer.write(String.valueOf(values[i][2]));	//Column 3: cluster center RMS value
			writer.write('\n');
		}
		writer.close();
	}
	
	private static void saveBisector(double bisector, double stddev) {
		double slope = Math.tan(bisector);
		double slopeUp = Math.tan(bisector + stddev);
		double slopeDown = Math.tan(bisector - stddev);
		int lines;
		boolean save = true;
		
		//Determine if data needs to be added to "otherData/slopeData.csv"
		try {
			lines = (int) Files.lines(Paths.get("otherData/slopeData.csv")).count();
			
		} catch (IOException e) {
			lines = 0;
		}
		
		if (lines != 0) {
			try {
				Scanner sloped = new Scanner(slopeData);
				sloped.useDelimiter(",");
				for (int i = 0; i < lines; i ++) {
					int dataID = sloped.nextInt();
					if (dataID == fileNum) save = false;
					sloped.nextLine();
				}
				sloped.close();
			} catch (FileNotFoundException e) {
				
			}
		}
		
		if (save) {
			try {
				//Write data
				FileWriter slopes = new FileWriter(slopeData, true);
				slopes.write(fileEnd);
				slopes.write(',');
				slopes.write(String.valueOf(slope));
				slopes.write(',');
				slopes.write(String.valueOf(slopeUp));
				slopes.write(',');
				slopes.write(String.valueOf(slopeDown));
				slopes.write(',');
				slopes.write(String.valueOf(radius));
				slopes.write(',');
				slopes.write(String.valueOf(stddev));
				slopes.write('\n');
				slopes.close();
				if (debug) System.out.println("Bisector data saved");
			} catch (IOException e) {
			System.out.println("Something went wrong with saving bisector data");
			e.printStackTrace();
			}
		} else {
			if (debug) System.out.println("Slope data recorded previously, delete and re-run program to replace");
		}
	}
	
	private static void saveMinData(double min, double minX, double minY, double error, double size, double angle) {
		int lines;
		boolean save = true;
		
		//Determine if data needs to be added to "otherData/slopeData.csv"
		try {
			lines = (int) Files.lines(Paths.get("otherData/minData.csv")).count();
			
		} catch (IOException e) {
			lines = 0;
		}
		
		if (lines != 0) {
			try {
				Scanner mind = new Scanner(minData);
				mind.useDelimiter(",");
				for (int i = 0; i < lines; i ++) {
					int dataID = mind.nextInt();
					if (dataID == fileNum) save = false;
					mind.nextLine();
				}
				mind.close();
			} catch (FileNotFoundException e) {
				
			}
		}
		
		if (save) {
			try {
				//Write data
				FileWriter mins = new FileWriter(minData, true);
				mins.write(fileEnd);
				mins.write(',');
				mins.write(String.valueOf(min));
				mins.write(',');
				mins.write(String.valueOf(minX));
				mins.write(',');
				mins.write(String.valueOf(minY));
				mins.write(',');
				mins.write(String.valueOf(error));
				mins.write(',');
				mins.write(String.valueOf(size));
				mins.write(',');
				mins.write(String.valueOf(angle));
				mins.write('\n');
				mins.close();
				if (debug) System.out.println("Minimum RMS data saved");
			} catch (IOException e) {
			System.out.println("Something went wrong with saving minimum RMS data");
			e.printStackTrace();
			}
		} else {
			if (debug) System.out.println("Minimum RMS data recorded previously, delete and re-run program to replace");
		}
	}
	
	private static void saveSubset(double[][] centers) {	//Note: Requires centers be an array of size [n][2]
		int lines;
		boolean save = true;
		
		//Determine if data needs to be added to "otherData/subsetData.csv"
		try {
			lines = (int) Files.lines(Paths.get("otherData/subsetData.csv")).count();
			
		} catch (IOException e) {
			lines = 0;
		}
		
		if (lines != 0) {
			try {
				Scanner subsetd = new Scanner(subsetData);
				subsetd.useDelimiter(",");
				for (int i = 0; i < lines; i ++) {
					int dataID = subsetd.nextInt();
					if (dataID == fileNum) save = false;
					subsetd.nextLine();
				}
				subsetd.close();
			} catch (FileNotFoundException e) {
				
			}
		}
		
		if (save) {
			try {
				//Write data
				FileWriter subsets = new FileWriter(subsetData, true);
				subsets.write(fileEnd);
				for (int i = 0; i < centers.length; i ++) {
					subsets.write(',');
					subsets.write(String.valueOf(centers[i][0]));
					subsets.write(',');
					subsets.write(String.valueOf(centers[i][1]));
				}
				subsets.write('\n');
				subsets.close();
				if (debug) System.out.println("Subset data saved");
			} catch (IOException e) {
				System.out.println("Something went wrong with saving subset data");
				e.printStackTrace();
			}
		} else {
			if (debug) System.out.println("Subset data recorded previously, delete and re-run program to replace");
		}
	}
	
	private static void setClusterCenter(double x, double y) {
		for (int i = 0; i < events.length; i ++) {
			events[i].setGalaxyX(x);
			events[i].setGalaxyY(y);
		}
	}
	
	private static double fitTheta23(double theta12, double theta34) { //Gives theta23 from fundamental surface of quads
		double theta23 = p00; //0th order terms
		theta23 += p10*theta12 + p01*theta34; //1st order terms
		theta23 += p20*theta12*theta12 + p11*theta12*theta34 + p02*theta34*theta34; //2nd order terms
		theta23 += p30*theta12*theta12*theta12 + p21*theta12*theta12*theta34; //3rd order terms
		theta23 += p12*theta12*theta34*theta34 + p03*theta34*theta34*theta34; //3rd order terms
		theta23 += p40*theta12*theta12*theta12*theta12 + p31*theta12*theta12*theta12*theta34; //4th order terms
		theta23 += p22*theta12*theta12*theta34*theta34 + p13*theta12*theta34*theta34*theta34; //4th order terms
		theta23 += p04*theta34*theta34*theta34*theta34; //4th order terms
		
		return theta23;
	}
	
	private static double findTheta12(LensEvent event) { //Finds theta12 from data
		double angle1 = event.getAngle(0);
		double angle2 = event.getAngle(1);
		double theta = Math.abs(angle2 - angle1);
		if (theta > Math.PI) theta = Math.PI*2 - theta;
		return theta;
	}
	
	private static double findTheta23(LensEvent event) { //Finds theta23 from data
		double angle2 = event.getAngle(1);
		double angle3 = event.getAngle(2);
		double theta = Math.abs(angle2 - angle3);
		if (theta > Math.PI) theta = Math.PI*2 - theta;
		return theta;
	}
	
	private static double findTheta34(LensEvent event) { //Finds theta34 from data
		double angle3 = event.getAngle(2);
		double angle4 = event.getAngle(3);
		double theta = Math.abs(angle3 - angle4);
		if (theta > Math.PI) theta = Math.PI*2 - theta;
		return theta;
	}
	
	private static double RMS(double[] expected, double[] measured) { //Calculates the root mean square of the given data
		double sum = 0.0;
		for (int i = 0; i < expected.length; i ++) {
			sum += Math.pow(expected[i] - measured[i], 2.0);
		}
		return Math.sqrt(sum / expected.length);
	}
	
	private static double RMS(double expected, double[] measured) { //Calculates the root mean square of the given data
		double sum = 0.0;
		for (int i = 0; i < measured.length; i ++) {
			sum += Math.pow(expected - measured[i], 2.0);
		}
		return Math.sqrt(sum / measured.length);
	}
	
	private static double bisect(double[] center, double[] point1, double[] point2) { //Gives the angle of the bisector
		double angle1 = Math.atan2(point1[1] - center[1], point1[0] - center[0]);
		double angle2 = Math.atan2(point2[1] - center[1], point2[0] - center[0]);
		double angle = (angle1 + angle2) / 2;
		
		while (angle < 0) {
			angle += Math.PI;
		}
		return angle;
	}
	
	private static double[] findCenter(LensEvent[] subset, double aveX, double aveY) { //Returns minimum RMS center for subset of lensing events
		double best = Math.PI*2;
		double[] subsetMin = new double[2];
		
		//Find the RMS values of theta23 for having the galaxy cluster in (numTiles/2)^2 different locations
		int subsetTiles = (int) (radius / stepSize) + 1;
		for (int i = 0; i < subsetTiles; i ++) { //x index
			for (int j = 0; j < subsetTiles; j ++) { //y index	
				//Define cluster center for this iteration
				double centerX = aveX + stepSize * (i - subsetTiles / 2);
				double centerY = aveY + stepSize * (j - subsetTiles / 2);
				setClusterCenter(centerX, centerY);
				
				//Get expected and measured values
				double[] thetaExpct = new double[subset.length];
				double[] thetaMeasd = new double[subset.length];
				for (int k = 0; k < subset.length; k ++) {
					double theta12 = findTheta12(subset[k]);
					double theta34 = findTheta34(subset[k]);
					thetaExpct[k] = fitTheta23(theta12, theta34);
					thetaMeasd[k] = findTheta23(subset[k]);
				}
				double iterationVal = RMS(thetaExpct, thetaMeasd);
				
				//Update minimum location
				if (iterationVal < best) {
					best = iterationVal;
					subsetMin = new double[] {centerX, centerY};
				}
			}
		}
		
		return subsetMin;
	}
	
	private static int[][] getRandomIntsArray(int numInts, int numArrays, int min, int max) { //Returns a two-dimensional array with numArrays rows of length numInts of distinct integers in [min, max)
		if (numInts * numArrays > max-min) return new int[1][1]; //If not enough numbers exist, return an empty 1x1 array containing only 0
		else {
			int[][] sequenceList = new int[numArrays][numInts]; //Array that will be returned
			ArrayList<Integer> choices = new ArrayList<Integer>(); //ArrayList of integers that can be chosen
			for (int i = min; i < max; i ++) { 
				choices.add(i);
			}
			
			//Fills the sequenceList by selecting random integers from the remaining choices
			Random gen = new Random();
			for (int i = 0; i < numArrays; i ++) {
				for (int j = 0; j < numInts; j ++) {
					sequenceList[i][j] = choices.remove(gen.nextInt(choices.size()));
				}
			}
			
			return sequenceList;
		}
	}

	private static int[] getRandomInts(int numInts, int min, int max) {	//Returns a one-dimensional array of length numInts of distinct integers in [min, max)
		Random gen = new Random();
		int[] sequence = new int[numInts];
		for (int i = 0; i < numInts; i ++) {
			boolean reshuffle = false;	//Ensures that integers are distinct from one another
			do {
				reshuffle = false;
				sequence[i] = gen.nextInt(max - min) + min;
				for (int j = 0; j < i; j ++) {
					if (sequence[i] == sequence[j] || numInts >= events.length) reshuffle = true;
				}
			} while (reshuffle);
		}
		return sequence;
	}

	private static double distance(double x1, double y1, double x2, double y2) {	//Computes distance between p1 and p2
		double x = x1 - x2;
		double y = y1 - y2;
		return Math.sqrt(x*x + y*y);
	}
	
	/*==========================================================
	 * Everything past here is a "getXXX" or "setXXX" function
	==========================================================*/
	public int getSource(int index) { //Gives source #
		return index / 5;
	}
	public int getOrder(int index) { //Gives arrival order from source
		return (index % 5) + 1;
	}
	public int getNumOfImages() { //Gives total number of images
		return imageX.length;
	}
}
