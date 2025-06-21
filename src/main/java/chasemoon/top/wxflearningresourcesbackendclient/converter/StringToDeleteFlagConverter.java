package chasemoon.top.wxflearningresourcesbackendclient.converter;

import chasemoon.top.wxflearningresourcesbackendclient.entity.enums.DeleteFlag;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class StringToDeleteFlagConverter implements Converter<String, DeleteFlag> {

    @Override
    public DeleteFlag convert(String source) {
        try {
            int intValue = Integer.parseInt(source);
            for (DeleteFlag flag : DeleteFlag.values()) {
                if (flag.getValue() == intValue) {
                    return flag;
                }
            }
        } catch (NumberFormatException e) {
            // It might be the enum name itself, e.g., "NORMAL"
        }
        
        try {
            return DeleteFlag.valueOf(source.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown delete flag value: " + source);
        }
    }
} 