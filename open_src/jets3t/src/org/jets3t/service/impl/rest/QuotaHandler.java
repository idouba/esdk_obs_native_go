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

import org.jets3t.service.model.S3Quota;

public class QuotaHandler extends DefaultXmlHandler
{
    protected S3Quota quota = null;
    
    /**
     * @return 返回 quota
     */
    public S3Quota getQuota()
    {
        return quota;
    }
    
    @Override
    public void startElement(String name)
    {
        if (name.equals("Quota"))
        {
            quota = new S3Quota();
        }
    }
    
    @Override
    public void endElement(String name, String elementText)
    {
        if (name.equals("StorageQuota"))
        {
            quota.setStorageQuota(elementText);
        }
    }
}
