package com.luanvv.springboot.aws.ec2andrds.configuration;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.util.StringUtils;

import com.luanvv.springboot.aws.ec2andrds.SpringBootRestServiceApplication;
import com.luanvv.springboot.aws.ec2andrds.configuration.DataSourceUtils.Route;

@Configuration
@EnableJpaRepositories(
		basePackageClasses = SpringBootRestServiceApplication.class, 
		includeFilters = @ComponentScan.Filter(ReadOnlyRepository.class), 
		entityManagerFactoryRef = "readEntityManagerFactory")
@Profile({ "!test"})
public class ReadonlyDataSourceConfig {

	private static final String AWS_DB_SECRETS_REGION = "spring.aws.database.read.secretsmanager.region";
	private static final String AWS_DB_SECRET_NAME = "spring.aws.database.read.secretsmanager.secretName";

	@Bean
	public DataSource readDataSource(DataSourceUtils dataSourceUtils, Environment env) {
		String secretName = env.getProperty(AWS_DB_SECRETS_REGION);
		String region = env.getProperty(AWS_DB_SECRET_NAME);
		if(StringUtils.hasText(secretName)) {
			return dataSourceUtils.getBySecrets(secretName, region);
		}
		return dataSourceUtils.getByProperties(env, Route.READ);
	}

	@Bean
	public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(EntityManagerFactoryBuilder builder,
			@Qualifier DataSource dataSource) {
		return builder.dataSource(dataSource)
				.packages(SpringBootRestServiceApplication.class)
				.persistenceUnit("read")
				.build();
	}
}
