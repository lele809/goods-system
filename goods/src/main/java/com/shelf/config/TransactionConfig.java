package com.shelf.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.TransactionManagementConfigurer;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 事务管理配置
 */
@Slf4j
@Configuration
@EnableTransactionManagement(proxyTargetClass = true)
public class TransactionConfig implements TransactionManagementConfigurer {

    private final PlatformTransactionManager transactionManager;

    public TransactionConfig(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Override
    public PlatformTransactionManager annotationDrivenTransactionManager() {
        return transactionManager;
    }

    /**
     * 事务拦截器配置
     */
    @Bean
    public TransactionInterceptor transactionInterceptor() {
        Properties transactionAttributes = new Properties();
        
        // 只读事务配置（查询方法）- 增加rollback规则确保异常时正确回滚
        transactionAttributes.setProperty("get*", "PROPAGATION_REQUIRED,readOnly,timeout_30,+Exception");
        transactionAttributes.setProperty("find*", "PROPAGATION_REQUIRED,readOnly,timeout_30,+Exception");
        transactionAttributes.setProperty("search*", "PROPAGATION_REQUIRED,readOnly,timeout_30,+Exception");
        transactionAttributes.setProperty("query*", "PROPAGATION_REQUIRED,readOnly,timeout_30,+Exception");
        transactionAttributes.setProperty("list*", "PROPAGATION_REQUIRED,readOnly,timeout_30,+Exception");
        transactionAttributes.setProperty("count*", "PROPAGATION_REQUIRED,readOnly,timeout_30,+Exception");
        
        // 写操作事务配置 - 确保所有异常都回滚
        transactionAttributes.setProperty("save*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        transactionAttributes.setProperty("create*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        transactionAttributes.setProperty("update*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        transactionAttributes.setProperty("delete*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        transactionAttributes.setProperty("remove*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        transactionAttributes.setProperty("add*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        transactionAttributes.setProperty("insert*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        
        // 批量操作事务配置（更长超时时间）- 确保所有异常都回滚
        transactionAttributes.setProperty("batch*", "PROPAGATION_REQUIRED,timeout_60,+Exception");
        transactionAttributes.setProperty("bulk*", "PROPAGATION_REQUIRED,timeout_60,+Exception");
        transactionAttributes.setProperty("import*", "PROPAGATION_REQUIRED,timeout_120,+Exception");
        transactionAttributes.setProperty("export*", "PROPAGATION_REQUIRED,readOnly,timeout_60,+Exception");
        
        // 默认配置 - 确保所有异常都回滚
        transactionAttributes.setProperty("*", "PROPAGATION_REQUIRED,timeout_30,+Exception");
        
        TransactionInterceptor interceptor = new TransactionInterceptor();
        interceptor.setTransactionManager(transactionManager);
        interceptor.setTransactionAttributes(transactionAttributes);
        
        return interceptor;
    }

    /**
     * 持久化异常转换处理器
     */
    @Bean
    public PersistenceExceptionTranslationPostProcessor persistenceExceptionTranslationPostProcessor() {
        return new PersistenceExceptionTranslationPostProcessor();
    }
}