package club.boyuan.official.teammatching.service;

import club.boyuan.official.teammatching.dto.request.contact.ContactExchangeRequest;
import club.boyuan.official.teammatching.dto.request.contact.ContactExchangeRespondRequest;
import club.boyuan.official.teammatching.dto.response.contact.ContactExchangeResponse;
import club.boyuan.official.teammatching.dto.response.contact.ContactInfoResponse;

import java.util.Map;

/**
 * 联系方式交换相关服务
 */
public interface ContactService {

    ContactExchangeResponse requestExchange(Integer requesterId, ContactExchangeRequest request);

    Map<String, Object> respondExchange(Integer operatorId, Integer exchangeId, ContactExchangeRespondRequest request);

    ContactInfoResponse getContactInfo(Integer operatorId, Integer targetUserId);
}

