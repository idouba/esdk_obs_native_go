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

public class S3StorageInfo
{
    private String size;
    
    private String objectNum;
    
    /**
     * @return 返回 size
     */
    public String getSize()
    {
        return size;
    }
    /**
     * @param 对size进行赋值
     */
    public void setSize(String size)
    {
        this.size = size;
    }
    /**
     * @return 返回 objectNum
     */
    public String getObjectNum()
    {
        return objectNum;
    }
    /**
     * @param 对objectNum进行赋值
     */
    public void setObjectNum(String objectNum)
    {
        this.objectNum = objectNum;
    }
    
}
