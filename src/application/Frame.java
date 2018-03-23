package application;

import java.util.HashMap;
import java.util.Map;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

public class Frame {

	private int rows;
	private int cols;
	private double[] avgTuple;
	private Mat image;
	private int firstRow,firstCol;
	private int frameSize;

	public int getFirstRow() {
		return firstRow;
	}

	public void setFirstRow(int firstRow) {
		this.firstRow = firstRow;
	}

	public int getFirstCol() {
		return firstCol;
	}

	public void setFirstCol(int firstCol) {
		this.firstCol = firstCol;
	}

	public Frame(Mat image) {
		this.image = image;
		rows = image.rows();
		cols = image.cols();
	}

	public int getRows() {
		return rows;
	}

	public void setRows(int rows) {
		this.rows = rows;
	}

	public int getCols() {
		return cols;
	}

	public void setCols(int cols) {
		this.cols = cols;
	}

	public Mat getImage() {
		return image;
	}

	public void setImage(Mat image) {
		this.image = image;
	}

	public double[] getAverageTuple() throws Exception {
		if (avgTuple == null)
			return calculateAverage();
		else
			return avgTuple;
	}

	// return in BGR format
	public double[] calculateAverage() throws Exception {

		if (image == null)
			throw new Exception("Frame has no associated image!");

		avgTuple = new double[3];
		for (int row = firstRow; row < rows; row++) {
			for (int col = firstCol; col < cols; col++) {
				double[] currentTuple = image.get(row, col);
				avgTuple[0] += currentTuple[0];
				avgTuple[1] += currentTuple[1];
				avgTuple[2] += currentTuple[2];
			}
		}
		avgTuple[0] = avgTuple[0] / (frameSize);
		avgTuple[1] = avgTuple[1] / (frameSize);
		avgTuple[2] = avgTuple[2] / (frameSize);

		return avgTuple;
	}
	
	public void initialize(int firstRow,int firstCol,int quantum) {
		this.firstRow=firstRow;
		this.firstCol=firstCol;
		this.rows=firstRow+quantum;
		this.cols=firstCol+quantum;
		this.frameSize=quantum*quantum;
	}

	public void highlightFrame() {
		Map<String,Integer> point = new HashMap<String,Integer>();
		point.put("X", this.firstRow);
		point.put("Y",this.firstCol);
		colorTheBoundary(point,new HashMap<String,Integer>(point));
		
	}

	private void colorTheBoundary(Map<String, Integer> point1, Map<String, Integer> point2) {
		int x1 = point1.get("X");
		int y1 = point1.get("Y");
		
		for(;y1<=this.cols;y1++) {
			image.put(x1, y1, 0,0,0);
		}
		point1.put("Y", y1);
		int x2 = point2.get("X");
		int y2 = point2.get("Y");
		
		for(;x2<=this.rows;x2++) {
			image.put(x2, y2, 0,0,0);
		}
		point2.put("X", x2);
		
		if(x1!=x2 && y1!=y2)
			colorTheBoundary(point2, point1);
		Imgcodecs.imwrite("C:\\Users\\fazal\\Desktop\\Data\\AlgorithmComparison\\iqct_output\\output.jpg", image);
		
	}
}
