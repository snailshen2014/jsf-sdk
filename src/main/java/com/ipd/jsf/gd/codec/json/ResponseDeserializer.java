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

import com.ipd.fastjson.parser.DefaultJSONParser;
import com.ipd.fastjson.parser.Feature;
import com.ipd.fastjson.parser.JSONLexer;
import com.ipd.fastjson.parser.JSONToken;
import com.ipd.fastjson.parser.deserializer.ObjectDeserializer;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.JSFLogicSwitch;
import com.ipd.jsf.gd.msg.ResponseMessage;
import com.ipd.jsf.gd.util.CommonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 *
 * @since 2015/01/12 16:50
 */
public class ResponseDeserializer implements ObjectDeserializer,ResponseSerializable{

    private final static Logger logger = LoggerFactory.getLogger(ResponseDeserializer.class);

    public static final ResponseDeserializer instance = new ResponseDeserializer();

    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        if (CommonUtils.isNotEmpty(JSFLogicSwitch.JSON_PARSER_FEATURES)) {
            for (Feature feature : JSFLogicSwitch.JSON_PARSER_FEATURES) {
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

        ResponseMessage resMsg = new ResponseMessage();
        String responseClz = null;
        for (;;) {
            int token = -1;
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
            if (RES_CLASS.equals(key)) {
                token = lexer.token();
                if (token == JSONToken.LITERAL_STRING) {
                    responseClz = lexer.stringVal();
                }
                lexer.nextToken();
            } else if (RESPONSE.equals(key)) {
                if ( responseClz != null && !"".equals(responseClz)){
                    try {
                        Class responseClass = ClassTypeUtils.getClass(responseClz);
                        resMsg.setResponse(parser.parseObject(responseClass));
                    } catch (RuntimeException e) {
                        logger.error(" deserialize response msg error",e);
                    }
                }
            } else if (EXCEPTION.equals(key)) {
               if ( responseClz != null && !"".equals(responseClz)){
                    Class exceptionClass = ClassTypeUtils.getClass(responseClz);
                    if ( Throwable.class.isAssignableFrom(exceptionClass)){
                        resMsg.setException((Throwable) parser.parseObject(exceptionClass));
                    } else {
                        resMsg.setException(parser.parseObject(Throwable.class));
                    }
                } else {
                    resMsg.setException(parser.parseObject(Throwable.class));
                }
            }
            token = lexer.token();
            if (token == JSONToken.RBRACE) {
                lexer.nextToken(JSONToken.COMMA);
                break;
            }
        }
        return (T) resMsg;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LBRACE;
    }

}