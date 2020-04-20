package edu.hawaii.its.filedrop.type;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class BooleanToCharacterConverter implements AttributeConverter<Boolean, Character> {

    private static final Character YES = Character.valueOf('Y');
    private static final Character NO = Character.valueOf('N');

    @Override
    public Character convertToDatabaseColumn(Boolean value) {
        return value != null && value ? YES : NO;
    }

    @Override
    public Boolean convertToEntityAttribute(Character value) {
        return YES.equals(value);
    }

}
