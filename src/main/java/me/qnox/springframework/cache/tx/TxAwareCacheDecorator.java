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
import org.springframework.transaction.support.ResourceHolderSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Cache decorator which synchronizes its {@link #put}, {@link #evict} and {@link #clear}
 * operations with Spring-managed transactions (through Spring's {@link TransactionSynchronizationManager},
 * performing the actual cache put/evict/clear operation only in the after-commit phase of a
 * successful transaction.
 *
 * <p>Decorator has following major differences from {@link org.springframework.cache.transaction.TransactionAwareCacheDecorator}:
 * <p><ul>
 *     <li>tracks evicted keys, and do not return values for them inside transaction</li>
 *     <li>postpone {@link #putIfAbsent} operation</li>
 * </ul></p>
 *
 * @see TxAwareCacheManagerProxy
 */
class TxAwareCacheDecorator implements Cache {

    private final Cache cache;

    public TxAwareCacheDecorator(Cache cache) {
        if (cache == null) {
            throw new IllegalArgumentException("cache cannot be null");
        }
        this.cache = cache;
    }

    @Override
    public String getName() {
        return cache.getName();
    }

    @Override
    public Object getNativeCache() {
        return cache.getNativeCache();
    }

    @Override
    public ValueWrapper get(final Object key) {
        return getHolder().get(key, new Callable<ValueWrapper>() {
            @Override
            public ValueWrapper call() {
                return cache.get(key);
            }
        });
    }

    @Override
    public <T> T get(final Object key, Class<T> type) {
        ValueWrapper obj = getHolder().get(key, new Callable<ValueWrapper>() {
            @Override
            public ValueWrapper call() {
                return cache.get(key);
            }
        });
        if (obj == null) {
            return null;
        }
        return type.cast(obj.get());
    }

    @Override
    public void put(Object key, Object value) {
        getHolder().put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        return getHolder().putIfAbsent(key, value, cache.get(key));
    }

    @Override
    public void evict(Object key) {
        getHolder().evict(key);
    }

    @Override
    public void clear() {
        getHolder().clearCache();
    }

    private TxAwareCacheResourceHolder getHolder() {
        TxAwareCacheResourceHolder result;
        if (!TransactionSynchronizationManager.hasResource(cache)) {
            TxAwareCacheResourceHolder value = new TxAwareCacheResourceHolder();
            TransactionSynchronizationManager.registerSynchronization(new ResourceHolderSynchronization<TxAwareCacheResourceHolder, Cache>(value, cache) {
                @Override
                protected boolean shouldReleaseBeforeCompletion() {
                    return false;
                }

                @Override
                protected void processResourceAfterCommit(TxAwareCacheResourceHolder resourceHolder) {
                    Map<Object, Object> putted = resourceHolder.getPutted();
                    for (Map.Entry<Object, Object> entry : putted.entrySet()) {
                        cache.put(entry.getKey(), entry.getValue());
                    }

                    Set<Object> evictedKeys = resourceHolder.getEvictedKeys();
                    for (Object evictedKey : evictedKeys) {
                        cache.evict(evictedKey);
                    }

                }
            });
            TransactionSynchronizationManager.bindResource(cache, value);
            result = value;
        } else {
            result = (TxAwareCacheResourceHolder) TransactionSynchronizationManager.getResource(cache);
        }
        return result;
    }
}
