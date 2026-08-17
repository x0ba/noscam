package me.danielx.api.plaid;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlaidItemRepository extends JpaRepository<PlaidItem, Long> {
  List<PlaidItem> findAllByUserIdOrderByConnectedAtDesc(Long userId);

  Optional<PlaidItem> findByPublicIdAndUserId(UUID publicId, Long userId);

  Optional<PlaidItem> findByPlaidItemId(String plaidItemId);
}
