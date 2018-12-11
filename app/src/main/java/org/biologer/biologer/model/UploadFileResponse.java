package org.biologer.biologer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by brjovanovic on 3/12/2018.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadFileResponse {
    private String file;

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
}
