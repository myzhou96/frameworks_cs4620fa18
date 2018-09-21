package ray1.shader;

import ray1.shader.Texture;
import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector2;

/**
 * A Texture class that repeats the texture image as necessary for UV-coordinates
 * outside the [0.0, 1.0] range.
 * 
 * @author eschweic zz335
 *
 */
public class RepeatTexture extends Texture {

	public Colorf getTexColor(Vector2 texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colorf();
		}
				
		// TODO#A2 Fill in this function.
		// 1) Convert the input texture coordinates to integer pixel coordinates. Adding 0.5
		//    before casting a double to an int gives better nearest-pixel rounding.
		// 2) If these coordinates are outside the image boundaries, modify them to read from
		//    the correct pixel on the image to give a repeating-tile effect.
		// 3) Create a Color object based on the pixel coordinate (use Color.fromIntRGB
		//    and the image object from the Texture class), convert it to a Colorf, and return it.
		// NOTE: By convention, UV coordinates specify the lower-left corner of the image as the
		//    origin, but the ImageBuffer class specifies the upper-left corner as the origin.
		int height = image.getHeight();
		int width = image.getWidth();
//		System.out.println("repeat u: " + texCoord.x);
//		System.out.println("repeat v: " + texCoord.y);
		double u = texCoord.x *  width + 0.5;
		double v = texCoord.y *  height + 0.5;
		int castU = (int) u;
		int castV = (int) v;
		
		castU = castU % width;
		castV = castV % height;
		if(castV == 0) castV = 1;
//		System.out.println("converted repeat U: " + castU);
//		System.out.println("converted repeat V: " + castV);
		
		Color color = Color.fromIntRGB(image.getRGB(castU, height-castV));
		return new Colorf(color);
	}

}
