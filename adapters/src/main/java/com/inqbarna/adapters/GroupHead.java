package com.inqbarna.adapters;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * @author David García (david.garcia@inqbarna.com)
 * @version 1.0 08/05/2017
 */
public interface GroupHead extends GroupItem {
    /**
     * Group size including Head and Tail of group.
     * @return
     */
    int groupSize();
}
