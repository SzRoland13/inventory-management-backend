package dev.roland.inventory_management_backend.common.async;

/** Broker destination selected after a consumer has processed a job attempt. */
public enum AsyncJobDestination {
  RETRY {
    @Override
    public String routingKey(final AsyncJobType jobType) {
      return jobType.getRetryRoutingKey();
    }
  },
  DEAD_LETTER {
    @Override
    public String routingKey(final AsyncJobType jobType) {
      return jobType.getDeadLetterRoutingKey();
    }
  };

  /**
   * Returns the broker routing key for the job and destination.
   *
   * @param jobType kind of asynchronous job
   * @return broker routing key
   */
  public abstract String routingKey(final AsyncJobType jobType);
}
