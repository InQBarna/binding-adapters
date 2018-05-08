package com.inqbarna.libsamples;

import android.support.annotation.NonNull;
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
