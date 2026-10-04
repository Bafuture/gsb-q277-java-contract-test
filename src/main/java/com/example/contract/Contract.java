package com.example.contract;

import java.util.List;

/**
 * A named, versioned set of contract cases. The same contract can be
 * verified against any number of implementations. Bumping the version
 * invalidates previous verification records.
 */
public final class Contract<I, O> {

    private final String name;
    private final int version;
    private final List<ContractCase<I, O>> cases;

    public Contract(String name, int version, List<ContractCase<I, O>> cases) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("contract name must not be blank");
        }
        if (version < 1) {
            throw new IllegalArgumentException("contract version must be >= 1");
        }
        if (cases == null || cases.isEmpty()) {
            throw new IllegalArgumentException("contract must contain at least one case");
        }
        this.name = name;
        this.version = version;
        this.cases = List.copyOf(cases);
    }

    public String name() {
        return name;
    }

    public int version() {
        return version;
    }

    public List<ContractCase<I, O>> cases() {
        return cases;
    }

    /** Returns a copy of this contract at a new version, keeping the same cases. */
    public Contract<I, O> withVersion(int newVersion) {
        return new Contract<>(name, newVersion, cases);
    }
}
