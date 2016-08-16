/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.qnox.springframework.cache.tx;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.transaction.support.ResourceHolderSupport;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

public class TxAwareCacheResourceHolder extends ResourceHolderSupport {

    private Set<Object> evictedKeys = new HashSet<>();

    private Map<Object, Object> putted = new HashMap<>();

    private boolean cleared = false;

    public Set<Object> getEvictedKeys() {
        return evictedKeys;
    }

    public Map<Object, Object> getPutted() {
        return putted;
    }

    public void evict(Object key) {
        evictedKeys.add(key);
    }

    public void put(Object key, Object value) {
        evictedKeys.remove(key);
        putted.put(key, value);
    }

    public boolean isEvicted(Object key) {
        return evictedKeys.contains(key);
    }

    public Cache.ValueWrapper get(Object key, Callable<Cache.ValueWrapper> valueWrapperSuppplier) {
        if (isEvicted(key)) {
            return null;
        }
        if (putted.containsKey(key)) {
            return new SimpleValueWrapper(putted.get(key));
        }
        Cache.ValueWrapper result;
        try {
            result = !cleared ? valueWrapperSuppplier.call() : null;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    public Cache.ValueWrapper putIfAbsent(Object key, Object value, Cache.ValueWrapper valueWrapper) {
        if (isEvicted(key)) {
            put(key, value);
            return null;
        }
        if (putted.containsKey(key)) {
            return new SimpleValueWrapper(putted.get(key));
        } else {
            if (cleared || valueWrapper == null) {
                put(key, value);
                return null;
            } else {
                return valueWrapper;
            }
        }
    }

    public void clearCache() {
        cleared = true;
        putted.clear();
        evictedKeys.clear();
    }

}
