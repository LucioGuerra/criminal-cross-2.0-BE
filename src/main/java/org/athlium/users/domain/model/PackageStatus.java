package org.athlium.users.domain.model;

import org.athlium.shared.exception.BadRequestException;

public enum PackageStatus {

    ACTIVE(1),
    EXPIRING(2),
    INACTIVE(3),
    NO_PACKAGE(4);

    private final int priority;

    PackageStatus(int priority) {
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public static PackageStatus fromString(String status) {
        if (status == null || status.isBlank()) {
            throw new BadRequestException("Package status must not be empty");
        }
        try {
            return PackageStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid package status: " + status
                    + ". Valid values are: ACTIVE, EXPIRING, INACTIVE, NO_PACKAGE");
        }
    }
}
