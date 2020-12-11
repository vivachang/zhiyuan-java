package com.forest.dataacquisitionserver.gson;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GsonConfiguration {
    @Bean
    GsonBuilder gsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.addSerializationExclusionStrategy(new ExclusionStrategy() {
            @Override
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return fieldAttributes.getAnnotation(SkipSerialisation.class) != null;
            }

            @Override
            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        });
        gsonBuilder.registerTypeAdapterFactory(new ProtocolCodeAdapterFactory());
//        gsonBuilder.registerTypeAdapter(ProtocolCode.class, new ProtocolCodeAdapterFactory());

        return gsonBuilder;
    }
}
