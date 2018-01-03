/*
 * Copyright (c) SiteWhere, LLC. All rights reserved. http://www.sitewhere.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package com.sitewhere.user.spring;

import java.util.List;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.sitewhere.configuration.parser.IUserManagementParser.Elements;
import com.sitewhere.spi.microservice.spring.InstanceManagementBeans;
import com.sitewhere.spi.microservice.spring.UserManagementBeans;
import com.sitewhere.user.persistence.mongodb.MongoUserManagement;
import com.sitewhere.user.persistence.mongodb.UserManagementMongoClient;

/**
 * Parses configuration data for the SiteWhere user management microservice.
 * 
 * @author Derek
 */
public class UserManagementParser extends AbstractBeanDefinitionParser {

    /*
     * (non-Javadoc)
     * 
     * @see org.springframework.beans.factory.xml.AbstractBeanDefinitionParser#
     * parseInternal (org.w3c.dom.Element,
     * org.springframework.beans.factory.xml.ParserContext)
     */
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext context) {
	List<Element> dsChildren = DomUtils.getChildElements(element);
	for (Element child : dsChildren) {
	    Elements type = Elements.getByLocalName(child.getLocalName());
	    if (type == null) {
		throw new RuntimeException("Unknown user management element: " + child.getLocalName());
	    }
	    switch (type) {
	    case DefaultMongoDatastore: {
		parseDefaultMongoDatastore(child, context);
		break;
	    }
	    }
	}
	return null;
    }

    /**
     * Parse the default MongoDB datastore element.
     * 
     * @param element
     * @param context
     */
    protected void parseDefaultMongoDatastore(Element element, ParserContext context) {
	// Build MongoDB client using default global configuration.
	BeanDefinitionBuilder client = BeanDefinitionBuilder.rootBeanDefinition(UserManagementMongoClient.class);
	client.addConstructorArgReference(InstanceManagementBeans.BEAN_MONGO_CONFIGURATION_DEFAULT);

	context.getRegistry().registerBeanDefinition(UserManagementBeans.BEAN_MONGODB_CLIENT,
		client.getBeanDefinition());

	// Build tenant management implementation.
	BeanDefinitionBuilder management = BeanDefinitionBuilder.rootBeanDefinition(MongoUserManagement.class);
	management.addPropertyReference("mongoClient", UserManagementBeans.BEAN_MONGODB_CLIENT);

	context.getRegistry().registerBeanDefinition(UserManagementBeans.BEAN_USER_MANAGEMENT,
		management.getBeanDefinition());
    }
}