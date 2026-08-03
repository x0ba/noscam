package me.danielx.api.common.accounts;

import me.danielx.api.plaid.PlaidItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, Long> {
  Page<Account> findAllByUserId(Long userId, Pageable pageable);

  Optional<Account> findByPublicIdAndUserId(UUID accountId, Long userId);

  List<Account> findAllByPlaidItem(PlaidItem plaidItem);

  Optional<Account> findByPlaidItemAndProviderAccountId(PlaidItem plaidItem, String providerAccountId);
}
