package me.danielx.api.transactions;

import me.danielx.api.common.accounts.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository
    extends JpaRepository<Transaction, Long>, JpaSpecificationExecutor<Transaction> {
  Optional<Transaction> findByPublicIdAndUserId(UUID publicId, Long userId);

  Optional<Transaction> findByAccountAndSourceTypeAndExternalId(
      Account account, SourceType sourceType, String externalId);

  List<Transaction> findByUserIdAndStatusAndPostedAtGreaterThanEqual(
      Long userId, TransactionStatus status, Instant postedAfter);

  @Query(
      """
      select t from Transaction t
      where t.account.id = :accountId
        and t.status = me.danielx.api.transactions.TransactionStatus.ACTIVE
        and t.postedAt >= :since
      order by t.postedAt desc
      """)
  List<Transaction> findActiveHistory(
      @Param("accountId") Long accountId, @Param("since") Instant since);

  @Query(
      """
      select t from Transaction t
      where t.user.id = :userId
        and t.status = me.danielx.api.transactions.TransactionStatus.ACTIVE
        and t.postedAt >= :since
      order by t.postedAt desc
      """)
  List<Transaction> findActiveUserHistory(
      @Param("userId") Long userId, @Param("since") Instant since);
}
