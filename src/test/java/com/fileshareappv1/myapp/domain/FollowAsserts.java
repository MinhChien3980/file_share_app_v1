package com.fileshareappv1.myapp.domain;

import static org.assertj.core.api.Assertions.assertThat;

public class FollowAsserts {

    /**
     * Asserts that the entity has all properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFollowAllPropertiesEquals(Follow expected, Follow actual) {
        assertFollowAutoGeneratedPropertiesEquals(expected, actual);
        assertFollowAllUpdatablePropertiesEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all updatable properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFollowAllUpdatablePropertiesEquals(Follow expected, Follow actual) {
        assertFollowUpdatableFieldsEquals(expected, actual);
        assertFollowUpdatableRelationshipsEquals(expected, actual);
    }

    /**
     * Asserts that the entity has all the auto generated properties (fields/relationships) set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFollowAutoGeneratedPropertiesEquals(Follow expected, Follow actual) {
        assertThat(actual)
            .as("Verify Follow auto generated properties")
            .satisfies(a -> assertThat(a.getId()).as("check id").isEqualTo(expected.getId()));
    }

    /**
     * Asserts that the entity has all the updatable fields set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFollowUpdatableFieldsEquals(Follow expected, Follow actual) {
        assertThat(actual)
            .as("Verify Follow relevant properties")
            .satisfies(a -> assertThat(a.getCreatedAt()).as("check createdAt").isEqualTo(expected.getCreatedAt()));
    }

    /**
     * Asserts that the entity has all the updatable relationships set.
     *
     * @param expected the expected entity
     * @param actual the actual entity
     */
    public static void assertFollowUpdatableRelationshipsEquals(Follow expected, Follow actual) {
        // empty method
    }
}
