package com.acme.salary.employee;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.LocalDate;

/** SQLite has no DATE type; dates are stored as ISO-8601 text (yyyy-MM-dd), which also sorts correctly. */
@Converter
public class LocalDateStringConverter implements AttributeConverter<LocalDate, String> {

    @Override
    public String convertToDatabaseColumn(LocalDate date) {
        return date == null ? null : date.toString();
    }

    @Override
    public LocalDate convertToEntityAttribute(String value) {
        return value == null ? null : LocalDate.parse(value);
    }
}
