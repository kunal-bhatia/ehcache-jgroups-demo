package com.tutorial.k8s.ehcache.demo;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

import static java.lang.System.getenv;

@Slf4j
@SpringBootApplication
public class EhcacheJgroupsDemoApplication {

	public static void main(String[] args) {
		final String profiles = getenv("SPRING_PROFILES_ACTIVE");
		log.info("Running boot with SPRING_PROFILES_ACTIVE={}", profiles);
		final Runtime.Version version = Runtime.version();
		final int processors = Runtime.getRuntime().availableProcessors();
		final double maxMemory = Runtime.getRuntime().maxMemory() / (1_024.0 * 1_024.0 * 1_024.0);
		final String javaToolOptions = getenv("JAVA_TOOL_OPTIONS");
		final String springConfigLocation = getenv("SPRING_CONFIG_LOCATION");
		final long pid = ProcessHandle.current().pid();
		log.info("Some environment data: jvm={}, processors={}, max-memory={} GB, JAVA_TOOL_OPTIONS={}, SPRING_CONFIG_LOCATION={}, pid={}.",
				version,
				processors,
				String.format("%.2f", maxMemory),
				javaToolOptions,
				springConfigLocation,
				pid);
		SpringApplication.run(EhcacheJgroupsDemoApplication.class, args);
	}

}

@EnableCaching
@Configuration
class EHCacheConfig {

    @Bean
    public CacheManager cacheManager(){
        return new EhCacheCacheManager(ehCacheManagerFactory().getObject());
    }

    @Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactory(){
        EhCacheManagerFactoryBean ehCacheBean = new EhCacheManagerFactoryBean();
        ehCacheBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        ehCacheBean.setShared(true);
        return ehCacheBean;
    }

}

@Component
@Slf4j
class CacheManagerCheck implements CommandLineRunner {


	private final CacheManager cacheManager;

	public CacheManagerCheck(CacheManager cacheManager) {
		this.cacheManager = cacheManager;
	}

	@Override
	public void run(String... strings) {
		log.info("\n\n" + "=========================================================\n"
				+ "Using cache manager: " + this.cacheManager.getClass().getName() + "\n"
				+ "=========================================================\n\n");
	}

}

@RestController
class CountryController {

	@Autowired
	CountryRepository countryRepository;

	@GetMapping(value = "/country/{code}")
	public ResponseEntity<Country> getCountryByCode(@PathVariable("code") String code) {
		Country country = countryRepository.findByCode(code);
		return ResponseEntity.ok(country);
	}

	@DeleteMapping(value = "/country/{code}")
	public ResponseEntity deleteCountryByCode(@PathVariable("code") String code) {
		countryRepository.deleteCode(code);
		return ResponseEntity.ok().build();
	}
}

@Slf4j
@Component
class CountryRepository {

	@Cacheable(value="countries", key="#code")
	public Country findByCode(String code) {
		log.info("---> Loading country with code={}", code);
		return new Country(code);
	}

	@CacheEvict(value="countries",key = "#code")
	public int deleteCode(String code){
		log.info("---> Deleting country with code={}", code);
		return 0;
	}
}

@Data
class Country implements Serializable {

	private final String code;
}