package com.github.cbuschka.poboard.domain.scm;

import com.github.cbuschka.poboard.domain.auth.PrivateKeyCredentials;
import com.github.cbuschka.poboard.domain.auth.PrivateKeyLoader;
import com.github.cbuschka.poboard.domain.config.Config;
import com.github.cbuschka.poboard.domain.config.ConfigProvider;
import com.github.cbuschka.poboard.util.Integers;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.eclipse.jgit.transport.JschConfigSessionFactory;
import org.eclipse.jgit.transport.OpenSshConfig;
import org.eclipse.jgit.util.FS;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SshSessionContextAwareJschConfigSessionFactory extends JschConfigSessionFactory
{
	@Autowired
	private PrivateKeyLoader privateKeyLoader;
	@Autowired
	private ConfigProvider configProvider;

	@Override
	protected void configure(OpenSshConfig.Host hc, Session session)
	{
		try
		{
			Config config = configProvider.getConfig();
			SshSessionContext<Object> current = SshSessionContext.current();
			List<PrivateKeyCredentials> privateKeyCredentialsList = current.getPrivateKeyCredentialsList();
			session.setConfig("StrictHostKeyChecking", "no");
			CodeRepository codeRepository = current.getCodeRepository();
			session.setTimeout(Integers.firstNonNull(codeRepository.getConnectTimeoutMillis(), config.settings.getConnectTimeoutMillis(), config.defaults.connectTimeoutMillis));
			if (!privateKeyCredentialsList.isEmpty())
			{
				session.setConfig("PreferredAuthentications", "publickey");
			}
		}
		catch (JSchException ex)
		{
			throw new RuntimeException(ex);
		}
	}

	@Override
	protected JSch getJSch(final OpenSshConfig.Host hc, FS fs) throws JSchException
	{
		SshSessionContext<Object> current = SshSessionContext.current();
		List<PrivateKeyCredentials> privateKeyCredentialsList = current.getPrivateKeyCredentialsList();
		JSch jsch = super.getJSch(hc, fs);
		jsch.removeAllIdentity();
		for (PrivateKeyCredentials c : privateKeyCredentialsList)
		{
			jsch.addIdentity("", privateKeyLoader.getAsciiArmoredBytesUTF8(c), null, privateKeyLoader.getPasswordBytesUTF8(c));
		}
		return jsch;
	}
}
