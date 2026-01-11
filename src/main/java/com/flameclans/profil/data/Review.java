package com.flameclans.profil.data;

import java.sql.Timestamp;
import java.util.UUID;

public class Review {
    private final int reviewId;
    private final UUID targetUuid;
    private final UUID reviewerUuid;
    private final String reviewerName;
    private final int rating;
    private final String comment;
    private final Timestamp timestamp;
    private final String serverName;

    public Review(int reviewId, UUID targetUuid, UUID reviewerUuid, String reviewerName, int rating, String comment, Timestamp timestamp, String serverName) {
        this.reviewId = reviewId;
        this.targetUuid = targetUuid;
        this.reviewerUuid = reviewerUuid;
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
        this.timestamp = timestamp;
        this.serverName = serverName;
    }

    public int getReviewId() {
        return reviewId;
    }

    public UUID getTargetUuid() {
        return targetUuid;
    }

    public UUID getReviewerUuid() {
        return reviewerUuid;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public String getServerName() {
        return serverName;
    }
}
