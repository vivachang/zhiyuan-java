package com.forest.dataacquisitionserver.gson;

import com.forest.dataacquisitionserver.protocol.ProtocolCode;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ProtocolCodeAdapterFactory implements TypeAdapterFactory {
    @Override
    public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> typeToken) {
        Class<? super T> rawType = typeToken.getRawType();
        if (rawType == ProtocolCode.class) {
            return new ProtocolCodeTypeAdapter<T>();
        }
        return null;
    }
    public class ProtocolCodeTypeAdapter<T> extends TypeAdapter<T> {
        @Override
        public void write(JsonWriter jsonWriter, T t) throws IOException {
            if (null == t) {
                jsonWriter.nullValue();
                return;
            }
            ProtocolCode protocolCode = (ProtocolCode)t;
            jsonWriter.beginObject();
            jsonWriter.name("code");
            jsonWriter.value(protocolCode.getCode());
            jsonWriter.name("message");
            jsonWriter.value(protocolCode.getMessage());
            jsonWriter.endObject();
        }

        @Override
        public T read(JsonReader jsonReader) throws IOException {
            return null;
        }
    }
}
