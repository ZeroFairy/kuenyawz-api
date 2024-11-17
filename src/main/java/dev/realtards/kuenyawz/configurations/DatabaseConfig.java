package dev.realtards.kuenyawz.configurations;

import dev.realtards.kuenyawz.configurations.properties.ApplicationProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import javax.sql.DataSource;

/**
 * Configuration class for database. Configures the database based on the running
 * profile, if no specific profile is provided, the program will run with in memory
 * H2 database.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class DatabaseConfig {

	private final ApplicationProperties applicationProperties;
	private final ConfigurableApplicationContext applicationContext;

	@Bean
	@Profile("postgres")
	public DataSource postgresDataSource() {
		log.info("Mounting profile for postgres datasource (PostgreSQL database)");

		DataSource dataSource = DataSourceBuilder.create()
			.url(applicationProperties.getDatabase().getUrl())
			.username(applicationProperties.getDatabase().getUsername())
			.password(applicationProperties.getDatabase().getPassword())
			.driverClassName("org.postgresql.Driver")
			.build();

		testConnection(dataSource);

		log.info("Mounted profile for postgres datasource (PostgreSQL database)");
		return dataSource;
	}

	@Bean
	@Profile("!postgres")
	public DataSource defaultDataSource() {
		log.info("Mounting profile for default datasource (H2 in-memory database)");

		DataSource dataSource = DataSourceBuilder.create()
			.url("jdbc:h2:mem:testdb")
			.username("sa")
			.password("")
			.driverClassName("org.h2.Driver")
			.build();

		testConnection(dataSource);

		log.info("Mounted profile for default datasource (H2 in-memory database)");

		return dataSource;
	}

	private void testConnection(DataSource dataSource) {
		try {
			log.info("Testing database connection...");
			dataSource.getConnection().close();
			log.info("Database connection successful!");
		} catch (Exception e) {
			log.error(e.getMessage());
			log.error("Failed to connect to the database. Is the database running?");
			log.info("Shutting down the application...");
			System.exit(1);
		}
	}
}
