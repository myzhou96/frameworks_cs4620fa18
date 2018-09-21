package ray1.shader;

import ray1.shader.Texture;

import java.awt.image.BufferedImage;

import egl.math.Color;
import egl.math.Colorf;
import egl.math.Vector2;

/**
 * A Texture class that treats UV-coordinates outside the [0.0, 1.0] range as if they
 * were at the nearest image boundary.
 * @author eschweic zz335
 *
 */
public class ClampTexture extends Texture {

	public Colorf getTexColor(Vector2 texCoord) {
		if (image == null) {
			System.err.println("Warning: Texture uninitialized!");
			return new Colorf();
		}
				
		//This class uses the nearest pixel for UV-coordinates that are out of range. As a result, 
		//the colors of the boundary pixels of a texture are extended outwards in UV space.
		
		// TODO#A2 Fill in this function.
		// 1) Convert the input texture coordinates to integer pixel coordinates. Adding 0.5
		//    before casting a double to an int gives better nearest-pixel rounding.
		// 2) Clamp the resulting coordinates to the image boundary.
		// 3) Create a Color object based on the pixel coordinate (use Color.fromIntRGB
		//    and the image object from the Texture class), convert it to a Colord, and return it.
		// NOTE: By convention, UV coordinates specify the lower-left corner of the image as the
		//    origin, but the ImageBuffer class specifies the upper-left corner as the origin.
		int height = image.getHeight();
		int width = image.getWidth();
//		System.out.println("clamp u: " + texCoord.x);
//		System.out.println("clamp v: " + texCoord.y);
		double u = texCoord.x *  width + 0.5;
		double v = texCoord.y *  height + 0.5;
		int castU = (int) u;
		int castV = (int) v;
	
		castU = Math.max(0, Math.min(width-1, castU));
		castV = Math.max(1, Math.min(height-1, castV));
//		if(castV =)

		
//		System.out.println("converted clamp U: " + castU);
//		System.out.println("converted clamp v: " + castV);
		Color color = Color.fromIntRGB(image.getRGB(castU, height-castV));
		return new Colorf(color);
	}
	


}
