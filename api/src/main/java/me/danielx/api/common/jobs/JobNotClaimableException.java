package me.danielx.api.common.jobs;

public class JobNotClaimableException extends RuntimeException {
  public JobNotClaimableException() {
    super("Sync job is already running or is not claimable");
  }
}
