package com.ipd.fastjson.parser.deserializer;

import com.ipd.fastjson.JSONException;
import com.ipd.fastjson.annotation.JSONField;
import com.ipd.fastjson.parser.DefaultJSONParser;
import com.ipd.fastjson.parser.DefaultJSONParser.ResolveTask;
import com.ipd.fastjson.parser.JSONToken;
import com.ipd.fastjson.parser.ParseContext;
import com.ipd.fastjson.parser.ParserConfig;
import com.ipd.fastjson.util.FieldInfo;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Map;

public class DefaultFieldDeserializer extends FieldDeserializer {

    protected ObjectDeserializer fieldValueDeserilizer;

    public DefaultFieldDeserializer(ParserConfig mapping, Class<?> clazz, FieldInfo fieldInfo){
        super(clazz, fieldInfo);
    }

    public ObjectDeserializer getFieldValueDeserilizer(ParserConfig config) {
        if (fieldValueDeserilizer == null) {
            JSONField annotation = fieldInfo.getAnnotation();
            if (annotation != null && annotation.deserializeUsing() != Void.class) {
                Class<?> deserializeUsing = annotation.deserializeUsing();
                try {
                    fieldValueDeserilizer = (ObjectDeserializer) deserializeUsing.newInstance();
                } catch (Exception ex) {
                    throw new JSONException("create deserializeUsing ObjectDeserializer error", ex);
                }
            } else {
                fieldValueDeserilizer = config.getDeserializer(fieldInfo.fieldClass, fieldInfo.fieldType);
            }
        }

        return fieldValueDeserilizer;
    }

    @Override
    public void parseField(DefaultJSONParser parser, Object object, Type objectType, Map<String, Object> fieldValues) {
        if (fieldValueDeserilizer == null) {
            getFieldValueDeserilizer(parser.getConfig());
        }

        Type fieldType = fieldInfo.fieldType;
        if (objectType instanceof ParameterizedType) {
            ParseContext objContext = parser.getContext();
            if (objContext != null) {
                objContext.type = objectType;
            }
            fieldType = FieldInfo.getFieldType(this.clazz, objectType, fieldType);
            fieldValueDeserilizer = parser.getConfig().getDeserializer(fieldType);
        }

        // ContextObjectDeserializer
        Object value;
        if (fieldValueDeserilizer instanceof JavaBeanDeserializer && fieldInfo.parserFeatures != 0) {
            JavaBeanDeserializer javaBeanDeser = (JavaBeanDeserializer) fieldValueDeserilizer;
            value = javaBeanDeser.deserialze(parser, fieldType, fieldInfo.name, fieldInfo.parserFeatures);
        } else {
            if (this.fieldInfo.format != null && fieldValueDeserilizer instanceof ContextObjectDeserializer) {
                value = ((ContextObjectDeserializer) fieldValueDeserilizer) //
                                                                            .deserialze(parser, fieldType,
                                                                                        fieldInfo.name,
                                                                                        fieldInfo.format,
                                                                                        fieldInfo.parserFeatures);
            } else {
                value = fieldValueDeserilizer.deserialze(parser, fieldType, fieldInfo.name);
            }
        }
        if (parser.getResolveStatus() == DefaultJSONParser.NeedToResolve) {
            ResolveTask task = parser.getLastResolveTask();
            task.fieldDeserializer = this;
            task.ownerContext = parser.getContext();
            parser.setResolveStatus(DefaultJSONParser.NONE);
        } else {
            if (object == null) {
                fieldValues.put(fieldInfo.name, value);
            } else {
                setValue(object, value);
            }
        }
    }

    public int getFastMatchToken() {
        if (fieldValueDeserilizer != null) {
            return fieldValueDeserilizer.getFastMatchToken();
        }

        return JSONToken.LITERAL_INT;
    }
}
