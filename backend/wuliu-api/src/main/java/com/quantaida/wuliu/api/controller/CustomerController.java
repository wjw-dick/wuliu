package com.quantaida.wuliu.api.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quantaida.wuliu.common.web.BaseCrudController;
import com.quantaida.wuliu.common.result.PageResult;
import com.quantaida.wuliu.domain.entity.*;
import com.quantaida.wuliu.domain.mapper.*;
import com.quantaida.wuliu.service.system.AuditService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 客户域：客户档案、联系人、地址簿、协议价、线索、跟进。 */
@RestController
@RequestMapping("/api/tenant")
public class CustomerController extends BaseCrudController<Customer> {
    private final CustomerMapper customerMapper;
    private final ContactMapper contactMapper;
    private final AddressBookMapper addressMapper;
    private final AgreementMapper agreementMapper;
    private final FollowupMapper followupMapper;
    private final LeadMapper leadMapper;
    private final AuditService auditService;

    public CustomerController(CustomerMapper customerMapper, ContactMapper contactMapper,
                              AddressBookMapper addressMapper, AgreementMapper agreementMapper,
                              FollowupMapper followupMapper, LeadMapper leadMapper, AuditService auditService) {
        super(customerMapper, Customer.class);
        this.customerMapper = customerMapper;
        this.contactMapper = contactMapper;
        this.addressMapper = addressMapper;
        this.agreementMapper = agreementMapper;
        this.followupMapper = followupMapper;
        this.leadMapper = leadMapper;
        this.auditService = auditService;
    }

    @PostMapping("/customers")
    @Override
    public Customer create(@RequestBody Customer customer) {
        if (customer.getCode() == null || customer.getCode().isBlank()) {
            customer.setCode("KH" + System.currentTimeMillis() % 100000);
        }
        if (customer.getStatus() == null) customer.setStatus("合作中");
        if (customer.getCreditUsed() == null) customer.setCreditUsed(java.math.BigDecimal.ZERO);
        return super.create(customer);
    }

    @GetMapping("/customers")
    @Override
    public PageResult<Customer> list(@RequestParam Map<String, Object> params) {
        return super.list(params);
    }

    @PutMapping("/customers/{id}")
    public Customer updateCustomer(@PathVariable Long id, @RequestBody Customer customer) {
        return update(id, customer);
    }

    @DeleteMapping("/customers/{id}")
    public Map<String, Object> deleteCustomer(@PathVariable Long id) {
        return delete(id);
    }

    @GetMapping("/customers/{id}")
    public Map<String, Object> customerDetail(@PathVariable Long id) {
        Customer customer = customerMapper.selectById(id);
        if (customer == null) throw com.quantaida.wuliu.common.exception.BizException.notFound("客户不存在");
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("customer", customer);
        m.put("contacts", contactMapper.selectList(new LambdaQueryWrapper<Contact>()
                .eq(Contact::getCustomerId, id).orderByDesc(Contact::getIsDefault)));
        m.put("addresses", addressMapper.selectList(new LambdaQueryWrapper<AddressBook>()
                .eq(AddressBook::getCustomerId, id)));
        m.put("agreements", agreementMapper.selectList(new LambdaQueryWrapper<Agreement>()
                .eq(Agreement::getCustomerId, id).orderByDesc(Agreement::getId)));
        m.put("followups", followupMapper.selectList(new LambdaQueryWrapper<Followup>()
                .eq(Followup::getCustomerId, id).orderByDesc(Followup::getId)));
        return m;
    }

    @PostMapping("/customers/{id}/contacts")
    public Contact addContact(@PathVariable Long id, @RequestBody Contact contact) {
        contact.setId(null);
        contact.setCustomerId(id);
        contactMapper.insert(contact);
        return contact;
    }

    @PutMapping("/contacts/{id}")
    public Contact updateContact(@PathVariable Long id, @RequestBody Contact contact) {
        contact.setId(id);
        contactMapper.updateById(contact);
        return contactMapper.selectById(id);
    }

