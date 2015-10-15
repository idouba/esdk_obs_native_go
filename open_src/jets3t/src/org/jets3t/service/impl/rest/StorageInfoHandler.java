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

import org.jets3t.service.model.S3StorageInfo;

public class StorageInfoHandler extends DefaultXmlHandler 
{
    private S3StorageInfo storageInfo;

    /**
     * @return 返回 storageInfo
     */
    public S3StorageInfo getStorageInfo()
    {
        return storageInfo;
    }

    @Override
    public void startElement(String name)
    {
        if (name.equals("GetBucketStorageInfoResult"))
        {
            storageInfo = new S3StorageInfo();
        }
    }
    
    @Override
    public void endElement(String name, String elementText)
    {
        if (name.equals("Size"))
        {
            storageInfo.setSize(elementText);
        }
        else if (name.equals("ObjectNumber"))
        {
            storageInfo.setObjectNum(elementText);
        }
    }
}
