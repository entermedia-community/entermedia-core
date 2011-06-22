/*
 * Created on Dec 14, 2004
 */
package org.openedit.repository;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.openedit.repository.filesystem.FileRepository;

import com.openedit.util.PathUtilities;

/**
 * @author Matthew Avery, mavery@einnovation.com
 * 
 * This Repository implementation delegates to other repositories based
 * on the requested path. Be sure to set a default Repository.
 */
public class CompoundRepository implements Repository
{
	protected List fieldRepositories;
	protected Repository fieldDefaultRepository;
	protected File fieldRoot;
	
	public void addRepository(Repository inRepository)
	{
		removeRepository(inRepository.getPath());
		getRepositories().add(inRepository);
	}
	
	public File getRoot()
	{
		return fieldRoot;
	}

	public void setRoot(File inRoot)
	{
		fieldRoot = inRoot;
	}


	public void removeRepository( String inPath )
	{
		for (Iterator iterator = getRepositories().iterator(); iterator.hasNext();)
		{
			Repository repo = (Repository) iterator.next();
			if( repo.getPath().equals(inPath))
			{
				getRepositories().remove(repo);
				return;
			}
		}
	}
	public List getRepositories()
	{
		if (fieldRepositories == null)
		{
			fieldRepositories = new ArrayList();
		}
		return fieldRepositories;
	}
    /**
     * <p>
     * Return the Repository that should be used for the specified path.
     * </p>
     * 
     * @param inPath
     * @return
     */
    public Repository resolveRepository(String inPath)
	{
		for ( Iterator iter = getRepositories().iterator(); iter.hasNext(); )
		{
			Repository config = (Repository) iter.next();
			if ( config.matches(inPath) )
			{
				return config;
			}
		}
		return getDefaultRepository();
	}
	
	public ContentItem get( String inPath ) throws RepositoryException
	{
		return resolveRepository( inPath ).get( inPath );
	}
	public ContentItem getStub( String inPath ) throws RepositoryException
	{
		return resolveRepository( inPath ).getStub( inPath );
	}

	public void put( ContentItem inContent ) throws RepositoryException
	{
		Repository repos = resolveRepository( inContent.getPath() );
		repos.put( inContent );
	}

	public void copy( ContentItem inSource, ContentItem inDestination ) throws RepositoryException
	{
		//Repository rep = resolveRepository( inSource.getPath() );
		Repository destrep = resolveRepository( inDestination.getPath() );
		destrep.copy( inSource, inDestination);
	}

	public void move( ContentItem inSource, ContentItem inDestination ) throws RepositoryException
	{
		Repository source = resolveRepository( inSource.getPath() );
		Repository dest = resolveRepository( inDestination.getPath() );
		dest.move( inSource, source, inDestination);
	}

	public void remove( ContentItem inRevision ) throws RepositoryException
	{
		resolveRepository( inRevision.getPath() ).remove( inRevision );
	}

	public List getVersions( String inPath ) throws RepositoryException
	{
		return resolveRepository( inPath ).getVersions( inPath );
	}
	
	public Repository getDefaultRepository()
	{
		if (fieldDefaultRepository == null)
		{
			Repository repo = new FileRepository();
			repo.setPath("/");
			repo.setExternalPath(getRoot().getAbsolutePath());
			fieldDefaultRepository =repo;
		}

		return fieldDefaultRepository;
	}
	public void setDefaultRepository( Repository defaultRepository )
	{
		fieldDefaultRepository = defaultRepository;
	}
	public void setRepositories( List repositoryList )
	{
		fieldRepositories = repositoryList;
	}

	public boolean doesExist(String inPath) throws RepositoryException
	{
		return resolveRepository( inPath ).doesExist(inPath);
	}

	public ContentItem getLastVersion(String inPath) throws RepositoryException
	{
		return resolveRepository( inPath ).getLastVersion(inPath );
	}

	public List getChildrenNames(String inParent) throws RepositoryException
	{
		List children = resolveRepository( inParent ).getChildrenNames(inParent );
		List all = new ArrayList(children);
		//Now we might have another repository with a path in this directory parent
		for (Iterator iterator = getRepositories().iterator(); iterator.hasNext();)
		{
			Repository repo = (Repository) iterator.next();
			String path = repo.getPath();
			if( path.length() > 1)
			{
				String parent = PathUtilities.extractDirectoryPath( path );
				if( inParent.equals(parent))
				{
					all.remove(path);
					all.add(path);
				}
			}
		}
		return all;
	}

	public void deleteOldVersions(String inPath) throws RepositoryException
	{
		resolveRepository( inPath ).deleteOldVersions(inPath);		
	}

	public void move(ContentItem inSource,  Repository inSourceRepository, ContentItem inDestination) throws RepositoryException
	{
		Repository rep = resolveRepository( inDestination.getPath() );
		rep.move( inSource, inSourceRepository, inDestination  );
		
	}
	
	/**
	 * The ID is a combination of the path + filter. For example /stuff/*.pdf or /stuffnull or just /stuff
	 * @param inId
	 * @return
	 */
	public boolean containsRepository(String inPath)
	{
		return getRepository(inPath) != null;
	}
	
	/**
	 * get a specific repository config
	 */
	public Repository getRepository(String inPath)
	{
		for (Iterator iterator = getRepositories().iterator(); iterator.hasNext();)
		{
			Repository  repo = (Repository) iterator.next();

			if( repo.getPath().equals(inPath) )
			{
				return repo;
			}
		}
		return null;
	}
	
	public void sort()
	{
		//Collections.sort(getRepositoryConfigs() );
		Collections.sort(getRepositories(), new Comparator()
		{
			public int compare(Object arg0, Object arg1) 
			{
				Repository r1 = (Repository) arg0;
				Repository r2 = (Repository) arg1;
				return r2.getPath().length() - r1.getPath().length(); //sort by lenth
			}
		});
	}

	public String getPath()
	{
		return "/";
	}

	@Override
	public String getExternalPath()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilterIn()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getFilterOut()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getRepositoryType()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean matches(String inPath)
	{
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setExternalPath(String inRootAbsolutePath)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFilterIn(String inFilters)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setFilterOut(String inFilters)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setPath(String inPath)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRepositoryType(String inType)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getMatchesPostFix()
	{
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setMatchesPostFix(String inMatchesPostFix)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setProperty(String inPropName, String inValue)
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public String getProperty(String inPropName)
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public Map getProperties()
	{
		// TODO Auto-generated method stub
		return null;
	}
}
