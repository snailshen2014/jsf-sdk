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

import com.ipd.jsf.gd.config.RegistryConfig;
import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

import com.ipd.jsf.gd.config.ParameterConfig;

/**
 * Title: 继承NamespaceHandlerSupport，将xml的标签绑定到解析器<br>
 *
 * Description: 在META-INF下增加spring.handlers和spring.schemas<br>
 */
public class JSFNamespaceHandler extends NamespaceHandlerSupport {
	
	@Override
	public void init() {
		registerBeanDefinitionParser("provider", new JSFBeanDefinitionParser(ProviderBean.class, true));
		registerBeanDefinitionParser("consumer", new JSFBeanDefinitionParser(ConsumerBean.class, true));
		registerBeanDefinitionParser("consumerGroup", new JSFBeanDefinitionParser(ConsumerGroupBean.class, true));
		registerBeanDefinitionParser("server", new JSFBeanDefinitionParser(ServerBean.class, true));
		registerBeanDefinitionParser("registry", new JSFBeanDefinitionParser(RegistryConfig.class, true));
        registerBeanDefinitionParser("annotation", new JSFBeanDefinitionParser(AnnotationBean.class, true));
        //registerBeanDefinitionParser("method", new JSFBeanDefinitionParser(MethodConfig.class, false));
        registerBeanDefinitionParser("parameter", new JSFParameterDefinitionParser(ParameterConfig.class));
		registerBeanDefinitionParser("filter", new JSFBeanDefinitionParser(FilterBean.class, true));
	}
}