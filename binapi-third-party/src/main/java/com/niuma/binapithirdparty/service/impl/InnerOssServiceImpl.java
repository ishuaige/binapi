package com.niuma.binapithirdparty.service.impl;

import com.niuma.binapicommon.service.InnerOssService;
import com.niuma.binapithirdparty.service.OssService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;

/**
 * @author niuma
 * @create 2023-05-11 15:25
 */
@DubboService
public class InnerOssServiceImpl implements InnerOssService {
    @Resource
    OssService ossService;
    @Override
    public String uploadFileAvatar(MultipartFile file) {
        return ossService.uploadFileAvatar(file);
    }
}
