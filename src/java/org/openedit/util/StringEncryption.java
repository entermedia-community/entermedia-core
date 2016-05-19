package org.openedit.util;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Properties;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.WebPageRequest;

public class StringEncryption
{
//	public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
	public static final String DES_ENCRYPTION_SCHEME = "DES";
	//public static final String DEFAULT_ENCRYPTION_KEY	= "This is a fairly long phrase used to encrypt";
	public static final String DEFAULT_ENCRYPTION_KEY	= "7939805759879766427939805759879766";
	
	protected String fieldEncryptionKey;
	protected KeySpec				fieldKeySpec;
	protected SecretKeyFactory	fieldKeyFactory;
	protected Cipher				fieldCipher;
	protected SecretKey fieldSecretKey;
	protected File fieldRootDirectory;
	protected String fieldSecretKeyName = "userpassword";
	private static final String	UNICODE_FORMAT			= "UTF8";

	public StringEncryption()
	{
	}
	
	public StringEncryption( String encryptionKey )
	{
		try
		{
			setEncryptionKey(encryptionKey);
		}
		catch (Exception e)
		{
			throw new OpenEditRuntimeException(e);
		}

	}
	
	public void setEncryptionKey(String inEncryptionKey) 
	{
		
		if ( inEncryptionKey == null )
			throw new IllegalArgumentException( "encryption key was null" );
		if ( inEncryptionKey.trim().length() < 24 )
			throw new IllegalArgumentException(
					"encryption key was less than 24 characters" );
		fieldEncryptionKey = inEncryptionKey;
		setKeySpec(null);
		setCipher(null);
	}
	public String getEncryptionKey() throws Exception
	{
		if( fieldEncryptionKey == null)
		{
			File prop = new File( getRootDirectory(), "WEB-INF/encrypt.properties");
			if( prop.exists())
			{
				Properties props = new Properties();
				FileInputStream reader = null;
				try
				{
					reader = new FileInputStream(prop);
					props.load( reader);
				}
				finally
				{
					FileUtils.safeClose(reader);
				}
				fieldEncryptionKey = props.getProperty(getSecretKeyName());
			}
			if( fieldEncryptionKey == null)
			{
				fieldEncryptionKey = DEFAULT_ENCRYPTION_KEY;
			}
		}
		return fieldEncryptionKey;
	}
	
	public synchronized String encrypt( String unencryptedString ) throws OpenEditException
	{
		if ( unencryptedString == null || unencryptedString.trim().length() == 0 )
		{
				throw new IllegalArgumentException(
						"unencrypted string was null or empty" );
		}
		try
		{
			byte[] cleartext = unencryptedString.getBytes( UNICODE_FORMAT );
			Cipher cipher = getCipher(); //Not thread safe
			cipher.init( Cipher.ENCRYPT_MODE, getSecretKey() );

			byte[] ciphertext = cipher.doFinal( cleartext );

			Base64 base64encoder = new Base64();
			return "DES:" + new String( base64encoder.encode( ciphertext ), UNICODE_FORMAT );
		}
		catch (Exception e)
		{
			throw new OpenEditException( e );
		}
	}

	public SecretKey getSecretKey() throws Exception
	{
		if( fieldSecretKey == null)
		{
			fieldSecretKey = getKeyFactory().generateSecret( getKeySpec() );
		}
		return fieldSecretKey;
		
	}

	
	public synchronized String decrypt( String encryptedString ) throws OpenEditException
	{
		if ( encryptedString == null || encryptedString.trim().length() <= 0 )
				throw new IllegalArgumentException( "encrypted string was null or empty" );

		try
		{
			
			if( encryptedString.startsWith("DES:"))
			{
				encryptedString = encryptedString.substring(4);
			}
			if( encryptedString.startsWith("DES"))
			{
				encryptedString = encryptedString.substring(3);
			}
			Cipher cipher = getCipher();
			cipher.init( Cipher.DECRYPT_MODE, getSecretKey() );
			Base64 base64decoder = new Base64();
			byte[] cleartext = base64decoder.decode( encryptedString.getBytes( UNICODE_FORMAT ) );
			byte[] ciphertext = cipher.doFinal( cleartext );

			return bytes2String( ciphertext );
		}
		catch (Exception e)
		{
			throw new OpenEditException( e );
		}
	}
	
