package me.danielx.api.plaid;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PlaidWebhookEventRepository extends JpaRepository<PlaidWebhookEvent, Long> {
  Optional<PlaidWebhookEvent> findByDedupeHash(String dedupeHash);
}