    @DeleteMapping("/contacts/{id}")
    public Map<String, Object> deleteContact(@PathVariable Long id) {
        contactMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @PostMapping("/customers/{id}/addresses")
    public AddressBook addAddress(@PathVariable Long id, @RequestBody AddressBook address) {
        address.setId(null);
        address.setCustomerId(id);
        addressMapper.insert(address);
        return address;
    }

    @DeleteMapping("/addresses/{id}")
    public Map<String, Object> deleteAddress(@PathVariable Long id) {
        addressMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/agreements")
    public PageResult<Agreement> agreements(@RequestParam Map<String, Object> params) {
        return superList(agreementMapper, params, Agreement.class, new String[]{"origin", "destination", "vehicleType"});
    }

    @PostMapping("/agreements")
    public Agreement createAgreement(@RequestBody Agreement agreement) {
        agreement.setId(null);
        if (agreement.getStatus() == null) agreement.setStatus("生效中");
        agreementMapper.insert(agreement);
        auditService.log("新增协议价", agreement.getOrigin() + "→" + agreement.getDestination(),
                agreement.getPrice() + " 元");
        return agreement;
    }

    @PutMapping("/agreements/{id}")
    public Agreement updateAgreement(@PathVariable Long id, @RequestBody Agreement agreement) {
        agreement.setId(id);
        agreementMapper.updateById(agreement);
        return agreementMapper.selectById(id);
    }

    @DeleteMapping("/agreements/{id}")
    public Map<String, Object> deleteAgreement(@PathVariable Long id) {
        agreementMapper.deleteById(id);
        return Map.of("deleted", true);
    }

    @GetMapping("/leads")
    public PageResult<Lead> leads(@RequestParam Map<String, Object> params) {
        return superList(leadMapper, params, Lead.class, new String[]{"name", "company", "phone"});
    }

    @PostMapping("/leads")
    public Lead createLead(@RequestBody Lead lead) {
        lead.setId(null);
        if (lead.getStatus() == null) lead.setStatus("待分配");
        leadMapper.insert(lead);
        return lead;
    }

    @PostMapping("/leads/{id}/assign")
    public Lead assignLead(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) throw com.quantaida.wuliu.common.exception.BizException.notFound("线索不存在");
        lead.setAssigneeId(body.get("userId") == null ? null : Long.valueOf(String.valueOf(body.get("userId"))));
        lead.setAssigneeName(String.valueOf(body.getOrDefault("assigneeName", "")));
        lead.setStatus("跟进中");
        leadMapper.updateById(lead);
        return lead;
    }

    @PostMapping("/leads/{id}/convert")
    public Customer convertLead(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        Lead lead = leadMapper.selectById(id);
        if (lead == null) throw com.quantaida.wuliu.common.exception.BizException.notFound("线索不存在");
        Customer customer = new Customer();
        customer.setCode("KH" + System.currentTimeMillis() % 100000);
        customer.setName(String.valueOf(body.getOrDefault("name", lead.getCompany() == null || lead.getCompany().isBlank()
                ? lead.getName() : lead.getCompany())));
        customer.setRecordType("企业客户");
        customer.setContactName(lead.getName());
        customer.setContactPhone(lead.getPhone());
        customer.setSettleType("现结");
        customer.setCreditLimit(java.math.BigDecimal.ZERO);
        customer.setCreditUsed(java.math.BigDecimal.ZERO);
        customer.setSource(lead.getSource());
        customer.setStatus("合作中");
        customerMapper.insert(customer);
        lead.setStatus("已转化");
        leadMapper.updateById(lead);
        return customer;
    }

    @GetMapping("/followups")
    public PageResult<Followup> followups(@RequestParam Map<String, Object> params) {
        return superList(followupMapper, params, Followup.class, new String[]{"content"});
    }

    @PostMapping("/followups")
    public Followup createFollowup(@RequestBody Followup followup) {
        followup.setId(null);
        if (followup.getOperator() == null) followup.setOperator(com.quantaida.wuliu.common.context.TenantContext.username());
        if (followup.getStatus() == null) followup.setStatus("已完成");
        followupMapper.insert(followup);
        return followup;
    }

    private <T> PageResult<T> superList(com.baomidou.mybatisplus.core.mapper.BaseMapper<T> mapper,
                                        Map<String, Object> params, Class<T> clazz, String[] keyword) {
        var qw = new com.baomidou.mybatisplus.core.conditions.query.QueryWrapper<T>();
        String kw = params.get("keyword") == null ? "" : String.valueOf(params.get("keyword"));
        if (!kw.isBlank()) {
            qw.and(w -> {
                for (int i = 0; i < keyword.length; i++) {
                    String col = camel(keyword[i]);
                    if (i == 0) w.like(col, kw);
                    else w.or().like(col, kw);
                }
            });
        }
        for (Map.Entry<String, Object> e : params.entrySet()) {
            String key = e.getKey();
            if (List.of("page", "size", "keyword", "sort", "order").contains(key)) continue;
            if (e.getValue() == null || String.valueOf(e.getValue()).isBlank()) continue;
            if (List.of("customerId", "status", "recordType").contains(key)) {
                qw.eq(camel(key), e.getValue());
            }
        }
        qw.orderByDesc("id");
        var page = mapper.selectPage(new com.baomidou.mybatisplus.extension.plugins.pagination.Page<>(
                pl(params.get("page"), 1), pl(params.get("size"), 10)), qw);
        return PageResult.of(page.getRecords(), page.getTotal(), page.getCurrent(), page.getSize());
    }

    private static long pl(Object v, long def) {
        try {
            return v == null ? def : Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private static String camel(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isUpperCase(c)) sb.append('_').append(Character.toLowerCase(c));
            else sb.append(c);
        }
        return sb.toString();
    }
}