	//This seems wrong. Only works for ASCII. Why not just use new String().getBytes()?
	public static String bytes2String( byte[] bytes )
	{
		StringBuffer stringBuffer = new StringBuffer();
		for (int i = 0; i < bytes.length; i++)
		{
			stringBuffer.append( (char) bytes[i] );
		}
		return stringBuffer.toString();
	}

	public Cipher getCipher() throws Exception
	{
		if( fieldCipher == null)
		{
			fieldCipher = Cipher.getInstance( DES_ENCRYPTION_SCHEME );
		}
		return fieldCipher;
	}

	public void setCipher(Cipher inCipher)
	{
		fieldCipher = inCipher;
	}

	public SecretKeyFactory getKeyFactory() throws Exception
	{
		if( fieldKeyFactory == null)
		{
			fieldKeyFactory = SecretKeyFactory.getInstance( DES_ENCRYPTION_SCHEME );
		}
		return fieldKeyFactory;
	}

	public void setKeyFactory(SecretKeyFactory inKeyFactory)
	{
		fieldKeyFactory = inKeyFactory;
	}

	public KeySpec getKeySpec() throws Exception
	{
		if( fieldKeySpec == null)
		{
			byte[] keyAsBytes = getEncryptionKey().getBytes( UNICODE_FORMAT );

			fieldKeySpec = new DESKeySpec( keyAsBytes );

		}
		return fieldKeySpec;
	}

	public void setKeySpec(KeySpec inKeySpec)
	{
		fieldKeySpec = inKeySpec;
	}

	public File getRootDirectory()
	{
		return fieldRootDirectory;
	}

	public void setRootDirectory(File inRootDirectory)
	{
		fieldRootDirectory = inRootDirectory;
	}

	public String getSecretKeyName()
	{
		return fieldSecretKeyName;
	}

	public void setSecretKeyName(String inSecretKeyName)
	{
		fieldSecretKeyName = inSecretKeyName;
	}

	public String decryptIfNeeded(String inPassword)
	{
		if( inPassword != null)
		{
			if( inPassword.startsWith("DES"))
			{
				return decrypt(inPassword);
			}
		}
		return inPassword;
	}

	public String asMd5(String key) throws NoSuchAlgorithmException
	{
		MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
		digest.update(key.getBytes());
		byte[] md5 = digest.digest();

		StringBuffer hexString = new StringBuffer();
		for (int i=0;i<md5.length;i++) 
		{
			if( md5[i] <= 16)
			{
				hexString.append("0");
			}
			hexString.append( Integer.toHexString(0xFF & md5[i]));
		}
		return hexString.toString();
	}

	public String getPasswordMd5(String inPassword) 
	{
		String password = decryptIfNeeded(inPassword);
		try
		{
			String secret = getEncryptionKey() + password;
			String hash = asMd5(secret); 
			return hash;
		}
		catch( Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}
	public void removeCookie(WebPageRequest inReq, String key)
	{
		HttpServletResponse res = inReq.getResponse();
		if (res != null)
		{
			Cookie cookie = new Cookie(createMd5CookieName(inReq,key,true), "none");
			cookie.setMaxAge(0);
			cookie.setPath("/"); // http://www.unix.org.ua/orelly/java-ent/servlet/ch07_04.htm
			res.addCookie(cookie);

			cookie = new Cookie(createMd5CookieName(inReq,key, false), "none");
			cookie.setMaxAge(0);
			cookie.setPath("/"); // http://www.unix.org.ua/orelly/java-ent/servlet/ch07_04.htm
			res.addCookie(cookie);

		}
	}

	public String createMd5CookieName(WebPageRequest inReq, String keybase, boolean withapp)
	{
		String home = (String) inReq.getPageValue("home");
		
		String name = keybase + home;
		if( withapp )
		{
			String root = PathUtilities.extractRootDirectory(inReq.getPath() );
			if( root != null && root.length() > 1)
			{
				name = name + root.substring(1);
			}
		}
		
		return name;
	}

	
}