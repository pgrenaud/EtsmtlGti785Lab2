package ca.etsmtl.gti785.lib.entity;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class PeerEntity implements Comparable<PeerEntity> {

    private static final String NAME_UUID_FORMAT = "%s:%s";
    private static final String ADDRESS_FORMAT = "%s:%s";
    private static final String ACCESSED_AT_FORMAT = "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS";

    // Do not serialize accessedAt or online
    private transient Date accessedAt;
    private transient boolean online;

    private UUID uuid;
    @SerializedName("name")
    private String displayName;
    @SerializedName("ip")
    private String ipAddress;
    private Integer port;
    @SerializedName("loc")
    private LocationEntity location;

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

    public UUID getUUID() {
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

    public String getHost() {
        return String.format(Locale.getDefault(), ADDRESS_FORMAT, ipAddress, port);
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

    @Override
    public int compareTo(@NonNull PeerEntity peerEntity) {
        return getDisplayName().compareTo(peerEntity.getDisplayName());
    }

    public String encode() {
        Gson gson = new Gson();

        return gson.toJson(this);
    }

    public static PeerEntity decode(String json) throws JsonSyntaxException {
        Gson gson = new Gson();

        return gson.fromJson(json, PeerEntity.class);
    }
}
