/*
 * Copyright 2013-2014 the original author or authors.
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

package org.elasticspring.context.config.xml;

import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import static org.elasticspring.core.config.xml.XmlWebserviceConfigurationUtils.getCustomClientOrDefaultClientBeanName;

/**
 * @author Agim Emruli
 */
class ContextInstanceDataPlaceholderResolverBeanDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String POST_PROCESSOR_CLASS_NAME = "org.elasticspring.context.config.AmazonEc2InstanceDataPropertySourcePostProcessor";
	private static final String POST_PROCESSOR_BEAN_NAME = "AmazonEc2InstanceDataPropertySourcePostProcessor";
	private static final String USER_TAGS_BEAN_CLASS_NAME = "org.elasticspring.core.env.ec2.AmazonEc2InstanceUserTagsFactoryBean";
	private static final String EC2_CLIENT_CLASS_NAME = "com.amazonaws.services.ec2.AmazonEC2Client";

	@Override
	protected String resolveId(Element element, AbstractBeanDefinition definition, ParserContext parserContext) throws BeanDefinitionStoreException {
		return POST_PROCESSOR_BEAN_NAME;
	}

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder postProcessorBuilder = BeanDefinitionBuilder.genericBeanDefinition(POST_PROCESSOR_CLASS_NAME);

		if (StringUtils.hasText(element.getAttribute("user-tags-map"))) {
			BeanDefinitionBuilder userTagsBuilder = BeanDefinitionBuilder.genericBeanDefinition(USER_TAGS_BEAN_CLASS_NAME);

			userTagsBuilder.addConstructorArgReference(
					getCustomClientOrDefaultClientBeanName(element, parserContext, "amazon-ec2", EC2_CLIENT_CLASS_NAME));

			if (StringUtils.hasText(element.getAttribute("instance-id-provider"))) {
				userTagsBuilder.addConstructorArgReference(element.getAttribute("instance-id-provider"));
			}

			BeanDefinitionReaderUtils.registerBeanDefinition(
					new BeanDefinitionHolder(userTagsBuilder.getBeanDefinition(), element.getAttribute("user-tags-map")),
					parserContext.getRegistry());
		}

		return postProcessorBuilder.getBeanDefinition();
	}

}