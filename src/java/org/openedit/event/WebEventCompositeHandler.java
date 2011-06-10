package org.openedit.event;

import java.util.Iterator;
import java.util.List;


public class WebEventCompositeHandler extends WebEventHandler {

	protected List fieldWebEventListeners;

	public void eventFired(WebEvent inEvent) {
			
		if (fieldWebEventListeners != null) {
			for (Iterator iterator = getWebEventListeners().iterator(); iterator.hasNext();) {
				WebEventListener listener = (WebEventListener) iterator.next();
				listener.eventFired(inEvent);
			}
		}
	}

	public List getWebEventListeners() {
		return fieldWebEventListeners;
	}

	public void setWebEventListeners(List inWebEventListeners) {

		fieldWebEventListeners = inWebEventListeners;
	}

}
