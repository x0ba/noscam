package me.danielx.api.risk;

public interface RiskFactor {
  String key();

  FactorContribution evaluate(RiskFactorContext context);
}
