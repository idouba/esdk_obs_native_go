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
package org.jets3t.service.impl.rest;

import org.jets3t.service.model.S3StoragePolicy;

public class StoragePolicyHandler extends DefaultXmlHandler
{
    private S3StoragePolicy storagePolicy = null;

    /**
     * @return 返回 storagePolicy
     */
    public S3StoragePolicy getStoragePolicy()
    {
        return storagePolicy;
    }

    @Override
    public void startElement(String name)
    {
        if (name.equals("StoragePolicy"))
        {
            storagePolicy = new S3StoragePolicy();
        }
    }
    
    @Override
    public void endElement(String name, String elementText)
    {
        if (name.equals("Name"))
        {
            storagePolicy.setName(elementText);
        }
    }
}
