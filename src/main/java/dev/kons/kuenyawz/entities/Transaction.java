package dev.kons.kuenyawz.entities;

import dev.kons.kuenyawz.utils.idgenerator.SnowFlakeIdValue;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
public class Transaction extends Auditables {
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

	@Column(name = "invoice_link")
	private String invoiceLink;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private TransactionStatus status;

	@Column(name = "finalized_at")
	private LocalDateTime finalizedAt;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "account_id", nullable = false)
	private Account account;

	public enum TransactionStatus {
		PENDING, EXPIRED, SUCCESS, CANCELLED
	}
}
