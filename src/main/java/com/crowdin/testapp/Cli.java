package com.crowdin.testapp;

import com.crowdin.client.core.model.Credentials;
import com.crowdin.testapp.client.CrowdinApiClient;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import picocli.CommandLine;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.util.ResourceBundle;

public class Cli implements Runnable {


    public static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("app");
    private final String ORGANIZATION = RESOURCE_BUNDLE.getString("app.organization");
    private final String BASE_URL = RESOURCE_BUNDLE.getString("app.base_url");

    private static final String NO_FILES_ERROR_MESSAGE = "No files were found that match the wildcard.";
    @Parameters(index = "0", paramLabel = "Project ID", description = "ID of Crowdin's Project")
    private Long projectId;

    @Parameters(index = "1", paramLabel = "TOKEN", description = "Crowdin Personal Access Token")
    private String token;
    @Parameters(index = "2", paramLabel = "WILDCARD", description = "WILDCARD to process")
    private String wildcard;

    public void run() {
        var files = FileUtils.getFilesByFilter(new WildcardFileFilter(wildcard));
        printDetectedFilesMessage(files);
        var client = new CrowdinApiClient(new Credentials(token, ORGANIZATION, BASE_URL));
        client.uploadListOfFiles(projectId, files);
    }

    public static void main(String[] args) {
        System.out.println("Start...");
        try {
            var exitCode = launchApp(args);
            System.exit(exitCode);
        } catch (FileNotFoundError | RuntimeException e) {
            System.out.println(e.getMessage());
        } finally {
            System.out.println("Finish!");
        }
    }

    private static int launchApp(String[] args) {
        if (args.length == 0) {
            throw new CommandLine.PicocliException("Please specify the project ID, PAT, and wildcard pattern!");
        }
        return new CommandLine(new Cli()).execute(args);
    }

    private void printDetectedFilesMessage(File[] files) {
        int filesFound = files.length;
        if (filesFound == 0) {
            throw new FileNotFoundError(NO_FILES_ERROR_MESSAGE);
        } else {
            System.out.printf("Found %s file%s matching the wildcard, uploading...%n", filesFound, filesFound > 1 ? "s" : "");
        }
    }

}
