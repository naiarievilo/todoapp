package dev.naiarievilo.todoapp.security.jackson;

import com.fasterxml.jackson.databind.BeanDescription;
import com.fasterxml.jackson.databind.DeserializationConfig;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.BeanDeserializerModifier;

public class StringDeserializerModifier extends BeanDeserializerModifier {

    @Override
    public JsonDeserializer<?> modifyDeserializer(DeserializationConfig config, BeanDescription beanDesc,
        JsonDeserializer<?> deserializer) {
        if (beanDesc.getBeanClass() == String.class) {
            return new StringSanitizationDeserializer(deserializer);
        } else {
            return deserializer;
        }
    }
}
