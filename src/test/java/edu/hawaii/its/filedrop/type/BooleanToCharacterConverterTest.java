package edu.hawaii.its.filedrop.type;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BooleanToCharacterConverterTest {

    private BooleanToCharacterConverter converter;

    private final Character YES = Character.valueOf('Y');
    private final Character NO = Character.valueOf('N');

    @BeforeEach
    public void setUp() {
        converter = new BooleanToCharacterConverter();
    }

    @Test
    public void convertToDatabaseColumn() {
        assertThat(converter.convertToDatabaseColumn(Boolean.TRUE), equalTo(YES));
        assertThat(converter.convertToDatabaseColumn(TRUE), equalTo(YES));
        assertThat(converter.convertToDatabaseColumn(FALSE), equalTo(NO));
        assertThat(converter.convertToDatabaseColumn(null), equalTo(NO));
    }

    @Test
    public void convertToEntityAttribute() {
        assertThat(converter.convertToEntityAttribute(YES), equalTo(TRUE));
        assertThat(converter.convertToEntityAttribute(NO), equalTo(FALSE));

        // Unexpected cases.
        assertThat(converter.convertToEntityAttribute(null), equalTo(FALSE));
        assertThat(converter.convertToEntityAttribute(Character.valueOf('T')),
                equalTo(FALSE));
    }

}
