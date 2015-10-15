/*
 * Copyright Notice:
 *      Copyright  1998-2009, Huawei Technologies Co., Ltd.  ALL Rights Reserved.
 *
 *      Warning: This computer software sourcecode is protected by copyright law
 *      and international treaties. Unauthorized reproduction or distribution
 *      of this sourcecode, or any portion of it, may result in severe civil and
 *      criminal penalties, and will be prosecuted to the maximum extent
 *      possible under the law.
 */
package org.jets3t.service.model;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.jets3t.service.Constants;
import org.jets3t.service.ServiceException;

import com.jamesmurty.utils.XMLBuilder;

public class S3Quota
{
    private String storageQuota;
    
    /**
     * @return 返回 storageQuota
     */
    public String getStorageQuota()
    {
        return storageQuota;
    }
    /**
     * @param 对storageQuota进行赋值
    
    
     */
    public void setStorageQuota(String storageQuota)
    {
        this.storageQuota = storageQuota;
    }
    
    public XMLBuilder toXMLBuilder() throws ServiceException, ParserConfigurationException, FactoryConfigurationError,
        TransformerException
    {
        XMLBuilder builder =
            XMLBuilder.create("Quota")
            .attr("xmlns", Constants.XML_NAMESPACE)
                .elem("StorageQuota")
                .text(getStorageQuota()).up();
        
        return builder;
    }
    
    /**
     * @return
     * an XML representation of the storageQuota object, suitable to send to
     * a storage service in the request body.
     */
    public String toXml() throws ServiceException
    {
        try
        {
            return toXMLBuilder().asString();
        }
        catch (Exception e)
        {
            throw new ServiceException("Failed to build XML document for storageQuota", e);
        }
    }
}
