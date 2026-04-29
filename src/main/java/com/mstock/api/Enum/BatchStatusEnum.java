package com.mstock.api.Enum;

/**
 * Enum for Batch Status
 * ACTIVE: Normal stock available for use
 * QUARANTINE: Under recall/investigation - blocked from withdrawals
 * RETIRED: Expired or removed from service - not available
 */
public enum BatchStatusEnum {
    ACTIVE,
    QUARANTINE,
    RETIRED
}
