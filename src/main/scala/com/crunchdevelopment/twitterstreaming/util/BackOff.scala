package com.crunchdevelopment.twitterstreaming.util

/**
 * BackOff
 *
 * This class implements the waiting strategy when errors arise, and trust me, they will.
 */
case class BackOff(var origBackOffTime: Long, capBackOffAt: Long) {
  var backOffTime = origBackOffTime

  def backOff = {
    Thread.sleep(backOffTime)
    backOffTime *= 2
    if(backOffTime > capBackOffAt) {
      backOffTime = capBackOffAt
    }
  }

  /**
   * After all errors are resolved (ie successful connection), we reset the sleeping counter.
   */
  def reset() = { backOffTime = origBackOffTime }
}
