package chasemoon.top.wxflearningresourcesbackendclient.converter;

import chasemoon.top.wxflearningresourcesbackendclient.entity.enums.FileStatus;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToFileStatusConverter implements Converter<String, FileStatus> {

    @Override
    public FileStatus convert(String source) {
        try {
            int intValue = Integer.parseInt(source);
            for (FileStatus status : FileStatus.values()) {
                if (status.getValue() == intValue) {
                    return status;
                }
            }
        } catch (NumberFormatException e) {
            // It might be the enum name itself, e.g., "APPROVED"
        }
        // Fallback for string-based enum names or invalid numbers
        try {
            return FileStatus.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown file status value: " + source);
        }
    }
} 