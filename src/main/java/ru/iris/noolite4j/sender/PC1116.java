/*
 * Copyright 2014 Nikolay A. Viguro
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.iris.noolite4j.sender;

import java.util.HashMap;
import java.util.Map;

public class PC1116 extends PC11xx {

    Map<Short, ?> devices = new HashMap<Short, Object>(16);

    public PC1116() {

        super();

        /**
         * PC1116
         */
        availableChannels = 16;
    }
}
