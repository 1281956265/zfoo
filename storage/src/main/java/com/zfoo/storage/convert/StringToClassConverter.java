/*
 * Copyright (C) 2020 The zfoo Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package com.zfoo.storage.convert;

import com.zfoo.protocol.util.ClassUtils;
import com.zfoo.protocol.util.StringUtils;
import org.springframework.core.convert.converter.Converter;

/**
 * @author godotg
 */
public class StringToClassConverter implements Converter<String, Class<?>> {

    public static final StringToClassConverter INSTANCE = new StringToClassConverter();

    private StringToClassConverter() {
    }

    @Override
    public Class<?> convert(String source) {
        if (!source.contains(".") && !source.startsWith("[")) {
            source = "java.lang." + source;
        }

        try {
            return Class.forName(source, true, ClassUtils.getDefaultClassLoader());
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(StringUtils.format("Unable to convert string [{}] to Class object", source));
        }

    }
}
