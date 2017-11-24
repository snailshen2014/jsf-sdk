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
import java.util.Date;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
@XmlRootElement(name = "fileRegistry")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileRegistryVo implements Serializable {


    /**
     * The Consumer vos.
     */
    @XmlElements(@XmlElement(name = "consumer", type = ConsumerVo.class))
    private List<ConsumerVo> consumerVos;

    /**
     * The jsf version.
     */
    @XmlAttribute
    private String jsfVersion;

    /**
     * The Backup time.
     */
    @XmlAttribute
    @XmlJavaTypeAdapter(DateAdapter.class)
    private Date backupTime;

    /**
     * Gets consumer vos.
     *
     * @return the consumer vos
     */
    public List<ConsumerVo> getConsumerVos() {
        return consumerVos;
    }

    /**
     * Sets consumer vos.
     *
     * @param consumerVos  the consumer vos
     */
    public void setConsumerVos(List<ConsumerVo> consumerVos) {
        this.consumerVos = consumerVos;
    }

    /**
     * Gets jsf version.
     *
     * @return the jsf version
     */
    public String getJsfVersion() {
        return jsfVersion;
    }

    /**
     * Sets jsf version.
     *
     * @param jsfVersion  the jsf version
     */
    public void setJsfVersion(String jsfVersion) {
        this.jsfVersion = jsfVersion;
    }

    /**
     * Gets backup time.
     *
     * @return the backup time
     */
    public Date getBackupTime() {
        return backupTime;
    }

    /**
     * Sets backup time.
     *
     * @param backupTime  the backup time
     */
    public void setBackupTime(Date backupTime) {
        this.backupTime = backupTime;
    }
}