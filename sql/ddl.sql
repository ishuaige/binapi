/*
 Navicat Premium Data Transfer

 Source Server         : MySQL8.0
 Source Server Type    : MySQL
 Source Server Version : 80026
 Source Host           : localhost:3306
 Source Schema         : binapi

 Target Server Type    : MySQL
 Target Server Version : 80026
 File Encoding         : 65001

 Date: 13/05/2023 20:53:08
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for alipay_info
-- ----------------------------
DROP TABLE IF EXISTS `alipay_info`;
CREATE TABLE `alipay_info`  (
                                `orderNumber` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单id',
                                `subject` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易名称',
                                `totalAmount` float(10, 2) NOT NULL COMMENT '交易金额',
  `buyerPayAmount` float(10, 2) NOT NULL COMMENT '买家付款金额',
  `buyerId` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '买家在支付宝的唯一id',
  `tradeNo` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '支付宝交易凭证号',
  `tradeStatus` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '交易状态',
  `gmtPayment` datetime(0) NOT NULL COMMENT '买家付款时间',
  PRIMARY KEY (`orderNumber`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for api_order
-- ----------------------------
DROP TABLE IF EXISTS `api_order`;
CREATE TABLE `api_order`  (
                              `id` bigint(0) NOT NULL COMMENT '主键',
                              `interfaceId` bigint(0) NOT NULL COMMENT '接口id',
                              `userId` bigint(0) NOT NULL COMMENT '用户id',
                              `orderNumber` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单号',
                              `total` bigint(0) NOT NULL COMMENT '购买数量',
                              `charging` float(255, 2) NOT NULL COMMENT '单价',
  `totalAmount` float(10, 2) NOT NULL COMMENT '交易金额',
  `status` int(0) NOT NULL DEFAULT 0 COMMENT '交易状态【0->待付款；1->已完成；2->无效订单】',
  `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `isDelete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interface_charging
-- ----------------------------
DROP TABLE IF EXISTS `interface_charging`;
CREATE TABLE `interface_charging`  (
                                       `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                       `interfaceId` bigint(0) NOT NULL COMMENT '接口id',
                                       `charging` float(255, 2) NOT NULL COMMENT '计费规则（元/条）',
  `availablePieces` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口剩余可调用次数',
  `userId` bigint(0) NOT NULL COMMENT '创建人',
  `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  `isDelete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删, 1-已删)',
  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for interface_info
-- ----------------------------
DROP TABLE IF EXISTS `interface_info`;
CREATE TABLE `interface_info`  (
                                   `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                   `name` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '名称',
                                   `methodName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'SDK方法名',
                                   `url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '接口地址',
                                   `requestHeader` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求头',
                                   `description` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '描述',
                                   `status` int(0) NOT NULL DEFAULT 0 COMMENT '接口状态（0-关闭，1-开启）',
                                   `method` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '请求类型',
                                   `responseHeader` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '响应头',
                                   `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                   `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                   `isDelete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除，1-已删除)',
                                   `isCharging` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否收费(0-不收费，1-收费)',
                                   `requestParams` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '请求参数',
                                   `userId` bigint(0) NOT NULL COMMENT '创建人',
                                   PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 44 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '接口信息' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for order_lock
-- ----------------------------
DROP TABLE IF EXISTS `order_lock`;
CREATE TABLE `order_lock`  (
                               `id` bigint(0) NOT NULL COMMENT '主键',
                               `orderNumber` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '订单编号',
                               `chargingId` bigint(0) NOT NULL COMMENT '计费id',
                               `userId` bigint(0) NOT NULL COMMENT '用户id',
                               `lockNum` bigint(0) NOT NULL COMMENT '锁定数量',
                               `lockStatus` int(0) NOT NULL COMMENT '锁定状态(1-已锁定  0-已解锁 2-扣减)',
                               `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                               `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                               PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user
-- ----------------------------
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`  (
                         `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT 'id',
                         `userName` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户昵称',
                         `userAccount` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '账号',
                         `userAvatar` varchar(1024) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '用户头像',
                         `gender` tinyint(0) NULL DEFAULT NULL COMMENT '性别',
                         `userRole` varchar(256) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'user' COMMENT '用户角色：user / admin',
                         `userPassword` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '密码',
                         `phoneNum` varchar(15) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '手机号码',
                         `accessKey` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'accessKey',
                         `secretKey` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT 'secretKey',
                         `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                         `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                         `isDelete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除',
                         PRIMARY KEY (`id`) USING BTREE,
                         UNIQUE INDEX `uni_userAccount`(`userAccount`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 11 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Table structure for user_interface_info
-- ----------------------------
DROP TABLE IF EXISTS `user_interface_info`;
CREATE TABLE `user_interface_info`  (
                                        `id` bigint(0) NOT NULL AUTO_INCREMENT COMMENT '主键',
                                        `userId` bigint(0) NOT NULL COMMENT '调用者 id',
                                        `interfaceInfoId` bigint(0) NOT NULL COMMENT '接口 id',
                                        `totalNum` int(0) NOT NULL DEFAULT 0 COMMENT '总调用次数',
                                        `leftNum` int(0) NOT NULL DEFAULT 0 COMMENT '剩余调用次数',
                                        `status` int(0) NOT NULL DEFAULT 0 COMMENT '0-正常，1-禁用）',
                                        `createTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
                                        `updateTime` datetime(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
                                        `isDelete` tinyint(0) NOT NULL DEFAULT 0 COMMENT '是否删除(0-未删除，1-已删除)',
                                        PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户调用接口关系' ROW_FORMAT = Dynamic;

SET FOREIGN_KEY_CHECKS = 1;

-- ----------------------------
-- Table structure for interface_audit
-- ----------------------------
-- auto-generated definition
create table interface_audit
(
    id          bigint auto_increment comment '主键'
        primary key,
    interfaceId bigint                             not null comment '接口ID',
    userId      bigint                             not null comment '申请人id',
    approverId  bigint                             null comment '审批人ID',
    createTime  datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    updateTime  datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    isDeleted   tinyint  default 0                 not null comment '逻辑删除标志',
    remark      varchar(225)                       null comment '备注',
    auditStatus int      default 0                 not null comment '0--待审核 1--审核结束'
);