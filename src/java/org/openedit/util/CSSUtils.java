/*
 * Created on Jul 29, 2025
 */
package org.openedit.util;


/**
 * @author sxakil
 *
 */
public class CSSUtils
{
	public static int[] hexToRgb(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new int[]{r, g, b};
    }

    public static String rgbToHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X", r, g, b);
    }

    public static float[] rgbToHsl(int r, int g, int b) {
        float rf = r / 255f, gf = g / 255f, bf = b / 255f;
        float max = Math.max(rf, Math.max(gf, bf));
        float min = Math.min(rf, Math.min(gf, bf));
        float h = 0, s, l = (max + min) / 2;

        if (max == min) {
            h = s = 0; // achromatic
        } else {
            float d = max - min;
            s = l > 0.5 ? d / (2 - max - min) : d / (max + min);

            if (max == rf) {
                h = (gf - bf) / d + (gf < bf ? 6 : 0);
            } else if (max == gf) {
                h = (bf - rf) / d + 2;
            } else {
                h = (rf - gf) / d + 4;
            }

            h /= 6;
        }

        return new float[]{h * 360, s * 100, l * 100};
    }

    public static int[] hslToRgb(float h, float s, float l) {
        h /= 360;
        s /= 100;
        l /= 100;

        float r, g, b;

        if (s == 0) {
            r = g = b = l; // achromatic
        } else {
            float q = l < 0.5 ? l * (1 + s) : l + s - l * s;
            float p = 2 * l - q;
            r = hueToRgb(p, q, h + 1f / 3f);
            g = hueToRgb(p, q, h);
            b = hueToRgb(p, q, h - 1f / 3f);
        }

        return new int[]{
            Math.round(r * 255),
            Math.round(g * 255),
            Math.round(b * 255)
        };
    }

    private static float hueToRgb(float p, float q, float t) {
        if (t < 0) t += 1;
        if (t > 1) t -= 1;
        if (t < 1f / 6f) return p + (q - p) * 6 * t;
        if (t < 1f / 2f) return q;
        if (t < 2f / 3f) return p + (q - p) * (2f / 3f - t) * 6;
        return p;
    }

    public static String lightenColor(String hex, float percent) {
        return adjustLightness(hex, percent);
    }

    public static String darkenColor(String hex, float percent) {
        return adjustLightness(hex, -percent);
    }

    private static String adjustLightness(String hex, float percent) {
        int[] rgb = hexToRgb(hex);
        float[] hsl = rgbToHsl(rgb[0], rgb[1], rgb[2]);
        
        if(hsl[2] + percent > 100 || hsl[2] + percent < 0)
        {
        	percent *= -1;
        }

        hsl[2] = clamp(hsl[2] + percent, 0, 100); // Adjust lightness
        int[] adjustedRgb = hslToRgb(hsl[0], hsl[1], hsl[2]);

        return rgbToHex(adjustedRgb[0], adjustedRgb[1], adjustedRgb[2]);
    }

    private static float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }
    
    public static String normalizeHex(String hex) {
        if (hex == null) return null;

        hex = hex.trim().replace("#", "");

        // Expand 3-digit hex to 6-digit
        if (hex.length() == 3) {
            hex = "" + hex.charAt(0) + hex.charAt(0)
                    + hex.charAt(1) + hex.charAt(1)
                    + hex.charAt(2) + hex.charAt(2);
        }

        // Validate 6-digit hex
        if (hex.matches("^[0-9a-fA-F]{6}$")) {
            return "#" + hex.toUpperCase();
        } else {
            return null; // Invalid
        }
    }

    public static double getLuminance(int[] rgb) {
    	double[] srgb = {0, 0, 0};
    	for (int i = 0; i < rgb.length; i++)
    	{
    		
    		float c = rgb[i] / 255;
    		srgb[i] = c <= 0.03928 ? c / 12.92 : Math.pow((c + 0.055) / 1.055, 2.4);
    	}
    	return 0.2126 * srgb[0] + 0.7152 * srgb[1] + 0.0722 * srgb[2];

    }

    public static String makeGradient(String color1, String angle) {
        String color2 = lightenColor(color1, 15);
        String gradient = "linear-gradient(" + angle + "deg, " + color1 + " 40%, " + color2 + " 100%)";
        return gradient;
    }

    public static String makeGradient(String color1) {
        return makeGradient(color1, "45");
    }

    public static String makeGradientHover(String color1, String angle) {
        String color2 = lightenColor(color1, 15);
        String gradient = "linear-gradient(" + angle + "deg, " + color1 + " 50%, " + color2 + " 100%)";
        return gradient;
    }

    public static String makeGradientHover(String color1) {
        return makeGradientHover(color1, "45");
    }

    public static String makeContrast(String hex) {
//        hex = normalizeHex(hex);
//        if (hex == null) return null;
//        
//        int[] rgb = hexToRgb(hex);
//        double luminance = getLuminance(rgb);
//
//        // Try increasing lightness for light text
//        for (let testL = 100; testL >= 0; testL -= 1) {
//          const testRgb = hslToRgb(h, s, testL);
//          const testLum = getLuminance(testRgb);
//          const cr = contrastRatio(bgLum, testLum);
//          if (cr >= requiredContrast) {
//            return `hsl(${h}, ${s}%, ${testL}%)`;
//          }
//        }
//
//        // As fallback, just return white
//        return `hsl(${h}, ${s}%, ${l > 50 ? 0 : 100}%)`;

        int[] rgb = hexToRgb(hex);

        float threshold = rgb[0] * 0.299f + rgb[1] * 0.587f + rgb[2] * 0.114f;

        return threshold > 186f ? "#252525" : "#FFFFFF";

    }

    public static String makeHover(String hex) {
        hex = normalizeHex(hex);
        if (hex == null) return null;

        return lightenColor(hex, 10);
    }
}
