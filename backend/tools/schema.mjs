// 单一数据源：表结构定义。生成 Java 实体、Mapper、H2/MySQL DDL。
// 用法：node tools/schema.mjs
import fs from "node:fs";
import path from "node:path";

const S = (len = 64) => ["String", len];
const DEC = (p = "14,2") => ["BigDecimal", p];
const INT = ["Integer"];
const LONG = ["Long"];
const DT = ["LocalDateTime"];

/** @type {Array<{name:string,cls:string,fields:any[],ignoreTenant?:boolean,indexes?:string[][]}>} */
const tables = [
  {
    name: "tenant", cls: "Tenant", ignoreTenant: true, noTenant: true,
    fields: [
      ["code", ...S(32)], ["name", ...S(128)], ["creditCode", ...S(32)],
      ["industry", ...S(32)], ["status", ...S(16)], ["planId", ...LONG],
      ["seatLimit", ...INT], ["seatUsed", ...INT], ["trialEnd", ...DT],
      ["serviceStart", ...DT], ["serviceEnd", ...DT], ["networkMode", ...INT],
      ["contactName", ...S(32)], ["contactPhone", ...S(64)],
      ["contactEmail", ...S(64)], ["remark", ...S(500)],
    ],
    indexes: [["status"], ["code"]],
  },
  {
    name: "tenant_license", cls: "TenantLicense", ignoreTenant: true,
    fields: [
      ["licenseNo", ...S(64)], ["licenseExpire", ...DT],
      ["businessLicenseUrl", ...S(255)], ["legalIdUrl", ...S(255)],
      ["networkLicenseNo", ...S(64)], ["networkLicenseExpire", ...DT],
      ["status", ...S(16)], ["rejectReason", ...S(500)],
      ["reviewedBy", ...S(64)], ["reviewedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "plan", cls: "Plan", ignoreTenant: true, noTenant: true,
    fields: [
      ["name", ...S(64)], ["seatLimit", ...INT], ["storageGb", ...INT],
      ["smsCount", ...INT], ["priceMonth", ...DEC()], ["features", ...S(0)],
      ["status", ...S(16)], ["sortNo", ...INT],
    ],
  },
  {
    name: "seat_order", cls: "SeatOrder", ignoreTenant: true,
    fields: [["quantity", ...INT], ["amount", ...DEC()], ["remark", ...S(255)]],
    indexes: [["tenant_id"]],
  },
  {
    name: "bill", cls: "Bill", ignoreTenant: true,
    fields: [
      ["billNo", ...S(32)], ["period", ...S(16)], ["seatFee", ...DEC()],
      ["extraFee", ...DEC()], ["amount", ...DEC()], ["paidAmount", ...DEC()],
      ["status", ...S(16)], ["paidAt", ...DT], ["remark", ...S(255)],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "platform_user", cls: "PlatformUser", ignoreTenant: true, noTenant: true,
    fields: [
      ["username", ...S(64)], ["password", ...S(128)], ["nickname", ...S(64)],
      ["phone", ...S(64)], ["role", ...S(32)], ["status", ...S(16)],
      ["lastLoginAt", ...DT],
    ],
  },
  {
    name: "platform_role", cls: "PlatformRole", ignoreTenant: true, noTenant: true,
    fields: [["name", ...S(64)], ["menus", ...S(0)], ["buttons", ...S(0)], ["status", ...S(16)]],
  },
  {
    name: "announcement", cls: "Announcement", ignoreTenant: true,
    fields: [
      ["title", ...S(128)], ["content", ...S(0)],
      ["recordType", ...S(16)], ["status", ...S(16)], ["publishedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "audit_log", cls: "AuditLog", ignoreTenant: true,
    fields: [
      ["scope", ...S(16)], ["operatorId", ...LONG], ["operatorName", ...S(64)],
      ["action", ...S(64)], ["target", ...S(128)], ["detail", ...S(0)],
      ["ip", ...S(64)],
    ],
    indexes: [["tenant_id"], ["created_at"]],
  },
  {
    name: "platform_config", cls: "PlatformConfig", ignoreTenant: true, noTenant: true,
    fields: [["configKey", ...S(64)], ["configValue", ...S(0)], ["groupName", ...S(32)], ["remark", ...S(255)]],
  },
  {
    name: "platform_dict", cls: "PlatformDict", ignoreTenant: true, noTenant: true,
    fields: [["code", ...S(32)], ["name", ...S(64)], ["itemsJson", ...S(0)], ["status", ...S(16)]],
  },
  {
    name: "tenant_user", cls: "TenantUser",
    fields: [
      ["username", ...S(64)], ["password", ...S(128)], ["nickname", ...S(64)],
      ["phone", ...S(64)], ["roleId", ...LONG], ["orgId", ...LONG],
      ["status", ...S(16)], ["lastLoginAt", ...DT],
    ],
    indexes: [["tenant_id"], ["username"]],
  },
  {
    name: "tenant_role", cls: "TenantRole",
    fields: [
      ["name", ...S(64)], ["menus", ...S(0)], ["buttons", ...S(0)],
      ["dataScope", ...S(16)], ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "org", cls: "Org",
    fields: [
      ["name", ...S(64)], ["recordType", ...S(16)], ["parentId", ...LONG],
      ["address", ...S(255)], ["phone", ...S(64)], ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "sys_config", cls: "SysConfig",
    fields: [["configKey", ...S(64)], ["configValue", ...S(0)], ["groupName", ...S(32)], ["remark", ...S(255)]],
    indexes: [["tenant_id"]],
  },
  {
    name: "dict_type", cls: "DictType",
    fields: [["code", ...S(32)], ["name", ...S(64)], ["sortNo", ...INT], ["status", ...S(16)]],
    indexes: [["tenant_id"]],
  },
  {
    name: "dict_item", cls: "DictItem",
    fields: [
      ["typeCode", ...S(32)], ["code", ...S(32)], ["name", ...S(64)],
      ["sortNo", ...INT], ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "menu", cls: "Menu",
    fields: [
      ["channel", ...S(32)], ["name", ...S(64)], ["path", ...S(255)],
      ["icon", ...S(64)], ["sortNo", ...INT], ["parentId", ...LONG],
      ["payload", ...S(0)], ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "msg_template", cls: "MsgTemplate",
    fields: [
      ["node", ...S(32)], ["channel", ...S(32)], ["templateId", ...S(64)],
      ["title", ...S(128)], ["content", ...S(0)], ["smsChannel", ...S(32)],
      ["enabled", ...INT],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "msg_log", cls: "MsgLog",
    fields: [
      ["node", ...S(32)], ["channel", ...S(32)], ["receiver", ...S(64)],
      ["content", ...S(0)], ["status", ...S(16)], ["errorMsg", ...S(255)],
      ["retryCount", ...INT], ["waybillId", ...LONG],
    ],
    indexes: [["tenant_id"], ["created_at"]],
  },
  {
    name: "storage_config", cls: "StorageConfig",
    fields: [["driver", ...S(32)], ["configJson", ...S(0)], ["status", ...S(16)]],
    indexes: [["tenant_id"]],
  },
  {
    name: "sso_config", cls: "SsoConfig",
    fields: [["provider", ...S(32)], ["configJson", ...S(0)], ["enabled", ...INT]],
    indexes: [["tenant_id"]],
  },
  {
    name: "wechat_account", cls: "WechatAccount",
    fields: [
      ["accountType", ...S(32)], ["appId", ...S(64)], ["appSecret", ...S(128)],
      ["serverUrl", ...S(255)], ["token", ...S(64)], ["aesKey", ...S(64)],
      ["domains", ...S(255)], ["ipWhitelist", ...S(255)], ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "i18n_item", cls: "I18nItem",
    fields: [["lang", ...S(16)], ["keyName", ...S(128)], ["valueText", ...S(0)]],
    indexes: [["tenant_id"]],
  },
  {
    name: "cms_content", cls: "CmsContent",
    fields: [
      ["contentType", ...S(32)], ["title", ...S(128)], ["summary", ...S(255)],
      ["content", ...S(0)], ["cover", ...S(255)], ["lang", ...S(16)],
      ["status", ...S(16)], ["seoTitle", ...S(128)], ["seoKeywords", ...S(255)],
      ["publishedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["content_type"]],
  },
  {
    name: "site", cls: "Site",
    fields: [
      ["name", ...S(64)], ["address", ...S(255)], ["phone", ...S(64)],
      ["businessHours", ...S(64)], ["lat", ...DEC("10,6")], ["lng", ...DEC("10,6")],
      ["serviceArea", ...S(255)], ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "customer", cls: "Customer",
    fields: [
      ["code", ...S(32)], ["name", ...S(128)], ["recordType", ...S(32)],
      ["level", ...S(16)], ["contactName", ...S(32)], ["contactPhone", ...S(64)],
      ["settleType", ...S(32)], ["creditLimit", ...DEC()], ["creditUsed", ...DEC()],
      ["accountPeriodDays", ...INT], ["source", ...S(32)],
      ["address", ...S(255)], ["status", ...S(16)], ["remark", ...S(500)],
      ["salesId", ...LONG],
    ],
    indexes: [["tenant_id"], ["name"]],
  },
  {
    name: "contact", cls: "Contact",
    fields: [
      ["customerId", ...LONG], ["name", ...S(32)], ["phone", ...S(64)],
      ["title", ...S(32)], ["isDefault", ...INT],
    ],
    indexes: [["tenant_id"], ["customer_id"]],
  },
  {
    name: "address_book", cls: "AddressBook",
    fields: [
      ["customerId", ...LONG], ["name", ...S(32)], ["phone", ...S(64)],
      ["region", ...S(255)], ["detail", ...S(255)], ["recordType", ...S(16)],
      ["isDefault", ...INT],
    ],
    indexes: [["tenant_id"], ["customer_id"]],
  },
  {
    name: "agreement", cls: "Agreement",
    fields: [
      ["customerId", ...LONG], ["origin", ...S(64)], ["destination", ...S(64)],
      ["vehicleType", ...S(32)], ["price", ...DEC()], ["minPrice", ...DEC()],
      ["validFrom", ...DT], ["validTo", ...DT], ["status", ...S(16)],
      ["approver", ...S(64)],
    ],
    indexes: [["tenant_id"], ["customer_id"]],
  },
  {
    name: "lead", cls: "Lead",
    fields: [
      ["name", ...S(64)], ["phone", ...S(64)], ["company", ...S(128)],
      ["source", ...S(32)], ["remark", ...S(500)], ["status", ...S(16)],
      ["assigneeId", ...LONG], ["assigneeName", ...S(64)],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "followup", cls: "Followup",
    fields: [
      ["customerId", ...LONG], ["leadId", ...LONG], ["recordType", ...S(32)],
      ["content", ...S(0)], ["nextTime", ...DT], ["operator", ...S(64)],
      ["status", ...S(16)],
    ],
    indexes: [["tenant_id"], ["customer_id"]],
  },
  {
    name: "pricing_rule", cls: "PricingRule",
    fields: [
      ["name", ...S(64)], ["version", ...S(16)], ["status", ...S(16)],
      ["effectiveFrom", ...DT], ["effectiveTo", ...DT], ["configJson", ...S(0)],
      ["remark", ...S(255)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "quote", cls: "Quote",
    fields: [
      ["quoteNo", ...S(32)], ["customerId", ...LONG], ["origin", ...S(64)],
      ["destination", ...S(64)], ["cargoType", ...S(32)], ["weight", ...DEC("12,3")],
      ["volume", ...DEC("12,3")], ["pieces", ...INT], ["vehicleType", ...S(32)],
      ["baseFreight", ...DEC()], ["fuelSurcharge", ...DEC()], ["loadingFee", ...DEC()],
      ["upstairsFee", ...DEC()], ["insuranceFee", ...DEC()], ["codFee", ...DEC()],
      ["remoteFee", ...DEC()], ["holidayFee", ...DEC()], ["totalAmount", ...DEC()],
      ["ruleVersion", ...S(16)], ["priceSource", ...S(32)], ["detailJson", ...S(0)],
      ["snapshotJson", ...S(0)], ["status", ...S(16)], ["validUntil", ...DT],
      ["approvalStatus", ...S(16)], ["createdBy", ...S(64)],
    ],
    indexes: [["tenant_id"], ["quote_no"]],
  },
  {
    name: "waybill", cls: "Waybill",
    fields: [
      ["waybillNo", ...S(32)], ["customerId", ...LONG], ["customerName", ...S(128)],
      ["senderName", ...S(32)], ["senderPhone", ...S(64)], ["senderAddress", ...S(255)],
      ["receiverName", ...S(32)], ["receiverPhone", ...S(64)], ["receiverAddress", ...S(255)],
      ["origin", ...S(64)], ["destination", ...S(64)], ["cargoName", ...S(128)],
      ["cargoType", ...S(32)], ["weight", ...DEC("12,3")], ["actualWeight", ...DEC("12,3")],
      ["volume", ...DEC("12,3")], ["actualVolume", ...DEC("12,3")],
      ["pieces", ...INT], ["vehicleType", ...S(32)], ["pickupType", ...S(32)],
      ["deliveryType", ...S(32)], ["settleType", ...S(32)], ["pickupTime", ...DT],
      ["deliveryTime", ...DT], ["status", ...S(16)], ["quoteNo", ...S(32)],
      ["baseFreight", ...DEC()], ["fuelSurcharge", ...DEC()], ["loadingFee", ...DEC()],
      ["upstairsFee", ...DEC()], ["insuranceFee", ...DEC()], ["codFee", ...DEC()],
      ["remoteFee", ...DEC()], ["holidayFee", ...DEC()], ["totalAmount", ...DEC()],
      ["receivableAmount", ...DEC()], ["payableAmount", ...DEC()], ["grossProfit", ...DEC()],
      ["insured", ...INT], ["codAmount", ...DEC()], ["ruleVersion", ...S(16)],
      ["driverId", ...LONG], ["driverName", ...S(32)], ["driverPhone", ...S(64)],
      ["plateNo", ...S(32)], ["carrierId", ...LONG], ["carrierName", ...S(128)],
      ["source", ...S(32)], ["remark", ...S(500)], ["createdBy", ...S(64)],
      ["repriceStatus", ...S(16)], ["repriceAmount", ...DEC()],
      ["acceptedAt", ...DT], ["signedAt", ...DT], ["finishedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["waybill_no"], ["status"], ["customer_id"]],
  },
  {
    name: "waybill_item", cls: "WaybillItem",
    fields: [
      ["waybillId", ...LONG], ["name", ...S(128)], ["quantity", ...INT],
      ["weight", ...DEC("12,3")], ["volume", ...DEC("12,3")], ["remark", ...S(255)],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "waybill_photo", cls: "WaybillPhoto",
    fields: [
      ["waybillId", ...LONG], ["stage", ...S(32)], ["url", ...S(255)],
      ["sortNo", ...INT],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "waybill_status_log", cls: "WaybillStatusLog",
    fields: [
      ["waybillId", ...LONG], ["fromStatus", ...S(16)], ["toStatus", ...S(16)],
      ["action", ...S(32)], ["operator", ...S(64)], ["reason", ...S(500)],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "waybill_version", cls: "WaybillVersion",
    fields: [
      ["waybillId", ...LONG], ["version", ...INT], ["snapshotJson", ...S(0)],
      ["changeReason", ...S(255)],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "dispatch", cls: "Dispatch",
    fields: [
      ["dispatchNo", ...S(32)], ["waybillId", ...LONG], ["vehicleId", ...LONG],
      ["driverId", ...LONG], ["carrierId", ...LONG], ["status", ...S(16)],
      ["source", ...S(32)], ["assignee", ...S(64)], ["assignedAt", ...DT],
      ["acceptedAt", ...DT], ["emptyFee", ...DEC()], ["remark", ...S(255)],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "vehicle", cls: "Vehicle",
    fields: [
      ["plateNo", ...S(32)], ["vehicleType", ...S(32)], ["tonnage", ...DEC("10,2")],
      ["volume", ...DEC("10,2")], ["transportNo", ...S(64)], ["transportExpire", ...DT],
      ["insuranceExpire", ...DT], ["fleet", ...S(64)], ["status", ...S(16)],
      ["currentLat", ...DEC("10,6")], ["currentLng", ...DEC("10,6")],
      ["currentStatus", ...S(16)],
    ],
    indexes: [["tenant_id"], ["plate_no"]],
  },
  {
    name: "driver", cls: "Driver",
    fields: [
      ["name", ...S(32)], ["phone", ...S(64)], ["licenseNo", ...S(64)],
      ["licenseExpire", ...DT], ["qualificationNo", ...S(64)],
      ["qualificationExpire", ...DT], ["vehicleId", ...LONG], ["status", ...S(16)],
      ["userId", ...LONG], ["carrierId", ...LONG],
    ],
    indexes: [["tenant_id"], ["phone"]],
  },
  {
    name: "carrier", cls: "Carrier",
    fields: [
      ["name", ...S(128)], ["licenseNo", ...S(64)], ["licenseExpire", ...DT],
      ["settlePriceJson", ...S(0)], ["score", ...DEC("5,2")], ["status", ...S(16)],
      ["contactName", ...S(32)], ["contactPhone", ...S(64)], ["userId", ...LONG],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "track_point", cls: "TrackPoint",
    fields: [
      ["waybillId", ...LONG], ["vehicleId", ...LONG], ["lat", ...DEC("10,6")],
      ["lng", ...DEC("10,6")], ["speed", ...DEC("8,2")], ["reportedAt", ...DT],
      ["bucket", ...S(16)],
    ],
    indexes: [["tenant_id"], ["waybill_id"], ["bucket"]],
  },
  {
    name: "track_node", cls: "TrackNode",
    fields: [
      ["waybillId", ...LONG], ["node", ...S(32)], ["lat", ...DEC("10,6")],
      ["lng", ...DEC("10,6")], ["operator", ...S(64)], ["remark", ...S(255)],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "exception_order", cls: "ExceptionOrder",
    fields: [
      ["waybillId", ...LONG], ["recordType", ...S(32)], ["description", ...S(0)],
      ["images", ...S(0)], ["status", ...S(16)], ["assignee", ...S(64)],
      ["conclusion", ...S(500)], ["escalated", ...INT], ["handledAt", ...DT],
    ],
    indexes: [["tenant_id"], ["waybill_id"], ["status"]],
  },
  {
    name: "sign_record", cls: "SignRecord",
    fields: [
      ["waybillId", ...LONG], ["signerName", ...S(32)], ["signerPhone", ...S(64)],
      ["signImage", ...S(255)], ["lat", ...DEC("10,6")], ["lng", ...DEC("10,6")],
      ["signedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "receipt", cls: "Receipt",
    fields: [
      ["waybillId", ...LONG], ["images", ...S(0)], ["status", ...S(16)],
      ["rejectReason", ...S(255)], ["reviewer", ...S(64)], ["reviewedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "return_task", cls: "ReturnTask",
    fields: [
      ["waybillId", ...LONG], ["reason", ...S(255)], ["disposition", ...S(32)],
      ["returnFee", ...DEC()], ["status", ...S(16)], ["completedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["waybill_id"]],
  },
  {
    name: "receivable", cls: "Receivable",
    fields: [
      ["waybillId", ...LONG], ["waybillNo", ...S(32)], ["customerId", ...LONG],
      ["customerName", ...S(128)], ["amount", ...DEC()], ["paidAmount", ...DEC()],
      ["status", ...S(16)], ["source", ...S(32)], ["statementId", ...LONG],
    ],
    indexes: [["tenant_id"], ["customer_id"], ["status"]],
  },
  {
    name: "payable", cls: "Payable",
    fields: [
      ["waybillId", ...LONG], ["waybillNo", ...S(32)], ["carrierId", ...LONG],
      ["driverId", ...LONG], ["payeeName", ...S(128)], ["amount", ...DEC()],
      ["paidAmount", ...DEC()], ["status", ...S(16)], ["statementId", ...LONG],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "statement", cls: "Statement",
    fields: [
      ["statementNo", ...S(32)], ["recordType", ...S(16)], ["customerId", ...LONG],
      ["carrierId", ...LONG], ["partyName", ...S(128)], ["periodStart", ...DT],
      ["periodEnd", ...DT], ["totalAmount", ...DEC()], ["confirmedAmount", ...DEC()],
      ["status", ...S(16)], ["confirmedAt", ...DT], ["disputeReason", ...S(500)],
      ["createdBy", ...S(64)],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "statement_item", cls: "StatementItem",
    fields: [
      ["statementId", ...LONG], ["waybillId", ...LONG], ["waybillNo", ...S(32)],
      ["amount", ...DEC()], ["difference", ...DEC()], ["remark", ...S(255)],
    ],
    indexes: [["tenant_id"], ["statement_id"]],
  },
  {
    name: "invoice", cls: "Invoice",
    fields: [
      ["invoiceNo", ...S(64)], ["statementId", ...LONG], ["customerId", ...LONG],
      ["invoiceType", ...S(32)], ["amount", ...DEC()], ["status", ...S(16)],
      ["appliedBy", ...S(64)], ["issuedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["customer_id"]],
  },
  {
    name: "payment", cls: "Payment",
    fields: [
      ["paymentNo", ...S(32)], ["statementId", ...LONG], ["customerId", ...LONG],
      ["amount", ...DEC()], ["method", ...S(32)], ["differenceAmount", ...DEC()],
      ["differenceReason", ...S(255)], ["operator", ...S(64)], ["paidAt", ...DT],
    ],
    indexes: [["tenant_id"], ["statement_id"]],
  },
  {
    name: "cod_settle", cls: "CodSettle",
    fields: [
      ["waybillId", ...LONG], ["waybillNo", ...S(32)], ["amount", ...DEC()],
      ["fee", ...DEC()], ["status", ...S(16)], ["channelNo", ...S(64)],
      ["settledAt", ...DT],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "claim", cls: "Claim",
    fields: [
      ["claimNo", ...S(32)], ["waybillId", ...LONG], ["waybillNo", ...S(32)],
      ["recordType", ...S(32)], ["description", ...S(0)], ["evidence", ...S(0)],
      ["liability", ...S(32)], ["liabilityReason", ...S(500)], ["amount", ...DEC()],
      ["status", ...S(16)], ["rejectReason", ...S(500)], ["handler", ...S(64)],
      ["closedAt", ...DT],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
  {
    name: "complaint", cls: "Complaint",
    fields: [
      ["waybillId", ...LONG], ["customerId", ...LONG], ["recordType", ...S(32)],
      ["content", ...S(0)], ["satisfaction", ...INT], ["handler", ...S(64)],
      ["status", ...S(16)],
    ],
    indexes: [["tenant_id"]],
  },
  {
    name: "repair_task", cls: "RepairTask",
    fields: [
      ["recordType", ...S(32)], ["waybillId", ...LONG], ["detail", ...S(1000)],
      ["status", ...S(16)], ["handledAt", ...DT],
    ],
    indexes: [["tenant_id"], ["status"]],
  },
];

// ---------- 生成 ----------
const root = path.resolve(process.cwd());
const domain = path.join(root, "wuliu-domain/src/main/java/com/quantaida/wuliu/domain");
const apiRoot = path.join(root, "wuliu-api/src/main/java/com/quantaida/wuliu/api");
fs.mkdirSync(path.join(domain, "entity"), { recursive: true });
fs.mkdirSync(path.join(domain, "mapper"), { recursive: true });

const javaType = (t) => ({ String: "String", BigDecimal: "BigDecimal", Integer: "Integer", Long: "Long", LocalDateTime: "LocalDateTime" }[t]);
const pad = (s, n = 64) => s;

function entityCode(t) {
  const lines = [];
  lines.push("package com.quantaida.wuliu.domain.entity;");
  lines.push("");
  lines.push("import com.baomidou.mybatisplus.annotation.*;");
  lines.push("import lombok.Data;");
  lines.push("import java.math.BigDecimal;");
  lines.push("import java.time.LocalDateTime;");
  lines.push("");
  lines.push(`/** ${t.cls} 实体（表 ${t.name}） */`);
  lines.push("@Data");
  lines.push(`@TableName("${t.name}")`);
  lines.push(`public class ${t.cls} {`);
  lines.push(`    @TableId(type = IdType.AUTO)\n    private Long id;`);
  if (!t.noTenant) lines.push(`    private Long tenantId;`);
  t.fields.forEach(([n, ty]) => {
    if (ty === "String" && /phone/i.test(n)) {
      lines.push(`    @com.baomidou.mybatisplus.annotation.TableField(typeHandler = com.quantaida.wuliu.common.crypto.CryptoTypeHandler.class)`);
      lines.push(`    @com.fasterxml.jackson.databind.annotation.JsonSerialize(using = com.quantaida.wuliu.common.crypto.PhoneMaskSerializer.class)`);
    }
    lines.push(`    private ${javaType(ty)} ${n};`);
  });
  if (!t.noBase) {
    lines.push(`    @TableField(fill = FieldFill.INSERT)\n    private LocalDateTime createdAt;`);
    lines.push(`    @TableField(fill = FieldFill.INSERT_UPDATE)\n    private LocalDateTime updatedAt;`);
    lines.push(`    @TableLogic\n    private Integer deleted;`);
  }
  lines.push("}");
  return lines.join("\n");
}

function mapperCode(t) {
  return `package com.quantaida.wuliu.domain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quantaida.wuliu.domain.entity.${t.cls};
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ${t.cls}Mapper extends BaseMapper<${t.cls}> {
}
`;
}

const colType = (t, len) => {
  if (t === "String") return len === 0 ? "TEXT" : `VARCHAR(${len})`;
  if (t === "BigDecimal") return `DECIMAL(${len})`;
  if (t === "Integer") return "INT";
  if (t === "Long") return "BIGINT";
  if (t === "LocalDateTime") return "DATETIME";
  return "VARCHAR(255)";
};
const lower = (s) => s.replace(/[A-Z]/g, (c) => "_" + c.toLowerCase());

function ddl(prefix, inlineIndex) {
  const parts = [];
  const indexSql = [];
  for (const t of tables) {
    const cols = [`  id BIGINT PRIMARY KEY AUTO_INCREMENT`];
    if (!t.noTenant) cols.push(`  tenant_id BIGINT NOT NULL DEFAULT 0`);
    t.fields.forEach(([n, ty, len]) => cols.push(`  ${lower(n)} ${colType(ty, len)}${ty === "String" && len !== 0 ? " NOT NULL DEFAULT ''" : ""}`));
    if (!t.noBase) {
      cols.push("  created_at DATETIME DEFAULT CURRENT_TIMESTAMP");
      cols.push("  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP");
      cols.push("  deleted INT NOT NULL DEFAULT 0");
    }
    const idxDefs = [];
    if (!t.noTenant) idxDefs.push([`idx_${t.name}_tenant`, ["tenant_id"]]);
    (t.indexes || []).forEach((colsArr, i) => idxDefs.push([`idx_${t.name}_${i}`, colsArr]));
    if (inlineIndex) {
      idxDefs.forEach(([name, arr]) => cols.push(`  KEY ${name} (${arr.map(lower).join(",")})`));
    } else {
      idxDefs.forEach(([name, arr]) =>
        indexSql.push(`CREATE INDEX IF NOT EXISTS ${name} ON ${t.name} (${arr.map(lower).join(",")});`));
    }
    parts.push(`CREATE TABLE IF NOT EXISTS ${t.name} (\n${cols.join(",\n")}\n)${prefix};`);
  }
  return parts.join("\n\n") + "\n\n" + indexSql.join("\n") + "\n";
}

for (const t of tables) {
  fs.writeFileSync(path.join(domain, "entity", `${t.cls}.java`), entityCode(t));
  fs.writeFileSync(path.join(domain, "mapper", `${t.cls}Mapper.java`), mapperCode(t));
}
fs.mkdirSync(path.join(root, "sql"), { recursive: true });
fs.writeFileSync(path.join(root, "sql/schema-h2.sql"), ddl("", false));
fs.writeFileSync(path.join(root, "sql/schema-mysql.sql"), ddl(" ENGINE=InnoDB DEFAULT CHARSET=utf8mb4", true));
fs.writeFileSync(path.join(root, "sql/drop.sql"), tables.map((t) => `DROP TABLE IF EXISTS ${t.name};`).join("\n"));

console.log(`generated ${tables.length} entities/mappers + DDL`);
