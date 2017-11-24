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
package com.ipd.jsf.gd.config.spring;

import com.ipd.jsf.gd.util.JSFContext;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.BeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import com.ipd.jsf.gd.util.CommonUtils;
import com.ipd.jsf.gd.util.Constants;

/**
 * Title: 普通的keyvalue形式的介绍<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class JSFParameterDefinitionParser implements BeanDefinitionParser {

    private Class beanClass;

    public JSFParameterDefinitionParser(Class beanClass) {
        this.beanClass = beanClass;
    }

    @Override
    public BeanDefinition parse(Element element, ParserContext parserContext) {
        RootBeanDefinition beanDefinition = new RootBeanDefinition();
        beanDefinition.setBeanClass(beanClass);
        beanDefinition.setLazyInit(false);

        String key = element.getAttribute("key");
        String value = element.getAttribute("value");
        String hide = element.getAttribute("hide");
        if(CommonUtils.isTrue(hide)){
            JSFContext.putGlobalVal(Constants.HIDE_KEY_PREFIX + key, value);
        } else {
            JSFContext.putGlobalVal(key, value);
        }

        beanDefinition.getPropertyValues().addPropertyValue("key", key);
        beanDefinition.getPropertyValues().addPropertyValue("value", value);
        beanDefinition.getPropertyValues().addPropertyValue("hide", Boolean.valueOf(hide));

        return beanDefinition;
    }
}