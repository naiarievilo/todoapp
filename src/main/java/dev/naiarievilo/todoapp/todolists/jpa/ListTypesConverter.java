package dev.naiarievilo.todoapp.todolists.jpa;

import dev.naiarievilo.todoapp.todolists.ListTypes;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class ListTypesConverter implements AttributeConverter<ListTypes, String> {

    @Override
    public String convertToDatabaseColumn(ListTypes listType) {
        if (listType == null) {
            return null;
        }

        return listType.getType();
    }

    @Override
    public ListTypes convertToEntityAttribute(String str) {
        if (str == null) {
            return null;
        }

        for (ListTypes type : ListTypes.values()) {
            if (type.getType().equals(str)) {
                return type;
            }
        }

        throw new IllegalArgumentException(str + " is not a valid list type");
    }
}
