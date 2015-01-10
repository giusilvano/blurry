package com.giusilvano.blurry;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;

public class ImageProcessor implements Runnable {

    private final File file;
    private final float sampleCoverage;
    private final DirectoryProcessor.processingProgress progress;

    public ImageProcessor(File file, float sampleCoverage, DirectoryProcessor.processingProgress progress) {
        this.file = file;
        this.sampleCoverage = sampleCoverage;
        this.progress = progress;
    }

    @Override
    public void run() {
        try {
            final BufferedImage bufferedImage = ImageIO.read(file);
            if (bufferedImage == null) {
                progress.fileSkipped(file);
            } else {
                final double score = ImageContrastEvaluation.getImageContrastScore(bufferedImage, sampleCoverage);
                // Ok, we have the score, so now rename the file putting the score in the filename
                try {
                    FileRenamer.renameAddingScore(file, score);
                    progress.fileProcessSucceeded(file, score);
                } catch (FileRenamer.CantRenameException e) {
                    progress.fileProcessSucceededButCantRename(file, score, e.newFilename);
                }
            }
        } catch (ImageContrastEvaluation.UnsupportedImageTypeException e) {
            progress.fileProcessFailedBecauseImageNotSupported(file);
        } catch (Exception e) {
            progress.fileProcessFailed(file);
        }
    }
}
