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

package com.inqbarna.adapters;

/**
 * Created by David García <david.garcia@inqbarna.com> on 3/02/17.
 */

public interface TreeNode<T extends NestableMarker<T>> {
    TreeNode<T> getParent();

    boolean isOpened();

    T getData();

    boolean open(boolean notify);

    boolean close(boolean notify);

    boolean closeChilds(boolean notify);

    boolean isChild(TreeNode<T> other, boolean findClosed);

    boolean openToChild(TreeNode<T> child, boolean notify);

    TreeNode<T> root();
}
