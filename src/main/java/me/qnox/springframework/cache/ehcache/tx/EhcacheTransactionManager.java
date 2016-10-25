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

import net.sf.ehcache.TransactionController;
import net.sf.ehcache.transaction.local.LocalTransactionContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.DefaultTransactionStatus;

/**
 * {@link org.springframework.transaction.PlatformTransactionManager} based on Ehcache {@link TransactionController}
 *
 * @author Anton Efimchuk
 */
public class EhcacheTransactionManager extends AbstractPlatformTransactionManager {

    private TransactionController transactionController;

    public EhcacheTransactionManager(TransactionController transactionController) {
        this.transactionController = transactionController;
    }

    @Override
    protected Object doGetTransaction() throws TransactionException {
        return new EhcacheTransactionObject(transactionController.getCurrentTransactionContext());
    }

    @Override
    protected void doBegin(Object o, TransactionDefinition transactionDefinition) throws TransactionException {
        int timeout = transactionDefinition.getTimeout();
        if (timeout != TransactionDefinition.TIMEOUT_DEFAULT) {
            transactionController.begin(timeout);
        } else {
            transactionController.begin();
        }
    }

    @Override
    protected void doCommit(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
        transactionController.commit();
    }

    @Override
    protected void doRollback(DefaultTransactionStatus defaultTransactionStatus) throws TransactionException {
        transactionController.rollback();
    }

    public class EhcacheTransactionObject {

        private LocalTransactionContext currentTransactionContext;

        public EhcacheTransactionObject(LocalTransactionContext currentTransactionContext) {
            this.currentTransactionContext = currentTransactionContext;
        }

    }

}
