package com.giusilvano.blurry;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

public class ImageContrastEvaluationTest {

    private final static float SAMPLE_COVERAGE = 0.01f;

    @Test
    public void testGetImageContrastScore() throws Exception {
        final double delta = 0;

        BufferedImage img;
        double score;

        // In a chessboard image, every pixel is surrounded (vertically and
        // horizontally, not diagonally) by pixels of the opposite color. In
        // this type of image, the luminosity difference between a pixel and
        // its subsequent will be always 1. This is true whether you scan
        // pixels horizontally or vertically. So the average contrast for
        // a chessboard image is 1.
        img = TestImages.newTestImage(TestImages::chessboardPixelGenerator);
        score = ImageContrastEvaluation.getImageContrastScore(img, SAMPLE_COVERAGE);
        assertEquals(1, score, delta);

        // An image made of vertical full-height lines with alternate black -
        // white colors will get contrast score 1 in horizontal scan, and
        // contrast score 0 in vertical scan, so the average will be 0.5.
        img = TestImages.newTestImage(TestImages::verticalZebraPixelGenerator);
        score = ImageContrastEvaluation.getImageContrastScore(img, SAMPLE_COVERAGE);
        assertEquals(0.5, score, delta);

        // Same as the reasoning above, but in this image lines are horizontal.
        img = TestImages.newTestImage(TestImages::horizontalZebraPixelGenerator);
        score = ImageContrastEvaluation.getImageContrastScore(img, SAMPLE_COVERAGE);
        assertEquals(0.5, score, delta);

        // An image having the same color in every pixel will have contrast 0.
        img = TestImages.newTestImage(TestImages::emptyPixelGenerator);
        score = ImageContrastEvaluation.getImageContrastScore(img, SAMPLE_COVERAGE);
        assertEquals(0, score, delta);
    }
}