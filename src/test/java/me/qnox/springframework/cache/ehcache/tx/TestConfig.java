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

import me.qnox.springframework.cache.tx.TxAwareCacheManagerProxy;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.transaction.ChainedTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

import java.io.FileNotFoundException;

@Configuration
@ComponentScan
@EnableTransactionManagement
public class TestConfig implements TransactionManagementConfigurer {

    @Bean
    public net.sf.ehcache.CacheManager ehcacheManager() {
        return net.sf.ehcache.CacheManager.newInstance(TestConfig.class.getResourceAsStream("/ehcache.xml"));
    }

    @Bean
    public PlatformTransactionManager transactionManager() {
        return new ChainedTransactionManager(ehcacheTransactionManager(), mainTransactionManager());
    }

    @Bean
    public EhcacheTransactionManager ehcacheTransactionManager() {
        return new EhcacheTransactionManager(ehcacheManager().getTransactionController());
    }

    @Bean
    public PlatformTransactionManager mainTransactionManager() {
        return new AbstractPlatformTransactionManager() {
            @Override
            protected Object doGetTransaction() throws TransactionException {
                return null;
            }

            @Override
            protected void doBegin(Object transaction, TransactionDefinition definition) throws TransactionException {

            }

            @Override
            protected void doCommit(DefaultTransactionStatus status) throws TransactionException {

            }

            @Override
            protected void doRollback(DefaultTransactionStatus status) throws TransactionException {

            }
        };
    }

    @Bean
    public CacheManager cacheManager() throws FileNotFoundException {
        return new EhCacheCacheManager(net.sf.ehcache.CacheManager.getInstance());
    }

    @Bean
    public CacheManager txCacheManager(CacheManager cacheManager) throws FileNotFoundException {
        return new TxAwareCacheManagerProxy(cacheManager);
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager();
    }

    @Bean
    public CacheManagerService cacheManagerService(CacheManager cacheManager) {
        return new CacheManagerService(cacheManager);
    }

    @Bean
    public CacheManagerService txCacheManagerService(CacheManager txCacheManager) {
        return new CacheManagerService(txCacheManager);
    }

}
