package com.crowdin.testapp;

import org.apache.commons.io.filefilter.WildcardFileFilter;

import java.io.File;
import java.io.FileFilter;

public class FileUtils {

    public static File[] getFilesByFilter(WildcardFileFilter wildcardFileFilter) {
        File dir = new File(".");
        return dir.listFiles((FileFilter) wildcardFileFilter);
    }
}