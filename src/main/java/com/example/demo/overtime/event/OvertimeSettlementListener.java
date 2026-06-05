package com.example.demo.overtime.event;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OvertimeSettlementListener {

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOverTimeSettled(OvertimeSettledEvent event) {
        try {
            log.info("SMS sent to workerid={}: Your overtime for {} of amount ₹{} has been settled.",
                    event.getWorkerId(), event.getMonth(), event.getSettledAmount());
        } catch (Exception ex) {
            log.error(
                    "Failed to send overtime settlement SMS for workerId={}",
                    event.getWorkerId(),
                    ex);
        }
    }
}
