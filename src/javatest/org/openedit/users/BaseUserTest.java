/*
 * Created on Oct 11, 2006
 */
package org.openedit.users;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import org.openedit.BaseTestCase;
import org.openedit.users.BaseGroup;
import org.openedit.users.BaseUser;
import org.openedit.users.Group;

public class BaseUserTest extends BaseTestCase
{
		public BaseUserTest(String inName)
		{
			super( inName);
		}
		
		public void testGetOrderedGroups() throws Exception{
			BaseUser user = new BaseUser(){

				@Override
				public Collection getGroups() {
					Set groups = new LinkedHashSet<Group>();
					Group g1 = new BaseGroup();
					g1.setName("cat");
					g1.setId("1");
					Group g2 = new BaseGroup();
					g2.setName("Dog");
					g2.setId("2");
					Group g3 = new BaseGroup();
					g3.setName("bear");
					g3.setId("3");
					Group g4 = new BaseGroup();
					g4.setName("deer");
					g4.setId("4");
					groups.add(g1);
					groups.add(g2);
					groups.add(g3);
					groups.add(g4);
					return groups;
				}
				
			};
			Collection<Group> result = null;
			result =  user.getOrderedGroups();
			assertNotNull("NULL result", result);
			Object[] resultArray = result.toArray();
			
			assertEquals("3", ((Group)resultArray[0]).getId());
			assertEquals("1", ((Group)resultArray[1]).getId());
			assertEquals("4", ((Group)resultArray[2]).getId());
			assertEquals("2", ((Group)resultArray[3]).getId());
		}
	}

	
