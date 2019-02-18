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

public class GroupAttributes {
    private int mColor;
    private int mGroupMarginTop;
    private int mGroupMarginBottom;

    private boolean mGroupHead;
    private int mGroupSize;


    public GroupAttributes() {
        reset();
    }

    public void reset() {
        mColor = Integer.MIN_VALUE;
        mGroupMarginBottom = 0;
        mGroupMarginTop = 0;
        mGroupHead = false;
        mGroupSize = 0;
    }

    public int color() {
        return mColor;
    }

    int groupMarginTop() {
        return mGroupMarginTop;
    }

    int groupMarginBottom() {
        return mGroupMarginBottom;
    }

    boolean isGroupHead() {
        return mGroupHead;
    }

    int groupSize() {
        return mGroupSize;
    }


    public GroupAttributes setColor(int color) {
        mColor = color;
        return this;
    }

    public GroupAttributes setGroupMarginTop(int groupMarginTop) {
        mGroupMarginTop = groupMarginTop;
        return this;
    }

    public GroupAttributes setGroupMarginBottom(int groupMarginBottom) {
        mGroupMarginBottom = groupMarginBottom;
        return this;
    }

    GroupAttributes setGroupHead(boolean groupHead) {
        mGroupHead = groupHead;
        return this;
    }

    GroupAttributes setGroupSize(int groupSize) {
        mGroupSize = groupSize;
        return this;
    }

    public GroupAttributes setNonGroupValues(GroupAttributes other) {
        mColor = other.mColor;
        // NOTE: we don't copy header parameters not related to format
        return this;
    }

    public GroupAttributes setTo(GroupAttributes other) {
        mColor = other.mColor;
        mGroupMarginBottom = other.mGroupMarginBottom;
        mGroupMarginTop = other.mGroupMarginTop;

        mGroupHead = other.mGroupHead;
        mGroupSize = other.mGroupSize;
        return this;
    }
}