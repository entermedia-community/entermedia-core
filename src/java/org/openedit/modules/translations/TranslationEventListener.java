package org.openedit.modules.translations;

public interface TranslationEventListener
{
	void translationEvent( String key, String value );

	void tokenEvent( String token );
}
