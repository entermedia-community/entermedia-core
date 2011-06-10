/*

 Copyright (c) 2002 eInnovation Inc. All rights reserved.

 Redistribution and use in source and binary forms, with or without modification,
 are permitted provided that the following conditions are met:

 1.	Redistributions of source code must retain the above copyright notice, this 
 list of conditions and the following disclaimer.

 2. 	Redistributions in binary form must reproduce the above copyright notice, 
 this list of conditions and the following disclaimer in the documentation 
 and/or other materials provided with the distribution.

 3. 	The end-user documentation included with the redistribution, if any, must 
 include the following acknowledgment:

 "This product includes software developed by eInnovation Inc. 
 (http://www.einnovation.com/)."

 Alternately, this acknowledgment may appear in the software itself, if and 
 wherever such third-party acknowledgments normally appear.

 4. 	The names "openedit" and "eInnovation" must not be used to endorse or 
 promote products derived from this software without prior written 
 permission. For written permission, please contact info@einnovation.com.

 5. 	Products derived from this software may not be called "openedit", nor may
 "openedit" appear in their name, without prior written permission of 
 eInnovation Inc.

 THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, 
 INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND 
 FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE 
 SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, 
 INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT 
 LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
 LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

 *******************************************************************************/

package com.openedit.users.filesystem;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * DOCUMENT ME!
 *
 * @author
 * @version 1.0
 */
public class AllTests {


	/**
	 *  
	 *
	 * @return  
	 */
	public static Test suite() {
		TestSuite suite = new TestSuite("Test for com.openedit.users.filesystem");

		//$JUnit-BEGIN$
		suite.addTest( new TestSuite( GroupTest.class ) );
		suite.addTest( new TestSuite( UserManagerTest.class));
		
		//$JUnit-END$
		return suite;
	}
}