package com.senity.waved.domain.paymentRecord.service;

import com.senity.waved.domain.paymentRecord.dto.request.PaymentRecordRequestDto;

public interface PaymentRecordService {
    void validateAndSavePaymentRecord(String email, Long myChallengeId, PaymentRecordRequestDto requestDto);
    String checkDepositRefundedOrNot(String email, Long myChallengeId);
    void cancelChallengePayment(String email, Long myChallengeId);
}