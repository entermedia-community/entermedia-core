package com.openedit.config;

import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;


public class SpringContext extends GenericApplicationContext 
{
	/*
	 * Copyright 2002-2012 the original author or authors.
	 *
	 * Licensed under the Apache License, Version 2.0 (the "License");
	 * you may not use this file except in compliance with the License.
	 * You may obtain a copy of the License at
	 *
	 *      http://www.apache.org/licenses/LICENSE-2.0
	 *
	 * Unless required by applicable law or agreed to in writing, software
	 * distributed under the License is distributed on an "AS IS" BASIS,
	 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
	 * See the License for the specific language governing permissions and
	 * limitations under the License.
	 */



	/**
	 * Convenient application context with built-in XML support.
	 * This is a flexible alternative to {@link ClassPathXmlApplicationContext}
	 * and {@link FileSystemXmlApplicationContext}, to be configured via setters,
	 * with an eventual {@link #refresh()} call activating the context.
	 *
	 * <p>In case of multiple configuration files, bean definitions in later files
	 * will override those defined in earlier files. This can be leveraged to
	 * deliberately override certain bean definitions via an extra configuration file.
	 *
	 * @author Juergen Hoeller
	 * @author Chris Beams
	 * @since 3.0
	 * @see #load
	 * @see XmlBeanDefinitionReader
	 * @see org.springframework.context.annotation.AnnotationConfigApplicationContext
	 */

		private final XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(this);
		
		public SpringContext(DefaultListableBeanFactory factory)
		{
			super(factory);
		}

		/**
		 * Create a new GenericXmlApplicationContext that needs to be
		 * {@linkplain #load loaded} and then manually {@link #refresh refreshed}.
		 */
		public SpringContext() {
			reader.setEnvironment(this.getEnvironment());
		}

		/**
		 * Create a new GenericXmlApplicationContext, loading bean definitions
		 * from the given resources and automatically refreshing the context.
		 * @param resources the resources to load from
		 */
		public SpringContext(Resource... resources) {
			load(resources);
			refresh();
		}

		/**
		 * Create a new GenericXmlApplicationContext, loading bean definitions
		 * from the given resource locations and automatically refreshing the context.
		 * @param resourceLocations the resources to load from
		 */
		public SpringContext(String... resourceLocations) {
			load(resourceLocations);
			refresh();
		}

		/**
		 * Create a new GenericXmlApplicationContext, loading bean definitions
		 * from the given resource locations and automatically refreshing the context.
		 * @param relativeClass class whose package will be used as a prefix when
		 * loading each specified resource name
		 * @param resourceNames relatively-qualified names of resources to load
		 */
		public SpringContext(Class<?> relativeClass, String... resourceNames) {
			load(relativeClass, resourceNames);
			refresh();
		}

		/**
		 * Set whether to use XML validation. Default is {@code true}.
		 */
		public void setValidating(boolean validating) {
			this.reader.setValidating(validating);
		}

		/**
		 * {@inheritDoc}
		 * <p>Delegates the given environment to underlying {@link XmlBeanDefinitionReader}.
		 * Should be called before any call to {@link #load}.
		 */
		@Override
		public void setEnvironment(ConfigurableEnvironment environment) {
			super.setEnvironment(environment);
			this.reader.setEnvironment(this.getEnvironment());
		}

		/**
		 * Load bean definitions from the given XML resources.
		 * @param resources one or more resources to load from
		 */
		public void load(Resource... resources) {
			this.reader.loadBeanDefinitions(resources);
		}

		/**
		 * Load bean definitions from the given XML resources.
		 * @param resourceLocations one or more resource locations to load from
		 */
		public void load(String... resourceLocations) {
			this.reader.loadBeanDefinitions(resourceLocations);
		}

		/**
		 * Load bean definitions from the given XML resources.
		 * @param relativeClass class whose package will be used as a prefix when
		 * loading each specified resource name
		 * @param resourceNames relatively-qualified names of resources to load
		 */
		public void load(Class<?> relativeClass, String... resourceNames) {
			Resource[] resources = new Resource[resourceNames.length];
			for (int i = 0; i < resourceNames.length; i++) {
				resources[i] = new ClassPathResource(resourceNames[i], relativeClass);
			}
			this.load(resources);
		}

	}
