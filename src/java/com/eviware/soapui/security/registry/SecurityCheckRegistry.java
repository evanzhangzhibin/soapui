/*
 *  soapUI, copyright (C) 2004-2011 eviware.com 
 *
 *  soapUI is free software; you can redistribute it and/or modify it under the 
 *  terms of version 2.1 of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation.
 *
 *  soapUI is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without 
 *  even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 *  See the GNU Lesser General Public License for more details at gnu.org.
 */

package com.eviware.soapui.security.registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.eviware.soapui.SoapUI;
import com.eviware.soapui.config.SecurityCheckConfig;
import com.eviware.soapui.model.testsuite.TestStep;
import com.eviware.soapui.security.ui.SecurityConfigurationDialogBuilder;
import com.eviware.soapui.support.types.StringToStringMap;

/**
 * Registry of SecurityCheck factories
 * 
 * @author soapUI team
 */

public class SecurityCheckRegistry
{
	protected static SecurityCheckRegistry instance;
	private Map<String, SecurityScanFactory> availableSecurityChecks = new HashMap<String, SecurityScanFactory>();
	private StringToStringMap securityCheckNames = new StringToStringMap();

	public SecurityCheckRegistry()
	{
		addFactory( new GroovySecurityCheckFactory() );
		addFactory( new CrossSiteScriptingScanFactory() );
		addFactory( new XmlBombSecurityCheckFactory() );
		addFactory( new MaliciousAttachmentSecurityCheckFactory() );
		// this is actually working
		addFactory( new XPathInjectionSecurityCheckFactory() );
		addFactory( new InvalidTypesSecurityCheckFactory() );
		addFactory( new BoundarySecurityCheckFactory() );
		addFactory( new SQLInjectionCheckFactory() );
		addFactory( new MalformedXmlSecurityCheckFactory() );
		addFactory( new FuzzerSecurityScanFactory() );
		
		for( SecurityScanFactory factory : SoapUI.getFactoryRegistry().getFactories( SecurityScanFactory.class ) )
		{
			addFactory( factory );
		}

	}

	/**
	 * Gets the right SecurityCheck Factory, depending on the type
	 * 
	 * @param type
	 *           The securityCheck to get the factory for
	 * @return
	 */
	public SecurityScanFactory getFactory( String type )
	{
		for( String cc : availableSecurityChecks.keySet() )
		{
			SecurityScanFactory scf = availableSecurityChecks.get( cc );
			if( scf.getSecurityCheckType().equals( type ) )
				return scf;

		}
		return null;
	}

	/**
	 * Gets the right SecurityCheck Factory using name
	 * 
	 * @param name
	 *           The securityCheck name to get the factory for
	 * @return
	 */
	public SecurityScanFactory getFactoryByName( String name )
	{
		String type = getSecurityScanTypeForName( name );

		if( type != null )
		{
			return getFactory( type );
		}

		return null;
	}

	/**
	 * Adding a new factory to the registry
	 * 
	 * @param factory
	 */
	public void addFactory( SecurityScanFactory factory )
	{
		removeFactory( factory.getSecurityCheckType() );
		availableSecurityChecks.put( factory.getSecurityCheckName(), factory );
		securityCheckNames.put( factory.getSecurityCheckName(), factory.getSecurityCheckType() );
	}

	/**
	 * Removing a factory from the registry
	 * 
	 * @param type
	 */
	public void removeFactory( String type )
	{
		for( String scfName : availableSecurityChecks.keySet() )
		{
			SecurityScanFactory csf = availableSecurityChecks.get( scfName );
			if( csf.getSecurityCheckType().equals( type ) )
			{
				availableSecurityChecks.remove( scfName );
				securityCheckNames.remove( scfName );
				break;
			}
		}
	}

	/**
	 * 
	 * @return The registry instance
	 */
	public static synchronized SecurityCheckRegistry getInstance()
	{
		if( instance == null )
			instance = new SecurityCheckRegistry();

		return instance;
	}

	/**
	 * Checking if the registry contains a factory.
	 * 
	 * @param config
	 *           A configuration to check the factory for
	 * @return
	 */
	public boolean hasFactory( SecurityCheckConfig config )
	{
		return getFactory( config.getType() ) != null;
	}

	/**
	 * Returns the list of available checks
	 * 
	 * @param monitorOnly
	 *           Set this to true to get only the list of checks which can be
	 *           used in the http monitor
	 * @return A String Array containing the names of all the checks
	 */
	public String[] getAvailableSecurityChecksNames()
	{
		List<String> result = new ArrayList<String>();

		for( SecurityScanFactory securityCheck : availableSecurityChecks.values() )
		{
			result.add( securityCheck.getSecurityCheckName() );
		}

		String[] sortedResult = result.toArray( new String[result.size()] );
		Arrays.sort( sortedResult );

		return sortedResult;
	}

	// TODO drso: test and implement properly
	public String[] getAvailableSecurityChecksNames( TestStep testStep )
	{
		List<String> result = new ArrayList<String>();

		for( SecurityScanFactory securityCheck : availableSecurityChecks.values() )
		{
			if( securityCheck.canCreate( testStep ) )
				result.add( securityCheck.getSecurityCheckName() );
		}

		String[] sortedResult = result.toArray( new String[result.size()] );
		Arrays.sort( sortedResult );

		return sortedResult;
	}

	public SecurityConfigurationDialogBuilder getUIBuilder()
	{
		return new SecurityConfigurationDialogBuilder();
	}

	public String getSecurityScanTypeForName( String name )
	{
		return securityCheckNames.get( name );
	}

}