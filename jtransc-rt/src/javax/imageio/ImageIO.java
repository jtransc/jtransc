package javax.imageio;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.OutputStream;

public class ImageIO {
	native public static boolean write(RenderedImage im, String formatName, OutputStream output) throws IOException;
}
