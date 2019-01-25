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

package com.inqbarna.rxutil.paging;

import com.inqbarna.common.paging.PaginateConfig;

import org.jetbrains.annotations.NotNull;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 19/9/16
 */

public class RxPagingConfig extends PaginateConfig {

    final boolean notifyAsInsertions;
    final ErrorHandlingModel errorHandlingModel;

    protected RxPagingConfig(Builder builder) {
        super(builder);
        notifyAsInsertions = builder.notifyInsertions;
        errorHandlingModel = builder.errorHandlingModel;
    }

    public static class Builder extends PaginateConfig.Builder {

        private boolean notifyInsertions = true;
        private ErrorHandlingModel errorHandlingModel;

        public Builder disableNotifyAsInsertions() {
            notifyInsertions = false;
            return this;
        }

        public Builder withErrorHandlingModel(ErrorHandlingModel model) {
            this.errorHandlingModel = model;
            return this;
        }

        public RxPagingConfig build() {
            if (null == errorHandlingModel) {
                errorHandlingModel = DEFAULT;
            }
            return new RxPagingConfig(this);
        }
    }

    private static final ErrorHandlingModel DEFAULT = new ErrorHandlingModel() {
        @Override
        public void processError(@NotNull PageErrorAction error) {
            error.abort();
        }
    };
}
