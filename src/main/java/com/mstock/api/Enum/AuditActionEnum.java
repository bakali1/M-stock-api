package com.mstock.api.Enum;

/**
 * Enum for Audit Action Types
 * CREATE: New record created
 * UPDATE: Existing record modified
 * DELETE: Record deleted (soft delete)
 */
public enum AuditActionEnum {
    CREATE,
    UPDATE,
    DELETE
}
