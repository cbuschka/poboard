package com.github.cbuschka.poboard.domain.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;

@Component
@Slf4j
public class ConfigProvider
{
	@Value("${poboard.config:classpath:config.yaml}")
	private Resource configResource;

	private Config config;

	@PostConstruct
	public synchronized Config getConfig()
	{
		if (this.config == null)
		{
			this.config = loadConfig();
		}

		return this.config;
	}

	private Config loadConfig()
	{
		try
		{
			Config config = new ObjectMapper(new YAMLFactory()).readerFor(Config.class).readValue(this.configResource.getInputStream());

			log.info("Config loaded from {}.", this.configResource.getURI());

			return config;
		}
		catch (IOException ex)
		{
			throw new RuntimeException(ex);
		}
	}
}
