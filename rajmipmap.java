import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

/**
 * rajmipmap - a simple java RGBA gamma-correct mipmap creator.
 * After a quick google found blog posts, but no code, I did this.
 * Here is some code so someone else doesn't have to spend an evening rewriting this.
 * <p>
 * Code is at https://github.com/rogerallen/rajmipmap
 * <p>
 * Reads in a level 0 RGBA png file, averages the components in a gammma-correct
 * manner and outputs RGBA PNG mipmaps for levels [1,n].  Gamma is currently
 * hard-coded to 2.2, but that is simple enough to change below.
 * <p>
 * Some background (and a handy test image or two):<ul>
 * <li>http://filmicgames.com/archives/327
 * <li>http://http.developer.nvidia.com/GPUGems3/gpugems3_ch24.html
 * </ul>
 * Usage:<br>
 * <code>java rajmipmap base_mipmap.png</code>
 * <p>
 * @author  Roger Allen (rallen@gmail.com)
 * @version 1.0
 * <code>License:
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * See <http://www.gnu.org/licenses/>.
 * </code>
 **/
public class rajmipmap {

    // TODO add this as a command line option
    private static final double GAMMA = 2.2;

    /**
     * The main entry point.  Reads commandline arguments, assumes they are filenames
     * and dispatches them to the make_mipmaps routine.  Files must be png images.
     * @param args   the array of commandline arguments
     */
    public static void main(String[] args)
    {
        if(args.length > 0) {
            for(int i = 0; i < args.length; i++ ) {
                make_mipmaps(args[i]);
            }
        }
    }

    /**
     * Reads in the base mipmap and creates each of the smaller mipmaps.
     * Output filenames are suffixed with "_1", "_2", etc.
     * Handles the non-square image case and should also handle non-pow-2.
     * @param name   the filename of the base image.  must be RGBA format. must end in ".png".
     */
    private static void make_mipmaps(String name)
    {
        BufferedImage base_image, cur_image, next_image;
        System.out.println("make_mipmaps: "+name);
        String base_name = name.replace(".png", "");
        try {
            InputStream in = new FileInputStream(name);
            base_image = ImageIO.read(in);
            cur_image = base_image;
            int width = base_image.getWidth();
            int height = base_image.getHeight();
            width = Math.max(1,width/2);
            height = Math.max(1,height/2);
            int number = 1;
            boolean done = false;
            while( !done ) {
                done = ((width == 1) && (height == 1));
                next_image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                make_a_mipmap(cur_image,next_image);
                String next_image_file_name = base_name+"_"+number+".png";
                File next_image_file = new File(next_image_file_name);
                try {
                    ImageIO.write( next_image, "PNG", next_image_file );
                } catch (IOException ex) {
                    ex.printStackTrace();
                    System.out.println("ERROR causing me to quit");
                    System.exit(0);
                }
                System.out.println("creating "+next_image_file_name+" width="+width+" height="+height);
                cur_image = next_image;
                width = Math.max(1,width/2);
                height = Math.max(1,height/2);
                number += 1;
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            System.out.println("ERROR causing me to quit");
            System.exit(0);
        }
    }

    /**
     * creates 1/2 resolution next_image from full resolution cur_image.
     * @param cur_image   the input RGBA image.  Should be 2x size of next_image.
     * @param next_image  the output RGBA image
     */
    private static void make_a_mipmap(BufferedImage cur_image, BufferedImage next_image)
    {
        for(int y = 0; y < next_image.getHeight(); ++y) {
            for(int x = 0; x < next_image.getWidth(); ++x) {
                int i0,i1,j0,j1;
                i0 = Math.min(cur_image.getWidth()-1, 2*x);
                i1 = Math.min(cur_image.getWidth()-1, 2*x+1);
                j0 = Math.min(cur_image.getHeight()-1, 2*y);
                j1 = Math.min(cur_image.getHeight()-1, 2*y+1);
                int c0 = cur_image.getRGB(i0,j0);
                int c1 = cur_image.getRGB(i0,j1);
                int c2 = cur_image.getRGB(i1,j0);
                int c3 = cur_image.getRGB(i1,j1);
                int c_ave = average_colors(c0,c1,c2,c3);
                next_image.setRGB(x, y, c_ave);
            }
        }
    }

    /**
     * break up the input colors into 8-bit RGBA components and average each before returning.
     * @param c0   RGBA color to average
     * @param c1   RGBA color to average
     * @param c2   RGBA color to average
     * @param c3   RGBA color to average
     * @return     the averaged RGBA color
     */
    private static int average_colors(int c0, int c1, int c2, int c3) {
        int r = gamma_average_component(c0&0xff,       c1&0xff,       c2&0xff,       c3&0xff);
        int g = gamma_average_component((c0>> 8)&0xff, (c1>> 8)&0xff, (c2>> 8)&0xff, (c3>> 8)&0xff)<<8;
        int b = gamma_average_component((c0>>16)&0xff, (c1>>16)&0xff, (c2>>16)&0xff, (c3>>16)&0xff)<<16;
        // alpha is linear and does not need gamma-correction
        int a = average_component((c0>>24)&0xff, (c1>>24)&0xff, (c2>>24)&0xff, (c3>>24)&0xff)<<24;
        int c_ave = r + g + b + a;
        return c_ave;
    }

    /**
     * Gamma-correct averaging of each of the input components.
     * @param i   8-bit gamma-encoded color component
     * @param j   8-bit gamma-encoded color component
     * @param k   8-bit gamma-encoded color component
     * @param l   8-bit gamma-encoded color component
     * @return    8-bit gamma-correct averaged gamma-encoded color component
     */
    private static int gamma_average_component(int i, int j, int k, int l) {
        // convert i,j,k,l components from gamma-correct to linear
        double id = i/255.0, jd = j/255.0, kd = k/255.0, ld = l/255.0;
        id = Math.pow(id, GAMMA); jd = Math.pow(jd, GAMMA); kd = Math.pow(kd, GAMMA); ld = Math.pow(ld, GAMMA);
        // average the linear components
        double c_ave_d = (id+jd+kd+ld)/4;
        // convert linear back to gamma-correct
        c_ave_d = Math.pow(c_ave_d,1.0/GAMMA);
        // and make sure it returns 0-255.
        int c_ave = Math.max(0,(int)Math.min(255, 255*c_ave_d));
        return c_ave;
    }

    /**
     * averaging of each of the input components in linear space.
     * @param i   8-bit linear color component
     * @param j   8-bit linear color component
     * @param k   8-bit linear color component
     * @param l   8-bit linear color component
     * @return    8-bit averaged linear color component
     */
    private static int average_component(int i, int j, int k, int l) {
        int c_ave = (i+j+k+l)/4;
        c_ave = Math.max(0,(int)Math.min(255, c_ave));
        return c_ave;
    }

}
