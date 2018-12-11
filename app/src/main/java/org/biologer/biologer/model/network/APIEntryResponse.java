package org.biologer.biologer.model.network;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Created by brjovanovic on 3/12/2018.
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class APIEntryResponse {

    Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
