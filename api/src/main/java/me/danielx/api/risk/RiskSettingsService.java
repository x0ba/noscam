package me.danielx.api.risk;

import me.danielx.api.common.jobs.JobType;
import me.danielx.api.common.jobs.SyncJobService;
import me.danielx.api.risk.dto.RiskFactorConfigRequest;
import me.danielx.api.risk.dto.RiskSettingsResponse;
import me.danielx.api.risk.dto.UpdateRiskSettingsRequest;
import me.danielx.api.users.AuthenticatedUserNotFoundException;
import me.danielx.api.users.User;
import me.danielx.api.users.UserRepository;
import me.danielx.api.users.dto.AuthenticatedUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class RiskSettingsService {
  private final RiskSettingsRepository riskSettingsRepository;
  private final UserRepository userRepository;
  private final DefaultRiskSettingsFactory defaultRiskSettingsFactory;
  private final RiskSettingsValidator validator;
  private final SyncJobService syncJobService;

  public RiskSettingsService(
      RiskSettingsRepository riskSettingsRepository,
      UserRepository userRepository,
      DefaultRiskSettingsFactory defaultRiskSettingsFactory,
      RiskSettingsValidator validator,
      SyncJobService syncJobService) {
    this.riskSettingsRepository = riskSettingsRepository;
    this.userRepository = userRepository;
    this.defaultRiskSettingsFactory = defaultRiskSettingsFactory;
    this.validator = validator;
    this.syncJobService = syncJobService;
  }

  @Transactional
  public RiskSettings getOrCreate(User user) {
    return riskSettingsRepository
        .findByUserId(user.getId())
        .orElseGet(() -> riskSettingsRepository.save(defaultRiskSettingsFactory.create(user)));
  }

  @Transactional
  public RiskSettingsResponse getForCurrentUser(AuthenticatedUser currentUser) {
    User user = requireUser(currentUser);
    return RiskSettingsResponse.from(getOrCreate(user));
  }

  @Transactional
  public RiskSettingsResponse replaceForCurrentUser(
      AuthenticatedUser currentUser, UpdateRiskSettingsRequest request) {
    validator.validate(
        request.alertThreshold(),
        request.lowMax(),
        request.mediumMax(),
        request.factors().stream()
            .map(
                factor ->
                    new RiskSettingsValidator.FactorDraft(
                        factor.key(), factor.enabled(), factor.maxPoints(), factor.parameters()))
            .toList());

    User user = requireUser(currentUser);
    RiskSettings settings = getOrCreate(user);
    settings.setAlertThreshold(request.alertThreshold());
    settings.setLowMax(request.lowMax());
    settings.setMediumMax(request.mediumMax());
    settings.setEngineVersion(RiskEngine.ENGINE_VERSION);
    settings.setConfigVersion(settings.getConfigVersion() + 1);
    settings.replaceFactorConfigs(
        request.factors().stream().map(this::toConfig).toList());
    RiskSettings saved = riskSettingsRepository.save(settings);
    syncJobService.enqueue(
        user,
        null,
        JobType.SETTINGS_RESCORE,
        Map.of("configVersion", saved.getConfigVersion()));
    return RiskSettingsResponse.from(saved);
  }

  private RiskFactorConfig toConfig(RiskFactorConfigRequest request) {
    Map<String, Object> parameters =
        request.parameters() == null ? new LinkedHashMap<>() : new LinkedHashMap<>(request.parameters());
    return RiskFactorConfig.builder()
        .factorKey(request.key())
        .enabled(request.enabled())
        .maxPoints(request.maxPoints())
        .parameters(parameters)
        .build();
  }

  private User requireUser(AuthenticatedUser currentUser) {
    return userRepository
        .findById(currentUser.id())
        .orElseThrow(AuthenticatedUserNotFoundException::new);
  }
}
