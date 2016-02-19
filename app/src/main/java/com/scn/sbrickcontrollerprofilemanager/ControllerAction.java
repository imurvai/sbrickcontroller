package com.scn.sbrickcontrollerprofilemanager;

import android.content.SharedPreferences;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

/**
 * ControllerAction class.
 */
public final class ControllerAction implements Parcelable {

    //
    // Private members.
    //

    private static final String TAG = ControllerAction.class.getSimpleName();

    private static final String SBrickAddressKey = "sbrick_address_key";
    private static final String ChannelKey = "channel_key";
    private static final String InvertKey = "invert_key";
    private static final String ToggleKey = "toggle_key";

    private String sbrickAddress;
    private int channel;
    private boolean invert;
    private boolean toggle;

    //
    // Constructor.
    //

    /**
     * Creates a new instance of the ControllerAction class.
     * @param sbrickAddress is the address of the SBrick.
     * @param channel is the channel.
     * @param invert is true if value has to be inverted.
     * @param toggle is true if the action is a toggle switch.
     */
    public ControllerAction(String sbrickAddress, int channel, boolean invert, boolean toggle) {
        Log.i(TAG, "ControllerAction...");

        validateSBrickAddress(sbrickAddress);
        validateChannel(channel);

        this.sbrickAddress = sbrickAddress;
        this.channel = channel;
        this.invert = invert;
        this.toggle = toggle;
    }

    ControllerAction(SharedPreferences prefs, String profileName, String controllerActionId, int controllerActionIndex) {
        Log.i(TAG, "ControllerAction from shared preferences...");

        String keyBase = profileName + "_" + controllerActionId + "_" + controllerActionIndex + "_";
        sbrickAddress = prefs.getString(keyBase + SBrickAddressKey, "");
        channel = prefs.getInt(keyBase + ChannelKey, 0);
        invert = prefs.getInt(keyBase + InvertKey, 0) != 0;
        toggle = prefs.getInt(keyBase + ToggleKey, 0) != 0;

        validateSBrickAddress(sbrickAddress);
        validateChannel(channel);
    }

    ControllerAction(Parcel parcel) {
        Log.i(TAG, "ControllerAction from parcel...");

        sbrickAddress = parcel.readString();
        channel = parcel.readInt();
        invert = parcel.readInt() != 0;
        toggle = parcel.readInt() != 0;

        validateSBrickAddress(sbrickAddress);
        validateChannel(channel);
    }

    //
    // API
    //

    /**
     * Gets the SBrick address.
     * @return The SBrick address.
     */
    public String getSBrickAddress() { return sbrickAddress; }

    /**
     * Gets the channel.
     * @return The channel.
     */
    public int getChannel() { return channel; }

    /**
     * Gets the value indicating if the value has to be inverted.
     * @return True if the value has to be inverted, false otherwise.
     */
    public boolean getInvert() { return invert; }

    /**
     * Gets the value indicating if the action is a toggle switch.
     * @return
     */
    public boolean getToggle() { return toggle; }

    //
    // Object overrides
    //

    @Override
    public String toString() {
        return sbrickAddress + " - " + channel + " - " + (invert ? "invert" : "not-invert") + " - " + (toggle ? "toggle" : "not-toggle");
    }

    @Override
    public boolean equals(Object o) {

        if (o == null)
            return false;

        if (!(o instanceof ControllerAction))
            return false;

        ControllerAction other = (ControllerAction)o;

        return sbrickAddress.equals(other.getSBrickAddress()) &&
                channel == other.getChannel() &&
                invert == other.getInvert() &&
                toggle == other.getToggle();
    }

    @Override
    public int hashCode() {
        return sbrickAddress.hashCode() ^ (channel * 377) ^ (invert ? 1 : -1) ^ (invert ? 2 : -2);
    }

    //
    // Internal API
    //

    void saveToPreferences(SharedPreferences.Editor editor, String profileName, String controllerActionId, int controllerActionIndex) {
        Log.i(TAG, "SaveToPreferences...");

        String keyBase = profileName + "_" + controllerActionId + "_" + controllerActionIndex + "_";
        editor.putString(keyBase + SBrickAddressKey, sbrickAddress);
        editor.putInt(keyBase + ChannelKey, channel);
        editor.putInt(keyBase + InvertKey, invert ? 1 : 0);
        editor.putInt(keyBase + ToggleKey, toggle ? 1 : 0);
    }

    //
    // Parcelable methods
    //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Log.i(TAG, "writeToParcel...");

        dest.writeString(sbrickAddress);
        dest.writeInt(channel);
        dest.writeInt(invert ? 1 : 0);
        dest.writeInt(toggle ? 1 : 0);
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new ControllerAction(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new ControllerAction[size];
        }
    };

    //
    // Private methods
    //

    private void validateSBrickAddress(String address) {
        if (address == null || address.length() == 0)
            throw new RuntimeException("SBrick address can't be null or empty.");
    }

    private void validateChannel(int channel) {
        if (channel < 0 || 3 < channel)
            throw new RuntimeException("Channel must be in range [0, 3].");
    }
}
