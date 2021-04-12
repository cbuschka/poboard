package com.github.cbuschka.poboard.domain.deployment;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

@Component
public class PropertiesDeploymentInfoExtractionHandler implements DeploymentInfoExtractionHandler
{
	@Override
	public boolean handles(Endpoint endpoint)
	{
		return endpoint.getUrl() != null && endpoint.getUrl().endsWith(".properties");
	}

	@Override
	public DeploymentInfo extractDeploymentInfoFrom(Endpoint endpoint, InputStream in, String system, String env) throws IOException
	{
		Properties properties = new Properties();
		properties.load(in);

		String version = getStringFrom(properties, DeploymentInfoExtractor.VERSION_ALIASES);
		String commitish = getStringFrom(properties, DeploymentInfoExtractor.COMMITISH_ALIASES);
		String branch = getStringFrom(properties, DeploymentInfoExtractor.BRANCH_ALIASES);
		return new DeploymentInfo(DeploymentStatus.AVAILABLE, system, env, commitish, version, branch);
	}

	private String getStringFrom(Properties properties, List<String> keys)
	{
		for (String key : keys)
		{
			String value = properties.getProperty(key);
			if (value != null)
			{
				return value;
			}
		}

		return null;
	}
}
