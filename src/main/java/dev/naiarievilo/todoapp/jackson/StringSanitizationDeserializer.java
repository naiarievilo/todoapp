package dev.naiarievilo.todoapp.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

import java.io.IOException;

public class StringSanitizationDeserializer extends StdDeserializer<String> {

    private final transient JsonDeserializer<?> defaultJsonDeserializer;

    public StringSanitizationDeserializer(JsonDeserializer<?> jsonDeserializer) {
        super(String.class);
        this.defaultJsonDeserializer = jsonDeserializer;
    }

    @Override
    public String deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String result = (String) defaultJsonDeserializer.deserialize(jsonParser, deserializationContext);
        return Jsoup.clean(result, Safelist.none());
    }
}
