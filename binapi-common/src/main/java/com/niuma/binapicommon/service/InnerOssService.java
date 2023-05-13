package com.niuma.binapicommon.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author niuma
 * @create 2023-05-11 15:24
 */
public interface InnerOssService {
    /**
     * 上传头像到 Oss
     * @param file
     * @return
     */
    String uploadFileAvatar(MultipartFile file);
}
