package net.workbook.mileage.model;

import android.os.Parcel;
import android.os.Parcelable;

public class DriveLocation implements Parcelable
{
	private String description;
	private String address;
	
	public DriveLocation(String description, String address)
	{
		this.description = description;
		this.address = address;
	}

	public DriveLocation(Parcel in)
	{
		this.description = in.readString();
		this.address = in.readString();
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return description;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeString(description);
		out.writeString(address);
	}
	
	public static final Parcelable.Creator<DriveLocation> CREATOR = new Parcelable.Creator<DriveLocation>() 
			{
				public DriveLocation createFromParcel(Parcel in)
				{
					return new DriveLocation(in);
				}
				public DriveLocation[] newArray(int size)
				{
					return new DriveLocation[size];
				}
			};
}
