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
package com.ipd.jsf.gd.filter.validation;

import java.util.concurrent.ConcurrentHashMap;
import javax.validation.ValidationException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ipd.jsf.gd.error.InitErrorException;

/**
 * Title: 校验器生成工厂<br>
 * <p/>
 * Description: <br>
 * <p/>
 */
public class ValidatorFactory {

    /**
     * slf4j Logger for this class
     */
    private final static Logger LOGGER = LoggerFactory.getLogger(ValidatorFactory.class);

    /**
     * The validator cache.
     */
    private static ConcurrentHashMap<String, Validator> validatorMap = new ConcurrentHashMap<String, Validator>();

    /**
     * Gets validator.
     *
     * @param className
     *         the interface name
     * @param customImpl
     *         the custom impl
     * @return the validator
     */
    public static Validator getValidator(String className, String customImpl) {

        Validator validator = validatorMap.get(className);
        if (validator == null) {
            try {
                LOGGER.info("build validator for {}", className);
                validator = new Jsr303Validator(className, customImpl);
                Validator vd = validatorMap.putIfAbsent(className, validator);
                if (vd != null) {
                    validator = vd;
                }
            } catch (ValidationException e) {
                throw new InitErrorException("The ValidatorFactory cannot be built", e);
            } catch (ClassNotFoundException e) {
                throw new InitErrorException("");
            }
        }
        return validator;
    }
}