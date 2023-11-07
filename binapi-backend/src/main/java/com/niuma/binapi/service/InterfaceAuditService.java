package com.niuma.binapi.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.niuma.binapi.model.dto.interfaceAudit.InterfaceAuditQueryRequest;
import com.niuma.binapi.model.dto.interfaceAudit.InterfaceAuditRequest;
import com.niuma.binapi.model.dto.interfaceinfo.InterfaceInfoAddRequest;
import com.niuma.binapi.model.entity.InterfaceAudit;
import com.baomidou.mybatisplus.extension.service.IService;
import com.niuma.binapi.model.vo.InterfaceAuditVO;
import com.niuma.binapicommon.model.entity.User;

import javax.servlet.http.HttpServletRequest;

/**
* @author niumazlb
* @description 针对表【interface_audit】的数据库操作Service
* @createDate 2023-06-12 22:26:29
*/
public interface InterfaceAuditService extends IService<InterfaceAudit> {
    /**
     * 审核接口通过
     * @param interfaceAuditRequest
     * @param loginUser
     * @return
     */
    boolean auditInterface(InterfaceAuditRequest interfaceAuditRequest, User loginUser);

    /**
     * 分页获取审核接口列表
     * @param interfaceAuditQueryRequest
     * @return
     */
    Page<InterfaceAuditVO> getInterfaceAuditListPage(InterfaceAuditQueryRequest interfaceAuditQueryRequest);
}
