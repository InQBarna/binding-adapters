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

package com.inqbarna.libsamples;

import androidx.annotation.NonNull;
import android.view.ViewGroup;

import com.inqbarna.adapters.BasicPagerAdapter;

import java.util.List;

import timber.log.Timber;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 07/05/2018
 */
public class MyAdapter extends BasicPagerAdapter<TestPagerAdapter.PageVM> {
    public MyAdapter(List<String> data) {
        super(BR.model);
        setData(data, TestPagerAdapter.PageVM::new);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        super.setPrimaryItem(container, position, object);
        Timber.d("Yeep, setting primary item pos: %d", position);
    }
}
