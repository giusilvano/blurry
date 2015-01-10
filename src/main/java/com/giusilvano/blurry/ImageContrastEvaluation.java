package com.giusilvano.blurry;

import org.apache.commons.math3.util.FastMath;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;

/**
 * Contains methods to evaluate images' contrast and assign a score.
 *
 * <p>Some inspirational references:
 * <ul>
 * <li>http://www.cardinalpeak.com/blog/detecting-well-focused-images/
 * <li>http://en.wikipedia.org/wiki/Autofocus#Contrast_detection
 * <li>http://nikonrumors.com/2014/10/20/nikon-confirms-the-51-af-points-in-the-d750-are-narrower-compared-to-the-d810.aspx/
 * <li>http://www.dpreview.com/forums/post/54369183
 * <li>http://www.dpreview.com/forums/post/54386830
 * <li>http://en.wikipedia.org/wiki/Contrast_%28vision%29
 * </ul>
 */
public class ImageContrastEvaluation {

    // Cache all the luminance values for the RGB space
    private static final double[][][] luminance;

    /**
     * Exception thrown when methods of this class can't analyze a certain image type.
     *
     * <p>In this moment, the only image type supported is 3-bytes BGR.
     */
    public static class UnsupportedImageTypeException extends Exception {}

    static {
        // Luminance calculated according to the W3C rules:
        // http://www.w3.org/TR/2008/REC-WCAG20-20081211/#relativeluminancedef

        final double[] rCache = new double[256];
        final double[] gCache = new double[256];
        final double[] bCache = new double[256];

        for (int i = 0; i <= 255; i++) {
            double value = i / 255d;
            // Apply gamma compensation, according W3C rules.
            // Explanation (but with different compensation values): http://stackoverflow.com/a/13558570
            if (value <= 0.03928d) {
                value = value / 12.92d;
            } else {
                value = FastMath.pow((value + 0.055d) / 1.055d, 2.4d);
            }
            rCache[i] = value * 0.2126d;
            gCache[i] = value * 0.7152d;
            bCache[i] = value * 0.0722d;
        }

        // Store in cache all the values for faster computations
        luminance  = new double[256][256][256];
        for (int r = 0; r <= 255; r++) {
            for (int g = 0; g <= 255; g++) {
                for (int b = 0; b <= 255; b++) {
                    luminance[r][g][b] = rCache[r] + gCache[g] + bCache[b];
                }
            }
        }
    }

    /**
     * Computes all the pixels luminance differences and returns the sum of them.
     *
     * @param bytes array of the bytes describing the image
     * @param startIndex index of the first byte of the first pixel to analyze
     * @param indexIncrement what is needed to jump to the next pixel position in the bytes[] array
     * @param stopIndex the index where the method must stop
     * @return the sum of all the pixels luminance differences found in bytes[] array from startIndex to stopIndex
     */
    private static double getPixelLineContrastsSum(byte[] bytes, int startIndex, int indexIncrement, int stopIndex) {
        // Following code is a bit ugly to keep everything as fastest and lightest possible
        int index = startIndex;
        // Take the luminance of the first pixel
        double prevLuminance = luminance[bytes[index + 2] & 0xFF][bytes[index + 1] & 0xFF][bytes[index] & 0xFF];
        double sum = 0;
        // Jump to the next pixel
        index += indexIncrement;

        // For all other pixels, compute the luminance difference with the previous and add it to the final sum
        for (; index <= stopIndex; index += indexIncrement) {
            final double curLuminance = luminance[bytes[index + 2] & 0xFF][bytes[index + 1] & 0xFF][bytes[index] & 0xFF];
            sum += FastMath.abs(curLuminance - prevLuminance);
            prevLuminance = curLuminance;
        }
        return sum;
    }

    /**
     * Computes the contrast score for the provided image.
     *
     * <p>This method will choose a grid of  rows and columns to scan, according to the sampleCoverage parameter. Then
     * he will scan every pixel in each of these rows and columns, computing the luminance difference between each pixel
     * and its previous. At the end, the average of all these luminance differences will represent the contrast score of
     * the image.
     *
     * @param bufferedImage the image to process
     * @param sampleCoverage with 1 the method will scan all rows and columns in the image, with less the method will
     *                       reduce proportionally the number of rows/columns involved, distributing them uniformly
     *                       to cover all the image area
     * @return a number between 0 and 1 that expresses the contrast score
     * @throws UnsupportedImageTypeException if bufferedImage type is not 3-byte BGR
     */
    public static double getImageContrastScore(BufferedImage bufferedImage, float sampleCoverage) throws UnsupportedImageTypeException {
        // Thanks to: http://stackoverflow.com/a/9470843
        final byte[] bytes = ((DataBufferByte) bufferedImage.getRaster().getDataBuffer()).getData();
        if (bufferedImage.getType() != BufferedImage.TYPE_3BYTE_BGR) {
            // Maybe in future other image types will be supported
            throw new UnsupportedImageTypeException();
        }
        final int bytesPerPixel = 3;
        final int width = bufferedImage.getWidth();
        final int height = bufferedImage.getHeight();

        // The image will be divided in stripes, according to the sample coverage
        int horizontalStripesCount = (int)(height * sampleCoverage);
        // If image is too small make 100 stripes by default; if the image is even smaller, make a stripe for each pixel
        if (horizontalStripesCount == 0) horizontalStripesCount = FastMath.min(height, 100);
        final int horizontalStripesSize = height / horizontalStripesCount;

        int verticalStripesCount = (int)(width * sampleCoverage);
        if (verticalStripesCount == 0) verticalStripesCount = FastMath.min(width, 100);
        final int verticalStripesSize = width / verticalStripesCount;

        // Store the sum of all the pixels luminance differences
        double sum = 0;

        // For each horizontal stripe, take the center row of pixels and process it
        for (int i = 0; i < horizontalStripesCount; i++) {
            // The Y coord of the center row of pixels in this stripe
            final int centerPixelsRowY = (horizontalStripesSize * i) + (horizontalStripesSize / 2);
            // Starting index for the first pixel
            final int startIndex = centerPixelsRowY * width * bytesPerPixel;
            // What is needed to jump to the next pixel in the row
            final int indexIncrement = bytesPerPixel;
            // Stop when all pixels in this row are done
            final int stopIndex = startIndex + (width-1) * indexIncrement;
            sum += getPixelLineContrastsSum(bytes, startIndex, indexIncrement, stopIndex);
        }

        // For each vertical stripe, take the center column of pixels and process it
        for (int i = 0; i < verticalStripesCount; i++) {
            // The X coord of the center column of pixels in this stripe
            final int centerPixelsColX = (verticalStripesSize * i) + (verticalStripesSize / 2);
            // Starting index for the first pixel
            final int startIndex = centerPixelsColX * bytesPerPixel;
            // What is needed to jump to the next pixel in the column
            final int indexIncrement = width * bytesPerPixel;
            // Stop when all pixels in this column are done
            final int stopIndex = startIndex + (height-1) * indexIncrement;
            sum += getPixelLineContrastsSum(bytes, startIndex, indexIncrement, stopIndex);
        }

        // The number of samples is equal to the number of pixel processed except the first of each row and column
        final double count = horizontalStripesCount * (width-1) + verticalStripesCount * (height-1);
        // Return the average difference between pixels luminance
        return (sum / count);
    }

}
