package org.jets3t.service.model;

import java.util.List;

/**
 * 列出段的返回结果
 */
public class S3ListPartsResult
{
    /**
     * 桶名
     */
    private String bucket;
    /**
     * 对象名
     */
    private String key;
    /**
     * 上传Id
     */
    private String uploadId;
    /**
     * 初始化的用户
     */
    private S3Owner initiator;
    /**
     * 所有者的用户
     */
    private S3Owner owner;
    
    /**
     * 对象存储类型
     */
    private String storageClass;
    
    /**
     * 已经上传的段
     */
    private List<MultipartPart> part;
    
    /**
     * 返回请求中最大的Part数目
     */
    private Integer maxParts;
    
    /**
     * @return 上传任务所在的桶
     */
    public String getBucket()
    {
        return bucket;
    }
    /**
     * @param bucket 上传任务所在的桶
     */
    public void setBucket(String bucket)
    {
        this.bucket = bucket;
    }
    /**
     * @return 上传任务所属的对象名称
     */
    public String getKey()
    {
        return key;
    }
    /**
     * @param key 上传任务所属的对象名称
     */
    public void setKey(String key)
    {
        this.key = key;
    }
    /**
     * @return 上传任务的id
     */
    public String getUploadId()
    {
        return uploadId;
    }
    /**
     * @param uploadId 上传任务的id
     */
    public void setUploadId(String uploadId)
    {
        this.uploadId = uploadId;
    }
    
    /**
     * @return 任务初始化的用户
     */
    public S3Owner getInitiator()
    {
        return initiator;
    }
    
    /**
     * @param initiator 任务初始化的用户 
     */
    public void setInitiator(S3Owner initiator)
    {
        this.initiator = initiator;
    }
    /**
     * @return 拥有者
     */
    public S3Owner getOwner()
    {
        return owner;
    }
    /**
     * @param owner 拥有者
     */
    public void setOwner(S3Owner owner)
    {
        this.owner = owner;
    }
    /**
     * @return 存储类型
     */
    public String getStorageClass()
    {
        return storageClass;
    }
    /**
     * @param storageClass 存储类型
     */
    public void setStorageClass(String storageClass)
    {
        this.storageClass = storageClass;
    }
    /**
     * @return 上传任务中的段
     */
    public List<MultipartPart> getPart()
    {
        return part;
    }
    /**
     * @param part 上传任务中的段
     */
    public void setPart(List<MultipartPart> part)
    {
        this.part = part;
    }
    /**
     * @return 返回请求中最大的Part数目
     */
    public Integer getMaxParts()
    {
        return maxParts;
    }
    /**
     * @param maxParts 请求中最大的Part数目
     */
    public void setMaxParts(Integer maxParts)
    {
        this.maxParts = maxParts;
    }
    @Override
    public String toString()
    {
        return "S3ListPartsResult [bucket=" + bucket + ", key=" + key + ", uploadId=" + uploadId + ", initiator="
            + initiator + ", owner=" + owner + ", storageClass=" + storageClass + ", part=" + part + ", maxParts="
            + maxParts + "]";
    }
    
}
