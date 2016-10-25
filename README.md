Spring cache extensions library
=====================================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/me.qnox/spring-cache/badge.svg)](https://maven-badges.herokuapp.com/maven-central/me.qnox/spring-cache)

Library was inspired by a question on [StackOverflow](http://stackoverflow.com/questions/31944204/ehcache-local-transactions-with-spring-transactional) and provides set of support classes for spring cache abstraction:
* [TxAwareCacheDecorator](src/main/java/me/qnox/springframework/cache/tx/TxAwareCacheDecorator.java) decorates any cache to integrate it into spring transactions framework
* [EhcacheTransactionManager](src/main/java/me/qnox/springframework/cache/ehcache/tx/EhcacheTransactionManager.java) integrates ehcache into spring transactions framework. It is supposed to be used with [org.springframework.data.transaction.ChainedTransactionManager](http://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/transaction/ChainedTransactionManager.html)

