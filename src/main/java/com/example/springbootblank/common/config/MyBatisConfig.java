package com.example.springbootblank.common.config;

import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import javax.sql.DataSource;

@Configuration
public class MyBatisConfig {

    @Bean
    public SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
        SqlSessionFactoryBean factoryBean = new SqlSessionFactoryBean();
        factoryBean.setDataSource(dataSource);

        Resource[] mapperLocations = new PathMatchingResourcePatternResolver()
                .getResources("classpath*:mapper/**/*.xml");
        factoryBean.setMapperLocations(mapperLocations);

        SqlSessionFactory sqlSessionFactory = factoryBean.getObject();
        if (sqlSessionFactory == null) {
            throw new IllegalStateException("SqlSessionFactory 初始化失败");
        }
        return sqlSessionFactory;
    }

    @Bean
    public SqlSessionTemplate sqlSessionTemplate(SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }
}

