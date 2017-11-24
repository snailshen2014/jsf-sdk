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
package com.ipd.jsf.gd.codec.msgpack;

import java.io.IOException;

import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.jsf.gd.msg.ResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.unpacker.Unpacker;

/**
 * Created on 14-4-24.
 */
public class ResponseTemplate extends JSFAbstractTemplate<ResponseMessage> {

    private static Logger logger = LoggerFactory.getLogger(ResponseTemplate.class);

    private JSFExceptionTemplate<Throwable> exceptionTemplate = JSFExceptionTemplate.getInstance();


    @Override
    public void write(Packer pk, ResponseMessage v, boolean required) throws IOException {
        if (v == null) {
            if (required) {
                throw new NullPointerException();
            }
            pk.writeNil();
            return;
        }
        Object response = v.getResponse();
        Throwable exception = v.getException();

        pk.writeArrayBegin(2);
        {
//           pk.write(className);
//           pk.write(response);
           writeValue(pk,response);
           if(exception == null){
               pk.writeNil();
           }else {

               exceptionTemplate.write(pk,exception);

           }
        }

        pk.writeArrayEnd();




    }

    public ResponseTemplate() {
    }

    @Override
    public ResponseMessage read(Unpacker u, ResponseMessage to, boolean required) throws IOException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        if (to == null) {
            to = new ResponseMessage();
        }
        u.readArrayBegin();
        {
            //String className = u.readString();
            try {
                Object response = readValue(u);

                to.setResponse(response);

                Throwable tr = u.read(exceptionTemplate);
                to.setException(tr);
            } catch (MessageTypeException e) {
                throw new JSFCodecException(e.getMessage(), e);
            } catch (JSFCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new IOException(e);
            }
        }
        u.readArrayEnd();
        return to;
    }
}