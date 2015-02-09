package net.workbook.mileage.model;

import android.os.Parcel;
import android.os.Parcelable;

public class MileageType implements Parcelable
{
	private String comment;
	private int mileageRateId,compId;
	private boolean active;
	
	public MileageType(int mileageRateId,int compId,String comment,boolean active)
	{
		this.mileageRateId = mileageRateId;
		this.compId = compId;
		this.comment = comment;
		this.active = active;
	}
	
	public MileageType(Parcel in)
	{
		mileageRateId = in.readInt();
		compId = in.readInt();
		comment = in.readString();
		active = in.readByte() == 1;
	}
	
	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public int getMileageRateId() {
		return mileageRateId;
	}

	public void setMileageRateId(int mileageRateId) {
		this.mileageRateId = mileageRateId;
	}

	public int getCompId() {
		return compId;
	}

	public void setCompId(int compId) {
		this.compId = compId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	@Override
	public String toString() {
		return comment;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(mileageRateId);
		out.writeInt(compId);
		out.writeString(comment);
		out.writeByte((byte) (active ? 1 : 0));
	}
	
	public static final Parcelable.Creator<MileageType> CREATOR = new Parcelable.Creator<MileageType>() 
			{
				public MileageType createFromParcel(Parcel in)
				{
					return new MileageType(in);
				}
				public MileageType[] newArray(int size)
				{
					return new MileageType[size];
				}
			};

}
