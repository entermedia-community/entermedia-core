package org.openedit.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.SecretKeySpec;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.OpenEditException;
import org.openedit.OpenEditRuntimeException;
import org.openedit.WebPageRequest;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;

public class StringEncryption
{
	private static final Log log = LogFactory.getLog(StringEncryption.class);
//	public static final String DESEDE_ENCRYPTION_SCHEME = "DESede";
	public static final String DES_ENCRYPTION_SCHEME = "DES";
	//public static final String DEFAULT_ENCRYPTION_KEY	= "This is a fairly long phrase used to encrypt";
	public static final String DEFAULT_ENCRYPTION_KEY	= "7939805759879766427939805759879766";
	
	protected String fieldEncryptionKey;
	protected KeySpec				fieldKeySpec;
	protected SecretKeyFactory	fieldKeyFactory;
	
	protected ThreadLocal<Cipher> fieldDecodePool = new ThreadLocal<Cipher>();
	protected ThreadLocal<Cipher> fieldEncodePool = new ThreadLocal<Cipher>();
	
	protected SecretKey fieldSecretKey;
	protected File fieldRootDirectory;
	protected String fieldSecretKeyName = "userpassword";
	private static final String	UNICODE_FORMAT			= "UTF8";

	protected SearcherManager fieldSearcherManager;
	
	public SearcherManager getSearcherManager()
	{
		return fieldSearcherManager;
	}

	public void setSearcherManager(SearcherManager inSearcherManager)
	{
		fieldSearcherManager = inSearcherManager;
	}

	public StringEncryption()
	{
	}
	
	
	/*
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
	}
	*/
	
	/**
	 * Computes RFC 2104-compliant HMAC signature. * @param data The data to be
	 * signed.
	 * 
	 * @param key
	 *            The signing key.
	 * @return The Base64-encoded RFC 2104-compliant HMAC signature.
	 * @throws java.security.SignatureException
	 *             when signature generation fails
	 */
	public String calculateRFC2104HMAC(String privatekey, String data)
	{
		String HMAC_SHA1_ALGORITHM = "HmacSHA1";

		byte[] result;
		try
		{
			// get an hmac_sha1 key from the raw key bytes
			SecretKeySpec signingKey = new SecretKeySpec(privatekey.getBytes(), HMAC_SHA1_ALGORITHM);

			// get an hmac_sha1 Mac instance and initialize with the signing key
			Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM);
			mac.init(signingKey);

			// compute the hmac on input data bytes
			byte[] rawHmac = mac.doFinal(data.getBytes());

			// base64-encode the hmac
			org.apache.commons.codec.binary.Base64 base64encoder = new org.apache.commons.codec.binary.Base64();
			
			result = base64encoder.encode(rawHmac);
			return new String(result, "UTF8");
		}
		catch (Exception e)
		{
			throw new OpenEditException("Failed to generate HMAC : " + e.getMessage(), e);
		}
	}
	
	public String getEncryptionKey() 
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
				catch( Throwable ex)
				{
					throw new OpenEditException(ex);
				}
				finally
				{
					FileUtils.safeClose(reader);
				}
				fieldEncryptionKey = props.getProperty(getSecretKeyName());
			}
			if( fieldEncryptionKey == null)
			{
				fieldEncryptionKey = getEncryptionKey("autologincookiekey");  //7939805759879766427939805759879766
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
				throw new IllegalArgumentException("unencrypted string was null or empty" );
		}
		try
		{
			byte[] cleartext = unencryptedString.getBytes( UNICODE_FORMAT );
			Cipher cipher = getEncodeCipher(); //Not thread safe

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
			byte[] ciphertext = decodeKey(encryptedString);

			return bytes2String( ciphertext );
		}
		catch (Exception e)
		{
			//throw new OpenEditException( e );
			log.error(e);
			return null;
		}
	}

	protected byte[] decodeKey(String encryptedString) throws Exception
	{
		Base64 base64decoder = new Base64();
		byte[] cleartext = base64decoder.decode( encryptedString.getBytes( UNICODE_FORMAT ) );
		Cipher cipher = getDecodeCipher();
		byte[] ciphertext = null;
		try
		{
			ciphertext = cipher.doFinal( cleartext );
		}
		catch (Exception ex)
		{
			Cipher oldCipher = Cipher.getInstance( DES_ENCRYPTION_SCHEME );
			byte[] keyAsBytes = DEFAULT_ENCRYPTION_KEY.getBytes( UNICODE_FORMAT );
			DESKeySpec oldSpec = new DESKeySpec( keyAsBytes );
			oldCipher.init( Cipher.DECRYPT_MODE, getKeyFactory().generateSecret( oldSpec ) );
			ciphertext = oldCipher.doFinal( cleartext );
		}
		return ciphertext;
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

	public Cipher getDecodeCipher()
	{
		try
		{
			Cipher w = fieldDecodePool.get();
		    if(w == null) {
				w = Cipher.getInstance( DES_ENCRYPTION_SCHEME );
				w.init( Cipher.DECRYPT_MODE, getSecretKey() );
				fieldDecodePool.set(w);		    	
		    }
		    return w; 
		} 
		catch ( Exception ex)
		{
			throw new OpenEditException(ex);
		}
	}

	public Cipher getEncodeCipher()
	{
		try
		{
			Cipher w = fieldEncodePool.get();
		    if(w == null) {
				w = Cipher.getInstance( DES_ENCRYPTION_SCHEME );
				w.init( Cipher.ENCRYPT_MODE, getSecretKey() );
				fieldEncodePool.set(w);		    	
		    }
		    return w; 
		} 
		catch ( Exception ex)
		{
			throw new OpenEditException(ex);
		}
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

	public String getEncryptionKey(String inType)
	{
		if( fieldSearcherManager == null)
		{
			return null;
		}
		Searcher searcher = getSearcherManager().getSearcher("system", "systemsettings");
		Data found = searcher.query().match("id", inType).searchOne();
		String key = null;
		if( found != null)
		{
			key = found.get("value");
		}
		return key;
	}

	
}