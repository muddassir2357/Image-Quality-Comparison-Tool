package application;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;

public class AppController {

	@FXML
	private ImageView imageFrame1;

	@FXML
	private TextField path1;

	@FXML
	private Button uploadImage1;

	@FXML
	private ImageView imageFrame2;

	@FXML
	private TextField path2;

	@FXML
	private Button uploadImage2;

	@FXML
	private ImageView imageFrame3;

	@FXML
	private TextField path3;

	@FXML
	private Button uploadImage3;

	@FXML
	private TextArea resultArea1;

	@FXML
	private TextArea resultArea2;

	@FXML
	private TextArea resultArea3;

	@FXML
	private TextField errorThreshold;

	@FXML
	public void imageUploadHandler(ActionEvent event) {

		String path = null;
		Button pressedButton = (Button) event.getSource();
		String pressedButtonId = pressedButton.getId();
		FileChooser fileChoser = new FileChooser();
		fileChoser.setTitle("Select Image");
		fileChoser.setInitialDirectory(new File(System.getProperty("user.dir")));
		File file=null;

		switch (pressedButtonId) {
		case "uploadButton1":
			file =fileChoser.showOpenDialog(null);
			path=file.getPath();
			this.path1.setText(path);
			path = "file:" + path;
			uploadImage(path, imageFrame1);
			break;
		case "uploadButton2":
			file =fileChoser.showOpenDialog(null);
			path=file.getPath();
			this.path2.setText(path);
			path = "file:" + path;
			uploadImage(path, imageFrame2);
			break;
		case "uploadButton3":
			file =fileChoser.showOpenDialog(null);
			path=file.getPath();
			this.path2.setText(path);
			path = "file:" + path;
			uploadImage(path, imageFrame3);
			break;
		}

	}

	@FXML
	public void compareImageHandler(ActionEvent event) {

		List<String> pathUrl = getpathUrls();
		int i = 0;
		for (String imagePath : pathUrl) {
			double[] BGRError;
			i++;
			try {
				BGRError = findBGRError(imagePath);
				if (i == 1)
					this.resultArea2.setText("Blue Error: " + BGRError[0] + "%\nGreen Error: " + BGRError[1]
							+ "%\nRed Error: " + BGRError[2] + "%");
				else
					this.resultArea3.setText("Blue Error: " + BGRError[0] + "%\nGreen Error: " + BGRError[1]
							+ "%\nRed Error: " + BGRError[2] + "%");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	private List<String> getpathUrls() {
		List<String> pathUrl = new ArrayList<String>();
		if (!path2.getText().equals(""))
			pathUrl.add(path2.getText());
		if (!path3.getText().equals(""))
			pathUrl.add(path3.getText());

		return pathUrl;
	}

	private double[] findBGRError(String path) throws Exception {

		Mat originalImage = Imgcodecs.imread(path1.getText());
		Mat ditheredImage = Imgcodecs.imread(path);

		int imageRows = originalImage.height();
		int imageCols = originalImage.width();

		double[] origavgTuple = new double[3];
		double[] dithavgTuple = new double[3];
		double[] errorTuple = new double[3];
		double[] errorPercentTuple;
		int quantum = calculateQuantum(originalImage);
		int step = quantum / 5;
		int frameCounter = 0;

		for (int row = 0; row < imageRows - quantum; row += step) {
			for (int col = 0; col < imageCols - quantum; col += step) {

				frameCounter++;
				Frame originalFrame = new Frame(originalImage);
				Frame ditheredFrame = new Frame(ditheredImage);
				originalFrame.initialize(row, col, quantum);
				ditheredFrame.initialize(row, col, quantum);
				origavgTuple = originalFrame.getAverageTuple();
				dithavgTuple = ditheredFrame.getAverageTuple();
				double[] currentErrorTuple = calculateError(origavgTuple, dithavgTuple);
				errorTuple[0] += currentErrorTuple[0];
				errorTuple[1] += currentErrorTuple[1];
				errorTuple[2] += currentErrorTuple[2];
				if (faultyFrame(currentErrorTuple)) {
					ditheredFrame.highlightFrame();
				}

			}
		}

		errorPercentTuple = calculateErrorPercent(errorTuple, frameCounter);

		return errorPercentTuple;
	}

	private int calculateQuantum(Mat image) {
		int length = image.rows();
		int breadth = image.cols();
		long imageSizeInPixel = length * breadth;

		if (imageSizeInPixel < 179200)
			return 30;
		else if (imageSizeInPixel < 786432)
			return 40;
		else if (imageSizeInPixel < 995328)
			return 50;
		else
			return 100;
	}

	private boolean faultyFrame(double[] currentErrorTuple) {
		double mean = (currentErrorTuple[0] + currentErrorTuple[1] + currentErrorTuple[2]) / 3;
		int threshold = 30;
		try {
			threshold = Integer.parseInt(this.errorThreshold.getText());
		} catch (Exception e) {
			threshold=30;
			e.printStackTrace();
		}
		if (mean > threshold) {
			System.out.println("Faulty frame with Mean: " + mean);
			return true;
		}
		return false;
	}

	private double[] calculateErrorPercent(double[] errorTuple, int frameCounter) {
		double[] errorPercentTuple = new double[3];
		errorPercentTuple[0] = (errorTuple[0] * 100) / (255 * frameCounter);
		errorPercentTuple[1] = (errorTuple[1] * 100) / (255 * frameCounter);
		errorPercentTuple[2] = (errorTuple[2] * 100) / (255 * frameCounter);
		return errorPercentTuple;
	}

	private double[] calculateError(double[] origavgTuple, double[] dithavgTuple) {
		double[] errorTuple = new double[3];
		errorTuple[0] = Math.abs(origavgTuple[0] - dithavgTuple[0]);
		errorTuple[1] = Math.abs(origavgTuple[1] - dithavgTuple[1]);
		errorTuple[2] = Math.abs(origavgTuple[2] - dithavgTuple[2]);
		return errorTuple;
	}

	/**
	 * Update the {@link ImageView} in the JavaFX main thread
	 * 
	 * @param view
	 *            the {@link ImageView} to update
	 * @param image
	 *            the {@link Image} to show
	 */
	private void updateImageView(ImageView view, Image image) {
		Utils.onFXThread(view.imageProperty(), image);
	}

	private void uploadImage(String path, ImageView frame) {

		Image image = new Image(path);
		frame.setImage(image);
	}

}
