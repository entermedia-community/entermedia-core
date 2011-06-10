/*
 * Created on Jul 10, 2006
 */
package com.openedit.users.authenticate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.openedit.OpenEditException;
import com.openedit.users.UserManagerException;
import com.openedit.util.StringEncryption;


public class FileSystemAuthenticator extends BaseAuthenticator
{
	private static final Log log = LogFactory.getLog(FileSystemAuthenticator.class);
	
	protected StringEncryption fieldEncryption;
	
	public boolean authenticate(AuthenticationRequest inAReq) throws UserManagerException
	{
		String password = inAReq.getUser().getPassword(); 
		if ( password != null)
		{
			String inPassword = inAReq.getPassword();
			//Decrypt their stored password
			if( password.startsWith("DES:"))
			{
				if ( inPassword.startsWith("DES:"))
				{
					boolean ok = inPassword.equals(password); //there are both encrypted so just compare
					if( !ok )
					{
						//log.info("Encrypted passwords did not match. Should be:" + password  + " was:" + inPassword);
						log.info("Could not log in user");
					}
					return ok;
				}
				else
				{
					String decryptedString = decrypt(password);
						if ( decryptedString.equals(inPassword))
						{
							return true;
						}
						log.info("Could not log in user");
						//log.debug("decryptedString" + decryptedString + " from " + password + " did not equal " + inPassword);
				}
			}
			else if ( password.equals(inPassword))
			{
				return true;
			}
		}
		return false;
	}

	protected String decrypt(String inPassword) throws UserManagerException
	{
//		long encryptionKey = 7939805759879765L; //TODO: Move this to properties file
//		encryptionKey++;
		try
		{
			return getStringEncryption().decrypt(inPassword);
		} catch ( Exception ex)
		{
			throw new UserManagerException(ex);
		}
	}
	
	public String encrypt(String inPassword) throws UserManagerException
	{
		try
		{
//			long encryptionKey = 7939805759879765L; encryptionKey++;
//			StringEncryption encrypter = new StringEncryption( StringEncryption.DES_ENCRYPTION_SCHEME, encryptionKey + "42" + encryptionKey );
			String decryptedString = getStringEncryption().encrypt( inPassword );
			return decryptedString;
		} catch ( OpenEditException ex)
		{
			throw new UserManagerException(ex);
		}
	}

	public StringEncryption getStringEncryption()
	{
		return fieldEncryption;
	}

	public void setStringEncryption(StringEncryption inEncryption)
	{
		fieldEncryption = inEncryption;
	}

	
}
