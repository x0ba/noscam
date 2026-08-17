package me.danielx.api.plaid;

import me.danielx.api.common.jobs.JobType;
import me.danielx.api.common.jobs.SyncJobService;
import me.danielx.api.common.utils.Sha256Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.Optional;

@Service
public class PlaidWebhookService {
  private static final Logger log = LoggerFactory.getLogger(PlaidWebhookService.class);

  private final PlaidWebhookVerifier verifier;
  private final PlaidWebhookEventRepository eventRepository;
  private final PlaidItemRepository plaidItemRepository;
  private final SyncJobService syncJobService;
  private final ObjectMapper objectMapper;

  public PlaidWebhookService(
      PlaidWebhookVerifier verifier,
      PlaidWebhookEventRepository eventRepository,
      PlaidItemRepository plaidItemRepository,
      SyncJobService syncJobService,
      ObjectMapper objectMapper) {
    this.verifier = verifier;
    this.eventRepository = eventRepository;
    this.plaidItemRepository = plaidItemRepository;
    this.syncJobService = syncJobService;
    this.objectMapper = objectMapper;
  }

  @Transactional
  public void capture(String verificationHeader, String rawBody) {
    verifier.verify(verificationHeader, rawBody);
    JsonNode payload = objectMapper.readTree(rawBody);
    String webhookType = text(payload, "webhook_type");
    String webhookCode = text(payload, "webhook_code");
    String itemId = text(payload, "item_id");
    String dedupeHash = Sha256Util.sha256(rawBody);
    if (eventRepository.findByDedupeHash(dedupeHash).isPresent()) {
      return;
    }
    Optional<PlaidItem> item =
        itemId == null ? Optional.empty() : plaidItemRepository.findByPlaidItemId(itemId);
    PlaidWebhookEvent event =
        PlaidWebhookEvent.builder()
            .dedupeHash(dedupeHash)
            .providerEventId(text(payload, "webhook_id"))
            .plaidItem(item.orElse(null))
            .webhookType(webhookType == null ? "UNKNOWN" : webhookType)
            .webhookCode(webhookCode == null ? "UNKNOWN" : webhookCode)
            .payload(objectMapper.convertValue(payload, new TypeReference<Map<String, Object>>() {}))
            .build();
    try {
      eventRepository.saveAndFlush(event);
    } catch (DataIntegrityViolationException ex) {
      return;
    }
    if (item.isEmpty()) {
      log.info("Stored Plaid webhook for unknown item");
      return;
    }
    PlaidItem plaidItem = item.get();
    if ("ITEM".equalsIgnoreCase(webhookType)
        && ("ERROR".equalsIgnoreCase(webhookCode) || "USER_PERMISSION_REVOKED".equalsIgnoreCase(webhookCode))) {
      plaidItem.setStatus(PlaidItemStatus.ERROR);
      plaidItem.setLastErrorCode(webhookCode);
      return;
    }
    if ("TRANSACTIONS".equalsIgnoreCase(webhookType)) {
      syncJobService.enqueue(
          plaidItem.getUser(),
          plaidItem,
          JobType.PLAID_TRANSACTIONS_SYNC,
          Map.of("webhookCode", webhookCode == null ? "" : webhookCode));
    }
  }

  private static String text(JsonNode node, String field) {
    JsonNode value = node.path(field);
    return value.isMissingNode() || value.isNull() ? null : value.asText();
  }
}
