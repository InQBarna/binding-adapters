/* 
 * Copyright 2014 InQBarna Kenkyuu Jo SL 
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

package com.inqbarna.iqloaders;

import com.inqbarna.iqloaders.paged.PageProvider;

import java.util.Collection;

/**
 * Created by David on 30/09/14.
 */
public class IQProviders {

    public static <T> IQProvider<T> fromError(final Throwable error) {
        return new IQProvider<T>() {
            @Override
            public T get() throws Throwable {
                throw error;
            }
        };
    }

    public static <T> IQProvider<T> fromResult(final T result) {
        return new IQProvider<T>() {
            @Override
            public T get() throws Throwable {
                return result;
            }
        };
    }

    public static <T> PageProvider<T> pageFromResult(final Collection<T> elements, final boolean completed, final int page) {
        return new PageProvider<T>() {
            @Override
            public boolean isCompleted() {
                return completed;
            }

            @Override
            public int getCurrentPage() {
                return page;
            }

            @Override
            public Collection<T> get() throws Throwable {
                return elements;
            }
        };
    }

    public static <T> PageProvider<T> pageFromError(final Throwable error) {
        return new PageProvider<T>() {
            @Override
            public boolean isCompleted() {
                return true;
            }

            @Override
            public int getCurrentPage() {
                return 0;
            }

            @Override
            public Collection<T> get() throws Throwable {
                throw error;
            }
        };
    }
}
