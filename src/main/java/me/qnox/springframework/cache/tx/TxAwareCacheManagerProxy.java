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
import org.springframework.cache.CacheManager;

import java.util.Collection;

/**
 * Proxy for a target {@link CacheManager}, exposing transaction-aware {@link Cache} objects
 * which synchronize their {@link Cache#put} operations with Spring-managed transactions.
 *
 * @author Anton Efimchuk
 */
public class TxAwareCacheManagerProxy implements CacheManager {
    private final CacheManager targetCacheManager;

    public TxAwareCacheManagerProxy(CacheManager targetCacheManager) {
        this.targetCacheManager = targetCacheManager;
    }

    @Override
    public Cache getCache(String name) {
        Cache cache = targetCacheManager.getCache(name);
        return new TxAwareCacheDecorator(cache);
    }

    @Override
    public Collection<String> getCacheNames() {
        return targetCacheManager.getCacheNames();
    }

}
