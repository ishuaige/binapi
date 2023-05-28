package com.niuma.binapi.controller;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.niuma.binapi.annotation.AuthCheck;
import com.niuma.binapi.model.excel.InterfaceInfoInvokeExcel;
import com.niuma.binapi.model.excel.InterfaceInfoOrderExcel;
import com.niuma.binapi.model.vo.InterfaceInfoVO;
import com.niuma.binapi.service.InterfaceInfoService;
import com.niuma.binapi.service.UserInterfaceInfoService;
import com.niuma.binapicommon.common.BaseResponse;
import com.niuma.binapicommon.common.ResultUtils;
import com.niuma.binapicommon.model.entity.InterfaceInfo;
import com.niuma.binapicommon.model.entity.Order;
import com.niuma.binapicommon.model.vo.OrderVO;
import com.niuma.binapicommon.service.InnerOrderService;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 分析控制器
 *
 * @author niumazlb
 */
@RestController
@RequestMapping("/analysis")
@Slf4j
public class AnalysisController {


    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;

    @DubboReference
    private InnerOrderService innerOrderService;

    @GetMapping("/top/interface/invoke")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<InterfaceInfoVO>> listTopInvokeInterfaceInfo() {
        List<InterfaceInfoVO> interfaceInfoVOList = userInterfaceInfoService.interfaceInvokeTopAnalysis(5);
        return ResultUtils.success(interfaceInfoVOList);
    }


    @GetMapping("/top/interface/invoke/excel")
    @AuthCheck(mustRole = "admin")
    public void topInvokeInterfaceInfoExcel(HttpServletResponse response) throws IOException {
        List<InterfaceInfoVO> interfaceInfoVOList = userInterfaceInfoService.interfaceInvokeTopAnalysis(100);
        List<InterfaceInfoInvokeExcel> collect = interfaceInfoVOList.stream().map(interfaceInfoVO -> {
            InterfaceInfoInvokeExcel interfaceInfoExcel = new InterfaceInfoInvokeExcel();
            BeanUtils.copyProperties(interfaceInfoVO, interfaceInfoExcel);
            return interfaceInfoExcel;
        }).sorted((a,b)-> b.getTotalNum() - a.getTotalNum()).collect(Collectors.toList());

        String fileName = "interface_invoke.xlsx";
        genExcel(response,fileName, InterfaceInfoInvokeExcel.class,collect);
    }

    @GetMapping("/top/interface/buy")
    @AuthCheck(mustRole = "admin")
    public BaseResponse<List<OrderVO>> listTopBuyInterfaceInfo() {
        List<OrderVO> orderVOList = interfaceBuyTopAnalysis();
        return ResultUtils.success(orderVOList);
    }

    private List<OrderVO> interfaceBuyTopAnalysis() {
        List<Order> orderList = innerOrderService.listTopBuyInterfaceInfo(5);
        List<OrderVO> orderVOList = orderList.stream().map(order -> {
            Long interfaceId = order.getInterfaceId();
            InterfaceInfo interfaceInfo = interfaceInfoService.getById(interfaceId);
            OrderVO orderVO = new OrderVO();
            orderVO.setInterfaceId(interfaceId);
            orderVO.setTotal(order.getTotal());
            orderVO.setInterfaceName(interfaceInfo.getName());
            orderVO.setInterfaceDesc(interfaceInfo.getDescription());
            return orderVO;
        }).collect(Collectors.toList());
        return orderVOList;
    }

    @GetMapping("/top/interface/buy/excel")
    @AuthCheck(mustRole = "admin")
    public void topBuyInterfaceInfoExcel(HttpServletResponse response) throws IOException {
        List<OrderVO> orderVOList = interfaceBuyTopAnalysis();
        List<InterfaceInfoOrderExcel> collect = orderVOList.stream().map(orderVO -> {
            InterfaceInfoOrderExcel interfaceInfoOrderExcel = new InterfaceInfoOrderExcel();
            BeanUtils.copyProperties(orderVO, interfaceInfoOrderExcel);
            return interfaceInfoOrderExcel;
        }).sorted((a, b) -> (int) (b.getTotal() - a.getTotal())).collect(Collectors.toList());
        String fileName = "interface_buy.xlsx";

        genExcel(response,fileName,InterfaceInfoOrderExcel.class,collect);
    }

    private void genExcel(HttpServletResponse response,String fileName,Class entity, List collect) throws IOException {

        String sheetName = "analysis";
        response.setContentType("application/vnd.ms-excel");
        response.setCharacterEncoding("utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=" + fileName);
        // 创建ExcelWriter对象
        ExcelWriter excelWriter = EasyExcel.write(response.getOutputStream(), entity).build();
        // 创建工作表
        WriteSheet writeSheet = EasyExcel.writerSheet(sheetName).build();

        // 写入数据到工作表
        excelWriter.write(collect, writeSheet);

        // 关闭ExcelWriter对象
        excelWriter.finish();
    }
}
