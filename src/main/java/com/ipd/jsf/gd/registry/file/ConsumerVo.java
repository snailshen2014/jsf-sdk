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
package com.ipd.jsf.gd.registry.file;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.ipd.jsf.gd.registry.Provider;

/**
 * Title: 代表一个订阅者<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
@XmlRootElement(name="interface")
@XmlAccessorType(XmlAccessType.FIELD)
public class ConsumerVo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 8400259053036022236L;

	/**
     * The Interface id.
     */
    @XmlAttribute(name = "interface")
	private String interfaceId;

    /**
     * The Alias.
     */
    @XmlAttribute
	private String alias;

    /**
     * The Protocol.
     */
    @XmlAttribute
	private String protocol;

    /**
     * The Providers.
     */
    @XmlElementWrapper
    @XmlElement(name="provider")
    @XmlJavaTypeAdapter(ProviderAdapter.class)
    private List<Provider> providers;

    /**
     * Gets interface id.
     *
     * @return the interface id
     */
    public String getInterfaceId() {
        return interfaceId;
    }

    /**
     * Sets interface id.
     *
     * @param interfaceId  the interface id
     */
    public void setInterfaceId(String interfaceId) {
        this.interfaceId = interfaceId;
    }

    /**
     * Gets alias.
     *
     * @return the alias
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Sets alias.
     *
     * @param alias  the alias
     */
    public void setAlias(String alias) {
        this.alias = alias;
    }

    /**
     * Gets protocol.
     *
     * @return the protocol
     */
    public String getProtocol() {
        return protocol;
    }

    /**
     * Sets protocol.
     *
     * @param protocol  the protocol
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Gets providers.
     *
     * @return the providers
     */
    public List<Provider> getProviders() {
        return providers;
    }

    /**
     * Sets providers.
     *
     * @param providers  the providers
     */
    public void setProviders(List<Provider> providers) {
        this.providers = providers;
    }
}