package com.senity.waved.domain.paymentRecord.service;

import com.senity.waved.domain.challengeGroup.entity.ChallengeGroup;
import com.senity.waved.domain.challengeGroup.service.ChallengeGroupUtil;
import com.senity.waved.domain.member.entity.Member;
import com.senity.waved.domain.member.service.MemberUtil;
import com.senity.waved.domain.myChallenge.entity.MyChallenge;
import com.senity.waved.domain.myChallenge.repository.MyChallengeRepository;
import com.senity.waved.domain.myChallenge.service.MyChallengeUtil;
import com.senity.waved.domain.paymentRecord.dto.request.PaymentRecordRequestDto;
import com.senity.waved.domain.paymentRecord.entity.PaymentRecord;
import com.senity.waved.domain.paymentRecord.entity.PaymentStatus;
import com.senity.waved.domain.paymentRecord.exception.DepositAmountNotMatchException;
import com.senity.waved.domain.paymentRecord.exception.MemberAndMyChallengeNotMatchException;
import com.senity.waved.domain.paymentRecord.exception.PaymentRecordExistException;
import com.senity.waved.domain.paymentRecord.repository.PaymentRecordRepository;
import com.siot.IamportRestClient.IamportClient;
import com.siot.IamportRestClient.exception.IamportResponseException;
import com.siot.IamportRestClient.request.CancelData;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.Optional;


@Slf4j
@Service
@AllArgsConstructor
public class PaymentRecordServiceImpl implements PaymentRecordService {

    private final MyChallengeRepository myChallengeRepository;
    private final PaymentRecordRepository paymentRecordRepository;

    private final MemberUtil memberUtil;
    private final ChallengeGroupUtil challengeGroupUtil;
    private final MyChallengeUtil myChallengeUtil;
    private final IamportClient api;

    @Override
    @Transactional
    public void validateAndSavePaymentRecord(String email, Long myChallengeId, PaymentRecordRequestDto requestDto) {
        Member member = memberUtil.getByEmail(email);
        MyChallenge myChallenge = myChallengeUtil.getById(myChallengeId);
        validateMember(member, myChallenge);

        if (!myChallenge.getDeposit().equals(requestDto.getDeposit())) {
            cancelChallengePayment(email, myChallengeId);
            myChallengeRepository.deleteById(myChallengeId);
            throw new DepositAmountNotMatchException("마이 챌린지의 예치금과 결제 금액이 일치하지 않습니다.");
        }
        savePaymentRecord(myChallenge, member, PaymentStatus.APPLIED);
        myChallenge.updateImpUid(requestDto.getImp_uid());
        myChallenge.updateIsPaid(true);
        myChallengeRepository.save(myChallenge);
    }

    @Override
    @Transactional
    public void cancelChallengePayment(String email, Long myChallengeId) {
        Member member = memberUtil.getByEmail(email);
        MyChallenge myChallenge = myChallengeUtil.getById(myChallengeId);

        validateMember(member, myChallenge);
        cancelImportPayment(String.valueOf(myChallenge.getImpUid()));

        savePaymentRecord(myChallenge, member, PaymentStatus.CANCELED);
        myChallengeRepository.delete(myChallenge);
    }

    @Override
    @Transactional
    public String checkDepositRefundedOrNot(String email, Long myChallengeId) {
        Member member = memberUtil.getByEmail(email);
        MyChallenge myChallenge = myChallengeUtil.getById(myChallengeId);
        ChallengeGroup group = challengeGroupUtil.getById(myChallenge.getChallengeGroupId());

        long days = ChronoUnit.DAYS.between(group.getStartDate(), group.getEndDate());
        int successCount = days > 10 ? 10 : 5;

        PaymentStatus status = myChallenge.getSuccessCount() < successCount ?
                PaymentStatus.FAIL : PaymentStatus.SUCCESS;

        String message = myChallenge.getSuccessCount() < successCount ?
                "챌린지 성공률을 달성하지 못해 예치금을 환급받지 못했습니다." : "챌린지 성공률을 달성해 예치금을 환급받았습니다.";

        if (status == PaymentStatus.SUCCESS && myChallenge.getDeposit() != 0) {
            cancelImportPayment(String.valueOf(myChallenge.getImpUid()));
        }

        myChallenge.updateIsRefundRequested();
        savePaymentRecord(myChallenge, member, status);
        return message;
    }

    @Transactional(isolation = Isolation.SERIALIZABLE)
    public void savePaymentRecord(MyChallenge myChallenge, Member member, PaymentStatus status) {
        checkIfPaymentRecordExist(member.getId(), myChallenge.getId(), status);
        ChallengeGroup group = challengeGroupUtil.getById(myChallenge.getChallengeGroupId());

        String groupTitle = group.getGroupTitle();
        updateGroupParticipantCount(group, status);

        PaymentRecord paymentRecord = PaymentRecord.of(status, member, myChallenge, groupTitle);
        paymentRecordRepository.save(paymentRecord);
    }

    private void checkIfPaymentRecordExist(Long memberId, Long myChallengeId, PaymentStatus paymentStatus) {
        Optional<PaymentRecord> paymentRecords = paymentRecordRepository.findByMemberIdAndMyChallengeIdAndPaymentStatus(memberId, myChallengeId, paymentStatus);
        if (paymentRecords.isPresent()) {
            throw new PaymentRecordExistException("이미 존재하는 결제 내역입니다.");
        }
    }

    private void cancelImportPayment(String impUid) {
        try {
            CancelData cancelData = new CancelData(impUid, true);
            api.cancelPaymentByImpUid(cancelData);
        } catch (IamportResponseException | IOException e) {
            throw new RuntimeException("결제 취소 중 오류가 발생했습니다.", e);
        }
    }

    private void validateMember(Member member, MyChallenge myChallenge) {
        if(!myChallenge.getMember().equals(member))
            throw new MemberAndMyChallengeNotMatchException("해당 멤버의 마이 챌린지가 아닙니다.");
    }

    private void updateGroupParticipantCount(ChallengeGroup group, PaymentStatus status) {
        if (status.equals(PaymentStatus.APPLIED)) {
            group.addParticipantCount();
        } else if (status.equals(PaymentStatus.CANCELED)) {
            group.subtractParticipantCount();
        }
    }
}