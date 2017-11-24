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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.ipd.jsf.gd.config.RegistryConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.ipd.jsf.gd.config.ConsumerConfig;
import com.ipd.jsf.gd.filter.AbstractFilter;
import com.ipd.jsf.gd.util.CommonUtils;

/**
 * Title: 需要实现FactoryBean和DisposableBean<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
@SuppressWarnings("rawtypes")
public class ConsumerBean<T> extends ConsumerConfig<T> implements InitializingBean, FactoryBean,
        ApplicationContextAware, DisposableBean, BeanNameAware {
    /**
     *
     */
    private static final long serialVersionUID = 6835324481364430812L;

    /**
     * slf4j logger for this class
     */
    private final static Logger LOGGER = LoggerFactory
            .getLogger(ConsumerBean.class);

    /**
     * 默认构造函数，不允许从外部new
     */
    protected ConsumerBean() {

    }

    private ApplicationContext applicationContext;

    private transient String beanName;

    private transient T object;

    private transient Class objectType;

    /**
     * @param name
     * @see org.springframework.beans.factory.BeanNameAware#setBeanName(java.lang.String)
     */
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    /**
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    @Override
    public void setApplicationContext(ApplicationContext appContext)
            throws BeansException {
        this.applicationContext = appContext;
    }

    /**
     * 根据config实例化所需的Reference实例<br/>
     * 返回的应该是具备全操作能力的接口代理实现类对象
     *
     * @see org.springframework.beans.factory.FactoryBean#getObject()
     */
    @Override
    public T getObject() throws Exception {
        object = CommonUtils.isUnitTestMode() ? null : refer();
        return object;
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        if (applicationContext != null) {
            // 如果没有配置注册中心，则默认订阅全部注册中心
            if (getRegistry() == null) {
                Map<String, RegistryConfig> registryMaps = applicationContext
                        .getBeansOfType(RegistryConfig.class, false, false);
                List<RegistryConfig> registryLists = null;
                if (CommonUtils.isNotEmpty(registryMaps)) {
                    Collection<RegistryConfig> registryConfigs = registryMaps.values();
                    if (CommonUtils.isNotEmpty(registryConfigs)) {
                        registryLists = new ArrayList<RegistryConfig>(registryConfigs);
                    }
                }
                super.setRegistry(registryLists);
            }
            // 看有没有全局过滤器配置
            Map<String, FilterBean> registryMaps = applicationContext
                    .getBeansOfType(FilterBean.class, false, false);
            for (Map.Entry<String, FilterBean> entry : registryMaps.entrySet()) {
                FilterBean filterBean = entry.getValue();
                if (filterBean.containsConsumer(beanName)) {
                    List<AbstractFilter> filters = getFilter();
                    if (filters == null) {
                        filters = new ArrayList<AbstractFilter>();
                        filters.add(filterBean.getRef());
                        setFilter(filters);
                    } else {
                        filters.add(filterBean.getRef());
                    }
                }
            }
        }
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#getObjectType()
     */
    @Override
    public Class getObjectType() {
        try {
            // 如果spring注入在前，reference操作在后，则会提前走到此方法，此时interface为空
            objectType = super.getProxyClass();
        } catch (Exception e) {
            objectType = null;
        }
        return objectType;
    }

    /**
     * @see org.springframework.beans.factory.FactoryBean#isSingleton()
     */
    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * @see org.springframework.beans.factory.DisposableBean#destroy()
     */
    @Override
    public void destroy() throws Exception {
        LOGGER.info("JSF destroy consumer with bean name : {}", beanName);
        super.unrefer();
    }

}