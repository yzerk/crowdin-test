package com.crowdin.testapp.client;

import com.crowdin.client.Client;
import com.crowdin.client.core.model.Credentials;
import com.crowdin.client.sourcefiles.model.AddFileRequest;
import com.crowdin.client.storage.model.Storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.util.stream.Stream;

public class CrowdinApiClient extends CrowdinClientCore {

    private Client client;
    private Credentials credentials;

    public CrowdinApiClient(Credentials credentials) {
        client = new Client(credentials);
    }

    public void uploadListOfFiles(Long projectId, File[] files) {
        Stream.of(files).forEach(file -> uploadFile(projectId, file));
    }

    public void uploadFile(Long projectId, File file) {
        var request = new AddFileRequest();
        var storageId = uploadStorage(file.getName(), file);
        request.setStorageId(storageId);
        request.setType("json");
        request.setName(file.getName());
        var data = executeRequest(() -> this.client.getSourceFilesApi().addFile(projectId, request))
                .getData();
        System.out.printf("File %s successfully uploaded!\n", data.getName());
    }

    public Long uploadStorage(String fileName, File file) {
        try (InputStream data = new FileInputStream(file)) {
            Storage storage = executeRequest(() -> this.client.getStorageApi()
                    .addStorage(fileName, data)
                    .getData());
            return storage.getId();
        } catch (IOException e) {
            throw new RuntimeException(String.format(RESOURCE_BUNDLE.getString("error.write_file"), file.getAbsoluteFile()), e);
        }
    }
}
