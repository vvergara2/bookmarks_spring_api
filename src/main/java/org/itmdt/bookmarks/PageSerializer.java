package org.itmdt.bookmarks;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

import java.io.IOException;
@Component
public class PageSerializer extends StdSerializer<Page> {
    public PageSerializer() {
        this(null);
    }

    public PageSerializer(Class<Page> page) {
        super(page);
    }

    @Override
    public void serialize(Page page, JsonGenerator jsonGen, SerializerProvider serializerProvider)
            throws IOException, JsonProcessingException {
        ObjectMapper om = new ObjectMapper()
                .disable(MapperFeature.DEFAULT_VIEW_INCLUSION)
                .setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        jsonGen.writeStartObject();
        jsonGen.writeFieldName("size");
        jsonGen.writeNumber(page.getSize());
        jsonGen.writeFieldName("number");
        jsonGen.writeNumber(page.getNumber());
        jsonGen.writeFieldName("totalElements");
        jsonGen.writeNumber(page.getTotalElements());
        jsonGen.writeFieldName("last");
        jsonGen.writeBoolean(page.isLast());
        jsonGen.writeFieldName("totalPages");
        jsonGen.writeNumber(page.getTotalPages());
        jsonGen.writeObjectField("sort", page.getSort());
        jsonGen.writeFieldName("first");
        jsonGen.writeBoolean(page.isFirst());
        jsonGen.writeFieldName("numberOfElements");
        jsonGen.writeNumber(page.getNumberOfElements());
        jsonGen.writeFieldName("content");
        jsonGen.writeRawValue(om.writerWithView(serializerProvider.getActiveView())
                .writeValueAsString(page.getContent()));
        jsonGen.writeEndObject();
    }
}
