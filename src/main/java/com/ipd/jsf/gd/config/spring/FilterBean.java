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

import java.io.Serializable;

import com.ipd.jsf.gd.config.AbstractIdConfig;
import com.ipd.jsf.gd.error.IllegalConfigureException;
import com.ipd.jsf.gd.error.InitErrorException;
import com.ipd.jsf.gd.filter.AbstractFilter;
import com.ipd.jsf.gd.util.ClassLoaderUtils;
import com.ipd.jsf.gd.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.InitializingBean;

/**
 * Title: 需要实现InitializingBean和DisposableBean<br>
 *
 * Description: <br>
 */
public class FilterBean extends AbstractIdConfig implements InitializingBean, BeanNameAware, Serializable {
    
    private static final long serialVersionUID = 1858408193580927334L;

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(FilterBean.class);

    /**
     * 默认构造函数，不允许从外部new
     */
    protected FilterBean() {

    }

    private transient String beanName;

    /**
     * filter ref 比class属性的优先级更高，即如果两个属性都配置了已ref的为准
     */
    private AbstractFilter ref;

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        LOGGER.info("Find global filter: [{}], providers is {}, consumer is {}",
                new Object[]{beanName, providers, consumers});
        checkClass();
    }

    /**
     * The Providers.
     */
    private String providers = "*";

    /**
     * The Consumers.
     */
    private String consumers = "*";

    /**
     * The Clazz.
     */
    private String clazz;

    /**
     * 检查class
     */
    public void checkClass() {
        if (ref == null && StringUtils.isEmpty(clazz)) {
            throw new IllegalConfigureException(21702, "filter.class or ref", clazz,
                    "Both class and ref is null.please set class property or ref value");
        } else {
            if (ref != null){
                if (!(ref instanceof AbstractFilter)){
                    throw new IllegalConfigureException(21703, "filter.ref", ref.getClass().getCanonicalName(),
                            "Must extends " + AbstractFilter.class.getName() + " !");
                }
                return;
            }
            Class cls;
            try {
                cls = ClassLoaderUtils.forName(clazz);
            } catch (ClassNotFoundException e) {
                throw new InitErrorException("", e);
            }
            if (!AbstractFilter.class.isAssignableFrom(cls)) {
                throw new IllegalConfigureException(21703, "filter.class", clazz,
                        "Must extends " + AbstractFilter.class.getName() + " !");
            } else {

            }
        }
    }

    /**
     * Contains provider.
     *
     * @param beanId  the bean id
     * @return the boolean
     */
    public boolean containsProvider(String beanId) {
        return contains(providers, beanId);
    }

    /**
     * Contains consumer.
     *
     * @param beanId  the bean id
     * @return the boolean
     */
    public boolean containsConsumer(String beanId) {
        return contains(consumers, beanId);
    }

    /**
     * Contains boolean.
     *
     * @param all  the all
     * @param beanId  the bean id
     * @return the boolean
     */
    private boolean contains(String all, String beanId) {
        if("*".equals(all)){
            return true;
        }
        if(StringUtils.isEmpty(all)) {
            return false;
        }
        String[] ps = all.split(",");
        for (String p : ps) {
            if (beanId.equals(p)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets providers.
     *
     * @return the providers
     */
    public String getProviders() {
        return providers;
    }

    /**
     * Sets providers.
     *
     * @param providers  the providers
     */
    public void setProviders(String providers) {
        this.providers = providers;
    }

    /**
     * Gets consumers.
     *
     * @return the consumers
     */
    public String getConsumers() {
        return consumers;
    }

    /**
     * Sets consumers.
     *
     * @param consumers  the consumers
     */
    public void setConsumers(String consumers) {
        this.consumers = consumers;
    }

    /**
     * Gets clazz.
     *
     * @return the clazz
     */
    public String getClazz() {
        return clazz;
    }

    /**
     * Sets clazz.
     *
     * @param clazz  the clazz
     */
    public void setClazz(String clazz) {
        this.clazz = clazz;
    }

    public void setRef(AbstractFilter ref) {
        this.ref = ref;
    }

    /**
     * Gets ref.
     *
     * @return the ref
     */
    public AbstractFilter getRef() {
        if (ref != null){
            return ref;
        }
        try {
            return (AbstractFilter) ClassLoaderUtils.newInstance(ClassLoaderUtils.forName(clazz));
        } catch (Exception e) {
            throw new InitErrorException("Error when new instance of filter class :" + clazz, e);
        }
    }
}