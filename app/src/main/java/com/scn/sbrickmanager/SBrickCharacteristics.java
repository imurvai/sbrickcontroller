package com.scn.sbrickmanager;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * SBrick characteristics.
 */
public final class SBrickCharacteristics implements Parcelable {

    //
    // Private members
    //

    private static final String NA = "N/A";

    private String address = NA;
    private String deviceName = NA;
    private String appearance = NA;
    private String modelNumber = NA;
    private String firmwareRevision = NA;
    private String hardwareRevision = NA;
    private String softwareRevision = NA;
    private String manufacturerName = NA;

    //
    // Constructors
    //

    SBrickCharacteristics() {
    }

    SBrickCharacteristics(Parcel parcel) {
        address = parcel.readString();
        deviceName = parcel.readString();
        appearance = parcel.readString();
        modelNumber = parcel.readString();
        firmwareRevision = parcel.readString();
        hardwareRevision = parcel.readString();
        softwareRevision = parcel.readString();
        manufacturerName = parcel.readString();
    }

    //
    // API
    //

    public String getAddress() { return address; }
    void setAddress(String address) { this.address = address; }

    public String getDeviceName() { return deviceName; }
    void setDeviceName(String deviceName) { this.deviceName = deviceName; }

    public String getAppearance() { return appearance; }
    void setAppearance(String appearance) { this.appearance = appearance; }

    public String getModelNumber() { return modelNumber; }
    void setModelNumber(String modelNumber) { this.modelNumber = modelNumber; }

    public String getFirmwareRevision() { return firmwareRevision; }
    void setFirmwareRevision(String firmwareRevision) { this.firmwareRevision = firmwareRevision; }

    public String getHardwareRevision() { return hardwareRevision; }
    void setHardwareRevision(String hardwareRevision) { this.hardwareRevision = hardwareRevision; }

    public String getSoftwareRevision() { return softwareRevision; }
    void setSoftwareRevision(String softwareRevision) { this.softwareRevision = softwareRevision; }

    public String getManufacturerName() { return manufacturerName; }
    void setManufacturerName(String manufacturerName) { this.manufacturerName = manufacturerName; }

    //
    // Parceable overrides
    //

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(address);
        dest.writeString(deviceName);
        dest.writeString(appearance);
        dest.writeString(modelNumber);
        dest.writeString(firmwareRevision);
        dest.writeString(hardwareRevision);
        dest.writeString(softwareRevision);
        dest.writeString(manufacturerName);
    }

    public static final Parcelable.Creator<SBrickCharacteristics> CREATOR = new Parcelable.Creator() {

        @Override
        public Object createFromParcel(Parcel source) {
            return new SBrickCharacteristics(source);
        }

        @Override
        public Object[] newArray(int size) {
            return new Object[0];
        }
    };
}
