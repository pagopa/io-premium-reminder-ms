package it.gov.pagopa.reminder;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
public class FlywayConf {

    @Bean
    public DataSource flywayDatasource(
            @Value("${spring.quartz.properties.org.quartz.dataSource.quartzDS.URL}") String datasourceUrl,
            @Value("${spring.quartz.properties.org.quartz.dataSource.quartzDS.user}") String datasourceUser,
            @Value("${spring.quartz.properties.org.quartz.dataSource.quartzDS.password}") String datasourcePassword) {
        return DataSourceBuilder.create().url(datasourceUrl).username(datasourceUser).password(datasourcePassword)
                .build();
    }
}
