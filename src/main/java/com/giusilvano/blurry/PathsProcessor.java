package com.giusilvano.blurry;

import javax.activation.MimetypesFileTypeMap;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PathsProcessor {

    /**
     * Returns true if the given file is an image.
     */
    private static boolean isImage(File file) {
        final String mimeType = new MimetypesFileTypeMap().getContentType(file);
        if (mimeType.substring(0, 5).equalsIgnoreCase("image")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Returns all the files of a directory that are images.
     */
    private static List<File> getImagesFiles(List<String> paths) {
        final List<File> images = new ArrayList<>();
        for (String path : paths) {
            final File file = new File(path);
            if (file.isDirectory()) {
                for (final File childFile : file.listFiles()) {
                    if (isImage(childFile)) {
                        images.add(childFile);
                    }
                }
            } else {
                if (file.exists() && isImage(file)) {
                    images.add(file);
                }
            }
        }
        return images;
    }

    public static void process(List<String> paths, float sampleCoverage) {
        System.out.println("Looking for cool pictures...");
        final List<File> images = getImagesFiles(paths);
        if (images.isEmpty()) {
            System.out.println("Sorry, no images found.");
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

    public static void restoreFilenames(List<String> paths) {
        System.out.println("Looking for cool pictures...");
        final List<File> images = getImagesFiles(paths);
        int restoredImages = 0;
        for (final File file : images) {
            try {
                if (FileRenamer.renameRemovingScore(file)) {
                    restoredImages++;
                }
            } catch (FileRenamer.CantRenameException e) {
                System.out.printf("Cannot rename file %s: %s already exists.\n", file.getName(), e.newFilename);
            }
        }
        if (restoredImages == 0) {
            System.out.println("No image matched the Blurry filename pattern, nothing to restore.");
        } else {
            System.out.printf("%d images successfully restored to their original names!\n", restoredImages);
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
