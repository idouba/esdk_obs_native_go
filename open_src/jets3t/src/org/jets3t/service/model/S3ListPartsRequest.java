package org.jets3t.service.model;
/**
 * 
 * @author zWX228053
 */
public class S3ListPartsRequest
{
    /**
     * 桶名
     */
    private String bucketName;
    
    /**
     * 对象名
     */
    private String key;
    
    /**
     * 上传Id
     */
    private String uploadId;
    
    /**
     * 规定在列举已上传段响应中的最大Part数目。
     * 默认值1000。
     */
    private Integer maxParts;
    
    /**
     * 指定List的起始位置，只有Part Number数目大于该参数的Part会被列出。
     */
    private Integer partNumberMarker;
    
    public String getBucketName()
    {
        return bucketName;
    }

    public void setBucketName(String bucketName)
    {
        this.bucketName = bucketName;
    }

    public String getKey()
    {
        return key;
    }

    public void setKey(String key)
    {
        this.key = key;
    }

    public String getUploadId()
    {
        return uploadId;
    }

    public void setUploadId(String uploadId)
    {
        this.uploadId = uploadId;
    }

    public Integer getMaxParts()
    {
        return maxParts;
    }

    public void setMaxParts(Integer maxParts)
    {
        this.maxParts = maxParts;
    }

    public Integer getPartNumberMarker()
    {
        return partNumberMarker;
    }

    public void setPartNumberMarker(Integer partNumberMarker)
    {
        this.partNumberMarker = partNumberMarker;
    }
}
