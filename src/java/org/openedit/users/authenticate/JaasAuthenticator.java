/*
 * Created on Jul 20, 2006
 */
package org.openedit.users.authenticate;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import javax.security.sasl.RealmCallback;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.users.User;
import org.openedit.users.UserManagerException;

public class JaasAuthenticator extends BaseAuthenticator
{
	private static final Log log = LogFactory.getLog(JaasAuthenticator.class);

	public boolean authenticate(AuthenticationRequest inAReq) throws UserManagerException
	{
		User inUser = inAReq.getUser();
		
		String jaasconfig = inUser.get("oe.jaasconfig");//"OpenEditConfig"
		if( jaasconfig == null)
		{
			return false;
		}
		String inPassword= inAReq.getPassword();
		// Obtain a LoginContext, needed for authentication. Tell it 
		// to use the LoginModule implementation specified by the 
		// entry named "JaasSample" in the JAAS login configuration 
		// file and to also use the specified CallbackHandler.
		try
		{
			LoginContext loginContext = new LoginContext(jaasconfig, new UserPasswordCallbackHandler(inUser, inPassword));
			loginContext.login();

			//			 Now we're logged in, so we can get the current subject.
			//Subject subject = loginContext.getSubject();
		}
		catch (LoginException le)
		{
			log.error("Cannot create LoginContext. " + le.getMessage());
			return false;
		}
		catch (SecurityException se)
		{
			log.error("Cannot create LoginContext. " + se.getMessage());
			return false;
		}
		return true;
	}

	public class UserPasswordCallbackHandler implements CallbackHandler
	{
		private User fieldUser;
		private String mPassword;

		/**
		 *	We need a stateful handler to return the username and password.
		 */
		public UserPasswordCallbackHandler(User username, String password)
		{
			fieldUser = username;
			mPassword = password;
		}

		public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException
		{
			for (int ii = 0; ii < callbacks.length; ii++)
			{
				if (callbacks[ii] instanceof NameCallback)
				{
					NameCallback ncb = (NameCallback) callbacks[ii];
					ncb.setName(fieldUser.getUserName());
				}
				else if (callbacks[ii] instanceof PasswordCallback)
				{
					PasswordCallback pcb = (PasswordCallback) callbacks[ii];
					pcb.setPassword(mPassword.toCharArray());
				}
				else if (callbacks[ii] instanceof RealmCallback)
				{
					RealmCallback rcb = (RealmCallback) callbacks[ii];
					String realm = fieldUser.get("realm");
					if( realm != null)
					{
						rcb.setText(realm);
					}
				}
			}
		}

	}

}
