/**
 * Copyright 2004-2048 .
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.ipd.jsf.gd.codec.json;

import com.ipd.fastjson.JSONReader;
import com.ipd.fastjson.parser.DefaultJSONParser;
import com.ipd.fastjson.parser.Feature;
import com.ipd.fastjson.parser.JSONLexer;
import com.ipd.fastjson.parser.JSONToken;
import com.ipd.fastjson.parser.deserializer.ObjectDeserializer;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.Invocation;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

import static com.ipd.jsf.gd.util.JSFLogicSwitch.JSON_PARSER_FEATURES;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 *
 * @since 2015/01/10 10:58
 */
public class InvocationDeserializer implements ObjectDeserializer, InvocationSerializable {

    private final static Logger logger = LoggerFactory.getLogger(InvocationDeserializer.class);

    public static final InvocationDeserializer instance = new InvocationDeserializer();


    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        if (CommonUtils.isNotEmpty(JSON_PARSER_FEATURES)) {
            for (Feature feature : JSON_PARSER_FEATURES) {
                parser.config(feature, true);
            }
        }
        JSONLexer lexer = parser.getLexer();
        if (lexer.token() == JSONToken.NULL) {
            lexer.nextToken();
            return null;
        }

        if (lexer.token() != JSONToken.LBRACE) {
            throw new JSFCodecException("syntax error");
        }

        Invocation invocation = new Invocation();
        for (;;) {
            int token = -1;
            // lexer.scanSymbol
            String key = lexer.scanSymbol(parser.getSymbolTable());
            if (key == null) {
                token = lexer.token();
                if ( token == JSONToken.RBRACE) {
                    lexer.nextToken(JSONToken.COMMA);
                    break;
                }
                token = lexer.token();
                if (token == JSONToken.COMMA) {
                    if (lexer.isEnabled(Feature.AllowArbitraryCommas)) {
                        continue;
                    }
                }
            }
            lexer.nextTokenWithColon(JSONToken.LITERAL_STRING);

            if (CLAZZ_NAME.equals(key)) {
                token = lexer.token();
                if (token == JSONToken.LITERAL_STRING) {
                    invocation.setClazzName(lexer.stringVal());
                } else {
                    throw new JSFCodecException("syntax error: clazzName is empty");
                }
                lexer.nextToken();
            } else if (METHOD_NAME.equals(key)) {
                if (lexer.token() == JSONToken.LITERAL_STRING) {
                    invocation.setMethodName(lexer.stringVal());
                } else {
                    throw new JSFCodecException("syntax error:method is empty");
                }
                lexer.nextToken();
            } else if (ALIAS.equals(key)) {
                if (lexer.token() == JSONToken.LITERAL_STRING) {
                    invocation.setAlias(lexer.stringVal());
                } else {
                    if ( !CALLBACK_CLZ.equals(invocation.getClazzName()) ){
                        throw new JSFCodecException("syntax error: alias is empty");
                    }
                }
                lexer.nextToken();
            }  else if (ARGS_TYPE.equals(key)){
                invocation.setArgsType(parser.parseObject(String[].class));
            }else if (ARGS.equals(key)){
                Class[] argsClass = null;
                if ( invocation.getArgsType() != null ){
                    argsClass = ClassTypeUtils.getClasses(invocation.getArgsType());
                } else {
                    argsClass = ReflectUtils.getMethodArgsType(invocation.getClazzName(),invocation.getMethodName());
                    //如果调用此方法返回null说明在接口中没有找到对应的方法，接口发生变化
                    if ( argsClass == null ){
                        throw new JSFCodecException(" no method "+invocation.getMethodName()+" in "+invocation.getClazzName() +" interface ");
                    }
                }
                invocation.setArgsType(argsClass);
                if ( argsClass == null || argsClass.length == 0 ){
                    continue;
                }
                deserializeArgs(invocation, parser,argsClass);
            } else if ( ATTACHMENTS.equals(key)){
                invocation.getAttachments().putAll(parser.parseObject());
            }
            token = lexer.token();
            if (token == JSONToken.RBRACE) {
                lexer.nextToken(JSONToken.COMMA);
                break;
            }
        }
        return (T) invocation;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }

    private void deserializeArgs(Invocation invocation,DefaultJSONParser parser,Class[] argsClass){
        JSONReader reader = new JSONReader(parser);
        reader.startArray();
        int i = 0 ;
        Object[] actualArgsObjects = new Object[argsClass.length];
        while ( reader.hasNext() ){
            if ( i >= actualArgsObjects.length ){
                throw new JSFCodecException(String.format("the method:%s of %s argument length is:%s,but the actual args length larger than:%s",
                        invocation.getMethodName(),
                        invocation.getClazzName(),
                        actualArgsObjects.length,
                        actualArgsObjects.length));
            }
            actualArgsObjects[i] = reader.readObject(argsClass[i]);
            i++;
        }
        reader.endArray();
        //reader.close();//DefaultJSONParser will close
        invocation.setArgs(actualArgsObjects);

    }
}