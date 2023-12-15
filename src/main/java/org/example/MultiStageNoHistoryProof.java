package org.example;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class MultiStageNoHistoryProof {
    ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    Map<String, byte[]> unTar(InputStream tarStream) {
        var result = new LinkedHashMap<String, byte[]>();
        try (BufferedInputStream inputStream = new BufferedInputStream(tarStream);
             TarArchiveInputStream tar = new TarArchiveInputStream(inputStream)) {
            ArchiveEntry entry;
            while ((entry = tar.getNextEntry()) != null) {
                entry.getName();
                var os = new ByteArrayOutputStream();
                tar.transferTo(os);
                result.put(entry.getName(), os.toByteArray());
            }
        }

        return result;
    }

    /**
     * each data structure supports additional properties intentionally
     * so that if any unwanted data is present it would be detected
     */
    @Data
    @Accessors(chain = true)
    static class Config {
        @JsonAnyGetter
        @JsonAnySetter
        @JsonIgnore
        LinkedHashMap<String, Object> additional;
        String architecture;
        ConfigConfig config;
        String created;
        List<HistoryEntry> history;
        String os;
        RootFs rootfs;
        String variant;

        @Data
        @Accessors(chain = true)
        static class ConfigConfig {
            @JsonAnyGetter
            @JsonAnySetter
            @JsonIgnore
            LinkedHashMap<String, Object> additional;
            @JsonProperty("Env")
            List<String> env;
            @JsonProperty("Cmd")
            List<String> cmd;
        }

        @Data
        @Accessors(chain = true)
        static class HistoryEntry {
            @JsonAnyGetter
            @JsonAnySetter
            @JsonIgnore
            LinkedHashMap<String, Object> additional;
            String created;
            @JsonProperty("created_by")
            String createdBy;
            @JsonProperty("empty_layer")
            Boolean emptyLayer;
            String comment;
        }

        @Data
        @Accessors(chain = true)
        static class RootFs {
            @JsonAnyGetter
            @JsonAnySetter
            @JsonIgnore
            LinkedHashMap<String, Object> additional;
            String type;
        }
    }
}
