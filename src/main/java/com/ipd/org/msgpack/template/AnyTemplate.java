//
// MessagePack for Java
//
// Copyright (C) 2009 - 2013 FURUHASHI Sadayuki
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package com.ipd.org.msgpack.template;

import java.io.IOException;
import java.util.HashSet;

import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.packer.Packer;
import com.ipd.org.msgpack.unpacker.Unpacker;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.ClassTypeUtils;
import com.ipd.jsf.gd.util.CodecUtils;

public class AnyTemplate<T> extends AbstractTemplate<T> {

    private TemplateRegistry registry;

    public AnyTemplate(TemplateRegistry registry) {
        this.registry = registry;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void write(Packer pk, T target, boolean required) throws IOException {
        if (target == null) {
            if (required) {
                throw new MessageTypeException("Attempted to write null");
            }
            pk.writeNil();
        } else {
            pk.writeArrayBegin(2);
            Class<?> clazz = target.getClass();
            pk.write(ClassTypeUtils.getTypeStr(clazz));
            Template temp = null;
            try {
                temp = registry.lookup(clazz);
            } catch (MessageTypeException e) {
                CodecUtils.checkAndRegistryClass(clazz, new HashSet<Class<?>>());
                temp = registry.lookup(clazz);
            }
            temp.write(pk, target);
            pk.writeArrayEnd();
        }
    }

    @Override
    public T read(Unpacker u, T to, boolean required) throws IOException,
            MessageTypeException {
        if (!required && u.trySkipNil()) {
            return null;
        }
        u.readArrayBegin();
        String className = u.readString();
        Class<?> clazz = null;
        try {
            clazz = ClassLoaderUtils.forName(className);
        } catch (Exception e) {
            throw new MessageTypeException("The class:[" + className + "] is invalid", e);
        }
        Template temp = null;
        try {
            temp = registry.lookup(clazz);
        } catch (MessageTypeException e) {
            CodecUtils.checkAndRegistryClass(clazz, new HashSet<Class<?>>());
            temp = registry.lookup(clazz);
        }
        if(to == null) {
            try {
                to = (T) ClassLoaderUtils.newInstance(clazz);
            } catch (Exception e) {
                to = null;
            }
        }
        T o = (T) temp.read(u, to);
        u.readArrayEnd();
        if (required && o == null) {
            throw new MessageTypeException("Unexpected nil value");
        }
        return o;
    }
}
