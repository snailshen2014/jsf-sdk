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

import javax.xml.bind.annotation.adapters.XmlAdapter;

import com.ipd.jsf.gd.registry.Provider;

/**
 * Title: <br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ProviderAdapter extends XmlAdapter<String,Provider> {


    @Override
    public Provider unmarshal(String v) throws Exception {
        return Provider.valueOf(v);
    }

    @Override
    public String marshal(Provider v) throws Exception {
        return v.toUrl();
    }
}