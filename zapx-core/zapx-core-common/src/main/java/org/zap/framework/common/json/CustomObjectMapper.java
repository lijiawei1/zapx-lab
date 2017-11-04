package org.zap.framework.common.json;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.fasterxml.jackson.databind.*;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;
import org.zap.framework.lang.LDouble;
import org.zap.framework.util.DataTypeUtils;
import org.zap.framework.util.DateUtils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.ser.std.NullSerializer;

public class CustomObjectMapper extends ObjectMapper {

    /**
     *
     */
    private static final long serialVersionUID = 7531811819559816842L;

    public CustomObjectMapper() {

//        // 允许单引号  
//        this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//       // 字段和值都加引号  
//       this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);  
//       // 数字也加引号  
//       this.configure(JsonGenerator.Feature.WRITE_NUMBERS_AS_STRINGS, true);  
//       this.configure(JsonGenerator.Feature.QUOTE_NON_NUMERIC_NUMBERS, true);

        //关闭错误Could not read JSON: Unrecognized field,接收的ENTITY没有传过来的属性
        this.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        SimpleModule module = new SimpleModule();

        module.addDeserializer(String.class, new JsonDeserializer<String>() {
            @Override
            public String deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
                return DataTypeUtils.parseString(jp.getText());
            }
        });

        module.addDeserializer(LDouble.class, new JsonDeserializer<LDouble>() {

            @Override
            public LDouble deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException {
                return DataTypeUtils.parseLDouble(jp.getText());
            }

        });

        module.addDeserializer(Boolean.class, new JsonDeserializer<Boolean>() {

            @Override
            public Boolean deserialize(JsonParser jp, DeserializationContext ctxt)
                    throws IOException, JsonProcessingException {
                return DataTypeUtils.parseBoolean(jp.getText());
            }
        });


        module.addDeserializer(LocalDateTime.class, new JsonDeserializer<LocalDateTime>() {

            @Override
            public LocalDateTime deserialize(JsonParser jp,
                                             DeserializationContext ctxt) throws IOException,
                    JsonProcessingException {

                return DateUtils.parseDateTime(jp.getText());
            }
        });

        module.addDeserializer(LocalDate.class, new JsonDeserializer<LocalDate>() {

            @Override
            public LocalDate deserialize(JsonParser jp,
                                         DeserializationContext ctxt) throws IOException,
                    JsonProcessingException {

                return DateUtils.parseDate(jp.getText());
            }
        });

        module.addDeserializer(LocalTime.class, new JsonDeserializer<LocalTime>() {

            @Override
            public LocalTime deserialize(JsonParser jp,
                                         DeserializationContext ctxt) throws IOException,
                    JsonProcessingException {

                return DateUtils.parseTime(jp.getText());
            }
        });


        module.addSerializer(LocalDate.class, new JsonSerializer<LocalDate>() {

            @Override
            public void serialize(LocalDate value, JsonGenerator jgen,
                                  SerializerProvider provider) throws IOException,
                    JsonProcessingException {
                jgen.writeString(value.format(DateUtils.FORMATTER_DATE));
            }

        });

        module.addSerializer(Void.class, NullSerializer.instance);
        module.addSerializer(LocalDateTime.class, new JsonSerializer<LocalDateTime>() {

            @Override
            public void serialize(LocalDateTime value, JsonGenerator jgen,
                                  SerializerProvider provider) throws IOException,
                    JsonProcessingException {
                jgen.writeString(value.format(DateUtils.FORMATTER_DATETIME));
            }
        });

        module.addSerializer(LocalTime.class, new JsonSerializer<LocalTime>() {

            @Override
            public void serialize(LocalTime value, JsonGenerator jgen,
                                  SerializerProvider provider) throws IOException,
                    JsonProcessingException {
                jgen.writeString(value.format(DateUtils.FORMATTER_TIME));
            }
        });

        registerModule(module);

        this.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
            @Override
            public void serialize(Object value, JsonGenerator jgen,
                                  SerializerProvider provider) throws IOException {
//				jgen.writeString("");
                jgen.writeNull();
            }
        });

    }
} 
