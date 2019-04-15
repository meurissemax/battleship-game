import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Map;
import java.util.HashMap;
import java.util.Base64;

/**
 * This class is used to deal with all the images of the game.
 * All images are compressed.
 *
 * @author Maxime Meurisse & Valentin Vermeylen
 * @version 2019.04.20
 */

public class ImageHandler {
	private Map<String, String> images;

	public ImageHandler() {
		images = new HashMap<String, String>();

		images.put("water", "data:image/png;base64," + base64encoder("src/resources/png/water.png"));
		images.put("miss", "data:image/png;base64," + base64encoder("src/resources/png/miss.png"));
		images.put("hit", "data:image/png;base64," + base64encoder("src/resources/png/hit.png"));

		images.put("favicon", "data:image/png;base64," + base64encoder("src/resources/png/favicon.png"));
		images.put("apple-touch-icon", "data:image/png;base64," + base64encoder("src/resources/png/apple-touch-icon.png"));
	}

	public String getImage(String name) {
		return images.get(name);
	}

	/**
	 * This function is used to perform a base 64 encoding on a file.
	 *
	 * @param filepath the path of the file
	 *
	 * @return the compressed file
	 */
	private String base64encoder(String filepath) {
		String base64Image = "";
		File file = new File(filepath);

		try(FileInputStream imageInFile = new FileInputStream(file)) {
			byte imageData[] = new byte[(int) file.length()];

			imageInFile.read(imageData);
			base64Image = Base64.getEncoder().encodeToString(imageData);
		} catch(FileNotFoundException fnfe) {
			System.err.println("ImageHandler : file not found.");
		} catch(IOException ioe) {
			System.err.println("ImageHandler : unable to read file.");
		} catch(Exception e) {
			System.err.println(e.getMessage());
		}

		return base64Image;
	}
}
