package ca.etsmtl.gti785.lib.entity;

import android.support.annotation.Nullable;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PeerEntity {

    private static final String NAME_UUID_FORMAT = "%s:%s";
    private static final String ACCESSED_AT_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS";

    // Do not serialize online
    private transient boolean online;

    private UUID uuid;
    private String displayName;
    private String ipAddress;
    private Integer port;
    private LocationEntity location;
    private Date accessedAt;

    public PeerEntity(String displayName, String ipAddress, Integer port) {
        this.displayName = displayName;
        this.ipAddress = ipAddress;
        this.port = port;

        uuid = UUID.nameUUIDFromBytes(generateNameForUUID());
        location = new LocationEntity();
        online = false;
    }

    private byte[] generateNameForUUID() {
        return String.format(Locale.getDefault(), NAME_UUID_FORMAT, ipAddress, port).getBytes();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public Integer getPort() {
        return port;
    }

    public LocationEntity getLocation() {
        return location;
    }

    @Nullable
    public Date getAccessedAt() {
        return accessedAt;
    }

    public String getFormatedAccessedAt() {
        return String.format(Locale.getDefault(), ACCESSED_AT_FORMAT, accessedAt);
    }

    public void updateAccessedAt() {
        this.accessedAt = new Date();
    }

    public boolean isOnline() {
        return online;
    }

    public void setOnline(boolean online) {
        this.online = online;
    }
}
