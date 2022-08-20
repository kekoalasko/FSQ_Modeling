package util;

import java.util.ArrayList;

public class LensEvent {
	private int[] images; //Stores the indices of images in the event
	private double[] imageX; //Position on x-axis for respective image
	private double[] imageY; //Position on y-axis for respective image
	private double galaxyX; //Position on x-axis for lensing object
	private double galaxyY; //Position on y-axis for lensing object
	private int galaxy; //Stores the lensing galaxy index
	private int[] foci; //Images to be analyzed
	private int center; //Image closest to galaxy; 
	
	//Constructor for events with arbitrarily many images
	public LensEvent(ArrayList<Integer> imgs) {
		images = new int[imgs.size()];
		for (int i = 0; i < imgs.size(); i ++) {
			images[i] = imgs.get(i);
		}
	}
	
	//Constructor for events with fixed number of images
	public LensEvent(int[] imgs) {
		images = imgs;
	}
	
	//1 means input is contained within this LensEvent
	//0 means LensEvents are unrelated
	//-1 means this LensEvent is contained within input
	public int compareTo(LensEvent input) {
		if (input.getNumOfImages() <= getNumOfImages()) {
			int counter = 0;
			for (int i = 0; i < input.getNumOfImages(); i ++) {
				for (int j = 0; j < getNumOfImages(); j ++) {
					if (input.getImage(i) == images[j]) {
						counter ++;
						j = getNumOfImages();
					}
				}
			}
			if (counter == input.getNumOfImages()) {
				return 1;
			}
		} else {
			int counter = 0;
			for (int i = 0; i < getNumOfImages(); i ++) {
				for (int j = 0; j < input.getNumOfImages(); j ++) {
					if (input.getImage(j) == images[i]) {
						counter ++;
						j = input.getNumOfImages();
					}
				}
			}
			if (counter == getNumOfImages()) {
				return -1;
			}
		}
		
		return 0;
	}
	
	//Removes a given image index from images
	public void removeImage(int img) {
		int[] temp = images;
		images = new int[temp.length - 1];
		
		for (int i = 0; i < images.length; i ++) {
			if (i < img) {
				images[i] = temp[i];
			} else {
				images[i] = temp[i+1];
			}
		}
	}
	
	//Guesses at and assigns the center image
	public int findCenter() {
		//TODO: Special cases need to work without this manual interference
		if (images[0] / 5 == 40) {
			return 0;
		}
		
		//Gets average coordinate values
		double aveX = 0.0;
		double aveY = 0.0;
		for (int i = 0; i < getNumOfImages(); i ++) {
			aveX += imageX[i] / getNumOfImages();
			aveY += imageY[i] / getNumOfImages();
		}
		
		//Finds image closest to average
		double highest = 500.0;
		int best = -1;
		for (int i = 0; i < getNumOfImages(); i ++) {
			double score = Math.sqrt(Math.pow(imageX[i] - aveX, 2.0) + Math.pow(imageY[i] - aveY, 2.0));
			if (score < highest) {
				highest = score;
				best = i;
			}
		}
		
		//Return this only if no center is found
		if (best == -1) return -1;
		
		//Set center image
		setCenter(best);
		return images[best];
	}
	
	//Gives angle made between the horizontal and the line between given image and lensing object
	public double getAngle(int index) {
		return Math.atan2(imageY[index] - galaxyY, imageX[index] - galaxyX);
	}
	
	/*==========================================================
	 * Everything past here is a "getXXX" or "setXXX" function
	==========================================================*/
	public void setGalaxy(int gal) {
		galaxy = gal;
	}
	public void setFoci(int img1, int img2) {
		foci = new int[] {img1, img2};
	}
	public void setCenter(int img) {
		center = img;
	}
	public void setImageX(double[] imgX) {
		imageX = imgX;
	}
	public void setImageY(double[] imgY) {
		imageY = imgY;
	}
	public void setGalaxyX(double galX) {
		galaxyX = galX;
	}
	public void setGalaxyY(double galY) {
		galaxyY = galY;
	}
	
	public int getNumOfImages() {
		return images.length;
	}
	public int getImage(int i) {
		return images[i];
	}
	public int[] getImages() {
		return images;
	}
	public int getGalaxy() {
		return galaxy;
	}
	public int[] getFoci() {
		return foci;
	}
	public int getCenter() {
		return center;
	}
	public double[] getXs() {
		return imageX;
	}
	public double[] getYs() {
		return imageY;
	}
	public double getX(int index) { //Gives the x-coordinate of the nth image in this event
		return imageX[index];
	}
	public double getY(int index) { //Gives the y-coordinate of the nth image in this event
		return imageY[index];
	}
}