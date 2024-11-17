package dev.realtards.kuenyawz.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import dev.realtards.kuenyawz.utils.idgenerator.SnowFlakeIdValue;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
//@EqualsAndHashCode(callSuper = true, exclude = "product")
//@ToString(callSuper = true, exclude = "product")
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class Transactions extends Auditables {
    @Id
    @SnowFlakeIdValue(name = "transaction_id")
    @Column(name = "transaction_id", columnDefinition = "BIGINT", updatable = false, nullable = false)
    private Long transactionId;

    @Setter
    @Getter
    @Column(name = "reference_id", unique = true, nullable = false)
    private UUID referenceId;

    @Column
    private BigDecimal amount;

    @Setter
    @Getter
    @Column(name = "invoice_link")
    private String invoiceLink;

    public enum TransactionStatus {
        DEPOSIT, PAID
    }

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TransactionStatus status;

    @Column(name = "finalized_at")
    private LocalDateTime finalizedAt;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;
}
