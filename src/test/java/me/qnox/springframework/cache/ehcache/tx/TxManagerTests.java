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

package me.qnox.springframework.cache.ehcache.tx;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
public class TxManagerTests {

    @Autowired
    private CacheManagerService cacheManagerService;

    @Autowired
    private CacheManagerService txCacheManagerService;

    @Test
    public void testEhcacheTransactionManager() {
        String key1 = "key1";
        cacheManagerService.doTxLogic(key1);
        Assert.assertTrue(cacheManagerService.getValueFromLocalTxCache(key1) != null);
        Assert.assertTrue(cacheManagerService.getValueFromNoTxCache(key1) != null);

        String key2 = "key2";
        try {
            cacheManagerService.doTxLogicWithException(key2);
        } catch (RuntimeException e) {
            if (e.getMessage() != null) {
                throw e;
            }
        }
        Assert.assertTrue(cacheManagerService.getValueFromLocalTxCache(key2) == null);
        Assert.assertTrue(cacheManagerService.getValueFromNoTxCache(key2) != null);
    }

    @Test
    public void testEhcacheTransactionManagerWithTxAwareManager() {
        String key1 = "key1";
        txCacheManagerService.doTxLogic(key1);
        Assert.assertTrue(txCacheManagerService.getValueFromLocalTxCache(key1) != null);
        Assert.assertTrue(txCacheManagerService.getValueFromNoTxCache(key1) != null);

        String key2 = "key2";
        try {
            txCacheManagerService.doTxLogicWithException(key2);
        } catch (RuntimeException e) {
            if (e.getMessage() != null) {
                throw e;
            }
        }
        Assert.assertTrue(txCacheManagerService.getValueFromLocalTxCache(key2) == null);
        Assert.assertTrue(txCacheManagerService.getValueFromNoTxCache(key2) == null);
    }


}