/*
 * This contains all the CSS functions definitions that handle color duties
 * 
 * David Boyd
 */
package com.baobei.css.model;

import java.util.Vector;

public class Color {
    // Returns a lighter or darker color for "rgb" value that can be used to show disabled text states
    // 
    // Usage:
    //  
    //   color: saturation(#ffffff, 40%)
    // 
    // Expecting vector with two RuleValue arguments:
    //    
    //   - color rgb (ie. #303030)
    //   - percent saturation (ie. 30%)
    //
    public static int saturation(Vector<RuleValue> args) throws Exception {
        int[] hsb = new int[3];
        int percentBrightness = 0;

        if (args.size() != 2)
            throw new Exception("Incorrect # of arguments for saturation()");

        int rgb = args.elementAt(0).getColor();
        int percentSaturation = args.elementAt(1).getInt();

        Color.getHSB(rgb, hsb);

        if (percentSaturation != 0) {
            hsb[1] = hsb[1] - ((255 * percentSaturation) / 100);

            if (hsb[1] < 0)
                hsb[1] = 0;

            percentBrightness = percentSaturation;
        }

        if (percentBrightness != 0) {
            hsb[2] = hsb[2] - ((255 * percentBrightness) / 100);

            if (hsb[2] < 0)
                hsb[2] = 0;
        }

        return Color.getRGB(hsb);
    }

    // Returns an HSB value for a given RGB input
    private static void getHSB(int rgb, int[] hsb) {
        int red, green, blue; // , alpha;
        int hue, saturation, brightness;
        int min, max, delta;

        // -----------------------------------
        // Convert to HSV (same as HSB)
        // -----------------------------------
        blue = rgb & 0x000000ff;
        green = (rgb & 0x0000ff00) >> 8;
        red = (rgb & 0x00ff0000) >> 16;
        //alpha = (color & 0xff000000) >> 24;

        //System.err.println("----  pixel : " + i);
        //System.err.println("red: " + red + ", green: " + green + ", blue: " + blue);

        if (red > green) {
            max = (red > blue ? red : blue);
            min = (green < blue ? green : blue);
        } else {
            max = (green > blue ? green : blue);
            min = (red < blue ? red : blue);
        }

        delta = max - min;

        // Brightness is the maximum of RGB
        brightness = max;

        // Saturation is the variance from min to max
        saturation = (max != 0) ? (delta * 255 / max) : 0;

        // Hue is based on which quadrant the colour is in
        if (saturation == 0)
            hue = 0;
        else if (red == max)
            hue = ((green - blue) * 255) / delta;
        else if (green == max)
            hue = 512 + (((blue - red) * 255) / delta);
        else
            hue = 1024 + (((red - green) * 255) / delta);

        if (hue < 0)
            hue += 1536;

        hue = hue / 6;

        hsb[0] = hue;
        hsb[1] = saturation;
        hsb[2] = brightness;

        return;
    }

    // Returns an RGB value for a given HSB input
    private static int getRGB(int[] hsb) {
        int red = 0, green = 0, blue = 0;
        int hue, saturation, brightness;
        int m1, m2, m3, quad, fract;

        hue = hsb[0];
        saturation = hsb[1];
        brightness = hsb[2];

        if (brightness < 0)
            brightness = 0;
        else if (brightness > 255)
            brightness = 255;

        // System.err.println("hue: " + hue + ", sat: " + saturation + ", brightness: " + brightness);

        // Find out which quadrant (well, hextant actually) we are in
        quad = (hue * 6) >>> 8;

        // Now get the fraction of the quadrant
        fract = (hue * 6) & 255;

        m1 = (brightness * (255 - saturation)) / 255;
        m2 = (brightness * (255 - ((saturation * fract) >> 8))) / 255;
        m3 = (brightness * (255 - ((saturation * (256 - fract)) >> 8))) / 255;

        switch (quad) {
            case 0:
                red = brightness;
                green = m3;
                blue = m1;
                break;

            case 1:
                red = m2;
                green = brightness;
                blue = m1;
                break;

            case 2:
                red = m1;
                green = brightness;
                blue = m3;
                break;

            case 3:
                red = m1;
                green = m2;
                blue = brightness;
                break;

            case 4:
                red = m3;
                green = m1;
                blue = brightness;
                break;

            case 5:
                red = brightness;
                green = m1;
                blue = m2;
                break;
        }

        // System.err.println("red: " + red + ", green: " + green + ", blue: " + blue);

        return ((red << 16) + (green << 8) + blue);
    }
}