package com.giusilvano.blurry;

import java.awt.image.BufferedImage;

public class TestImages {
    private final static int TEST_IMAGES_WIDTH = 50;
    private final static int TEST_IMAGES_HEIGHT = 50;
    private final static int BLACK_PIXEL = 0;
    private final static int WHITE_PIXEL = (255 << 16) | (255 << 8) | 255;

    /**
     * Abstracts the method to generate pixels procedurally.
     */
    public interface PixelGenerator {
        /**
         * Returns the color (3-byte BGR) of the pixel at the x and y coordinates.
         */
        public int generatePixel(int x, int y);
    }

    /**
     * Returns an image generated using the provided PixelGenerator.
     * You can use the generators already provided by this class.
     */
    public static BufferedImage newTestImage(PixelGenerator pixelGenerator) {
        final BufferedImage img = new BufferedImage(TEST_IMAGES_WIDTH, TEST_IMAGES_HEIGHT, BufferedImage.TYPE_3BYTE_BGR);
        for (int x = 0; x < TEST_IMAGES_WIDTH; x++) {
            for (int y = 0; y < TEST_IMAGES_HEIGHT; y++) {
                img.setRGB(x, y, pixelGenerator.generatePixel(x, y));
            }
        }
        return img;
    }

    /**
     * Generates an image with all pixels black.
     */
    public static int emptyPixelGenerator(int x, int y) {
        return BLACK_PIXEL;
    }

    /**
     * Generates an image with alternated black/white full-width horizontal lines.
     */
    public static int horizontalZebraPixelGenerator(int x, int y) {
        return ( y % 2 == 0 ? BLACK_PIXEL : WHITE_PIXEL);
    }

    /**
     * Generates an image with alternated black/white full-height vertical lines.
     */
    public static int verticalZebraPixelGenerator(int x, int y) {
        return ( x % 2 == 0 ? BLACK_PIXEL : WHITE_PIXEL);
    }

    /**
     * Generates an image like a chessboard: each pixel is a square of the chessboard, black or white.
     */
    public static int chessboardPixelGenerator(int x, int y) {
        return ( (x + y) % 2 == 0 ? BLACK_PIXEL : WHITE_PIXEL);
    }
}
