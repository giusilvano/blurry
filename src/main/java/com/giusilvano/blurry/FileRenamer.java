package com.giusilvano.blurry;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FileRenamer {

    private static final String FILENAME_RENAME_FORMAT = "%s __%.4f.%s";
    private static final Pattern FILENAME_RESTORE_PATTERN = Pattern.compile("^.+( __[\\.\\d]+)$");

    public static class CantRenameException extends Exception {
        public final String newFilename;

        public CantRenameException(String newFilename) {
            this.newFilename = newFilename;
        }
    }

    public static void renameAddingScore(File file, Double score) throws CantRenameException {
        final String filenameWithoutExtension = FilenameUtils.removeExtension(file.getAbsolutePath());
        final String filenameExtension = FilenameUtils.getExtension(file.getName());
        final String newFilename = String.format(FILENAME_RENAME_FORMAT, filenameWithoutExtension, score, filenameExtension);
        final File renamedFile = new File(newFilename);
        if (renamedFile.exists()) {
            throw new CantRenameException(newFilename);
        } else {
            file.renameTo(renamedFile);
        }
    }

    public static boolean renameRemovingScore(File file) throws CantRenameException {
        final String filenameWithoutExtension = FilenameUtils.removeExtension(file.getAbsolutePath());
        final Matcher m = FILENAME_RESTORE_PATTERN.matcher(filenameWithoutExtension);
        if (!m.find()) {
            return false;
        } else {
            final String filenameExtension = FilenameUtils.getExtension(file.getName());
            final String newFilename = filenameWithoutExtension.substring(0, filenameWithoutExtension.length() - m.group(1).length()) + "." + filenameExtension;
            final File renamedFile = new File(newFilename);
            if (renamedFile.exists()) {
                throw new CantRenameException(newFilename);
            } else {
                file.renameTo(renamedFile);
                return true;
            }
        }
    }
}
