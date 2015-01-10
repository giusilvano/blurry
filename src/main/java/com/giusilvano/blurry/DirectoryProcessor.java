package com.giusilvano.blurry;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class DirectoryProcessor {

    /**
     * Returns all the files of a directory that are images.
     */
    private static List<File> filterImages(File dir) {
        if (!dir.isDirectory()) {
            throw new IllegalArgumentException("dir parameter is not a directory");
        }
        final List<File> images = new ArrayList<>();
        for (final File file : dir.listFiles()) {
            final String mimeType = new MimetypesFileTypeMap().getContentType(file);
            if (mimeType.substring(0, 5).equalsIgnoreCase("image")) {
                images.add(file);
            }
        }
        return images;
    }

    public static void process(String path, float sampleCoverage) {
        final File dir = new File(path);
        if (!dir.isDirectory()) {
            System.out.printf("Error: path %s is not a directory.\n", path);
        } else {
            System.out.printf("Looking for cool pictures in %s ...\n", path);
            final List<File> images = filterImages(dir);
            if (images.isEmpty()) {
                System.out.println("Sorry, no images found in this path.");
            } else {
                System.out.printf("Great! Blurry found %d images! Starting processing...\n", images.size());
                final ExecutorService executor = Executors.newWorkStealingPool();
                final processingProgress progress = new processingProgress(images.size());
                for (final File image : images) {
                    executor.execute(new ImageProcessor(image, sampleCoverage, progress));
                }
                executor.shutdown();
                try {
                    executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    public static void restoreFilenames(String path) {
        final File dir = new File(path);
        if (!dir.isDirectory()) {
            System.out.printf("Error: path %s is not a directory.\n", path);
        } else {
            System.out.printf("Looking for cool pictures in %s ...\n", path);
            int processedImages = 0;
            for (final File file : dir.listFiles()) {
                try {
                    if (FileRenamer.renameRemovingScore(file)) {
                        processedImages++;
                    }
                } catch (FileRenamer.CantRenameException e) {
                    System.out.printf("Cannot rename file %s: %s already exists.\n", file.getName(), e.newFilename);
                }
            }
            if (processedImages == 0) {
                System.out.println("No file matched the Blurry filename pattern, nothing to restore.");
            } else {
                System.out.printf("%d files successfully restored to their original names!\n", processedImages);
            }
        }
    }


    public static class processingProgress {

        private final long startTime;
        private final int toProcess;
        private int processed = 0;

        private processingProgress(int imagesToProcess) {
            this.startTime = System.currentTimeMillis();
            this.toProcess = imagesToProcess;
        }

        public synchronized void fileSkipped(File file) {
            processed++;
            System.out.printf("%s skipped, not an image\n", getProgressAndFilenameString(file));
            checkIfAllFilesProcessed();
        }

        public synchronized void fileProcessSucceeded(File file, double score) {
            processed++;
            System.out.printf("%s %.4f\n", getProgressAndFilenameString(file), score);
            checkIfAllFilesProcessed();
        }

        public synchronized void fileProcessSucceededButCantRename(File file, double score, String newFilename) {
            processed++;
            System.out.printf("%s %.4f\n", getProgressAndFilenameString(file), score);
            System.out.printf("Cannot rename file %s: %s already exists\n", file.getName(), newFilename);
            checkIfAllFilesProcessed();
        }

        public synchronized void fileProcessFailed(File file) {
            processed++;
            System.out.printf("%s sorry, processing stopped because of an error\n", getProgressAndFilenameString(file));
            checkIfAllFilesProcessed();
        }

        public synchronized void fileProcessFailedBecauseImageNotSupported(File file) {
            processed++;
            System.out.printf("%s skipped, image type not supported\n", getProgressAndFilenameString(file));
            checkIfAllFilesProcessed();
        }

        private String getProgressString() {
            return String.format("%d%%", Math.round(((double) processed / toProcess) * 100));
        }

        private String getProgressAndFilenameString(File file) {
            return String.format("%s  %s -->", getProgressString(), file.getName());
        }

        private void checkIfAllFilesProcessed() {
            if (processed == toProcess) {
                System.out.printf("Yeah! %d images successfully processed in %d  milliseconds!\n", processed, System.currentTimeMillis() - startTime);
            }
        }
    }

}
