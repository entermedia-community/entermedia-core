/*
 * Created on Jul 10, 2006
 */
package org.openedit.users.authenticate;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.OpenEditException;
import org.openedit.users.UserManagerException;
import org.openedit.util.StringEncryption;


public class FileSystemAuthenticator extends BaseAuthenticator
{
	private static final Log log = LogFactory.getLog(FileSystemAuthenticator.class);
	
	protected StringEncryption fieldEncryption;
	//https://crackstation.net/hashing-security.htm
//	
//	To Store a Password
//
//	Generate a long random salt using a CSPRNG.
//	Prepend the salt to the password and hash it with a standard password hashing function like Argon2, bcrypt, scrypt, or PBKDF2.
//	Save both the salt and the hash in the user's database record.
//	To Validate a Password
//
//	Retrieve the user's salt and hash from the database.
//	Prepend the salt to the given password and hash it using the same hash function.
//	Compare the hash of the given password with the hash from the database. If they match, the password is correct. Otherwise, the password is incorrect.
//
	
	
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
						log.info("Could not log in " + inAReq.getUserName() + ", bad DES password");
					}
					return ok;
				}
				else
				{
					String decryptedString = decrypt(password);
						if ( decryptedString != null && decryptedString.equals(inPassword))
						{
							return true;
						}
						log.info("Could not log in " + inAReq.getUserName() + ", bad password");
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
