/*
 * JetS3t : Java S3 Toolkit
 * Project hosted at http://bitbucket.org/jmurty/jets3t/
 *
 * Copyright 2011 James Murty
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jets3t.service.model;

import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jets3t.service.Constants;

import com.jamesmurty.utils.XMLBuilder;

/**
 * Represents the website configuraton of a bucket
 *
 * @author James Murty
 */
public class S3WebsiteConfiguration {
    private String indexDocumentSuffix = null;
    private String errorDocumentKey = null;
    private RedirectRule redirectAllRequestsTo;   
    private List<RoutingRule> routingRules = new LinkedList<RoutingRule>();

    public S3WebsiteConfiguration(){
        
    }
    
    public S3WebsiteConfiguration(String indexDocumentSuffix, String errorDocumentKey) {
        this.indexDocumentSuffix = indexDocumentSuffix;
        this.errorDocumentKey = errorDocumentKey;
    }

    public S3WebsiteConfiguration(String indexDocumentSuffix) {
        this(indexDocumentSuffix, null);
    }

    public String getIndexDocumentSuffix() {
        return indexDocumentSuffix;
    }

    public String getErrorDocumentKey() {
        return errorDocumentKey;
    }

    public boolean isWebsiteConfigActive() {
        return (indexDocumentSuffix != null);
    }
    
    public List<RoutingRule> getRoutingRules()
    {
        return routingRules;
    }

    public void setRoutingRules(List<RoutingRule> routingRules)
    {
        this.routingRules = routingRules;
    }

    public RedirectRule getRedirectAllRequestsTo()
    {
        return redirectAllRequestsTo;
    }

    public void setRedirectAllRequestsTo(RedirectRule redirectAllRequestsTo)
    {
        this.redirectAllRequestsTo = redirectAllRequestsTo;
    }

    public void setIndexDocumentSuffix(String indexDocumentSuffix)
    {
        this.indexDocumentSuffix = indexDocumentSuffix;
    }

    public void setErrorDocumentKey(String errorDocumentKey)
    {
        this.errorDocumentKey = errorDocumentKey;
    }

    /**
     * 如果是全部重定向:RedirectAllRequestsTo和HostName是必选条件，Protocol可选
     * 否则:
     *   1.IndexDocument 必选
     *   2.ErrorDocument 可选
     *   3.RoutingRules  可选
     *       RoutingRule   必选
     *         Condition     可选
     *           KeyPrefixEquals 可选
     *           HttpErrorCodeReturnedEquals 可选
     *         Redirect      必选
     *           ReplaceKeyWith
     *           HostName
     *           ReplaceKeyPrefixWith
     * @return
     * An XML representation of the object suitable for use as an input to the REST/HTTP interface.
     *
     * @throws FactoryConfigurationError
     * @throws ParserConfigurationException
     * @throws TransformerException
     */
    public String toXml() throws ParserConfigurationException,
        FactoryConfigurationError, TransformerException
    {
        XMLBuilder builder = XMLBuilder.create("WebsiteConfiguration");
        builder.attr("xmlns", Constants.XML_NAMESPACE);
        // 如果是全部重定向
        if(this.redirectAllRequestsTo != null && !"".equals(this.redirectAllRequestsTo))
        {
            if(null != this.redirectAllRequestsTo.getHostName())
            {
                builder = builder.elem("RedirectAllRequestsTo").elem("HostName").text(this.redirectAllRequestsTo.getHostName());
            }
            if(null != this.redirectAllRequestsTo.getReplaceKeyWith())
            {
                builder = builder.up().elem("ReplaceKeyWith").text(this.redirectAllRequestsTo.getReplaceKeyWith());
            }
            if(null != this.redirectAllRequestsTo.getReplaceKeyPrefixWith())
            {
                builder = builder.up().elem("ReplaceKeyPrefixWith").text(this.redirectAllRequestsTo.getReplaceKeyPrefixWith());
            }
            if(null != this.redirectAllRequestsTo.getProtocol())
            {
                builder = builder.up().elem("Protocol").text(this.redirectAllRequestsTo.getProtocol());
            }
            builder.up().up();
            return builder.asString();
        }
        builder.elem("IndexDocument").elem("Suffix").text(this.indexDocumentSuffix)
            .up().up();
        if (this.errorDocumentKey != null && this.errorDocumentKey.length() > 0) {
            builder.elem("ErrorDocument").elem("Key").text(this.errorDocumentKey);
            builder.up().up();
        }
        if(null != this.getRoutingRules() && this.getRoutingRules().size() > 0)
        {
            builder = builder.elem("RoutingRules");
            for (RoutingRule routingRule : routingRules)
            {
                builder = builder.elem("RoutingRule");
                RoutingRuleCondition condition = routingRule.getCondition();
                RedirectRule redirect = routingRule.getRedirect();
                if(null != condition)
                {
                    builder = builder.elem("Condition");
                    String keyPrefixEquals = condition.getKeyPrefixEquals();
                    String hecre = condition.getHttpErrorCodeReturnedEquals();
                    if(null != keyPrefixEquals && !"".equals(keyPrefixEquals))
                    {
                        builder = builder.elem("KeyPrefixEquals").text(keyPrefixEquals);
                        builder = builder.up();
                    }
                    if(null != hecre && !"".equals(hecre))
                    {
                        builder = builder.elem("HttpErrorCodeReturnedEquals").text(hecre);
                        builder = builder.up();
                    }
                    builder = builder.up();
                }
                if(null != redirect)
                {
                    builder = builder.elem("Redirect");
                    String hostName = redirect.getHostName();
                    String repalceKeyWith = redirect.getReplaceKeyWith();
                    String replaceKeyPrefixWith = redirect.getReplaceKeyPrefixWith();
                    String protocol = redirect.getProtocol();
                    String redirectCode = redirect.getHttpRedirectCode();
                    if(null != hostName && !"".equals(hostName))
                    {
                        builder = builder.elem("HostName").text(hostName);
                        builder = builder.up();
                    }
                    if(null != redirectCode && !"".equals(redirectCode))
                    {
                        builder = builder.elem("HttpRedirectCode").text(redirectCode);
                        builder = builder.up();
                    }
                    if(null != repalceKeyWith && !"".equals(repalceKeyWith))
                    {
                        builder = builder.elem("ReplaceKeyWith").text(repalceKeyWith);
                        builder = builder.up();
                    }
                    if(null != replaceKeyPrefixWith && !"".equals(replaceKeyPrefixWith))
                    {
                        builder = builder.elem("ReplaceKeyPrefixWith").text(replaceKeyPrefixWith);
                        builder = builder.up();
                    }
                    if(null != protocol && !"".equals(protocol))
                    {
                        builder = builder.elem("Protocol").text(protocol);
                        builder = builder.up();
                    }
                    builder = builder.up();
                }
                builder = builder.up();
            }
            builder = builder.up();
        }
        return builder.asString();
    }

    @Override
    public String toString()
    {
        return "WebsiteConfig [indexDocumentSuffix=" + indexDocumentSuffix + ", errorDocumentKey=" + errorDocumentKey
            + ", redirectAllRequestsTo=" + redirectAllRequestsTo + ", routingRules=" + routingRules + "]";
    }
    
    
}