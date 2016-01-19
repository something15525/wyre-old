package com.somexapps.wyre.utils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Copyright 2015 Michael Limb
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Code taken from http://stackoverflow.com/a/13179983
 */
public class RequestCodeGenerator {
    private static final AtomicInteger seed = new AtomicInteger();
    public static int getFreshInt() {
        return seed.incrementAndGet();
    }
}
