package net.workbook.mileage.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Car implements Parcelable
{
	private String name;
	private int id;
	
	public Car(int id,String name)
	{
		this.id = id;
		this.name = name;
	}
	
	public Car(Parcel in)
	{
		id = in.readInt();
		name = in.readString();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	@Override
	public String toString() {
		return id+" - "+name;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel out, int flags) {
		out.writeInt(id);
		out.writeString(name);
	}
	
	public static final Parcelable.Creator<Car> CREATOR = new Parcelable.Creator<Car>()
			{
				public Car createFromParcel(Parcel in)
				{
					return new Car(in);
				}
				public Car[] newArray(int size)
				{
					return new Car[size];
				}
			};
	
}
