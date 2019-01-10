package com.inqbarna.adapters;

import androidx.annotation.NonNull;

import java.util.List;

/**
 * @author David García <david.garcia@inqbarna.com>
 * @version 1.0 31/1/17
 */

public interface NestableMarker<T extends NestableMarker<T>> extends TypeMarker {
    @NonNull List<T> children();
    @NonNull Object getKey();
}
