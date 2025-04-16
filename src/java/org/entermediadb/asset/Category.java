package org.entermediadb.asset;

import java.util.Collection;
import java.util.List;

import org.openedit.MultiValued;

public interface Category extends MultiValued, Comparable
{

	String getIndexId();

	void setIndexId(String inIndexId);

	void sortChildren(boolean inRecursive);

	List getRelatedCategoryIds();

	void setRelatedCategoryIds(List fieldRelatedCategoryIds);

	String toString();

	int getItemCount();

	void setItemCount(int inItemCount);

	boolean isContainsItems();

	/**
	 * @return Returns the children.
	 */
	List getChildren();

	/**
	 * @param children
	 *            The children to set.
	 */
	void setChildren(List inChildren);

	Category addChild(Category inNewChild);

	Category getChild(String inId);

	void removeChild(Category inChild);

	boolean hasParent(String inId);
	
	boolean hasParentCategory(Category inId);

	/**
	 * @return
	 */
	boolean hasChildren();

	boolean hasCatalog(String inId);

	boolean hasChild(String inId);

	boolean isAncestorOf(Category inCatalog);

	Category getParentCategory();

	void setParentCategory(Category parentCatalog);

	/**
	 * Returns a list of all the ancestors of this catalog, starting at the
	 * catalog at the given level and ending at this catalog itself.
	 * 
	 * @param inStartLevel
	 *            The level at which to start listing ancestors (0 is the root,
	 *            1 is the first-level children, etc.)
	 * 
	 * @return The list of ancestors of this catalog
	 */
	List listAncestorsAndSelf(int inStartLevel);

	List getChildrenInRows(int inColCount);

	int getLevel();

	String getDescription();

	void setDescription(String inDescription);

	String getShortDescription();

	void setShortDescription(String inShortDecription);

	void clearChildren();

	Category getChildByName(String inCatName);

	String getLink();

	List getParentCategories();

	List getParentCategoriesFrom(int inStartFrom);

	void clearRelatedCategoryIds();

	void addRelatedCategoryId(String inId);

	String getLinkedToCategoryId();

	void setLinkedToCategoryId(String inLinkedToCategoryId);

	String getParentId();

	void setParentId(String inParentId);

	String getCategoryPath();

	String loadCategoryPath();

	int compareTo(Object c2);

	boolean refresh();

	boolean isDirty();

	boolean equals(Object obj);

	boolean hasParent(Collection<String> inCategorids);

	boolean hasParentCategory(Collection<Category> inCategorids);

	boolean hasSelf(Collection<String> inCategorids);

	Object findValue(String inString);

	Collection findValues(String inString);
	
	Collection<String> collectValues(String inKey);
	
	void collectValues(String inKey, Collection<String> inValue);
	
	boolean hasLoadedParent();

	boolean hasSelfCategory(Collection<Category> inViewcategories);


}