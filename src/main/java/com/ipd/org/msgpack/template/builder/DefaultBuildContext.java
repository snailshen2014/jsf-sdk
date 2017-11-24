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
package com.ipd.org.msgpack.template.builder;

import com.ipd.jsf.gd.util.*;
import com.ipd.org.msgpack.template.builder.beans.ParamsBean;
import com.ipd.jsf.gd.codec.msgpack.JSFMapTemplate;
import com.ipd.jsf.gd.error.JSFCodecException;
import com.ipd.org.msgpack.MessageTypeException;
import com.ipd.org.msgpack.template.Template;
import com.ipd.org.msgpack.unpacker.MessagePackUnpacker;
import javassist.*;

import java.lang.reflect.*;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DefaultBuildContext extends BuildContext<FieldEntry> {
    protected FieldEntry[] entries;

    protected Class<?> origClass;

    protected String origName;

    protected Template<?>[] templates;

    public DefaultBuildContext(JavassistTemplateBuilder director) {
        super(director);
    }

    public Template buildTemplate(Class targetClass, FieldEntry[] entries,
            Template[] templates) {
        this.entries = entries;
        this.templates = templates;
        this.origClass = targetClass;
        this.origName = ClassTypeUtils.getTypeStr(origClass);
        return build(origName);
    }

    protected void setSuperClass() throws CannotCompileException, NotFoundException {
        tmplCtClass.setSuperclass(director.getCtClass(
                JavassistTemplateBuilder.JavassistTemplate.class.getName()));
    }

    protected void buildConstructor() throws CannotCompileException,
            NotFoundException {

        // Constructor(Class targetClass, Template[] templates)
        CtConstructor newCtCons = CtNewConstructor.make(
                new CtClass[] {
                        director.getCtClass(Class.class.getName()),
                        director.getCtClass(Template.class.getName() + "[]")
                }, new CtClass[0], tmplCtClass);
        //TODO jsf add
        newCtCons.insertAfter(buildFields());
        newCtCons.insertAfter(initInstanceField());
        tmplCtClass.addConstructor(newCtCons);
    }

    //TODO jsf add
    private String buildFields() throws CannotCompileException{
        String origClassName = ClassTypeUtils.getTypeStr(origClass);
        String fieldBeanClassName = ClassTypeUtils.getTypeStr(FieldBean.class);
        CtField field = CtField.make("private " + fieldBeanClassName + "[] fieldList;\n", tmplCtClass);

        tmplCtClass.addField(field);
        StringBuilder init_body = new StringBuilder();
        init_body.append("{\n").append("fieldList = new " + fieldBeanClassName + "["+entries.length+"];\n");
        for(int i=0; i < entries.length; i++){
            init_body.append(Field.class.getCanonicalName() + " field_" + entries[i].getName() + " = ");
            String fieldClassName = ClassTypeUtils.getTypeStr(((DefaultFieldEntry)entries[i]).getField().getDeclaringClass());
            if(fieldClassName.equals(origClassName)){
                init_body.append(origClassName + ".class.getDeclaredField(\"" + entries[i].getName() + "\");\n");
            } else {
                Class tempClass = this.origClass;
                while(true){
                    if(!fieldClassName.equals(ClassTypeUtils.getTypeStr(tempClass))){
                        tempClass = tempClass.getSuperclass();
                        continue;
                    }
                    init_body.append(ClassTypeUtils.getTypeStr(tempClass) + ".class.getDeclaredField(\"" + entries[i].getName() + "\");\n");
                    break;
                }
            }
            init_body.append(" field_" + entries[i].getName() + ".setAccessible(true);\n");
            init_body.append("fieldList[" + i + "] = new " + fieldBeanClassName + "(field_" + entries[i].getName() + ");\n");
        }
        init_body.append("\n}\n");
        return init_body.toString();
    }

    //TODO JSF add
    private String initInstanceField() throws CannotCompileException {


        String conClassName = ClassTypeUtils.getTypeStr(Constructor.class);
        String objClassName = ClassTypeUtils.getTypeStr(ParamsBean.class);
        //定义field
        CtField _constructorField = CtField.make("private " + conClassName + " _constructor;\n",
                tmplCtClass);

        tmplCtClass.addField(_constructorField);

        CtField argsField = CtField.make("private "+objClassName+" _consArgList = new "+objClassName+"();\n",
                tmplCtClass);
        tmplCtClass.addField(argsField);

        //增加处理方法
        String origClassName = ClassTypeUtils.getTypeStr(origClass);

        StringBuilder initBuf = new StringBuilder();
        initBuf.append(conClassName).append(" constructors[] = ").append(origClassName).append("" +
                ".class.getDeclaredConstructors();\n");

        initBuf.append("long bestCost = Long.MAX_VALUE;\n");

        initBuf.append("for (int i = 0; i < constructors.length; i++) {\n");
        initBuf.append("    Class param[] = constructors[i].getParameterTypes();\n");
        initBuf.append("    long cost = 0;\n");

        initBuf.append("    for (int j = 0; j < param.length; j++) {\n");
        initBuf.append("        cost = 4 * cost;\n");

        initBuf.append("        if (Object.class.equals(param[j]))\n");
        initBuf.append("            cost += 1;\n");
        initBuf.append("        else if (String.class.equals(param[j]))\n");
        initBuf.append("            cost += 2;\n");
        initBuf.append("        else if (int.class.equals(param[j]))\n");
        initBuf.append("            cost += 3;\n");
        initBuf.append("        else if (long.class.equals(param[j]))\n");
        initBuf.append("            cost += 4;\n");
        initBuf.append("        else if (param[j].isPrimitive())\n");
        initBuf.append("            cost += 5;\n");
        initBuf.append("        else\n");
        initBuf.append("            cost += 6;\n");
        initBuf.append("    }\n");

        initBuf.append("    if (cost < 0 || cost > (1 << 48))\n");
        initBuf.append("        cost = 1 << 48;\n");

        initBuf.append("    cost += (long) param.length << 48;\n");

        initBuf.append("    if (cost < bestCost) {\n");
        initBuf.append("        _constructor = constructors[i];\n");
        initBuf.append("        bestCost = cost;\n");
        initBuf.append("    }\n");
        initBuf.append("}\n");

        initBuf.append("if (_constructor != null) {\n");
        initBuf.append("    _constructor.setAccessible(true);\n");
        initBuf.append("    Class []params = _constructor.getParameterTypes();\n");
        initBuf.append("    for (int i = 0; i < params.length; i++) {\n");
        initBuf.append("        _consArgList.add(").append(CodecUtils.class.getCanonicalName())
                .append(".getDefParamArg(params[i]));\n");
        initBuf.append("    }\n");
        initBuf.append("}\n");

        String methodClassName = Method.class.getCanonicalName();
        CtField methodField = CtField.make("private " + methodClassName + " _readResolve = null;\n", tmplCtClass);
        tmplCtClass.addField(methodField);

        initBuf.append(Class.class.getCanonicalName() + " _rrCl =" + origClassName + ".class;\n");
        initBuf.append("for (;_rrCl != null; _rrCl = _rrCl.getSuperclass()) {\n");
        initBuf.append("    " + methodClassName + " methods[] = _rrCl.getDeclaredMethods();\n");
        initBuf.append("    for (int i = 0; i < methods.length; i++) {\n");
        initBuf.append("        " + methodClassName + " method = methods[i];\n");
        initBuf.append("        if (method.getName().equals(\"readResolve\") &&\n");
        initBuf.append("            method.getParameterTypes().length == 0){\n");
        initBuf.append("            _readResolve = method; \n");
        initBuf.append("            _readResolve.setAccessible(true);break; \n");
        initBuf.append("        }\n");
        initBuf.append("    }\n");
        initBuf.append("    if(_readResolve != null)break;\n");
        initBuf.append("}");
        return initBuf.toString();
    }

    protected Template buildInstance(Class<?> c) throws NoSuchMethodException,
            InstantiationException, IllegalAccessException,
            InvocationTargetException {
        Constructor<?> cons = c.getConstructor(new Class[] { Class.class, Template[].class });
        Object tmpl = cons.newInstance(new Object[] { origClass, templates });
        return (Template) tmpl;
    }

    protected void buildMethodInit() {
    	StringBuilder initMethod = new StringBuilder();
    	initMethod.append("private ").append(Field.class.getCanonicalName())
    		.append(" ").append(origClass.getSimpleName()).append("_fields[];");
    }

    protected String buildWriteMethodBody() {
        resetStringBuilder();
        buildString("\n{\n");

        buildString("  if ($2 == null) {\n");
        buildString("    if ($3) {\n");
        buildString("      throw new %s(\"Attempted to write null\");\n", MessageTypeException.class.getName());
        buildString("    }\n");
        buildString("    $1.writeNil();\n");
        buildString("    return;\n");
        buildString("  }\n");

        buildString("if($1.addRef($2)){\n");
        buildString("   return;\n");
        buildString("}\n");

        buildString("  %s _$$_t = (%s) $2;\n", origName, origName);
        buildString("  $1.writeArrayBegin(%d);\n", entries.length + 1);
        // 要发送给的服务端的jsf版本
        buildString("   %s jsfVersion = (%s) %s.getContext().getAttachment(%s.HIDDEN_KEY_DST_JSF_VERSION);\n",Short.class.getName(),Short.class.getName(), RpcContext.class.getName(), Constants.class.getName());
        //检查父子类
        buildString("  "+Map.class.getName()+" jsf_checkMap = new "+HashMap.class.getName()+"();\n");
        //根据配置进行父子类检查
        if (JSFLogicSwitch.SETTING_SERIALIZE_CHECK_CLASS) {
            buildString("%s.checkMap(jsf_checkMap,_$$_t,fieldList);\n",
                    BuildContextUtil.class.getName()
            );
        }
        buildString(JSFMapTemplate.class.getName() + ".getInstance().write($1, jsf_checkMap);\n");

        for (int i = 0; i < entries.length; i++) {
            buildFieldWrite(i);
        }

        buildString("  $1.writeArrayEnd();\n");
        buildString("}\n");
        return getBuiltString();
    }

    protected String buildReadMethodBody() {
        resetStringBuilder();
        buildString("\n{\n");
        buildString("  if (!$3 && $1.trySkipNil()) {\n");
        buildString("    return null;\n");
        buildString("  }\n");

        buildString("  %s _$$_t;\n", origName);
        buildString("  if ($2 == null) {\n");
        buildString("   if(_constructor != null){\n");
        buildString("       _$$_t = _constructor.newInstance(_consArgList.getAll());\n");
        buildString("   } else {\n");
        buildString("    _$$_t =  %s.newInstance(%s.getClass(\"%s\"));\n", ClassLoaderUtils.class.getCanonicalName(),
        			ClassTypeUtils.class.getCanonicalName(), origName);
        buildString("   }\n");
        buildString("  } else {\n");
        buildString("    _$$_t = (%s) $2;\n", origName);
        buildString("  }\n");
        buildString("  int ref = $1.isRef();\n");
        buildString("  if(ref > -1){\n");
        buildString("     return (%s)$1.readRef(ref);\n", origName);
        buildString("  }\n");
        buildString("  $1.setRef(_$$_t);\n");

        buildString("  int refIndex = -1;"); // 记住当前第几个
        buildString("  " + MessagePackUnpacker.class.getName() + " unPacker = null;\n");
        buildString("  if(_readResolve != null && $1 instanceof " + MessagePackUnpacker.class.getName() + ") {\n");
        buildString("    unPacker = (" + MessagePackUnpacker.class.getName() + ") $1;\n");
        buildString("    refIndex = unPacker.getRefs().size() -1;\n");
        buildString("  }\n");

        //TODO
        buildString("  %s curField = null;\n", FieldBean.class.getCanonicalName());
        buildString("  try{\n");
        buildString("  int fieldLen = $1.readArrayBegin()-1;\n");
        // 要发送给的服务端的jsf版本
        buildString("   %s jsfVersion = (%s) %s.getContext().getAttachment(%s.HIDDEN_KEY_DST_JSF_VERSION);\n",Short.class.getName(),Short.class.getName(), RpcContext.class.getName(), Constants.class.getName());
        buildString(" "+Map.class.getName()+" jsf_checkMap = " + JSFMapTemplate.class.getName() + ".getInstance().read($1, null);\n");
        for (int i = 0; i < entries.length; i++) {
            buildFieldRead(i);
        }

        buildString("  $1.readArrayEnd();\n");
        buildString("   }catch(%s mte){\n", Exception.class.getName());
        buildString("       if(mte instanceof %s){\n", MessageTypeException.class.getCanonicalName());
        buildString("           if(curField == null){\n");
        buildString("               throw new %s(\"The data cannot convert to [%s].\",mte);\n", JSFCodecException.class.getCanonicalName(), origName);
        buildString("           } else {\n");
        buildString("               throw new %s(\"Class:[%s] FieldName:[\"+curField.getName()+\"] cannot be " +
                "resolved.\", mte);\n", JSFCodecException.class.getCanonicalName(), origName);
        buildString("           }\n");
        buildString("       } else {\n");
        buildString("               throw new %s(\"Class:[%s] FieldName:[\"+curField.getName()+\"] cannot be resolved.\", mte);\n", JSFCodecException.class.getCanonicalName(), origName);
        buildString("       }\n");
        buildString("   }\n");

        buildString("  if(_readResolve != null){\n");
        buildString("   _$$_t = _readResolve.invoke(_$$_t, new Object[0]);\n");
        buildString("   if(refIndex>=0){\n");
        buildString("    unPacker.getRefs().set(refIndex, _$$_t);\n"); // 旧的值已被替换，根据index更新refs里
        buildString("   }\n");
        buildString("  }\n");

        buildString("  return _$$_t;\n");

        buildString("}\n");
        return getBuiltString();
    }

    @Override
    public void writeTemplate(Class<?> targetClass, FieldEntry[] entries,
            Template[] templates, String directoryName) {
        this.entries = entries;
        this.templates = templates;
        this.origClass = targetClass;
        this.origName = origClass.getName();
        write(origName, directoryName);
    }

    @Override
    public Template loadTemplate(Class<?> targetClass, FieldEntry[] entries, Template[] templates) {
        this.entries = entries;
        this.templates = templates;
        this.origClass = targetClass;
        this.origName = origClass.getName();
        return load(origName);
    }


    /**
     * 生成write字段模板部分
     * @param i
     */
    private void buildFieldWrite(int i){
        FieldEntry e = entries[i];
        if (!e.isAvailable()) {
            buildString("  $1.writeNil();\n");
            return;
        }
        DefaultFieldEntry de = (DefaultFieldEntry) e;
        boolean isPublic = Modifier.isPublic(de.getField().getModifiers());
        Class<?> type = de.getType();
        if ( type.isPrimitive() && isPublic ){
//            buildString("  $1.%s(_$$_t.%s);\n", primitiveWriteName(type), de.getName());
            buildString("%s.doPPFieldWrite($1,_$$_t.%s);\n",
                    BuildContextUtil.class.getName(),
                    de.getName()
                    );
        } else {
            buildString("%s.doFieldWrite(_templates,%d,$1,$2,$3,%s,%s,%s,fieldList[%d],jsfVersion,jsf_checkMap);\n",
                    BuildContextUtil.class.getName(),
                    i,
                    isPublic,
                    type.isPrimitive(),
                    de.isNotNullable(),
                    i
            );
        }
    }


    /**
     * 生成read字段模板部分
     * @param i
     */
    private void buildFieldRead(int i){
        FieldEntry e = entries[i];
        if (!e.isAvailable()) {
            buildString("  $1.skip();\n");
            return;
        }
        DefaultFieldEntry de = (DefaultFieldEntry) e;
        Class<?> type = de.getType();
        buildString("%s.doFieldRead(_templates,%d,fieldLen,$1,_$$_t,%s,%s,fieldList[%d],jsfVersion,jsf_checkMap);\n",
                BuildContextUtil.class.getName(),
                i,
                type.isPrimitive(),
                e.isOptional(),
                i
        );
    }
}
