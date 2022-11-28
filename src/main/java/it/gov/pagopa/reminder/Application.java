package it.gov.pagopa.reminder;

import javax.sql.DataSource;

import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public DataSource flywayDatasource(
            @Value("${spring.quartz.properties.org.quartz.dataSource.quartzDS.URL}") String datasourceUrl,
            @Value("${spring.quartz.properties.org.quartz.dataSource.quartzDS.user}") String datasourceUser,
            @Value("${spring.quartz.properties.org.quartz.dataSource.quartzDS.password}") String datasourcePassword) {
        return DataSourceBuilder.create().url(datasourceUrl).username(datasourceUser).password(datasourcePassword)
                .build();
    }

}