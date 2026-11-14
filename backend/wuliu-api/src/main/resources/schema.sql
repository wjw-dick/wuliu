CREATE TABLE IF NOT EXISTS tenant (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL DEFAULT '',
  name VARCHAR(128) NOT NULL DEFAULT '',
  credit_code VARCHAR(32) NOT NULL DEFAULT '',
  industry VARCHAR(32) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  plan_id BIGINT,
  seat_limit INT,
  seat_used INT,
  trial_end DATETIME,
  service_start DATETIME,
  service_end DATETIME,
  network_mode INT,
  contact_name VARCHAR(32) NOT NULL DEFAULT '',
  contact_phone VARCHAR(64) NOT NULL DEFAULT '',
  contact_email VARCHAR(64) NOT NULL DEFAULT '',
  remark VARCHAR(500) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tenant_license (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  license_no VARCHAR(64) NOT NULL DEFAULT '',
  license_expire DATETIME,
  business_license_url VARCHAR(255) NOT NULL DEFAULT '',
  legal_id_url VARCHAR(255) NOT NULL DEFAULT '',
  network_license_no VARCHAR(64) NOT NULL DEFAULT '',
  network_license_expire DATETIME,
  status VARCHAR(16) NOT NULL DEFAULT '',
  reject_reason VARCHAR(500) NOT NULL DEFAULT '',
  reviewed_by VARCHAR(64) NOT NULL DEFAULT '',
  reviewed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS plan (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL DEFAULT '',
  seat_limit INT,
  storage_gb INT,
  sms_count INT,
  price_month DECIMAL(14,2),
  features TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  sort_no INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS seat_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  quantity INT,
  amount DECIMAL(14,2),
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS bill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  bill_no VARCHAR(32) NOT NULL DEFAULT '',
  period VARCHAR(16) NOT NULL DEFAULT '',
  seat_fee DECIMAL(14,2),
  extra_fee DECIMAL(14,2),
  amount DECIMAL(14,2),
  paid_amount DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  paid_at DATETIME,
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS platform_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(64) NOT NULL DEFAULT '',
  password VARCHAR(128) NOT NULL DEFAULT '',
  nickname VARCHAR(64) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  role VARCHAR(32) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  last_login_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS platform_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(64) NOT NULL DEFAULT '',
  menus TEXT,
  buttons TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS announcement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  title VARCHAR(128) NOT NULL DEFAULT '',
  content TEXT,
  record_type VARCHAR(16) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  published_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS audit_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  scope VARCHAR(16) NOT NULL DEFAULT '',
  operator_id BIGINT,
  operator_name VARCHAR(64) NOT NULL DEFAULT '',
  action VARCHAR(64) NOT NULL DEFAULT '',
  target VARCHAR(128) NOT NULL DEFAULT '',
  detail TEXT,
  ip VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS platform_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  config_key VARCHAR(64) NOT NULL DEFAULT '',
  config_value TEXT,
  group_name VARCHAR(32) NOT NULL DEFAULT '',
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS platform_dict (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(32) NOT NULL DEFAULT '',
  name VARCHAR(64) NOT NULL DEFAULT '',
  items_json TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tenant_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  username VARCHAR(64) NOT NULL DEFAULT '',
  password VARCHAR(128) NOT NULL DEFAULT '',
  nickname VARCHAR(64) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  role_id BIGINT,
  org_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  last_login_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS tenant_role (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL DEFAULT '',
  menus TEXT,
  buttons TEXT,
  data_scope VARCHAR(16) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS org (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL DEFAULT '',
  record_type VARCHAR(16) NOT NULL DEFAULT '',
  parent_id BIGINT,
  address VARCHAR(255) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sys_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  config_key VARCHAR(64) NOT NULL DEFAULT '',
  config_value TEXT,
  group_name VARCHAR(32) NOT NULL DEFAULT '',
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dict_type (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  code VARCHAR(32) NOT NULL DEFAULT '',
  name VARCHAR(64) NOT NULL DEFAULT '',
  sort_no INT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dict_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  type_code VARCHAR(32) NOT NULL DEFAULT '',
  code VARCHAR(32) NOT NULL DEFAULT '',
  name VARCHAR(64) NOT NULL DEFAULT '',
  sort_no INT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS menu (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  channel VARCHAR(32) NOT NULL DEFAULT '',
  name VARCHAR(64) NOT NULL DEFAULT '',
  path VARCHAR(255) NOT NULL DEFAULT '',
  icon VARCHAR(64) NOT NULL DEFAULT '',
  sort_no INT,
  parent_id BIGINT,
  payload TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS msg_template (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  node VARCHAR(32) NOT NULL DEFAULT '',
  channel VARCHAR(32) NOT NULL DEFAULT '',
  template_id VARCHAR(64) NOT NULL DEFAULT '',
  title VARCHAR(128) NOT NULL DEFAULT '',
  content TEXT,
  sms_channel VARCHAR(32) NOT NULL DEFAULT '',
  enabled INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS msg_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  node VARCHAR(32) NOT NULL DEFAULT '',
  channel VARCHAR(32) NOT NULL DEFAULT '',
  receiver VARCHAR(64) NOT NULL DEFAULT '',
  content TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  error_msg VARCHAR(255) NOT NULL DEFAULT '',
  retry_count INT,
  waybill_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS storage_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  driver VARCHAR(32) NOT NULL DEFAULT '',
  config_json TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sso_config (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  provider VARCHAR(32) NOT NULL DEFAULT '',
  config_json TEXT,
  enabled INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS wechat_account (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  account_type VARCHAR(32) NOT NULL DEFAULT '',
  app_id VARCHAR(64) NOT NULL DEFAULT '',
  app_secret VARCHAR(128) NOT NULL DEFAULT '',
  server_url VARCHAR(255) NOT NULL DEFAULT '',
  token VARCHAR(64) NOT NULL DEFAULT '',
  aes_key VARCHAR(64) NOT NULL DEFAULT '',
  domains VARCHAR(255) NOT NULL DEFAULT '',
  ip_whitelist VARCHAR(255) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS i18n_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  lang VARCHAR(16) NOT NULL DEFAULT '',
  key_name VARCHAR(128) NOT NULL DEFAULT '',
  value_text TEXT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cms_content (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  content_type VARCHAR(32) NOT NULL DEFAULT '',
  title VARCHAR(128) NOT NULL DEFAULT '',
  summary VARCHAR(255) NOT NULL DEFAULT '',
  content TEXT,
  cover VARCHAR(255) NOT NULL DEFAULT '',
  lang VARCHAR(16) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  seo_title VARCHAR(128) NOT NULL DEFAULT '',
  seo_keywords VARCHAR(255) NOT NULL DEFAULT '',
  published_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS site (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL DEFAULT '',
  address VARCHAR(255) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  business_hours VARCHAR(64) NOT NULL DEFAULT '',
  lat DECIMAL(10,6),
  lng DECIMAL(10,6),
  service_area VARCHAR(255) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS customer (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  code VARCHAR(32) NOT NULL DEFAULT '',
  name VARCHAR(128) NOT NULL DEFAULT '',
  record_type VARCHAR(32) NOT NULL DEFAULT '',
  level VARCHAR(16) NOT NULL DEFAULT '',
  contact_name VARCHAR(32) NOT NULL DEFAULT '',
  contact_phone VARCHAR(64) NOT NULL DEFAULT '',
  settle_type VARCHAR(32) NOT NULL DEFAULT '',
  credit_limit DECIMAL(14,2),
  credit_used DECIMAL(14,2),
  account_period_days INT,
  source VARCHAR(32) NOT NULL DEFAULT '',
  address VARCHAR(255) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  remark VARCHAR(500) NOT NULL DEFAULT '',
  sales_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS contact (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  customer_id BIGINT,
  name VARCHAR(32) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  title VARCHAR(32) NOT NULL DEFAULT '',
  is_default INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS address_book (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  customer_id BIGINT,
  name VARCHAR(32) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  region VARCHAR(255) NOT NULL DEFAULT '',
  detail VARCHAR(255) NOT NULL DEFAULT '',
  record_type VARCHAR(16) NOT NULL DEFAULT '',
  is_default INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS agreement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  customer_id BIGINT,
  origin VARCHAR(64) NOT NULL DEFAULT '',
  destination VARCHAR(64) NOT NULL DEFAULT '',
  vehicle_type VARCHAR(32) NOT NULL DEFAULT '',
  price DECIMAL(14,2),
  min_price DECIMAL(14,2),
  valid_from DATETIME,
  valid_to DATETIME,
  status VARCHAR(16) NOT NULL DEFAULT '',
  approver VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS lead (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  company VARCHAR(128) NOT NULL DEFAULT '',
  source VARCHAR(32) NOT NULL DEFAULT '',
  remark VARCHAR(500) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  assignee_id BIGINT,
  assignee_name VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS followup (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  customer_id BIGINT,
  lead_id BIGINT,
  record_type VARCHAR(32) NOT NULL DEFAULT '',
  content TEXT,
  next_time DATETIME,
  operator VARCHAR(64) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS pricing_rule (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(64) NOT NULL DEFAULT '',
  version VARCHAR(16) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  effective_from DATETIME,
  effective_to DATETIME,
  config_json TEXT,
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS quote (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  quote_no VARCHAR(32) NOT NULL DEFAULT '',
  customer_id BIGINT,
  origin VARCHAR(64) NOT NULL DEFAULT '',
  destination VARCHAR(64) NOT NULL DEFAULT '',
  cargo_type VARCHAR(32) NOT NULL DEFAULT '',
  weight DECIMAL(12,3),
  volume DECIMAL(12,3),
  pieces INT,
  vehicle_type VARCHAR(32) NOT NULL DEFAULT '',
  base_freight DECIMAL(14,2),
  fuel_surcharge DECIMAL(14,2),
  loading_fee DECIMAL(14,2),
  upstairs_fee DECIMAL(14,2),
  insurance_fee DECIMAL(14,2),
  cod_fee DECIMAL(14,2),
  remote_fee DECIMAL(14,2),
  holiday_fee DECIMAL(14,2),
  total_amount DECIMAL(14,2),
  rule_version VARCHAR(16) NOT NULL DEFAULT '',
  price_source VARCHAR(32) NOT NULL DEFAULT '',
  detail_json TEXT,
  snapshot_json TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  valid_until DATETIME,
  approval_status VARCHAR(16) NOT NULL DEFAULT '',
  created_by VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS waybill (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_no VARCHAR(32) NOT NULL DEFAULT '',
  customer_id BIGINT,
  customer_name VARCHAR(128) NOT NULL DEFAULT '',
  sender_name VARCHAR(32) NOT NULL DEFAULT '',
  sender_phone VARCHAR(64) NOT NULL DEFAULT '',
  sender_address VARCHAR(255) NOT NULL DEFAULT '',
  receiver_name VARCHAR(32) NOT NULL DEFAULT '',
  receiver_phone VARCHAR(64) NOT NULL DEFAULT '',
  receiver_address VARCHAR(255) NOT NULL DEFAULT '',
  origin VARCHAR(64) NOT NULL DEFAULT '',
  destination VARCHAR(64) NOT NULL DEFAULT '',
  cargo_name VARCHAR(128) NOT NULL DEFAULT '',
  cargo_type VARCHAR(32) NOT NULL DEFAULT '',
  weight DECIMAL(12,3),
  actual_weight DECIMAL(12,3),
  volume DECIMAL(12,3),
  actual_volume DECIMAL(12,3),
  pieces INT,
  vehicle_type VARCHAR(32) NOT NULL DEFAULT '',
  pickup_type VARCHAR(32) NOT NULL DEFAULT '',
  delivery_type VARCHAR(32) NOT NULL DEFAULT '',
  settle_type VARCHAR(32) NOT NULL DEFAULT '',
  pickup_time DATETIME,
  delivery_time DATETIME,
  status VARCHAR(16) NOT NULL DEFAULT '',
  quote_no VARCHAR(32) NOT NULL DEFAULT '',
  base_freight DECIMAL(14,2),
  fuel_surcharge DECIMAL(14,2),
  loading_fee DECIMAL(14,2),
  upstairs_fee DECIMAL(14,2),
  insurance_fee DECIMAL(14,2),
  cod_fee DECIMAL(14,2),
  remote_fee DECIMAL(14,2),
  holiday_fee DECIMAL(14,2),
  total_amount DECIMAL(14,2),
  receivable_amount DECIMAL(14,2),
  payable_amount DECIMAL(14,2),
  gross_profit DECIMAL(14,2),
  insured INT,
  cod_amount DECIMAL(14,2),
  rule_version VARCHAR(16) NOT NULL DEFAULT '',
  driver_id BIGINT,
  driver_name VARCHAR(32) NOT NULL DEFAULT '',
  driver_phone VARCHAR(64) NOT NULL DEFAULT '',
  plate_no VARCHAR(32) NOT NULL DEFAULT '',
  carrier_id BIGINT,
  carrier_name VARCHAR(128) NOT NULL DEFAULT '',
  source VARCHAR(32) NOT NULL DEFAULT '',
  remark VARCHAR(500) NOT NULL DEFAULT '',
  created_by VARCHAR(64) NOT NULL DEFAULT '',
  reprice_status VARCHAR(16) NOT NULL DEFAULT '',
  reprice_amount DECIMAL(14,2),
  accepted_at DATETIME,
  signed_at DATETIME,
  finished_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS waybill_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  name VARCHAR(128) NOT NULL DEFAULT '',
  quantity INT,
  weight DECIMAL(12,3),
  volume DECIMAL(12,3),
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS waybill_photo (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  stage VARCHAR(32) NOT NULL DEFAULT '',
  url VARCHAR(255) NOT NULL DEFAULT '',
  sort_no INT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS waybill_status_log (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  from_status VARCHAR(16) NOT NULL DEFAULT '',
  to_status VARCHAR(16) NOT NULL DEFAULT '',
  action VARCHAR(32) NOT NULL DEFAULT '',
  operator VARCHAR(64) NOT NULL DEFAULT '',
  reason VARCHAR(500) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS waybill_version (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  version INT,
  snapshot_json TEXT,
  change_reason VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS dispatch (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  dispatch_no VARCHAR(32) NOT NULL DEFAULT '',
  waybill_id BIGINT,
  vehicle_id BIGINT,
  driver_id BIGINT,
  carrier_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  source VARCHAR(32) NOT NULL DEFAULT '',
  assignee VARCHAR(64) NOT NULL DEFAULT '',
  assigned_at DATETIME,
  accepted_at DATETIME,
  empty_fee DECIMAL(14,2),
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS vehicle (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  plate_no VARCHAR(32) NOT NULL DEFAULT '',
  vehicle_type VARCHAR(32) NOT NULL DEFAULT '',
  tonnage DECIMAL(10,2),
  volume DECIMAL(10,2),
  transport_no VARCHAR(64) NOT NULL DEFAULT '',
  transport_expire DATETIME,
  insurance_expire DATETIME,
  fleet VARCHAR(64) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  current_lat DECIMAL(10,6),
  current_lng DECIMAL(10,6),
  current_status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS driver (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(32) NOT NULL DEFAULT '',
  phone VARCHAR(64) NOT NULL DEFAULT '',
  license_no VARCHAR(64) NOT NULL DEFAULT '',
  license_expire DATETIME,
  qualification_no VARCHAR(64) NOT NULL DEFAULT '',
  qualification_expire DATETIME,
  vehicle_id BIGINT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  user_id BIGINT,
  carrier_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS carrier (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  name VARCHAR(128) NOT NULL DEFAULT '',
  license_no VARCHAR(64) NOT NULL DEFAULT '',
  license_expire DATETIME,
  settle_price_json TEXT,
  score DECIMAL(5,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  contact_name VARCHAR(32) NOT NULL DEFAULT '',
  contact_phone VARCHAR(64) NOT NULL DEFAULT '',
  user_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS track_point (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  vehicle_id BIGINT,
  lat DECIMAL(10,6),
  lng DECIMAL(10,6),
  speed DECIMAL(8,2),
  reported_at DATETIME,
  bucket VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS track_node (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  node VARCHAR(32) NOT NULL DEFAULT '',
  lat DECIMAL(10,6),
  lng DECIMAL(10,6),
  operator VARCHAR(64) NOT NULL DEFAULT '',
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS exception_order (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  record_type VARCHAR(32) NOT NULL DEFAULT '',
  description TEXT,
  images TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  assignee VARCHAR(64) NOT NULL DEFAULT '',
  conclusion VARCHAR(500) NOT NULL DEFAULT '',
  escalated INT,
  handled_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS sign_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  signer_name VARCHAR(32) NOT NULL DEFAULT '',
  signer_phone VARCHAR(64) NOT NULL DEFAULT '',
  sign_image VARCHAR(255) NOT NULL DEFAULT '',
  lat DECIMAL(10,6),
  lng DECIMAL(10,6),
  signed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS receipt (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  images TEXT,
  status VARCHAR(16) NOT NULL DEFAULT '',
  reject_reason VARCHAR(255) NOT NULL DEFAULT '',
  reviewer VARCHAR(64) NOT NULL DEFAULT '',
  reviewed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS return_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  reason VARCHAR(255) NOT NULL DEFAULT '',
  disposition VARCHAR(32) NOT NULL DEFAULT '',
  return_fee DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  completed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS receivable (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  waybill_no VARCHAR(32) NOT NULL DEFAULT '',
  customer_id BIGINT,
  customer_name VARCHAR(128) NOT NULL DEFAULT '',
  amount DECIMAL(14,2),
  paid_amount DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  source VARCHAR(32) NOT NULL DEFAULT '',
  statement_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS payable (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  waybill_no VARCHAR(32) NOT NULL DEFAULT '',
  carrier_id BIGINT,
  driver_id BIGINT,
  payee_name VARCHAR(128) NOT NULL DEFAULT '',
  amount DECIMAL(14,2),
  paid_amount DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  statement_id BIGINT,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS statement (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  statement_no VARCHAR(32) NOT NULL DEFAULT '',
  record_type VARCHAR(16) NOT NULL DEFAULT '',
  customer_id BIGINT,
  carrier_id BIGINT,
  party_name VARCHAR(128) NOT NULL DEFAULT '',
  period_start DATETIME,
  period_end DATETIME,
  total_amount DECIMAL(14,2),
  confirmed_amount DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  confirmed_at DATETIME,
  dispute_reason VARCHAR(500) NOT NULL DEFAULT '',
  created_by VARCHAR(64) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS statement_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  statement_id BIGINT,
  waybill_id BIGINT,
  waybill_no VARCHAR(32) NOT NULL DEFAULT '',
  amount DECIMAL(14,2),
  difference DECIMAL(14,2),
  remark VARCHAR(255) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS invoice (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  invoice_no VARCHAR(64) NOT NULL DEFAULT '',
  statement_id BIGINT,
  customer_id BIGINT,
  invoice_type VARCHAR(32) NOT NULL DEFAULT '',
  amount DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  applied_by VARCHAR(64) NOT NULL DEFAULT '',
  issued_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS payment (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  payment_no VARCHAR(32) NOT NULL DEFAULT '',
  statement_id BIGINT,
  customer_id BIGINT,
  amount DECIMAL(14,2),
  method VARCHAR(32) NOT NULL DEFAULT '',
  difference_amount DECIMAL(14,2),
  difference_reason VARCHAR(255) NOT NULL DEFAULT '',
  operator VARCHAR(64) NOT NULL DEFAULT '',
  paid_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS cod_settle (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  waybill_no VARCHAR(32) NOT NULL DEFAULT '',
  amount DECIMAL(14,2),
  fee DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  channel_no VARCHAR(64) NOT NULL DEFAULT '',
  settled_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS claim (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  claim_no VARCHAR(32) NOT NULL DEFAULT '',
  waybill_id BIGINT,
  waybill_no VARCHAR(32) NOT NULL DEFAULT '',
  record_type VARCHAR(32) NOT NULL DEFAULT '',
  description TEXT,
  evidence TEXT,
  liability VARCHAR(32) NOT NULL DEFAULT '',
  liability_reason VARCHAR(500) NOT NULL DEFAULT '',
  amount DECIMAL(14,2),
  status VARCHAR(16) NOT NULL DEFAULT '',
  reject_reason VARCHAR(500) NOT NULL DEFAULT '',
  handler VARCHAR(64) NOT NULL DEFAULT '',
  closed_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS complaint (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  waybill_id BIGINT,
  customer_id BIGINT,
  record_type VARCHAR(32) NOT NULL DEFAULT '',
  content TEXT,
  satisfaction INT,
  handler VARCHAR(64) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE TABLE IF NOT EXISTS repair_task (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  tenant_id BIGINT NOT NULL DEFAULT 0,
  record_type VARCHAR(32) NOT NULL DEFAULT '',
  waybill_id BIGINT,
  detail VARCHAR(1000) NOT NULL DEFAULT '',
  status VARCHAR(16) NOT NULL DEFAULT '',
  handled_at DATETIME,
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  updated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  deleted INT NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_tenant_0 ON tenant (status);
CREATE INDEX IF NOT EXISTS idx_tenant_1 ON tenant (code);
CREATE INDEX IF NOT EXISTS idx_tenant_license_tenant ON tenant_license (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_license_0 ON tenant_license (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_license_1 ON tenant_license (status);
CREATE INDEX IF NOT EXISTS idx_seat_order_tenant ON seat_order (tenant_id);
CREATE INDEX IF NOT EXISTS idx_seat_order_0 ON seat_order (tenant_id);
CREATE INDEX IF NOT EXISTS idx_bill_tenant ON bill (tenant_id);
CREATE INDEX IF NOT EXISTS idx_bill_0 ON bill (tenant_id);
CREATE INDEX IF NOT EXISTS idx_bill_1 ON bill (status);
CREATE INDEX IF NOT EXISTS idx_announcement_tenant ON announcement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_announcement_0 ON announcement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_announcement_1 ON announcement (status);
CREATE INDEX IF NOT EXISTS idx_audit_log_tenant ON audit_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_0 ON audit_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_audit_log_1 ON audit_log (created_at);
CREATE INDEX IF NOT EXISTS idx_tenant_user_tenant ON tenant_user (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_user_0 ON tenant_user (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_user_1 ON tenant_user (username);
CREATE INDEX IF NOT EXISTS idx_tenant_role_tenant ON tenant_role (tenant_id);
CREATE INDEX IF NOT EXISTS idx_tenant_role_0 ON tenant_role (tenant_id);
CREATE INDEX IF NOT EXISTS idx_org_tenant ON org (tenant_id);
CREATE INDEX IF NOT EXISTS idx_org_0 ON org (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_config_tenant ON sys_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sys_config_0 ON sys_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dict_type_tenant ON dict_type (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dict_type_0 ON dict_type (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dict_item_tenant ON dict_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dict_item_0 ON dict_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_menu_tenant ON menu (tenant_id);
CREATE INDEX IF NOT EXISTS idx_menu_0 ON menu (tenant_id);
CREATE INDEX IF NOT EXISTS idx_msg_template_tenant ON msg_template (tenant_id);
CREATE INDEX IF NOT EXISTS idx_msg_template_0 ON msg_template (tenant_id);
CREATE INDEX IF NOT EXISTS idx_msg_log_tenant ON msg_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_msg_log_0 ON msg_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_msg_log_1 ON msg_log (created_at);
CREATE INDEX IF NOT EXISTS idx_storage_config_tenant ON storage_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_storage_config_0 ON storage_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sso_config_tenant ON sso_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sso_config_0 ON sso_config (tenant_id);
CREATE INDEX IF NOT EXISTS idx_wechat_account_tenant ON wechat_account (tenant_id);
CREATE INDEX IF NOT EXISTS idx_wechat_account_0 ON wechat_account (tenant_id);
CREATE INDEX IF NOT EXISTS idx_i18n_item_tenant ON i18n_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_i18n_item_0 ON i18n_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_cms_content_tenant ON cms_content (tenant_id);
CREATE INDEX IF NOT EXISTS idx_cms_content_0 ON cms_content (tenant_id);
CREATE INDEX IF NOT EXISTS idx_cms_content_1 ON cms_content (content_type);
CREATE INDEX IF NOT EXISTS idx_site_tenant ON site (tenant_id);
CREATE INDEX IF NOT EXISTS idx_site_0 ON site (tenant_id);
CREATE INDEX IF NOT EXISTS idx_customer_tenant ON customer (tenant_id);
CREATE INDEX IF NOT EXISTS idx_customer_0 ON customer (tenant_id);
CREATE INDEX IF NOT EXISTS idx_customer_1 ON customer (name);
CREATE INDEX IF NOT EXISTS idx_contact_tenant ON contact (tenant_id);
CREATE INDEX IF NOT EXISTS idx_contact_0 ON contact (tenant_id);
CREATE INDEX IF NOT EXISTS idx_contact_1 ON contact (customer_id);
CREATE INDEX IF NOT EXISTS idx_address_book_tenant ON address_book (tenant_id);
CREATE INDEX IF NOT EXISTS idx_address_book_0 ON address_book (tenant_id);
CREATE INDEX IF NOT EXISTS idx_address_book_1 ON address_book (customer_id);
CREATE INDEX IF NOT EXISTS idx_agreement_tenant ON agreement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_agreement_0 ON agreement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_agreement_1 ON agreement (customer_id);
CREATE INDEX IF NOT EXISTS idx_lead_tenant ON lead (tenant_id);
CREATE INDEX IF NOT EXISTS idx_lead_0 ON lead (tenant_id);
CREATE INDEX IF NOT EXISTS idx_lead_1 ON lead (status);
CREATE INDEX IF NOT EXISTS idx_followup_tenant ON followup (tenant_id);
CREATE INDEX IF NOT EXISTS idx_followup_0 ON followup (tenant_id);
CREATE INDEX IF NOT EXISTS idx_followup_1 ON followup (customer_id);
CREATE INDEX IF NOT EXISTS idx_pricing_rule_tenant ON pricing_rule (tenant_id);
CREATE INDEX IF NOT EXISTS idx_pricing_rule_0 ON pricing_rule (tenant_id);
CREATE INDEX IF NOT EXISTS idx_quote_tenant ON quote (tenant_id);
CREATE INDEX IF NOT EXISTS idx_quote_0 ON quote (tenant_id);
CREATE INDEX IF NOT EXISTS idx_quote_1 ON quote (quote_no);
CREATE INDEX IF NOT EXISTS idx_waybill_tenant ON waybill (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_0 ON waybill (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_1 ON waybill (waybill_no);
CREATE INDEX IF NOT EXISTS idx_waybill_2 ON waybill (status);
CREATE INDEX IF NOT EXISTS idx_waybill_3 ON waybill (customer_id);
CREATE INDEX IF NOT EXISTS idx_waybill_item_tenant ON waybill_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_item_0 ON waybill_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_item_1 ON waybill_item (waybill_id);
CREATE INDEX IF NOT EXISTS idx_waybill_photo_tenant ON waybill_photo (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_photo_0 ON waybill_photo (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_photo_1 ON waybill_photo (waybill_id);
CREATE INDEX IF NOT EXISTS idx_waybill_status_log_tenant ON waybill_status_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_status_log_0 ON waybill_status_log (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_status_log_1 ON waybill_status_log (waybill_id);
CREATE INDEX IF NOT EXISTS idx_waybill_version_tenant ON waybill_version (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_version_0 ON waybill_version (tenant_id);
CREATE INDEX IF NOT EXISTS idx_waybill_version_1 ON waybill_version (waybill_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_tenant ON dispatch (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_0 ON dispatch (tenant_id);
CREATE INDEX IF NOT EXISTS idx_dispatch_1 ON dispatch (waybill_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_tenant ON vehicle (tenant_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_0 ON vehicle (tenant_id);
CREATE INDEX IF NOT EXISTS idx_vehicle_1 ON vehicle (plate_no);
CREATE INDEX IF NOT EXISTS idx_driver_tenant ON driver (tenant_id);
CREATE INDEX IF NOT EXISTS idx_driver_0 ON driver (tenant_id);
CREATE INDEX IF NOT EXISTS idx_driver_1 ON driver (phone);
CREATE INDEX IF NOT EXISTS idx_carrier_tenant ON carrier (tenant_id);
CREATE INDEX IF NOT EXISTS idx_carrier_0 ON carrier (tenant_id);
CREATE INDEX IF NOT EXISTS idx_track_point_tenant ON track_point (tenant_id);
CREATE INDEX IF NOT EXISTS idx_track_point_0 ON track_point (tenant_id);
CREATE INDEX IF NOT EXISTS idx_track_point_1 ON track_point (waybill_id);
CREATE INDEX IF NOT EXISTS idx_track_point_2 ON track_point (bucket);
CREATE INDEX IF NOT EXISTS idx_track_node_tenant ON track_node (tenant_id);
CREATE INDEX IF NOT EXISTS idx_track_node_0 ON track_node (tenant_id);
CREATE INDEX IF NOT EXISTS idx_track_node_1 ON track_node (waybill_id);
CREATE INDEX IF NOT EXISTS idx_exception_order_tenant ON exception_order (tenant_id);
CREATE INDEX IF NOT EXISTS idx_exception_order_0 ON exception_order (tenant_id);
CREATE INDEX IF NOT EXISTS idx_exception_order_1 ON exception_order (waybill_id);
CREATE INDEX IF NOT EXISTS idx_exception_order_2 ON exception_order (status);
CREATE INDEX IF NOT EXISTS idx_sign_record_tenant ON sign_record (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sign_record_0 ON sign_record (tenant_id);
CREATE INDEX IF NOT EXISTS idx_sign_record_1 ON sign_record (waybill_id);
CREATE INDEX IF NOT EXISTS idx_receipt_tenant ON receipt (tenant_id);
CREATE INDEX IF NOT EXISTS idx_receipt_0 ON receipt (tenant_id);
CREATE INDEX IF NOT EXISTS idx_receipt_1 ON receipt (waybill_id);
CREATE INDEX IF NOT EXISTS idx_return_task_tenant ON return_task (tenant_id);
CREATE INDEX IF NOT EXISTS idx_return_task_0 ON return_task (tenant_id);
CREATE INDEX IF NOT EXISTS idx_return_task_1 ON return_task (waybill_id);
CREATE INDEX IF NOT EXISTS idx_receivable_tenant ON receivable (tenant_id);
CREATE INDEX IF NOT EXISTS idx_receivable_0 ON receivable (tenant_id);
CREATE INDEX IF NOT EXISTS idx_receivable_1 ON receivable (customer_id);
CREATE INDEX IF NOT EXISTS idx_receivable_2 ON receivable (status);
CREATE INDEX IF NOT EXISTS idx_payable_tenant ON payable (tenant_id);
CREATE INDEX IF NOT EXISTS idx_payable_0 ON payable (tenant_id);
CREATE INDEX IF NOT EXISTS idx_payable_1 ON payable (status);
CREATE INDEX IF NOT EXISTS idx_statement_tenant ON statement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_statement_0 ON statement (tenant_id);
CREATE INDEX IF NOT EXISTS idx_statement_1 ON statement (status);
CREATE INDEX IF NOT EXISTS idx_statement_item_tenant ON statement_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_statement_item_0 ON statement_item (tenant_id);
CREATE INDEX IF NOT EXISTS idx_statement_item_1 ON statement_item (statement_id);
CREATE INDEX IF NOT EXISTS idx_invoice_tenant ON invoice (tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoice_0 ON invoice (tenant_id);
CREATE INDEX IF NOT EXISTS idx_invoice_1 ON invoice (customer_id);
CREATE INDEX IF NOT EXISTS idx_payment_tenant ON payment (tenant_id);
CREATE INDEX IF NOT EXISTS idx_payment_0 ON payment (tenant_id);
CREATE INDEX IF NOT EXISTS idx_payment_1 ON payment (statement_id);
CREATE INDEX IF NOT EXISTS idx_cod_settle_tenant ON cod_settle (tenant_id);
CREATE INDEX IF NOT EXISTS idx_cod_settle_0 ON cod_settle (tenant_id);
CREATE INDEX IF NOT EXISTS idx_cod_settle_1 ON cod_settle (status);
CREATE INDEX IF NOT EXISTS idx_claim_tenant ON claim (tenant_id);
CREATE INDEX IF NOT EXISTS idx_claim_0 ON claim (tenant_id);
CREATE INDEX IF NOT EXISTS idx_claim_1 ON claim (status);
CREATE INDEX IF NOT EXISTS idx_complaint_tenant ON complaint (tenant_id);
CREATE INDEX IF NOT EXISTS idx_complaint_0 ON complaint (tenant_id);
CREATE INDEX IF NOT EXISTS idx_repair_task_tenant ON repair_task (tenant_id);
CREATE INDEX IF NOT EXISTS idx_repair_task_0 ON repair_task (tenant_id);
CREATE INDEX IF NOT EXISTS idx_repair_task_1 ON repair_task (status);
